package com.craftinginterpreters.pascal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.pascal.TokenType.*;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("function", FUNCTION);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("not",    NOT);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("exit",   EXIT);
        keywords.put("super",  SUPER);
        keywords.put("then",   THEN);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);

        keywords.put("begin",  BEGIN);
        keywords.put("end",    END);

        keywords.put("do",     DO);

        keywords.put("unit",  UNIT);
        keywords.put("uses",  USES);
        keywords.put("const", CONST);
    }
    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '[': addToken(LEFT_BRACKET); break;
            case ']': addToken(RIGHT_BRACKET); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '+': addToken(PLUS); break;
            case '-': addToken(MINUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '=': addToken(EQUAL); break;

            case ':':
                addToken(match('=') ? ASSIGN : COLON);
                break;

            case '<':
                if (match('=')) {
                    addToken(LESS_EQUAL);
                }
                else if (match('>')) {
                    addToken(NOT_EQUAL);
                }
                else {
                    addToken(LESS);
                }
                break;

            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;

            case '/':
                if (match('/')) {
                    // A comment goes until the end of the line
                    while (peek() != '\n' && ! isAtEnd()) advance();
                }
                else {
                    addToken(SLASH);
                }
                break;

            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace
                break;

            case '\n':
                line++;
                break;

            case '\'': string(); break;

            case '#': char_(); break;

            default:
                if (isDigit(c)) {
                    number();
                }
                else if (isAlpha(c)) {
                    identifier();
                }
                else {
                    Pascal.error(line, "Unexpected character: " + c);
                }
                break;
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        var text = source.substring(start, current);
        var type = keywords.get(text.toLowerCase());
        if (type == null) {
            type = IDENTIFIER;
        }

        addToken(type);
    }

    private void number() {
        while (isDigit(peek())) advance();

        // look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            advance();
        }

        while (isDigit(peek())) advance();

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void string() {
        while (peek() != '\'' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            Pascal.error(line, "Unterminated string.");
            return;
        }

        advance(); // the closing "

        // Trim the surrounding quotes
        String value = source.substring(start + 1, current -1);
        addToken(STRING, value);
    }

    private void char_() {
        if (!isDigit(peek()) || isAtEnd()) {
            Pascal.error(line, "Invalid character.");
            return;
        }

        while (isDigit(peek())) advance();

        addToken(CHAR, Integer.parseInt(source.substring(start + 1, current)));
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 > source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_' || c == '?';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c < '9';
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        var text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
