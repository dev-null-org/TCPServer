package commands;

import java.util.regex.Pattern;

public class Date implements Command {

    @Override
    public Pattern commandPattern() {
        return Pattern.compile("^date.*", Pattern.CASE_INSENSITIVE);
    }

    @Override
    public String commandName() {
        return "date";
    }

    @Override
    public void execute(CommandServerClient client, String message) {
        client.println(new java.util.Date().toString());
    }
}
