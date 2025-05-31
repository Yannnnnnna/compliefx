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
            case PROGRAM: //  程序
                return generateProgramCode(node);
            case FUNCTION_DEF: // 函数定义
                return generateFunctionCode(node);
            case BLOCK_STMT: // 块语句
                return generateBlockCode(node);
            case IF_STMT: // if语句
                return generateIfCode(node);
            case WHILE_STMT: // while语句
                return generateWhileCode(node);
            case FOR_STMT: // for语句
                return generateForCode(node);
            case DECLARATION_STMT: // 声明语句
                return generateDeclarationCode(node);
            case EXPRESSION_STMT: // 表达式语句
                return generateExpressionCode(node);
            case ASSIGNMENT: // 赋值语句
                return generateAssignmentCode(node);
            case BINARY_OP: // 二元运算符
                return generateBinaryOpCode(node);
            case UNARY_OP: // 一元运算符
                return generateUnaryOpCode(node);
            case IDENTIFIER: // 标识符
                return node.getValue();
            case INT_LITERAL:   //  整数字面量
            case FLOAT_LITERAL: //  浮点数字面量
            case STRING_LITERAL: // 字符串字面量
            case CHAR_LITERAL: //  字符字面量
            case BOOL_LITERAL: //  布尔字面量
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
        emit("FUNC", functionName, "", ""); // 生成函数声明

        // 处理函数体
        for (int i = 2; i < node.getChildren().size(); i++) {
            generateCode(node.getChildren().get(i)); // 处理函数体
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
// ========== 代码优化相关方法 ==========

    /**
     * 基础优化主方法
     */
    public void optimize() {
        System.out.println("开始代码优化...");
        int rounds = 0;
        boolean changed = true;

        while (changed && rounds < 5) {
            changed = false;
            rounds++;
            System.out.println("第 " + rounds + " 轮优化:");

            // 基础优化
            changed |= constantFolding();           // 常量折叠
            changed |= constantPropagation();       // 常量传播
            changed |= algebraicSimplification();   // 代数化简

            if (changed) {
                System.out.println("  - 本轮有优化");
            } else {
                System.out.println("  - 本轮无优化，结束");
            }
        }
        System.out.println("优化完成，共进行了 " + rounds + " 轮优化");
    }

    /**
     * 常量折叠优化
     */
    private boolean constantFolding() {
        boolean changed = false;

        for (int i = 0; i < quadruples.size(); i++) {
            Quadruple quad = quadruples.get(i);
            String op = quad.getOp();

            // 跳过非运算指令
            if (!isArithmeticOp(op) && !isComparisonOp(op)) {
                continue;
            }

            String arg1 = quad.getArg1();
            String arg2 = quad.getArg2();

            // 检查是否都是常量
            if (isConstant(arg1) && (arg2.isEmpty() || isConstant(arg2))) {
                String result = evaluateConstantExpression(op, arg1, arg2);
                if (result != null) {
                    // 替换为赋值语句
                    quad.setOp("=");
                    quad.setArg1(result);
                    quad.setArg2("");
                    changed = true;
                }
            }
        }

        return changed;
    }

    /**
     * 常量传播优化
     */
    private boolean constantPropagation() {
        boolean changed = false;
        java.util.Map<String, String> constants = new java.util.HashMap<>();

        for (int i = 0; i < quadruples.size(); i++) {
            Quadruple quad = quadruples.get(i);
            String op = quad.getOp();

            // 记录常量赋值
            if ("=".equals(op) && isConstant(quad.getArg1())) {
                constants.put(quad.getResult(), quad.getArg1());
            }
            // 传播已知常量
            else {
                // 替换操作数
                if (constants.containsKey(quad.getArg1())) {
                    quad.setArg1(constants.get(quad.getArg1()));
                    changed = true;
                }
                if (!quad.getArg2().isEmpty() && constants.containsKey(quad.getArg2())) {
                    quad.setArg2(constants.get(quad.getArg2()));
                    changed = true;
                }

                // 清除被重定义的变量
                if (!quad.getResult().isEmpty() &&
                        !"LABEL".equals(op) && !"DECLARE".equals(op) &&
                        !"JMP".equals(op) && !"JZ".equals(op) && !"JNZ".equals(op)) {
                    constants.remove(quad.getResult());
                }
            }
        }

        return changed;
    }

    /**
     * 代数化简优化
     */
    private boolean algebraicSimplification() {
        boolean changed = false;

        for (int i = 0; i < quadruples.size(); i++) {
            Quadruple quad = quadruples.get(i);
            String op = quad.getOp();
            String arg1 = quad.getArg1();
            String arg2 = quad.getArg2();

            String simplified = null;

            switch (op) {
                case "+":
                    if ("0".equals(arg1)) {
                        simplified = arg2; // 0 + x = x
                    } else if ("0".equals(arg2)) {
                        simplified = arg1; // x + 0 = x
                    }
                    break;

                case "-":
                    if ("0".equals(arg2)) {
                        simplified = arg1; // x - 0 = x
                    } else if (arg1.equals(arg2)) {
                        simplified = "0"; // x - x = 0
                    }
                    break;

                case "*":
                    if ("0".equals(arg1) || "0".equals(arg2)) {
                        simplified = "0"; // x * 0 = 0
                    } else if ("1".equals(arg1)) {
                        simplified = arg2; // 1 * x = x
                    } else if ("1".equals(arg2)) {
                        simplified = arg1; // x * 1 = x
                    }
                    break;

                case "/":
                    if ("1".equals(arg2)) {
                        simplified = arg1; // x / 1 = x
                    } else if ("0".equals(arg1)) {
                        simplified = "0"; // 0 / x = 0
                    }
                    break;
            }

            if (simplified != null) {
                quad.setOp("=");
                quad.setArg1(simplified);
                quad.setArg2("");
                changed = true;
            }
        }

        return changed;
    }

// ========== 辅助方法 ==========

    /**
     * 判断是否为常量
     */
    private boolean isConstant(String value) {
        if (value == null || value.isEmpty()) return false;

        // 数字常量
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e1) {
            try {
                Double.parseDouble(value);
                return true;
            } catch (NumberFormatException e2) {
                // 不是数字
            }
        }

        // 布尔常量
        return "true".equals(value) || "false".equals(value);
    }

    /**
     * 判断是否为算术运算符
     */
    private boolean isArithmeticOp(String op) {
        return "+".equals(op) || "-".equals(op) || "*".equals(op) || "/".equals(op) || "%".equals(op);
    }

    /**
     * 计算常量表达式
     */
    private String evaluateConstantExpression(String op, String arg1, String arg2) {
        try {
            if (isArithmeticOp(op)) {
                double val1 = Double.parseDouble(arg1);
                double val2 = Double.parseDouble(arg2);
                double result = 0;

                switch (op) {
                    case "+": result = val1 + val2; break;
                    case "-": result = val1 - val2; break;
                    case "*": result = val1 * val2; break;
                    case "/":
                        if (val2 == 0) return null; // 避免除零
                        result = val1 / val2;
                        break;
                    case "%":
                        if (val2 == 0) return null;
                        result = val1 % val2;
                        break;
                }

                // 如果结果是整数，返回整数格式
                if (result == (int)result) {
                    return String.valueOf((int)result);
                } else {
                    return String.valueOf(result);
                }
            }

            if (isComparisonOp(op)) {
                double val1 = Double.parseDouble(arg1);
                double val2 = Double.parseDouble(arg2);
                boolean result = false;

                switch (op) {
                    case "==": result = val1 == val2; break;
                    case "!=": result = val1 != val2; break;
                    case "<": result = val1 < val2; break;
                    case "<=": result = val1 <= val2; break;
                    case ">": result = val1 > val2; break;
                    case ">=": result = val1 >= val2; break;
                }

                return String.valueOf(result);
            }

        } catch (NumberFormatException e) {
            // 解析失败，返回null
        }

        return null;
    }
}
