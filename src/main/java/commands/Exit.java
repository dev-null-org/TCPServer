package commands;

import java.util.regex.Pattern;

public class Exit implements Command {
    @Override
    public Pattern commandPattern() {
        return Pattern.compile("^exit.*", Pattern.CASE_INSENSITIVE);
    }

    @Override
    public String commandName() {
        return "exit";
    }

    @Override
    public void execute(CommandServerClient client, String message) {
        client.close();
    }
}
