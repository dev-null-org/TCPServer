package robot_controller;

import server.ServerClient;
import server.ServerClientFactory;
import server.TCPServer;
import utils.ColorManager;

import java.net.Socket;

public class RobotClientFactory implements ServerClientFactory {

    public static final ServerClientFactory factory = new RobotClientFactory();

    private RobotClientFactory() {

    }

    @Override
    public ServerClient create(Socket socket, TCPServer server, boolean inQueue, ColorManager.Color logColor) {
        return new RobotClient(socket, server, inQueue, logColor);
    }
}
