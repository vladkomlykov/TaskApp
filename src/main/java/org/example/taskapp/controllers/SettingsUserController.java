package org.example.taskapp.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
import java.sql.SQLException;
import java.util.logging.Logger;

public class SettingsUserController {
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private TextField newLogin;
    @FXML
    private TextField newMail;
    @FXML
    private TextField newPassword;
    @FXML
    private Button backBtn;
    @FXML
    private Label errMessage;
    @Getter
    @Setter
    private String userId;
    DatabaseManager databaseManager;
    private static final Logger logger = LoggerConfig.getLogger();

    public SettingsUserController() {
        this.databaseManager = new DatabaseManager();
    }


    public void onBackBtnClick(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/taskapp/login-view.fxml"));
        Parent root = fxmlLoader.load();

        Stage stage = (Stage) anchorPane.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void onSaveBtnClick(ActionEvent actionEvent) {
        StringBuilder stringBuilder = new StringBuilder("UPDATE Users SET ");
        boolean firstField = false;

        if (!newLogin.getText().isEmpty()) {
            stringBuilder.append("Login = ?");
            firstField = false;
        }
        if (!newMail.getText().isEmpty()) {
            if (!firstField) stringBuilder.append(" ,");
            stringBuilder.append("Mail = ?");
            firstField = false;
        }
        if (!newPassword.getText().isEmpty()) {
            if (!firstField) stringBuilder.append(" ,");
            stringBuilder.append("Password = ?");
            firstField = false;
        }

        if (firstField) {
            errMessage.setText("Пожалуйста, заполните хотя бы одно поле для обновления.");
            errMessage.setTextFill(Color.RED);
            errMessage.setVisible(true);
            return;
        }

        stringBuilder.append(" WHERE UserId = ?");

        try (Connection connection = databaseManager.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(stringBuilder.toString());
            int paramIndex = 1;

            if (!newLogin.getText().isEmpty()) {
                preparedStatement.setString(paramIndex++, newLogin.getText());
            }
            if (!newMail.getText().isEmpty()) {
                preparedStatement.setString(paramIndex++, newMail.getText());
            }
            if (!newPassword.getText().isEmpty()) {
                preparedStatement.setString(paramIndex++, newPassword.getText());
            }


            preparedStatement.setInt(paramIndex, Integer.parseInt(userId));

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0){
                errMessage.setText("Данные успешно обновлены");
                errMessage.setTextFill(Color.GREEN);
                errMessage.setVisible(true);
                backBtn.setVisible(true);
                logger.info("Данные успешно обновлены");
            } else {
                errMessage.setText("Пользователь не найден");
                errMessage.setTextFill(Color.RED);
                errMessage.setVisible(true);
                logger.warning("Пользователь не найден");
            }
        } catch (SQLException e) {
            logger.severe("Ошибка базы данных " + e.getMessage());
        }
    }
}
