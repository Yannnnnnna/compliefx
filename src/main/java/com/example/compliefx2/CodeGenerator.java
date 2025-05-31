package com.example.compliefx2;

import com.example.compliefx2.intermediateCode.IntermediateCodeGenerator;
import com.example.compliefx2.intermediateCode.Quadruple;
import com.example.compliefx2.syntaxAnalyzer.StatementParser;

import java.io.IOException;
import java.util.*;

/**
 * 目标代码生成器 - 将四元式转换为8086汇编代码
 * 修改版本：整数运算结果为浮点数时忽略小数部分
 */
public class CodeGenerator {
    private List<Quadruple> quadruples;                      // 四元式列表
    private final Map<String, String> symbolTable;          // 变量名 -> 类型映射
    private final Set<String> tempVars;                      // 临时变量集合
    private final List<String> dataSection;                 // 数据段内容
    private final List<String> codeSection;                 // 代码段内容
    private int assemblyLabelCounter;                        // 汇编标签计数器
    private final Set<String> userVariables;                // 用户定义的变量（非临时变量）

    public CodeGenerator() {
        this.quadruples = new ArrayList<>();
        this.symbolTable = new HashMap<>();
        this.tempVars = new HashSet<>();
        this.dataSection = new ArrayList<>();
        this.codeSection = new ArrayList<>();
        this.assemblyLabelCounter = 0;
        this.userVariables = new HashSet<>();
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
        userVariables.clear();

        for (Quadruple q : quadruples) {
            // 处理变量声明
            if ("DECLARE".equals(q.getOp())) {
                String varName = q.getResult();
                String varType = q.getArg1();

                // 确保变量名不是保留字
                if (!isReservedWord(varName) && !isLanguageKeyword(varName)) {
                    symbolTable.put(varName, varType);
                    userVariables.add(varName);
                }
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
        } else if (identifier.matches("[a-zA-Z_][a-zA-Z0-9_]*") &&
                !symbolTable.containsKey(identifier) &&
                !isReservedWord(identifier) &&
                !isLanguageKeyword(identifier)) {
            // 普通变量，确保不是关键字或保留字
            symbolTable.put(identifier, "int");
            userVariables.add(identifier);
        }
    }

    /**
     * 判断是否为立即数
     */
    private boolean isImmediate(String operand) {
        if (operand == null) return false;
        return operand.matches("-?\\d+") ||
                operand.matches("-?\\d+\\.\\d+") ||
                operand.equals("true") ||
                operand.equals("false");
    }

    /**
     * 判断是否为浮点数
     */
    private boolean isFloatingPoint(String operand) {
        if (operand == null) return false;
        return operand.matches("-?\\d+\\.\\d+");
    }

    /**
     * 判断是否为保留字或标签
     */
    private boolean isReservedWord(String word) {
        return word.startsWith("L") || word.equals("main") || word.equals("true") || word.equals("false");
    }

    /**
     * 判断是否为编程语言关键字
     */
    private boolean isLanguageKeyword(String word) {
        Set<String> keywords = new HashSet<>(Arrays.asList(
                "int", "float", "double", "char", "void", "if", "else", "while",
                "for", "do", "break", "continue", "return", "switch", "case",
                "default", "struct", "union", "enum", "typedef", "const", "static",
                "extern", "auto", "register", "volatile", "sizeof", "goto"
        ));
        return keywords.contains(word.toLowerCase());
    }

    /**
     * 判断是否为汇编保留字
     */
    private boolean isAssemblyReserved(String word) {
        Set<String> asmReserved = new HashSet<>(Arrays.asList(
                "AX", "BX", "CX", "DX", "SI", "DI", "BP", "SP", "AL", "AH", "BL", "BH",
                "CL", "CH", "DL", "DH", "CS", "DS", "ES", "SS", "IP", "FLAGS",
                "MOV", "ADD", "SUB", "MUL", "DIV", "CMP", "JMP", "JE", "JNE", "JL", "JG",
                "PUSH", "POP", "CALL", "RET", "INT", "NOP", "DB", "DW", "DD", "DQ",
                "ORG", "END", "PROC", "ENDP", "SEGMENT", "ENDS", "ASSUME", "OFFSET",
                "PTR", "BYTE", "WORD", "DWORD", "QWORD", "NEAR", "FAR", "SHORT",
                "MODEL", "SMALL", "MEDIUM", "LARGE", "HUGE", "TINY", "FLAT",
                "DATA", "CODE", "STACK", "BSS", "CONST"
        ));
        return asmReserved.contains(word.toUpperCase());
    }

    /**
     * 获取安全的变量名（避免汇编关键字冲突）
     */
    private String getSafeVariableName(String varName) {
        if (isAssemblyReserved(varName)) {
            return "var_" + varName;
        }
        return varName;
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
            String safeVarName = getSafeVariableName(varName);

            dataSection.add("    " + safeVarName + " DW 0    ; " + varType + " variable (" + varName + ")");
        }

        // 声明临时变量
        for (String tempVar : tempVars) {
            String safeTempVar = getSafeVariableName(tempVar);
            dataSection.add("    " + safeTempVar + " DW 0    ; temporary variable (" + tempVar + ")");
        }

        // 添加输出相关的数据
        dataSection.add("");
        dataSection.add("    ; 输出相关的字符串和缓冲区");
        dataSection.add("    msg_header DB 'Program execution results:', 0Dh, 0Ah, '$'");
        dataSection.add("    msg_separator DB '=', '=', '=', '=', '=', '=', '=', '=', '=', '=', 0Dh, 0Ah, '$'");
        dataSection.add("    msg_success DB 'Program executed successfully!', 0Dh, 0Ah, '$'");
        dataSection.add("    msg_var_prefix DB '  ', '$'");
        dataSection.add("    msg_equals DB ' = ', '$'");
        dataSection.add("    msg_newline DB 0Dh, 0Ah, '$'");
        dataSection.add("    msg_press_key DB 'Press any key to exit...', '$'");

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

        // 输出程序执行结果
        generateOutputCode();

        // 程序结束
        codeSection.add("");
        codeSection.add("    MOV AH, 4Ch    ; DOS程序结束");
        codeSection.add("    INT 21h");

        // 添加辅助函数
        generateHelperFunctions();

        codeSection.add("");
        codeSection.add("END START");
    }

    /**
     * 生成输出代码
     */
    private void generateOutputCode() {
        codeSection.add("    ; ===== 输出程序执行结果 =====");
        codeSection.add("    LEA DX, msg_header");
        codeSection.add("    MOV AH, 09h");
        codeSection.add("    INT 21h");
        codeSection.add("");

        codeSection.add("    LEA DX, msg_separator");
        codeSection.add("    MOV AH, 09h");
        codeSection.add("    INT 21h");
        codeSection.add("");

        codeSection.add("    LEA DX, msg_success");
        codeSection.add("    MOV AH, 09h");
        codeSection.add("    INT 21h");
        codeSection.add("");

        // 输出每个用户变量的值
        for (String varName : userVariables) {
            generateVariableOutput(varName);
        }

        codeSection.add("    LEA DX, msg_separator");
        codeSection.add("    MOV AH, 09h");
        codeSection.add("    INT 21h");
        codeSection.add("");

        codeSection.add("    LEA DX, msg_press_key");
        codeSection.add("    MOV AH, 09h");
        codeSection.add("    INT 21h");
        codeSection.add("");

        codeSection.add("    ; 等待按键");
        codeSection.add("    MOV AH, 01h");
        codeSection.add("    INT 21h");
        codeSection.add("");
    }

    /**
     * 生成单个变量的输出代码
     */
    private void generateVariableOutput(String varName) {
        String safeVarName = getSafeVariableName(varName);

        codeSection.add("    ; 输出变量 " + varName);
        codeSection.add("    LEA DX, msg_var_prefix");
        codeSection.add("    MOV AH, 09h");
        codeSection.add("    INT 21h");
        codeSection.add("");

        // 输出变量名
        codeSection.add("    ; 输出变量名 '" + varName + "'");
        for (char c : varName.toCharArray()) {
            codeSection.add("    MOV DL, '" + c + "'");
            codeSection.add("    MOV AH, 02h");
            codeSection.add("    INT 21h");
        }

        codeSection.add("    LEA DX, msg_equals");
        codeSection.add("    MOV AH, 09h");
        codeSection.add("    INT 21h");
        codeSection.add("");

        // 输出变量值
        codeSection.add("    MOV AX, " + safeVarName);
        codeSection.add("    CALL PrintNumber");
        codeSection.add("");

        codeSection.add("    LEA DX, msg_newline");
        codeSection.add("    MOV AH, 09h");
        codeSection.add("    INT 21h");
        codeSection.add("");
    }

    /**
     * 生成辅助函数
     */
    private void generateHelperFunctions() {
        codeSection.add("");
        codeSection.add("; ===== 辅助函数 =====");
        codeSection.add("");

        // 数字输出函数
        codeSection.add("PrintNumber PROC");
        codeSection.add("    ; 输入: AX = 要输出的数字");
        codeSection.add("    ; 保存寄存器");
        codeSection.add("    PUSH BX");
        codeSection.add("    PUSH CX");
        codeSection.add("    PUSH DX");
        codeSection.add("    PUSH SI");
        codeSection.add("");

        codeSection.add("    ; 处理负数");
        codeSection.add("    MOV CX, 0         ; 数字位数计数器");
        codeSection.add("    MOV BX, 10        ; 除数");
        codeSection.add("    CMP AX, 0");
        codeSection.add("    JGE PositiveNumber");
        codeSection.add("");

        codeSection.add("    ; 输出负号");
        codeSection.add("    PUSH AX");
        codeSection.add("    MOV DL, '-'");
        codeSection.add("    MOV AH, 02h");
        codeSection.add("    INT 21h");
        codeSection.add("    POP AX");
        codeSection.add("    NEG AX            ; 转为正数");
        codeSection.add("");

        codeSection.add("PositiveNumber:");
        codeSection.add("    ; 特殊处理0");
        codeSection.add("    CMP AX, 0");
        codeSection.add("    JNE ConvertLoop");
        codeSection.add("    MOV DL, '0'");
        codeSection.add("    MOV AH, 02h");
        codeSection.add("    INT 21h");
        codeSection.add("    JMP PrintNumberEnd");
        codeSection.add("");

        codeSection.add("ConvertLoop:");
        codeSection.add("    ; 将数字转换为字符并压栈");
        codeSection.add("    CMP AX, 0");
        codeSection.add("    JE PrintLoop");
        codeSection.add("    XOR DX, DX        ; 清除高位");
        codeSection.add("    DIV BX            ; AX = AX/10, DX = AX%10");
        codeSection.add("    ADD DL, '0'       ; 转换为ASCII");
        codeSection.add("    PUSH DX           ; 压栈保存");
        codeSection.add("    INC CX            ; 位数+1");
        codeSection.add("    JMP ConvertLoop");
        codeSection.add("");

        codeSection.add("PrintLoop:");
        codeSection.add("    ; 从栈中弹出并输出字符");
        codeSection.add("    CMP CX, 0");
        codeSection.add("    JE PrintNumberEnd");
        codeSection.add("    POP DX");
        codeSection.add("    MOV AH, 02h");
        codeSection.add("    INT 21h");
        codeSection.add("    DEC CX");
        codeSection.add("    JMP PrintLoop");
        codeSection.add("");

        codeSection.add("PrintNumberEnd:");
        codeSection.add("    ; 恢复寄存器");
        codeSection.add("    POP SI");
        codeSection.add("    POP DX");
        codeSection.add("    POP CX");
        codeSection.add("    POP BX");
        codeSection.add("    RET");
        codeSection.add("PrintNumber ENDP");
        codeSection.add("");
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
        // 函数结束通常不需要特殊处理
    }


    /**
     * 处理赋值操作 - 修改版本：正确处理布尔值和浮点数
     */
    private void handleAssignment(String source, String destination) {
        String safeSource = getSafeVariableName(source);
        String safeDest = getSafeVariableName(destination);

        codeSection.add("    ; " + destination + " = " + source);

        if (isImmediate(source)) {
            if (isFloatingPoint(source)) {
                // 浮点数立即数：取整数部分
                double value = Double.parseDouble(source);
                int intValue = (int) value;  // 忽略小数部分
                codeSection.add("    MOV AX, " + intValue);
            } else {
                // 使用convertImmediate方法处理布尔值
                codeSection.add("    MOV AX, " + convertImmediate(source));
            }
        } else {
            codeSection.add("    MOV AX, " + safeSource);
        }
        codeSection.add("    MOV " + safeDest + ", AX");
        codeSection.add("");
    }

    /**
     * 处理算术运算 - 修改版本：整数运算时忽略小数部分
     */
    private void handleArithmetic(String operation, String operand1, String operand2, String result) {
        String safeOp1 = getSafeVariableName(operand1);
        String safeOp2 = getSafeVariableName(operand2);
        String safeResult = getSafeVariableName(result);

        codeSection.add("    ; " + result + " = " + operand1 + " " + operation + " " + operand2);

        // 加载第一个操作数到AX
        if (isImmediate(operand1)) {
            if (isFloatingPoint(operand1)) {
                // 浮点数立即数：取整数部分
                double value = Double.parseDouble(operand1);
                int intValue = (int) value;
                codeSection.add("    MOV AX, " + intValue);
            } else {
                codeSection.add("    MOV AX, " + operand1);
            }
        } else {
            codeSection.add("    MOV AX, " + safeOp1);
        }

        // 执行运算
        switch (operation) {
            case "+":
                if (isImmediate(operand2)) {
                    if (isFloatingPoint(operand2)) {
                        double value = Double.parseDouble(operand2);
                        int intValue = (int) value;
                        codeSection.add("    ADD AX, " + intValue);
                    } else {
                        codeSection.add("    ADD AX, " + operand2);
                    }
                } else {
                    codeSection.add("    ADD AX, " + safeOp2);
                }
                break;

            case "-":
                if (isImmediate(operand2)) {
                    if (isFloatingPoint(operand2)) {
                        double value = Double.parseDouble(operand2);
                        int intValue = (int) value;
                        codeSection.add("    SUB AX, " + intValue);
                    } else {
                        codeSection.add("    SUB AX, " + operand2);
                    }
                } else {
                    codeSection.add("    SUB AX, " + safeOp2);
                }
                break;

            case "*":
                if (isImmediate(operand2)) {
                    if (isFloatingPoint(operand2)) {
                        double value = Double.parseDouble(operand2);
                        int intValue = (int) value;
                        codeSection.add("    MOV BX, " + intValue);
                    } else {
                        codeSection.add("    MOV BX, " + operand2);
                    }
                } else {
                    codeSection.add("    MOV BX, " + safeOp2);
                }
                codeSection.add("    MUL BX");
                break;

            case "/":
                if (isImmediate(operand2)) {
                    if (isFloatingPoint(operand2)) {
                        double value = Double.parseDouble(operand2);
                        int intValue = (int) value;
                        codeSection.add("    MOV BX, " + intValue);
                    } else {
                        codeSection.add("    MOV BX, " + operand2);
                    }
                } else {
                    codeSection.add("    MOV BX, " + safeOp2);
                }
                codeSection.add("    XOR DX, DX    ; 清除高位");
                codeSection.add("    DIV BX        ; 整数除法，自动忽略小数部分");
                break;
        }

        // 存储结果
        codeSection.add("    MOV " + safeResult + ", AX");
        codeSection.add("");
    }

    /**
     * 处理比较运算
     */
    private void handleComparison(String operation, String operand1, String operand2, String result) {
        String safeOp1 = getSafeVariableName(operand1);
        String safeOp2 = getSafeVariableName(operand2);
        String safeResult = getSafeVariableName(result);

        codeSection.add("    ; " + result + " = " + operand1 + " " + operation + " " + operand2);

        String trueLabel = "TRUE_" + (assemblyLabelCounter++);
        String endLabel = "END_" + (assemblyLabelCounter++);

        // 加载操作数并比较
        if (isImmediate(operand1)) {
            if (isFloatingPoint(operand1)) {
                double value = Double.parseDouble(operand1);
                int intValue = (int) value;
                codeSection.add("    MOV AX, " + intValue);
            } else {
                codeSection.add("    MOV AX, " + convertImmediate(operand1));
            }
        } else {
            codeSection.add("    MOV AX, " + safeOp1);
        }

        if (isImmediate(operand2)) {
            if (isFloatingPoint(operand2)) {
                double value = Double.parseDouble(operand2);
                int intValue = (int) value;
                codeSection.add("    CMP AX, " + intValue);
            } else {
                codeSection.add("    CMP AX, " + convertImmediate(operand2));
            }
        } else {
            codeSection.add("    CMP AX, " + safeOp2);
        }

        // 根据比较结果跳转
        String jumpInstruction = getJumpInstruction(operation);
        codeSection.add("    " + jumpInstruction + " " + trueLabel);

        // 条件为假
        codeSection.add("    MOV " + safeResult + ", 0");
        codeSection.add("    JMP " + endLabel);

        // 条件为真
        codeSection.add(trueLabel + ":");
        codeSection.add("    MOV " + safeResult + ", 1");

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
     * 处理条件跳转 - 修复版本：正确处理布尔值
     */
    private void handleConditionalJump(String jumpType, String condition, String label) {
        String safeCond = getSafeVariableName(condition);

        codeSection.add("    ; 条件跳转: " + jumpType + " " + condition + " -> " + label);

        // 检查条件值
        if (isImmediate(condition)) {
            // 使用convertImmediate方法正确转换布尔值
            String convertedValue = convertImmediate(condition);
            codeSection.add("    MOV AX, " + convertedValue);
            codeSection.add("    CMP AX, 0");
        } else {
            codeSection.add("    CMP " + safeCond + ", 0");
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
        } else if (isFloatingPoint(immediate)) {
            // 浮点数转换为整数（忽略小数部分）
            double value = Double.parseDouble(immediate);
            int intValue = (int) value;
            return String.valueOf(intValue);
        }
        return immediate;
    }

    /**
     * 获取对应的跳转指令
     */
    private String getJumpInstruction(String compareOp) {
        switch (compareOp) {
            case "<":
                return "JL";
            case ">":
                return "JG";
            case "<=":
                return "JLE";
            case ">=":
                return "JGE";
            case "==":
                return "JE";
            case "!=":
                return "JNE";
            default:
                return "JMP";
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
}