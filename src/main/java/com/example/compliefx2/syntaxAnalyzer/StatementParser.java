package com.example.compliefx2.syntaxAnalyzer;

import com.example.compliefx2.ManualLexer;
import com.example.compliefx2.Token;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

/**
 * 语句和程序结构分析器，扩展了已有的表达式解析器
 * 实现以下功能：
 * 1. 程序结构解析（Program）
 * 2. 主函数解析（void main(){}）
 * 3. 语句块解析（StatementList）
 * 4. if语句解析（IfStatement）
 * 5. 简单声明语句解析（Declaration）
 * 6. while语句解析（WhileStatement）
 * 7. for语句解析（ForStatement）
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
 *
 * 文法规则如下：
 * Program → FunctionDeclaration
 * FunctionDeclaration → "void" "main" "(" ")" "{" StatementList "}"
 * TypeSpecifier → "int" | "float" | "void" | "char" | "string"
 * StatementList → Statement StatementList | ε
 * Statement → ExpressionStatement | CompoundStatement | IfStatement | Declaration | WhileStatement | ForStatement
 * ExpressionStatement → Expression ";" | ";"
 * CompoundStatement → "{" StatementList "}"
 * IfStatement → "if" "(" Expression ")" Statement ElsePart
 * ElsePart → "else" Statement | ε
 * Declaration → TypeSpecifier Identifier ("=" Expression)? ";"
 * WhileStatement → "while" "(" Expression ")" Statement
 * ForStatement → "for" "(" ForInit ExpressionStatement Expression ")" Statement
 * ForInit → Declaration | ExpressionStatement
 */
public class StatementParser extends UnifiedExpressionParser {

    // 类型说明符Token
    private int TOKEN_INT;    // int
    private int TOKEN_FLOAT;  // float
    private int TOKEN_VOID;   // void
    private int TOKEN_CHAR;   // char
    private int TOKEN_STRING; // string

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

    /**
     * 构造函数
     * @param reader 输入源
     * @param tokenMap Token映射表
     * @throws IOException 如果IO操作失败
     */
    public StatementParser(Reader reader, Map<String, Integer> tokenMap) throws IOException {
        super(reader, tokenMap); // 调用父类构造函数
        // 初始化语句相关Token类型
        initStatementTokenTypes(tokenMap);
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

        System.out.println("语句解析器Token类型初始化：" +
                "int=" + TOKEN_INT +
                ", if=" + TOKEN_IF +
                ", else=" + TOKEN_ELSE +
                ", while=" + TOKEN_WHILE +
                ", for=" + TOKEN_FOR +
                ", {=" + TOKEN_LBRACE +
                ", }=" + TOKEN_RBRACE +
                ", ;=" + TOKEN_SEMICOLON);
    }

    /**
     * 解析程序
     * 程序 → 函数声明
     * @return 解析是否成功
     * @throws IOException 如果IO操作失败
     */
    public boolean parseProgram() throws IOException {
        try {
            addToParseTree("程序 → 函数声明列表");
            parseFunctionDeclarationList();
            // 检查是否所有Token都已处理完毕
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
    private void parseFunctionDeclarationList() throws IOException {
        indentLevel++;
        try {
            // 同样先给出整条文法提示
            addToParseTree("函数声明列表 → 函数声明 函数声明列表 | ε");

            // 只要下一个是类型说明符，就循环解析一个函数声明
            while (isTypeSpecifier()) {
                addToParseTree("函数声明列表 → 函数声明 函数声明列表");
                parseFunctionDeclaration();
            }

            // 如果一开始就不是类型说明符，或解析完所有声明后，就归到 ε
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
                match(TOKEN_CHAR) || match(TOKEN_STRING);
    }

    /**
     * 解析类型说明符
     * 类型说明符 → int | float | void | char | string
     * @throws IOException 如果IO操作失败
     */
    private void parseTypeSpecifier() throws IOException {
        indentLevel++;
        try {
            if (match(TOKEN_INT) || match(TOKEN_FLOAT) || match(TOKEN_VOID) || match(TOKEN_CHAR) || match(TOKEN_STRING)) {
                addToParseTree("类型说明符 → " + currentToken.value);
                addToParseTree("匹配类型说明符: " + currentToken.value);
                advance();
            } else {
                error("期望类型说明符 (int, float, void, char, string)");
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
    private void parseFunctionDeclaration() throws IOException {
        indentLevel++;
        try {
            addToParseTree("函数声明 → 类型说明符 标识符 ( 形参列表 ) { 语句列表 }");
            // 解析返回类型
            parseTypeSpecifier();

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

            // 解析形参列表
            parseParameterList();

            // 解析右括号
            if (match(TOKEN_RPAREN)) {
                addToParseTree("匹配右括号: )");
                advance();
            } else {
                error("期望 ')'");
                return;
            }

            // 解析左大括号
            if (match(TOKEN_LBRACE)) {
                addToParseTree("匹配左大括号: {");
                advance();
            } else {
                error("期望 '{'");
                return;
            }

            // 解析语句列表
            parseStatementList();

            // 解析右大括号
            if (match(TOKEN_RBRACE)) {
                addToParseTree("匹配右大括号: }");
                advance();
            } else {
                error("期望 '}'");
            }
        } finally {
            indentLevel--;
        }
    }

    /**
     * 解析形参列表
     * 形参列表 → 形参 , 形参列表 | 形参 | ε
     * @throws IOException 如果IO操作失败
     */
    private void parseParameterList() throws IOException {
        indentLevel++;
        try {
            // 检查是否是类型说明符，形参必须以类型说明符开始
            if (isTypeSpecifier()) {
                addToParseTree("形参列表 → 形参列表的内容");
                parseParameter();  // 解析第一个形参

                // 检查是否有更多形参
                while (match(TOKEN_COMMA)) {
                    addToParseTree("匹配逗号: ,");
                    advance();
                    parseParameter();  // 解析下一个形参
                }
            } else {
                // 如果不是类型说明符，则表示形参列表为空
                addToParseTree("形参列表 → 空");
            }
        } finally {
            indentLevel--;
        }
    }
    /**
     * 解析单个形参
     * 形参 → 类型说明符 标识符
     * @throws IOException 如果IO操作失败
     */
    private void parseParameter() throws IOException {
        indentLevel++;
        try {
            addToParseTree("形参 → 类型说明符 标识符");
            parseTypeSpecifier();  // 解析类型说明符

            // 解析参数名标识符
            if (match(TOKEN_ID)) {
                addToParseTree("匹配参数名: " + currentToken.value);
                advance();
            } else {
                error("期望参数名标识符");
            }
        } finally {
            indentLevel--;
        }
    }

    /**
     * 解析语句列表
     * 语句列表 → 语句 语句列表 | 空
     * @throws IOException 如果IO操作失败
     */
    private void parseStatementList() throws IOException {
        indentLevel++;
        try {
            // 无论是否有语句，先打出这一条分支提示
            addToParseTree("语句列表 → 语句 语句列表 | 空");

            // 只要是语句起始，就一直解析下去
            while (isStatementStart()) {
                addToParseTree("语句列表 → 语句 语句列表");
                parseStatement();
            }

            // 循环结束后，要么遇到右大括号/EOF，要么是个非法起始
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
                match(TOKEN_SEMICOLON); // 空语句也是有效的语句开始
    }

    /**
     * 修改语句解析函数，添加函数调用语句解析
     * 语句 → 表达式语句 | 复合语句 | 条件语句 | 声明语句 | while语句 | for语句 | 函数调用语句
     * @throws IOException 如果IO操作失败
     */
    private void parseStatement() throws IOException {
        indentLevel++;
        try {
            if (match(TOKEN_LBRACE)) {
                addToParseTree("语句 → 复合语句");
                parseCompoundStatement();
            } else if (match(TOKEN_IF)) {
                addToParseTree("语句 → 条件语句");
                parseIfStatement();
            } else if (match(TOKEN_WHILE)) {
                addToParseTree("语句 → while语句");
                parseWhileStatement();
            } else if (match(TOKEN_FOR)) {
                addToParseTree("语句 → for语句");
                parseForStatement();
            } else if (match(TOKEN_INT) || match(TOKEN_FLOAT) || match(TOKEN_VOID) || match(TOKEN_CHAR) || match(TOKEN_STRING)) {
                addToParseTree("语句 → 声明语句");
                parseDeclaration();
            } else if (match(TOKEN_ID) && lexer.peekNextToken().type == TOKEN_LPAREN) {
                // 如果当前是标识符，并且下一个token是左括号，那可能是函数调用
                addToParseTree("语句 → 函数调用语句");
                parseFunctionCall();
            } else {
                addToParseTree("语句 → 表达式语句");
                parseExpressionStatement();
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
    private void parseExpressionStatement() throws IOException {
        indentLevel++;
        try {
            if (match(TOKEN_SEMICOLON)) {
                addToParseTree("表达式语句 → ;");
                addToParseTree("匹配分号: ;");
                advance();
            } else {
                addToParseTree("表达式语句 → 表达式 ;");
                S();  // 使用已有的表达式解析方法
                if (match(TOKEN_SEMICOLON)) {
                    addToParseTree("匹配分号: ;");
                    advance();
                } else {
                    error("期望 ';' 分号");
                }
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
    private void parseCompoundStatement() throws IOException {
        indentLevel++;
        try {
            addToParseTree("复合语句 → { 语句列表 }");
            if (match(TOKEN_LBRACE)) {
                addToParseTree("匹配左大括号: {");
                advance();
            } else {
                error("期望 '{'");
                return;
            }
            parseStatementList();
            if (match(TOKEN_RBRACE)) {
                addToParseTree("匹配右大括号: }");
                advance();
            } else {
                error("期望 '}'");
            }
        } finally {
            indentLevel--;
        }
    }

    /**
     * 解析If语句
     * 条件语句 → if ( 表达式 ) 语句 else部分
     * @throws IOException 如果IO操作失败
     */
    private void parseIfStatement() throws IOException {
        indentLevel++;
        try {
            addToParseTree("条件语句 → if ( 表达式 ) 语句 else部分");
            if (match(TOKEN_IF)) {
                addToParseTree("匹配条件关键字: if");
                advance();
            } else {
                error("期望 'if'");
                return;
            }
            if (match(TOKEN_LPAREN)) {
                addToParseTree("匹配左括号: (");
                advance();
            } else {
                error("期望 '('");
                return;
            }
            S();  // 解析条件表达式
            if (match(TOKEN_RPAREN)) {
                addToParseTree("匹配右括号: )");
                advance();
            } else {
                error("期望 ')'");
                return;
            }
            parseStatement();  // 解析if块中的语句
            parseElsePart();   // 解析else部分
        } finally {
            indentLevel--;
        }
    }

    /**
     * 解析Else部分
     * else部分 → else 语句 | 空
     * @throws IOException 如果IO操作失败
     */
    private void parseElsePart() throws IOException {
        indentLevel++;
        try {
            if (match(TOKEN_ELSE)) {
                addToParseTree("else部分 → else 语句");
                addToParseTree("匹配else关键字: else");
                advance();
                parseStatement();  // 解析else块中的语句
            } else {
                addToParseTree("else部分 → 空");
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
    private void parseDeclaration() throws IOException {
        indentLevel++;
        try {
            addToParseTree("声明语句 → 类型说明符 标识符 = 表达式 ;");
            parseTypeSpecifier();  // 解析类型说明符
            if (match(TOKEN_ID)) {
                addToParseTree("匹配标识符: " + currentToken.value);
                advance();
            } else {
                error("期望标识符");
                return;
            }
            // 处理可选的初始化部分
            if (match(TOKEN_ASSIGN)) {
                addToParseTree("匹配赋值符号: =");
                advance();
                S();  // 解析初始化表达式
            }
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
     * 解析While语句
     * while语句 → while ( 表达式 ) 语句
     * @throws IOException 如果IO操作失败
     */
    private void parseWhileStatement() throws IOException {
        indentLevel++;
        try {
            addToParseTree("while语句 → while ( 表达式 ) 语句");
            if (match(TOKEN_WHILE)) {
                addToParseTree("匹配循环关键字: while");
                advance();
            } else {
                error("期望 'while'");
                return;
            }
            if (match(TOKEN_LPAREN)) {
                addToParseTree("匹配左括号: (");
                advance();
            } else {
                error("期望 '('");
                return;
            }
            S();  // 解析循环条件表达式
            if (match(TOKEN_RPAREN)) {
                addToParseTree("匹配右括号: )");
                advance();
            } else {
                error("期望 ')'");
                return;
            }
            parseStatement();  // 解析while循环体中的语句
        } finally {
            indentLevel--;
        }
    }

    /**
     * 解析For语句
     * for语句 → for ( 表达式语句 表达式语句 表达式 ) 语句
     * @throws IOException 如果IO操作失败
     */
    private void parseForStatement() throws IOException {
        indentLevel++;
        try {
            addToParseTree("for语句 → for ( 表达式语句 表达式语句 表达式 ) 语句");
            if (match(TOKEN_FOR)) {
                addToParseTree("匹配循环关键字: for");
                advance();
            } else {
                error("期望 'for'");
                return;
            }
            if (match(TOKEN_LPAREN)) {
                addToParseTree("匹配左括号: (");
                advance();
            } else {
                error("期望 '('");
                return;
            }

            // 允许声明语句作为初始化
            addToParseTree("解析for循环初始化表达式");
            // 检查是否是声明语句
            if (match(TOKEN_INT) || match(TOKEN_FLOAT) || match(TOKEN_VOID) || match(TOKEN_CHAR) || match(TOKEN_STRING)) {
                parseDeclaration();
            } else {
                parseExpressionStatement();
            }

            // 解析条件表达式语句
            addToParseTree("解析for循环条件表达式");
            parseExpressionStatement();

            // 解析迭代表达式
            addToParseTree("解析for循环迭代表达式");
            S();  // 使用已有的表达式解析方法

            if (match(TOKEN_RPAREN)) {
                addToParseTree("匹配右括号: )");
                advance();
            } else {
                error("期望 ')'");
                return;
            }

            // 解析for循环体
            parseStatement();
        } finally {
            indentLevel--;
        }
    }
}