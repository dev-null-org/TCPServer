package chat;

import java.util.HashMap;
import java.util.Random;

public class ChatLobby {

    private final int ID_LENGHT = 4;

    private static ChatLobby instance;
    private final HashMap<String, ChatRoom> chatRooms;
    private final Random random;

    private ChatLobby() {
        random = new Random();
        chatRooms = new HashMap<>();
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
    }

    public String randomId() {
        String randomString;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < ID_LENGHT; i++) {
                sb.append(random.nextInt(10));
            }
            randomString = sb.toString();
        } while (chatRooms.containsKey(randomString));
        return randomString;
    }

}
