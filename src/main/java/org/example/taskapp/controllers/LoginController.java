package org.example.taskapp.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
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

public class LoginController {
    @FXML
    private Button loginBtn;
    @FXML
    private Button backBtn;
    @FXML
    private TextField mailLogin;
    @FXML
    private Label wrongPasswordMessage;
    @FXML
    AnchorPane anchorPane;
    @FXML
    private Label resetPasswordLabelLogin;
    @FXML
    private TextField loginLogin;
    @FXML
    private PasswordField passwordLogin;
    @FXML
    private Button codeBtn;
    private String email;
    DatabaseManager databaseManager;
    private static final Logger logger = LoggerConfig.getLogger();

    public LoginController() {
        databaseManager = new DatabaseManager();
    }

    public void initialize() {
        loginBtn.setVisible(true);
        backBtn.setVisible(true);
        loginLogin.setVisible(true);
        passwordLogin.setVisible(true);
        mailLogin.setVisible(false);
        codeBtn.setVisible(false);
    }

    public void onBackClick(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/taskapp/start-view.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) anchorPane.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void onLoginClick(ActionEvent actionEvent) {

        String login = loginLogin.getText();
        String password = passwordLogin.getText();

        String query = "SELECT Password, UserId, Mail FROM Users WHERE Login = ?";
        Connection connection = null;

        try {
            connection = databaseManager.getConnection();


            if (connection == null) {
                logger.severe("Не удалось установить соединение с базой данных");
                return;
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, login);

                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    String storedPassword = resultSet.getString("Password");
                    if (storedPassword.equals(password)) {
                            logger.info("Успешный вход");

                            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/taskapp/app-view.fxml"));
                            Parent root = fxmlLoader.load();

                            AppController appController = fxmlLoader.getController();
                            appController.setUserId(resultSet.getString("UserId"));

                        Stage stage = (Stage) anchorPane.getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.show();

                    } else {
                        wrongPasswordMessage.setText("Неверный логин или пароль");
                        wrongPasswordMessage.setTextFill(Color.RED);
                        wrongPasswordMessage.setVisible(true);
                        logger.warning("Неверный логин или пароль");
                    }
                } else {
                    wrongPasswordMessage.setText("Неверный логин или пароль");
                    wrongPasswordMessage.setTextFill(Color.RED);
                    wrongPasswordMessage.setVisible(true);
                    logger.warning("Неверный логин или пароль");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void onLabelClicked(MouseEvent mouseEvent) throws IOException {
        loginLogin.setVisible(false);
        passwordLogin.setVisible(false);
        mailLogin.setVisible(true);
        codeBtn.setVisible(true);
        loginBtn.setVisible(false);
        backBtn.setVisible(false);

    }
    @FXML
    public void OnGetCodeClick(ActionEvent actionEvent) {
        String email = mailLogin.getText();  // Получаем почту

        String query = "SELECT Mail FROM Users WHERE Mail = ?";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = databaseManager.getConnection(); // Устанавливаем соединение с БД
            if (connection == null) {
                logger.severe("Ошибка подключения к базе данных");
                return;
            }

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);  // Устанавливаем значение почты

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                logger.info("Почта существует: " + email);

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/taskapp/mail-view.fxml"));
                Parent root = loader.load();

                MailController mailController = loader.getController();
                mailController.setEmail(email);  // Передаем почту в MailController

                Stage stage = (Stage) anchorPane.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } else {
                logger.warning("Почта не найдена: " + email);
                wrongPasswordMessage.setText("Почта не найдена");
                wrongPasswordMessage.setTextFill(Color.RED);
                wrongPasswordMessage.setVisible(true);
            }
        } catch (SQLException e) {
            logger.severe("Ошибка базы данных: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException ex) {
                logger.severe("Ошибка при закрытии ресурсов: " + ex.getMessage());
            }
        }
    }
}