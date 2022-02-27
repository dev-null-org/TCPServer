package commands;

import java.util.regex.Pattern;

public interface Command {
    Pattern commandPattern();

    String commandName();

    void execute(CommandServerClient client, String message);
}
