package com.craftinginterpreters.pascal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests TypeLookup.
 */
public class TypeLookupTest {
    // Tests adding a global.
    //
    @Test
    void testGlobal() {
        var uut = new TypeLookup();
        uut.setType("Abc", "String");

        var type = uut.getType("Abc");

        assertEquals("String", type);
    }

    // Tests scoped lookups
    //
    @Test
    void testScopedLookup() {
        var uut = new TypeLookup();
        uut.beginScope();

        uut.setType("Abc", "String");

        var type = uut.getType("Abc");

        assertEquals("String", type);
    }

    // Tests scoped lookup chained
    //
    @Test
    void testScopedLookupChained() {
        var uut = new TypeLookup();
        uut.setType("Abc", "String");
        uut.beginScope();
        uut.beginScope();

        var type = uut.getType("Abc");

        assertEquals("String", type);
    }

    // Tests out-of-scope.
    //
    @Test
    void testOutOfScope() {
        var uut = new TypeLookup();
        uut.beginScope();
        uut.setType("Abc", "String");
        uut.endScope();

        var type = uut.getType("Abc");

        assertNull(type);
    }
}
