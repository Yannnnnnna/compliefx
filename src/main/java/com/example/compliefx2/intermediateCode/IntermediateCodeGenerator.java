package com.example.compliefx2.intermediateCode;

/**
 * @author wyr on 2025/5/25
 */

import com.example.compliefx2.ASTNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * 中间代码生成器
 */
public class IntermediateCodeGenerator {
    private List<Quadruple> quadruples;     // 四元式列表
    private int tempVarCounter;             // 临时变量计数器
    private int labelCounter;               // 标签计数器
    private Stack<String> trueList;         // 真出口链表栈
    private Stack<String> falseList;        // 假出口链表栈

    public IntermediateCodeGenerator() {
        this.quadruples = new ArrayList<>();
        this.tempVarCounter = 0;
        this.labelCounter = 0;
        this.trueList = new Stack<>();
        this.falseList = new Stack<>();
    }

    /**
     * 生成新的临时变量名
     */
    public String newTemp() {
        return "t" + (tempVarCounter++);
    }

    /**
     * 生成新的标签名
     */
    public String newLabel() {
        return "L" + (labelCounter++);
    }

    /**
     * 添加四元式
     */
    public int emit(String op, String arg1, String arg2, String result) {
        Quadruple quad = new Quadruple(op, arg1, arg2, result);
        quadruples.add(quad);
        return quadruples.size() - 1; // 返回四元式的索引
    }

    /**
     * 回填四元式
     */
    public void backpatch(int index, String value) {
        if (index >= 0 && index < quadruples.size()) {
            Quadruple quad = quadruples.get(index);
            if (quad.getResult().equals("_")) {
                quad.setResult(value);
            }
        }
    }

    /**
     * 回填跳转地址列表
     */
    public void backpatchList(List<Integer> list, String label) {
        for (int index : list) {
            backpatch(index, label);
        }
    }

    /**
     * 合并两个跳转地址列表
     */
    public List<Integer> merge(List<Integer> list1, List<Integer> list2) {
        List<Integer> result = new ArrayList<>(list1);
        result.addAll(list2);
        return result;
    }

    /**
     * 为AST节点生成中间代码
     */
    public String generateCode(ASTNode node) {
        if (node == null) return null;

        switch (node.getType()) {
            case PROGRAM:
                return generateProgramCode(node);
            case FUNCTION_DEF:
                return generateFunctionCode(node);
            case BLOCK_STMT:
                return generateBlockCode(node);
            case IF_STMT:
                return generateIfCode(node);
            case WHILE_STMT:
                return generateWhileCode(node);
            case FOR_STMT:
                return generateForCode(node);
            case DECLARATION_STMT:
                return generateDeclarationCode(node);
            case EXPRESSION_STMT:
                return generateExpressionCode(node);
            case ASSIGNMENT:
                return generateAssignmentCode(node);
            case BINARY_OP:
                return generateBinaryOpCode(node);
            case UNARY_OP:
                return generateUnaryOpCode(node);
            case IDENTIFIER:
                return node.getValue();
            case INT_LITERAL:
            case FLOAT_LITERAL:
            case STRING_LITERAL:
            case CHAR_LITERAL:
            case BOOL_LITERAL:
                return node.getValue();
            default:
                return null;
        }
    }

    /**
     * 生成程序代码
     */
    private String generateProgramCode(ASTNode node) {
        for (ASTNode child : node.getChildren()) {
            generateCode(child);
        }
        return null;
    }

    /**
     * 生成函数代码
     */
    private String generateFunctionCode(ASTNode node) {
        String functionName = node.getChildren().get(0).getValue(); // 函数名
        emit("FUNC", functionName, "", "");

        // 处理函数体
        for (int i = 2; i < node.getChildren().size(); i++) {
            generateCode(node.getChildren().get(i));
        }

        emit("ENDFUNC", functionName, "", "");
        return null;
    }

    /**
     * 生成块语句代码
     */
    private String generateBlockCode(ASTNode node) {
        for (ASTNode child : node.getChildren()) {
            generateCode(child);
        }
        return null;
    }

    /**
     * 生成if语句代码
     */
    private String generateIfCode(ASTNode node) {
        List<ASTNode> children = node.getChildren();
        ASTNode condition = children.get(0);    // 条件表达式
        ASTNode thenStmt = children.get(1);     // then语句
        ASTNode elseStmt = children.size() > 2 ? children.get(2) : null; // else语句

        // 生成条件表达式代码
        String condResult = generateBooleanExpression(condition);

        String elseLabel = newLabel();
        String endLabel = newLabel();

        // 条件为假时跳转到else标签
        emit("JZ", condResult, "", elseLabel);

        // 生成then语句代码
        generateCode(thenStmt);

        if (elseStmt != null) {
            // 跳过else部分
            emit("JMP", "", "", endLabel);
        }

        // else标签
        emit("LABEL", elseLabel, "", "");

        // 生成else语句代码
        if (elseStmt != null) {
            generateCode(elseStmt);
            emit("LABEL", endLabel, "", "");
        }

        return null;
    }

    /**
     * 生成while循环代码
     */
    private String generateWhileCode(ASTNode node) {
        List<ASTNode> children = node.getChildren();
        ASTNode condition = children.get(0);    // 条件表达式
        ASTNode body = children.get(1);         // 循环体

        String beginLabel = newLabel();
        String endLabel = newLabel();

        // 循环开始标签
        emit("LABEL", beginLabel, "", "");

        // 生成条件表达式代码
        String condResult = generateBooleanExpression(condition);

        // 条件为假时跳出循环
        emit("JZ", condResult, "", endLabel);

        // 生成循环体代码
        generateCode(body);

        // 跳回循环开始
        emit("JMP", "", "", beginLabel);

        // 循环结束标签
        emit("LABEL", endLabel, "", "");

        return null;
    }

    /**
     * 生成for循环代码
     */
    private String generateForCode(ASTNode node) {
        List<ASTNode> children = node.getChildren();
        ASTNode init = children.get(0);         // 初始化
        ASTNode condition = children.get(1);    // 条件表达式
        ASTNode update = children.get(2);       // 更新表达式
        ASTNode body = children.get(3);         // 循环体

        // 生成初始化代码
        generateCode(init);

        String beginLabel = newLabel();
        String endLabel = newLabel();

        // 循环开始标签
        emit("LABEL", beginLabel, "", "");

        // 生成条件表达式代码
        if (condition != null && condition.getChildren() != null && !condition.getChildren().isEmpty()) {
            String condResult = generateBooleanExpression(condition.getChildren().get(0));
            // 条件为假时跳出循环
            emit("JZ", condResult, "", endLabel);
        }

        // 生成循环体代码
        generateCode(body);

        // 生成更新表达式代码
        generateCode(update);

        // 跳回循环开始
        emit("JMP", "", "", beginLabel);

        // 循环结束标签
        emit("LABEL", endLabel, "", "");

        return null;
    }

    /**
     * 生成声明语句代码
     */
    private String generateDeclarationCode(ASTNode node) {
        String type = node.getValue();
        String varName = node.getChildren().get(0).getValue();

        // 声明变量
        emit("DECLARE", type, "", varName);

        // 如果有初始化表达式
        if (node.getChildren().size() > 1) {
            String initValue = generateCode(node.getChildren().get(1));
            emit("=", initValue, "", varName);
        }

        return varName;
    }

    /**
     * 生成表达式语句代码
     */
    private String generateExpressionCode(ASTNode node) {
        if (node.getChildren().isEmpty()) {
            return null; // 空语句
        }
        return generateCode(node.getChildren().get(0));
    }

    /**
     * 生成赋值表达式代码
     */
    private String generateAssignmentCode(ASTNode node) {
        List<ASTNode> children = node.getChildren();
        String lhs = children.get(0).getValue(); // 左值（标识符）
        String rhs = generateCode(children.get(1)); // 右值表达式

        emit("=", rhs, "", lhs);
        return lhs;
    }

    /**
     * 生成二元运算表达式代码
     */
    private String generateBinaryOpCode(ASTNode node) {
        String op = node.getValue();
        List<ASTNode> children = node.getChildren();
        String left = generateCode(children.get(0));
        String right = generateCode(children.get(1));

        // 对于逻辑运算符，需要特殊处理
        if ("&&".equals(op) || "||".equals(op)) {
            return generateLogicalOpCode(op, left, right);
        }

        String temp = newTemp();
        emit(op, left, right, temp);
        return temp;
    }

    /**
     * 生成逻辑运算代码
     */
    private String generateLogicalOpCode(String op, String left, String right) {
        String temp = newTemp();
        String label1 = newLabel();
        String label2 = newLabel();
        String endLabel = newLabel();

        if ("&&".equals(op)) {
            // 短路与运算
            emit("JZ", left, "", label1);      // 如果left为假，跳转
            emit("JZ", right, "", label1);     // 如果right为假，跳转
            emit("=", "true", "", temp);       // 都为真
            emit("JMP", "", "", endLabel);
            emit("LABEL", label1, "", "");
            emit("=", "false", "", temp);      // 至少一个为假
            emit("LABEL", endLabel, "", "");
        } else { // ||
            // 短路或运算
            emit("JNZ", left, "", label1);     // 如果left为真，跳转
            emit("JNZ", right, "", label1);    // 如果right为真，跳转
            emit("=", "false", "", temp);      // 都为假
            emit("JMP", "", "", endLabel);
            emit("LABEL", label1, "", "");
            emit("=", "true", "", temp);       // 至少一个为真
            emit("LABEL", endLabel, "", "");
        }

        return temp;
    }

    /**
     * 生成一元运算表达式代码
     */
    private String generateUnaryOpCode(ASTNode node) {
        String op = node.getValue();
        String operand = generateCode(node.getChildren().get(0));
        String temp = newTemp();

        if ("!".equals(op)) {
            // 逻辑非运算
            String label1 = newLabel();
            String endLabel = newLabel();
            emit("JZ", operand, "", label1);   // 如果operand为假，跳转
            emit("=", "false", "", temp);      // operand为真，结果为假
            emit("JMP", "", "", endLabel);
            emit("LABEL", label1, "", "");
            emit("=", "true", "", temp);       // operand为假，结果为真
            emit("LABEL", endLabel, "", "");
        } else {
            emit(op, operand, "", temp);
        }

        return temp;
    }

    /**
     * 生成布尔表达式代码（用于条件判断）
     */
    private String generateBooleanExpression(ASTNode node) {
        String result = generateCode(node);

        // 如果结果不是布尔类型的临时变量，需要转换
        if (node.getType() == ASTNode.NodeType.BINARY_OP) {
            String op = node.getValue();
            if (isComparisonOp(op)) {
                return result; // 比较运算的结果已经是布尔值
            }
        }

        // 对于其他类型，生成比较代码
        String temp = newTemp();
        emit("!=", result, "0", temp);
        return temp;
    }

    /**
     * 判断是否为比较运算符
     */
    private boolean isComparisonOp(String op) {
        return "==".equals(op) || "!=".equals(op) || "<".equals(op) ||
                "<=".equals(op) || ">".equals(op) || ">=".equals(op);
    }

    /**
     * 获取生成的四元式列表
     */
    public List<Quadruple> getQuadruples() {
        return quadruples;
    }

    /**
     * 打印四元式列表
     */
    public void printQuadruples() {
        System.out.println("=== 中间代码 (四元式) ===");
        for (int i = 0; i < quadruples.size(); i++) {
            System.out.printf("%3d: %s\n", i, quadruples.get(i));
        }
    }

    /**
     * 获取四元式的字符串表示
     */
    public String getQuadruplesAsString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 中间代码 (四元式) ===\n");
        for (int i = 0; i < quadruples.size(); i++) {
            sb.append(String.format("%3d: %s\n", i, quadruples.get(i)));
        }
        return sb.toString();
    }
}
