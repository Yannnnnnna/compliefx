package com.example.compliefx2;

import com.example.compliefx2.intermediateCode.IntermediateCodeGenerator;
import com.example.compliefx2.intermediateCode.Quadruple;
import com.example.compliefx2.syntaxAnalyzer.StatementParser;

import java.io.IOException;
import java.util.*;

/**
 * 目标代码生成器 - 将四元式转换为8086汇编代码
 * 重写版本，修复了原有问题并实现基本的算术运算和跳转指令
 */
public class CodeGenerator {
    private List<Quadruple> quadruples;                      // 四元式列表
    private final Map<String, String> symbolTable;          // 变量名 -> 类型映射
    private final Set<String> tempVars;                      // 临时变量集合
    private final List<String> dataSection;                 // 数据段内容
    private final List<String> codeSection;                 // 代码段内容
    private int assemblyLabelCounter;                        // 汇编标签计数器

    public CodeGenerator() {
        this.quadruples = new ArrayList<>();
        this.symbolTable = new HashMap<>();
        this.tempVars = new HashSet<>();
        this.dataSection = new ArrayList<>();
        this.codeSection = new ArrayList<>();
        this.assemblyLabelCounter = 0;
    }

    /**
     * 设置四元式列表并分析符号
     */
    public void setQuadruples(List<Quadruple> quads) {
        this.quadruples = new ArrayList<>(quads);
        analyzeSymbols();
    }

    /**
     * 分析符号表，识别变量和临时变量
     */
    private void analyzeSymbols() {
        symbolTable.clear();
        tempVars.clear();

        for (Quadruple q : quadruples) {
            // 处理变量声明
            if ("DECLARE".equals(q.getOp())) {
                symbolTable.put(q.getResult(), q.getArg1());
            }

            // 收集所有出现的标识符
            collectIdentifier(q.getArg1());
            collectIdentifier(q.getArg2());
            collectIdentifier(q.getResult());
        }
    }

    /**
     * 收集标识符，区分变量和临时变量
     */
    private void collectIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty() || isImmediate(identifier)) {
            return;
        }

        if (identifier.startsWith("t") && identifier.substring(1).matches("\\d+")) {
            // 临时变量（格式：t0, t1, t2, ...）
            tempVars.add(identifier);
        } else if (identifier.matches("[a-zA-Z_][a-zA-Z0-9_]*") && !symbolTable.containsKey(identifier)) {
            // 普通变量，如果未在声明中出现，默认为int类型
            if (!isReservedWord(identifier)) {
                symbolTable.put(identifier, "int");
            }
        }
    }

    /**
     * 判断是否为立即数
     */
    private boolean isImmediate(String operand) {
        if (operand == null) return false;
        return operand.matches("-?\\d+") || operand.equals("true") || operand.equals("false");
    }

    /**
     * 判断是否为保留字或标签
     */
    private boolean isReservedWord(String word) {
        return word.startsWith("L") || word.equals("main") || word.equals("true") || word.equals("false");
    }

    /**
     * 生成完整的汇编代码
     */
    public String generateAssembly() {
        generateDataSection();
        generateCodeSection();
        return buildFinalAssembly();
    }

    /**
     * 生成数据段
     */
    private void generateDataSection() {
        dataSection.clear();
        dataSection.add(".MODEL SMALL");
        dataSection.add(".DATA");

        // 声明用户变量
        for (Map.Entry<String, String> entry : symbolTable.entrySet()) {
            String varName = entry.getKey();
            String varType = entry.getValue();
            if ("int".equals(varType)) {
                dataSection.add("    " + varName + " DW 0    ; " + varType + " variable");
            } else {
                dataSection.add("    " + varName + " DW 0    ; " + varType + " variable");
            }
        }

        // 声明临时变量
        for (String tempVar : tempVars) {
            dataSection.add("    " + tempVar + " DW 0    ; temporary variable");
        }

        dataSection.add(".STACK 100h");
    }

    /**
     * 生成代码段
     */
    private void generateCodeSection() {
        codeSection.clear();
        codeSection.add(".CODE");
        codeSection.add("START:");
        codeSection.add("    MOV AX, @DATA");
        codeSection.add("    MOV DS, AX");
        codeSection.add("");

        // 处理每个四元式
        for (Quadruple q : quadruples) {
            generateInstructionForQuadruple(q);
        }

        // 程序结束
        codeSection.add("");
        codeSection.add("    MOV AH, 4Ch    ; DOS程序结束");
        codeSection.add("    INT 21h");
        codeSection.add("END START");
    }

    /**
     * 为单个四元式生成汇编指令
     */
    private void generateInstructionForQuadruple(Quadruple q) {
        String op = q.getOp();
        String arg1 = q.getArg1();
        String arg2 = q.getArg2();
        String result = q.getResult();

        switch (op) {
            case "FUNC":
                handleFunction(arg1);
                break;
            case "ENDFUNC":
                handleEndFunction(arg1);
                break;
            case "DECLARE":
                // 变量声明已在数据段处理
                break;
            case "=":
                handleAssignment(arg1, result);
                break;
            case "+":
                handleArithmetic("+", arg1, arg2, result);
                break;
            case "-":
                handleArithmetic("-", arg1, arg2, result);
                break;
            case "*":
                handleArithmetic("*", arg1, arg2, result);
                break;
            case "/":
                handleArithmetic("/", arg1, arg2, result);
                break;
            case "JMP":
                handleJump(result);
                break;
            case "JZ":
                handleConditionalJump("JZ", arg1, result);
                break;
            case "JNZ":
                handleConditionalJump("JNZ", arg1, result);
                break;
            case "LABEL":
                handleLabel(arg1);
                break;
            case "<":
            case ">":
            case "<=":
            case ">=":
            case "==":
            case "!=":
                handleComparison(op, arg1, arg2, result);
                break;
            default:
                codeSection.add("    ; 未实现的操作: " + op);
                break;
        }
    }

    /**
     * 处理函数定义
     */
    private void handleFunction(String funcName) {
        if ("main".equals(funcName)) {
            codeSection.add("MAIN_PROC:");
        } else {
            codeSection.add(funcName + "_PROC:");
        }
    }

    /**
     * 处理函数结束
     */
    private void handleEndFunction(String funcName) {
        // 函数结束通常不需要特殊处理，因为我们在程序末尾统一处理结束
    }

    /**
     * 处理赋值操作
     */
    private void handleAssignment(String source, String destination) {
        codeSection.add("    ; " + destination + " = " + source);

        if (isImmediate(source)) {
            // 立即数赋值
            String value = convertImmediate(source);
            codeSection.add("    MOV AX, " + value);
        } else {
            // 变量赋值
            codeSection.add("    MOV AX, " + source);
        }
        codeSection.add("    MOV " + destination + ", AX");
        codeSection.add("");
    }

    /**
     * 处理算术运算
     */
    private void handleArithmetic(String operation, String operand1, String operand2, String result) {
        codeSection.add("    ; " + result + " = " + operand1 + " " + operation + " " + operand2);

        // 加载第一个操作数到AX
        if (isImmediate(operand1)) {
            codeSection.add("    MOV AX, " + convertImmediate(operand1));
        } else {
            codeSection.add("    MOV AX, " + operand1);
        }

        // 执行运算
        switch (operation) {
            case "+":
                if (isImmediate(operand2)) {
                    codeSection.add("    ADD AX, " + convertImmediate(operand2));
                } else {
                    codeSection.add("    ADD AX, " + operand2);
                }
                break;
            case "-":
                if (isImmediate(operand2)) {
                    codeSection.add("    SUB AX, " + convertImmediate(operand2));
                } else {
                    codeSection.add("    SUB AX, " + operand2);
                }
                break;
            case "*":
                if (isImmediate(operand2)) {
                    codeSection.add("    MOV BX, " + convertImmediate(operand2));
                } else {
                    codeSection.add("    MOV BX, " + operand2);
                }
                codeSection.add("    MUL BX");
                break;
            case "/":
                if (isImmediate(operand2)) {
                    codeSection.add("    MOV BX, " + convertImmediate(operand2));
                } else {
                    codeSection.add("    MOV BX, " + operand2);
                }
                codeSection.add("    XOR DX, DX    ; 清除高位");
                codeSection.add("    DIV BX");
                break;
        }

        // 存储结果
        codeSection.add("    MOV " + result + ", AX");
        codeSection.add("");
    }

    /**
     * 处理比较运算
     */
    private void handleComparison(String operation, String operand1, String operand2, String result) {
        codeSection.add("    ; " + result + " = " + operand1 + " " + operation + " " + operand2);

        String trueLabel = "TRUE_" + (assemblyLabelCounter++);
        String endLabel = "END_" + (assemblyLabelCounter++);

        // 加载操作数并比较
        if (isImmediate(operand1)) {
            codeSection.add("    MOV AX, " + convertImmediate(operand1));
        } else {
            codeSection.add("    MOV AX, " + operand1);
        }

        if (isImmediate(operand2)) {
            codeSection.add("    CMP AX, " + convertImmediate(operand2));
        } else {
            codeSection.add("    CMP AX, " + operand2);
        }

        // 根据比较结果跳转
        String jumpInstruction = getJumpInstruction(operation);
        codeSection.add("    " + jumpInstruction + " " + trueLabel);

        // 条件为假
        codeSection.add("    MOV " + result + ", 0");
        codeSection.add("    JMP " + endLabel);

        // 条件为真
        codeSection.add(trueLabel + ":");
        codeSection.add("    MOV " + result + ", 1");

        codeSection.add(endLabel + ":");
        codeSection.add("");
    }

    /**
     * 处理无条件跳转
     */
    private void handleJump(String label) {
        codeSection.add("    JMP " + label);
    }

    /**
     * 处理条件跳转
     */
    private void handleConditionalJump(String jumpType, String condition, String label) {
        codeSection.add("    ; 条件跳转: " + jumpType + " " + condition + " -> " + label);

        // 检查条件值
        if (isImmediate(condition)) {
            codeSection.add("    MOV AX, " + convertImmediate(condition));
            codeSection.add("    CMP AX, 0");
        } else {
            codeSection.add("    CMP " + condition + ", 0");
        }

        // 根据跳转类型生成指令
        if ("JZ".equals(jumpType)) {
            codeSection.add("    JE " + label + "    ; 为0时跳转");
        } else if ("JNZ".equals(jumpType)) {
            codeSection.add("    JNE " + label + "   ; 不为0时跳转");
        }
        codeSection.add("");
    }

    /**
     * 处理标签
     */
    private void handleLabel(String label) {
        codeSection.add(label + ":");
    }

    /**
     * 转换立即数格式
     */
    private String convertImmediate(String immediate) {
        if ("true".equals(immediate)) {
            return "1";
        } else if ("false".equals(immediate)) {
            return "0";
        }
        return immediate;
    }

    /**
     * 获取对应的跳转指令
     */
    private String getJumpInstruction(String compareOp) {
        switch (compareOp) {
            case "<":  return "JL";
            case ">":  return "JG";
            case "<=": return "JLE";
            case ">=": return "JGE";
            case "==": return "JE";
            case "!=": return "JNE";
            default:   return "JMP";
        }
    }

    /**
     * 构建最终的汇编代码
     */
    private String buildFinalAssembly() {
        StringBuilder sb = new StringBuilder();

        // 添加数据段
        for (String line : dataSection) {
            sb.append(line).append("\n");
        }

        sb.append("\n");

        // 添加代码段
        for (String line : codeSection) {
            sb.append(line).append("\n");
        }

        return sb.toString();
    }

    /**
     * 打印四元式（调试用）
     */
    public void printQuadruples() {
        System.out.println("\n=== 四元式列表 ===");
        for (int i = 0; i < quadruples.size(); i++) {
            Quadruple q = quadruples.get(i);
            System.out.printf("%3d: (%s, %s, %s, %s)\n",
                    i, q.getOp(), q.getArg1(), q.getArg2(), q.getResult());
        }
    }

    /**
     * 修正的测试主函数
     */
    public static void main(String[] args) {
        String[] testPrograms = {
                "void main() { int x = 5; }",
                "void main() { int x = 5; int y = x + 10; }",
                "void main() { int a = 3; int b = 4; int c = a + b * 2; }",
                "void main() { if (x > 0) { y = x + 1; } else { y = 0; } }",
                "void main() { while (i < 10) { i = i + 1; } }"
        };

        Map<String, Integer> tokenMap = TokenLibrary.readToken(
                "C:\\Users\\WYR\\Desktop\\编译原理\\compliefx2\\src\\main\\resources\\com\\example\\compliefx2\\tokenTable.json");

        for (String program : testPrograms) {
            try {
                System.out.println("\n" + "=".repeat(60));
                System.out.println("测试程序: " + program);
                System.out.println("=".repeat(60));

                StatementParser parser = new StatementParser(new java.io.StringReader(program), tokenMap);
                boolean isValid = parser.parseProgram();

                System.out.println("解析结果: " + (isValid ? "成功" : "失败"));

                if (!parser.getErrorMsg().isEmpty()) {
                    System.out.println("\n错误信息:");
                    System.out.println(parser.getErrorMsg());
                }

                // 输出AST
                ASTNode ast = parser.getAST();
                if (ast != null) {
                    System.out.println("\nAST结构:");
                    System.out.println(ast.toString());
                }

                // 输出中间代码
                if (isValid) {
                    IntermediateCodeGenerator codeGen = parser.getCodeGenerator();
                    System.out.println("\n中间代码:");
                    codeGen.printQuadruples();

                    // 生成目标代码
                    CodeGenerator gen = new CodeGenerator();
                    gen.setQuadruples(codeGen.getQuadruples());  // 正确设置四元式
                    String asm = gen.generateAssembly();
                    System.out.println("\n生成的汇编代码:");
                    System.out.println(asm);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}