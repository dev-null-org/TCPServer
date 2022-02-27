package commands;

import chat.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

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
    public void execute(CommandServerClient client, String message) {
        User user = null;
        ChatRoom room = null;
        try {
            user = getUser(client);
            room = getChatRoom(client, user);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (user == null || room == null) {
            client.println("Exiting chat command.");
        } else {
            ConnectedUser connectedUser = new ConnectedUser(client, user);
            room.joinChatRoom(connectedUser);
        }
    }

    private ChatRoom getChatRoom(CommandServerClient client, User user) throws IOException {
        ChatLobby chatLobby = ChatLobby.getInstance();
        ChatRoom room = null;
        if (user != null) {
            boolean repeatRoomJoin = true;
            while (repeatRoomJoin) {
                client.println("Do you want to create new room(C), join exist one(J), list existing(L) or exit(Q)?");
                String input = client.readLine();
                switch (input.trim().toUpperCase()) {
                    case "C":
                        client.println("Should the room have password(Y/N)?");
                        if ("Y".equalsIgnoreCase(client.readLine().trim())) {
                            client.println("What should the password be?");
                            String password = client.readLine();
                            try {
                                room = new ChatRoom(password);
                                client.println("Room created ID(" + room.getId() + ") joining");
                                repeatRoomJoin = false;
                            } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                                client.println("Something went wrong on the server please contact admin team!!");
                                repeatRoomJoin = false;
                                e.printStackTrace();
                            }
                        } else {
                            room = new ChatRoom();
                            client.println("Room created ID(" + room.getId() + ") joining");
                            repeatRoomJoin = false;
                        }
                        break;
                    case "J":
                        client.println("What is the room ID?");
                        String roomId = client.readLine().trim();
                        room = chatLobby.getRoom(roomId);
                        if (room != null) {
                            if (room.isLocked()) {
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
                            } else {
                                client.println("Room found joining");
                                repeatRoomJoin = false;
                            }
                        } else {
                            client.println("Room with that ID is not existing");
                        }
                        break;
                    case "L":
                        Collection<ChatRoom> chatRooms = chatLobby.getAllRooms();
                        if (chatRooms.size() == 0) {
                            client.println("No active rooms found :(");
                        } else {
                            for (ChatRoom roomInDatabase : chatRooms) {
                                if (roomInDatabase.isLocked()) {
                                    client.println("Room ID is " + roomInDatabase.getId() + ", but it's locked");
                                } else {
                                    List<ConnectedUser> users = roomInDatabase.getConnectedUsers();
                                    client.println("Room ID is " + roomInDatabase.getId() + (users.size() > 0 ? ", and those members are online" : ", but no one is there :("));
                                    if (users.size() > 0) {
                                        for (ConnectedUser activeUser : roomInDatabase.getConnectedUsers()) {
                                            client.println("\t" + activeUser.getUser().toString() + "\u001B[0m");
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    case "Q":
                        repeatRoomJoin = false;
                        break;
                }
            }
        } else {
            return null;
        }
        return room;
    }

    private User getUser(CommandServerClient client) throws IOException {
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
                            String color = "\u001B[37m";
                            boolean repeatColorPick = true;
                            while (repeatColorPick) {
                                client.println("What color do you want to be red(R),green(G),yellow(Y),blue(B),purple(P),cyan(C),white(W)");
                                switch (client.readLine().trim().toUpperCase()) {
                                    case "R":
                                        color = "\u001B[31m";
                                        break;
                                    case "G":
                                        color = "\u001B[32m";
                                        break;
                                    case "Y":
                                        color = "\u001B[33m";
                                        break;
                                    case "B":
                                        color = "\u001B[34m";
                                        break;
                                    case "P":
                                        color = "\u001B[35m";
                                        break;
                                    case "C":
                                        color = "\u001B[36m";
                                        break;
                                    default:
                                        color = "\u001B[37m";
                                        break;
                                }
                                client.println("Your color will look like this (" + color + userName + ":example\u001B[0m) do you want to change your color(Y/N)?");
                                if (!client.readLine().trim().equalsIgnoreCase("Y")) {
                                    repeatColorPick = false;
                                }
                            }

                            user = new User(userName, new Password(password), color);
                            if (database.addUser(user)) {
                                client.println("Successfully created account. Welcome!");
                                repeatLogin = false;
                            } else {
                                client.println("Something went wrong please try again or contact admin");
                                user = null;
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
