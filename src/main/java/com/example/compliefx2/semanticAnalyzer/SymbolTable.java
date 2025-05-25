package com.example.compliefx2.semanticAnalyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 修复后的符号表管理器
 */
class SymbolTable {
    private List<Map<String, Symbol>> scopeStack;
    private int currentScope;
    // 新增：用于保存所有已声明过的符号（包括已退出作用域的）
    private List<Symbol> allDeclaredSymbols;

    public SymbolTable() {
        this.scopeStack = new ArrayList<>();
        this.currentScope = 0;
        this.allDeclaredSymbols = new ArrayList<>();
        // 添加全局作用域
        this.scopeStack.add(new HashMap<>());
    }

    // 进入新作用域
    public void enterScope() {
        currentScope++;
        scopeStack.add(new HashMap<>());
    }

    // 退出当前作用域
    public void exitScope() {
        if (currentScope > 0) {
            // 在删除作用域前，保存其中的符号到全局记录中
            Map<String, Symbol> currentScopeMap = scopeStack.get(scopeStack.size() - 1);
            for (Symbol symbol : currentScopeMap.values()) {
                // 避免重复添加（函数符号可能已经在外层作用域中）
                if (!containsSymbol(symbol.getName(), symbol.getScope())) {
                    allDeclaredSymbols.add(symbol);
                }
            }

            scopeStack.remove(scopeStack.size() - 1);
            currentScope--;
        }
    }

    // 在当前作用域添加符号
    public boolean addSymbol(Symbol symbol) {
        Map<String, Symbol> currentScopeMap = scopeStack.get(scopeStack.size() - 1);
        if (currentScopeMap.containsKey(symbol.getName())) {
            return false; // 重复声明
        }
        currentScopeMap.put(symbol.getName(), symbol);

        // 同时添加到全局记录中（对于当前仍活跃的符号）
        if (!containsSymbol(symbol.getName(), symbol.getScope())) {
            allDeclaredSymbols.add(symbol);
        }

        return true;
    }

    // 查找符号（从内到外查找作用域）
    public Symbol lookupSymbol(String name) {
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            Map<String, Symbol> scope = scopeStack.get(i);
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }
        return null;
    }

    // 获取当前作用域级别
    public int getCurrentScope() {
        return currentScope;
    }

    /**
     * 获取所有符号（包括已退出作用域的符号）
     */
    public List<Symbol> getAllSymbols() {
        List<Symbol> allSymbols = new ArrayList<>(allDeclaredSymbols);

        // 添加当前仍活跃的符号
        for (Map<String, Symbol> scope : scopeStack) {
            for (Symbol symbol : scope.values()) {
                if (!containsSymbol(symbol.getName(), symbol.getScope())) {
                    allSymbols.add(symbol);
                }
            }
        }

        return allSymbols;
    }

    /**
     * 检查是否已包含指定名称和作用域的符号
     */
    private boolean containsSymbol(String name, int scope) {
        for (Symbol symbol : allDeclaredSymbols) {
            if (symbol.getName().equals(name) && symbol.getScope() == scope) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取指定作用域的符号
     */
    public List<Symbol> getSymbolsInScope(int scopeLevel) {
        List<Symbol> symbols = new ArrayList<>();

        // 从全局记录中查找指定作用域的符号
        for (Symbol symbol : allDeclaredSymbols) {
            if (symbol.getScope() == scopeLevel) {
                symbols.add(symbol);
            }
        }

        // 也检查当前活跃的作用域
        if (scopeLevel >= 0 && scopeLevel < scopeStack.size()) {
            for (Symbol symbol : scopeStack.get(scopeLevel).values()) {
                if (!containsSymbol(symbol.getName(), symbol.getScope())) {
                    symbols.add(symbol);
                }
            }
        }

        return symbols;
    }

    /**
     * 获取当前作用域的符号
     */
    public List<Symbol> getCurrentScopeSymbols() {
        if (!scopeStack.isEmpty()) {
            return new ArrayList<>(scopeStack.get(scopeStack.size() - 1).values());
        }
        return new ArrayList<>();
    }

    /**
     * 清除所有符号记录（用于重新分析）
     */
    public void clear() {
        allDeclaredSymbols.clear();
        scopeStack.clear();
        currentScope = 0;
        scopeStack.add(new HashMap<>());
    }
}