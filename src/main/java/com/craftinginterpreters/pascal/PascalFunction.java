package com.craftinginterpreters.pascal;

import java.util.ArrayList;
import java.util.List;

/**
 * Function in Pascal.
 */
public class PascalFunction implements PascalCallable {
    public final Stmt.Function declaration;
    private final Environment closure;
    private final boolean isInitializer;
    public final List<PascalFunction> overloads = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param declaration the top statement of function.
     * @param closure the function environment.
     * @param isInitializer is it an initializer?
     */
    public PascalFunction(Stmt.Function declaration, Environment closure, boolean isInitializer) {
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer;
    }

    public String getSignature() {
       List<String> types = new ArrayList<>();
       for (var type : declaration.types) {
           types.add(type.lexeme);
       }
       return declaration.name.lexeme + "(" + String.join(",", types) + ")";
    }

    public PascalInstance getParent() {
        Object instance = null;
        try {
            instance = closure.get(new Token(TokenType.IDENTIFIER, "this", null, 0, 0, null));
        }
        catch (RuntimeError e) {}

        if (instance instanceof PascalInstance e) {
            return e;
        }
        return null;
    }

    protected PascalFunction bind(PascalInstance instance) {
        var environment = new Environment(closure);
        environment.define("this", instance);

        return new PascalFunction(declaration, environment, isInitializer);
    }

    /**
     * Returns how many parameters the function has.
     *
     * @return number of parameters.
     */
    @Override
    public int arity() {
        return declaration.params.size();
    }

    public boolean isMatch(List<String> args) {
        if (args.size() != declaration.types.size()) {
            return false;
        }

        int i = 0;
        for (var token : declaration.types) {
            if ("any".equalsIgnoreCase(token.lexeme)) continue;

            if (!token.lexeme.equalsIgnoreCase(args.get(i))) {
                return false;
            }
            i++;
        }
        return true;
    }

    public PascalFunction match(List<String> args) {
        if (isMatch(args)) {
            return this;
        }

        for (var fun : overloads) {
            if (fun.isMatch(args)) {
                return fun;
            }
        }
        return null;
    }

    /**
     * Calls the function.
     *
     * @param interpreter the interpreter to run the function.
     * @param arguments arguments to function.  should match parameters.
     * @return result of call.
     */
    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        var environment = new Environment(closure);

        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i).lexeme, arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        }
        catch (Return returnValue) {
            if (isInitializer) return closure.getAt(0, "this");
            return returnValue.value;
        }

        if (isInitializer) return closure.getAt(0, "this");
        return null;
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }
}