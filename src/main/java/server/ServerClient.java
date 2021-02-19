package server;

import commands.Command;
import commands.CommandManager;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;

public class ServerClient implements Runnable, Closeable {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    private int id;

    private SocketAddress remoteIp;

    private TCPServer server;

    private boolean inQueue;

    private boolean closed;

    public ServerClient(Socket socket, TCPServer server, boolean inQueue) {
        initiateVariables(socket, server, inQueue);
    }

    public void initiateVariables(Socket socket, TCPServer server, boolean inQueue) {
        this.closed = false;
        this.id = this.hashCode();
        this.socket = socket;
        this.server = server;
        this.inQueue = inQueue;
        try {
            if (!inQueue) {
                InputStreamReader reader = new InputStreamReader(socket.getInputStream());
                input = new BufferedReader(reader);
            }
            output = new PrintWriter(socket.getOutputStream(), true);
            remoteIp = socket.getRemoteSocketAddress();
            if (inQueue) {
                println("You are in QUEUE wait to get started");
            } else {
                println("Welcome you can now use commands");
            }
        } catch (IOException e) {
            close();
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public SocketAddress remoteIpAddress() {
        return remoteIp;
    }

    @Override
    public void run() {
        if (inQueue) {
            inQueue = false;
            try {
                long skippedNumberOfChars = socket.getInputStream().skip(socket.getInputStream().available());
                server.log("SERVER-CLIENT(" + id + "): skipped " + skippedNumberOfChars + " client was in queue");
                InputStreamReader reader = new InputStreamReader(socket.getInputStream());
                input = new BufferedReader(reader);
            } catch (IOException e) {
                close();
                e.printStackTrace();
            }
            println("Welcome you can now use commands");
        }
        CommandManager commandManager = CommandManager.getInstance();
        while (!closed) {
            String inputString;
            try {
                inputString = this.readLine();
            } catch (IOException e) {
                close();
                break;
            }
            if(inputString==null){
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

    public void println(String message) {
        server.log("SERVER-CLIENT(" + id + ") printing: " + message + "\u001B[0m");
        output.println(message);
    }

    public void print(String message) {
        server.log("SERVER-CLIENT(" + id + ") printing: " + message + "\u001B[0m");
        output.print(message);
    }

    public String readLine() throws IOException {
        IOException exception;
        try {
            String in = input.readLine();
            server.log("SERVER-CLIENT(" + id + "): " + in + "\u001B[0m");
            return in;
        } catch (IOException e) {
            exception=e;
            this.close();
        }
        throw exception;
    }

    @Override
    public void close() {
        closed = true;
        if (socket != null) {
            try {
                println("Closing this connection bey.");
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        server.remove(this);
    }
}
