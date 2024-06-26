package com.craftinginterpreters.pascal;

import com.craftinginterpreters.pascal.nativefunction.PascalMap;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests PascalList.
 */
public class PascalMapTest {
    // Tests creating a Map, adding a value, getting the value, and checking the length.
    @Test
    void testMap() {
        var uut = new PascalMap();

        var add = (PascalCallable) uut.get(new Token(TokenType.IDENTIFIER, "put", null, 0, 0, "test"));
        assertEquals(2, add.arity());

        var args = new ArrayList<>();
        args.add(1);
        args.add("ABC");
        add.call(null, args);

        var get = (PascalCallable) uut.get(new Token(TokenType.IDENTIFIER, "get", null, 0, 0, "test"));
        assertEquals(1, get.arity());

        args = new ArrayList<>();
        args.add(1);
        var value = get.call(null, args);
        assertEquals("ABC", value);

        var contains = (PascalCallable) uut.get(new Token(TokenType.IDENTIFIER, "contains", null, 0, 0, "test"));
        assertEquals(1, contains.arity());
        args = new ArrayList<>();
        args.add(1);
        var result = contains.call(null, args);
        assertEquals(true, result);

        assertEquals("{1=ABC}", uut.toString());
    }

    // Get invalid property should fail.
    //
    @Test
    void testGetInvalidProperty() {
        var ex = assertThrows(RuntimeError.class, () -> {
            var uut = new PascalMap();

            uut.get(new Token(TokenType.IDENTIFIER, "invalid", null, 0, 0, "test"));
        });
        assertEquals("Undefined property 'invalid'.", ex.getMessage());
    }

    // Set invalid property should fail.
    //
    @Test
    void testSetInvalidProperty() {
          var ex = assertThrows(RuntimeError.class, () -> {
            var uut = new PascalMap();

            uut.set(new Token(TokenType.IDENTIFIER, "invalid", null, 0, 0, "test"), 1);
        });
        assertEquals("Can't add properties to maps.", ex.getMessage());
    }
}
