package com.craftinginterpreters.pascal;

/**
 * Token.
 */
public class Token {
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line;

    /**
     * Constructor.
     *
     * @param type the token type.
     * @param lexeme the lexeme.
     * @param literal the literal value.
     * @param line the line number of token.
     */
    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    @Override
    public String toString() {
        return type + " " + lexeme + " " + ((literal == null) ? "" : literal.toString());
    }
}