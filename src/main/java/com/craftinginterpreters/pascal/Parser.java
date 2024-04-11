package com.craftinginterpreters.pascal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.craftinginterpreters.pascal.TokenType.*;


public class Parser {
    private static final boolean DEBUG = false;
    private final boolean synchronize;

    static class ParseError extends RuntimeException {
        ParseError(String message) {
            super(message);
        }
    }

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens)  {
        this(tokens, true);
    }

    Parser(List<Token> tokens, boolean synchronize)  {
        this.tokens = tokens;
        this.synchronize = synchronize;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    protected Expr expression() {
        return assignment();
    }

    private Stmt declaration() {
        try {
            if (match(CLASS)) return classDeclaration();
            if (match(FUNCTION)) return function("function");
            if (match(VAR)) return varDeclaration();
            return statement();
        }
        catch (ParseError error) {
            if (!this.synchronize) throw error;

            synchronize();
        }
        return null;
    }

    private Stmt classDeclaration() {
        var name = consume(IDENTIFIER, "Expect class name.");

        Expr.Variable superclass = null;
        if (match(LEFT_PAREN)) {
            consume(IDENTIFIER, "Expect superclass name.");
            superclass = new Expr.Variable(previous());
            consume(RIGHT_PAREN, "Expect ')' after superclass name.");
        }
        consume(SEMICOLON, "Expect ';' after class declaration.");
        consume(BEGIN, "Expect 'begin' before class body.");

        List<Stmt.Function> methods = new ArrayList<>();
        while (!check(END) && !isAtEnd()) {
            methods.add(function("method"));
        }
        consume(END, "Expect 'end' after class body.");

        return new Stmt.Class(name, superclass, methods);
    }

    private Stmt statement() {
        if (match(FOR)) return forStatement();
        if (match(IF)) return ifStatement();
        if (match(PRINT)) return printStatement();
        if (match(EXIT)) return exitStatement();
        if (match(WHILE)) return whileStatement();
        if (match(BEGIN)) return new Stmt.Block(block());

        return expressionStatement();
    }

    private Stmt forStatement() {
        Stmt initializer;
        if (match(SEMICOLON)) {
            initializer = null;
        }
        else if (match(VAR)) {
            initializer = varDeclaration();
        }
        else {
            initializer = expressionStatement();
        }

        Expr condition = null;
        if (!check(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "Expect ';' after loop condition");

        Expr increment = null;
        if (!check(SEMICOLON)) {
            increment = expression();
        }
        consume(DO, "Expect 'do' after for clauses.");
        Stmt body = statement();

        if (increment != null) {
            body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));
        }

        if (condition == null) condition = new Expr.Literal(true);
        body = new Stmt.While(condition, body);

        if (initializer != null) {
            body = new Stmt.Block(Arrays.asList(initializer, body));
        }
        return body;
    }

    private Stmt ifStatement() {
        Expr condition = expression();
        consume(THEN, "Expect 'then' after if condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt printStatement() {
        Expr value = expression();

        consume(SEMICOLON, "Expect ';' after value.");

        return new Stmt.Print(value);
    }

    private Stmt exitStatement() {
        var keyword = previous();
        Expr value = null;
        if (!check(SEMICOLON)) {
            value = expression();
        }

        consume(SEMICOLON, "Expect ';' after exit value.");
        return new Stmt.Return(keyword, value);
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(ASSIGN)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");

        return new Stmt.Var(name, initializer);
    }

    private Stmt whileStatement() {
        Expr condition = expression();
        consume(DO, "Expect 'do' after condition.");

        Stmt body = statement();

        return new Stmt.While(condition, body);
    }

    private Stmt expressionStatement() {
        Expr value = expression();

        consume(SEMICOLON, "Expect ';' after value.");

        return new Stmt.Expression(value);
    }

    private Stmt.Function function(String kind) {
       var name = consume(IDENTIFIER, "Expect " + kind + " name.");

       List<Token> parameters = new ArrayList<>();
       if (match(LEFT_PAREN)) {
           if (!check(RIGHT_PAREN)) {
               do {
                   if (parameters.size() >= 255) {
                       throw error(peek(), "Can't have more than 255 parameters.");
                   }
                   parameters.add(consume(IDENTIFIER, "Expect parameter name."));
               }
               while (match(COMMA));
           }
           consume(RIGHT_PAREN, "Expect ') after parameters.");
       }
       consume(SEMICOLON, "Expect ';'");
       consume(BEGIN, "Expect 'begin' before " + kind + " body.");
       List<Stmt> body = block();
       return new Stmt.Function(name, parameters, body);
    }

    private List<Stmt> block() {
       List<Stmt> statements = new ArrayList<>();

       while (!check(END) && !isAtEnd()) {
           statements.add(declaration());
       }

       consume(END, "Expect 'end' after block.");
       return statements;
    }

    private Expr assignment() {
        Expr expr = or();

        if (match(ASSIGN)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }
            else if (expr instanceof Expr.Get get) {
                return new Expr.Set(get.object, get.name, value);
            }
            throw error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr or() {
        Expr expr = and();

        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr and() {
        Expr expr = equality();

        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr equality() {
        debug("equality");

        Expr expr = comparison();
        while (match(NOT_EQUAL, EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr comparison() {
        debug("comparison");

        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        debug("term");

        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr factor() {
        debug("factor");

        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary() {
        debug("unary");

        if (match(NOT, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return call();
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();

        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 255) {
                    throw error(peek(), "Can't have more than 255 arguments.");
                }
                arguments.add(expression());
            }
            while (match(COMMA));
        }

        Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");

        return new Expr.Call(callee, paren, arguments);
    }

    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr);
            }
            else if (match(DOT)) {
                Token name = consume(IDENTIFIER, "Expect property name after '.'");
                expr = new Expr.Get(expr, name);
            }
            else {
                break;
            }
        }
        return expr;
    }

    private Expr primary() {
        debug("primary");

        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(SUPER)) {
           Token keyword = previous();
           consume(DOT, "Expect '.' after 'super'.");
           Token method = consume(IDENTIFIER, "Expect superclass method name");

           return new Expr.Super(keyword, method);
        }

        if (match(THIS)) return new Expr.This(previous());

        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }
        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Expect expression.");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        Pascal.error(token, message);
        return new ParseError(message);
    }

    private void synchronize() {
       advance();
       while (!isAtEnd()) {
           if (previous().type == SEMICOLON) return;

           switch (peek().type) {
               case CLASS:
               case FOR:
               case FUNCTION:
               case IF:
               case PRINT:
               case EXIT:
               case VAR:
               case WHILE:
                   return;
           }
           advance();
       }
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private void debug(String message) {
        if (DEBUG) {
            System.out.println(message);
        }
    }
}
