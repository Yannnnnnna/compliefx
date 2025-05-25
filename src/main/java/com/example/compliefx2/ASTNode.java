package com.example.compliefx2;
import java.util.ArrayList;
import java.util.List;

/**
 * 通用AST节点类 - 简化设计，减少类的数量
 */
public class ASTNode {
    // 节点类型枚举
    public enum NodeType {
        // 表达式类型
        BINARY_OP,      // 二元运算
        UNARY_OP,       // 一元运算
        ASSIGNMENT,     // 赋值
        IDENTIFIER,     // 标识符
        INT_LITERAL,    // 整数字面量
        FLOAT_LITERAL,  // 浮点数字面量
        STRING_LITERAL, // 字符串字面量
        CHAR_LITERAL,   // 字符字面量
        BOOL_LITERAL,   // 布尔字面量

        // 语句类型
        EXPRESSION_STMT,    // 表达式语句
        IF_STMT,           // if语句
        WHILE_STMT,        // while语句
        FOR_STMT,          // for语句
        BLOCK_STMT,        // 复合语句
        DECLARATION_STMT,  // 声明语句
        RETURN_STMT,       // return语句
        BREAK_STMT,        // break语句
        CONTINUE_STMT,     // continue语句

        // 程序结构
        PROGRAM,           // 程序根节点
        FUNCTION_DEF,      // 函数定义
        PARAMETER_LIST,    // 参数列表
        ARGUMENT_LIST      // 参数列表
    }

    private NodeType type;           // 节点类型
    private String value;            // 节点值（如运算符、标识符名称、字面量值）
    private List<ASTNode> children;  // 子节点列表
    private int lineNumber;          // 行号
    private int columnNumber;        // 列号

    // 构造函数
    public ASTNode(NodeType type, String value, int lineNumber, int columnNumber) {
        this.type = type;
        this.value = value;
        this.children = new ArrayList<>();
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    public ASTNode(NodeType type, String value) {
        this(type, value, 0, 0);
    }

    public ASTNode(NodeType type) {
        this(type, null, 0, 0);
    }

    // Getter和Setter方法
    public NodeType getType() { return type; }
    public String getValue() { return value; }
    public List<ASTNode> getChildren() { return children; }
    public int getLineNumber() { return lineNumber; }
    public int getColumnNumber() { return columnNumber; }

    public void setValue(String value) { this.value = value; }

    // 添加子节点
    public void addChild(ASTNode child) {
        if (child != null) {
            children.add(child);
        }
    }

    // 获取指定位置的子节点
    public ASTNode getChild(int index) {
        if (index >= 0 && index < children.size()) {
            return children.get(index);
        }
        return null;
    }

    // 获取子节点数量
    public int getChildCount() {
        return children.size();
    }

    // 判断是否为叶子节点
    public boolean isLeaf() {
        return children.isEmpty();
    }

    // 便捷方法：创建二元运算节点
    public static ASTNode createBinaryOp(String operator, ASTNode left, ASTNode right,
                                         int lineNumber, int columnNumber) {
        ASTNode node = new ASTNode(NodeType.BINARY_OP, operator, lineNumber, columnNumber);
        node.addChild(left);
        node.addChild(right);
        return node;
    }

    // 便捷方法：创建一元运算节点
    public static ASTNode createUnaryOp(String operator, ASTNode operand,
                                        int lineNumber, int columnNumber) {
        ASTNode node = new ASTNode(NodeType.UNARY_OP, operator, lineNumber, columnNumber);
        node.addChild(operand);
        return node;
    }

    // 便捷方法：创建赋值节点
    public static ASTNode createAssignment(ASTNode target, ASTNode value,
                                           int lineNumber, int columnNumber) {
        ASTNode node = new ASTNode(NodeType.ASSIGNMENT, "=", lineNumber, columnNumber);
        node.addChild(target);
        node.addChild(value);
        return node;
    }

    // 便捷方法：创建标识符节点
    public static ASTNode createIdentifier(String name, int lineNumber, int columnNumber) {
        return new ASTNode(NodeType.IDENTIFIER, name, lineNumber, columnNumber);
    }

    // 便捷方法：创建字面量节点
    public static ASTNode createIntLiteral(String value, int lineNumber, int columnNumber) {
        return new ASTNode(NodeType.INT_LITERAL, value, lineNumber, columnNumber);
    }

    public static ASTNode createFloatLiteral(String value, int lineNumber, int columnNumber) {
        return new ASTNode(NodeType.FLOAT_LITERAL, value, lineNumber, columnNumber);
    }

    public static ASTNode createStringLiteral(String value, int lineNumber, int columnNumber) {
        return new ASTNode(NodeType.STRING_LITERAL, value, lineNumber, columnNumber);
    }

    public static ASTNode createCharLiteral(String value, int lineNumber, int columnNumber) {
        return new ASTNode(NodeType.CHAR_LITERAL, value, lineNumber, columnNumber);
    }

    public static ASTNode createBoolLiteral(String value, int lineNumber, int columnNumber) {
        return new ASTNode(NodeType.BOOL_LITERAL, value, lineNumber, columnNumber);
    }

    // 便捷方法：创建语句节点
    public static ASTNode createExpressionStmt(ASTNode expression) {
        ASTNode node = new ASTNode(NodeType.EXPRESSION_STMT);
        node.addChild(expression);
        return node;
    }

    public static ASTNode createIfStmt(ASTNode condition, ASTNode thenStmt, ASTNode elseStmt) {
        ASTNode node = new ASTNode(NodeType.IF_STMT);
        node.addChild(condition);
        node.addChild(thenStmt);
        if (elseStmt != null) {
            node.addChild(elseStmt);
        }
        return node;
    }

    public static ASTNode createWhileStmt(ASTNode condition, ASTNode body) {
        ASTNode node = new ASTNode(NodeType.WHILE_STMT);
        node.addChild(condition);
        node.addChild(body);
        return node;
    }

    public static ASTNode createBlockStmt() {
        return new ASTNode(NodeType.BLOCK_STMT);
    }

    // 访问者模式支持
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }

    // toString方法，用于调试
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb, 0);
        return sb.toString();
    }

    private void toString(StringBuilder sb, int depth) {
        // 添加缩进
        for (int i = 0; i < depth; i++) {
            sb.append("  ");
        }

        // 添加节点信息
        sb.append(type.name());
        if (value != null && !value.isEmpty()) {
            sb.append("(").append(value).append(")");
        }
        sb.append("\n");

        // 递归处理子节点
        for (ASTNode child : children) {
            child.toString(sb, depth + 1);
        }
    }

    // 简化的访问者接口
    public interface ASTVisitor<T> {
        T visit(ASTNode node);
    }
}
