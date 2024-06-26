package com.craftinginterpreters.pascal.nativefunction;

import com.craftinginterpreters.pascal.Environment;
import com.craftinginterpreters.pascal.Interpreter;
import com.craftinginterpreters.pascal.RuntimeError;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class AssertionInvoker implements Assertion {
    private final Method method;
    private final int arity;
    private final List<String> parameters = new ArrayList<>();

    public AssertionInvoker(Method method) {
        this.method = method;
        this.arity = method.getParameterCount();

        for (var param : method.getParameters()) {
            parameters.add(param.getType().getSimpleName());
        }
    }

    @Override
    public int arity() {
        return arity;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        try {
            return method.invoke(null, arguments.toArray());
        }
        catch (IllegalAccessException | InvocationTargetException e) {
            throw (RuntimeError)e.getCause();
        }

    }

    public static void register(Environment globals, Class clazz) throws NoSuchMethodException {
        for (var method : clazz.getMethods()) {
            if (Modifier.isStatic(method.getModifiers())) {
                globals.define(method.getName(), new AssertionInvoker(method));
            }
        }
    }
}