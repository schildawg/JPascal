package com.craftinginterpreters.pascal;

public enum TokenType {
    // Single-character tokens.
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACKET, RIGHT_BRACKET,
    COMMA, DOT, MINUS, PLUS, COLON, SEMICOLON, SLASH, STAR,

    // One or two character tokens
    NOT_EQUAL, EQUAL, ASSIGN,
    GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,

    // literals
    IDENTIFIER, STRING, CHAR, NUMBER, INTEGER,

    // Keywords
    AND, CLASS, CONST, DO, ELSE, EXIT, FALSE, FOR, IF, NIL, NOT, OR,
    PRINT, SUPER, THEN, THIS, TRUE, TYPE, UNIT, USES, VAR, WHILE,

    CONSTRUCTOR, FUNCTION, PROCEDURE,

    CASE, OF,

    BEGIN, END,

    EOF
}