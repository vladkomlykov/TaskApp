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
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import lombok.Setter;
import org.example.taskapp.LoggerConfig;
import org.example.taskapp.database.DatabaseManager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

public class ResetPasswordController {
    @FXML
    private Button backBtn;
    @FXML
    private Label errMessage;
    @FXML
    private PasswordField confirmPassword;
    @FXML
    private TextField newPassword;
    @FXML
    private TextField codeMail;
    @FXML
    private AnchorPane anchorPane;
    private String generatedCode;
    @Setter
    private String email;
    private DatabaseManager databaseManager = new DatabaseManager();
    private static final Logger logger = LoggerConfig.getLogger();

    public void setCode(String code) {
        this.generatedCode = code;
    }

    public void onChangePasswordClick(ActionEvent actionEvent) {
        String userCode = codeMail.getText(); // Введённый пользователем код
        String newPass = newPassword.getText(); // Новый пароль
        String confirmPass = confirmPassword.getText(); // Подтверждение пароля

        if (!newPass.equals(confirmPass)) {
            errMessage.setText("Пароли не совпадают");
            errMessage.setTextFill(Color.RED);
            errMessage.setVisible(true);
            logger.warning("Пароли не совпадают");
            return;
        }

        if (generatedCode == null || !generatedCode.equals(userCode)) {
            errMessage.setText("Неверный код подтверждения");
            errMessage.setTextFill(Color.RED);
            errMessage.setVisible(true);
            logger.warning("Неверный код подтверждения");
            return;
        }

        if (email == null || email.isEmpty()) {
            errMessage.setText("Ошибка: email не был передан.");
            errMessage.setTextFill(Color.RED);
            errMessage.setVisible(true);
            logger.warning("Ошибка: email не был передан.");
            return;
        }

        String query = "UPDATE Users SET Password = ? WHERE Mail = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, newPass);
            preparedStatement.setString(2, email);

            int rowsUpdated = preparedStatement.executeUpdate();
            logger.info("Обновлено строк: " + rowsUpdated);
            if (rowsUpdated > 0) {
                errMessage.setText("Пароль успешно изменён");
                errMessage.setTextFill(Color.GREEN);
                errMessage.setVisible(true);
                backBtn.setVisible(true);
                logger.info("Пароль успешно изменён");
            } else {
                errMessage.setText("Ошибка изменения пароля. Пользователь с таким email не найден.");
                errMessage.setTextFill(Color.RED);
                errMessage.setVisible(true);
                logger.warning("Ошибка изменения пароль. Пользователь с таким email не найден.");
            }

        } catch (SQLException e) {
            errMessage.setText("Ошибка базы данных: " + e.getMessage());
            errMessage.setTextFill(Color.RED);
            errMessage.setVisible(true);
            logger.severe("SQL Error: " + e.getMessage());
        }
    }

    public void onBackBtnClick(ActionEvent actionEvent) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/taskapp/login-view.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) anchorPane.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}