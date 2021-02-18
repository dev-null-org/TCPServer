package chat;

import server.ServerClient;

public class ConnectedUser {
    protected ServerClient client;
    protected User user;

    public ConnectedUser(ServerClient client, User user) {
        this.client = client;
        this.user = user;
    }

}
