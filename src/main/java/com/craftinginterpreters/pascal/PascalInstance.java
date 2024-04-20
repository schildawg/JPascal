package com.craftinginterpreters.pascal;

import java.util.HashMap;
import java.util.Map;

/**
 * Instance for Pascal.
 */
public class PascalInstance {
    private final Map<String, Object> fields = new HashMap<>();

    private final PascalClass klass;

    /**
     * Constructor.
     *
     * @param klass the instance's class.
     */
    public PascalInstance(PascalClass klass) {
        this.klass = klass;
    }

    protected Object get(Token name) {
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }
        var method = klass.findMethod(name.lexeme);
        if (method != null) return method.bind(this);

        throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
    }

    protected void set(Token name, Object value) {
        fields.put(name.lexeme, value);
    }

    @Override
    public String toString() {
        return klass.name + " instance";
    }
}