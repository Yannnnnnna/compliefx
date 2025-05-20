package com.example.compliefx2;

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
    private Button Check;

    @FXML
    private Button Help;

    @FXML
    private MenuItem openFile;

    @FXML
    private MenuItem saveFile;

    // 读取Token映射表（路径需根据实际环境调整）
    Map<String, Integer> tokenMap = TokenLibrary.readToken("C:\\Users\\WYR\\Desktop\\编译原理\\compliefx2\\src\\main\\resources\\com\\example\\compliefx2\\tokenTable.json");
    ManualLexer lexer;

    // 当前选中的语法分析类型（保留原有枚举用于界面交互）
    private SyntaxType currentSyntaxType = SyntaxType.STATEMENT;

    // 语法分析类型枚举
    public enum SyntaxType {
        ARITHMETIC,      // 算术表达式
        BOOLEAN,         // 布尔表达式
        ASSIGNMENT,      // 赋值表达式
        STATEMENT        // 语句
    }

    @FXML
    void LexicalAnalysisGenerate(ActionEvent event) {
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
            while ((token = lexer.nextToken()) != null){}

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
        // 中间代码生成功能的实现
    }

    @FXML  // 语法分析器
    void SyntaxAnalysisGenerate(ActionEvent event) {
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

            // 根据当前语法类型选择分析方法
            switch (currentSyntaxType) {
                case ARITHMETIC:
                case BOOLEAN:
                case ASSIGNMENT:
                    // 使用统一表达式分析器
                    result = SyntaxAnalyzer.analyzeUnifiedExpression(sourceCode, tokenMap);
                    break;
                case STATEMENT:
                    // 使用完整程序结构分析器
                    result = SyntaxAnalyzer.analyzeProgram(sourceCode, tokenMap);
                    break;
                default:
                    ErrorMsgPrint.setText("未知的语法分析类型");
                    return;
            }

            if (result != null) {
                ResultPrint.setText(result.getParseTree());
                if (result.isValid()) {
                    ErrorMsgPrint.setText("语法分析成功");
                } else {
                    ErrorMsgPrint.setText(result.getErrorMsg());
                }
            } else {
                ErrorMsgPrint.setText("语法分析结果为空");
            }

            System.out.println("语法分析结束");

        } catch (Exception e) {
            e.printStackTrace();
            ErrorMsgPrint.setText("语法分析出错: " + e.getMessage());
        }
    }

    @FXML
    void TargetCodeGenerate(ActionEvent event) {
        // 目标代码生成功能的实现
    }

    // 设置当前语法分析类型
    public void setSyntaxType(SyntaxType type) {
        this.currentSyntaxType = type;
    }
}