package com.craftinginterpreters.pascal;

import java.util.List;

interface PascalCallable {
    int aritity();
    Object call(Interpreter interpreter, List<Object> arguments);
}
