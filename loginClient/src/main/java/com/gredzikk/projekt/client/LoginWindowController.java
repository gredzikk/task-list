package com.gredzikk.projekt.client;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.json.JSONObject;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class LoginWindowController {
    private Stage myStage;
    @FXML
    private TextField loginField;
    @FXML
    protected PasswordField passwordField;
    @FXML
    protected Label errorLabel;
    @FXML
    private Button loginButton;
    String password;
    String login;
    Socket socket;
    boolean authSuccess;

    public Stage getMyStage() {
        return myStage;
    }

    public void setMyStage(Stage myStage) {
        this.myStage = myStage;
    }

    @FXML
    protected void onLoginButtonClick() throws IOException, NoSuchAlgorithmException {
        errorLabel.setText("");
        if (loginField.getText().isEmpty()) {
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            errorLabel.setText("Pole login nie może być puste");
        } else if (passwordField.getText().isEmpty()) {
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            errorLabel.setText("Pole hasło nie może być puste");
        } else {
            login = loginField.getText();
            password = passwordField.getText();
            errorLabel.setText("");
//            System.out.println("LOGIN: Login: " + loginField.getText() + ", haslo: " + passwordField.getText());
//            System.out.println(password.hashCode());

            socket = new Socket("localhost", 5000);
            BufferedWriter clientToServer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader serverToClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            JSONObject credentials = new JSONObject();
            credentials.put("login", login);
            String hashPass;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            hashPass = hexString.toString();
            credentials.put("password", hashPass);

//            System.out.println("LOGIN: hashed password is " + hashPass + ", length of " + hashPass.length());

            clientToServer.write(credentials.toString());
            clientToServer.newLine();
            clientToServer.flush();

            String sr = serverToClient.readLine();
            JSONObject serverToClientData = new JSONObject(sr);
            authSuccess = serverToClientData.getBoolean("auth");

            if (authSuccess) {
                User clientData = new User();
                String auth = serverToClient.readLine();
                JSONObject authorized = new JSONObject(auth);
                String ll = authorized.getString("last");
                clientData.setLastLogin(ll);
                clientData.setLogin(login);
                clientData.setPassword(password);
                clientData.setSocket(socket);
                clientData.setHashedPassword(hashPass);
                errorLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                errorLabel.setText("Zalogowano pomyslnie");

                errorLabel.setText("");
                loginField.setText("");
                passwordField.setText("");
                
                showMainWindow(clientData);
            } else {
                errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                errorLabel.setText("Bledne haslo");
            }
        }
    }

    public void showMainWindow(User userData) throws IOException {
        System.out.println("MAIN: entry point");
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main-window.fxml"));
        MainWindowController mwc = new MainWindowController(userData);
        fxmlLoader.setControllerFactory(c -> mwc);
        System.out.println("MAIN: fxml main loader: " + mwc);
        Stage mainWindow = new Stage();
        mainWindow.setTitle("Zadania użytkownika " + userData.getLogin());
        mainWindow.setScene(new Scene(fxmlLoader.load(), 900, 600));
        mwc.setStage(mainWindow);
        mainWindow.show();
    }

    public void showAdminPanel(User userData) throws IOException {
        System.out.println("ADMIN: entry point");
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("admin-window.fxml"));
        AdminWindowController awc = new AdminWindowController(userData);
        fxmlLoader.setControllerFactory(c -> awc);
        System.out.println("ADMIN: fxml main loader: " + awc);
        Stage mainWindow = new Stage();
        mainWindow.setTitle("Panel administratora");
        mainWindow.setScene(new Scene(fxmlLoader.load(), 800, 600));
        awc.setStage(mainWindow);
        mainWindow.show();
    }

    public void sendStatus(String status) throws IOException {
        BufferedWriter clientToServer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader serverToClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        JSONObject clientToServerStatus = new JSONObject();
        clientToServerStatus.put("status", status);
        clientToServer.write(clientToServerStatus.toString());
        clientToServer.newLine();
        clientToServer.flush();
    }

}