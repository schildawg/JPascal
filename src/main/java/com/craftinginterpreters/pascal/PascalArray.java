package com.craftinginterpreters.pascal;

import java.util.List;

class PascalArray extends PascalInstance {
    private final Object[] elements;

    PascalArray(int size) {
        super(null);
        elements = new Object[size];
    }

    @Override
    protected Object get(Token name) {
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
    protected void set(Token name, Object value) {
        throw new RuntimeError(name, "Can't add properties to arrays.");
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[");
        for (int i = 0; i < elements.length; i++) {
            if (i != 0) buffer.append(", ");
            buffer.append(elements[i]);
        }
        buffer.append("]");
        return buffer.toString();
    }
}