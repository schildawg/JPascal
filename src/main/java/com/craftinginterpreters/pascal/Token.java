package com.craftinginterpreters.pascal;

/**
 * Token.
 */
public class Token {
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line;
    final int offset;
    final String fileName;

    /**
     * Constructor.
     *
     * @param type the token type.
     * @param lexeme the lexeme.
     * @param literal the literal value.
     * @param line the line number of token.
     * @param offset offset of the token on the line.
     * @param fileName source file.
     */
    Token(TokenType type, String lexeme, Object literal, int line, int offset, String fileName) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
        this.offset = offset;
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return type + " " + lexeme + " " + ((literal == null) ? "" : literal.toString());
    }
}