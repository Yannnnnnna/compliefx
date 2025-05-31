package com.example.compliefx2;

import com.example.compliefx2.intermediateCode.IntermediateCodeGenerator;
import com.example.compliefx2.intermediateCode.Quadruple;
import com.example.compliefx2.semanticAnalyzer.SemanticAnalyzer;
import com.example.compliefx2.syntaxAnalyzer.StatementParser;
import com.example.compliefx2.syntaxAnalyzer.SyntaxAnalyzer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class Text {

    @FXML
    private TextArea ResultPrint;

    @FXML
    private TextArea ErrorMsgPrint;

    @FXML
    private Label SourceProgram;

    @FXML
    private TextArea SourceProgramRead;

    @FXML
    private Button Edit;

    @FXML
    private Button LexicalAnalysis;

    @FXML
    private Button SyntaxAnalysis;

    @FXML
    private Button TargetCode;

    @FXML
    private Button IntermediateCode;

    @FXML
    private Button CodeOptimization;

    @FXML
    private MenuItem openFile;

    @FXML
    private MenuItem saveFile;

    @FXML
    private Button SemanticAnalyzer;

    // 目标代码输出目录
    private static final String OUTPUT_DIRECTORY = "C:\\Users\\WYR\\Desktop\\编译原理\\compliefx2\\src\\main\\resources\\result";

    // 读取Token映射表（路径需根据实际环境调整）
    Map<String, Integer> tokenMap = TokenLibrary.readToken("C:\\Users\\WYR\\Desktop\\编译原理\\compliefx2\\src\\main\\resources\\com\\example\\compliefx2\\tokenTable.json");
    ManualLexer lexer;
    private IntermediateCodeGenerator intermediateCodeGenerator; // 保存中间代码生成器实例
    private boolean isOptimized = false; // 标记是否已执行优化

    // 当前选中的语法分析类型（保留原有枚举用于界面交互）
    private SyntaxType currentSyntaxType = SyntaxType.STATEMENT;
    ASTNode astNode;

    // 语法分析类型枚举
    public enum SyntaxType {
        ARITHMETIC,      // 算术表达式
        BOOLEAN,         // 布尔表达式
        ASSIGNMENT,      // 赋值表达式
        STATEMENT        // 语句
    }

    @FXML
    void CodeOptimizationGenerate(ActionEvent event) {
        // 清空输出文本框
        clearOutputTextAreas();

        // 检查是否已生成中间代码
        if (intermediateCodeGenerator == null) {
            ErrorMsgPrint.setText("请先生成中间代码");
            return;
        }

        try {
            // 获取优化前的代码
            String originalCode = intermediateCodeGenerator.getQuadruplesAsString();

            // 执行优化
            intermediateCodeGenerator.optimize();
            String optimizedCode = intermediateCodeGenerator.getQuadruplesAsString();

            // 标记已优化
            isOptimized = true;

            // 显示优化前后的对比
            ResultPrint.setText("=== 优化前中间代码 ===\n" + originalCode +
                    "\n=== 优化后中间代码 ===\n" + optimizedCode);

            ErrorMsgPrint.setText("代码优化完成");

        } catch (Exception e) {
            e.printStackTrace();
            ErrorMsgPrint.setText("代码优化错误: " + e.getMessage());
        }
    }

    @FXML
    void LexicalAnalysisGenerate(ActionEvent event) {
        // 清空输出文本框
        clearOutputTextAreas();

        System.out.println("调用词法分析器");
        String sourceCode = SourceProgramRead.getText();
        performLexicalAnalysis(sourceCode);
        System.out.println(sourceCode);
    }

    // 具体词法分析器的实现
    private void performLexicalAnalysis(String sourceCode) {
        System.out.println("开始分析");
        try {
            StringReader reader = new StringReader(sourceCode);
            lexer = new ManualLexer(reader, tokenMap);

            Token token;
            while ((token = lexer.nextToken()) != null) {
            }

            String result = lexer.getResult();
            String error = lexer.getError();

            ResultPrint.setText(result);
            ErrorMsgPrint.setText(error);
            System.out.println("分析结束，输出结果");
        } catch (Exception e) {
            e.printStackTrace();
            ErrorMsgPrint.setText("词法分析出错: " + e.getMessage());
        }
    }

    @FXML
    void openFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        Window stage = SourceProgramRead.getScene().getWindow();

        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(selectedFile));
                String line;
                StringBuilder stringBuilder = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                reader.close();

                SourceProgramRead.setText(stringBuilder.toString());
            } catch (IOException e) {
                e.printStackTrace();
                ErrorMsgPrint.setText("打开文件出错: " + e.getMessage());
            }
        }
    }

    @FXML
    void saveFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        Window stage = SourceProgramRead.getScene().getWindow();

        File selectedFile = fileChooser.showSaveDialog(stage);

        if (selectedFile != null) {
            try {
                FileWriter writer = new FileWriter(selectedFile);
                writer.write(SourceProgramRead.getText());
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
                ErrorMsgPrint.setText("保存文件出错: " + e.getMessage());
            }
        }
    }

    @FXML
    void IntermediateCodeGenerate(ActionEvent event) {
        // 清空输出文本框
        clearOutputTextAreas();

        // 中间代码生成功能的实现
        if (astNode == null) {
            ErrorMsgPrint.setText("请先执行语法分析以生成AST");
            return;
        }

        try {
            intermediateCodeGenerator = new IntermediateCodeGenerator();
            intermediateCodeGenerator.generateCode(astNode); // 生成中间代码

            // 重置优化标记
            isOptimized = false;

            String originalCode = intermediateCodeGenerator.getQuadruplesAsString();

            // 只显示原始中间代码，不执行优化
            ResultPrint.setText("=== 中间代码 ===\n" + originalCode);
            ErrorMsgPrint.setText("中间代码生成完成");

        } catch (Exception e) {
            e.printStackTrace();
            ErrorMsgPrint.setText("中间代码生成错误: " + e.getMessage());
        }
    }

    @FXML
        // 语法分析器
    void SyntaxAnalysisGenerate(ActionEvent event) {
        // 清空输出文本框
        clearOutputTextAreas();

        System.out.println("调用语法分析器");
        String sourceCode = SourceProgramRead.getText();
        performSyntaxAnalysis(sourceCode);
    }

    /**
     * 根据当前选择的语法分析类型执行相应的分析
     */
    private void performSyntaxAnalysis(String sourceCode) {
        System.out.println("开始语法分析");
        try {
            SyntaxAnalyzer.AnalysisResult result = null;
            StatementParser parser = new StatementParser(new StringReader(sourceCode), tokenMap);
            boolean isValid = parser.parseProgram();

            if (isValid) {
                astNode = parser.getAST();
                if (astNode != null) {
                    System.out.println("AST 结构：");
                    ResultPrint.setText(astNode.toString());
                    System.out.println(astNode.toString());
                }

                System.out.println("语法分析结束");
            } else {
                System.out.println("语法分析失败");
                ErrorMsgPrint.setText("语法分析出错: " + parser.getErrorMsg());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void TargetCodeGenerate(ActionEvent event) {
        // 清空输出文本框
        clearOutputTextAreas();

        // 目标代码生成功能的实现
        if (intermediateCodeGenerator == null) {
            ErrorMsgPrint.setText("请先生成中间代码");
            return;
        }

        try {
            // 根据是否优化选择使用的四元式
            List<Quadruple> quadruples = intermediateCodeGenerator.getQuadruples();

            // 生成目标代码
            CodeGenerator codeGen = new CodeGenerator();
            codeGen.setQuadruples(quadruples);
            String assemblyCode = codeGen.generateAssembly();

            // 显示汇编代码，标明使用的是哪种代码
            String codeType = isOptimized ? "优化后" : "原始";
            ResultPrint.setText("=== 基于" + codeType + "中间代码生成的8086汇编代码 ===\n" + assemblyCode);

            // 让用户选择保存位置和文件名
            String savedFilePath = saveAssemblyToFileWithDialog(assemblyCode, codeType);
            if (savedFilePath != null) {
                ErrorMsgPrint.setText("目标代码生成成功（基于" + codeType + "中间代码），已保存到: " + savedFilePath);
            } else {
                ErrorMsgPrint.setText("目标代码生成成功，用户取消保存或保存失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ErrorMsgPrint.setText("目标代码生成错误: " + e.getMessage());
        }
    }

    /**
     * 清空输出文本框的通用方法
     */
    private void clearOutputTextAreas() {
        ResultPrint.clear();
        ErrorMsgPrint.clear();
    }

    /**
     * 通过文件对话框让用户选择保存位置和文件名来保存汇编代码
     * @param assemblyCode 汇编代码内容
     * @param codeType 代码类型标识（原始或优化后）
     * @return 保存的文件路径，如果用户取消或保存失败返回null
     */
    private String saveAssemblyToFileWithDialog(String assemblyCode, String codeType) {
        try {
            // 创建文件选择器
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("保存汇编代码文件");

            // 设置文件扩展名过滤器
            FileChooser.ExtensionFilter asmFilter = new FileChooser.ExtensionFilter("汇编文件 (*.asm)", "*.asm");
            FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("所有文件 (*.*)", "*.*");
            fileChooser.getExtensionFilters().addAll(asmFilter, allFilter);
            fileChooser.setSelectedExtensionFilter(asmFilter);

            // 设置默认保存目录（如果目录存在）
            File defaultDir = new File(OUTPUT_DIRECTORY);
            if (defaultDir.exists() && defaultDir.isDirectory()) {
                fileChooser.setInitialDirectory(defaultDir);
            }

            // 设置默认文件名，包含代码类型信息
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            String timestamp = now.format(formatter);
            String filePrefix = codeType.equals("优化后") ? "optimized_target_code_" : "target_code_";
            fileChooser.setInitialFileName(filePrefix + timestamp + ".asm");

            // 获取当前窗口
            Window stage = SourceProgramRead.getScene().getWindow();

            // 显示保存对话框
            File selectedFile = fileChooser.showSaveDialog(stage);

            if (selectedFile != null) {
                // 确保文件扩展名为.asm
                String fileName = selectedFile.getName();
                if (!fileName.toLowerCase().endsWith(".asm")) {
                    selectedFile = new File(selectedFile.getParent(), fileName + ".asm");
                }

                // 创建文件内容（添加注释头）
                StringBuilder content = new StringBuilder();
                content.append("; Generated Assembly Code (").append(codeType).append("中间代码)\n");
                content.append("; Generated at: ").append(now.toString()).append("\n");
                content.append("; Compiler: ComplieFX2\n");
                content.append("; File: ").append(selectedFile.getName()).append("\n");
                content.append(";\n\n");
                content.append(assemblyCode);

                // 使用 OutputStreamWriter 指定编码为 GB18030
                try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(selectedFile), "GB18030")) {
                    writer.write(content.toString());
                }

                System.out.println("汇编代码已保存到: " + selectedFile.getAbsolutePath());
                return selectedFile.getAbsolutePath();
            } else {
                System.out.println("用户取消了文件保存");
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("保存汇编文件时出错: " + e.getMessage());
            return null;
        }
    }

    // 设置当前语法分析类型
    public void setSyntaxType(SyntaxType type) {
        this.currentSyntaxType = type;
    }

    @FXML
    void SemanticAnalyzerGenerate(ActionEvent event) {
        // 清空输出文本框
        clearOutputTextAreas();

        System.out.println("语法分析通过，开始语义分析...");

        // Step 2: 语义分析
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
        boolean semanticValid = semanticAnalyzer.analyze(astNode);

        // 获取分析结果字符串
        String analysisResult = semanticAnalyzer.getAnalysisResult();

        if (semanticValid) {
            System.out.println("✓ 语义分析通过");
            // Step 3: 输出结果到界面
            ResultPrint.setText(analysisResult + "\n\n✓ 语义分析通过");
        } else {
            System.out.println("✗ 语义分析发现错误");
            // 显示分析结果（包含错误信息）
            ResultPrint.setText(analysisResult + "\n\n✗ 语义分析发现错误");
        }
    }
}