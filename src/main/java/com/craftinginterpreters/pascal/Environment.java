package com.craftinginterpreters.pascal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Environment {
    final Environment enclosing;
    public final Map<String, Object> values = new HashMap<>();

    public Environment() {
        enclosing = null;
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

   public Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }
        if (enclosing != null) return enclosing.get(name);

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

   public  void define(String name, Object value) {
        if (values.containsKey(name)) {
            var existing = values.get(name);
            if (existing instanceof PascalFunction && value instanceof PascalFunction) {
                return;
            }
            throw new RuntimeException("Redefined: " + name);
        }
        values.put(name, value);
    }

    public Environment ancestor(int distance) {
        var environment = this;
        for (int i = 0; i < distance; i++) {
            environment = environment.enclosing;
        }

        return environment;
    }

    public Object getAt(int distance, String name) {
        return ancestor(distance).values.get(name);
    }

    public void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }
        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    public void assignAt(int distance, Token name, Object value) {
        ancestor(distance).values.put(name.lexeme, value);
    }

    public PascalFunction findFunction(Token name, List<String> types) {
        Object function = null;
        try {
            function = get(name);
        } catch (RuntimeError e) {}

        if (function instanceof PascalFunction fun) {
            var matched = fun.match(types);
            if (matched != null) {
               return matched;
            }
        }
        if (enclosing != null) {
            return enclosing.findFunction(name, types);
        }
        return null;
    }
}