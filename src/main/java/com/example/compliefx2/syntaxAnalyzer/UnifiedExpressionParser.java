package com.example.compliefx2.syntaxAnalyzer;
import com.example.compliefx2.ASTNode;
import com.example.compliefx2.ManualLexer;
import com.example.compliefx2.Token;
import com.example.compliefx2.TokenLibrary;
import com.example.compliefx2.intermediateCode.IntermediateCodeGenerator;
import com.example.compliefx2.semanticAnalyzer.SemanticAnalyzer;


import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;



/**
 * 统一表达式解析器 - 支持AST构建
 *
 * 语法规则:
 * S → E
 * E → A E'
 * E' → "=" A | ε
 * O → L O'
 * O' → "||" L O' | ε
 * L → C L'
 * L' → "&&" C L' | ε
 * C → R C'
 * C' → ("==" | "!=") R C' | ε
 * R → T R'
 * R' → ("<" | "<=" | ">" | ">=") T R' | ε
 * T → M T'
 * T' → ("+" | "-") M T' | ε
 * M → U M'
 * M' → ("*" | "/" | "%") U M' | ε
 * U → "!" U | P
 * P → "(" S ")" | Identifier | IntegerLiteral | FloatLiteral | StringLiteral | CharLiteral | "true" | "false"
 */
public class UnifiedExpressionParser {
    public ManualLexer lexer;  // 词法分析器
    public Token currentToken; // 当前处理的Token
    public StringBuilder parseTree; // 解析树字符串（用于调试）
    public StringBuilder errorMsg;  // 错误信息
    public Map<String, Integer> tokenMap; // Token映射表
    public int indentLevel; // 用于构建解析树的缩进级别
    private SemanticAnalyzer semanticAnalyzer;
    private ASTNode astRoot; // AST根节点
    // 添加中间代码生成器（可选，因为StatementParser会有自己的实例）
    protected IntermediateCodeGenerator codeGenerator;

    // Token类型常量定义
    public int TOKEN_LOGICAL_AND;
    public int TOKEN_LOGICAL_OR;
    public int TOKEN_LOGICAL_NOT;
    public int TOKEN_LESS;
    public int TOKEN_LESS_EQUAL;
    public int TOKEN_GREATER;
    public int TOKEN_GREATER_EQUAL;
    public int TOKEN_EQUAL;
    public int TOKEN_NOT_EQUAL;
    public int TOKEN_PLUS;
    public int TOKEN_MINUS;
    public int TOKEN_MULTIPLY;
    public int TOKEN_DIVIDE;
    public int TOKEN_MOD;
    public int TOKEN_LPAREN;
    public int TOKEN_RPAREN;
    public int TOKEN_ID;
    public int TOKEN_NUMBER;
    public int TOKEN_STRING;
    public int TOKEN_FLOAT;
    public int TOKEN_CHAR;
    public int TOKEN_TRUE;
    public int TOKEN_FALSE;
    public int TOKEN_ASSIGN;

    /**
     * 构造函数 - 修改版本
     */
    public UnifiedExpressionParser(Reader reader, Map<String, Integer> tokenMap) throws IOException {
        this.lexer = new ManualLexer(reader, tokenMap);
        this.parseTree = new StringBuilder();
        this.errorMsg = new StringBuilder();
        this.tokenMap = tokenMap;
        this.indentLevel = 0;
        this.semanticAnalyzer = new SemanticAnalyzer();
        this.codeGenerator = new IntermediateCodeGenerator();
        initTokenTypes(tokenMap);
        advance();
    }
    /**
     * 获取中间代码生成器
     */
    public IntermediateCodeGenerator getCodeGenerator() {
        return codeGenerator;
    }

    /**
     * 设置中间代码生成器（用于StatementParser传递其生成器实例）
     */
    public void setCodeGenerator(IntermediateCodeGenerator codeGenerator) {
        this.codeGenerator = codeGenerator;
    }

    /**
     * 初始化Token类型常量
     */
    public void initTokenTypes(Map<String, Integer> tokenMap) {
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
    }

    /**
     * 前进到下一个Token
     */
    public void advance() throws IOException {
        currentToken = lexer.nextToken();
        if (currentToken != null) {
            System.out.println("当前Token: 类型=" + currentToken.type + ", 值=" + currentToken.value);
        }
    }

    /**
     * 检查当前Token是否匹配预期类型
     */
    public boolean match(int tokenType) {
        return currentToken != null && currentToken.type == tokenType;
    }

    /**
     * 添加错误信息
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
     */
    public boolean parse() throws IOException {
        try {
            astRoot = S(); // 从S开始解析，保存AST根节点
            // 检查是否所有Token都已处理完毕
            if (currentToken != null && currentToken.type != -1 && currentToken.type != 0) {
                error("输入结束后存在额外的Token: " + currentToken.value);
                return false;
            }
            return errorMsg.length() == 0;
        } catch (Exception e) {
            error("解析过程发生异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取AST根节点
     */
    public ASTNode getAST() {
        return astRoot;
    }

    /**
     * 添加一行到解析树（调试用）
     */
    public void addToParseTree(String text) {
        StringBuilder spaces = new StringBuilder();
        for (int i = 0; i < indentLevel; i++) {
            spaces.append("  ");
        }
        parseTree.append(spaces.toString()).append(text).append("\n");
    }

    /**
     * 获取解析树字符串
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
     * S → E
     */
    public ASTNode S() throws IOException {
        indentLevel++;
        try {
            addToParseTree("S → E");
            ASTNode expr = E();
            this.astRoot = expr;
            return expr;
        } finally {
            indentLevel--;
        }
    }

    /**
     * E → A E'
     */
    public ASTNode E() throws IOException {
        indentLevel++;
        try {
            addToParseTree("E → A E'");
            ASTNode left = A();
            return EPrime(left);
        } finally {
            indentLevel--;
        }
    }

    /**
     * E' → "=" A | ε
     */
    public ASTNode EPrime(ASTNode left) throws IOException {
        indentLevel++;
        try {
            if (match(TOKEN_ASSIGN)) {
                // 检查左边是否为标识符
                if (left == null || left.getType() != ASTNode.NodeType.IDENTIFIER) {
                    error("赋值操作的左边必须是标识符");
                    return left;
                }
                addToParseTree("E' → \"=\" A");
                addToParseTree("匹配: =");

                int line = currentToken.getLineNumber();
                int col = currentToken.getColumnNumber();
                advance(); // 消耗=运算符

                ASTNode right = A();
                return ASTNode.createAssignment(left, right, line, col);
            } else {
                addToParseTree("E' → ε");
                return left;
            }
        } finally {
            indentLevel--;
        }
    }

    /**
     * A → O
     */
    public ASTNode A() throws IOException {
        indentLevel++;
        try {
            addToParseTree("A → O");
            return O();
        } finally {
            indentLevel--;
        }
    }

    /**
     * O → L O'
     */
    public ASTNode O() throws IOException {
        indentLevel++;
        try {
            addToParseTree("O → L O'");
            ASTNode left = L();
            return OPrime(left);
        } finally {
            indentLevel--;
        }
    }

    /**
     * O' → "||" L O' | ε
     */
    public ASTNode OPrime(ASTNode left) throws IOException {
        indentLevel++;
        try {
            if (match(TOKEN_LOGICAL_OR)) {
                addToParseTree("O' → \"||\" L O'");
                addToParseTree("匹配: ||");

                String op = currentToken.value;
                int line = currentToken.getLineNumber();
                int col = currentToken.getColumnNumber();
                advance(); // 消耗||运算符

                ASTNode right = L();
                ASTNode binaryOp = ASTNode.createBinaryOp(op, left, right, line, col);
                return OPrime(binaryOp); // 递归处理更多||运算
            } else {
                addToParseTree("O' → ε");
                return left;
            }
        } finally {
            indentLevel--;
        }
    }

    /**
     * L → C L'
     */
    public ASTNode L() throws IOException {
        indentLevel++;
        try {
            addToParseTree("L → C L'");
            ASTNode left = C();
            return LPrime(left);
        } finally {
            indentLevel--;
        }
    }

    /**
     * L' → "&&" C L' | ε
     */
    public ASTNode LPrime(ASTNode left) throws IOException {
        indentLevel++;
        try {
            if (match(TOKEN_LOGICAL_AND)) {
                addToParseTree("L' → \"&&\" C L'");
                addToParseTree("匹配: &&");

                String op = currentToken.value;
                int line = currentToken.getLineNumber();
                int col = currentToken.getColumnNumber();
                advance(); // 消耗&&运算符

                ASTNode right = C();
                ASTNode binaryOp = ASTNode.createBinaryOp(op, left, right, line, col);
                return LPrime(binaryOp); // 递归处理更多&&运算
            } else {
                addToParseTree("L' → ε");
                return left;
            }
        } finally {
            indentLevel--;
        }
    }

    /**
     * C → R C'
     */
    public ASTNode C() throws IOException {
        indentLevel++;
        try {
            addToParseTree("C → R C'");
            ASTNode left = R();
            return CPrime(left);
        } finally {
            indentLevel--;
        }
    }

    /**
     * C' → ("==" | "!=") R C' | ε
     */
    public ASTNode CPrime(ASTNode left) throws IOException {
        indentLevel++;
        try {
            if (match(TOKEN_EQUAL) || match(TOKEN_NOT_EQUAL)) {
                String op = currentToken.value;
                addToParseTree("C' → \"" + op + "\" R C'");
                addToParseTree("匹配: " + op);

                int line = currentToken.getLineNumber();
                int col = currentToken.getColumnNumber();
                advance(); // 消耗等值比较运算符

                ASTNode right = R();
                ASTNode binaryOp = ASTNode.createBinaryOp(op, left, right, line, col);
                return CPrime(binaryOp); // 递归处理更多等值比较
            } else {
                addToParseTree("C' → ε");
                return left;
            }
        } finally {
            indentLevel--;
        }
    }

    /**
     * R → T R'
     */
    public ASTNode R() throws IOException {
        indentLevel++;
        try {
            addToParseTree("R → T R'");
            ASTNode left = T();
            return RPrime(left);
        } finally {
            indentLevel--;
        }
    }

    /**
     * R' → ("<" | "<=" | ">" | ">=") T R' | ε
     */
    public ASTNode RPrime(ASTNode left) throws IOException {
        indentLevel++;
        try {
            if (match(TOKEN_LESS) || match(TOKEN_LESS_EQUAL) ||
                    match(TOKEN_GREATER) || match(TOKEN_GREATER_EQUAL)) {
                String op = currentToken.value;
                addToParseTree("R' → \"" + op + "\" T R'");
                addToParseTree("匹配: " + op);

                int line = currentToken.getLineNumber();
                int col = currentToken.getColumnNumber();
                advance(); // 消耗关系比较运算符

                ASTNode right = T();
                ASTNode binaryOp = ASTNode.createBinaryOp(op, left, right, line, col);
                return RPrime(binaryOp); // 递归处理更多关系比较
            } else {
                addToParseTree("R' → ε");
                return left;
            }
        } finally {
            indentLevel--;
        }
    }

    /**
     * T → M T'
     */
    public ASTNode T() throws IOException {
        indentLevel++;
        try {
            addToParseTree("T → M T'");
            ASTNode left = M();
            return TPrime(left);
        } finally {
            indentLevel--;
        }
    }

    /**
     * T' → ("+" | "-") M T' | ε
     */
    public ASTNode TPrime(ASTNode left) throws IOException {
        indentLevel++;
        try {
            if (match(TOKEN_PLUS) || match(TOKEN_MINUS)) {
                String op = currentToken.value;
                addToParseTree("T' → \"" + op + "\" M T'");
                addToParseTree("匹配: " + op);

                int line = currentToken.getLineNumber();
                int col = currentToken.getColumnNumber();
                advance(); // 消耗加减运算符

                ASTNode right = M();
                ASTNode binaryOp = ASTNode.createBinaryOp(op, left, right, line, col);
                return TPrime(binaryOp); // 递归处理更多加减运算
            } else {
                addToParseTree("T' → ε");
                return left;
            }
        } finally {
            indentLevel--;
        }
    }

    /**
     * M → U M'
     */
    public ASTNode M() throws IOException {
        indentLevel++;
        try {
            addToParseTree("M → U M'");
            ASTNode left = U();
            return MPrime(left);
        } finally {
            indentLevel--;
        }
    }

    /**
     * M' → ("*" | "/" | "%") U M' | ε
     */
    public ASTNode MPrime(ASTNode left) throws IOException {
        indentLevel++;
        try {
            if (match(TOKEN_MULTIPLY) || match(TOKEN_DIVIDE) || match(TOKEN_MOD)) {
                String op = currentToken.value;
                addToParseTree("M' → \"" + op + "\" U M'");
                addToParseTree("匹配: " + op);

                int line = currentToken.getLineNumber();
                int col = currentToken.getColumnNumber();
                advance(); // 消耗乘除模运算符

                ASTNode right = U();
                ASTNode binaryOp = ASTNode.createBinaryOp(op, left, right, line, col);
                return MPrime(binaryOp); // 递归处理更多乘除模运算
            } else {
                addToParseTree("M' → ε");
                return left;
            }
        } finally {
            indentLevel--;
        }
    }

    /**
     * U → "!" U | P
     */
    public ASTNode U() throws IOException {
        indentLevel++;
        try {
            if (match(TOKEN_LOGICAL_NOT)) {
                addToParseTree("U → \"!\" U");
                addToParseTree("匹配: !");

                String op = currentToken.value;
                int line = currentToken.getLineNumber();
                int col = currentToken.getColumnNumber();
                advance(); // 消耗!运算符

                ASTNode operand = U(); // 递归解析操作数
                return ASTNode.createUnaryOp(op, operand, line, col);
            } else {
                addToParseTree("U → P");
                return P();
            }
        } finally {
            indentLevel--;
        }
    }

    /**
     * P → "(" S ")" | Identifier | IntegerLiteral | FloatLiteral | StringLiteral | CharLiteral | "true" | "false"
     */
    public ASTNode P() throws IOException {
        indentLevel++;
        try {
            if (match(TOKEN_LPAREN)) {
                addToParseTree("P → \"(\" S \")\"");
                addToParseTree("匹配: (");
                advance(); // 消耗左括号

                ASTNode expr = S(); // 解析括号内的表达式

                if (match(TOKEN_RPAREN)) {
                    addToParseTree("匹配: )");
                    advance(); // 消耗右括号
                    return expr; // 返回括号内的表达式
                } else {
                    error("期望 ')'");
                    return expr; // 即使缺少右括号，也返回表达式
                }
            } else if (match(TOKEN_ID)) {
                String name = currentToken.value;
                int line = currentToken.getLineNumber();
                int col = currentToken.getColumnNumber();
                addToParseTree("P → 标识符 (" + name + ")");
                advance();
                return ASTNode.createIdentifier(name, line, col);
            } else if (match(TOKEN_NUMBER)) {
                String value = currentToken.value;
                int line = currentToken.getLineNumber();
                int col = currentToken.getColumnNumber();
                addToParseTree("P → 整型 (" + value + ")");
                advance();
                return ASTNode.createIntLiteral(value, line, col);
            } else if (match(TOKEN_FLOAT)) {
                String value = currentToken.value;
                int line = currentToken.getLineNumber();
                int col = currentToken.getColumnNumber();
                addToParseTree("P → 浮点数 (" + value + ")");
                advance();
                return ASTNode.createFloatLiteral(value, line, col);
            } else if (match(TOKEN_STRING)) {
                String value = currentToken.value;
                int line = currentToken.getLineNumber();
                int col = currentToken.getColumnNumber();
                addToParseTree("P → 字符串 (" + value + ")");
                advance();
                return ASTNode.createStringLiteral(value, line, col);
            } else if (match(TOKEN_CHAR)) {
                String value = currentToken.value;
                int line = currentToken.getLineNumber();
                int col = currentToken.getColumnNumber();
                addToParseTree("P → 字符 (" + value + ")");
                advance();
                return ASTNode.createCharLiteral(value, line, col);
            } else if (match(TOKEN_TRUE)) {
                int line = currentToken.getLineNumber();
                int col = currentToken.getColumnNumber();
                addToParseTree("P → \"true\"");
                advance();
                return ASTNode.createBoolLiteral("true", line, col);
            } else if (match(TOKEN_FALSE)) {
                int line = currentToken.getLineNumber();
                int col = currentToken.getColumnNumber();
                addToParseTree("P → \"false\"");
                advance();
                return ASTNode.createBoolLiteral("false", line, col);
            } else {
                error("期望一个因子 (括号表达式、标识符、数字字面量、字符串字面量、字符字面量、true或false)");
                return null;
            }
        } finally {
            indentLevel--;
        }
    }

    /**
     * 主函数用于测试
     */
    /**
     * 主函数 - 修改版本，展示表达式的中间代码生成
     */
    public static void main(String[] args) {
        String[] testExpressions = {
                "3 + 4 * (5 - 2)",
                "result = a + b * c / d",
                "(status == \"active\") && (count > 0 || retries < 3)",
                "true",
                "!!flag",
                "x > 0 && y < 10"
        };

        Map<String, Integer> tokenMap = TokenLibrary.readToken("C:\\Users\\WYR\\Desktop\\编译原理\\compliefx2\\src\\main\\resources\\com\\example\\compliefx2\\tokenTable.json");

        for (String expression : testExpressions) {
            try {
                System.out.println("\n" + "=".repeat(50));
                System.out.println("测试表达式: " + expression);
                System.out.println("=".repeat(50));

                UnifiedExpressionParser parser = new UnifiedExpressionParser(new StringReader(expression), tokenMap);
                boolean isValid = parser.parse();

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

                    // 生成并输出中间代码
                    if (isValid) {
                        System.out.println("\n表达式中间代码:");
                        parser.getCodeGenerator().generateCode(ast);
                        parser.getCodeGenerator().printQuadruples();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}