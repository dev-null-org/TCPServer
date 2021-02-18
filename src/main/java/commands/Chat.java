package commands;

import server.ServerClient;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.regex.Pattern;

import chat.*;

public class Chat implements Command {

    @Override
    public Pattern commandPattern() {
        return Pattern.compile("^chat.*", Pattern.CASE_INSENSITIVE);
    }

    @Override
    public String commandName() {
        return "chat";
    }

    @Override
    public ExecutionCode execute(ServerClient client, String message) {
        User user=getUser(client);
        ChatRoom room=getChatRoom(client,user);
        if(user ==null||room==null){
            client.println("Exiting chat command.");
        }else{
            ConnectedUser connectedUser=new ConnectedUser(client, user);
            room.joinChatRoom(connectedUser);
        }
        return ExecutionCode.SUCCESS;
    }

    private ChatRoom getChatRoom(ServerClient client,User user){
        ChatLobby chatLobby=ChatLobby.getInstance();
        ChatRoom room=null;
        if(user!=null){
            boolean repeatRoomJoin=true;
            while (repeatRoomJoin) {
                client.println("Do you want to create new room(C), join exist one(J) or exit(Q)?");
                String input = client.readLine();
                switch (input.trim().toUpperCase()) {
                    case "C":
                        client.println("Should the room have password(Y/N)?");
                        if ("Y".equalsIgnoreCase(client.readLine().trim())) {
                            String password = client.readLine();
                            try {
                                room = new ChatRoom(password);
                                client.println("Room created ID("+room.getId()+") joining");
                            } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                                client.println("Something went wrong on the server please contact admin team!!");
                                repeatRoomJoin = false;
                                e.printStackTrace();
                            }
                        }else {
                            room = new ChatRoom();
                            client.println("Room created ID("+room.getId()+") joining");
                            repeatRoomJoin=false;
                        }
                        break;
                    case "J":
                        client.println("What is the room ID?");
                        String roomId=client.readLine().trim();
                        room=chatLobby.getRoom(roomId);
                        if(room!=null){
                            if(room.isLocked()){
                                client.println("Room is locked!");
                                boolean repeatPassword = true;
                                while (repeatPassword) {
                                    client.println("What is the password?");
                                    String password = client.readLine();
                                    if (room.verifyPassword(password)) {
                                        client.println("Correct password joining room");
                                        repeatPassword = false;
                                        repeatRoomJoin = false;
                                    } else {
                                        client.println("Wrong password do you want to try again (Y/N)?");
                                        if (!"Y".equalsIgnoreCase(client.readLine().trim())) {
                                            room = null;
                                            repeatPassword = false;
                                        }
                                    }
                                }
                            }else{
                                client.println("Room found joining");
                                repeatRoomJoin=false;
                            }
                        }else{
                            client.println("Room with that ID is not existing");
                        }
                        break;
                    case "Q":
                        repeatRoomJoin = false;
                        break;
                }
            }
        }else{
            return null;
        }
        return room;
    }
    private User getUser(ServerClient client){
        UserDatabase database = UserDatabase.getInstance();
        boolean repeatLogin = true;
        String userName;
        User user = null;
        while (repeatLogin) {
            client.println("What do you want to do login(L), register(R) or exit(Q)?");
            String input = client.readLine();
            switch (input.trim().toUpperCase()) {
                case "L":
                    client.println("What is your username?");
                    userName = client.readLine();
                    user = database.getUser(userName);
                    if (user != null) {
                        boolean repeatPassword = true;
                        while (repeatPassword) {
                            client.println("What is your password?");
                            String password = client.readLine();
                            if (user.logIn(password)) {
                                client.println("Successfully logged in.");
                                repeatPassword = false;
                                repeatLogin = false;
                            } else {
                                client.println("Wrong password do you want to try again (Y/N)?");
                                if (!"Y".equalsIgnoreCase(client.readLine().trim())) {
                                    user = null;
                                    repeatPassword = false;
                                }
                            }
                        }
                    } else {
                        client.println("Wrong/not-registered username!");
                    }
                    break;
                case "R":
                    client.println("What is your username?");
                    userName = client.readLine();
                    user = database.getUser(userName);
                    if (user == null) {
                        client.println("What is your password?");
                        String password = client.readLine();
                        try {
                            user = new User(userName, new Password(password));
                            if(database.addUser(user)){
                                client.println("Successfully created account. Welcome!");
                                repeatLogin=false;
                            }else{
                                client.println("Something went wrong please try again or contact admin");
                                user=null;
                            }
                        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                            client.println("Something went wrong on the server please contact admin team!!");
                            repeatLogin = false;
                        }
                    } else {
                        client.println("User with this username already exists");
                    }
                    break;
                case "Q":
                    repeatLogin = false;
                    break;
            }
        }
        return user;
    }
}
