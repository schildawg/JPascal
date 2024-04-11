package com.craftinginterpreters.pascal;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests PascalClass.
 */
public class PascalInstanceTest {
    // Tests creating an Instance.
    //
    @Test
    void testCreate() {
        var klass = new PascalClass("Bagel", null, new HashMap<>());
        var uut = new PascalInstance(klass);

        assertEquals("Bagel instance", uut.toString());
    }

    // Test instance get field.
    //
    @Test
    void testInstanceGet() {
        var klass = new PascalClass("Bagel", null, new HashMap<>());
        var uut = new PascalInstance(klass);

        var token = new Token(TokenType.STRING, "ABC", null, 1);

        uut.set(token, 123.0);
        assertEquals(123.0, uut.get(token));
    }

    // Test instance get field undefined.
    //
    @Test
    void testInstanceGetUndefined() {
        var klass = new PascalClass("Bagel", null, new HashMap<>());
        var uut = new PascalInstance(klass);

        var ex = assertThrows(RuntimeError.class, () -> {
            uut.get(new Token(TokenType.STRING, "ABC", null, 1));
        });
        assertEquals("Undefined property 'ABC'.", ex.getMessage());
    }
}



