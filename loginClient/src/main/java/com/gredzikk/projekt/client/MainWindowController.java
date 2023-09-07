package com.gredzikk.projekt.client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.*;
import java.sql.*;

public class MainWindowController {
    public MainWindowController(User client) {
        this.currentClient = client;
    }

    @FXML
    private TextField taskNameInputField;
    @FXML
    private TextField taskDescInputField;
    @FXML
    private Label errorLabel;

    @FXML
    private Button exitButton;
    User currentClient;
    Stage mainStage;
    @FXML
    private TableView<Task> tasksTable;
    @FXML
    private TableColumn<Task, String> nameColumn;
    @FXML
    private TableColumn<Task, String> noteColumn;
    @FXML
    private TableColumn<Task, Boolean> doneColumn;
    ObservableList<Task> taskList = FXCollections.observableArrayList();
    private Connection conn;

    public void setStage(Stage stage) {
        this.mainStage = stage;
        mainStage = (Stage) exitButton.getScene().getWindow();
        mainStage.setOnCloseRequest(event -> {
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
        //inicjowanie kolumn
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        noteColumn.setCellValueFactory(new PropertyValueFactory<>("note"));
        doneColumn.setCellValueFactory(new PropertyValueFactory<>("done"));
        tasksTable.setFixedCellSize(40.0);

        //zaladuj dane z bazy do tableview

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM " + currentClient.login + "_tasks");
        while (rs.next()) {
            Task task = new Task(rs.getString("name"), rs.getString("note"), rs.getBoolean("done"));
            taskList.add(task);
        }
        tasksTable.setItems(taskList);

        //aktywuj edycje
        tasksTable.setEditable(true);
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        noteColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        doneColumn.setCellFactory(CheckBoxTableCell.forTableColumn(doneColumn));

        //wprowadz zmiany w bazie przy zmianie zawartosci tabeli
        nameColumn.setOnEditCommit(event -> {
            Task task = event.getRowValue();
            removeTask(task);
            taskList.remove(task);
            task.setName(event.getNewValue());
            insertTask(task);
            taskList.add(task);
            try {
                sendStatus("EDI@" + currentClient.login);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        noteColumn.setOnEditCommit(event -> {
            Task task = event.getRowValue();
            removeTask(task);
            taskList.remove(task);
            task.setNote(event.getNewValue());
            insertTask(task);
            taskList.add(task);
            try {
                sendStatus("EDI@" + currentClient.login);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        doneColumn.setOnEditCommit(event -> {
            System.out.println("DONE EDIT");
            Task task = event.getRowValue();
            removeTask(task);
            taskList.remove(task);
            task.setDone(event.getNewValue());
            System.out.println("done: " + task.getDone());
            insertTask(task);
            taskList.add(task);
            try {
                sendStatus("EDI@" + currentClient.login);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void insertTask(Task task) {
        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO " + currentClient.login + "_tasks(name, note, done) VALUES (?, ?, ?)");
            stmt.setString(1, task.getName());
            stmt.setString(2, task.getNote());
            stmt.setBoolean(3, task.getDone());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removeTask(Task task) {
        String rem = "DELETE FROM " + currentClient.login + "_tasks WHERE name = '" + task.getName() + "'";
        try {
            PreparedStatement stmt = conn.prepareStatement(rem);
            stmt.executeUpdate();
            stmt.close();
            taskList.remove(task);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void onAddTaskButton() throws IOException, SQLException {
        if ((!taskNameInputField.getText().isEmpty())) {
            if ((taskNameInputField.getLength() < 100)) {
                if ((!taskDescInputField.getText().isEmpty())) {
                    Task task = new Task(taskNameInputField.getText(), taskDescInputField.getText(), false);
                    PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) AS count FROM " + currentClient.login + "_tasks WHERE name = ?");
                    stmt.setString(1, task.getName());
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next() && rs.getInt("count") > 0) {
                        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                        errorLabel.setText("Zadanie juz istnieje!");
                    } else {
                        taskList.add(task);
                        insertTask(task);
                        sendStatus("ADD@" + currentClient.login);
                        taskNameInputField.clear();
                        taskDescInputField.clear();
                        errorLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                        errorLabel.setText("Dodano zadanie!");
                    }
                } else {
                    errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    errorLabel.setText("Brak opisu!");
                }
            } else {
                errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                errorLabel.setText("Nazwa zbyt dluga!");
            }
        } else {
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            errorLabel.setText("Wprowadz nazwe!");
        }
    }

    @FXML
    public void onRemoveTaskButton() throws IOException {
        Task selectedTask = tasksTable.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            removeTask(selectedTask);
            sendStatus("REM@" + currentClient.login);
        }
        errorLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        errorLabel.setText("Zadanie usuniete");
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
        mainStage.close();
    }
}

