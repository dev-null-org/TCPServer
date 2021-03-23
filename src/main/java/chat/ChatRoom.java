package chat;

import utils.DatabaseConnector;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ChatRoom {

    private final String id;
    private final List<ConnectedUser> connectedUsers;
    private List<Message> messages;
    private Password password;

    public ChatRoom() {
        this.id = ChatLobby.getInstance().randomId();
        this.connectedUsers = Collections.synchronizedList(new LinkedList<>());
        this.messages = Collections.synchronizedList(new LinkedList<>());
        ChatLobby.getInstance().addRoom(this);
    }

    public ChatRoom(String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
        this.id = ChatLobby.getInstance().randomId();
        this.connectedUsers = Collections.synchronizedList(new LinkedList<>());
        this.password = new Password(password);
        this.messages = Collections.synchronizedList(new LinkedList<>());
        ChatLobby.getInstance().addRoom(this);
    }

    protected ChatRoom(String id, String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
        this.id = id;
        this.connectedUsers = Collections.synchronizedList(new LinkedList<>());
        if (password != null) {
            this.password = new Password(password, true);
        }
    }

    public Password getPassword() {
        return password;
    }

    public String getId() {
        return id;
    }

    public void sendMessage(Message message) {
        for (ConnectedUser connectedUser : connectedUsers) {
            if (!connectedUser.user.equals(message.author)) {
                connectedUser.client.println(message.toString() + connectedUser.user.getColorCode());
            }
        }
        //language=MariaDB
        String messageQuery = "insert into Message(content, author) values(?,(select User.id from User where userName=?))";
        Connection connection = DatabaseConnector.getInstance().getConnection();
        if (connection != null) {
            try (PreparedStatement messageStatement = connection.prepareStatement(messageQuery, Statement.RETURN_GENERATED_KEYS)) {
                messageStatement.setString(1, message.getContent());
                messageStatement.setString(2, message.getAuthor().getUserName());
                int result = messageStatement.executeUpdate();
                if (result == 0) {
                    System.out.println("SOMETHING WENT WRONG WHEN INSERTING INTO CHAT MESSAGE!!");
                }
                ResultSet resultSet = messageStatement.getGeneratedKeys();
                if (resultSet.next()) {
                    int id = resultSet.getInt(1);
                    //language=MariaDB
                    String messagesQuery = "insert into Messages(Message_id, ChatRoom_id) values (?,(select ChatRoom.id from ChatRoom where roomId=?))";
                    try (PreparedStatement messagesStatement = connection.prepareStatement(messagesQuery)) {
                        messagesStatement.setInt(1, id);
                        messagesStatement.setString(2, this.id);
                        result = messagesStatement.executeUpdate();
                        if (result == 0) {
                            System.out.println("SOMETHING WENT WRONG WHEN INSERTING INTO CHAT MESSAGES!!");
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("SOMETHING WENT WRONG WHEN INSERTING INTO MESSAGES!!");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        this.messages.add(message);
    }

    public boolean isLocked() {
        return password != null;
    }

    public boolean verifyPassword(String password) {
        if (this.password != null) {
            try {
                return this.password.verifyPassword(password);
            } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public void joinChatRoom(ConnectedUser connectedUser) {
        UserDatabase userDatabase = UserDatabase.getInstance();
        Connection connection = DatabaseConnector.getInstance().getConnection();
        if (messages == null) {
            LinkedList<Message> roomMessages = new LinkedList<>();
            if (connection != null) {
                connectedUser.client.println("Loading messages from database please wait");
                //language=MariaDB
                String messagesQuery = "select User.userName as userName,Message.content as content from Messages inner join Message on Messages.Message_id = Message.id " +
                        "inner join User on Message.author = User.id inner join ChatRoom on Messages.ChatRoom_id = ChatRoom.id where ChatRoom.roomId=? order by Messages.id";
                try (PreparedStatement messagesStatement = connection.prepareStatement(messagesQuery)) {
                    messagesStatement.setString(1, id);
                    ResultSet messagesResult = messagesStatement.executeQuery();
                    while (messagesResult.next()) {
                        String userName = messagesResult.getString("userName");
                        String content = messagesResult.getString("content");
                        User author = userDatabase.getUser(userName);
                        Message message = new Message(author, content);
                        roomMessages.add(message);
                    }
                    messagesResult.close();

                    connectedUser.client.println("Messages loaded");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            this.messages = Collections.synchronizedList(roomMessages);
        }
        for (Message message : messages) {
            connectedUser.client.println(message.toString());
        }
        for (ConnectedUser user : connectedUsers) {
            user.client.println(connectedUser.user.toString() + "\u001B[0m has joined"+ user.user.getColorCode());
            connectedUser.client.println(user.user.toString() + "\u001B[0m is online");
        }
        connectedUsers.add(connectedUser);
        connectedUser.client.println("\u001B[0mWelcome to chat room to quit write Q any time" + connectedUser.user.getColorCode());
        while (true) {
            String input;
            try {
                input = connectedUser.client.readLine();
            } catch (IOException e) {
                break;
            }
            if (input == null || input.trim().equalsIgnoreCase("Q")) {
                break;
            } else {
                sendMessage(new Message(connectedUser.user, input));
            }
        }
        connectedUser.client.println("\u001B[0mLeaving chat room and chat command");
        connectedUsers.remove(connectedUser);

        for (ConnectedUser user : connectedUsers) {
            user.client.println(connectedUser.user.toString() + "\u001B[0m has left" + user.user.getColorCode());
        }
    }

    public List<ConnectedUser> getConnectedUsers() {
        if (isLocked()) {
            return null;
        } else {
            return new LinkedList<>(this.connectedUsers);
        }
    }
}
