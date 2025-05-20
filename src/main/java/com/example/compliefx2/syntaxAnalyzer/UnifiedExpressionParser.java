package com.example.compliefx2.syntaxAnalyzer;
import com.example.compliefx2.ManualLexer;
import com.example.compliefx2.Token;
import com.example.compliefx2.TokenLibrary;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

/**
 * 开始符号
 * S → E
 *
 * 赋值表达式
 * E → A E'
 * E' → "=" A | ε
 *
 * 逻辑或运算
 * O → L O'
 * O' → "||" L O' | ε
 *
 * 逻辑与运算
 * L → C L'
 * L' → "&&" C L' | ε
 *
 * 等值比较运算
 * C → R C'
 * C' → ("==" | "!=") R C' | ε
 *
 * 关系比较运算
 * R → T R'
 * R' → ("<" | "<=" | ">" | ">=") T R' | ε
 *
 * 加减运算
 * T → M T'
 * T' → ("+" | "-") M T' | ε
 *
 * 乘除取模运算
 * M → U M'
 * M' → ("*" | "/" | "%") U M' | ε
 *
 * 逻辑非运算
 * U → "!" U | P
 *
 * 基础表达式
 * P → "(" S ")"
 *    | 标识符(Identifier)
 *    | 整数字面量(IntegerLiteral)
 *    | 浮点数字面量(FloatLiteral)
 *    | 字符串字面量(StringLiteral)
 *    | 字符字面量(CharLiteral)
 *    | 布尔值"true"
 *    | 布尔值"false"
 */
public class UnifiedExpressionParser {
    public ManualLexer lexer;  // 词法分析器
    public Token currentToken; // 当前处理的Token
    public StringBuilder parseTree; // 解析树字符串
    public StringBuilder errorMsg;  // 错误信息
    public Map<String, Integer> tokenMap; // Token映射表
    public int indentLevel; // 用于构建解析树的缩进级别

    // Token类型常量定义
    public int TOKEN_LOGICAL_AND;  // &&
    public int TOKEN_LOGICAL_OR;   // ||
    public int TOKEN_LOGICAL_NOT;  // !
    public int TOKEN_LESS;         // <
    public int TOKEN_LESS_EQUAL;   // <=
    public int TOKEN_GREATER;      // >
    public int TOKEN_GREATER_EQUAL; // >=
    public int TOKEN_EQUAL;        // ==
    public int TOKEN_NOT_EQUAL;    // !=
    public int TOKEN_PLUS;         // +
    public int TOKEN_MINUS;        // -
    public int TOKEN_MULTIPLY;     // *
    public int TOKEN_DIVIDE;       // /
    public int TOKEN_MOD;          // %
    public int TOKEN_LPAREN;       // (
    public int TOKEN_RPAREN;       // )
    public int TOKEN_ID;           // 标识符
    public int TOKEN_NUMBER;       // 整数
    public int TOKEN_STRING;       // 字符串字面量类型
    public int TOKEN_FLOAT;        // 浮点数字面量类型
    public int TOKEN_CHAR;         // 字符
    public int TOKEN_TRUE;         // true
    public int TOKEN_FALSE;        // false
    public int TOKEN_ASSIGN;       // =

    /**
     * 构造函数
     * @param reader 输入源
     * @param tokenMap Token映射表
     * @throws IOException 如果IO操作失败
     */
    public UnifiedExpressionParser(Reader reader, Map<String, Integer> tokenMap) throws IOException {
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
    public void initTokenTypes(Map<String, Integer> tokenMap) {
        // 从映射表中获取各种Token类型
        // 使用默认值作为备选
        TOKEN_LOGICAL_AND = tokenMap.getOrDefault("&&", 217);
        TOKEN_LOGICAL_OR = tokenMap.getOrDefault("||", 218);
        TOKEN_LOGICAL_NOT = tokenMap.getOrDefault("!", 205);
        TOKEN_LESS = tokenMap.getOrDefault("<", 211);
        TOKEN_LESS_EQUAL = tokenMap.getOrDefault("<=", 212);
        TOKEN_GREATER = tokenMap.getOrDefault(">", 213);
        TOKEN_GREATER_EQUAL = tokenMap.getOrDefault(">=", 214);
        TOKEN_EQUAL = tokenMap.getOrDefault("==", 215);
        TOKEN_NOT_EQUAL = tokenMap.getOrDefault("!=", 216);
        TOKEN_PLUS = tokenMap.getOrDefault("+", 209);
        TOKEN_MINUS = tokenMap.getOrDefault("-", 210);
        TOKEN_MULTIPLY = tokenMap.getOrDefault("*", 206);
        TOKEN_DIVIDE = tokenMap.getOrDefault("/", 207);
        TOKEN_MOD = tokenMap.getOrDefault("%", 208);
        TOKEN_LPAREN = tokenMap.getOrDefault("(", 201);
        TOKEN_RPAREN = tokenMap.getOrDefault(")", 202);
        TOKEN_ID = tokenMap.getOrDefault("Identifier", 700);
        TOKEN_NUMBER = tokenMap.getOrDefault("Integer", 400);
        TOKEN_STRING = tokenMap.getOrDefault("String", 600);
        TOKEN_CHAR = tokenMap.getOrDefault("Character", 500);
        TOKEN_FLOAT = tokenMap.getOrDefault("FloatNumber", 900);
        TOKEN_TRUE = tokenMap.getOrDefault("true", 115);
        TOKEN_FALSE = tokenMap.getOrDefault("false", 116);
        TOKEN_ASSIGN = tokenMap.getOrDefault("=", 219);
//        // 调试信息输出
//        System.out.println("合并表达式解析器Token类型初始化：" +
//                "&&=" + TOKEN_LOGICAL_AND +
//                ", ||=" + TOKEN_LOGICAL_OR +
//                ", +=" + TOKEN_PLUS +
//                ", -=" + TOKEN_MINUS +
//                ", *=" + TOKEN_MULTIPLY +
//                ", /=" + TOKEN_DIVIDE +
//                ", %=" + TOKEN_MOD +
//                ", <=" + TOKEN_LESS +
//                ", >=" + TOKEN_GREATER_EQUAL +
//                ", ===" + TOKEN_EQUAL +
//                ", !==" + TOKEN_NOT_EQUAL +
//                ", ==" + TOKEN_ASSIGN);
    }

    /**
     * 前进到下一个Token
     * @throws IOException 如果IO操作失败
     */
    public void advance() throws IOException {
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
    public boolean match(int tokenType) {
        return currentToken != null && currentToken.type == tokenType;
    }

//    /**
//     * 处理给定类型的Token，如果不匹配则报错
//     * @param tokenType 预期的Token类型
//     * @param errorMessage 错误消息
//     * @throws IOException 如果IO操作失败
//     */
//    public void consume(int tokenType, String errorMessage) throws IOException {
//        if (match(tokenType)) {
//            advance();
//        } else {
//            error(errorMessage);
//        }
//    }

    /**
     * 添加错误信息
     * @param message 错误消息
     */
    public void error(String message) {
        if (currentToken != null) {
            errorMsg.append("错误(行 ").append(currentToken.getLineNumber()).append("): ");
        } else {
            errorMsg.append("错误: ");
        }
        errorMsg.append(message).append("\n");
    }

    /**
     * 解析表达式
     * @return 如果解析成功则返回true，否则返回false
     * @throws IOException 如果IO操作失败
     */
    public boolean parse() throws IOException {
        try {
            S(); // 从S开始解析
            // 检查是否所有Token都已处理完毕
            if (currentToken != null && currentToken.type != -1 && currentToken.type != 0) {
                error("输入结束后存在额外的Token: " + currentToken.value);
                return false;
            }
            // 如果没有错误，则解析成功
            return errorMsg.length() == 0;
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
    public void addToParseTree(String text) {
        StringBuilder spaces = new StringBuilder();
        for (int i = 0; i < indentLevel; i++) {
            spaces.append("  "); // 使用两个空格作为缩进
        }
        parseTree.append(spaces.toString()).append(text).append("\n");
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
     * S → E
     * 起始符号，表示整个表达式
     * @throws IOException 如果IO操作失败
     */
    public void S() throws IOException {
        indentLevel++;
        try {
            addToParseTree("S → E");
            E(); // 解析E
        } finally {
            indentLevel--;
        }
    }

    /**
     * E → A E'
     * 处理赋值表达式
     * @throws IOException 如果IO操作失败
     */
    public void E() throws IOException {
        indentLevel++;
        try {
            addToParseTree("E → A E'");
            A(); // 解析A
            EPrime(); // 解析E'
        } finally {
            indentLevel--;
        }
    }

    /**
     * E' → "=" A | ε
     * 处理可选的赋值操作
     * @throws IOException 如果IO操作失败
     */
    public void EPrime() throws IOException {
        indentLevel++;
        try {
            if (match(TOKEN_ASSIGN)) {
                addToParseTree("E' → \"=\" A");
                addToParseTree("匹配: =");
                advance(); // 消耗=运算符
                A();  // 解析A
            } else {
                addToParseTree("E' → ε");
            }
        } finally {
            indentLevel--;
        }
    }

    /**
     * A → O
     * @throws IOException 如果IO操作失败
     */
    public void A() throws IOException {
        indentLevel++;
        try {
            addToParseTree("A → O");
            O(); // 解析O
        } finally {
            indentLevel--;
        }
    }

    /**
     * O → L O'
     * 处理逻辑或运算
     * @throws IOException 如果IO操作失败
     */
    public void O() throws IOException {
        indentLevel++;
        try {
            addToParseTree("O → L O'");
            L(); // 解析L
            OPrime();  // 解析O'
        } finally {
            indentLevel--;
        }
    }

    /**
     * O' → "||" L O' | ε
     * 处理多个逻辑或运算
     * @throws IOException 如果IO操作失败
     */
    public void OPrime() throws IOException {
        indentLevel++;
        try {
            if (match(TOKEN_LOGICAL_OR)) {
                addToParseTree("O' → \"||\" L O'");
                addToParseTree("匹配: ||");
                advance(); // 消耗||运算符
                L();  // 解析L
                OPrime();  // 递归解析O'
            } else {
                addToParseTree("O' → ε");
            }
        } finally {
            indentLevel--;
        }
    }

    /**
     * L → C L'
     * 处理逻辑与运算
     * @throws IOException 如果IO操作失败
     */
    public void L() throws IOException {
        indentLevel++;
        try {
            addToParseTree("L → C L'");
            C(); // 解析C
            LPrime();  // 解析L'
        } finally {
            indentLevel--;
        }
    }

    /**
     * L' → "&&" C L' | ε
     * 处理多个逻辑与运算
     * @throws IOException 如果IO操作失败
     */
    public void LPrime() throws IOException {
        indentLevel++;
        try {
            if (match(TOKEN_LOGICAL_AND)) {
                addToParseTree("L' → \"&&\" C L'");
                addToParseTree("匹配: &&");
                advance(); // 消耗&&运算符
                C();  // 解析C
                LPrime();  // 递归解析L'
            } else {
                addToParseTree("L' → ε");
            }
        } finally {
            indentLevel--;
        }
    }

    /**
     * C → R C'
     * 处理等值比较
     * @throws IOException 如果IO操作失败
     */
    public void C() throws IOException {
        indentLevel++;
        try {
            addToParseTree("C → R C'");
            R(); // 解析R
            CPrime();  // 解析C'
        } finally {
            indentLevel--;
        }
    }

    /**
     * C' → ("==" | "!=") R C' | ε
     * 处理多个等值比较
     * @throws IOException 如果IO操作失败
     */
    public void CPrime() throws IOException {
        indentLevel++;
        try {
            if (match(TOKEN_EQUAL) || match(TOKEN_NOT_EQUAL)) {
                String op = currentToken.value;
                addToParseTree("C' → \"" + op + "\" R C'");
                addToParseTree("匹配: " + op);
                advance(); // 消耗等值比较运算符
                R();  // 解析R
                CPrime();  // 递归解析C'
            } else {
                addToParseTree("C' → ε");
            }
        } finally {
            indentLevel--;
        }
    }

    /**
     * R → T R'
     * 处理关系比较
     * @throws IOException 如果IO操作失败
     */
    public void R() throws IOException {
        indentLevel++;
        try {
            addToParseTree("R → T R'");
            T(); // 解析T
            RPrime();  // 解析R'
        } finally {
            indentLevel--;
        }
    }

    /**
     * R' → ("<" | "<=" | ">" | ">=") T R' | ε
     * 处理多个关系比较
     * @throws IOException 如果IO操作失败
     */
    public void RPrime() throws IOException {
        indentLevel++;
        try {
            if (match(TOKEN_LESS) || match(TOKEN_LESS_EQUAL) ||
                    match(TOKEN_GREATER) || match(TOKEN_GREATER_EQUAL)) {
                String op = currentToken.value;
                addToParseTree("R' → \"" + op + "\" T R'");
                addToParseTree("匹配: " + op);
                advance(); // 消耗关系比较运算符
                T();  // 解析T
                RPrime();  // 递归解析R'
            } else {
                addToParseTree("R' → ε");
            }
        } finally {
            indentLevel--;
        }
    }

    /**
     * T → M T'
     * 处理加法运算
     * @throws IOException 如果IO操作失败
     */
    public void T() throws IOException {
        indentLevel++;
        try {
            addToParseTree("T → M T'");
            M(); // 解析M
            TPrime();  // 解析T'
        } finally {
            indentLevel--;
        }
    }

    /**
     * T' → ("+" | "-") M T' | ε
     * 处理多个加法运算
     * @throws IOException 如果IO操作失败
     */
    public void TPrime() throws IOException {
        indentLevel++;
        try {
            if (match(TOKEN_PLUS) || match(TOKEN_MINUS)) {
                String op = currentToken.value;
                addToParseTree("T' → \"" + op + "\" M T'");
                addToParseTree("匹配: " + op);
                advance(); // 消耗加减运算符
                M();  // 解析M
                TPrime();  // 递归解析T'
            } else {
                addToParseTree("T' → ε");
            }
        } finally {
            indentLevel--;
        }
    }

    /**
     * M → U M'
     * 处理乘法运算
     * @throws IOException 如果IO操作失败
     */
    public void M() throws IOException {
        indentLevel++;
        try {
            addToParseTree("M → U M'");
            U(); // 解析U
            MPrime();  // 解析M'
        } finally {
            indentLevel--;
        }
    }

    /**
     * M' → ("*" | "/" | "%") U M' | ε
     * 处理多个乘法运算
     * @throws IOException 如果IO操作失败
     */
    public void MPrime() throws IOException {
        indentLevel++;
        try {
            if (match(TOKEN_MULTIPLY) || match(TOKEN_DIVIDE) || match(TOKEN_MOD)) {
                String op = currentToken.value;
                addToParseTree("M' → \"" + op + "\" U M'");
                addToParseTree("匹配: " + op);
                advance(); // 消耗乘除模运算符
                U();  // 解析U
                MPrime();  // 递归解析M'
            } else {
                addToParseTree("M' → ε");
            }
        } finally {
            indentLevel--;
        }
    }

    /**
     * U → "!" U | P
     * 处理逻辑非运算
     * @throws IOException 如果IO操作失败
     */
    public void U() throws IOException {
        indentLevel++;
        try {
            if (match(TOKEN_LOGICAL_NOT)) {
                addToParseTree("U → \"!\" U");
                addToParseTree("匹配: !");
                advance(); // 消耗!运算符
                U();  // 递归解析U
            } else {
                addToParseTree("U → P");
                P(); // 解析P
            }
        } finally {
            indentLevel--;
        }
    }

    /**
     * P → "(" S ")" | Identifier | IntegerLiteral | FloatLiteral | StringLiteral | CharLiteral | "true" | "false"
     * 处理基本表达式单元
     * @throws IOException 如果IO操作失败
     */
    public void P() throws IOException {
        indentLevel++;
        try {
            if (match(TOKEN_LPAREN)) {
                addToParseTree("P → \"(\" S \")\"");
                addToParseTree("匹配: (");
                advance(); // 消耗左括号
                S(); // 解析S
                if (match(TOKEN_RPAREN)) {
                    addToParseTree("匹配: )");
                    advance(); // 消耗右括号
                } else {
                    error("期望 ')'");
                }
            } else if (match(TOKEN_ID)) {
                addToParseTree("P → 标识符 (" + currentToken.value + ")");
                advance(); // 消耗标识符
            } else if (match(TOKEN_NUMBER)) {
                addToParseTree("P → 整型 (" + currentToken.value + ")");
                advance(); // 消耗整数字面量
            } else if (match(TOKEN_FLOAT)) {
                addToParseTree("P → 浮点数 (" + currentToken.value + ")");
                advance(); // 消耗浮点数字面量
            } else if (match(TOKEN_STRING)) {
                addToParseTree("P → 字符串 (" + currentToken.value + ")");
                advance(); // 消耗字符串字面量
            } else if (match(TOKEN_CHAR)) {
                addToParseTree("P → 字符 (" + currentToken.value + ")");
                advance(); // 消耗字符字面量
            } else if (match(TOKEN_TRUE)) {
                addToParseTree("P → \"true\"");
                advance(); // 消耗true
            } else if (match(TOKEN_FALSE)) {
                addToParseTree("P → \"false\"");
                advance(); // 消耗false
            } else {
                error("期望一个因子 (括号表达式、标识符、数字字面量、字符串字面量、字符字面量、true或false)");
            }
        } finally {
            indentLevel--;
        }
    }

    /**
     * 主函数用于测试
     */
    public static void main(String[] args) {
        // 测试表达式
        String[] testExpressions = {
                // 基本正确表达式
                "3 + 4 * (5 - 2)",
                "result = a + b * c / d",
                "(status == \"active\") && (count > 0 || retries < 3)",
                "valid = (a + b) * c >= (d - e) / f && (status == \"ready\" || count > 5)",
                "true",

                // 优先级与结合性
                "5 + 3 * 2",
                "10 - 4 - 2",
                "a = b = c = 5",
                "!!flag",

                // 错误处理
                "3 + * 2",
                "(a + b * c",
                "5 ** 2",
                "5 = x",
                "\"hello\" + 5",

                // 边界情况
                "x",
                "",
                "(((((((a)))))))",
                "result = 3.14 * radius + 'A' == \"test\" ? false : true"
        };
        Map<String, Integer> tokenMap = TokenLibrary.readToken("C:\\Users\\WYR\\Desktop\\编译原理\\compliefx2\\src\\main\\resources\\com\\example\\compliefx2\\tokenTable.json");
        for (String expression : testExpressions) {
            try {
                System.out.println("\n======= 测试表达式: " + expression + " =======");
                // 预处理：检查词法分析器能否识别所有标记
                System.out.println("预处理标记:");
                ManualLexer testLexer = new ManualLexer(new StringReader(expression), tokenMap);
                Token token;
                while ((token = testLexer.nextToken()) != null && token.type != -1) {
                    System.out.println("  " + token.type + ": " + token.value);
                }
                // 进行解析
                UnifiedExpressionParser parser = new UnifiedExpressionParser(new StringReader(expression), tokenMap);
                boolean isValid = parser.parse();
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
}