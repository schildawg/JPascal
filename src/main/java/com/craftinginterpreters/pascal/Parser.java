package com.craftinginterpreters.pascal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.craftinginterpreters.pascal.TokenType.*;

/**
 * Parser.
 */
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
            if (match(TYPE)) return typeDeclaration();

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
            match(FUNCTION, PROCEDURE, CONSTRUCTOR);

            methods.add(function("method"));
        }
        consume(END, "Expect 'end' after class body.");

        return new Stmt.Class(name, superclass, methods);
    }

    private Stmt typeDeclaration() {
        var name = consume(IDENTIFIER, "Expect enum name.");

        consume(EQUAL, "Expect '=' after enum declaration.");
        consume(LEFT_PAREN, "Expect '('");

        List<Token> parameters = new ArrayList<>();
        do {
            parameters.add(consume(IDENTIFIER, "Expect enum identifier."));
        }
        while (match(COMMA));
        consume(RIGHT_PAREN, "Expect ')'");
        consume(SEMICOLON, "Expect ';'");

        return new Stmt.Enum(name, parameters);
    }

    private Stmt statement() {
        if (match(FOR)) return forStatement();
        if (match(IF)) return ifStatement();
        if (match(CASE)) return caseStatement();
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
        var condition = expression();
        consume(THEN, "Expect 'then' after if condition.");

        var thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt caseStatement() {
        var left = expression();
        consume(OF, "Expect 'of' after case condition.");
        Stmt.If top = null;
        Stmt.If current = null;

        do {
            var right = expression();
            var condition = (Expr) new Expr.Binary(left, new Token(EQUAL, null, null, previous().line), right);

            while (match(COMMA)) {
                right = expression();
                var additional = new Expr.Binary(left, new Token(EQUAL, null, null, previous().line), right);
                condition = new Expr.Logical(condition, new Token(OR, null, null, previous().line), additional);
            }

            consume(COLON, "Expect ':' after condition.");

             var stmt = statement();

            var ifStmt = new Stmt.If(condition, stmt, null);
            if (top == null) {
                top = ifStmt;
            }
            else {
                current.elseBranch = ifStmt;
            }
            current = ifStmt;
        }
        while (!match(ELSE, END));

        if (previous().type == ELSE) {
            current.elseBranch = statement();
            consume(END, "Expected 'end'.");
        }
        return top;
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
        var name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(ASSIGN)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");

        return new Stmt.Var(name, initializer);
    }

    private Stmt whileStatement() {
        var condition = expression();
        consume(DO, "Expect 'do' after condition.");

        Stmt body = statement();

        return new Stmt.While(condition, body);
    }

    private Stmt expressionStatement() {
        var value = expression();

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
       var body = new ArrayList<Stmt>();
       if (match(TYPE)) {
           while (!check(BEGIN)) {
               body.add(typeDeclaration());
           }
       }
       consume(BEGIN, "Expect 'begin' before " + kind + " body.");
       body.addAll(block());

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
        var expr = or();

        if (match(ASSIGN)) {
            var equals = previous();
            var value = assignment();

            if (expr instanceof Expr.Variable) {
                var name = ((Expr.Variable)expr).name;
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
        var expr = and();

        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr and() {
        var expr = equality();

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

        var expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        debug("term");

        var expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr factor() {
        debug("factor");

        var expr = unary();

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
        var expr = primary();

        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr);
            }
            else if (match(LEFT_BRACKET)) {
                var subscript = expression();
                consume(RIGHT_BRACKET, "Expect ']' after subscript.");

                expr = new Expr.Subscript(previous(), expr, subscript);
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

        if (match(NUMBER, INTEGER, STRING, CHAR)) {
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

        if (match(LEFT_BRACKET)) {
            var map = new HashMap<Expr, Expr>();
            do {
                var key = expression();
                consume(TokenType.COLON, "Expect ':' after key.");
                var value = expression();
                map.put(key, value);
            }
            while (match(COMMA));
            consume(RIGHT_BRACKET, "Expect ']' after map.");

            return new Expr.Map(map);
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
