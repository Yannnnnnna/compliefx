package com.example.compliefx2;

/**
 * @author wyr on 2025/5/3
 */
public  class Token {
    public final int type;
    public final String value;
    public final int LineNumber;
    public final int columnNumber;

    public Token(int type, String value, int lineNumber, int columnNumber) {
        this.type = type;
        this.value = value;
        this.LineNumber = lineNumber;
        this.columnNumber = columnNumber;

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

    public int getColumnNumber() {
        return columnNumber;
    }

    @Override
    public String toString() {
        return "(" + type + ", " + value + ")";
    }
}
