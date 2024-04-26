package com.craftinginterpreters.pascal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests Token.
 */
public class TokenTest {
    // Tests creating a new token.
    //
    @Test
    void testCreateToken() {
        var token = new Token(TokenType.STRING, "ABC", null, 1, 0, "test");

        assertEquals(TokenType.STRING, token.type);
        assertEquals("ABC", token.lexeme);
        assertNull(token.literal);
        assertEquals(1, token.line);
    }

    // Tests Token.toString()
    //
    @Test
    void testToString() {
        var token = new Token(TokenType.STRING, "ABC", null, 1, 0, "test");

        assertEquals("STRING ABC ", token.toString());
    }
}
