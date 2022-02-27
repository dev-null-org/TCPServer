package chat;

import commands.CommandServerClient;

public class ConnectedUser {
    protected final CommandServerClient client;
    protected final User user;

    public ConnectedUser(CommandServerClient client, User user) {
        this.client = client;
        this.user = user;
    }

    public User getUser() {
        return user;
    }

}
