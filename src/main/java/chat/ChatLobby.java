package chat;

import utils.ConfigManager;
import utils.DatabaseConnector;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

public class ChatLobby {

    private static ChatLobby instance;
    private final HashMap<String, ChatRoom> chatRooms;
    private final Random random;

    private ChatLobby() {
        random = new Random();
        chatRooms = new HashMap<>();

        UserDatabase userDatabase = UserDatabase.getInstance();

        //language=MariaDB
        String query = "select roomId,password,id from tcp_server.ChatRoom";
        Connection connection = DatabaseConnector.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String roomId = resultSet.getString("roomId");
                String password = resultSet.getString("password");

                int id = resultSet.getInt("id");
                LinkedList<Message> roomMessages=new LinkedList<>();
                //language=MariaDB
                String messagesQuery = "select User.userName as userName,Message.content as content from Messages inner join Message on Messages.Message_id = Message.id " +
                        "inner join User on Message.author = User.id where ChatRoom_id=? order by Messages.id";
                try (PreparedStatement messagesStatement = connection.prepareStatement(messagesQuery)) {
                    messagesStatement.setInt(1, id);
                    ResultSet messagesResult = messagesStatement.executeQuery();
                    while (messagesResult.next()) {
                        String userName = messagesResult.getString("userName");
                        String content = messagesResult.getString("content");
                        User author = userDatabase.getUser(userName);
                        Message message = new Message(author, content);
                        roomMessages.add(message);
                    }
                    messagesResult.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                ChatRoom room = new ChatRoom(roomId, password,roomMessages);
                chatRooms.put(room.getId(), room);
            }
        } catch (SQLException | InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static ChatLobby getInstance() {
        if (instance == null) {
            instance = new ChatLobby();
        }
        return instance;
    }

    public ChatRoom getRoom(String id) {
        return chatRooms.get(id);
    }

    public void addRoom(ChatRoom chatRoom) {
        chatRooms.put(chatRoom.getId(), chatRoom);
        //language=MariaDB
        String query = "insert into tcp_server.ChatRoom(roomId, password) values(?,?)";
        Connection connection = DatabaseConnector.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, chatRoom.getId());
            if (chatRoom.isLocked()) {
                preparedStatement.setString(2, chatRoom.getPassword().getPasswordHash());
            } else {
                preparedStatement.setNull(2, Types.VARCHAR);
            }
            int result = preparedStatement.executeUpdate();
            if (result == 0) {
                System.out.println("SOMETHING WENT WRONG WHEN INSERTING INTO CHAT ROOMS!!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String randomId() {
        String randomString;
        do {
            StringBuilder sb = new StringBuilder();
            int ID_LENGTH = 4;
            ConfigManager configManager=ConfigManager.getInstance();
            if(configManager.configFileLoaded()){
                Object value = configManager.getProperty("roomIdLength");
                if(value instanceof Long){
                    ID_LENGTH=((Long) value).intValue();
                }
            }
            for (int i = 0; i < ID_LENGTH; i++) {
                sb.append(random.nextInt(10));
            }
            randomString = sb.toString();
        } while (chatRooms.containsKey(randomString));
        return randomString;
    }

}
