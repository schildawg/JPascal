package com.craftinginterpreters.pascal;

import java.util.List;

interface PascalCallable {

    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}
