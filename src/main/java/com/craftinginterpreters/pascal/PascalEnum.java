package com.craftinginterpreters.pascal;

class PascalEnum {
    private final String enumName;
    private final String name;
    private final int value;

    public PascalEnum(String enumName, String name, int value) {
        this.enumName = enumName;
        this.name = name;
        this.value = value;
    }

    public String toString() {
        return name;
    }
}
