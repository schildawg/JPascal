package com.craftinginterpreters.pascal;

import java.util.List;

/**
 * Function in Pascal.
 */
public class PascalFunction implements PascalCallable {
    private final Stmt.Function declaration;
    private final Environment closure;
    private final boolean isInitializer;

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
    public int aritity() {
        return declaration.params.size();
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