package com.craftinginterpreters.pascal;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests PascalFunction.
 */
public class PascalFunctionTest {
    private PascalFunction makeFunction() {
       var code = """
           function fib(n);
           begin
               if n < 2 then exit n;

               exit fib(n - 1) + fib(n - 2);
           end""";

        var scanner = new Scanner(code);
        var tokens = scanner.scanTokens();

        var parser = new Parser(tokens, false);
        var stmts = parser.parse();

        return new PascalFunction((Stmt.Function) stmts.get(0), new Environment(), false);
    }

    // Tests creating a new Function.
    //
    @Test
    void testCreate() {
        var function = makeFunction();

        assertEquals(1, function.aritity());
    }

    // Tests PascalFunction.toString()
    //
    @Test
    void testToString() {
        var function = makeFunction();

        assertEquals("<fn fib>", function.toString());
    }

    // Tests calling a function
    //
    @Test
    void testCall() {
        var code = """
           function fib(n);
           begin
               if n < 2 then exit n;

               exit fib(n - 1) + fib(n - 2);
           end""";

        var scanner = new Scanner(code);
        var tokens = scanner.scanTokens();

        var parser = new Parser(tokens, false);
        var stmts = parser.parse();

        var interpreter = new Interpreter();
        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);

        var function = new PascalFunction((Stmt.Function) stmts.get(0), interpreter.globals, false);

        interpreter.globals.define("fib", function);

        List<Object> args = new ArrayList<>();
        args.add(7.0);

        function.call(interpreter, args);
    }
}



