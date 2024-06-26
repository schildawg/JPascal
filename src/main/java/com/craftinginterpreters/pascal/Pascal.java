package com.craftinginterpreters.pascal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


public class Pascal {
    public static boolean hadError = false;
    public static boolean hadRuntimeError = false;
    public static String lastError;

    private static final Interpreter interpreter = new Interpreter();

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jpascal [script]");
        }
        else if (args.length == 1) {
            runFile(args[0]);
        }
        else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        var fileName = Console.ANSI_CYAN + path + Console.ANSI_RESET;
        Console.header(path);
        Console.info("Building...");
        Console.info("");
        Console.success(path);
        var bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
    }

    private static void runPrompt() throws IOException {
        var input = new InputStreamReader(System.in);
        var reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            var line = reader.readLine();
            if (line == null) {
                break;
            }
            run(line);
        }
    }

    private static void run(String source) {
        var scanner = new Scanner("REPL", source);
        var tokens = scanner.scanTokens();

        var parser = new Parser(tokens);
        List<Stmt> statements = parser.parseWithError();

        if (hadError) {
            Console.info(Console.BAR);
            Console.info(Console.ANSI_RED + "BUILD FAILED" + Console.ANSI_RESET);
            Console.info(Console.BAR);
            return;
        }

        Console.info(Console.BAR);
        Console.info(Console.ANSI_GREEN + "BUILD SUCCESS" + Console.ANSI_RESET);
        Console.info(Console.BAR);

        var resolver = new Resolver(interpreter);
        resolver.resolve(statements);

        try {
            var typeEnforcer = new TypeChecker();
            typeEnforcer.resolve(statements);
        }
        catch (RuntimeError e) {
            Console.error(e);
            hadError = true;
        }
        // Stop if there was a resolution error.
        if (hadError) return;
        interpreter.runTests(statements);
        //interpreter.interpret(statements);
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        lastError = "[line " + line + "] Error" + where + ": " + message;
        hadError = true;
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } 
        else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    static void runtimeError(RuntimeError error) {
        hadRuntimeError = true;
    }
}
