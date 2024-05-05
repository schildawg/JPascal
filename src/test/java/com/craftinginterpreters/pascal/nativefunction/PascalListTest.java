package com.craftinginterpreters.pascal.nativefunction;

import com.craftinginterpreters.pascal.PascalCallable;
import com.craftinginterpreters.pascal.RuntimeError;
import com.craftinginterpreters.pascal.Token;
import com.craftinginterpreters.pascal.TokenType;
import com.craftinginterpreters.pascal.nativefunction.PascalList;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests PascalList.
 */
public class PascalListTest {
    // Tests creating a list, adding a value, getting the value, and checking the length.
    @Test
    void testList() {
        var uut = new PascalList();

        var add = (PascalCallable) uut.get(new Token(TokenType.IDENTIFIER, "add", null, 0, 0, "test"));
        assertEquals(1, add.arity());

        var args = new ArrayList<>();
        args.add("ABC");
        add.call(null, args);

        var get = (PascalCallable) uut.get(new Token(TokenType.IDENTIFIER, "get", null, 0, 0, "test"));
        assertEquals(1, get.arity());

        args = new ArrayList<>();
        args.add(0);
        var value = get.call(null, args);
        assertEquals("ABC", value);

        var length = (int) uut.get(new Token(TokenType.IDENTIFIER, "length", null, 0, 0, "test"));
        assertEquals(1, length);

        assertEquals("[ABC]", uut.toString());
    }

    // Get invalid property should fail.
    //
    @Test
    void testGetInvalidProperty() {
        var ex = assertThrows(RuntimeError.class, () -> {
            var uut = new PascalList();

            uut.get(new Token(TokenType.IDENTIFIER, "invalid", null, 0, 0, "test"));
        });
        assertEquals("Undefined property 'invalid'.", ex.getMessage());
    }

    // Set invalid property should fail.
    //
    @Test
    void testSetInvalidProperty() {
          var ex = assertThrows(RuntimeError.class, () -> {
            var uut = new PascalList();

            uut.set(new Token(TokenType.IDENTIFIER, "invalid", null, 0, 0, "test"), 1);
        });
        assertEquals("Can't add properties to lists.", ex.getMessage());
    }
}
