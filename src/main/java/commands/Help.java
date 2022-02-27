package commands;

import java.util.regex.Pattern;

public class Help implements Command {
    @Override
    public Pattern commandPattern() {
        return Pattern.compile("^help.*", Pattern.CASE_INSENSITIVE);
    }

    @Override
    public String commandName() {
        return "help";
    }

    @Override
    public void execute(CommandServerClient client, String message) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Command command : CommandManager.getInstance().commands()) {
            stringBuilder.append("Command: ").append(command.commandName()).append("\r\n");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        client.println(stringBuilder.toString());
    }
}
