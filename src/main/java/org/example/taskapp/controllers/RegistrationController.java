package org.example.taskapp.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.example.taskapp.LoggerConfig;
import org.example.taskapp.database.DatabaseManager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

public class RegistrationController {
    @FXML
    private TextField loginRegistration;
    @FXML
    private TextField mailRegistration;
    @FXML
    private PasswordField passwordRegistration;
    @FXML
    private Label errMailMessage;
    private DatabaseManager databaseManager;
    private static final Logger logger = LoggerConfig.getLogger();

    public RegistrationController() {
        databaseManager = new DatabaseManager();
    }

    public void initialize() {
        errMailMessage.setVisible(false);
    }

    @FXML
    AnchorPane anchorPane;

    public void onBackClick(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/taskapp/start-view.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) anchorPane.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void onRegistrationClick(ActionEvent actionEvent) {
        String login = loginRegistration.getText();
        String mail = mailRegistration.getText();
        String password = passwordRegistration.getText();

        if (!isValidEmailSimple(mail)) {
            errMailMessage.setText("Неправильно указана почта");
            errMailMessage.setTextFill(Color.RED);
            errMailMessage.setVisible(true);
            logger.warning("Неверно указан логин или пароль");
            return;
        }

        if (!isUniqueEmail(mail)) {
            errMailMessage.setText("Почта уже зарегистрирована!");
            errMailMessage.setTextFill(Color.RED);
            errMailMessage.setVisible(true);
            logger.warning("Почта уже зарегестрирована");
            return;
        }

        if (checkLogin()){
            return;
        }

        registerUser(login, mail, password);
    }
    public boolean checkLogin() {
        String login = loginRegistration.getText();

        String query = "SELECT Login FROM Users WHERE Login = ?"; // исправлено
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = databaseManager.getConnection();
            if (connection == null) {
                logger.severe(  "Ошибка подключения к базе данных");
                return false; // Если нет соединения, сразу возвращаем false
            }

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, login);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                errMailMessage.setText("Пользователь с таким логином уже существует");
                errMailMessage.setTextFill(Color.RED);
                errMailMessage.setVisible(true);
                logger.warning("Пользователь с таким логином уже существует");
                return true; // Логин занят
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return false; // Логин свободен
    }

    private void registerUser(String login, String mail, String password) {
        String query = "INSERT INTO Users (Login, Mail, Password) VALUES (?, ?, ?)";
        Connection connection = null;

        try {
            connection = databaseManager.getConnection();
            if (connection == null) {
                logger.severe("Не удалось установить соединение с базой данных");
                return;
            }
            connection = databaseManager.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, login);
            preparedStatement.setString(2, mail);
            preparedStatement.setString(3, password);

            preparedStatement.executeUpdate();
            logger.info("Добавлен пользователь: " + login);

                errMailMessage.setText("Регистрация успеша, вернитесь на страницу входа");
                errMailMessage.setTextFill(Color.GREEN);
                errMailMessage.setVisible(true);

        } catch (SQLException e) {
                logger.severe("Ошибка базы данных: " + e.getMessage());
            } finally {
            databaseManager.closeConnection(connection);
        }
    }
    public boolean isValidEmailSimple(String email) {
        return email.contains("@") && email.indexOf("@") < email.lastIndexOf(".");
    }

    private boolean isUniqueEmail(String email){
        String query = "SELECT COUNT(*) FROM Users Where Mail = ?";
        try(Connection connection = databaseManager.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query)){
            preparedStatement.setString(1,email);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next() && resultSet.getInt(1) > 0){
                return false;
            }
        } catch (SQLException e){
            logger.warning("Ошибка проверки уникальности почты: " + e.getMessage());
        }
        return true;
    }
}
