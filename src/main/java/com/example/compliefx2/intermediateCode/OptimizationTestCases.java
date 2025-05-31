package com.example.compliefx2.intermediateCode;

import com.example.compliefx2.intermediateCode.IntermediateCodeGenerator;

/**
 * 中间代码优化测试用例
 */
public class OptimizationTestCases {

    /**
     * 测试常量折叠优化
     */
    public static void testConstantFolding() {
        System.out.println("=== 测试常量折叠优化 ===");

        IntermediateCodeGenerator generator = new IntermediateCodeGenerator();

        // 生成测试四元式
        generator.emit("+", "3", "5", "t0");        // t0 = 3 + 5
        generator.emit("*", "4", "6", "t1");        // t1 = 4 * 6
        generator.emit("-", "10", "3", "t2");       // t2 = 10 - 3
        generator.emit("/", "20", "4", "t3");       // t3 = 20 / 4
        generator.emit("==", "5", "5", "t4");       // t4 = 5 == 5
        generator.emit("!=", "3", "7", "t5");       // t5 = 3 != 7
        generator.emit("<", "2", "8", "t6");        // t6 = 2 < 8

        System.out.println("优化前:");
        generator.printQuadruples();

        generator.optimize();

        System.out.println("\n优化后:");
        generator.printQuadruples();

        System.out.println("期望结果: 所有运算应该被计算为常量值\n");
    }

    /**
     * 测试常量传播优化
     */
    public static void testConstantPropagation() {
        System.out.println("=== 测试常量传播优化 ===");

        IntermediateCodeGenerator generator = new IntermediateCodeGenerator();

        // 生成测试四元式
        generator.emit("=", "10", "", "a");         // a = 10
        generator.emit("=", "20", "", "b");         // b = 20
        generator.emit("+", "a", "b", "t0");        // t0 = a + b
        generator.emit("*", "a", "3", "t1");        // t1 = a * 3
        generator.emit("-", "b", "5", "t2");        // t2 = b - 5
        generator.emit("=", "a", "", "c");          // c = a

        System.out.println("优化前:");
        generator.printQuadruples();

        generator.optimize();

        System.out.println("\n优化后:");
        generator.printQuadruples();

        System.out.println("期望结果: a和b的值应该被传播，并进行常量折叠\n");
    }

    /**
     * 测试代数化简优化
     */
    public static void testAlgebraicSimplification() {
        System.out.println("=== 测试代数化简优化 ===");

        IntermediateCodeGenerator generator = new IntermediateCodeGenerator();

        // 生成测试四元式
        generator.emit("+", "x", "0", "t0");        // t0 = x + 0
        generator.emit("+", "0", "y", "t1");        // t1 = 0 + y
        generator.emit("-", "z", "0", "t2");        // t2 = z - 0
        generator.emit("-", "a", "a", "t3");        // t3 = a - a
        generator.emit("*", "b", "1", "t4");        // t4 = b * 1
        generator.emit("*", "1", "c", "t5");        // t5 = 1 * c
        generator.emit("*", "d", "0", "t6");        // t6 = d * 0
        generator.emit("*", "0", "e", "t7");        // t7 = 0 * e
        generator.emit("/", "f", "1", "t8");        // t8 = f / 1
        generator.emit("/", "0", "g", "t9");        // t9 = 0 / g

        System.out.println("优化前:");
        generator.printQuadruples();

        generator.optimize();

        System.out.println("\n优化后:");
        generator.printQuadruples();

        System.out.println("期望结果: 应该进行代数化简，如 x+0=x, x*1=x, x*0=0 等\n");
    }

    /**
     * 测试综合优化
     */
    public static void testComprehensiveOptimization() {
        System.out.println("=== 测试综合优化 ===");

        IntermediateCodeGenerator generator = new IntermediateCodeGenerator();

        // 生成复杂的测试四元式
        generator.emit("=", "5", "", "a");          // a = 5
        generator.emit("=", "3", "", "b");          // b = 3
        generator.emit("+", "a", "0", "t0");        // t0 = a + 0  (应化简为 a)
        generator.emit("*", "b", "1", "t1");        // t1 = b * 1  (应化简为 b)
        generator.emit("+", "t0", "t1", "t2");      // t2 = t0 + t1 (应传播为 a + b)
        generator.emit("*", "2", "4", "t3");        // t3 = 2 * 4  (应折叠为 8)
        generator.emit("-", "t2", "t3", "t4");      // t4 = t2 - t3
        generator.emit("/", "t4", "1", "t5");       // t5 = t4 / 1 (应化简为 t4)

        System.out.println("优化前:");
        generator.printQuadruples();

        generator.optimize();

        System.out.println("\n优化后:");
        generator.printQuadruples();

        System.out.println("期望结果: 综合应用所有优化技术\n");
    }

    /**
     * 测试边界情况
     */
    public static void testEdgeCases() {
        System.out.println("=== 测试边界情况 ===");

        IntermediateCodeGenerator generator = new IntermediateCodeGenerator();

        // 除零情况
        generator.emit("/", "10", "0", "t0");       // t0 = 10 / 0 (不应优化)
        generator.emit("%", "5", "0", "t1");        // t1 = 5 % 0  (不应优化)

        // 浮点数计算
        generator.emit("+", "3.14", "2.86", "t2");  // t2 = 3.14 + 2.86
        generator.emit("*", "1.5", "2.0", "t3");    // t3 = 1.5 * 2.0

        // 字符串常量
        generator.emit("=", "hello", "", "s1");     // s1 = "hello"
        generator.emit("=", "s1", "", "s2");        // s2 = s1

        System.out.println("优化前:");
        generator.printQuadruples();

        generator.optimize();

        System.out.println("\n优化后:");
        generator.printQuadruples();

        System.out.println("期望结果: 除零不优化，浮点数正确计算，字符串常量传播\n");
    }

    /**
     * 测试布尔表达式优化
     */
    public static void testBooleanOptimization() {
        System.out.println("=== 测试布尔表达式优化 ===");

        IntermediateCodeGenerator generator = new IntermediateCodeGenerator();

        // 布尔常量
        generator.emit("=", "true", "", "bool1");   // bool1 = true
        generator.emit("=", "false", "", "bool2");  // bool2 = false

        // 比较运算
        generator.emit("==", "true", "true", "t0"); // t0 = true == true
        generator.emit("!=", "false", "true", "t1");// t1 = false != true
        generator.emit("<", "1", "2", "t2");        // t2 = 1 < 2
        generator.emit(">=", "5", "3", "t3");       // t3 = 5 >= 3

        // 使用布尔变量
        generator.emit("==", "bool1", "true", "t4");// t4 = bool1 == true

        System.out.println("优化前:");
        generator.printQuadruples();

        generator.optimize();

        System.out.println("\n优化后:");
        generator.printQuadruples();

        System.out.println("期望结果: 布尔常量应该被正确传播和计算\n");
    }

    /**
     * 运行所有测试
     */
    public static void runAllTests() {
        System.out.println("开始运行代码优化测试用例...\n");

        testConstantFolding();
        testConstantPropagation();
        testAlgebraicSimplification();
        testComprehensiveOptimization();
        testEdgeCases();
        testBooleanOptimization();

        System.out.println("所有测试用例执行完毕！");
    }

    /**
     * 主方法
     */
    public static void main(String[] args) {
        runAllTests();
    }
}