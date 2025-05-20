package com.example.compliefx2;

/**
 * @author wyr on 2025/5/3
 */
public  class Token {
    public final int type;
    public final String value;
    public final int LineNumber;

    public Token(int type, String value, int lineNumber) {
        this.type = type;
        this.value = value;
        this.LineNumber = lineNumber;

    }

    public int getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public int getLineNumber() {
        return LineNumber;
    }

    @Override
    public String toString() {
        return "(" + type + ", " + value + ")";
    }
}
