package com.craftinginterpreters.pascal;

import java.util.List;
import java.util.Map;

/**
 * Class for Pascal.
 */
public class PascalClass implements PascalCallable {
    final PascalClass superclass;
    public String name;
    private final Map<String, PascalFunction> methods;

    /**
     * Constructor.
     *
     * @param name the name of the class.
     * @param superclass superclass.  null if none.
     * @param methods list of methods in class.
     */
    public PascalClass(String name, PascalClass superclass, Map<String, PascalFunction> methods) {
        this.superclass = superclass;
        this.name = name;
        this.methods = methods;
    }

    /**
     * Finds a method by name, in this class or any superclasses.
     *
     * @param name the name of the method.
     * @return the method, or null if none found.
     */
    protected PascalFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }
        if (superclass != null) {
            return superclass.findMethod(name);
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Number of parameters for class initializer.
     *
     * @return number of parameter for initializer.
     */
    @Override
    public int arity() {
        var initializer = findMethod("Init");
        if (initializer == null) return 0;

        return initializer.arity();
    }

    /**
     * Calls the "Init" method creating an Instance of Class.
     *
     * @param interpreter the interpreter that runs the initializer.
     * @param arguments the arguments to the initializer.
     * @return an instance of Class.
     */
    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        var instance = new PascalInstance(this);
        var initializer = findMethod("Init");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }
        return instance;
    }
}