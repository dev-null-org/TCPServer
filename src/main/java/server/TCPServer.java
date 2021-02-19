package server;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Queue;

public class TCPServer implements Runnable, Closeable {

    private final int maxNumberOfClients;
    private final int maxNumberClientsInPool;
    private final int maxQueue;

    private final ServerSocket serverSocket;

    private final LinkedList<ServerClient> clients;
    private final Queue<ServerClient> clientQueue;

    private final LinkedList<ServerClient> usedClients;

    private final boolean logging;

    public TCPServer(int portNumber, int maxNumberOfClients, int maxNumberClientsInPool, int maxQueue, Boolean logging) throws IOException {
        this.clientQueue = new ArrayDeque<>();
        this.clients = new LinkedList<>();
        this.usedClients = new LinkedList<>();
        this.serverSocket = new ServerSocket(portNumber);
        this.maxNumberOfClients = maxNumberOfClients;
        this.maxNumberClientsInPool = maxNumberClientsInPool;
        this.maxQueue = maxQueue;
        this.logging = logging;

    }

    public TCPServer(int portNumber, int maxNumberOfClients, int maxNumberClientsInPool, int maxQueue) throws IOException {
        this(portNumber, maxNumberOfClients, maxNumberClientsInPool, maxQueue, false);
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
        if (socket != null) {
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
        } else {
            log("SERVER: error null socket");
        }
    }

    private ServerClient createServerClient(Socket socket, TCPServer server, boolean inQueue) {
        ServerClient client;
        if (usedClients.size() > 0) {
            log("SERVER: NEW client from object pool");
            client = usedClients.removeFirst();
            client.initiateVariables(socket, server, inQueue);
        } else {
            log("SERVER: NEW client");
            client = new ServerClient(socket, server, inQueue);
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

            usedClients.add(serverClient);
        }
    }

    public void log(String message) {
        if (logging) {
            System.out.println(message);
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
