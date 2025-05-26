package com.example.compliefx2.intermediateCode;

/**
 * 四元式类
 */
public class Quadruple {
    private String op;      // 操作符
    private String arg1;    // 第一个操作数
    private String arg2;    // 第二个操作数
    private String result;  // 结果

    public Quadruple(String op, String arg1, String arg2, String result) {
        this.op = op;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.result = result;
    }

    // Getter方法
    public String getOp() {
        return op;
    }

    public String getArg1() {
        return arg1;
    }

    public String getArg2() {
        return arg2;
    }

    public String getResult() {
        return result;
    }

    public void setOp(String op) {
        this.op = op;
    }

    // Setter方法（用于回填）
    public void setArg1(String arg1) {
        this.arg1 = arg1;
    }

    public void setArg2(String arg2) {
        this.arg2 = arg2;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return String.format("(%s, %s, %s, %s)", op, arg1, arg2, result);
    }
}
