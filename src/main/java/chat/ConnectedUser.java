package chat;

import server.ServerClient;

public class ConnectedUser {
    protected final ServerClient client;
    protected final User user;

    public ConnectedUser(ServerClient client, User user) {
        this.client = client;
        this.user = user;
    }

}
