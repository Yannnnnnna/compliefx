package com.example.compliefx2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Stack;


public class ManualLexer {
    // Token映射表（用于获取Token编码）
    private Map<String, Integer> tokenMap;

    //存储语法分析器要分析的内容
    private Map<String, Integer> resultMap;

    // 输入读取相关
    private BufferedReader reader;
    private String currentLine;
    private int currentLineNum = 0;
    private int currentLineIndex;

    public int getCurrentLineIndex() {
        return currentLineIndex;
    }

    public void setCurrentLineIndex(int currentLineIndex) {
        this.currentLineIndex = currentLineIndex;
    }

    private StringBuilder result = new StringBuilder();
    private StringBuilder error = new StringBuilder();

    private Stack<Token> tokenBuffer = new Stack<>();
    /**
     * 回退一个已读取的token，实现向前看一个token的功能
     * @param token 要回退的token
     */
    public void pushBackToken(Token token) {
        // 实现token回退，可以简单地用一个栈或队列存储
        this.tokenBuffer.push(token);
    }

    //关键字列表
    private static final String[] KEYWORDS = {
            "char", "int", "float", "break", "const", "return", "void",
            "continue", "do", "while", "if", "else", "for", "string","true","false"
    };

    // 复合操作符列表
    private static final String[] COMPOUND_OPERATORS = {
            "++", "--", "-=", "+=", "==", "!=", ">=", "<=","&&", "||"
    };

    // 单字符操作符
    private static final String OPERATORS = "+-*/%=<>!&|";

    // 分隔符
    private static final String DELIMITERS = "{};,()[].:";
    //构造函数
    public ManualLexer(Reader reader, Map<String, Integer> tokenMap) {
        this.reader = new BufferedReader(reader);
        this.tokenMap = tokenMap;
        this.currentLineIndex = 0;
        this.result.setLength(0);
        this.error.setLength(0);
        skipBOM(); // 在开始解析前跳过BOM
    }
    //获取结果
    public String getResult() {
        return result.toString();
    }
    //获取错误
    public String getError() {
        return error.toString();
    }
    //获取种别码
    private int getTokenCode(String token) {
        return tokenMap.getOrDefault(token, -1);
    }
    //逐行读取
    private boolean readNextLine() throws IOException {
        currentLine = reader.readLine();
        currentLineNum++;
        currentLineIndex = 0;
        // 如果是新行，检查BOM
        if (currentLine != null && currentLineIndex == 0) {
            // 检查行首是否为BOM
            if (currentLine.length() > 0 && currentLine.charAt(0) == '\uFEFF') {
                currentLineIndex = 1; // 跳过BOM
            }
        }
        return currentLine != null;
    }


    /**
     * 查看当前字符但不消费
     */
    private char peek() {
        if (currentLine == null || currentLineIndex >= currentLine.length()) {
            return '\0';
        }
        return currentLine.charAt(currentLineIndex);
    }

    /**
     * 查看下一个字符但不消费
     */
    private char peekNext() {
        if (currentLine == null || currentLineIndex + 1 >= currentLine.length()) {
            return '\0';
        }
        return currentLine.charAt(currentLineIndex + 1);
    }

    /**
     * 消费并返回当前字符
     */
    private char consume() {
        char c = peek();
        currentLineIndex++;
        return c;
    }

    /**
     * 查看当前位置往后n个字符
     */
    private char peekAhead(int n) {
        if (currentLine == null || currentLineIndex + n >= currentLine.length()) {
            return '\0';
        }
        return currentLine.charAt(currentLineIndex + n);
    }

    /**
     * 判断字符是否为字母
     */
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    /**
     * 判断字符是否为数字
     */
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    /**
     * 判断字符是否为16进制数字
     */
    private boolean isHexDigit(char c) {
        return isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    /**
     * 判断字符是否为8进制数字
     */
    private boolean isOctalDigit(char c) {
        return c >= '0' && c <= '7';
    }


    /*跳过BOM*/
    private void skipBOM() {
        // 检查并跳过UTF-8 BOM (EF BB BF)
        if (peek() == '\uFEFF') {
            consume();
        }
    }
    /**
     * 判断字符是否为运算符
     */
    private boolean isOperator(char c) {
        return OPERATORS.indexOf(c) != -1;
    }

    /**
     * 判断字符是否为分隔符
     */
    private boolean isDelimiter(char c) {
        return DELIMITERS.indexOf(c) != -1;
    }

    /**
     * 判断字符串是否为关键字
     */
    private boolean isKeyword(String str) {
        for (String keyword : KEYWORDS) {
            if (keyword.equals(str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断字符是否为空白字符
     */
    private boolean isWhitespace(char c) {
        return Character.isWhitespace(c);
    }

    /**
     * 跳过空白字符
     */
    private void skipWhitespace() {
        while (peek() != '\0' && isWhitespace(peek())) {
            consume();
        }
    }


    /**
     * 处理标识符或关键字
     */
    private Token scanIdentifierOrKeyword() {
        StringBuilder sb = new StringBuilder();

        // 首字符必须是字母或下划线
        sb.append(consume());

        // 后续字符可以是字母、数字或下划线
        while (isAlpha(peek()) || isDigit(peek()) || peek() == '_') {
            sb.append(consume());
        }

        String value = sb.toString();

        // 判断是否为关键字
        if (isKeyword(value)) {
            return new Token(getTokenCode(value), value,currentLineNum);
        } else {
            return new Token(getTokenCode("Identifier"), value, currentLineNum);
        }
    }

    /**
     * 处理数字（包括整数、浮点数、16进制、8进制等）
     */
    private Token scanNumber() {
        // 以0开头的特殊处理（八/十六进制或浮点数）
        if (peek() == '0') {
            return processZeroPrefix();
        }
        // 处理普通十进制数
        return processDecimal();
    }

    /**
     * 处理以0开头的数字（八进制/十六进制/浮点数）
     */
    private Token processZeroPrefix() {
        consume(); // 消费首字符'0'
        StringBuilder sb = new StringBuilder("0");

        // 检测十六进制（0x...）
        if (peek() == 'x' || peek() == 'X') {
            return processHexadecimal(sb);
        }
        // 检测八进制（0...）
        if (isDigit(peek())) {
            return processOctal(sb);
        }
        // 处理浮点数（0.xxx）
        if (peek() == '.') {
            return scanFloat(sb);
        }
        // 单独的0
        return new Token(getTokenCode("Integer"), sb.toString(),currentLineNum);
    }

    /**
     * 处理十六进制数（0x...）
     * sb 包含"0x"的字符串构建器
     */
    private Token processHexadecimal(StringBuilder sb) {
        sb.append(consume()); // 消费x/X
        // 验证必须包含至少一个十六进制数字
        if (!isHexDigit(peek())) {
            return errorToken("无法识别的16进制数: " + sb);
        }

        // 收集所有连续十六进制字符
        while (isHexDigit(peek())) {
            sb.append(consume());
        }

        // 检查非法后缀
        if (isInvalidSuffix(peek())) {
            return handleInvalidSuffix(sb, "无法识别的16进制数: ");
        }

        return new Token(getTokenCode("Integer"), sb.toString(),currentLineNum);
    }

    /**
     * 处理八进制数（0...）
     * sb 包含前导0的字符串构建器
     */
    private Token processOctal(StringBuilder sb) {
        boolean hasMultipleLeadingZeros = false; // 多个前导零标志
        boolean hasNonZeroDigit = false;          // 存在非零数字标志
        boolean hasIllegalDigit = false;          // 存在非法数字（8/9）

        // 收集所有连续数字并进行验证
        while (isDigit(peek())) {
            char c = peek();
            // 检测第二个连续零（如007）
            if (c == '0' && sb.length() == 1) hasMultipleLeadingZeros = true;
            // 检测非零数字
            if (c != '0') hasNonZeroDigit = true;
            // 检测非法数字8/9
            if (c > '7') hasIllegalDigit = true;
            sb.append(consume());
        }

        // 错误处理逻辑
        if (hasMultipleLeadingZeros && hasNonZeroDigit) {
            return errorToken("无效的8进制数（多个前导零）: " + sb);
        }
        if (hasIllegalDigit) {
            return errorToken("无效的8进制数（包含数字8或9）: " + sb);
        }

        // 检查非法后缀（如0123a）
        if (isInvalidSuffix(peek())) {
            return handleInvalidSuffix(sb, "无法识别的8进制数: ");
        }

        return new Token(getTokenCode("Integer"), sb.toString(),currentLineNum);
    }

    /**
     * 处理十进制整数
     */
    private Token processDecimal() {
        StringBuilder sb = new StringBuilder();
        // 收集所有连续数字
        while (isDigit(peek())) {
            sb.append(consume());
        }

        // 浮点数处理（如123.45）
        if (peek() == '.') {
            return scanFloat(sb);
        }
        // 科学计数法处理（如1e5）
        if (peek() == 'e' || peek() == 'E') {
            return scanScientific(sb);
        }

        // 检查非法后缀（如123a）
        if (isInvalidSuffix(peek())) {
            return handleInvalidSuffix(sb, "无法识别的十进制数: ");
        }

        return new Token(getTokenCode("Integer"), sb.toString(),currentLineNum);
    }

    /**
     * 判断数字后是否为非法后缀字符
     */
    private boolean isInvalidSuffix(char c) {
        return isAlpha(c) || c == '_' || c == '.';
    }

    /**
     * 统一处理非法后缀错误
     * sb 原始数字字符串
     * errorMessage 错误信息前缀
     */
    private Token handleInvalidSuffix(StringBuilder sb, String errorMessage) {
        StringBuilder errorSb = new StringBuilder(sb);
        // 收集所有连续非法字符
        while (isAlpha(peek()) || isDigit(peek()) || peek() == '_' || peek() == '.') {
            errorSb.append(consume());
        }
        return errorToken(errorMessage + errorSb);
    }

    /**
     * 生成错误Token并记录错误信息
     */
    private Token errorToken(String message) {
        error.append(message).append("\n");
        // （格式："错误信息: 错误内容"）
        return new Token(-1, message.split(": ")[1],currentLineNum);
    }

    /**
     * 处理浮点数部分
     */
    private Token scanFloat(StringBuilder sb) {
        // 已经处理了整数部分，现在处理小数点
        sb.append(consume()); // 消费'.'

        // 检查是否小数点后面有数字
        boolean hasDigitsAfterDot = false;
        while (isDigit(peek())) {
            sb.append(consume());
            hasDigitsAfterDot = true;
        }

        // 检查是否为科学计数法
        if (peek() == 'e' || peek() == 'E') {
            return scanScientific(sb);
        }

        // 检查是否后面跟着非法字符
        if (isAlpha(peek()) || peek() == '_' || peek() == '.' ) {
            // 修改: 完整消费整个非法标识符
            StringBuilder errorSb = new StringBuilder(sb);
            while (isAlpha(peek()) || isDigit(peek()) || peek() == '_' || peek() == '.' ) {
                errorSb.append(consume());
            }
            error.append("无法识别的浮点数: ").append(errorSb).append("\n");
            return new Token(-1, errorSb.toString(),currentLineNum);
        }

        // 修改: 增强对异常浮点数的检查
        if (!hasDigitsAfterDot) {
            // 如果是以.开头且后面没有数字
            if (sb.length() == 1 && sb.charAt(0) == '.') {
                error.append("无法识别的浮点数: ").append(sb).append("\n");
                return new Token(-1, sb.toString(),currentLineNum);
            }
            // 如果是以数字开头，后面跟.但没有数字 (如15.)
            if (peek() != 'e' && peek() != 'E') {
                error.append("无法识别的浮点数: ").append(sb).append("\n");
                return new Token(-1, sb.toString(),currentLineNum);
            }
        }

        return new Token(getTokenCode("FloatNumber"), sb.toString(),currentLineNum);
    }

    /**
     * 处理科学计数法
     */
    private Token scanScientific(StringBuilder sb) {
        sb.append(consume()); // 消费'e'或'E'

        // 检查是否有+/-符号
        if (peek() == '+' || peek() == '-') {
            sb.append(consume());
        }

        // 必须有数字
        if (!isDigit(peek())) {
            // 修改：更详细的错误提示
            error.append("无法识别的浮点数: ").append(sb);
            if (peek() != '\0' && peek() != '\n') {
                error.append(peek());
            }
            error.append("\n");
            // 修改：完整消费非法标识符
            while (isAlpha(peek()) || isDigit(peek()) || peek() == '_' || peek() == '.' ) {
                sb.append(consume());
            }
            return new Token(-1, sb.toString(),currentLineNum);
        }

        // 消费所有数字
        while (isDigit(peek())) {
            sb.append(consume());
        }

        // 检查是否后面跟着非法字符
        if (isAlpha(peek()) || peek() == '_' || peek() == '.') {
            // 修改：完整消费非法标识符
            StringBuilder errorSb = new StringBuilder(sb);
            while (isAlpha(peek()) || isDigit(peek()) || peek() == '_' || peek() == '.') {
                errorSb.append(consume());
            }
            error.append("无法识别的科学计数法: ").append(errorSb).append("\n");
            return new Token(-1, errorSb.toString(),currentLineNum);
        }

        return new Token(getTokenCode("FloatNumber"), sb.toString(),currentLineNum);
    }

    /**
     * 处理字符常量
     */
    private Token scanCharacter() {
        StringBuilder sb = new StringBuilder();
        sb.append(consume()); // 消费开始的单引号

        // 检查是否为空字符
        if (peek() == '\'') {
            error.append("无法识别的字符常量: 空字符").append("\n");
            sb.append(consume());
            return new Token(-1, sb.toString(),currentLineNum);
        }

        // 处理转义字符
        if (peek() == '\\') {
            sb.append(consume());
            if ("0tnr'\"\\".indexOf(peek()) != -1) {
                sb.append(consume());
            } else {
                error.append("无法识别的转义字符: \\").append(peek()).append("\n");
                sb.append(consume());
            }
        } else {
            // 普通字符
            sb.append(consume());
        }

        // 必须以单引号结束
        if (peek() != '\'') {
            error.append("无法识别的字符常量: 缺少结束的单引号").append("\n");
            while (peek() != '\0' && peek() != '\'' && peek() != '\n') {
                sb.append(consume());
            }
            if (peek() == '\'') {
                sb.append(consume());
            }
            return new Token(-1, sb.toString(),currentLineNum);
        }

        sb.append(consume()); // 消费结束的单引号
        return new Token(getTokenCode("Character"), sb.toString(),currentLineNum);
    }

    /**
     * 处理字符串常量
     */
    private Token scanString() {
        StringBuilder sb = new StringBuilder();
        sb.append(consume()); // 消费开始的双引号

        // 收集字符直到遇到结束引号或行尾
        while (peek() != '\0' && peek() != '"' && peek() != '\n') {
            // 处理转义字符
            if (peek() == '\\') {
                sb.append(consume());
                if (peek() == '\0' || peek() == '\n') {
                    break;
                }
                sb.append(consume());
            } else {
                sb.append(consume());
            }
        }

        // 检查是否以双引号结束
        if (peek() != '"') {
            // 修改: 更详细的错误提示
            error.append("无法识别的字符串常量: 缺少结束的双引号").append("\n");
            return new Token(-1, sb.toString(),currentLineNum);
        }

        sb.append(consume()); // 消费结束的双引号
        return new Token(getTokenCode("String"), sb.toString(),currentLineNum);
    }

    /**
     * 处理注释
     */
    private Token scanComment() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(consume()); // 消费第一个'/'，指针指向下一个字符

        // 判断注释类型
        if (peek() == '/') { // 单行注释处理
            sb.append(consume()); // 消费第二个'/'

            // 消费直到行尾或文件结束
            while (peek() != '\0' && peek() != '\n') {
                sb.append(consume());
            }
            return null; // 单行注释不生成Token

        } else if (peek() == '*') { // 多行注释处理
            sb.append(consume()); // 消费'*'

            // 多行注释循环处理
            while (true) {
                // 处理文件结束符（未闭合的注释）
                if (peek() == '\0') {
                    // 尝试读取下一行输入
                    if (!readNextLine()) { //已经到文件末尾
                        error.append("无法识别的注释: 缺少结束的*/").append("\n");
                        return null;
                    }
                    sb.append('\n'); // 保留换行符
                    continue; // 继续处理新行内容
                }

                // 检测注释结束符（*后面跟/）
                if (peek() == '*' && peekNext() == '/') {
                    sb.append(consume()); // 消费'*'
                    sb.append(consume()); // 消费'/'
                    break; // 退出循环
                }

                // 没有找到*/消费当前字符（包括换行符）
                sb.append(consume());
            }
            return null; // 多行注释不生成Token

        } else {
            // 单独的'/'运算符（非注释）
            return new Token(getTokenCode("/"), "/",currentLineNum);
        }
    }
    /**
     * 处理运算符（包括复合运算符）
     */
    private Token scanOperator() {
        // 获取当前字符（不消费）
        char c = peek();
        // 创建字符串构建器保存运算符
        StringBuilder sb = new StringBuilder();
        // 消费当前字符并添加到构建器
        sb.append(consume());

        // 遍历所有预定义的复合运算符
        for (String op : COMPOUND_OPERATORS) {
            // 跳过长度不足2或首字符不匹配的运算符
            if (op.length() <= 1 || op.charAt(0) != c) {
                continue;
            }

            boolean isMatch = true;
            // 逐字符检查后续字符是否匹配
            // 使用i-1是因为已经消费了第一个字符
            for (int i = 1; i < op.length(); i++) {
                // 检查当前字符之后的第i-1个字符是否匹配
                if (peekAhead(i-1) != op.charAt(i)) {
                    isMatch = false;
                    break;
                }
            }

            if (isMatch) {
                // 消费剩余字符（从第2个字符开始）
                for (int i = 1; i < op.length(); i++) {
                    sb.append(consume());
                }
                // 返回对应的Token
                return new Token(getTokenCode(op), op,currentLineNum);
            }
        }

        // 如果未匹配到复合运算符，返回单字符运算符Token
        return new Token(getTokenCode(String.valueOf(c)), String.valueOf(c),currentLineNum);
    }

    /**
     * 处理分隔符
     */
    private Token scanDelimiter() {
        char c = consume();
        return new Token(getTokenCode(String.valueOf(c)), String.valueOf(c),currentLineNum);
    }

    /**
     * 处理单个token
     */
    private Token scanToken() throws IOException {
        // 跳过空白字符
        skipWhitespace();

        char c = peek();

        // 检查是否到达文件末尾
        if (c == '\0') {
            return null;
        }

        // 处理标识符和关键字
        if (isAlpha(c) || c == '_') {
            return scanIdentifierOrKeyword();
        }

        // 处理数字
        if (isDigit(c)) {
            return scanNumber();
        }

        // 处理点字符
        if (c == '.') {
            StringBuilder sb = new StringBuilder();
            sb.append(consume()); // 消费'.'

            // 消费点后面的所有数字字符，将它们作为一个整体处理
            while (isDigit(peek())) {
                sb.append(consume());
            }

            // 处理可能的科学计数法部分 (e/E 后面的数字)
            if ((peek() == 'e' || peek() == 'E')) {
                sb.append(consume()); // 消费'e'或'E'

                // 处理可能的正负号
                if (peek() == '+' || peek() == '-') {
                    sb.append(consume());
                }

                // 消费指数部分的数字
                while (isDigit(peek())) {
                    sb.append(consume());
                }
            }

            error.append("异常浮点数: 以点开头的数字 ").append(sb.toString()).append("\n");
            return new Token(-1, sb.toString(),currentLineNum);
        }

        // 处理字符串
        if (c == '"') {
            return scanString();
        }

        // 处理字符
        if (c == '\'') {
            return scanCharacter();
        }

        // 处理注释和除号
        if (c == '/') {
            if (peekNext() == '/' || peekNext() == '*') {
                return scanComment();
            }
        }

        // 处理运算符
        if (isOperator(c)) {
            return scanOperator();
        }

        // 处理分隔符
        if (isDelimiter(c)) {
            return scanDelimiter();
        }

        // 处理非法字符
        error.append("无法识别的字符: ").append(c).append("\n");
        consume();
        return new Token(-1, String.valueOf(c),currentLineNum);
    }


    public Token nextToken() throws IOException {
        if (!tokenBuffer.empty()) {
            return tokenBuffer.pop();
        }
        while (true) {
            // 处理文件末尾
            if (currentLine == null && !readNextLine()) {
                return null;
            }

            // 处理行末尾
            if (currentLineIndex >= currentLine.length()) {
                if (!readNextLine()) {
                    return null;
                }
                continue;
            }

            Token token = scanToken();

            // 如果token为null，继续扫描
            if (token == null) {
                continue;
            }

            // 记录token并返回
            if (token.type != -1) {
                result.append(token).append("\n");
            }

            return token;
        }
    }
    public Token peekNextToken() throws IOException {
        if (!tokenBuffer.isEmpty()) {
            return tokenBuffer.peek();
        }
        Token token = nextToken();
        if (token != null) {
            pushBackToken(token); // 将 Token 压回缓冲区
        }
        return token;
    }
    public void close() throws IOException {
        reader.close();
    }
}