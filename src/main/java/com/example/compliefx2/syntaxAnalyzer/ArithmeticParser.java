package com.example.compliefx2.syntaxAnalyzer;

import com.example.compliefx2.ManualLexer;
import com.example.compliefx2.Token;
import com.example.compliefx2.TokenLibrary;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 算术表达式的递归下降分析器
 * 根据以下文法实现:
 * E → T E'
 * E' → + T E' | - T E' | ε
 * T → F T'
 * T' → * F T' | / F T' | % F T' | ε
 * F → (E) | NUM | ID
 */
public class ArithmeticParser {
    private ManualLexer lexer;
    private Token currentToken;
    private StringBuilder parseTree;
    private StringBuilder errorMsg;
    private int treeIndent;

    // Token 类型常量（可以通过TokenLibrary动态获取）
    private int TOKEN_PLUS;      // +
    private int TOKEN_MINUS;     // -
    private int TOKEN_MULTIPLY;  // *
    private int TOKEN_DIVIDE;    // /
    private int TOKEN_MOD;       // %
    private int TOKEN_LPAREN;    // (
    private int TOKEN_RPAREN;    // )
    private int TOKEN_NUMBER;    // 数字
    private int TOKEN_ID;        // 标识符

    // 初始化Token类型常量
    private void initTokenTypes(Map<String, Integer> tokenMap) {
        // 从tokenMap获取对应的种别码
        TOKEN_PLUS = tokenMap.getOrDefault("+", 43);
        TOKEN_MINUS = tokenMap.getOrDefault("-", 45);
        TOKEN_MULTIPLY = tokenMap.getOrDefault("*", 42);
        TOKEN_DIVIDE = tokenMap.getOrDefault("/", 47);
        TOKEN_MOD = tokenMap.getOrDefault("%", 37);
        TOKEN_LPAREN = tokenMap.getOrDefault("(", 40);
        TOKEN_RPAREN = tokenMap.getOrDefault(")", 41);

        // 获取数字和标识符的种别码
        // 这里假设tokenMap中使用"NUM"表示数字，"ID"表示标识符
        // 根据实际tokenTable.json调整键名
        TOKEN_NUMBER = tokenMap.getOrDefault("Integer", 400);
        TOKEN_ID = tokenMap.getOrDefault("Identifier", 700);

        // 调试信息
        System.out.println("Token类型初始化：ID=" + TOKEN_ID + ", NUM=" + TOKEN_NUMBER);
    }

    public ArithmeticParser(ManualLexer lexer, Map<String, Integer> tokenMap) throws IOException {
        this.lexer = lexer;
        this.parseTree = new StringBuilder();
        this.errorMsg = new StringBuilder();
        this.treeIndent = 0;

        // 初始化Token类型
        initTokenTypes(tokenMap);

        this.advance();
    }

    /**
     * 获取下一个Token
     */
    private void advance() throws IOException {
        currentToken = lexer.nextToken();
    }

    /**
     * 记录解析树节点
     */
    private void addTreeNode(String node) {
        // 添加缩进
        for (int i = 0; i < treeIndent; i++) {
            parseTree.append("  ");
        }
        parseTree.append(node).append("\n");
    }

    /**
     * 表达式解析入口
     * E → T E'
     */
    public boolean E() throws IOException {
        addTreeNode("E");
        treeIndent++;

        boolean result = T();
        if (result) {
            result = EPrime();
        }

        treeIndent--;
        return result;
    }

    /**
     * 表达式的后续部分
     * E' → + T E' | - T E' | ε
     */
    private boolean EPrime() throws IOException {
        addTreeNode("E'");
        treeIndent++;

        // 如果当前Token是加号或减号
        if (currentToken != null && (currentToken.type == TOKEN_PLUS || currentToken.type == TOKEN_MINUS)) {
            int operatorType = currentToken.type;
            String operator = currentToken.value;

            addTreeNode("运算符: " + operator);
            advance(); // 消耗掉运算符

            if (!T()) {
                errorMsg.append("行").append(currentToken != null ? currentToken.getLineNumber() : "未知").append(": 期望一个项(T)在").append(operator).append("之后\n");
                treeIndent--;
                return false;
            }

            return EPrime(); // 递归检查是否有更多的加减法表达式
        }

        // E'可以推导出空，所以默认返回true
        addTreeNode("ε (空)");
        treeIndent--;
        return true;
    }

    /**
     * 项解析
     * T → F T'
     */
    private boolean T() throws IOException {
        addTreeNode("T");
        treeIndent++;

        boolean result = F();
        if (result) {
            result = TPrime();
        }

        treeIndent--;
        return result;
    }

    /**
     * 项的后续部分
     * T' → * F T' | / F T' | % F T' | ε
     */
    private boolean TPrime() throws IOException {
        addTreeNode("T'");
        treeIndent++;

        // 如果当前Token是乘号、除号或取模
        if (currentToken != null && (currentToken.type == TOKEN_MULTIPLY ||
                currentToken.type == TOKEN_DIVIDE ||
                currentToken.type == TOKEN_MOD)) {
            int operatorType = currentToken.type;
            String operator = currentToken.value;

            addTreeNode("运算符: " + operator);
            advance(); // 消耗掉运算符

            if (!F()) {
                errorMsg.append("行").append(currentToken != null ? currentToken.getLineNumber() : "未知").append(": 期望一个因子(F)在").append(operator).append("之后\n");
                treeIndent--;
                return false;
            }

            return TPrime(); // 递归检查是否有更多的乘除模表达式
        }

        // T'可以推导出空，所以默认返回true
        addTreeNode("ε (空)");
        treeIndent--;
        return true;
    }

    /**
     * 因子解析
     * F → (E) | NUM | ID
     */
    private boolean F() throws IOException {
        addTreeNode("F");
        treeIndent++;

        if (currentToken == null) {
            errorMsg.append("行未知: 意外的文件结束，期望一个因子\n");
            treeIndent--;
            return false;
        }

        boolean result = false;

        // 调试信息
        System.out.println("当前Token: 类型=" + currentToken.type + ", 值=" + currentToken.value);

        if (currentToken.type == TOKEN_LPAREN) { // (
            addTreeNode("左括号: (");
            advance(); // 消耗左括号

            result = E(); // 递归解析括号内的表达式

            if (result) {
                if (currentToken != null && currentToken.type == TOKEN_RPAREN) {
                    addTreeNode("右括号: )");
                    advance(); // 消耗右括号
                } else {
                    errorMsg.append("行").append(currentToken != null ? currentToken.getLineNumber() : "未知").append(": 缺少右括号\n");
                    result = false;
                }
            }
        } else if (currentToken.type == TOKEN_NUMBER) { // 数字
            addTreeNode("数字: " + currentToken.value);
            advance(); // 消耗数字
            result = true;
        } else if (currentToken.type == TOKEN_ID) { // 标识符
            addTreeNode("标识符: " + currentToken.value);
            advance(); // 消耗标识符
            result = true;
        } else {
            // 处理标识符，检查是否可能是一个变量名
            boolean isIdentifier = Character.isLetter(currentToken.value.charAt(0));
            if (isIdentifier) {
                addTreeNode("标识符(可能): " + currentToken.value);
                advance(); // 消耗标识符
                result = true;
            } else {
                errorMsg.append("行").append(currentToken.getLineNumber()).append(": 意外的Token: ").append(currentToken.value).append("，期望一个因子\n");
                result = false;
            }
        }

        treeIndent--;
        return result;
    }

    /**
     * 开始解析过程
     * @return 是否是有效的算术表达式
     */
    public boolean parse() throws IOException {
        boolean result = E(); // 从起始符号E开始

        // 检查是否已经到达输入的末尾
        if (result && currentToken != null) {
            errorMsg.append("行").append(currentToken.getLineNumber()).append(": 表达式后有额外的内容: ").append(currentToken.value).append("\n");
            return false;
        }

        return result;
    }

    /**
     * 获取解析树
     */
    public String getParseTree() {
        return parseTree.toString();
    }

    /**
     * 获取错误信息
     */
    public String getErrorMsg() {
        return errorMsg.toString();
    }

    /**
     * 主函数用于测试
     */
//    public static void main(String[] args) {
//        // 示例的算术表达式
//        String expression = "3 + 4 * (5 - 2)";
//        Map<String, Integer> tokenMap = TokenLibrary.readToken("C:\\Users\\WYR\\Desktop\\编译原理\\compliefx2\\src\\main\\resources\\com\\example\\compliefx2\\tokenTable.json");
//
//        try {
//            ArithmeticParser parser = new ArithmeticParser(new StringReader(expression), tokenMap);
//            boolean isValid = parser.parse();
//
//            System.out.println("表达式: " + expression);
//            System.out.println("是否有效: " + isValid);
//
//            if (!parser.getErrorMsg().isEmpty()) {
//                System.out.println("错误信息:");
//                System.out.println(parser.getErrorMsg());
//            }
//
//            System.out.println("解析树:");
//            System.out.println(parser.getParseTree());
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}