package server;

import utils.ColorManager;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

public abstract class ServerClient implements Runnable {

    protected TCPServer server;
    protected boolean inQueue;
    protected Socket socket;
    protected boolean closed;
    protected int id;
    private SocketAddress remoteIp;
    private ColorManager.Color logColor;

    public ServerClient(Socket socket, TCPServer server, boolean inQueue, ColorManager.Color logColor) {
        this.initiateVariables(socket, server, inQueue, logColor);
    }

    public int getId() {
        return id;
    }

    public void initiateVariables(Socket socket, TCPServer server, boolean inQueue, ColorManager.Color logColor) {
        this.logColor = logColor;
        this.closed = false;
        this.id = this.hashCode();
        this.socket = socket;
        this.server = server;
        this.inQueue = inQueue;
        this.remoteIp = socket.getRemoteSocketAddress();
    }

    public void logError(String error) {
        server.logError(getIdentifier() + " ERROR: " + error + "\u001B[0m");
    }

    public String getIdentifier() {
        return logColor.toString() + "SERVER-CLIENT(" + id + ")";
    }

    public void log(String message) {
        server.log(getIdentifier() + " printing: " + message + "\u001B[0m");
    }

    public void close() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!closed) server.remove(this);
        closed = true;
    }

    public SocketAddress remoteIpAddress() {
        return remoteIp;
    }

}
