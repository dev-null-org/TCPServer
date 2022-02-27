package server;

import commands.CommandServerClientFactory;
import utils.ColorManager;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Queue;

import static utils.ColorManager.COLOR_COUNT;

public class TCPServer implements Runnable, Closeable {

    private final int maxNumberOfClients;
    private final int maxNumberClientsInPool;
    private final int maxQueue;

    private final ServerSocket serverSocket;

    private final LinkedList<ServerClient> clients;
    private final Queue<ServerClient> clientQueue;

    private final LinkedList<ServerClient> usedClients;

    private final ServerClientFactory factory;
    private final boolean logging;
    private int colorCounter;

    public TCPServer(int portNumber, int maxNumberOfClients, int maxNumberClientsInPool, int maxQueue, Boolean logging, ServerClientFactory factory) throws IOException {
        this.clientQueue = new ArrayDeque<>();
        this.clients = new LinkedList<>();
        this.usedClients = new LinkedList<>();
        this.serverSocket = new ServerSocket(portNumber);
        this.maxNumberOfClients = maxNumberOfClients;
        this.maxNumberClientsInPool = maxNumberClientsInPool;
        this.maxQueue = maxQueue;
        this.logging = logging;
        this.factory = factory;
        this.colorCounter = (int) Math.floor(Math.random() * COLOR_COUNT());
    }

    public TCPServer(int portNumber, int maxNumberOfClients, int maxNumberClientsInPool, int maxQueue, boolean logging) throws IOException {
        this(portNumber, maxNumberOfClients, maxNumberClientsInPool, maxQueue, logging, CommandServerClientFactory.factory);
    }

    public TCPServer(int portNumber, int maxNumberOfClients, int maxNumberClientsInPool, int maxQueue) throws IOException {
        this(portNumber, maxNumberOfClients, maxNumberClientsInPool, maxQueue, false, CommandServerClientFactory.factory);
    }

    public TCPServer(int portNumber, int maxNumberOfClients) throws IOException {
        this(portNumber, maxNumberOfClients, 100, 50, false);
    }

    public TCPServer(int portNumber, int maxNumberOfClients, boolean logging) throws IOException {
        this(portNumber, maxNumberOfClients, 100, 50, logging);
    }

    @Override
    public void run() {
        while (true) {
            if (serverSocket != null) {
                log("SERVER: Waiting for new client");
                try {
                    connectNewClient(serverSocket.accept());
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            } else {
                break;
            }
        }
    }

    private void connectNewClient(Socket socket) {
        if (socket == null) {
            logError("SERVER: error null socket");
            return;
        }
        if (socket.isClosed()) {
            logError("SERVER: error socked is closed");
            return;
        }
        synchronized (this) {
            if (clients.size() < maxNumberOfClients) {
                ServerClient client = createServerClient(socket, this, false);
                log("SERVER: NEW client connected (" + socket.getRemoteSocketAddress() + ")");
                Thread clientThread = new Thread(client);
                clients.add(client);
                clientThread.start();
            } else {
                if (clientQueue.size() >= maxQueue) {
                    log("SERVER: Queue is full connection has been refused");
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    log("SERVER: List is full putting client to queue");
                    ServerClient client = createServerClient(socket, this, true);
                    clientQueue.add(client);
                }
            }
        }
    }

    private ServerClient createServerClient(Socket socket, TCPServer server, boolean inQueue) {
        ServerClient client;
        if (usedClients.size() > 0) {
            log("SERVER: NEW client from object pool");
            client = usedClients.removeFirst();
            client.initiateVariables(socket, server, inQueue, ColorManager.getColor(colorCounter++));
        } else {
            log("SERVER: NEW client");
            client = factory.create(socket, server, inQueue, ColorManager.getColor(colorCounter++));
        }
        return client;
    }

    public void remove(ServerClient serverClient) {
        log("SERVER: removed client(" + serverClient.getId() + ") with IP:" + serverClient.remoteIpAddress());
        clients.remove(serverClient);
        if (clientQueue.size() > 0) {
            ServerClient client = clientQueue.poll();
            log("SERVER: NEW client connected (" + client.remoteIpAddress() + ")");
            Thread clientThread = new Thread(client);
            clients.add(client);
            clientThread.start();
        }
        if (usedClients.size() < maxNumberClientsInPool) {
            if (!usedClients.contains(serverClient)) {
                usedClients.add(serverClient);
            } else {
                logError("SERVER: !!! Trying to add client back to pool twice !!!");
            }
        }
    }

    public void log(String message) {
        if (logging) {
            System.out.println(message);
        }
    }

    public void logError(String message) {
        if (logging) {
            System.err.println("\u001b[31;1m" + message + "\u001b[0m");
        }
    }

    @Override
    public void close() {
        for (ServerClient client : clientQueue) {
            client.close();
        }
        while (clients.size() > 0) {
            ServerClient client = clients.get(0);
            client.close();
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Runnable getShutDownHook() {
        return new ShutDownHook(this);
    }

    private static class ShutDownHook implements Runnable {
        private final TCPServer server;

        public ShutDownHook(TCPServer server) {
            this.server = server;
        }

        @Override
        public void run() {
            server.close();
        }
    }
}
