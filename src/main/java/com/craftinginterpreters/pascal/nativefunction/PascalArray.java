package com.craftinginterpreters.pascal.nativefunction;

import com.craftinginterpreters.pascal.*;

import java.util.List;

public class PascalArray extends PascalInstance {
    private final Object[] elements;

    PascalArray(int size) {
        super(null);
        elements = new Object[size];
    }

    @Override
    public Object get(Token name) {
        if (name.lexeme.equals("get")) {
            return new PascalCallable() {
                @Override
                public int arity() {
                    return 1;
                }

                @Override
                public Object call(Interpreter interpreter, List<Object> arguments) {
                    int index = (int) arguments.get(0);
                    return elements[index];
                }
            };
        }
        else if (name.lexeme.equals("set")) {
            return new PascalCallable() {
                @Override
                public int arity() {
                    return 2;
                }

                @Override
                public Object call(Interpreter interpreter, List<Object> arguments) {
                    int index = (int) arguments.get(0);
                    var value = arguments.get(1);
                    return elements[index] = value;
                }
            };
        }
        else if (name.lexeme.equals("length")) {
            return (double) elements.length;
        }

        throw new RuntimeError(name, // [hidden]
                "Undefined property '" + name.lexeme + "'.");
    }

    @Override
    public void set(Token name, Object value) {
        throw new RuntimeError(name, "Can't add properties to arrays.");
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < elements.length; i++) {
            if (i != 0) sb.append(", ");
            sb.append(elements[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}