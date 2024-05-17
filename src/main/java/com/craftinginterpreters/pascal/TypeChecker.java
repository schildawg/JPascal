package com.craftinginterpreters.pascal;

import java.util.List;

/**
 * Type Checker.
 */
class TypeChecker implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
     private Stmt.Function currentFunction = null;
     private ClassType currentClass = ClassType.NONE;

     private final TypeLookup lookup = new TypeLookup();

    TypeChecker() {
        lookup.inferred = new TypeLookup();
        lookup.parents = new TypeLookup();
        lookup.generics = new TypeLookup();
    }

    private enum ClassType {
        NONE,
        SUBCLASS,
        CLASS
    }

    void resolve(List<Stmt> statements) {
        for (Stmt statement : statements) {
            mapType(statement);
        }

        for (Stmt statement : statements) {
            resolve(statement);
        }
    }

    private void mapType(Stmt stmt) {
        if (stmt instanceof Stmt.Enum e) {
            for (var value : e.values) {
                if (lookup.getType(value.lexeme) != null) {
                    throw new RuntimeException(value.lexeme + "already exists!!!");
                }
                lookup.setType(value.lexeme, e.name.lexeme);
            }
        }
        else if (stmt instanceof Stmt.Function e) {
            lookup.setType(e.name.lexeme, e.returnType);
        }
        else if (stmt instanceof Stmt.Class e) {
            lookup.parents.setType(e.name.lexeme, ((e.superclass == null) ? "Any" : e.superclass.name.lexeme));
            lookup.setType(e.name.lexeme, e.name.lexeme);
            for (var fun : e.methods) {
                lookup.setType(e.name.lexeme + "::" + fun.name.lexeme, fun.returnType);
            }
            for (var expr : e.initializers) {
                if (expr instanceof Expr.ClassVar v) {
                    lookup.generics.setType(v.name.lexeme, v.generic);
                }
            }
        }
        else if (stmt instanceof Stmt.Var e) {
            lookup.setType(e.name.lexeme, e.type);
            lookup.generics.setType(e.name.lexeme, e.generic);
        }
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        lookup.beginScope();
        resolve(stmt.statements);
        lookup.endScope();
        return null;
    }

    @Override
    public Void visitEnumStmt(Stmt.Enum stmt) {
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        var enclosingClass = currentClass;
        currentClass = ClassType.CLASS;
        var previousClass = lookup.currentClass;
        lookup.currentClass = stmt;

        if (stmt.superclass != null) {
            currentClass = ClassType.SUBCLASS;
            resolve(stmt.superclass);
        }

        if (stmt.superclass != null) {
            lookup.beginScope();
           //scopes.peek().put("super", true);
        }

        for (Expr expr : stmt.initializers) {
            resolve(expr);
        }

        lookup.beginScope();
        //scopes.peek().put("this", true);
        for (Stmt.Function method : stmt.methods) {
            resolveFunction(method);
        }

        lookup.endScope();

        if (stmt.superclass != null) lookup.endScope();
        lookup.currentClass = previousClass;
        currentClass = enclosingClass;
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        resolveFunction(stmt);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null) resolve(stmt.elseBranch);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if (stmt.value == null) return null;
        if ("procedure".equalsIgnoreCase(currentFunction.type.lexeme)) {
            throw new RuntimeError(stmt.keyword, "Can't return value from procedure.");
        }
        var exitType = stmt.value.reduce(lookup);
        var returnType = currentFunction.returnType;

        if ("any".equalsIgnoreCase(returnType)) return null;

        if (exitType.equalsIgnoreCase(returnType)) {
            return null;
        }

        var parent = lookup.parents.getType(exitType);
        while (parent != null) {
            if (returnType.equalsIgnoreCase(parent)) {
                return null;
            }
            parent = lookup.parents.getType(parent);
        }

        throw new RuntimeError(stmt.keyword, "Type mismatch!");
    }

    @Override
    public Void visitRaiseStmt(Stmt.Raise stmt) {
        return null;
    }

    @Override
    public Void visitTryStmt(Stmt.Try stmt) {
        // TODO: Add logic
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        if (stmt.initializer != null) {
            var type = stmt.initializer.reduce(lookup);
            if ("any".equalsIgnoreCase(stmt.type)) {
                lookup.inferred.setType(stmt.name.lexeme,  type);
                return null;
            }

            lookup.setType(stmt.name.lexeme, stmt.type);
            if (stmt.type.equalsIgnoreCase(type)) {
                return null;
            }

            var parent = lookup.parents.getType(type);
            while (parent != null) {
                if (stmt.type.equalsIgnoreCase(parent)) {
                    return null;
                }
                parent = lookup.parents.getType(parent);
            }
            throw new RuntimeError(stmt.name, "Type mismatch!");
        }
        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        resolve(expr.value);
        var type = lookup.getType(expr.name.lexeme);
        if (type == null) {
            return null;
            //throw new RuntimeError(expr.name, "Type not defined!");
        }

        var inferred = expr.value.reduce(lookup);

        if ("any".equalsIgnoreCase(type)) {
            // probably not needed, but set infer anyway.
            lookup.inferred.setType(expr.name.lexeme, inferred);
            return null;
        }


        if (type.equalsIgnoreCase(inferred)) {
            return null;
        }

        var parent = lookup.parents.getType(inferred);
        while (parent != null) {
            if (type.equalsIgnoreCase(parent)) {
                return null;
            }
            parent = lookup.parents.getType(parent);
        }

        System.out.println(type + " != " + inferred);
        throw new RuntimeError(expr.name, "Type mismatch!");
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.callee);

        for (Expr argument : expr.arguments) {
            resolve(argument);
        }
        return null;
    }

    @Override
    public Void visitGetExpr(Expr.Get expr) {
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitMapExpr(Expr.Map expr) {
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        return null;
    }

    @Override
    public Void visitSetExpr(Expr.Set expr) {
        var expected = lookup.getType(expr.name.lexeme);
        var inferred = expr.value.reduce(lookup);

        resolve(expr.value);
        resolve(expr.object);

        if (expected == null || "any".equalsIgnoreCase(expected)) return null;
        if (!expected.equalsIgnoreCase(inferred)) {
            System.out.println(expected + " != " + inferred);
            throw new RuntimeError(expr.name, "Type mismatch.");
        }
        return null;
    }

    @Override
    public Void visitClassVarExpr(Expr.ClassVar expr) {
        lookup.setType(expr.name.lexeme, expr.type);

        resolve(expr.value);
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitSuperExpr(Expr.Super expr) {
        return null;
    }

    @Override
    public Void visitSubscriptExpr(Expr.Subscript expr) {
        resolve(expr.expr);
        resolve(expr.index);

        return null;
    }

    @Override
    public Void visitThisExpr(Expr.This expr) {
        return null;
    }

    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }

    private void resolveFunction(Stmt.Function function) {
        Stmt.Function enclosingFunction = currentFunction;
        currentFunction = function;

        lookup.beginScope();
        resolve(function.body);
        lookup.endScope();

        currentFunction = enclosingFunction;
    }
}