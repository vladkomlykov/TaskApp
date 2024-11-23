package org.example.taskapp.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.Setter;

import java.io.IOException;
import java.util.Random;

public class MailController {
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private TextField codeMail;
    Random random = new Random();
    @Setter
    private String email;

    private String generateCode(){
       int generatedCode = 100 + random.nextInt(900);
       return String.valueOf(generatedCode);
    }

    public void OnGetCodeClick(ActionEvent actionEvent) {
        codeMail.setVisible(true);
        codeMail.setText(generateCode());
    }

    public void onChangePasswordClick(ActionEvent actionEvent) throws IOException {
        String generatedCode = codeMail.getText();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/taskapp/reset-password-view.fxml"));
        try {
            Parent root = loader.load();
            ResetPasswordController resetPasswordController = loader.getController();
            resetPasswordController.setEmail(email);
            resetPasswordController.setCode(generatedCode);// Передаем почту

            Stage stage = (Stage) anchorPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
