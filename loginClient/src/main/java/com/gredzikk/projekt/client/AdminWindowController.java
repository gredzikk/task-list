package com.gredzikk.projekt.client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.*;

import java.sql.*;

public class AdminWindowController {
    public AdminWindowController(User client) {
        this.currentClient = client;
    }

    @FXML
    private Label errorLabel;
    @FXML
    private Button exitButton;
    User currentClient;
    Stage adminStage;
    private Connection conn;
    @FXML
    private TableView<User> usersTable;
    @FXML
    private TableColumn<User, String> loginColumn;
    @FXML
    private TableColumn<User, String> lastLoginColumn;
    @FXML
    ObservableList<User> userList = FXCollections.observableArrayList();

    public void setStage(Stage stage) {
        this.adminStage = stage;
        adminStage = (Stage) exitButton.getScene().getWindow();
        adminStage.setOnCloseRequest(event -> {
            try {
                disconnect();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void initialize() throws SQLException, IOException {

        sendStatus("AUT@" + currentClient.login);
        conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/projekt",
                currentClient.login, currentClient.hashedPassword);
        // Initialize the table columns
        loginColumn.setCellValueFactory(new PropertyValueFactory<>("login"));
        lastLoginColumn.setCellValueFactory(new PropertyValueFactory<>("lastLogin"));

        // Load data from the database into the table

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM users");
        while (rs.next()) {
            User user = new User(rs.getString("login"), rs.getString("last_login"));
            userList.add(user);
        }
        usersTable.setItems(userList);

        loginColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        lastLoginColumn.setCellFactory(TextFieldTableCell.forTableColumn());
    }

    @FXML
    public void onExitButton() throws IOException {
        disconnect();
    }

    public void sendStatus(String status) throws IOException {
        BufferedWriter clientToServer = new BufferedWriter(new OutputStreamWriter(currentClient.socket.getOutputStream()));
        BufferedReader serverToClient = new BufferedReader(new InputStreamReader(currentClient.socket.getInputStream()));

        JSONObject clientToServerStatus = new JSONObject();
        clientToServerStatus.put("status", status);
        clientToServer.write(clientToServerStatus.toString());
        clientToServer.newLine();
        clientToServer.flush();
    }

    public void disconnect() throws IOException {
        sendStatus("DIS@" + currentClient.login);
        currentClient.socket.close();
        adminStage.close();
    }
}
