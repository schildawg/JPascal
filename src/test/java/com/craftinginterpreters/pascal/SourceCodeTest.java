package com.craftinginterpreters.pascal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests SourceCode.
 */
public class SourceCodeTest {
    // Tests SourceCode.
    //
    @Test
    void testSourceCode() {
        SourceCode.INSTANCE.addLine("Test.pas", 1, "var Abc := 1;");
        SourceCode.INSTANCE.addLine("Test.pas", 2, "WriteLn(Abc);");

        SourceCode.INSTANCE.addLine("Scanner.pas", 1, "// This is a comment");

        assertEquals("var Abc := 1;", SourceCode.INSTANCE.getLine("Test.pas", 1));
        assertEquals("WriteLn(Abc);", SourceCode.INSTANCE.getLine("Test.pas", 2));
        assertEquals("", SourceCode.INSTANCE.getLine("Test.pas", 3));

        assertEquals("", SourceCode.INSTANCE.getLine("Test2.pas", 1));
    }
}
