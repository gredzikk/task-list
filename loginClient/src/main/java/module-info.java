module com.example.javafxzad1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;
    requires java.sql;


    opens com.gredzikk.projekt.client to javafx.fxml;
    exports com.gredzikk.projekt.client;
}