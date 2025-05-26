package com.example.compliefx2.intermediateCode;

import com.example.compliefx2.ASTNode;
import com.example.compliefx2.intermediateCode.IntermediateCodeGenerator;

/**
 * 中间代码生成与优化测试用例
 */
public class OptimizationTestCases {

    /**
     * 测试用例1：常量折叠
     * 源代码: int a = 3 + 5; int b = 10 * 2; int c = 20 / 4;
     */
    public static void testConstantFolding() {
        System.out.println("=== 测试用例1：常量折叠 ===");

        IntermediateCodeGenerator generator = new IntermediateCodeGenerator();

        // 手动构建AST并生成中间代码（模拟）
        // 这里直接添加四元式来模拟生成的中间代码
        generator.emit("+", "3", "5", "t1");           // t1 = 3 + 5
        generator.emit("DECLARE", "int", "", "a");     // declare int a
        generator.emit("=", "t1", "", "a");            // a = t1

        generator.emit("*", "10", "2", "t2");          // t2 = 10 * 2
        generator.emit("DECLARE", "int", "", "b");     // declare int b
        generator.emit("=", "t2", "", "b");            // b = t2

        generator.emit("/", "20", "4", "t3");          // t3 = 20 / 4
        generator.emit("DECLARE", "int", "", "c");     // declare int c
        generator.emit("=", "t3", "", "c");            // c = t3

        System.out.println("优化前:");
        generator.printQuadruples();

        generator.optimize();

        System.out.println("\n优化后:");
        generator.printQuadruples();
        System.out.println();
    }

    /**
     * 测试用例2：常量传播
     * 源代码: int x = 5; int y = x + 3; int z = y * 2;
     */
    public static void testConstantPropagation() {
        System.out.println("=== 测试用例2：常量传播 ===");

        IntermediateCodeGenerator generator = new IntermediateCodeGenerator();

        generator.emit("DECLARE", "int", "", "x");     // declare int x
        generator.emit("=", "5", "", "x");             // x = 5

        generator.emit("DECLARE", "int", "", "y");     // declare int y
        generator.emit("+", "x", "3", "t1");           // t1 = x + 3
        generator.emit("=", "t1", "", "y");            // y = t1

        generator.emit("DECLARE", "int", "", "z");     // declare int z
        generator.emit("*", "y", "2", "t2");           // t2 = y * 2
        generator.emit("=", "t2", "", "z");            // z = t2

        System.out.println("优化前:");
        generator.printQuadruples();

        generator.optimize();

        System.out.println("\n优化后:");
        generator.printQuadruples();
        System.out.println();
    }

    /**
     * 测试用例3：代数化简
     * 源代码: int a = x + 0; int b = y * 1; int c = z * 0; int d = w - 0;
     */
    public static void testAlgebraicSimplification() {
        System.out.println("=== 测试用例3：代数化简 ===");

        IntermediateCodeGenerator generator = new IntermediateCodeGenerator();

        generator.emit("DECLARE", "int", "", "x");     // declare int x
        generator.emit("=", "10", "", "x");            // x = 10

        generator.emit("+", "x", "0", "t1");           // t1 = x + 0
        generator.emit("DECLARE", "int", "", "a");     // declare int a
        generator.emit("=", "t1", "", "a");            // a = t1

        generator.emit("*", "x", "1", "t2");           // t2 = x * 1
        generator.emit("DECLARE", "int", "", "b");     // declare int b
        generator.emit("=", "t2", "", "b");            // b = t2

        generator.emit("*", "x", "0", "t3");           // t3 = x * 0
        generator.emit("DECLARE", "int", "", "c");     // declare int c
        generator.emit("=", "t3", "", "c");            // c = t3

        generator.emit("-", "x", "0", "t4");           // t4 = x - 0
        generator.emit("DECLARE", "int", "", "d");     // declare int d
        generator.emit("=", "t4", "", "d");            // d = t4

        System.out.println("优化前:");
        generator.printQuadruples();

        generator.optimize();

        System.out.println("\n优化后:");
        generator.printQuadruples();
        System.out.println();
    }

    /**
     * 测试用例4：死代码消除
     * 源代码: int x = 5; int y = x + 3; int z = 10; // z未使用
     */
    public static void testDeadCodeElimination() {
        System.out.println("=== 测试用例4：死代码消除 ===");

        IntermediateCodeGenerator generator = new IntermediateCodeGenerator();

        generator.emit("DECLARE", "int", "", "x");     // declare int x
        generator.emit("=", "5", "", "x");             // x = 5

        generator.emit("DECLARE", "int", "", "y");     // declare int y
        generator.emit("+", "x", "3", "t1");           // t1 = x + 3
        generator.emit("=", "t1", "", "y");            // y = t1

        // 未使用的变量和临时变量
        generator.emit("DECLARE", "int", "", "z");     // declare int z (未使用)
        generator.emit("=", "10", "", "z");            // z = 10 (未使用)
        generator.emit("+", "20", "30", "t2");         // t2 = 20 + 30 (未使用)

        // 使用y，所以y不会被删除
        generator.emit("=", "y", "", "result");        // result = y

        System.out.println("优化前:");
        generator.printQuadruples();

        generator.optimize();

        System.out.println("\n优化后:");
        generator.printQuadruples();
        System.out.println();
    }

    /**
     * 测试用例5：跳转优化
     * 源代码: if (x > 0) { y = 1; } else { y = 0; }
     */
    public static void testJumpOptimization() {
        System.out.println("=== 测试用例5：跳转优化 ===");

        IntermediateCodeGenerator generator = new IntermediateCodeGenerator();

        generator.emit("DECLARE", "int", "", "x");     // declare int x
        generator.emit("=", "5", "", "x");             // x = 5
        generator.emit("DECLARE", "int", "", "y");     // declare int y

        // 条件判断
        generator.emit(">", "x", "0", "t1");           // t1 = x > 0
        generator.emit("JZ", "t1", "", "L1");          // if t1 == 0 goto L1

        // then 分支
        generator.emit("=", "1", "", "y");             // y = 1
        generator.emit("JMP", "", "", "L2");           // goto L2

        // else 分支
        generator.emit("LABEL", "L1", "", "");         // L1:
        generator.emit("=", "0", "", "y");             // y = 0

        // 结束标签
        generator.emit("LABEL", "L2", "", "");         // L2:

        // 添加一些无用的跳转和标签
        generator.emit("JMP", "", "", "L3");           // goto L3
        generator.emit("LABEL", "L3", "", "");         // L3: (紧接着跳转目标)
        generator.emit("LABEL", "L4", "", "");         // L4: (未使用的标签)

        System.out.println("优化前:");
        generator.printQuadruples();

        generator.optimize();

        System.out.println("\n优化后:");
        generator.printQuadruples();
        System.out.println();
    }

    /**
     * 测试用例6：综合优化
     * 包含多种优化机会的复杂测试
     */
    public static void testComprehensiveOptimization() {
        System.out.println("=== 测试用例6：综合优化 ===");

        IntermediateCodeGenerator generator = new IntermediateCodeGenerator();

        // 常量折叠 + 代数化简
        generator.emit("+", "3", "5", "t1");           // t1 = 3 + 5 (常量折叠)
        generator.emit("*", "t1", "1", "t2");          // t2 = t1 * 1 (代数化简)
        generator.emit("+", "t2", "0", "t3");          // t3 = t2 + 0 (代数化简)

        // 常量传播
        generator.emit("DECLARE", "int", "", "a");     // declare int a
        generator.emit("=", "t3", "", "a");            // a = t3
        generator.emit("+", "a", "2", "t4");           // t4 = a + 2 (常量传播后变成 8 + 2)

        // 死代码
        generator.emit("*", "10", "20", "t5");         // t5 = 10 * 20 (未使用，死代码)

        // 逻辑运算化简
        generator.emit("&&", "true", "a", "t6");       // t6 = true && a (代数化简)
        generator.emit("||", "false", "t6", "t7");     // t7 = false || t6 (代数化简)

        generator.emit("DECLARE", "int", "", "result");
        generator.emit("=", "t7", "", "result");       // result = t7

        System.out.println("优化前:");
        generator.printQuadruples();

        generator.optimize();

        System.out.println("\n优化后:");
        generator.printQuadruples();
        System.out.println();
    }

    /**
     * 测试用例7：while循环优化
     */
    public static void testWhileLoopOptimization() {
        System.out.println("=== 测试用例7：while循环优化 ===");

        IntermediateCodeGenerator generator = new IntermediateCodeGenerator();

        generator.emit("DECLARE", "int", "", "i");     // declare int i
        generator.emit("=", "0", "", "i");             // i = 0
        generator.emit("DECLARE", "int", "", "sum");   // declare int sum
        generator.emit("=", "0", "", "sum");           // sum = 0

        // while (i < 5) { sum = sum + i * 1; i = i + 1; }
        generator.emit("LABEL", "L1", "", "");         // L1: 循环开始
        generator.emit("<", "i", "5", "t1");           // t1 = i < 5
        generator.emit("JZ", "t1", "", "L2");          // if t1 == 0 goto L2

        // 循环体 - 包含可优化的表达式
        generator.emit("*", "i", "1", "t2");           // t2 = i * 1 (代数化简)
        generator.emit("+", "sum", "t2", "t3");        // t3 = sum + t2
        generator.emit("=", "t3", "", "sum");          // sum = t3

        generator.emit("+", "i", "1", "t4");           // t4 = i + 1
        generator.emit("=", "t4", "", "i");            // i = t4

        generator.emit("JMP", "", "", "L1");           // goto L1
        generator.emit("LABEL", "L2", "", "");         // L2: 循环结束

        System.out.println("优化前:");
        generator.printQuadruples();

        generator.optimize();

        System.out.println("\n优化后:");
        generator.printQuadruples();
        System.out.println();
    }

    /**
     * 运行所有测试用例
     */
    public static void runAllTests() {
        System.out.println("开始运行中间代码优化测试用例...\n");

        testConstantFolding();
        testConstantPropagation();
        testAlgebraicSimplification();
        testDeadCodeElimination();
        testJumpOptimization();
        testComprehensiveOptimization();
        testWhileLoopOptimization();

        System.out.println("所有测试用例运行完成！");
    }

    public static void main(String[] args) {
        runAllTests();
    }
}