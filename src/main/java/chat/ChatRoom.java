package chat;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ChatRoom {
    private String id;
    private final List<Message> messages;
    private Password password;
    private final List<ConnectedUser> connectedUsers;

    public String getId() {
        return id;
    }

    public ChatRoom() {
        this.id = ChatLobby.getInstance().randomId();
        this.messages = Collections.synchronizedList(new LinkedList<>());
        ChatLobby.getInstance().addRoom(this);
        connectedUsers=Collections.synchronizedList(new LinkedList<>());
    }

    public ChatRoom(String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
        this();
        this.password = new Password(password);
    }

    public void sendMessage(Message message) {
        for (ConnectedUser connectedUser:connectedUsers){
            if(!connectedUser.user.equals(message.author)){
                connectedUser.client.println(message.toString());
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

    public void joinChatRoom(ConnectedUser connectedUser){
        for(Message message:messages){
            connectedUser.client.println(message.toString());
        }
        connectedUsers.add(connectedUser);
        connectedUser.client.println("Welcome to chat room to quit write Q any time");
        while (true){
            String input=connectedUser.client.readLine();
            if(input.trim().equalsIgnoreCase("Q")){
                break;
            }else{
                sendMessage(new Message(connectedUser.user,input));
            }
        }
        connectedUser.client.println("Leaving chat room and chat command");
        connectedUsers.remove(connectedUser);
    }
}
