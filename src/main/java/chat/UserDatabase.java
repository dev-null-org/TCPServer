package chat;

import utils.DatabaseConnector;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UserDatabase {
    private static UserDatabase instance;

    private final Map<String, User> users;

    private UserDatabase() {
        users = Collections.synchronizedMap(new HashMap<>());
        //language=MariaDB
        String query = "select colorCode,password,userName from tcp_server.User;";
        Connection connection = DatabaseConnector.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String colorCode = resultSet.getString("colorCode");
                String password = resultSet.getString("password");
                String userName = resultSet.getString("userName");
                User user = new User(userName, new Password(password, true), colorCode);
                users.put(user.getUserName(), user);
            }

        } catch (SQLException | InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static UserDatabase getInstance() {
        if (instance == null) {
            instance = new UserDatabase();
        }
        return instance;
    }

    public boolean addUser(User user) {
        if (users.containsKey(user.getUserName())) {
            return false;
        }
        //language=MariaDB
        String query = "insert into tcp_server.User(colorCode, password, userName) values(?,?,?)";
        Connection connection = DatabaseConnector.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, user.getColorCode());
            preparedStatement.setString(2, user.getPassword().getPasswordHash());
            preparedStatement.setString(3, user.getUserName());
            int result = preparedStatement.executeUpdate();
            if (result == 0) {
                System.out.println("SOMETHING WENT WRONG WHEN INSERTING INTO USERS!!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        users.put(user.getUserName(), user);
        return true;
    }

    public User getUser(String userName) {
        return users.get(userName);
    }
}
