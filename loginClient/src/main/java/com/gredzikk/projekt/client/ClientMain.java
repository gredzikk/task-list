package com.gredzikk.projekt.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientMain extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ClientMain.class.getResource("login-window.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 500, 500);
        stage.setTitle("Logowanie do listy zadań");
        stage.setScene(scene);
        stage.show();
        LoginWindowController lwc = fxmlLoader.getController();
        lwc.setMyStage(stage);
    }

    public static void main(String[] args) {
        launch();
    }
}