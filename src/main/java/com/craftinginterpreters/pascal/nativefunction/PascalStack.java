package com.craftinginterpreters.pascal.nativefunction;

import com.craftinginterpreters.pascal.*;

import java.util.List;
import java.util.Stack;

public class PascalStack extends PascalInstance {
    public final Stack stack;

    public PascalStack() {
        super(null);
        stack = new Stack();
    }

    @Override
    public Object get(Token name) {
        if (name.lexeme.equalsIgnoreCase("pop")) {
            return new PascalCallable() {
                @Override
                public int arity() {
                    return 0;
                }

                @Override
                public Object call(Interpreter interpreter, List<Object> arguments) {
                    return stack.pop();
                }
            };
        }
        if (name.lexeme.equalsIgnoreCase("isempty")) {
            return new PascalCallable() {
                @Override
                public int arity() {
                    return 0;
                }

                @Override
                public Object call(Interpreter interpreter, List<Object> arguments) {
                    return stack.isEmpty();
                }
            };
        }
        if (name.lexeme.equalsIgnoreCase("peek")) {
            return new PascalCallable() {
                @Override
                public int arity() {
                    return 0;
                }

                @Override
                public Object call(Interpreter interpreter, List<Object> arguments) {
                    return stack.peek();
                }
            };
        }
        else if (name.lexeme.equalsIgnoreCase("push")) {
            return new PascalCallable() {
                @Override
                public int arity() {
                    return 1;
                }

                @Override
                public Object call(Interpreter interpreter, List<Object> arguments) {
                    var value = arguments.get(0);
                    return stack.push(value);
                }
            };
        }
        else if (name.lexeme.equalsIgnoreCase("length")) {
            return stack.size();
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
        for (int i = 0; i < stack.size(); i++) {
            if (i != 0) sb.append(", ");
            sb.append(stack.get(i));
        }
        sb.append("]");
        return sb.toString();
    }
}