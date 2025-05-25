package com.example.compliefx2.semanticAnalyzer;

/**
 * @author wyr on 2025/5/25
 */
/**
 * 语义错误类
 */
class SemanticError {
    public enum ErrorType {
        DUPLICATE_DECLARATION,
        UNDECLARED_VARIABLE,
        TYPE_MISMATCH,
        INVALID_ASSIGNMENT,
        UNINITIALIZED_VARIABLE, FUNCTION_ERROR
    }

    private ErrorType type;
    private String message;
    private int lineNumber;

    public SemanticError(ErrorType type, String message, int lineNumber) {
        this.type = type;
        this.message = message;
        this.lineNumber = lineNumber;
    }

    public ErrorType getType() { return type; }
    public String getMessage() { return message; }
    public int getLineNumber() { return lineNumber; }

    @Override
    public String toString() {
        return String.format("语义错误 (行%d): %s", lineNumber, message);
    }
}
