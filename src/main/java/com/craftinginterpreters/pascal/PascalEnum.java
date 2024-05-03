package com.craftinginterpreters.pascal;

class PascalEnum {
    public final String enumName;
    public final String name;
    public final int value;

    public PascalEnum(String enumName, String name, int value) {
        this.enumName = enumName;
        this.name = name;
        this.value = value;
    }

    public String toString() {
        return name;
    }
}
