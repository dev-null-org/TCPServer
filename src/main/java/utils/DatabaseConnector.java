package utils;

import java.sql.*;

public class DatabaseConnector {

    private static DatabaseConnector instance;

    private Connection connection;

    private DatabaseConnector() {
        ConfigManager configManager = ConfigManager.getInstance();
        if (configManager.configFileLoaded()) {
            String url = null, user = null, password = null;
            Object value = configManager.getProperty("databaseAddress");
            if (value instanceof String) {
                url = (String) value;
            }
            value = configManager.getProperty("databaseUser");
            if (value instanceof String) {
                user = (String) value;
            }
            value = configManager.getProperty("databasePassword");
            if (value instanceof String) {
                password = (String) value;
            }
            if (url != null && user != null && password != null) {
                try {
                    this.connection = DriverManager.getConnection(url, user, password);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static DatabaseConnector getInstance() {
        if (instance == null) {
            instance = new DatabaseConnector();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

}
