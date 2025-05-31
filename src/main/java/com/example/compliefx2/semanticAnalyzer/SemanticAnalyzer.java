package com.example.compliefx2.semanticAnalyzer;

/**
 * @author wyr on 2025/5/20
 */
import com.example.compliefx2.ASTNode;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * 语义分析器主类
 */
public class SemanticAnalyzer implements ASTNode.ASTVisitor<Void> {
    private SymbolTable symbolTable;
    private List<SemanticError> errors;
    private boolean hasErrors;

    public SemanticAnalyzer() {
        this.symbolTable = new SymbolTable();
        this.errors = new ArrayList<>();
        this.hasErrors = false;
    }

    // 开始语义分析
    public boolean analyze(ASTNode root) {
        if (root == null) {
            return false;
        }

        try {
            visit(root);
        } catch (Exception e) {
            addError(SemanticError.ErrorType.FUNCTION_ERROR,
                    "语义分析过程中发生错误: " + e.getMessage(), 0);
        }

        return !hasErrors;
    }

    // 获取错误列表
    public List<SemanticError> getErrors() {
        return errors;
    }

    // 获取符号表（用于调试）
    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    // 添加错误
    private void addError(SemanticError.ErrorType type, String message, int lineNumber) {
        errors.add(new SemanticError(type, message, lineNumber));
        hasErrors = true;
    }

    // 访问者模式实现
    @Override
    public Void visit(ASTNode node) {
        if (node == null) {
            return null;
        }

        // 添加调试信息
        System.out.println("访问节点: " + node.getType() +
                (node.getValue() != null ? "(" + node.getValue() + ")" : ""));

        switch (node.getType()) {
            case PROGRAM:
                visitProgram(node);
                break;
            case FUNCTION_DEF:
                visitFunctionDef(node);
                break;
            case DECLARATION_STMT:
                visitDeclaration(node);
                break;
            case EXPRESSION_STMT:
                visitExpressionStmt(node);
                break;
            case ASSIGNMENT:
                visitAssignment(node);
                break;
            case IDENTIFIER:
                visitIdentifier(node);
                break;
            case BLOCK_STMT:
                visitBlockStmt(node);
                break;
            case IF_STMT:
                visitIfStmt(node);
                break;
            case WHILE_STMT:
                visitWhileStmt(node);
                break;
            case FOR_STMT:
                visitForStmt(node);
                break;
            case BINARY_OP:
                visitBinaryOp(node);
                break;
            case INT_LITERAL:
            case FLOAT_LITERAL:
            case STRING_LITERAL:
            case CHAR_LITERAL:
                // 字面量不需要特殊处理
                break;
            default:
                // 递归访问子节点
                for (ASTNode child : node.getChildren()) {
                    visit(child);
                }
                break;
        }

        return null;
    }

    private void visitProgram(ASTNode node) {
        // 访问程序的所有子节点
        for (ASTNode child : node.getChildren()) {
            visit(child);
        }
    }

    private void visitFunctionDef(ASTNode node) {
        if (node.getChildren().size() < 3) {
            addError(SemanticError.ErrorType.FUNCTION_ERROR,
                    "函数定义格式错误", node.getLineNumber());
            return;
        }

        String returnType = node.getValue(); // void, int等
        ASTNode nameNode = node.getChild(0);
        String functionName = nameNode.getValue();

        // 创建函数符号
        Symbol functionSymbol = new Symbol(functionName, returnType,
                symbolTable.getCurrentScope(), node.getLineNumber());
        functionSymbol.setFunction(true);

        // 添加到符号表
        if (!symbolTable.addSymbol(functionSymbol)) {
            addError(SemanticError.ErrorType.DUPLICATE_DECLARATION,
                    "重复声明函数: " + functionName, node.getLineNumber());
        }

        // 进入函数作用域
        symbolTable.enterScope();

        // 处理参数列表（如果有）
        ASTNode paramList = node.getChild(1);
        if (paramList != null && paramList.getType() == ASTNode.NodeType.PARAMETER_LIST) {
            visitParameterList(paramList, functionSymbol);
        }

        // 处理函数体
        ASTNode body = node.getChild(2);
        if (body != null) {
            visit(body);
        }

        // 退出函数作用域
        symbolTable.exitScope();
    }

    private void visitParameterList(ASTNode paramList, Symbol functionSymbol) {
        // 处理参数列表，添加参数到当前作用域
        for (ASTNode param : paramList.getChildren()) {
            if (param.getType() == ASTNode.NodeType.DECLARATION_STMT) {
                String paramType = param.getValue();
                ASTNode paramName = param.getChild(0);
                if (paramName != null) {
                    Symbol paramSymbol = new Symbol(paramName.getValue(), paramType,
                            symbolTable.getCurrentScope(), param.getLineNumber());
                    paramSymbol.setInitialized(true); // 参数默认已初始化

                    if (!symbolTable.addSymbol(paramSymbol)) {
                        addError(SemanticError.ErrorType.DUPLICATE_DECLARATION,
                                "重复声明参数: " + paramName.getValue(), param.getLineNumber());
                    }

                    functionSymbol.getParamTypes().add(paramType);
                }
            }
        }
    }

    private void visitDeclaration(ASTNode node) {
        String type = node.getValue(); // int, float等
        System.out.println("处理声明语句，类型: " + type);

        for (ASTNode child : node.getChildren()) {
            System.out.println("声明语句的子节点: " + child.getType() +
                    (child.getValue() != null ? "(" + child.getValue() + ")" : ""));

            if (child.getType() == ASTNode.NodeType.IDENTIFIER) {
                String varName = child.getValue();

                // 创建变量符号
                Symbol symbol = new Symbol(varName, type,
                        symbolTable.getCurrentScope(), node.getLineNumber());

                // 添加到符号表
                if (!symbolTable.addSymbol(symbol)) {
                    addError(SemanticError.ErrorType.DUPLICATE_DECLARATION,
                            "重复声明变量: " + varName, node.getLineNumber());
                } else {
                    System.out.println("声明变量: " + varName + " (类型: " + type + ")");
                }
            } else if (child.getType() == ASTNode.NodeType.ASSIGNMENT) {
                // 处理声明中的赋值
                visit(child);
            } else {
                // 递归访问其他子节点
                visit(child);
            }
        }
    }


    private void visitExpressionStmt(ASTNode node) {
        System.out.println("处理表达式语句，子节点数量: " + node.getChildren().size());

        // 打印所有子节点信息用于调试
        for (int i = 0; i < node.getChildren().size(); i++) {
            ASTNode child = node.getChildren().get(i);
            System.out.println("  子节点" + i + ": " + child.getType() +
                    (child.getValue() != null ? "(" + child.getValue() + ")" : ""));
        }

        // 访问表达式语句的子节点
        for (ASTNode child : node.getChildren()) {
            visit(child);
        }
    }

    private void visitAssignment(ASTNode node) {
        if (node.getChildren().size() < 2) {
            return;
        }

        ASTNode target = node.getChild(0);
        ASTNode value = node.getChild(1);

        // 检查赋值目标
        if (target.getType() == ASTNode.NodeType.IDENTIFIER) {
            String varName = target.getValue();
            Symbol symbol = symbolTable.lookupSymbol(varName);

            if (symbol == null) {
                addError(SemanticError.ErrorType.UNDECLARED_VARIABLE,
                        "未声明的变量: " + varName, node.getLineNumber());
            } else {
                // 标记变量已初始化
                symbol.setInitialized(true);

                // 检查类型匹配（简化版本）
                String valueType = inferExpressionType(value);
                if (valueType != null && !isTypeCompatible(symbol.getType(), valueType)) {
                    addError(SemanticError.ErrorType.TYPE_MISMATCH,
                            "类型不匹配: 不能将 " + valueType + " 赋值给 " + symbol.getType(),
                            node.getLineNumber());
                }

                System.out.println("赋值: " + varName + " = " + getExpressionString(value));
            }
        }

        // 访问右侧表达式（在检查完左侧后）
        visit(value);
    }

    private void visitIdentifier(ASTNode node) {
        String varName = node.getValue();
        System.out.println("访问标识符: " + varName);

        Symbol symbol = symbolTable.lookupSymbol(varName);

        if (symbol == null) {
            addError(SemanticError.ErrorType.UNDECLARED_VARIABLE,
                    "使用未声明的变量: " + varName, node.getLineNumber());
            System.out.println("错误: 未声明的变量 " + varName);
        } else {
            System.out.println("找到符号: " + varName + ", 类型: " + symbol.getType() +
                    ", 已初始化: " + symbol.isInitialized());
        }
    }

    private void visitBlockStmt(ASTNode node) {
        // 进入新作用域
        symbolTable.enterScope();

        // 访问块中的所有语句
        for (ASTNode child : node.getChildren()) {
            visit(child);
        }

        // 退出作用域
        symbolTable.exitScope();
    }

    private void visitIfStmt(ASTNode node) {
        // 访问条件表达式
        if (node.getChildren().size() > 0) {
            visit(node.getChild(0)); // 条件
        }

        // 访问then分支
        if (node.getChildren().size() > 1) {
            visit(node.getChild(1));
        }

        // 访问else分支（如果有）
        if (node.getChildren().size() > 2) {
            visit(node.getChild(2));
        }
    }

    private void visitWhileStmt(ASTNode node) {
        // 访问条件表达式
        if (node.getChildren().size() > 0) {
            visit(node.getChild(0));
        }

        // 访问循环体
        if (node.getChildren().size() > 1) {
            visit(node.getChild(1));
        }
    }

    private void visitForStmt(ASTNode node) {
        // 进入for循环作用域
        symbolTable.enterScope();

        // 访问初始化表达式
        if (node.getChildren().size() > 0) {
            visit(node.getChild(0));
        }

        // 访问条件表达式
        if (node.getChildren().size() > 1) {
            visit(node.getChild(1));
        }

        // 访问更新表达式
        if (node.getChildren().size() > 2) {
            visit(node.getChild(2));
        }

        // 访问循环体
        if (node.getChildren().size() > 3) {
            visit(node.getChild(3));
        }

        // 退出for循环作用域
        symbolTable.exitScope();
    }

    private void visitBinaryOp(ASTNode node) {
        // 访问左右操作数
        for (ASTNode child : node.getChildren()) {
            visit(child);
        }

        // 可以在这里添加运算符类型检查
        String operator = node.getValue();
        if (node.getChildren().size() == 2) {
            String leftType = inferExpressionType(node.getChild(0));
            String rightType = inferExpressionType(node.getChild(1));

            if (leftType != null && rightType != null) {
                if (!isOperationValid(operator, leftType, rightType)) {
                    addError(SemanticError.ErrorType.TYPE_MISMATCH,
                            "运算符 " + operator + " 不支持类型 " + leftType + " 和 " + rightType,
                            node.getLineNumber());
                }
            }
        }
    }

    // 辅助方法：推断表达式类型
    private String inferExpressionType(ASTNode node) {
        if (node == null) return null;

        switch (node.getType()) {
            case INT_LITERAL:
                return "int";
            case FLOAT_LITERAL:
                return "float";
            case STRING_LITERAL:
                return "string";
            case CHAR_LITERAL:
                return "char";
            case IDENTIFIER:
                Symbol symbol = symbolTable.lookupSymbol(node.getValue());
                return symbol != null ? symbol.getType() : null;
            case BINARY_OP:
                // 简化：假设算术运算结果类型与操作数相同
                return inferExpressionType(node.getChild(0));
            default:
                return null;
        }
    }

    // 辅助方法：检查类型兼容性
    private boolean isTypeCompatible(String targetType, String sourceType) {
        if (targetType.equals(sourceType)) {
            return true;
        }

        // 简单的类型转换规则
        if (targetType.equals("float") && sourceType.equals("int")) {
            return true;
        }

        return false;
    }

    // 辅助方法：检查运算是否有效
    private boolean isOperationValid(String operator, String leftType, String rightType) {
        // 算术运算符
        if (operator.equals("+") || operator.equals("-") || operator.equals("*") || operator.equals("/")) {
            return (leftType.equals("int") || leftType.equals("float")) &&
                    (rightType.equals("int") || rightType.equals("float"));
        }

        // 比较运算符
        if (operator.equals(">") || operator.equals("<") || operator.equals(">=") ||
                operator.equals("<=") || operator.equals("==") || operator.equals("!=")) {
            return isTypeCompatible(leftType, rightType) || isTypeCompatible(rightType, leftType);
        }

        return true; // 默认允许
    }

    // 辅助方法：获取表达式的字符串表示
    private String getExpressionString(ASTNode node) {
        if (node == null) return "";

        switch (node.getType()) {
            case INT_LITERAL:
            case FLOAT_LITERAL:
            case STRING_LITERAL:
            case CHAR_LITERAL:
            case IDENTIFIER:
                return node.getValue();
            case BINARY_OP:
                return getExpressionString(node.getChild(0)) + " " +
                        node.getValue() + " " +
                        getExpressionString(node.getChild(1));
            default:
                return node.getType().name();
        }
    }

    /**
     * 获取分析结果字符串（新增方法）
     */
    public String getAnalysisResult() {
        StringBuilder result = new StringBuilder();

        result.append("======= 语义分析结果 =======\n\n");

        if (hasErrors) {
            result.append("发现语义错误:\n");
            for (SemanticError error : errors) {
                result.append("  ").append(error.toString()).append("\n");
            }
        } else {
            result.append("语义分析通过，未发现错误。\n");
        }

        // 添加符号表信息
        result.append("\n").append(getSymbolTableString());

        result.append("\n语义分析完成。");

        return result.toString();
    }

    /**
     * 打印分析结果（保留原有方法，用于控制台输出）
     */
    public void printAnalysisResult() {
        System.out.println(getAnalysisResult());
    }

    /**
     * 获取符号表字符串（新增方法）
     */
    public String getSymbolTableString() {
        StringBuilder result = new StringBuilder();
        result.append("======= 符号表Symbol Table =======\n");

        // 收集所有符号
        List<Symbol> allSymbols = collectAllSymbols();
        if (allSymbols.isEmpty()) {
            result.append("符号表为空\n");
            return result.toString();
        }

        // 分类符号
        List<Symbol> variables = new ArrayList<>();
        List<Symbol> constants = new ArrayList<>();
        List<Symbol> functions = new ArrayList<>();

        for (Symbol symbol : allSymbols) {
            if (symbol.isFunction()) {
                functions.add(symbol);
            } else if (symbol.getType().startsWith("const") || symbol.getName().toUpperCase().equals(symbol.getName())) {
                // 改进常量判断：类型以const开头或变量名全大写
                constants.add(symbol);
            } else {
                variables.add(symbol);
            }
        }

        // 生成变量表
        if (!variables.isEmpty()) {
            result.append("\n变量表:\n");
            result.append("\t名字\t\t类型\t\t作用域\t\t值\t\t长度\t\t\n");
            for (Symbol symbol : variables) {
                result.append(String.format("\t%s\t\t%s\t\t/%d/%d\t\t%s\t\t%s\t\t\n",
                        truncate(symbol.getName(), 6),
                        truncate(symbol.getType(), 6),
                        0, // 作用域开始
                        symbol.getScope(), // 作用域结束
                        symbol.isInitialized() ? "已初始化" : "未初始化",
                        getTypeLength(symbol.getType())));
            }
            result.append("\n");
        }

        // 生成常量表
        if (!constants.isEmpty()) {
            result.append("\n常量表:\n");
            result.append("\t名字\t\t值\t\t类型\t\t\n");
            for (Symbol symbol : constants) {
                result.append(String.format("\t%s\t\t%s\t\t%s\t\t\n",
                        truncate(symbol.getName(), 6),
                        "***", // 常量值暂用***表示
                        truncate(symbol.getType(), 6)));
            }
            result.append("\n");
        }

        // 生成函数表
        if (!functions.isEmpty()) {
            result.append("\n函数表:\n");
            result.append("\t名字\t\t返回类型\t\t参数个数\t\t参数类型\t\t\n");
            for (Symbol symbol : functions) {
                String paramTypes = String.join(",", symbol.getParamTypes());
                if (paramTypes.length() > 12) {
                    paramTypes = paramTypes.substring(0, 9) + "...";
                }
                result.append(String.format("\t%s\t\t%s\t\t%d\t\t%s\t\t\n",
                        truncate(symbol.getName(), 6),
                        truncate(symbol.getType(), 6),
                        symbol.getParamTypes().size(),
                        paramTypes.isEmpty() ? "无" : paramTypes));
            }
            result.append("\n");
        }

        // 添加符号统计信息
        result.append("\n符号统计:\n");
        result.append("变量数量: ").append(variables.size()).append("\n");
        result.append("常量数量: ").append(constants.size()).append("\n");
        result.append("函数数量: ").append(functions.size()).append("\n");
        result.append("总符号数: ").append(allSymbols.size()).append("\n");

        return result.toString();
    }

    /**
     * 打印符号表（保留原有方法，用于控制台输出）
     */
    public void printSymbolTable() {
        System.out.println(getSymbolTableString());
    }

    /**
     * 收集所有作用域中的符号（修改后的版本）
     */
    private List<Symbol> collectAllSymbols() {
        return symbolTable.getAllSymbols();
    }

    /**
     * 截断字符串到指定长度
     */
    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }

    /**
     * 获取类型对应的长度
     */
    private String getTypeLength(String type) {
        switch (type.toLowerCase()) {
            case "int":
                return "4";
            case "float":
                return "4";
            case "double":
                return "8";
            case "char":
                return "1";
            case "boolean":
                return "1";
            default:
                return "***";
        }
    }
}