package org.example.taskapp.database;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Getter
@Setter
@AllArgsConstructor
public class DatabaseManager {
    private String url;
    private String user;
    private String password;

    public DatabaseManager(){
        this.url = "jdbc:postgresql://localhost:5432/TaskAppDb";
        this.user = "postgres";
        this.password = "123";
    }

    public Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(url,user,password);
        } catch (SQLException e){
            System.err.println("Ошибка при подключении к базе данных: " + e.getMessage());
            throw e;
        }
    }

    public void closeConnection(Connection connection){
        if (connection != null){
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Ошибка при закрытии соединения: " + e.getMessage());
            }
        }
    }
}
