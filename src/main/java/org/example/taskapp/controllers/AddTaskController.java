package org.example.taskapp.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.example.taskapp.LoggerConfig;
import org.example.taskapp.database.DatabaseManager;

import java.io.IOException;
import java.sql.*;
import java.time.LocalTime;
import java.util.logging.Logger;

public class AddTaskController {
    @FXML
    private Button backBtn;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private Label errMessage;
    @FXML
    private ChoiceBox<String> statusTask;
    @FXML
    private ChoiceBox<String> priorityTask;
    @FXML
    private TextField decriptionTask;
    @FXML
    private TextField titleTask;
    @Setter
    @Getter
    private String userId;
    private DatabaseManager databaseManager;
    private static final Logger logger = LoggerConfig.getLogger();

    public AddTaskController(){
        databaseManager = new DatabaseManager();
    }

    public void initialize(){
        ObservableList<String> priority = FXCollections.observableArrayList("низкий", "средний", "высокий");
        priorityTask.setItems(priority);

        priorityTask.setOnAction(actionEvent -> {
            String selectedPriority = priorityTask.getSelectionModel().getSelectedItem();
            logger.info("Выбран приоритет: " + selectedPriority);
        });

        ObservableList<String> status = FXCollections.observableArrayList("выполнено", "незавершено");
        statusTask.setItems(status);

        statusTask.setOnAction(actionEvent -> {
            String selectedStatus = statusTask.getSelectionModel().getSelectedItem();
            logger.info("Выбран статус: " + selectedStatus);
        });
    }

   public void onSaveBtnClick(ActionEvent actionEvent) {
        String query = "INSERT INTO Tasks(Title, Decrtiption, Date, Priority, Status, UserId) VALUES(?, ?, ?, ?, ?, ?)";
       java.sql.Timestamp timestamp = java.sql.Timestamp.valueOf(java.time.LocalDateTime.now());
       int userIdInt = Integer.parseInt(userId);

        Connection connection = null;

        try {
            connection = databaseManager.getConnection();

            if (connection == null){
                logger.severe("Не удалось установить подключение к базе данных");
                return;
            }
            try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, titleTask.getText());
                preparedStatement.setString(2, decriptionTask.getText());
                preparedStatement.setTimestamp(3, timestamp);
                preparedStatement.setString(4, priorityTask.getSelectionModel().getSelectedItem());
                preparedStatement.setString(5, statusTask.getSelectionModel().getSelectedItem());
                preparedStatement.setInt(6, userIdInt);

                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected > 0){
                    errMessage.setText("Задача успешно сохранена");
                    errMessage.setTextFill(Color.GREEN);
                    errMessage.setVisible(true);
                    backBtn.setVisible(true);
                    logger.info("Задача успешно сохранена");
                } else {
                    errMessage.setText("Ошибка при сохранении задачи");
                    errMessage.setTextFill(Color.RED);
                    errMessage.setVisible(true);
                    logger.warning("Ошибка при сохранении задачи");
                }
            }
        } catch (SQLException e){
            logger.severe("Ошибка быза данных: " + e.getMessage());
        }
    }

    public void onBackBtnClick(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/taskapp/app-view.fxml"));
        Parent root = fxmlLoader.load();

        AppController appController = fxmlLoader.getController();
        appController.setUserId(userId);

        Stage stage = (Stage) anchorPane.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();

    }
}
