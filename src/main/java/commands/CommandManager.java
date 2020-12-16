package commands;

import java.util.ArrayList;

public class CommandManager {
    private static CommandManager instance;
    private final ArrayList<Command> commands;

    private CommandManager() {
        commands = new ArrayList<>();
        commands.add(new Help());
        commands.add(new Exit());
        commands.add(new Date());
    }

    public static CommandManager getInstance() {
        if (instance == null) {
            instance = new CommandManager();
        }
        return instance;
    }

    public Command getCommand(String message) {
        for (Command command : commands) {
            if (command.commandPattern().matcher(message).matches()) {
                return command;
            }
        }
        return null;
    }

    public ArrayList<Command> commands() {
        return new ArrayList<>(commands);
    }
}
