package Server;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;

public class TCPServer implements Runnable, Closeable {
    private final ServerSocket serverSocket;
    private final int maxNumberOfClients;
    private final ArrayList<ServerClient> clients;
    private final Queue<ServerClient> clientQueue;

    private final boolean logging;

    public TCPServer(int portNumber, int maxNumberOfClients,Boolean logging) throws IOException {
        clientQueue = new ArrayDeque<>();
        clients = new ArrayList<>();
        serverSocket = new ServerSocket(portNumber);
        this.maxNumberOfClients = maxNumberOfClients;
        this.logging=logging;
    }

    public TCPServer(int portNumber, int maxNumberOfClients) throws IOException {
        this(portNumber,maxNumberOfClients,false);
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
                }
            } else {
                break;
            }
        }
    }

    private void connectNewClient(Socket socket) {
        if (socket != null) {
            if (clients.size() < maxNumberOfClients) {
                log("SERVER: NEW client connected (" + socket.getRemoteSocketAddress() + ")");
                ServerClient client = new ServerClient(socket, this,false);
                Thread clientThread = new Thread(client);
                clients.add(client);
                clientThread.start();
            } else {
                log("SERVER: List is full putting client to queue");
                clientQueue.add(new ServerClient(socket, this,true));
            }
        }else{
            log("SERVER: error null socket");
        }
    }

    public void remove(ServerClient serverClient) {
        log("SERVER: removed client("+serverClient.getId()+") with IP:"+serverClient.remoteIpAddress());
        clients.remove(serverClient);
        if (clientQueue.size() > 0) {
            ServerClient client= clientQueue.poll();
            log("SERVER: NEW client connected (" + client.remoteIpAddress() + ")");
            Thread clientThread = new Thread(client);
            clients.add(client);
            clientThread.start();
        }
    }
    
    public void log(String message){
        if(logging){
            System.out.println(message);
        }
    }

    @Override
    public void close() {
        for(ServerClient client: clientQueue){
            client.close();
        }
        while (clients.size()>0) {
            ServerClient client=clients.get(0);
            client.close();
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Runnable getShutDownHook(){
        return new ShutDownHook(this);
    }

    private class ShutDownHook implements Runnable{
        private TCPServer server;
        public ShutDownHook(TCPServer server){
            this.server=server;
        }
        @Override
        public void run() {
            server.close();
        }
    }
}
