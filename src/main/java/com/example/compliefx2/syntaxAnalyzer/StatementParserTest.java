package com.example.compliefx2.syntaxAnalyzer;

import com.example.compliefx2.TokenLibrary;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

/**
 * 语法分析器测试类
 * 用于测试程序结构、主函数、if语句和多行语句的语法分析
 */
public class StatementParserTest {
    public static void main(String[] args) {
        // 获取Token映射表
        Map<String, Integer> tokenMap = TokenLibrary.readToken("C:\\Users\\WYR\\Desktop\\编译原理\\compliefx2\\src\\main\\resources\\com\\example\\compliefx2\\tokenTable.json");

        // 测试主函数解析
        testMainFunction(tokenMap);

        // 测试if语句解析
        testIfStatement(tokenMap);

        // 测试复杂程序结构解析
        testComplexProgram(tokenMap);
    }

    /**
     * 测试主函数解析
     */
    private static void testMainFunction(Map<String, Integer> tokenMap) {
        String mainFunction = "int main() { int a = 5; }";

        System.out.println("\n======= 测试主函数: " + mainFunction + " =======");
        try {
            StatementParser parser = new StatementParser(new StringReader(mainFunction), tokenMap);
            boolean isValid = parser.parseProgram();
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

    /**
     * 测试if语句解析
     */
    private static void testIfStatement(Map<String, Integer> tokenMap) {
        String ifStatement = "int main() { if (x > 5) { y = 10; } else { y = 0; } }";

        System.out.println("\n======= 测试if语句: " + ifStatement + " =======");
        try {
            StatementParser parser = new StatementParser(new StringReader(ifStatement), tokenMap);
            boolean isValid = parser.parseProgram();
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

    /**
     * 测试复杂程序结构解析
     */
    private static void testComplexProgram(Map<String, Integer> tokenMap) {
        String complexProgram =
                "int main() {\n" +
                        "    int x = 10;\n" +
                        "    int y = 20;\n" +
                        "    if (x < y) {\n" +
                        "        int z = x + y;\n" +
                        "        x = z * 2;\n" +
                        "    } else {\n" +
                        "        y = y + 1;\n" +
                        "    }\n" +
                        "    int result = x + y;\n" +
                        "}";

        System.out.println("\n======= 测试复杂程序: =======");
        System.out.println(complexProgram);
        try {
            StatementParser parser = new StatementParser(new StringReader(complexProgram), tokenMap);
            boolean isValid = parser.parseProgram();
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