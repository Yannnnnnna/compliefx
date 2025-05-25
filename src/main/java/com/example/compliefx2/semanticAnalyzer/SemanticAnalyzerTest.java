package com.example.compliefx2.semanticAnalyzer;

import com.example.compliefx2.ASTNode;
import com.example.compliefx2.TokenLibrary;
import com.example.compliefx2.syntaxAnalyzer.StatementParser;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

/**
 * 语义分析器测试类
 */
public class SemanticAnalyzerTest {

    public static void main(String[] args) {
        // 测试用例
        String[] testPrograms = {
                // 测试1：正常的变量声明和使用
                "void main() { int x = 5; int y = x + 1; }",

                // 测试2：重复声明错误
                "void main() { int x = 5; int x = 10; }",

                // 测试3：使用未声明变量
                "void main() { int x = 5; y = x + 1; }",

                // 测试4：类型不匹配（如果支持多种类型）
                "void main() { int x = 5; float y = 3.14; x = y; }",

                // 测试5：复杂的控制结构
                "void main() { int i = 0; while (i < 10) { int j = i + 1; i = j; } }"
        };

        // 读取token映射
        Map<String, Integer> tokenMap = TokenLibrary.readToken(
                "C:\\Users\\WYR\\Desktop\\编译原理\\compliefx2\\src\\main\\resources\\com\\example\\compliefx2\\tokenTable.json"
        );

        if (tokenMap == null) {
            System.err.println("无法读取token表文件");
            return;
        }

        // 逐个测试程序
        for (int i = 0; i < testPrograms.length; i++) {
            System.out.println("======= 测试程序 " + (i + 1) + ": " + testPrograms[i] + " =======");
            testSemanticAnalysis(testPrograms[i], tokenMap);
            System.out.println();
        }
    }

    /**
     * 测试单个程序的语义分析
     */
    private static void testSemanticAnalysis(String sourceCode, Map<String, Integer> tokenMap) {
        try {
            // Step 1: 语法分析生成AST
            StatementParser parser = new StatementParser(new StringReader(sourceCode), tokenMap);
            boolean syntaxValid = parser.parseProgram();

            if (!syntaxValid) {
                System.out.println("语法分析失败，跳过语义分析");
//                return;
            }

            ASTNode astRoot = parser.getAST();
            if (astRoot == null) {
                System.out.println("未生成AST，跳过语义分析");
//                return;
            }
            System.out.println(parser.getParseTree());
            System.out.println(astRoot.toString());

            System.out.println("语法分析通过，开始语义分析...");

            // Step 2: 语义分析
            SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
            boolean semanticValid = semanticAnalyzer.analyze(astRoot);

            // Step 3: 输出结果
            semanticAnalyzer.printAnalysisResult();

            if (semanticValid) {
                System.out.println("✓ 语义分析通过");
            } else {
                System.out.println("✗ 语义分析发现错误");
            }

        } catch (IOException e) {
            System.err.println("IO错误: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("分析过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 单独测试语义分析器的功能
     */
    public static void testSemanticAnalyzerOnly() {
        System.out.println("======= 语义分析器独立测试 =======");

        // 手动构建一个简单的AST进行测试
        ASTNode program = new ASTNode(ASTNode.NodeType.PROGRAM);

        // 函数定义：void main()
        ASTNode functionDef = new ASTNode(ASTNode.NodeType.FUNCTION_DEF, "void");
        ASTNode functionName = new ASTNode(ASTNode.NodeType.IDENTIFIER, "main");
        ASTNode paramList = new ASTNode(ASTNode.NodeType.PARAMETER_LIST);
        ASTNode functionBody = new ASTNode(ASTNode.NodeType.BLOCK_STMT);

        functionDef.addChild(functionName);
        functionDef.addChild(paramList);
        functionDef.addChild(functionBody);

        // 变量声明：int x = 5;
        ASTNode declaration = new ASTNode(ASTNode.NodeType.DECLARATION_STMT, "int");
        ASTNode varName = new ASTNode(ASTNode.NodeType.IDENTIFIER, "x");
        ASTNode assignment = ASTNode.createAssignment(
                ASTNode.createIdentifier("x", 1, 1),
                ASTNode.createIntLiteral("5", 1, 1),
                1, 1
        );

        declaration.addChild(varName);
        declaration.addChild(assignment);
        functionBody.addChild(declaration);

        // 表达式语句：y = x + 1;
        ASTNode expressionStmt = new ASTNode(ASTNode.NodeType.EXPRESSION_STMT);
        ASTNode assignment2 = ASTNode.createAssignment(
                ASTNode.createIdentifier("y", 2, 1),
                ASTNode.createBinaryOp("+",
                        ASTNode.createIdentifier("x", 2, 5),
                        ASTNode.createIntLiteral("1", 2, 9),
                        2, 5),
                2, 1
        );

        expressionStmt.addChild(assignment2);
        functionBody.addChild(expressionStmt);

        program.addChild(functionDef);

        // 进行语义分析
        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        boolean result = analyzer.analyze(program);

        analyzer.printAnalysisResult();

        System.out.println("手动构建AST的语义分析结果: " + (result ? "通过" : "失败"));
    }
}