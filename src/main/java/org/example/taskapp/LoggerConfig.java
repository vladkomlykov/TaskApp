package org.example.taskapp;

import lombok.Getter;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerConfig {

    @Getter
    private static final Logger logger = Logger.getLogger("TaskAppLogger");

    static {
        try {
            FileHandler fileHandler = new FileHandler("taskapp.log", true); // true - дописывать в файл
            fileHandler.setFormatter(new SimpleFormatter()); // Простое форматирование

            logger.addHandler(fileHandler);

            logger.setUseParentHandlers(false); // Отключить вывод в консоль, если нужно
        } catch (IOException e) {
            System.err.println("Ошибка при настройке логгера: " + e.getMessage());
        }
    }
}
