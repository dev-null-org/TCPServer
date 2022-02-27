package commands;

import server.ServerClient;
import server.ServerClientFactory;
import server.TCPServer;
import utils.ColorManager;

import java.net.Socket;

public class CommandServerClientFactory implements ServerClientFactory {
    public static final ServerClientFactory factory = new CommandServerClientFactory();

    private CommandServerClientFactory() {

    }

    @Override
    public ServerClient create(Socket socket, TCPServer server, boolean inQueue, ColorManager.Color logColor) {
        return new CommandServerClient(socket, server, inQueue, logColor);
    }
}
