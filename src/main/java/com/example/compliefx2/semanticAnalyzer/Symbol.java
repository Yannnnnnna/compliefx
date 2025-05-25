package com.example.compliefx2.semanticAnalyzer;

/**
 * @author wyr on 2025/5/25
 */

import java.util.ArrayList;
import java.util.List;

/**
 * 符号信息类
 */
class Symbol {
    private String name;
    private String type;
    private int scope;
    private boolean isInitialized;
    private boolean isFunction;
    private List<String> paramTypes;
    private int lineNumber;

    public Symbol(String name, String type, int scope, int lineNumber) {
        this.name = name;
        this.type = type;
        this.scope = scope;
        this.isInitialized = false;
        this.isFunction = false;
        this.paramTypes = new ArrayList<>();
        this.lineNumber = lineNumber;
    }

    // Getter和Setter方法
    public String getName() { return name; }
    public String getType() { return type; }
    public int getScope() { return scope; }
    public boolean isInitialized() { return isInitialized; }
    public void setInitialized(boolean initialized) { this.isInitialized = initialized; }
    public boolean isFunction() { return isFunction; }
    public void setFunction(boolean function) { this.isFunction = function; }
    public List<String> getParamTypes() { return paramTypes; }
    public int getLineNumber() { return lineNumber; }
}
