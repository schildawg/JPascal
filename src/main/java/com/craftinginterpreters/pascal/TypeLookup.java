package com.craftinginterpreters.pascal;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Type Lookup.  Maps a symbol to a type in a scoped stack, with globals.
 */
public class TypeLookup {
    private final Stack<Map<String, String>> scopes = new Stack<>();
    public final Map<String, String> types = new HashMap<>();
    public TypeLookup inferred;

    public Stmt.Class currentClass = null;

    /**
     * Sets a type in the current scope.
     * @param symbol the symbol.
     * @param type type of the symbol.
     */
    public void setType(String symbol, String type) {
        var lookup = !scopes.isEmpty() ? scopes.peek() : types;

        lookup.put(symbol, type);
    }

    /**
     * Gets a type for a symbol.  Iterates up the scopes till it's found.
     * @param symbol the symbol.
     * @return the type.
     */
    public String getType(String symbol) {
        if (!scopes.isEmpty()) {
            for (int i = scopes.size() - 1; i >= 0; i--) {
                if (scopes.get(i).containsKey(symbol)) {
                    return scopes.get(i).get(symbol);
                }
            }
        }
        return types.get(symbol);
    }

    /**
     * Starts a new scope.
     */
    public void beginScope() {
        scopes.push(new HashMap<>());
    }

    /**
     * Closes the current scope.
     */
    public void endScope() {
        scopes.pop();
    }
}