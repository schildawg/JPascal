package com.craftinginterpreters.pascal;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests PascalClass.
 */
public class PascalClassTest {
    // Tests creating a Class.
    @Test
    void testCreate() {
        var uut = new PascalClass("Test", null, new HashMap<>());

        assertEquals("Test", uut.toString());
    }
}
