package com.craftinginterpreters.pascal.nativefunction;

import com.craftinginterpreters.pascal.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PascalMap extends PascalInstance {
    public final Map map;

    public PascalMap(Map map) {
        super(null);
        this.map = map;
    }

    public PascalMap() {
        this(new HashMap());
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
                    var key = arguments.get(0);
                    return map.get(key);
                }
            };
        }
        else if (name.lexeme.equalsIgnoreCase("put")) {
            return new PascalCallable() {
                @Override
                public int arity() {
                    return 2;
                }

                @Override
                public Object call(Interpreter interpreter, List<Object> arguments) {
                    var key = arguments.get(0);
                    var value = arguments.get(1);
                    return map.put(key, value);
                }
            };
        }
        else if (name.lexeme.equalsIgnoreCase("contains")) {
            return new PascalCallable() {
                @Override
                public int arity() {
                    return 1;
                }

                @Override
                public Object call(Interpreter interpreter, List<Object> arguments) {
                    var key = arguments.get(0);

                    return map.containsKey(key);
                }
            };
        }

        throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
    }

    @Override
    public void set(Token name, Object value) {
        throw new RuntimeError(name, "Can't add properties to maps.");
    }

    @Override
    public String toString() {
        return "<map>";
    }
}