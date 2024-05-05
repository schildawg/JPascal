package com.craftinginterpreters.pascal;

import com.craftinginterpreters.pascal.nativefunction.*;

import java.util.*;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    final Environment globals = new Environment();
    private Environment environment = globals;
    private final Map<Expr, Integer> locals = new HashMap<>();

    private final ErrorHandler errorHandler;

    Interpreter(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;

        try {
           NativeFunctionInvoker.register(globals, NativeFunctions.class);
        }
        catch (NoSuchMethodException e) {
            throw new RuntimeException("Error registering native functions");
        }
    }

    Interpreter() {
        this(new ErrorHandlerImpl());
    }

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        }
        catch (RuntimeError error) {
            Console.error(error);
            errorHandler.runtimeError(error);
        }
    }

    void runTests(List<Stmt> statements) {
        try {
            AssertionInvoker.register(globals, Assertions.class);
        }
        catch (NoSuchMethodException e) {
            throw new RuntimeException("Error registering assertions.");
        }

        try {
            Map<String, Set<Stmt.Function>> tests = new HashMap<>();
            int count = 0;
            for (Stmt statement : statements) {
                count++;
                if (statement instanceof Stmt.Function fun) {
                    execute(statement);
                    if (!tests.containsKey(fun.name.fileName)) {
                        tests.put(fun.name.fileName, new HashSet<>());
                    }
                    var set = tests.get(fun.name.fileName);
                    set.add(fun);
                }
                else if (statement instanceof Stmt.Class klass) {
                    execute(klass);
                }
                else if (statement instanceof Stmt.Enum stmt) {
                    execute(stmt);
                }
                else if (statement instanceof Stmt.Var stmt) {
                    execute(stmt);
                }
            }

            Console.info("Running " + count + " tests...");
            for (var key : tests.keySet()) {
                Console.subheader(key);

                var set = tests.get(key);
                for (var fun : set) {
                    try {
                        var test = (PascalCallable) lookupVariable(fun.name, null);
                        test.call(this, new ArrayList<>());
                        if (fun.name != null && fun.name.literal != null)
                            Console.info("Test: " + fun.name.literal + " " + ".".repeat(55 - fun.name.literal.toString().length()) + " [ " + Console.ANSI_GREEN + "PASS" + Console.ANSI_RESET + " ]");
                    }
                    catch (RuntimeError error) {
                        Console.info("Test: " + fun.name.literal + " " + ".".repeat(55 - fun.name.literal.toString().length()) + " [ " + Console.ANSI_RED + "FAIL" + Console.ANSI_RESET + " ]");

                        Console.error(error);
                        errorHandler.runtimeError(error);
                    }
                }
                Console.info("");
            }
        }
        catch (RuntimeError error) {
            Console.error(error);
            errorHandler.runtimeError(error);
        }
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        var value = evaluate(expr.value);

        var distance = locals.get(expr);
        if (distance != null) {
            try {
                environment.assignAt(distance, expr.name, value);
            }
            catch (Exception e) {
                try {
                    var object = (PascalInstance) environment.get(new Token(TokenType.THIS, "this", null, expr.name.line, 0, null));
                    object.set(new Token(TokenType.IDENTIFIER, expr.name.lexeme, null, expr.name.line, 0, null), value);
                } catch (Exception e2) {
                    throw e;
                }
            }
        }
        else {
            try {
                globals.assign(expr.name, value);
            }
            catch (Exception e) {
                try {
                    var object = (PascalInstance) environment.get(new Token(TokenType.THIS, "this", null, expr.name.line, 0, null));
                    object.set(new Token(TokenType.IDENTIFIER, expr.name.lexeme, null, expr.name.line, 0, null), value);
                } catch (Exception e2) {
                    throw e;
                }
            }
        }
        return value;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        var left = evaluate(expr.left);
        var right = evaluate(expr.right);


        switch (expr.operator.type) {
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double) {
                    return (double) left > (double) right;
                }
                else if (left instanceof Character) {
                    return (char) left >= (char) right;
                }
                else {
                    return (int) left > (int) right;
                }

            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double) {
                    return (double) left >= (double) right;
                }
                else if (left instanceof Character) {
                    return (char) left >= (char) right;
                }
                else {
                    return (int) left >= (int) right;
                }

            case LESS:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double) {
                    return (double) left < (double) right;
                }
                else if (left instanceof Character) {
                    return (char) left < (char) right;
                }
                else {
                    return (int) left < (int) right;
                }

            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double) {
                    return (double) left <= (double) right;
                }
                else if (left instanceof Character) {
                    return (char) left <= (char) right;
                }
                else {
                    return (int) left <= (int) right;
                }

            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double) {
                    return (double) left - (double) right;
                }
                else {
                    return (int) left - (int) right;
                }

            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left + (int) right;
                }
                if (left instanceof String || right instanceof String) {
                    return stringify(left) + stringify(right);
                }
                throw new RuntimeError(expr.operator, "Operands must be two numbers, or two strings.");
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double) {
                    return (double) left / (double) right;
                }
                else {
                    return (int) left / (int) right;
                }

            case STAR:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double) {
                    return (double) left * (double) right;
                }
                else {
                    return (int) left * (int) right;
                }

            case NOT_EQUAL:
                return !isEqual(left, right);
            case EQUAL:
                return isEqual(left, right);
        }
        // unreachable
        return null;
    }

    private Object lookupCallInternal(Expr expr) {
        try {
            return evaluate(expr);
        }
        catch (Exception e) {
            if (expr instanceof Expr.Variable variable) {
                try {
                    var object = environment.get(new Token(TokenType.THIS, "this", null, variable.name.line, 0, null));
                    return ((PascalInstance) object).get(new Token(TokenType.IDENTIFIER, variable.name.lexeme, null, variable.name.line, 0, null));

                } catch (Exception e2) {
                    throw e;
                }
            }
            throw e;
        }
    }

    private Object lookupCall(Expr expr) {
        return lookupCallInternal(expr);
    }

    public String type(Object obj) {
        if (obj == null) {
            return "Any";
        }

        var map = new HashMap<Class, String>();
        map.put(String.class, "String");
        map.put(Integer.class, "Integer");
        map.put(Boolean.class, "Boolean");
        map.put(Character.class, "Char");
        map.put(Double.class, "Double");

        if (map.containsKey(obj.getClass()))  {
            return map.get(obj.getClass());
        }

        if (obj instanceof PascalEnum e) {
            return e.enumName;
        }

        if (obj instanceof PascalInstance e) {
            return e.klass.name;
        }
        return "Any";
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        var callee = lookupCall(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        List<String> types = new ArrayList<>();
        for (var arg : arguments) {
            types.add(type(arg));
        }

        if (!(callee instanceof PascalCallable)) {
            throw new RuntimeError(expr.paren, "Can only call functions and classes.");
        }

        var function = (PascalCallable) callee;

        if (function instanceof PascalFunction fun) {
            // look in overloads
            function = fun.match(types);

            if (function == null) {
                var parent = fun.getParent();
                if (parent != null) {
                    function = parent.klass.findMethod(fun.declaration.name.lexeme, types);
                    function = ((PascalFunction)function).bind(parent);
                }
            }

            // walk up environments...
            if (function == null ) {
                function = environment.findFunction(fun.declaration.name, types);
            }

            // check in globals
            if (function == null ) {
                function = globals.findFunction(fun.declaration.name, types);
            }
            if (function == null) {
                throw new RuntimeError(expr.paren, "No matching signature for function.");
            }
        }

        if (function instanceof Assertion) {
            var newArgs = new ArrayList<>();
            newArgs.add(expr);
            newArgs.addAll(arguments);
            return function.call(this, newArgs);
        }
        else {
            if (arguments.size() != function.arity()) {
                throw new RuntimeError(expr.paren, "Expected " + function.arity() + " arguments but got " + arguments.size() + ".");
            }

            return function.call(this, arguments);
        }
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        var object = evaluate(expr.object);
        if (object instanceof PascalInstance) {
            return ((PascalInstance) object).get(expr.name);
        }
        throw new RuntimeError(expr.name, "Only instances have properties.");
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }

    void executeBlock(List<Stmt> statements, Environment environment) {
        var previous = this.environment;
        try {
            this.environment = environment;
            for (Stmt stmt : statements) {
                execute(stmt);
            }
        }
        finally {
            this.environment = previous;
        }
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitMapExpr(Expr.Map expr) {
        var map = new HashMap<>();
        var entries = expr.value.entrySet();
        for (Map.Entry<Expr, Expr> entry : entries) {
            var key = evaluate(entry.getKey());
            var value = evaluate(entry.getValue());

            map.put(key, value);
        }
        return new PascalMap(map);
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
           if (isTruthy(left)) return left;
        }
        else {
            if (!isTruthy(left)) return left;
        }
        return evaluate(expr.right);
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        try {
            return lookupVariable(expr.name, expr);
        }
        catch (Exception e) {
            try {
                var object = environment.get(new Token(TokenType.THIS, "this", null, expr.name.line, 0, null));

                return ((PascalInstance) object).get(new Token(TokenType.IDENTIFIER, expr.name.lexeme, null, expr.name.line, 0, null));
            }
            catch (Exception e2) {
                System.out.println(e2.getMessage());
                throw e;
            }
        }
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        var object = evaluate(expr.object);

        if (!(object instanceof PascalInstance)) {
            throw new RuntimeError(expr.name, "Only instances have fields.");
        }
        var value = evaluate(expr.value);
        ((PascalInstance)object).set(expr.name, value);
        return value;
    }

    @Override
    public Object visitClassVarExpr(Expr.ClassVar expr) {
        var object = evaluate(expr.object);

        if (!(object instanceof PascalInstance)) {
            throw new RuntimeError(expr.name, "Only instances have fields.");
        }
        var value = evaluate(expr.value);
        ((PascalInstance)object).set(expr.name, value);
        return value;
    }

    @Override
    public Object visitSuperExpr(Expr.Super expr) {
        int distance = locals.get(expr);
        var superclass = (PascalClass) environment.getAt(distance, "super");

        var object = (PascalInstance) environment.getAt(distance - 1, "this");
        var method = superclass.findMethod(expr.method.lexeme);
        if (method == null) {
            throw new RuntimeError(expr.method, "Undefined property '" + expr.method.lexeme + "'.");
        }
        return method.bind(object);
    }

    @Override
    public Object visitThisExpr(Expr.This expr) {
        return lookupVariable(expr.keyword, expr);
    }

    private Object lookupVariable(Token name, Expr expr) {
        var distance = locals.get(expr);
        if (distance != null) {
            return environment.getAt(distance, name.lexeme);
        }
        else {
            return globals.get(name);
        }
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case NOT:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                if (right instanceof Integer) return  -(int)right;
                else return -(double)right;
        }
        // unreachable
        return null;
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        if (operand instanceof Integer) return;
        if (operand instanceof Character) return;

        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        if (left instanceof Integer && right instanceof Integer) return;
        if (left instanceof Character && right instanceof Character) return;

        // Console.debug(left.getClass().getName() + " " + right.getClass().getName());
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        if (object instanceof Integer) return ((int)object != 0);
        if (object instanceof PascalEnum e) return e.value != 0;

        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a.equals(b);
    }

    private String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        Object superclass = null;
        if (stmt.superclass != null) {
            superclass = evaluate(stmt.superclass);
            if (!(superclass instanceof PascalClass)) {
                throw new RuntimeError(stmt.superclass.name, "Superclass must be a class.");
            }
        }
        environment.define(stmt.name.lexeme, null);

        if (stmt.superclass != null) {
            environment = new Environment(environment);
            environment.define("super", superclass);
        }
        Map<String, PascalFunction> methods = new HashMap<>();
        for (Stmt.Function method : stmt.methods) {
            var function = new PascalFunction(method, environment, method.name.lexeme.equals("Init"));
            if (methods.containsKey(method.name.lexeme)) {
                var first = methods.get(method.name.lexeme);
                first.overloads.add(function);
            }
            else {
                methods.put(method.name.lexeme, function);
            }
        }
        var klass = new PascalClass(stmt.name.lexeme, (PascalClass) superclass, methods);

        if (superclass != null) {
            environment = environment.enclosing;
        }
        environment.assign(stmt.name, klass);
        return null;
    }

    @Override
    public Void visitEnumStmt(Stmt.Enum stmt) {
        int  count = 0;
        for (var value : stmt.values) {
            environment.define(value.lexeme, new PascalEnum(stmt.name.lexeme, value.lexeme, count++));
        }
        return null;
    }

    @Override
    public Object visitSubscriptExpr(Expr.Subscript expr) {
        var target = evaluate(expr.expr);
        if (target instanceof String s) {
            int index = (int) Double.parseDouble(evaluate(expr.index).toString());
            return s.charAt(index);
        }
        else if (target instanceof PascalList list) {
            int index = Integer.parseInt(evaluate(expr.index).toString());
            return list.list.get(index);
        }

        throw new RuntimeError(expr.token, "Subscript target should be an ordinal.");
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        var function = new PascalFunction(stmt, environment, false);

        if (environment.values.containsKey(stmt.name.lexeme)) {
            var value = environment.values.get(stmt.name.lexeme);

            throw new RuntimeError(stmt.name, "Variable already exists!" + value.getClass());
        }
        environment.define(stmt.name.lexeme, function);

        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        }
        else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));

        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null) value = evaluate(stmt.value);

        throw new Return(value);
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }
        environment.define(stmt.name.lexeme, value);
        return null;
    }
}
