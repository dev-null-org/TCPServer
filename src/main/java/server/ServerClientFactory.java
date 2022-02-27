package server;

import utils.ColorManager;

import java.net.Socket;

public interface ServerClientFactory {
    ServerClient create(Socket socket, TCPServer server, boolean inQueue, ColorManager.Color logColor);
}
