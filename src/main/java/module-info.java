module com.example.compliefx2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;
    requires json.simple;
    requires jdk.compiler;


    opens com.example.compliefx2 to javafx.fxml;
    exports com.example.compliefx2;
    exports com.example.compliefx2.intermediateCode;
    opens com.example.compliefx2.intermediateCode to javafx.fxml;
}