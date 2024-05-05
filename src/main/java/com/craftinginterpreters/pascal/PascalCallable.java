package com.craftinginterpreters.pascal;

import java.util.List;

public interface PascalCallable {
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}
