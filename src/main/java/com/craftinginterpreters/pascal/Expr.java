package com.craftinginterpreters.pascal;


import java.util.HashMap;
import java.util.List;

public abstract class Expr {
    interface Visitor<R> {
        R visitAssignExpr(Assign expr);
        R visitBinaryExpr(Binary expr);
        R visitCallExpr(Call expr);
        R visitGetExpr(Get expr);
        R visitGroupingExpr(Grouping expr);
        R visitLiteralExpr(Literal expr);
        R visitMapExpr(Map expr);
        R visitLogicalExpr(Logical expr);
        R visitVariableExpr(Variable expr);
        R visitSetExpr(Set expr);
        R visitClassVarExpr(ClassVar expr);
        R visitSuperExpr(Super expr);
        R visitSubscriptExpr(Subscript expr);
        R visitThisExpr(This expr);
        R visitUnaryExpr(Unary expr);
    }
    static class Assign extends Expr {
        Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }

        final Token name;
        final Expr value;
    }

    static class Binary extends Expr {
        Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }

        final Expr left;
        final Token operator;
        final Expr right;

        public String reduce(TypeLookup lookup) {
            if (operator.type == TokenType.LESS || operator.type == TokenType.LESS_EQUAL || operator.type == TokenType.GREATER || operator.type == TokenType.GREATER_EQUAL || operator.type == TokenType.EQUAL || operator.type == TokenType.NOT_EQUAL) {
                // TODO: Add checking
                return "Boolean";
            }

            var leftType = left.reduce(lookup);
            var rightType = right.reduce(lookup);
            if ("any".equalsIgnoreCase(leftType) || "any".equalsIgnoreCase(rightType)) {
                return leftType;
            }

            if ("String".equals(leftType) || "String".equals(rightType)) {
                if (operator.type == TokenType.PLUS) {
                    return "String";
                }
            }

            // FIXME
            if (leftType == null) {
                return rightType;
            }

            if (!leftType.equals(rightType)) {
                throw new RuntimeError(operator, "Type mismatch.");
            }
            return leftType;
        }
    }

    public static class Call extends Expr {
        Call(Expr callee, Token paren, List<Expr> arguments) {
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpr(this);
        }

        final Expr callee;
        public final Token paren;
        final List<Expr> arguments;

        public String reduce(TypeLookup lookup) {
            var result = callee.reduce(lookup);
            if (callee instanceof Expr.Variable e) {
                // TODO: Make this better
                if ("Str".equalsIgnoreCase(e.name.lexeme)) {
                    return "String";
                }
                else if ("Copy".equalsIgnoreCase(e.name.lexeme)) {
                    return "String";
                }
                else if ("Length".equalsIgnoreCase(e.name.lexeme)) {
                    return "Integer";
                }

                if ("any".equalsIgnoreCase(result) && lookup.currentClass != null) {
                    result = lookup.getType(lookup.currentClass.name.lexeme + "::" + e.name.lexeme);
                }
            }

            return result == null ? "Any" : result;
        }
    }

    static class Subscript extends Expr {
        Subscript(Token token, Expr expr, Expr index) {
            this.token = token;
            this.expr = expr;
            this.index = index;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitSubscriptExpr(this);
        }

        public String reduce(TypeLookup lookup) {
            var type = expr.reduce(lookup);
            if ("string".equalsIgnoreCase(type)) {
                return "Char";
            }

            if (expr instanceof Expr.Variable e && lookup.generics != null)  {

                var generic = lookup.generics.getType(e.name.lexeme);
                if (generic != null) return generic;
            }
            return "Any";
        }

        final Token token;
        final Expr expr;
        final Expr index;
    }

    static class Get extends Expr {
        Get(Expr object, Token name) {
            this.object = object;
            this.name = name;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGetExpr(this);
        }

        final Expr object;
        final Token name;

        public String reduce(TypeLookup lookup) {
            var klass = object.reduce(lookup);

            if ("any".equalsIgnoreCase(klass)) {
                klass = object.reduce(lookup.inferred);
            }

            return lookup.getType( klass + "::" + name.lexeme);
        }
    }

    static class Grouping extends Expr {
        Grouping(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }

        final Expr expression;

        public String reduce(TypeLookup lookup) {
            return expression.reduce(lookup);
        }
    }

    static class Literal extends Expr {
        Literal(Object value) {
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }

        final Object value;

        public String reduce(TypeLookup lookup) {
            var map = new HashMap<Class, String>();
            map.put(String.class, "String");
            map.put(Integer.class, "Integer");
            map.put(Boolean.class, "Boolean");
            map.put(Character.class, "Char");
            map.put(Double.class, "Double");

            if (value != null && map.containsKey(value.getClass()))  {
                return map.get(value.getClass());
            }

            if (value instanceof PascalInstance e) {
                return e.klass.name;
            }
            return "Any";
        }
    }

    static class Map extends Expr {
        Map(java.util.Map<Expr, Expr> value) {
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitMapExpr(this);
        }

        final java.util.Map<Expr, Expr> value;
    }

    static class Logical extends Expr {
        Logical(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpr(this);
        }

        final Expr left;
        final Token operator;
        final Expr right;

        public String reduce(TypeLookup lookup) {
            var leftType = left.reduce(lookup);
            var rightType = right.reduce(lookup);

            if ("any".equalsIgnoreCase(leftType) || "any".equalsIgnoreCase(rightType)) {
                return "Boolean";
            }

            if (!leftType.equals(rightType)) {
                throw new RuntimeError(operator, "Type mismatch.");
            }
            return "Boolean";
        }
    }

    static class Variable extends Expr {
        Variable(Token name) {
            this.name = name;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableExpr(this);
        }

        final Token name;

        public String reduce(TypeLookup lookup) {
            if (lookup.getType(name.lexeme) == null) {
                if ("Str".equalsIgnoreCase(name.lexeme)) {
                    return "String";
                }
                else if ("Copy".equalsIgnoreCase(name.lexeme)) {
                    return "String";
                }
                else if ("Length".equalsIgnoreCase(name.lexeme)) {
                    return "Integer";
                }
                else if ("List".equalsIgnoreCase(name.lexeme)) {
                    return "List";
                }
                else if ("Stack".equalsIgnoreCase(name.lexeme)) {
                    return "Stack";
                }
                return "Any";
                //throw new RuntimeException(name.lexeme + " " + name.fileName + name.line);
            }
            return lookup.getType(name.lexeme);
        }
    }

    static class Set extends Expr {
        Set(Expr object, Token name, Expr value) {
            this.object = object;
            this.name = name;
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitSetExpr(this);
        }

        final Expr object;
        final Token name;
        final Expr value;
    }

    static class ClassVar extends Expr {
        ClassVar(Expr object, Token name, String type, String generic, Expr value) {
            this.object = object;
            this.name = name;
            this.type = type;
            this.generic = generic;
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitClassVarExpr(this);
        }

        final Expr object;
        final Token name;
        final String type;
        final String generic;
        final Expr value;
    }

    static class Super extends Expr {
        Super(Token keyword, Token method) {
            this.keyword = keyword;
            this.method = method;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitSuperExpr(this);
        }

        final Token keyword;
        final Token method;
    }

    static class This extends Expr {
        This(Token keyword) {
            this.keyword = keyword;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitThisExpr(this);
        }

        final Token keyword;
    }

    static class Unary extends Expr {
        Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }

        final Token operator;
        final Expr right;

        public String reduce(TypeLookup lookup) {
            return right.reduce(lookup);
        }
    }

    abstract <R> R accept(Visitor<R> visitor);
    public String reduce(TypeLookup lookup) {
        return "Any";
    }
}
