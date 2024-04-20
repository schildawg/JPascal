package com.craftinginterpreters.pascal;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests PascalArray.
 */
public class PascalArrayTest {
    // Tests creating a list, adding a value, getting the value, and checking the length.
    @Test
    void testList() {
        var uut = new PascalArray(1);

        var set = (PascalCallable) uut.get(new Token(TokenType.IDENTIFIER, "set", null, 0));
        assertEquals(2, set.arity());

        var args = new ArrayList<>();
        args.add(0);
        args.add("ABC");
        set.call(null, args);

        var get = (PascalCallable) uut.get(new Token(TokenType.IDENTIFIER, "get", null, 0));
        assertEquals(1, get.arity());

        args = new ArrayList<>();
        args.add(0);
        var value = get.call(null, args);
        assertEquals("ABC", value);

        var length = (double) uut.get(new Token(TokenType.IDENTIFIER, "length", null, 0));
        assertEquals(1, length);

        assertEquals("[ABC]", uut.toString());
    }

    // Get invalid property should fail.
    //
    @Test
    void testGetInvalidProperty() {
        var ex = assertThrows(RuntimeError.class, () -> {
            var uut = new PascalArray(1);

            uut.get(new Token(TokenType.IDENTIFIER, "invalid", null, 0));
        });
        assertEquals("Undefined property 'invalid'.", ex.getMessage());
    }

    // Set invalid property should fail.
    //
    @Test
    void testSetInvalidProperty() {
        var ex = assertThrows(RuntimeError.class, () -> {
            var uut = new PascalArray(1);

            uut.set(new Token(TokenType.IDENTIFIER, "invalid", null, 0), 1);
        });
        assertEquals("Can't add properties to arrays.", ex.getMessage());
    }
}
