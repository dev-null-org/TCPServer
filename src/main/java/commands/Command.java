package commands;

import server.ServerClient;

import java.util.regex.Pattern;

public interface Command {
    Pattern commandPattern();

    String commandName();

    void execute(ServerClient client, String message);
}
