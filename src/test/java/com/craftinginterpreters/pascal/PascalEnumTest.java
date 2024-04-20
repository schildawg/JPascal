package com.craftinginterpreters.pascal;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests PascalClass.
 */
public class PascalEnumTest {
    // Tests creating a Class.
    @Test
    void testCreate() {
        var uut = new PascalEnum("Color", "Red", 1);

        assertEquals("Red", uut.toString());
    }
}
