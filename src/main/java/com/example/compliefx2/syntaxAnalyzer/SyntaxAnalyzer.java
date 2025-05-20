package com.example.compliefx2.syntaxAnalyzer;

import com.example.compliefx2.ManualLexer;
import com.example.compliefx2.Token;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

/**
 * 语法分析器适配器类
 * 用于将各种不同类型的语法分析器与UI集成
 */
public class SyntaxAnalyzer {

    /**
     * 进行统一表达式的语法分析（支持算术/布尔/混合表达式）
     * @param sourceCode 源代码字符串
     * @param tokenMap Token映射表
     * @return 分析结果，包含是否有效、解析树和错误信息
     */
    public static AnalysisResult analyzeUnifiedExpression(String sourceCode, Map<String, Integer> tokenMap) {
        StringReader reader = new StringReader(sourceCode);
        UnifiedExpressionParser parser = null;
        try {
            parser = new UnifiedExpressionParser(reader, tokenMap);
            boolean isValid = parser.parse();
            String parseTree = parser.getParseTree();
            String errorMsg = parser.getErrorMsg();
            return new AnalysisResult(isValid, parseTree, errorMsg);
        } catch (IOException e) {
            e.printStackTrace();
            return new AnalysisResult(false, "", "解析过程发生错误: " + e.getMessage());
        }
    }

    /**
     * 进行完整程序结构的语法分析（支持函数、语句块、条件语句等）
     * @param sourceCode 源代码字符串
     * @param tokenMap Token映射表
     * @return 分析结果，包含是否有效、解析树和错误信息
     */
    public static AnalysisResult analyzeProgram(String sourceCode, Map<String, Integer> tokenMap) {
        StringReader reader = new StringReader(sourceCode);
        StatementParser parser = null;
        try {
            parser = new StatementParser(reader, tokenMap);
            boolean isValid = parser.parseProgram();
            String parseTree = parser.getParseTree();
            String errorMsg = parser.getErrorMsg();
            return new AnalysisResult(isValid, parseTree, errorMsg);
        } catch (IOException e) {
            e.printStackTrace();
            return new AnalysisResult(false, "", "解析过程发生错误: " + e.getMessage());
        }
    }

    /**
     * 分析结果类，封装语法分析的结果
     */
    public static class AnalysisResult {
        private final boolean isValid;
        private final String parseTree;
        private final String errorMsg;

        public AnalysisResult(boolean isValid, String parseTree, String errorMsg) {
            this.isValid = isValid;
            this.parseTree = parseTree;
            this.errorMsg = errorMsg;
        }

        public boolean isValid() {
            return isValid;
        }

        public String getParseTree() {
            return parseTree;
        }

        public String getErrorMsg() {
            return errorMsg;
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            result.append("语法分析结果: ").append(isValid ? "有效" : "无效").append("\n\n");

            if (!parseTree.isEmpty()) {
                result.append("解析树:\n").append(parseTree).append("\n");
            }

            if (!errorMsg.isEmpty()) {
                result.append("错误信息:\n").append(errorMsg);
            }

            return result.toString();
        }
    }
}