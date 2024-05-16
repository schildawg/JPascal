package com.craftinginterpreters.pascal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class Stmt {
    interface Visitor<R> {
        R visitBlockStmt(Block stmt);
        R visitClassStmt(Class stmt);
        R visitEnumStmt(Enum stmt);
        R visitExpressionStmt(Expression stmt);
        R visitFunctionStmt(Function stmt);
        R visitIfStmt(If stmt);
        R visitTryStmt(Try stmt);
        R visitPrintStmt(Print stmt);
        R visitReturnStmt(Return stmt);
        R visitRaiseStmt(Raise stmt);
        R visitWhileStmt(While stmt);
        R visitVarStmt(Var stmt);
        R visitBreakStmt(Break stmt);
    }
    static class Block extends Stmt {
        Block(List<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }

        final List<Stmt> statements;
    }

    static class Class extends Stmt {
        Class(Token name, Expr.Variable superclass, List<Expr> initializers, List<Stmt.Function> methods) {
            this.name = name;
            this.superclass = superclass;
            this.initializers = initializers;
            this.methods = methods;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitClassStmt(this);
        }

        final Token name;
        final Expr.Variable superclass;
        final List<Expr> initializers;
        final List<Stmt.Function> methods;
    }

    static class Enum extends Stmt {
        Enum(Token name, List<Token> values) {
            this.name = name;
            this.values = values;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitEnumStmt(this);
        }

        final Token name;
        final List<Token> values;
    }

    static class Expression extends Stmt {
        Expression(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }

        final Expr expression;
    }

    static class Function extends Stmt {
        Function(Token name, Token type, String returnType, List<Token> params, List<Token> types, List<Stmt> body) {
            this.name = name;
            this.type = type;
            this.returnType = returnType;
            this.params = params;
            this.types = types;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunctionStmt(this);
        }

        final Token name;
        final Token type;
        final String returnType;
        final List<Token> params;
        final List<Token> types;
        final List<Stmt> body;
    }

    static class If extends Stmt {
        If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }

        final Expr condition;
        final Stmt thenBranch;
        Stmt elseBranch;
    }

    static class Try extends Stmt {
        Try(Stmt tryBlock, Map<String, Except> exceptMap) {
            this.tryBlock = tryBlock;
            this.exceptMap = exceptMap;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitTryStmt(this);
        }

        final Stmt tryBlock;

        final Map<String, Except> exceptMap;
    }

    static class Except {
        Except(String name, Stmt stmt) {
            this.name = name;
            this.stmt = stmt;
        }

        final String name;
        final Stmt stmt;
    }

    static class Print extends Stmt {
        Print(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }

        final Expr expression;
    }

    static class Return extends Stmt {
        Return(Token keyword, Expr value) {
            this.keyword = keyword;
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStmt(this);
        }

        final Token keyword;
        final Expr value;
    }

    static class Raise extends Stmt {
        Raise(Token keyword, Expr value) {
            this.keyword = keyword;
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitRaiseStmt(this);
        }

        final Token keyword;
        final Expr value;
    }

    static class While extends Stmt {
        While(Expr condition, Stmt body) {
            this.condition = condition;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStmt(this);
        }

        final Expr condition;
        final Stmt body;
    }

    static class Var extends Stmt {
        Var(Token name, String type, String generic, Expr initializer) {
            this.name = name;
            this.type = type;
            this.generic = generic;

            this.initializer = initializer;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarStmt(this);
        }

        final Token name;
        String type;
        String generic;
        final Expr initializer;
    }

    static class Break extends Stmt {
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBreakStmt(this);
        }
    }

    abstract <R> R accept(Visitor<R> visitor);
}
