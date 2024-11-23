package org.example.taskapp.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
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

public class AppController {
    @FXML
    private DatePicker startDate;
    @FXML
    private DatePicker endDate;
    @FXML
    private ChoiceBox<String> choiceStatus;
    @FXML
    private TextField taskId;
    @FXML
    private ListView<String> tasksList;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private Label loginUser;
    @Getter
    @Setter
    private String userID;
    private DatabaseManager databaseManager;
    private static final Logger logger = LoggerConfig.getLogger();


    public AppController() {
        this.databaseManager = new DatabaseManager();

    }

    public void initialize() {
        ObservableList<String> status = FXCollections.observableArrayList("выполнено", "незавершено");
        choiceStatus.setItems(status);

        choiceStatus.setOnAction(actionEvent -> {
            String selectedStatus = choiceStatus.getSelectionModel().getSelectedItem();
            System.out.println("Выбран статус: " + selectedStatus);
        });
    }

    public void setUserId(String userId) {
        this.userID = userId;
        System.out.println("Идентификатор пользователя: " + userID);

        if (userID != null) {
            showLoginUser();
            showTasks();
        }
    }

    private void showLoginUser() {
        String query = "SELECT Login FROM Users WHERE UserId = ?";
        Connection connection = null;

        try {
            connection = databaseManager.getConnection();
            if (connection == null) {
                logger.severe("Не удалось установить соединение с базой данных");
                return;
            }

            connection = databaseManager.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            int userIdInt = Integer.parseInt(userID);
            preparedStatement.setInt(1, userIdInt);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    loginUser.setText(resultSet.getString("Login"));
                    logger.info("Пользователь найден: " + resultSet.getString("Login"));
                } else {
                    logger.warning("Ошибка: пользователь с таким ID не найден");
                }
            }
        } catch (SQLException e) {
            logger.severe("Ошибка базы данных: " + e.getMessage());
        }
    }

    private void showTasks() {
        String query = "SELECT TaskId, Title, Decrtiption, Date, Priority, Status FROM Tasks WHERE UserId = ?";
        ObservableList<String> tasks = FXCollections.observableArrayList();

        try (Connection connection = databaseManager.getConnection()) {
            if (connection == null) {
                logger.severe("Не удалось установить подключение к базе данных");
                return;
            }

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            int userIdInt = Integer.parseInt(userID);
            preparedStatement.setInt(1, userIdInt);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String tasksInfo = "Id: " + resultSet.getInt("TaskId") + "\n" +
                            "Название: " + resultSet.getString("Title") + "\n" +
                            "Описание: " + resultSet.getString("Decrtiption") + "\n" +
                            "Дата: " + resultSet.getString("Date") + "\n" +
                            "Приоритет: " + resultSet.getString("Priority") + "\n" +
                            "Статус: " + resultSet.getString("Status");
                    tasks.add(tasksInfo);
                }
            }
        } catch (SQLException e) {
            logger.severe("Ошибка базы данных: " + e.getMessage());
        }
        tasksList.setItems(tasks);
    }

    public void goToSettings(MouseEvent mouseEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/taskapp/settings-user-view.fxml"));
        Parent root = fxmlLoader.load();

        SettingsUserController settingsUserController = fxmlLoader.getController();
        settingsUserController.setUserId(userID);

        Stage stage = (Stage) anchorPane.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void editTask(ActionEvent actionEvent) throws IOException {
        String taskIdValue = taskId.getText();
        if (taskIdValue == null || taskIdValue.isEmpty()) {
            logger.warning("Id не введён");
            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/taskapp/edit-task-view.fxml"));
        Parent root = fxmlLoader.load();

        EditTaskController editTaskController = fxmlLoader.getController();
        editTaskController.setUserId(userID);
        editTaskController.setTaskId(taskIdValue);

        Stage stage = (Stage) anchorPane.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void delTask(ActionEvent actionEvent) {
        String query = "DELETE FROM Tasks WHERE TaskId = ?";
        Connection connection = null;

        try {
            connection = databaseManager.getConnection();

            if (connection == null) {
                logger.severe("Ошибка подключения к базе данных");
                return;
            }
            int taskIdInt = Integer.parseInt(taskId.getText());

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                preparedStatement.setInt(1, taskIdInt);
                int rowsAffects = preparedStatement.executeUpdate();

                if (rowsAffects > 0) {
                    logger.info("Задача успешно удалена");
                    refreshTaskList();
                } else {
                    logger.warning("Задача с таким ID не найдена");
                }
            }
        } catch (SQLException e) {
            logger.severe("Ошибка базы данных");
        } catch (NumberFormatException e) {
            logger.warning("Ошибка: введён некорректный ID");
        }
    }

    private void refreshTaskList() {
        showTasks();
    }

    public void addTask(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/taskapp/add-task-view.fxml"));
        Parent root = fxmlLoader.load();

        AddTaskController addTaskController = fxmlLoader.getController();
        addTaskController.setUserId(userID);

        Stage stage = (Stage) anchorPane.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void onSearchBtnClick(ActionEvent actionEvent) {
        String selectedStatus = choiceStatus.getSelectionModel().getSelectedItem();

        if (selectedStatus == null || selectedStatus.isEmpty()) {
            logger.warning("Ошибка: Пожалуйста, выберите статус для поиска");
            tasksList.setItems(FXCollections.observableArrayList("Нет задач для отображения."));
            return;
        }

        String query = "SELECT TaskId, Title, Decrtiption, Date, Priority, Status FROM Tasks WHERE Status = ? AND UserId = ?";
        int userIdInt = Integer.parseInt(userID);
        ObservableList<String> tasks = FXCollections.observableArrayList();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            if (connection == null) {
                logger.severe("Не удалось установить подключение к базе данных");
                return;
            }

            preparedStatement.setString(1, selectedStatus);
            preparedStatement.setInt(2, userIdInt);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String taskInfo = "Id: " + resultSet.getInt("TaskId") + "\n" +
                            "Название: " + resultSet.getString("Title") + "\n" +
                            "Описание: " + resultSet.getString("Decrtiption") + "\n" +
                            "Дата: " + resultSet.getString("Date") + "\n" +
                            "Приоритет: " + resultSet.getString("Priority") + "\n" +
                            "Статус: " + resultSet.getString("Status");
                    tasks.add(taskInfo);
                }
            }

            if (tasks.isEmpty()) {
                tasks.add("Нет задач для выбранного статуса.");
            }

            tasksList.setItems(tasks);
        } catch (SQLException e) {
            logger.severe("Ошибка базы данных: " + e.getMessage());
        }
    }

    public void buildReport(ActionEvent actionEvent) {
        PieChart pieChart = new PieChart();
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        String query = "SELECT Priority, COUNT(*) AS Count " +
                "FROM Tasks WHERE Status = 'выполнено' AND UserId = ? " +
                "AND Date BETWEEN ? AND ? GROUP BY Priority";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, Integer.parseInt(userID));

            java.sql.Date start = java.sql.Date.valueOf(startDate.getValue());
            java.sql.Date end = java.sql.Date.valueOf(endDate.getValue());

            preparedStatement.setDate(2, start);
            preparedStatement.setDate(3, end);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String priority = resultSet.getString("Priority");
                    int count = resultSet.getInt("Count");
                    pieData.add(new PieChart.Data(priority, count));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        pieChart.setData(pieData);
        pieChart.setTitle("Задачи по приоритету");

        anchorPane.getChildren().clear();
        anchorPane.getChildren().add(pieChart);

        AnchorPane.setTopAnchor(pieChart, 50.0);
        AnchorPane.setLeftAnchor(pieChart, 50.0);
        AnchorPane.setRightAnchor(pieChart, 50.0);
        AnchorPane.setBottomAnchor(pieChart, 50.0);

    }
}

