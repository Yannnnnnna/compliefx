package com.example.compliefx2.syntaxAnalyzer;

import com.example.compliefx2.ManualLexer;
import com.example.compliefx2.Token;
import com.example.compliefx2.TokenLibrary;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

/**
 * 布尔表达式的递归下降分析器
 * 实现以下文法:
 B -> B || T | T       // 支持左结合的逻辑或
 T -> T && F | F       // 支持左结合的逻辑与
 F -> !F | R | (B)     // 支持逻辑非和括号
 R -> E relop E | ID | true | false  // 支持关系表达式、标识符和布尔字面量
 R  -> E relop E
 | ID
 | true
 | false

 E  -> Integer
 | FloatNumber
 | String
 | Identifier
 | char

 relop -> < | <= | > | >= | == | !=  // 定义关系运算符
 E -> 算术表达式（已实现）
 */
public class BooleanParser {
    private ManualLexer lexer;  // 词法分析器
    private Token currentToken; // 当前处理的Token
    private StringBuilder parseTree; // 解析树字符串
    private StringBuilder errorMsg;  // 错误信息
    private Map<String, Integer> tokenMap; // Token映射表
    private int indentLevel; // 用于构建解析树的缩进级别

    // Token类型常量
    private int TOKEN_LOGICAL_AND;  // &&
    private int TOKEN_LOGICAL_OR;   // ||
    private int TOKEN_LOGICAL_NOT;  // !
    private int TOKEN_LESS;         // <
    private int TOKEN_LESS_EQUAL;   // <=
    private int TOKEN_GREATER;      // >
    private int TOKEN_GREATER_EQUAL; // >=
    private int TOKEN_EQUAL;        // ==
    private int TOKEN_NOT_EQUAL;    // !=
    private int TOKEN_LPAREN;       // (
    private int TOKEN_RPAREN;       // )
    private int TOKEN_ID;           // 标识符
    private int TOKEN_NUMBER;       // 数字
    private int TOKEN_STRING;   // 字符串字面量类型
    private int TOKEN_FLOAT;    // 浮点数字面量类型
    private int TOKEN_CHAR;     //字符
    private  int TOKEN_TRUE;
    private  int TOKEN_FALSE;


    /**
     * 构造函数
     * @param reader 输入源
     * @param tokenMap Token映射表
     * @throws IOException 如果IO操作失败
     */
    public BooleanParser(Reader reader, Map<String, Integer> tokenMap) throws IOException {
        this.lexer = new ManualLexer(reader, tokenMap);
        this.parseTree = new StringBuilder();
        this.errorMsg = new StringBuilder();
        this.tokenMap = tokenMap;
        this.indentLevel = 0;

        // 初始化Token类型常量
        initTokenTypes(tokenMap);

        // 初始化时获取第一个Token
        advance();
    }

    /**
     * 初始化Token类型常量
     */
    private void initTokenTypes(Map<String, Integer> tokenMap) {
        TOKEN_LOGICAL_AND = tokenMap.getOrDefault("&&", 217);
        TOKEN_LOGICAL_OR = tokenMap.getOrDefault("||", 218);
        TOKEN_LOGICAL_NOT = tokenMap.getOrDefault("!", 205);
        TOKEN_LESS = tokenMap.getOrDefault("<", 211);
        TOKEN_LESS_EQUAL = tokenMap.getOrDefault("<=", 212);
        TOKEN_GREATER = tokenMap.getOrDefault(">", 213);
        TOKEN_GREATER_EQUAL = tokenMap.getOrDefault(">=", 214);
        TOKEN_EQUAL = tokenMap.getOrDefault("==", 215);
        TOKEN_NOT_EQUAL = tokenMap.getOrDefault("!=", 216);
        TOKEN_LPAREN = tokenMap.getOrDefault("(", 201);
        TOKEN_RPAREN = tokenMap.getOrDefault(")", 202);
        TOKEN_ID = tokenMap.getOrDefault("Identifier", 700);
        TOKEN_NUMBER = tokenMap.getOrDefault("Integer", 400);
        TOKEN_STRING = tokenMap.getOrDefault("String", 600);
        TOKEN_CHAR = tokenMap.getOrDefault("Character",500);
        TOKEN_FLOAT = tokenMap.getOrDefault("FloatNumber", 900);
        TOKEN_TRUE = tokenMap.getOrDefault("true", 115);
        TOKEN_FALSE = tokenMap.getOrDefault("false", 116);
        // 调试信息
        System.out.println("布尔表达式Token类型初始化：" +
                "&&=" + TOKEN_LOGICAL_AND +
                ", ||=" + TOKEN_LOGICAL_OR +
                ", <==" + TOKEN_LESS_EQUAL +
                ", >=" + TOKEN_GREATER +
                ", ID=" + TOKEN_ID +
                ", char=" + TOKEN_CHAR +
                ", float=" + TOKEN_FLOAT +
                ", String=" + TOKEN_STRING);
    }

    /**
     * 前进到下一个Token
     * @throws IOException 如果IO操作失败
     */
    private void advance() throws IOException {
        currentToken = lexer.nextToken();
        if (currentToken != null) {
            System.out.println("当前Token: 类型=" + currentToken.type + ", 值=" + currentToken.value);
        }
    }

    /**
     * 检查当前Token是否匹配预期类型
     * @param tokenType 预期的Token类型
     * @return 如果匹配则返回true，否则返回false
     */
    private boolean match(int tokenType) {
        return currentToken != null && currentToken.type == tokenType;
    }

    /**
     * 处理给定类型的Token，如果不匹配则报错
     * @param tokenType 预期的Token类型
     * @param errorMessage 错误消息
     * @throws IOException 如果IO操作失败
     */
    private void consume(int tokenType, String errorMessage) throws IOException {
        if (match(tokenType)) {
            advance();
        } else {
            error(errorMessage);
        }
    }

    /**
     * 添加错误信息
     * @param message 错误消息
     */
    private void error(String message) {
        if (currentToken != null) {
            errorMsg.append("错误(行 ").append(currentToken.getLineNumber()).append("): ");
        } else {
            errorMsg.append("错误: ");
        }
        errorMsg.append(message).append("\n");
    }

    /**
     * 解析布尔表达式
     * @return 是否解析成功
     * @throws IOException 如果IO操作失败
     */
    public boolean parse() throws IOException {
        try {
            B(); // 从B开始解析
            // 如果最后没有更多Token且没有错误，则解析成功
            return errorMsg.length() == 0 && (currentToken == null || currentToken.type == -1);
        } catch (Exception e) {
            error("解析过程发生异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 添加一行到解析树
     * @param text 要添加的文本
     */
    private void addToParseTree(String text) {
        // 添加缩进
        for (int i = 0; i < indentLevel; i++) {
            parseTree.append("  ");
        }
        parseTree.append(text).append("\n");
    }

    /**
     * 获取解析树字符串
     * @return 解析树
     */
    public String getParseTree() {
        return parseTree.toString();
    }

    /**
     * 获取错误信息
     * @return 错误信息
     */
    public String getErrorMsg() {
        return errorMsg.toString();
    }

    /**
     * B -> B || T | T
     * 使用左递归消除：
     * B -> T B'
     * B' -> || T B' | ε
     * @throws IOException 如果IO操作失败
     */
    private void B() throws IOException {
        addToParseTree("B -> T B'");
        indentLevel++;

        T(); // 首先解析T
        BPrime(); // 然后解析B'

        indentLevel--;
    }

    /**
     * B' -> || T B' | ε
     * @throws IOException 如果IO操作失败
     */
    private void BPrime() throws IOException {
        // 检查是否是逻辑或运算符 ||
        if (match(TOKEN_LOGICAL_OR)) {
            addToParseTree("B' -> || T B'");
            indentLevel++;

            consume(TOKEN_LOGICAL_OR, "期望 || 运算符");
            addToParseTree("匹配: ||");

            T(); // 解析T
            BPrime(); // 递归解析B'

            indentLevel--;
        } else {
            // 空产生式
            addToParseTree("B' -> ε");
        }
    }

    /**
     * T -> T && F | F
     * 使用左递归消除：
     * T -> F T'
     * T' -> && F T' | ε
     * @throws IOException 如果IO操作失败
     */
    private void T() throws IOException {
        addToParseTree("T -> F T'");
        indentLevel++;

        F(); // 首先解析F
        TPrime(); // 然后解析T'

        indentLevel--;
    }

    /**
     * T' -> && F T' | ε
     * @throws IOException 如果IO操作失败
     */
    private void TPrime() throws IOException {
        // 检查是否是逻辑与运算符 &&
        if (match(TOKEN_LOGICAL_AND)) {
            addToParseTree("T' -> && F T'");
            indentLevel++;

            consume(TOKEN_LOGICAL_AND, "期望 && 运算符");
            addToParseTree("匹配: &&");

            F(); // 解析F
            TPrime(); // 递归解析T'

            indentLevel--;
        } else {
            // 空产生式
            addToParseTree("T' -> ε");
        }
    }

    /**
     * F -> !F | R | (B)
     * @throws IOException 如果IO操作失败
     */
    private void F() throws IOException {
        if (match(TOKEN_LOGICAL_NOT)) {
            addToParseTree("F -> !F");
            indentLevel++;

            consume(TOKEN_LOGICAL_NOT, "期望 ! 运算符");
            addToParseTree("匹配: !");

            F(); // 递归解析F

            indentLevel--;
        } else if (match(TOKEN_LPAREN)) {
            addToParseTree("F -> (B)");
            indentLevel++;

            consume(TOKEN_LPAREN, "期望 (");
            addToParseTree("匹配: (");

            B(); // 解析B

            consume(TOKEN_RPAREN, "期望 )");
            addToParseTree("匹配: )");

            indentLevel--;
        } else {
            addToParseTree("F -> R");
            indentLevel++;

            R(); // 解析R

            indentLevel--;
        }
    }

    /**
     * R -> E relop E | ID | true | false
     * @throws IOException 如果IO操作失败
     */
    private void R() throws IOException {
        // 直接匹配字符串、布尔字面量或标识符
        if (match(TOKEN_STRING)) {
            addToParseTree("E -> String (" + currentToken.value + ")");
            advance();
            return;
        } else if (match(TOKEN_FLOAT)) {
            addToParseTree("E -> Float (" + currentToken.value + ")");
            advance();
            return;
        } else if ("true".equals(currentToken.value) || "false".equals(currentToken.value)) {
            addToParseTree("R -> " + currentToken.value);
            advance();
            return;
        }else if (match(TOKEN_CHAR)){
            addToParseTree("R -> char (" + currentToken.value + ")");
            advance();
            return;
        }

        // 尝试解析关系表达式 (E relop E)
        // 首先保存当前位置，以便需要回溯
        Token savedToken = currentToken;
        int savedPosition = lexer.getCurrentLineIndex(); // 使用currentLineIndex作为位置标记

        // 首先检查第一个操作数是否是ID或NUMBER
        if (match(TOKEN_ID) || match(TOKEN_NUMBER)
                || match(TOKEN_STRING) || match(TOKEN_CHAR) || match(TOKEN_FLOAT)) {
            String firstOperand = currentToken.value;
            int firstOperandType = currentToken.type;

            addToParseTree("R -> E relop E");
            indentLevel++;

            if (firstOperandType == TOKEN_ID) {
                addToParseTree("E -> ID (" + firstOperand + ")");
            } else {
                addToParseTree("E -> NUM (" + firstOperand + ")");
            }

            advance(); // 消耗第一个操作数

            // 检查是否是关系运算符
            if (isRelOp(currentToken)) {
                int relOpType = currentToken.type;
                String relOp = currentToken.value;
                addToParseTree("relop -> " + relOp);
                advance(); // 消耗关系运算符

                // 检查第二个操作数
                if (match(TOKEN_ID) || match(TOKEN_NUMBER)||
                        match(TOKEN_STRING) || match(TOKEN_CHAR) || match(TOKEN_FLOAT)){
                    String secondOperand = currentToken.value;
                    int secondOperandType = currentToken.type;

                    if (secondOperandType == TOKEN_ID) {
                        addToParseTree("E -> ID (" + secondOperand + ")");
                    } else if (secondOperandType == TOKEN_STRING) {
                        addToParseTree("E -> String (" + secondOperand + ")");
                    } else {
                        addToParseTree("E -> NUM (" + secondOperand + ")");
                    }

                    advance(); // 消耗第二个操作数
                    indentLevel--;
                    return;
                } else {
                    error("期望标识符或数字作为关系表达式的右操作数");
                    // 回溯
                    restorePosition(savedToken, savedPosition);
                }
            } else {
                // 如果不是关系运算符，则回溯
                restorePosition(savedToken, savedPosition);
            }
        }

        // 如果不是关系表达式，检查是否是标识符
        if (match(TOKEN_ID)) {
            addToParseTree("R -> ID (" + currentToken.value + ")");
            advance();
            return;
        }

        // 如果都不是，则报错
        error("期望布尔表达式或关系表达式");
    }

    /**
     * 恢复到之前保存的位置
     * @param savedToken 保存的Token
     * @param savedPosition 保存的位置
     */
    private void restorePosition(Token savedToken, int savedPosition) {
        currentToken = savedToken;
        lexer.setCurrentLineIndex(savedPosition); // 使用currentLineIndex直接恢复位置
    }

    /**
     * 判断Token是否为关系运算符
     * @param token 需要检查的Token
     * @return 如果是关系运算符则返回true，否则返回false
     */
    private boolean isRelOp(Token token) {
        if (token == null) return false;

        int tokenType = token.type;
        return tokenType == TOKEN_LESS ||
                tokenType == TOKEN_LESS_EQUAL ||
                tokenType == TOKEN_GREATER ||
                tokenType == TOKEN_GREATER_EQUAL ||
                tokenType == TOKEN_EQUAL ||
                tokenType == TOKEN_NOT_EQUAL;
    }

    /**
     * 主函数用于测试
     */
    public static void main(String[] args) {
        // 示例的布尔表达式
        String expression = "(status == \"active\") && (count > 0 || retries < 3)";
        Map<String, Integer> tokenMap = TokenLibrary.readToken("C:\\Users\\WYR\\Desktop\\编译原理\\compliefx2\\src\\main\\resources\\com\\example\\compliefx2\\tokenTable.json");

        try {
            BooleanParser parser = new BooleanParser(new StringReader(expression), tokenMap);
            boolean isValid = parser.parse();

            System.out.println("表达式: " + expression);
            System.out.println("是否有效: " + isValid);

            if (!parser.getErrorMsg().isEmpty()) {
                System.out.println("错误信息:");
                System.out.println(parser.getErrorMsg());
            }

            System.out.println("解析树:");
            System.out.println(parser.getParseTree());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}