package commands;

import server.BasicServerClient;
import server.TCPServer;
import utils.ColorManager;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

public class CommandServerClient extends BasicServerClient implements Runnable, Closeable {
    public CommandServerClient(Socket socket, TCPServer server, boolean inQueue, ColorManager.Color logColor) {
        super(socket, server, inQueue, logColor);
    }

    public void initiateVariables(Socket socket, TCPServer server, boolean inQueue, ColorManager.Color logColor) {
        super.initiateVariables(socket, server, inQueue, logColor);
    }

    protected String goodBeyMessage() {
        return "Closing this connection bey.";
    }

    @Override
    protected String welcomeMessage() {
        if (inQueue) {
            return "You are in QUEUE wait to get started";
        } else {
            return "Welcome you can now use commands";
        }
    }

    @Override
    public void run() {
        super.run();
        CommandManager commandManager = CommandManager.getInstance();
        while (!closed) {
            String inputString;
            try {
                inputString = this.readLine();
            } catch (IOException e) {
                close();
                break;
            }
            if (inputString == null) {
                this.close();
                return;
            }
            Command command = commandManager.getCommand(inputString);
            if (command != null) {
                command.execute(this, inputString);
            } else {
                println("Unknown command: " + inputString + " use 'help' to see all command available");
            }
        }
    }
}
