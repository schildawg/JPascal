package com.craftinginterpreters.pascal;

import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ScannerTest {
    // Scans each of the single character tokens, verifying the correct token types are returned, and ends
    // with an EOF.
    @Test
    void scanTokensTest() {
        var scanner = new Scanner("()[],.-+;*");

        var tokens = scanner.scanTokens();

        assertEquals(TokenType.LEFT_PAREN, tokens.get(0).type);
        assertEquals(TokenType.RIGHT_PAREN, tokens.get(1).type);

        assertEquals(TokenType.LEFT_BRACKET, tokens.get(2).type);
        assertEquals(TokenType.RIGHT_BRACKET, tokens.get(3).type);

        assertEquals(TokenType.COMMA, tokens.get(4).type);
        assertEquals(TokenType.DOT, tokens.get(5).type);
        assertEquals(TokenType.MINUS, tokens.get(6).type);
        assertEquals(TokenType.PLUS, tokens.get(7).type);

        assertEquals(TokenType.SEMICOLON, tokens.get(8).type);
        assertEquals(TokenType.STAR, tokens.get(9).type);

        assertEquals(TokenType.EOF, tokens.get(10).type);
    }

    // Scans the two character operators, verifying the correct token types are returned.  Also checks that
    // the individual characters continue to be scanned properly.  Implicitly checks that whitespace is ignored.
    //
    @Test
    void scanOperatorsTest() {
        var scanner = new Scanner("= < > := <= >=");

        var tokens = scanner.scanTokens();

        int count = 0;
        assertEquals(TokenType.EQUAL, tokens.get(count++).type);

        assertEquals(TokenType.LESS, tokens.get(count++).type);
        assertEquals(TokenType.GREATER, tokens.get(count++).type);

        assertEquals(TokenType.ASSIGN, tokens.get(count++).type);
        assertEquals(TokenType.LESS_EQUAL, tokens.get(count++).type);
        assertEquals(TokenType.GREATER_EQUAL, tokens.get(count++).type);

        assertEquals(TokenType.EOF, tokens.get(count).type);
    }


    // Tests that a comment is ignored until the end of a line.
    //
    @Test
    void scanCommentTest() {
        var scanner = new Scanner("// this is a comment");

        var tokens = scanner.scanTokens();

        assertEquals(TokenType.EOF, tokens.get(0).type);
    }

    // Test that the line counter is increased when scanning an end-of-line character (\n).
    //
    @Test
    void scanNewlineTest() {
        var scanner = new Scanner("test\ntest2");

        var tokens = scanner.scanTokens();

        assertEquals(1, tokens.get(0).line);
        assertEquals(2, tokens.get(1).line);
    }

    // Test that quotation mark returns a String token type, with a lexeme including the quotation marks, and
    // the literal value a String object without them.
    //
    @Test
    void ScanStringTest() {
        var scanner = new Scanner("'ABC'");

        var tokens = scanner.scanTokens();
        var token = tokens.get(0);

        assertEquals(TokenType.STRING, token.type);
        assertEquals("'ABC'", token.lexeme);
        assertEquals("ABC", token.literal);
    }

    // If the end of the file is reached without a terminating quotation mark, an error should be sent to
    // Lox.
    //
    @Test
    void scanUnterminatedStringTest() {
        var scanner = new Scanner("'ABC");

        scanner.scanTokens();

        assertTrue(Pascal.hadError);
        assertEquals("[line 1] Error: Unterminated string.", Pascal.lastError);
    }

    // Tests an integer number.
    //
    @Test
    void scanIntegerTest() {
        var scanner = new Scanner("123");

        var tokens = scanner.scanTokens();
        var token = tokens.get(0);

        assertEquals(TokenType.INTEGER, token.type);
        assertEquals("123", token.lexeme);
        assertEquals(123, token.literal);
    }

    // Tests a floating number.
    //
    @Test
    void scanFloatTest() {
        var scanner = new Scanner("123.0");

        var tokens = scanner.scanTokens();
        var token = tokens.get(0);

        assertEquals(TokenType.NUMBER, token.type);
        assertEquals("123.0", token.lexeme);
        assertEquals(123.0, token.literal);
    }

    // Tests scanning a Char.
    //
    @Test
    void scanCharTest() {
        var scanner = new Scanner("#0");

        var tokens = scanner.scanTokens();
        var token = tokens.get(0);

        assertEquals(TokenType.CHAR, token.type);
        assertEquals("#0", token.lexeme);
        assertEquals(0, token.literal);
    }

    // Should report an error if a character contains a non-digut.
    ///
    @Test
    void scanCharInvalidTest() {
        var scanner = new Scanner("#F");
        scanner.scanTokens();

        assertTrue(Pascal.hadError);
        assertEquals("[line 1] Error: Invalid character: F", Pascal.lastError);
    }

    // If a period is encountered while scanning numbers, it should scan for additional numbers for a decimal
    // value.  Returns a Number with the String value in lexeme.
    //
    @Test
    void scanNumberDecimalTest() {
        var scanner = new Scanner("3.14");

        var tokens = scanner.scanTokens();
        var token = tokens.get(0);

        assertEquals(TokenType.NUMBER, token.type);
        assertEquals("3.14", token.lexeme);
        assertEquals(3.14, token.literal);
    }

    // Test scanning an identifier.  The lexeme should contain the name, and the literal value should be None.
    //
    @Test
    void scanIdentifierTest() {
        var scanner = new Scanner("test");

        var tokens = scanner.scanTokens();
        var token = tokens.get(0);

        assertEquals(TokenType.IDENTIFIER, token.type);
        assertEquals("test", token.lexeme);
        assertNull(token.literal);
    }

    // Tests that all of Lox's keywords are properly distinguished from identifiers.
    //
    @Test
    void scanKeywordsTest() {
        var scanner = new Scanner("and class else false for function if nil or print exit super this true var while begin end");

        var tokens = scanner.scanTokens();

        int count = 0;
        assertEquals(TokenType.AND,    tokens.get(count++).type);
        assertEquals(TokenType.CLASS,  tokens.get(count++).type);
        assertEquals(TokenType.ELSE,   tokens.get(count++).type);
        assertEquals(TokenType.FALSE,  tokens.get(count++).type);
        assertEquals(TokenType.FOR,    tokens.get(count++).type);
        assertEquals(TokenType.FUNCTION,tokens.get(count++).type);
        assertEquals(TokenType.IF,     tokens.get(count++).type);
        assertEquals(TokenType.NIL,    tokens.get(count++).type);
        assertEquals(TokenType.OR,     tokens.get(count++).type);
        assertEquals(TokenType.PRINT,  tokens.get(count++).type);
        assertEquals(TokenType.EXIT, tokens.get(count++).type);
        assertEquals(TokenType.SUPER,  tokens.get(count++).type);
        assertEquals(TokenType.THIS,   tokens.get(count++).type);
        assertEquals(TokenType.TRUE,   tokens.get(count++).type);
        assertEquals(TokenType.VAR,    tokens.get(count++).type);
        assertEquals(TokenType.WHILE,  tokens.get(count++).type);
        assertEquals(TokenType.BEGIN,  tokens.get(count++).type);
        assertEquals(TokenType.END,  tokens.get(count++).type);
        assertEquals(TokenType.EOF,    tokens.get(count).type);
    }

    // Should report an error to Lox if an unexpected character is scanned.
    ///
    @Test
    void scanUnexpectedCharacterTest() {
        var scanner = new Scanner("%");
        scanner.scanTokens();

        assertTrue(Pascal.hadError);
        assertEquals("[line 1] Error: Unexpected character: %", Pascal.lastError);
    }

    @Test
    void scanScannerTest() throws Exception {
        var bytes = Files.readAllBytes(Paths.get("src/test/resources/Scanner.pas"));
        var s = new String(bytes, Charset.defaultCharset());

        var scanner = new Scanner(s);
        var tokens = scanner.scanTokens();
        for (var token : tokens) {
            System.out.println("[" + token.line + "] " + token);
        }
    }
}
