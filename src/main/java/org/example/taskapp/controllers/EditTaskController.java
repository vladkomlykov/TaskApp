package org.example.taskapp.controllers;

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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

public class EditTaskController {
    @FXML
    private Button backBtn;
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
    @FXML
    private AnchorPane anchorPane;
    @Setter
    @Getter
    private String userId;
    @Getter
    @Setter
    private String taskId;
    private DatabaseManager databaseManager;
    private static final Logger logger = LoggerConfig.getLogger();

    public EditTaskController(){
        this.databaseManager = new DatabaseManager();
    }

    public void initialize(){
        priorityTask.setItems(javafx.collections.FXCollections.observableArrayList("низкий", "средний", "высокий"));
        statusTask.setItems(javafx.collections.FXCollections.observableArrayList("выполнено", "незавершено"));

        loadTaskData();
    }

    private void loadTaskData() {
        if (taskId == null || taskId.isEmpty()) {
            errMessage.setText("Ошибка: Задача не найдена.");
            errMessage.setTextFill(Color.RED);
            errMessage.setVisible(true);
            logger.warning("Ошибка: Задача не найдена");
            return;
        }

        String query = "SELECT Title, Decription, Priority, Status FROM Tasks WHERE TaskId = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, Integer.parseInt(taskId));

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    titleTask.setText(resultSet.getString("Title"));
                    decriptionTask.setText(resultSet.getString("Decription"));
                    priorityTask.setValue(resultSet.getString("Priority"));
                    statusTask.setValue(resultSet.getString("Status"));
                } else {
                    errMessage.setText("Ошибка: Задача не найдена.");
                    errMessage.setTextFill(Color.RED);
                    errMessage.setVisible(true);
                    logger.warning("Ошибка: Задача не найдена");
                }
            }
        } catch (SQLException e) {
            errMessage.setText("Ошибка базы данных: " + e.getMessage());
            errMessage.setTextFill(Color.RED);
            errMessage.setVisible(true);
            logger.severe("Ошибка базы данных" + e.getMessage());
            e.printStackTrace();
        }
    }

    public void onSaveBtnClick(ActionEvent actionEvent) {
        StringBuilder query = new StringBuilder("UPDATE Tasks SET ");
        boolean firstField = true;

        if (!titleTask.getText().isEmpty()) {
            query.append("Title = ?");
            firstField = false;
        }
        if (!decriptionTask.getText().isEmpty()){
            query.append("Decription = ?");
            firstField = false;
        }
        if (priorityTask.getValue() != null){
            if (!firstField) query.append(", ");
            query.append("Priority = ?");
            firstField = false;
        }
        if (statusTask.getValue() != null){
            if (!firstField) query.append(", ");
            query.append("Status = ?");
            firstField = false;
        }

        if (firstField) {
            errMessage.setText("Пожалуйста, заполните хотя бы одно поле для обновления.");
            return;
        }

        query.append(" WHERE TaskId = ?");

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query.toString())) {

            int paramIndex = 1;

            if (!titleTask.getText().isEmpty()){
                preparedStatement.setString(paramIndex++,titleTask.getText());
            }
            if (!decriptionTask.getText().isEmpty()){
                preparedStatement.setString(paramIndex++,decriptionTask.getText());
            }
            if (priorityTask.getValue() != null){
                preparedStatement.setString(paramIndex++,priorityTask.getValue());
            }
            if (statusTask.getValue() != null){
                preparedStatement.setString(paramIndex++,statusTask.getValue());
            }

            preparedStatement.setInt(paramIndex, Integer.parseInt(taskId));

            int rowsAffect = preparedStatement.executeUpdate();
            if (rowsAffect > 0){
                errMessage.setText("Задача успешно обновлена");
                errMessage.setTextFill(Color.GREEN);
                errMessage.setVisible(true);
                backBtn.setVisible(true);
                logger.info("Задача успешно обновлена");
            } else {
                errMessage.setText("Не удалось обновить задачу");
                errMessage.setTextFill(Color.RED);
                errMessage.setVisible(true);
                logger.warning("Не удалось обновить задачу");
            }
        } catch (SQLException e){
            logger.severe("Ошибка базы данных " + e.getMessage());

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
