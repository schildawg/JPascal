package com.craftinginterpreters.pascal.nativefunction;

import com.craftinginterpreters.pascal.Console;

public class NativeFunctions {
    /// Executes the Write procedure, then outputs an end-of-line marker to output.
    ///
    public static void WriteLn(Object text) {
       System.out.println(stringify(text));
    }

    /// Writes a value to output.
    ///
    public static void Write(Object text) {
        System.out.print(stringify(text));
    }

    /// Converts a value to its string representation.
    ///
    public static String Str(Object obj) {
        return obj == null ? "nil" : obj.toString();
    }

    /// Writes a class type to Debug console.
    ///
    public static void Debug(String fun, Object clazz) {
        Console.debug(fun + ": " + clazz.getClass().getName());
    }

    /// Returns a substring of a string.
    ///
    public static String Copy(String text, int begin, int end) {
        return text.substring(begin, end);
    }

    /// Returns the length of a string.
    ///
    public static int Length(String text) {
        return text.length();
    }

    public static double clock() {
        return (double) System.currentTimeMillis() / 1000.00;
    }

    /// Returns an Array.
    ///
    public static PascalArray Array(int size) {
        return new PascalArray(size);
    }

    /// Returns a List.
    ///
    public static PascalList List() {
        return new PascalList();
    }

    /// Returns a Map.
    ///
    public static PascalMap Map() {
        return new PascalMap();
    }


    private static String stringify(Object object) {
        if (object == null) return "nil";

        return object.toString();
    }
}