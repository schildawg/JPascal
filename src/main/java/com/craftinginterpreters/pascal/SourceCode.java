package com.craftinginterpreters.pascal;

import java.util.HashMap;
import java.util.Map;

/**
 * SourceCode.  Maintains a copy of the compiled code by file and line.  This is used in conjunction with the
 * Token to provide console feedback for any compile or runtime errors.
 */
public class SourceCode {
    private Map<String, Map<Integer, String>> code = new HashMap<>();

    public static final SourceCode INSTANCE = new SourceCode();

    private SourceCode() {
    }

    /**
     * Adds a line of code.
     * @param fileName name of file.
     * @param lineNumber line number.
     * @param line the line of code.
     */
    public void addLine(String fileName, int lineNumber, String line) {
        if (!code.containsKey(fileName)) {
            code.put(fileName, new HashMap<>());
        }
        var source = code.get(fileName);
        source.put(lineNumber, line);
    }

    /**
     * Gets a line of code from a source file.
     * @param fileName name of file.
     * @param lineNumber line number.
     * @return the line of code.
     */
    public String getLine(String fileName, int lineNumber) {
        if (code.containsKey(fileName)) {
            var map = code.get(fileName);
            if (map.containsKey(lineNumber)) {
                return map.get(lineNumber);
            }
        }
        return "";
    }
}
