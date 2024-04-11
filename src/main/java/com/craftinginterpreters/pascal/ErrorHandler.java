package com.craftinginterpreters.pascal;

public interface ErrorHandler {
    void runtimeError(RuntimeError error);
}

class ErrorHandlerImpl implements ErrorHandler {
    @Override
    public void runtimeError(RuntimeError error) {
        Pascal.runtimeError(error);
    }
}

class TestErrorHandler implements ErrorHandler {
    @Override
    public void runtimeError(RuntimeError error) {
        throw error;
    }
}