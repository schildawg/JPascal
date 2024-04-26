package com.craftinginterpreters.pascal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests Environment.
 */
public class EnvironmentTest {
    // Tests creating an environment, defining a variable and retrieving it.
    //
    @Test
    void testEnvironment() {
        var env = new Environment();

        env.define("test", 1);

        var result = env.get(new Token(TokenType.IDENTIFIER, "test", null, 0, 0, "test"));

        assertEquals(1, result);
    }

    // A variable defined in an environment should be able to be accessed from a lower scope.
    //
    @Test
    void testEnvironmentTwoDeep() {
        var globals = new Environment();
        var env = new Environment(globals);

        globals.define("test", 1);
        env.define("test", 2);

        var token = new Token(TokenType.IDENTIFIER, "test", null, 0, 0, "test");

        assertEquals(1, globals.get(token));
        assertEquals(2, env.get(token));
    }

    // Too often something goes wrong at 3 :)  So we are checking that a variable can be accessed from a scope three
    // levels deep.
    @Test
    void testEnvironmentThreeDeep() {
        var globals = new Environment();
        var env = new Environment(globals);
        var functionEnv = new Environment(env);

        globals.define("test", 1);

        var token = new Token(TokenType.IDENTIFIER, "test", null, 0, 0, "test");

        assertEquals(1, functionEnv.get(token));
    }

    @Test
    void testHopThreeDeep() {
        var globals = new Environment();
        var env = new Environment(globals);
        var functionEnv = new Environment(env);

        globals.define("test", 1);

        assertEquals(1, functionEnv.getAt(2, "test"));
    }

    // Assigning a value to a defined variable should change the value returned from get().
    //
    @Test
    void testEnvironmentAssign() {
        var globals = new Environment();
        var env = new Environment(globals);

        globals.define("test", 1);

        var token = new Token(TokenType.IDENTIFIER, "test", null, 0, 0, "test");

        env.assign(token, 2);
        assertEquals(2, env.get(token));
    }

    // Attempting to assign a value to an identifier with no variable defined should return a runtime error.
    //
    @Test
    void testAssignNotDefined() {
        var globals = new Environment();
        var env = new Environment(globals);

        var token = new Token(TokenType.IDENTIFIER, "test", null, 0, 0, "test");

        var ex = assertThrows(RuntimeError.class, () -> {
            env.assign(token, 2);
        });
        assertEquals("Undefined variable 'test'.", ex.getMessage());
    }

    // Attempting to get a value that is not defined should return a runtime error.
    //
    @Test
    void testGetNotDefined() {
        var globals = new Environment();
        var token = new Token(TokenType.IDENTIFIER, "test", null, 0, 0, "test");

        var ex = assertThrows(RuntimeError.class, () -> {
            globals.get(token);
        });
        assertEquals("Undefined variable 'test'.", ex.getMessage());
    }

    // Assigning a value to a variable defined in a higher scope should change that value.
    //
    @Test
    void testCascadeAssign() {
        var globals = new Environment();
        var env = new Environment(globals);
        var functionEnv = new Environment(env);

        globals.define("test", 1);

        var token = new Token(TokenType.IDENTIFIER, "test", null, 0, 0, "test");

        functionEnv.assign(token, 2);

        assertEquals(2, functionEnv.get(token));
    }
}
