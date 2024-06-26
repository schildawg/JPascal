package com.craftinginterpreters.pascal;

public class RuntimeError extends RuntimeException {
    final Token token;
    Object value;

    public RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }
}
