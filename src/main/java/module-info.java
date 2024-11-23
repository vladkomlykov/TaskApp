module org.example.taskapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;
    requires java.sql;


    opens org.example.taskapp to javafx.fxml;
    exports org.example.taskapp;
    exports org.example.taskapp.controllers;
    opens org.example.taskapp.controllers to javafx.fxml;
}