package chat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UserDatabase {
    private static UserDatabase instance;

    private final Map<String, User> users;

    private UserDatabase() {
        users = Collections.synchronizedMap(new HashMap<>());
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
        users.put(user.getUserName(), user);
        return true;
    }

    public User getUser(String userName) {
        return users.get(userName);
    }
}
