package com.craftinginterpreters.pascal.nativefunction;

import com.craftinginterpreters.pascal.*;

import java.util.ArrayList;
import java.util.List;

public class PascalList extends PascalInstance {
    public final List list;

    public PascalList() {
        super(null);
        list = new ArrayList();
    }

    @Override
    public Object get(Token name) {
        if (name.lexeme.equalsIgnoreCase("get")) {
            return new PascalCallable() {
                @Override
                public int arity() {
                    return 1;
                }

                @Override
                public Object call(Interpreter interpreter, List<Object> arguments) {
                    int index = (int) arguments.get(0);
                    return list.get(index);
                }
            };
        }
        else if (name.lexeme.equalsIgnoreCase("add")) {
            return new PascalCallable() {
                @Override
                public int arity() {
                    return 1;
                }

                @Override
                public Object call(Interpreter interpreter, List<Object> arguments) {
                    var value = arguments.get(0);
                    return list.add(value);
                }
            };
        }
        else if (name.lexeme.equalsIgnoreCase("length")) {
            return list.size();
        }

        throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
    }

    @Override
    public void set(Token name, Object value) {
        throw new RuntimeError(name, "Can't add properties to lists.");
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            if (i != 0) sb.append(", ");
            sb.append(list.get(i));
        }
        sb.append("]");
        return sb.toString();
    }
}