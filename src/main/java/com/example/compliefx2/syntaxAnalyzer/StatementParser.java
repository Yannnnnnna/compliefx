package com.example.compliefx2.syntaxAnalyzer;

import com.example.compliefx2.*;
import com.example.compliefx2.intermediateCode.IntermediateCodeGenerator;
import com.example.compliefx2.intermediateCode.Quadruple;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;

/**
 *
 * 文法规则：
 * 程序 → 函数声明列表
 * 函数声明列表 → 函数声明 函数声明列表 | ε
 * 函数声明 → 类型说明符 标识符 ( 形参列表 ) { 语句列表 }
 * 形参列表 → 形参 , 形参列表 | 形参 | ε
 * 形参 → 类型说明符 标识符
 * 类型说明符 → int | float | void | char | string
 * 语句列表 → 语句 语句列表 | 空
 * 语句 → 表达式语句 | 复合语句 | 条件语句 | 声明语句 | while语句 | for语句
 * 表达式语句 → 表达式 ; | ;
 * 复合语句 → { 语句列表 }
 * 条件语句 → if ( 表达式 ) 语句 else部分
 * else部分 → else 语句 | 空
 * 声明语句 → 类型说明符 标识符 = 表达式 ;
 * while语句 → while ( 表达式 ) 语句
 * for语句 → for ( ForInit 表达式语句 表达式 ) 语句
 * ForInit → 声明语句 | 表达式语句
 */
public class StatementParser extends UnifiedExpressionParser {

    // 类型说明符Token
    private int TOKEN_INT;    // int
    private int TOKEN_FLOAT;  // float
    private int TOKEN_VOID;   // void
    private int TOKEN_CHAR;   // char
    private int TOKEN_STRING; // string
    private int TOKEN_BOOL;   // boolean

    // 语句相关Token
    private int TOKEN_IF;     // if
    private int TOKEN_ELSE;   // else
    private int TOKEN_WHILE;  // while
    private int TOKEN_FOR;    // for
    private int TOKEN_RETURN; // return

    // 分隔符Token
    private int TOKEN_SEMICOLON; // ;
    private int TOKEN_LBRACE;    // {
    private int TOKEN_RBRACE;    // }
    private int TOKEN_COMMA;    // ,

    // 添加当前AST节点
    private ASTNode currentAST;
    // 添加中间代码生成器
    private IntermediateCodeGenerator codeGenerator;


    /**
     * 构造函数 - 修改版本
     */
    public StatementParser(Reader reader, Map<String, Integer> tokenMap) throws IOException {
        super(reader, tokenMap);
        initStatementTokenTypes(tokenMap);
        // 初始化中间代码生成器
        this.codeGenerator = new IntermediateCodeGenerator();
    }
    /**
     * 获取中间代码生成器
     */
    public IntermediateCodeGenerator getCodeGenerator() {
        return codeGenerator;
    }

    /**
     * 获取生成的AST
     */
    public ASTNode getAST() {
        return currentAST;
    }

    /**
     * 初始化语句相关Token类型常量
     */
    private void initStatementTokenTypes(Map<String, Integer> tokenMap) {
        // 类型说明符
        TOKEN_INT = tokenMap.getOrDefault("int", 102);
        TOKEN_FLOAT = tokenMap.getOrDefault("float", 103);
        TOKEN_VOID = tokenMap.getOrDefault("void", 107);
        TOKEN_CHAR = tokenMap.getOrDefault("char", 101);
        TOKEN_STRING = tokenMap.getOrDefault("string", 114);
        TOKEN_BOOL = tokenMap.getOrDefault("bool", 117);

        // 语句关键字
        TOKEN_IF = tokenMap.getOrDefault("if", 111);
        TOKEN_ELSE = tokenMap.getOrDefault("else", 112);
        TOKEN_WHILE = tokenMap.getOrDefault("while", 110);
        TOKEN_FOR = tokenMap.getOrDefault("for", 113);
        TOKEN_RETURN = tokenMap.getOrDefault("return", 106);

        // 分隔符
        TOKEN_SEMICOLON = tokenMap.getOrDefault(";", 303);
        TOKEN_LBRACE = tokenMap.getOrDefault("{", 301);
        TOKEN_RBRACE = tokenMap.getOrDefault("}", 302);
        TOKEN_COMMA = tokenMap.getOrDefault(",", 304);

    }

    /**
     * 解析程序 - 修改版本，添加中间代码生成
     */
    public boolean parseProgram() throws IOException {
        try {
            addToParseTree("程序 → 函数声明列表");
            currentAST = new ASTNode(ASTNode.NodeType.PROGRAM);
            parseFunctionDeclarationList(currentAST);

            if (currentToken != null && currentToken.type != -1 && currentToken.type != 0) {
                error("程序结束后存在额外的Token: " + currentToken.value);
                return false;
            }



            return getErrorMsg().isEmpty();
        } catch (Exception e) {
            error("解析程序时发生异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 解析函数声明列表
     * 函数声明列表 → 函数声明 函数声明列表 | ε
     * @throws IOException 如果IO操作失败
     */
    private void parseFunctionDeclarationList(ASTNode parent) throws IOException {
        indentLevel++;
        try {
            addToParseTree("函数声明列表 → 函数声明 函数声明列表 | ε");

            while (isTypeSpecifier()) {
                addToParseTree("函数声明列表 → 函数声明 函数声明列表");
                ASTNode functionNode = parseFunctionDeclaration();
                if (functionNode != null) {
                    parent.addChild(functionNode);
                }
            }

            addToParseTree("函数声明列表 → 空");
        } finally {
            indentLevel--;
        }
    }
    /**
     * 判断当前Token是否是类型说明符
     * @return 如果是类型说明符则返回true，否则返回false
     */
    private boolean isTypeSpecifier() {
        return match(TOKEN_INT) || match(TOKEN_FLOAT) || match(TOKEN_VOID) ||
                match(TOKEN_CHAR) || match(TOKEN_STRING) || match(TOKEN_BOOL);
    }

    /**
     * 解析类型说明符
     * 类型说明符 → int | float | void | char | string |bool
     * @throws IOException 如果IO操作失败
     */
    private String parseTypeSpecifier() throws IOException {
        indentLevel++;
        try {
            if (match(TOKEN_INT) || match(TOKEN_FLOAT) || match(TOKEN_VOID) ||
                    match(TOKEN_CHAR) || match(TOKEN_STRING) || match(TOKEN_BOOL)) {
                String type = currentToken.value;
                addToParseTree("类型说明符 → " + type);
                addToParseTree("匹配类型说明符: " + type);
                advance();
                return type;
            } else {
                error("期望类型说明符 (int, float, void, char, string, bool)");
                return null;
            }
        } finally {
            indentLevel--;
        }
    }
    /**
     * 解析函数声明
     * 函数声明 → 类型说明符 标识符 ( 形参列表 ) { 语句列表 }
     * @throws IOException 如果IO操作失败
     */
    private ASTNode parseFunctionDeclaration() throws IOException {
        indentLevel++;
        try {
            addToParseTree("函数声明 → 类型说明符 标识符 ( 形参列表 ) { 语句列表 }");

            ASTNode functionNode = new ASTNode(ASTNode.NodeType.FUNCTION_DEF);

            // 解析返回类型
            String returnType = parseTypeSpecifier();
            if (returnType != null) {
                functionNode.setValue(returnType);
            }

            // 解析函数名标识符
            if (match(TOKEN_ID)) {
                ASTNode nameNode = ASTNode.createIdentifier(currentToken.value,
                        currentToken.getLineNumber(), currentToken.getColumnNumber());
                functionNode.addChild(nameNode);
                addToParseTree("匹配函数名: " + currentToken.value);
                advance();
            } else {
                error("期望函数名标识符");
                return null;
            }

            // 解析左括号
            if (match(TOKEN_LPAREN)) {
                addToParseTree("匹配左括号: (");
                advance();
            } else {
                error("期望 '('");
                return null;
            }

            // 解析形参列表
            ASTNode paramListNode = parseParameterList();
            if (paramListNode != null) {
                functionNode.addChild(paramListNode);
            }

            // 解析右括号
            if (match(TOKEN_RPAREN)) {
                addToParseTree("匹配右括号: )");
                advance();
            } else {
                error("期望 ')'");
                return null;
            }

            // 解析左大括号
            if (match(TOKEN_LBRACE)) {
                addToParseTree("匹配左大括号: {");
                advance();
            } else {
                error("期望 '{'");
                return null;
            }

            // 解析语句列表
            ASTNode bodyNode = ASTNode.createBlockStmt();
            parseStatementList(bodyNode);
            functionNode.addChild(bodyNode);

            // 解析右大括号
            if (match(TOKEN_RBRACE)) {
                addToParseTree("匹配右大括号: }");
                advance();
            } else {
                error("期望 '}'");
            }

            return functionNode;
        } finally {
            indentLevel--;
        }
    }

    /**
     * 解析形参列表
     * 形参列表 → 形参 , 形参列表 | 形参 | ε
     * @throws IOException 如果IO操作失败
     */
    private ASTNode parseParameterList() throws IOException {
        indentLevel++;
        try {
            ASTNode paramListNode = new ASTNode(ASTNode.NodeType.PARAMETER_LIST);

            if (isTypeSpecifier()) {
                addToParseTree("形参列表 → 形参列表的内容");
                ASTNode param = parseParameter();
                if (param != null) {
                    paramListNode.addChild(param);
                }

                while (match(TOKEN_COMMA)) {
                    addToParseTree("匹配逗号: ,");
                    advance();
                    param = parseParameter();
                    if (param != null) {
                        paramListNode.addChild(param);
                    }
                }
            } else {
                addToParseTree("形参列表 → 空");
            }

            return paramListNode;
        } finally {
            indentLevel--;
        }
    }
    /**
     * 解析单个形参
     * 形参 → 类型说明符 标识符
     * @throws IOException 如果IO操作失败
     */
    private ASTNode parseParameter() throws IOException {
        indentLevel++;
        try {
            addToParseTree("形参 → 类型说明符 标识符");

            ASTNode paramNode = new ASTNode(ASTNode.NodeType.DECLARATION_STMT);

            // 解析类型说明符
            String type = parseTypeSpecifier();
            if (type != null) {
                paramNode.setValue(type);
            }

            // 解析参数名标识符
            if (match(TOKEN_ID)) {
                ASTNode nameNode = ASTNode.createIdentifier(currentToken.value,
                        currentToken.getLineNumber(), currentToken.getColumnNumber());
                paramNode.addChild(nameNode);
                addToParseTree("匹配参数名: " + currentToken.value);
                advance();
            } else {
                error("期望参数名标识符");
                return null;
            }

            return paramNode;
        } finally {
            indentLevel--;
        }
    }

    /**
     * 解析语句列表
     * 语句列表 → 语句 语句列表 | 空
     * @throws IOException 如果IO操作失败
     */
    private void parseStatementList(ASTNode parent) throws IOException {
        indentLevel++;
        try {
            addToParseTree("语句列表 → 语句 语句列表 | 空");

            while (isStatementStart()) {
                addToParseTree("语句列表 → 语句 语句列表");
                ASTNode stmtNode = parseStatement();
                if (stmtNode != null) {
                    parent.addChild(stmtNode);
                }
            }

            if (match(TOKEN_RBRACE) || currentToken == null || currentToken.type == -1) {
                addToParseTree("语句列表 → 空");
            } else {
                error("无效的语句开始: " + (currentToken != null ? currentToken.value : "null"));
            }
        } finally {
            indentLevel--;
        }
    }

    /**
     * 判断当前Token是否是语句的开始
     * @return 如果是语句开始则返回true，否则返回false
     */
    private boolean isStatementStart() {
        // 需要确保检查所有可能作为语句开始的token类型
        return match(TOKEN_ID) || match(TOKEN_NUMBER) || match(TOKEN_LPAREN) ||
                match(TOKEN_TRUE) || match(TOKEN_FALSE) || match(TOKEN_LBRACE) ||
                match(TOKEN_IF) || match(TOKEN_WHILE) || match(TOKEN_FOR) || match(TOKEN_RETURN) ||
                match(TOKEN_INT) || match(TOKEN_FLOAT) || match(TOKEN_VOID) || match(TOKEN_CHAR) || match(TOKEN_STRING) ||
                match(TOKEN_SEMICOLON) || match(TOKEN_BOOL); // 空语句也是有效的语句开始
    }

    /**
     * 修改语句解析函数，添加函数调用语句解析
     * 语句 → 表达式语句 | 复合语句 | 条件语句 | 声明语句 | while语句 | for语句 | 函数调用语句
     * @throws IOException 如果IO操作失败
     */
    private ASTNode parseStatement() throws IOException {
        indentLevel++;
        try {
            if (match(TOKEN_LBRACE)) {
                addToParseTree("语句 → 复合语句");
                return parseCompoundStatement();
            } else if (match(TOKEN_IF)) {
                addToParseTree("语句 → 条件语句");
                return parseIfStatement();
            } else if (match(TOKEN_WHILE)) {
                addToParseTree("语句 → while语句");
                return parseWhileStatement();
            } else if (match(TOKEN_FOR)) {
                addToParseTree("语句 → for语句");
                return parseForStatement();
            } else if (match(TOKEN_INT) || match(TOKEN_FLOAT) || match(TOKEN_VOID) ||
                    match(TOKEN_CHAR) || match(TOKEN_STRING) || match(TOKEN_BOOL)) {
                addToParseTree("语句 → 声明语句");
                return parseDeclaration();
            } else {
                addToParseTree("语句 → 表达式语句");
                return parseExpressionStatement();
            }
        } finally {
            indentLevel--;
        }
    }
    /**
     * 解析函数调用语句
     * 函数调用语句 → 标识符 ( 实参列表 ) ;
     * @throws IOException 如果IO操作失败
     */
    private void parseFunctionCall() throws IOException {
        indentLevel++;
        try {
            addToParseTree("函数调用语句 → 标识符 ( 实参列表 ) ;");

            // 解析函数名标识符
            if (match(TOKEN_ID)) {
                addToParseTree("匹配函数名: " + currentToken.value);
                advance();
            } else {
                error("期望函数名标识符");
                return;
            }

            // 解析左括号
            if (match(TOKEN_LPAREN)) {
                addToParseTree("匹配左括号: (");
                advance();
            } else {
                error("期望 '('");
                return;
            }

            // 解析实参列表
            parseArgumentList();

            // 解析右括号
            if (match(TOKEN_RPAREN)) {
                addToParseTree("匹配右括号: )");
                advance();
            } else {
                error("期望 ')'");
                return;
            }

            // 解析分号
            if (match(TOKEN_SEMICOLON)) {
                addToParseTree("匹配分号: ;");
                advance();
            } else {
                error("期望 ';' 分号");
            }
        } finally {
            indentLevel--;
        }
    }
    /**
     * 解析实参列表
     * 实参列表 → 表达式 , 实参列表 | 表达式 | ε
     * @throws IOException 如果IO操作失败
     */
    private void parseArgumentList() throws IOException {
        indentLevel++;
        try {
            // 检查是否可能是表达式的开始
            if (isStatementStart()) {
                addToParseTree("实参列表 → 表达式 ...");
                S();  // 解析第一个实参表达式

                // 检查是否有更多实参
                while (match(TOKEN_COMMA)) {
                    addToParseTree("匹配逗号: ,");
                    advance();
                    S();  // 解析下一个实参表达式
                }
            } else {
                // 如果不是表达式的开始，则表示实参列表为空
                addToParseTree("实参列表 → 空");
            }
        } finally {
            indentLevel--;
        }
    }

    /**
     * 解析表达式语句
     * 表达式语句 → 表达式 ; | ;
     * @throws IOException 如果IO操作失败
     */
    private ASTNode parseExpressionStatement() throws IOException {
        indentLevel++;
        try {
            if (match(TOKEN_SEMICOLON)) {
                advance();
                return ASTNode.createExpressionStmt(null); // 空语句
            } else {
                // ✅ 直接调用父类的S()解析表达式，并获取生成的AST
                ASTNode exprAST = S(); // 解析表达式
                if (exprAST == null) {
                    error("无效的表达式");
                    return null;
                }
                if (match(TOKEN_SEMICOLON)) {
                    advance();
                } else {
                    error("期望 ';' 分号");
                }
                // ✅ 使用解析得到的exprAST构建表达式语句
                return ASTNode.createExpressionStmt(exprAST);
            }
        } finally {
            indentLevel--;
        }
    }

    /**
     * 解析复合语句
     * 复合语句 → { 语句列表 }
     * @throws IOException 如果IO操作失败
     */
    private ASTNode parseCompoundStatement() throws IOException {
        indentLevel++;
        try {
            addToParseTree("复合语句 → { 语句列表 }");

            if (match(TOKEN_LBRACE)) {
                addToParseTree("匹配左大括号: {");
                advance();
            } else {
                error("期望 '{'");
                return null;
            }

            ASTNode blockNode = ASTNode.createBlockStmt();
            parseStatementList(blockNode);

            if (match(TOKEN_RBRACE)) {
                addToParseTree("匹配右大括号: }");
                advance();
            } else {
                error("期望 '}'");
            }

            return blockNode;
        } finally {
            indentLevel--;
        }
    }

    /**
     * 解析If语句
     * 条件语句 → if ( 表达式 ) 语句 else部分
     * @throws IOException 如果IO操作失败
     */
    private ASTNode parseIfStatement() throws IOException {
        indentLevel++;
        try {
            addToParseTree("条件语句 → if ( 表达式 ) 语句 else部分");

            if (match(TOKEN_IF)) {
                addToParseTree("匹配条件关键字: if");
                advance();
            } else {
                error("期望 'if'");
                return null;
            }

            if (match(TOKEN_LPAREN)) {
                addToParseTree("匹配左括号: (");
                advance();
            } else {
                error("期望 '('");
                return null;
            }

//            // 解析条件表达式
//            S();
//            ASTNode conditionNode = super.getAST();
            // 解析条件表达式
            ASTNode conditionNode = S();

            if (match(TOKEN_RPAREN)) {
                addToParseTree("匹配右括号: )");
                advance();
            } else {
                error("期望 ')'");
                return null;
            }

            // 解析then语句
            ASTNode thenStmt = parseStatement();

            // 解析else部分
            ASTNode elseStmt = parseElsePart();

            return ASTNode.createIfStmt(conditionNode, thenStmt, elseStmt);
        } finally {
            indentLevel--;
        }
    }

    /**
     * 解析Else部分
     * else部分 → else 语句 | 空
     * @throws IOException 如果IO操作失败
     */
    private ASTNode parseElsePart() throws IOException {
        indentLevel++;
        try {
            if (match(TOKEN_ELSE)) {
                addToParseTree("else部分 → else 语句");
                addToParseTree("匹配else关键字: else");
                advance();
                return parseStatement();
            } else {
                addToParseTree("else部分 → 空");
                return null;
            }
        } finally {
            indentLevel--;
        }
    }

    /**
     * 解析声明语句
     * 声明语句 → 类型说明符 标识符 = 表达式 ;
     * @throws IOException 如果IO操作失败
     */

    private ASTNode parseDeclaration() throws IOException {
        indentLevel++;
        try {
            addToParseTree("声明语句 → 类型说明符 标识符 = 表达式 ;");

            ASTNode declNode = new ASTNode(ASTNode.NodeType.DECLARATION_STMT);

            // 解析类型说明符
            String type = parseTypeSpecifier();
            if (type != null) {
                declNode.setValue(type);
            }

            // 解析标识符
            if (match(TOKEN_ID)) {
                ASTNode idNode = ASTNode.createIdentifier(currentToken.value,
                        currentToken.getLineNumber(), currentToken.getColumnNumber());
                declNode.addChild(idNode);
                addToParseTree("匹配标识符: " + currentToken.value);
                advance();
            } else {
                error("期望标识符");
                return null;
            }

            // 处理可选的初始化部分
            if (match(TOKEN_ASSIGN)) {
                addToParseTree("匹配赋值符号: =");
                advance();
                // 修复：只调用一次S()方法并保存结果
                ASTNode initExpr = S(); // 解析初始化表达式
                if (initExpr != null) {
                    declNode.addChild(initExpr);
                }
            }

            if (match(TOKEN_SEMICOLON)) {
                addToParseTree("匹配分号: ;");
                advance();
            } else {
                error("期望 ';' 分号");
            }

            return declNode;
        } finally {
            indentLevel--;
        }
    }

    /**
     * 解析While语句
     * while语句 → while ( 表达式 ) 语句
     * @throws IOException 如果IO操作失败
     */
    private ASTNode parseWhileStatement() throws IOException {
        indentLevel++;
        try {
            addToParseTree("while语句 → while ( 表达式 ) 语句");

            if (match(TOKEN_WHILE)) {
                addToParseTree("匹配循环关键字: while");
                advance();
            } else {
                error("期望 'while'");
                return null;
            }

            if (match(TOKEN_LPAREN)) {
                addToParseTree("匹配左括号: (");
                advance();
            } else {
                error("期望 '('");
                return null;
            }

//            // 解析循环条件表达式
//            S();
//            ASTNode conditionNode = super.getAST();
            // 解析循环条件表达式
            ASTNode conditionNode = S();

            if (match(TOKEN_RPAREN)) {
                addToParseTree("匹配右括号: )");
                advance();
            } else {
                error("期望 ')'");
                return null;
            }

            // 解析while循环体中的语句
            ASTNode bodyNode = parseStatement();

            return ASTNode.createWhileStmt(conditionNode, bodyNode);
        } finally {
            indentLevel--;
        }
    }

    /**
     * 解析For语句
     * for语句 → for ( 表达式语句 表达式语句 表达式 ) 语句
     * @throws IOException 如果IO操作失败
     */
    private ASTNode parseForStatement() throws IOException {
        indentLevel++;
        try {
            addToParseTree("for语句 → for ( 表达式语句 表达式语句 表达式 ) 语句");

            if (match(TOKEN_FOR)) {
                addToParseTree("匹配循环关键字: for");
                advance();
            } else {
                error("期望 'for'");
                return null;
            }

            if (match(TOKEN_LPAREN)) {
                addToParseTree("匹配左括号: (");
                advance();
            } else {
                error("期望 '('");
                return null;
            }

            ASTNode forNode = new ASTNode(ASTNode.NodeType.FOR_STMT);

            // 解析初始化部分
            addToParseTree("解析for循环初始化表达式");
            ASTNode initNode = null;
            if (match(TOKEN_INT) || match(TOKEN_FLOAT) || match(TOKEN_VOID) ||
                    match(TOKEN_CHAR) || match(TOKEN_STRING)) {
                initNode = parseDeclaration();
            } else {
                initNode = parseExpressionStatement();
            }
            if (initNode != null) {
                forNode.addChild(initNode);
            }

            // 解析条件表达式语句
            addToParseTree("解析for循环条件表达式");
            ASTNode conditionNode = parseExpressionStatement();
            if (conditionNode != null) {
                forNode.addChild(conditionNode);
            }

//            // 解析迭代表达式
//            addToParseTree("解析for循环迭代表达式");
//            S();
//            ASTNode iterNode = super.getAST();
            // 解析迭代表达式
            addToParseTree("解析for循环迭代表达式");
            ASTNode iterNode = S();
            if (iterNode != null) {
                forNode.addChild(iterNode);
            }

            if (match(TOKEN_RPAREN)) {
                addToParseTree("匹配右括号: )");
                advance();
            } else {
                error("期望 ')'");
                return null;
            }

            // 解析for循环体
            ASTNode bodyNode = parseStatement();
            if (bodyNode != null) {
                forNode.addChild(bodyNode);
            }

            return forNode;
        } finally {
            indentLevel--;
        }
    }

    /**
     * 主函数 - 修改版本，展示中间代码生成
     */
    public static void main(String[] args) {
        String[] testPrograms = {
//                "void main() { int x = 5; }",
//                "void main() { int x = 5; int y = x + 10; }",
//                "void main() { if (x > 0) { y = x + 1; } else { y = 0; } }",
//                "void main() { while (i < 10) { i = i + 1; } }",
                "void main() { for (int i = 0; i < 10; i = i + 1) { sum = sum + i; } }",
//                "void main() { int a = 3; int b = 4; int c = a + b * 2; }"

        };

        Map<String, Integer> tokenMap = TokenLibrary.readToken("C:\\Users\\WYR\\Desktop\\编译原理\\compliefx2\\src\\main\\resources\\com\\example\\compliefx2\\tokenTable.json");

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
                    System.out.println("\n中间代码:");
                    parser.getCodeGenerator().printQuadruples();
                }

                // 输出目标代码
                if (isValid) {
                    CodeGenerator gen = new CodeGenerator();
                    String asm = gen.generateAssembly();
                    System.out.println("\n生成的汇编代码:\n" + asm);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}