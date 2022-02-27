import chat.Password;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import utils.ConfigManager;
import utils.DatabaseConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class MainTest {

    @Test
    public void configFile() {
        Assertions.assertTrue(ConfigManager.getInstance().configFileLoaded());
    }


    @Test
    public void databaseConnection() {
        Connection connection = DatabaseConnector.getInstance().getConnection();
        Assertions.assertNotNull(connection);
        String query = "select version()";
        boolean connected = false;

        //language=MariaDB
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            connected = resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Assertions.assertTrue(connected);
    }

    @Test
    public void passwordHashGenerator() {
        Assertions.assertDoesNotThrow(() -> new Password("blablabla"));
    }

    @Test
    public void passwordTesting() {
        Password password = Assertions.assertDoesNotThrow(() -> new Password("blablabla"));
        Assertions.assertTrue(Assertions.assertDoesNotThrow(() -> password.verifyPassword("blablabla")));
        Assertions.assertFalse(Assertions.assertDoesNotThrow(() -> password.verifyPassword("blablabl")));
        Assertions.assertFalse(Assertions.assertDoesNotThrow(() -> password.verifyPassword("blablablaa")));
    }

    @Test
    public void ignoreMePls(){
        Assertions.assertFalse(false);
        //just test;
    }
}
