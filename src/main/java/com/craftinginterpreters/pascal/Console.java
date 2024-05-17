package com.craftinginterpreters.pascal;

/**
 * Console.  Outputs formatted messages.
 */
public class Console {
    public static final String BAR = "------------------------------------------------------------------------";

    // Console colors.
    //
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    // Outputs a header line:
    // [INFO] ------------------------------[ Test.pas ]------------------------------
    //
    public static void header(String name) {
        var length = BAR.length();
        length -= 4;
        length -= name.length();
        length = length / 2;
        info("-".repeat(length) + "[ " + ANSI_CYAN + name + ANSI_RESET + " ]" + "-".repeat(length));
    }

    // Outputs a subheader:
    // [INFO] < Scanner.pas >
    //
    public static void subheader(String name) {
        info("< " + ANSI_CYAN + name + ANSI_RESET + " >" );
    }
    public static void info(String text) {
        write(ANSI_WHITE + "[" + ANSI_BLUE + "INFO" + ANSI_WHITE + "] " + ANSI_RESET + text);
    }

    // Outputs a build success:
    // [INFO] TokenType.pas ................................................. SUCCESS
    //
    public static void success(String name) {
        var length = BAR.length();
        length -= 10;
        length -= name.length();

        info(name + " " + ".".repeat(length) + ANSI_GREEN + " SUCCESS" + ANSI_RESET);
    }

    // Outputs a build failure:
    // [INFO] Token.pas ..................................................... FAILED
    //
    public static void fail(String name) {
        var length = BAR.length();
        length -= 10;
        length -= name.length();

        info(name + " " + ".".repeat(length) + ANSI_RED + " FAILED" + ANSI_RESET);
    }

    // Outputs compile or runtime error:
    // [ERROR] Assertion 'left = right' failed.
    // [ERROR] 16 ║    AssertTrue(False);
    // [ERROR]    ║
    //
    public static void error(RuntimeError err) {
        var line = err.token.line;
        var file = err.token.fileName;
        var text = SourceCode.INSTANCE.getLine(file, line);

        var lineLength = String.valueOf(line).length();
        write(ANSI_WHITE + "[" + ANSI_RED + "ERROR" + ANSI_WHITE + "] " + ANSI_RESET + err.token.fileName + ": " + err.getMessage());
        write(ANSI_WHITE + "[" + ANSI_RED + "ERROR" + ANSI_WHITE + "] " + ANSI_RESET + line + " ║ " + text);
        write(ANSI_WHITE + "[" + ANSI_RED + "ERROR" + ANSI_RESET + "] " + " ".repeat(lineLength) + " ║"  + ANSI_RED + " ".repeat(err.token.offset + 1) + "^".repeat(err.token.lexeme.length()) + ANSI_RESET);
    }

    // Outputs a debug message
    // [DEBUG] Abc is equal to 123.
    //
    public static void debug(String text) {
        write(ANSI_WHITE + "[" + ANSI_YELLOW + "DEBUG" + ANSI_WHITE + "] " + ANSI_RESET + text);
    }

    private static void write(String string) {
        System.out.println(string);
    }
}
