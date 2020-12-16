package server;

import commands.Command;
import commands.CommandManager;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;

public class ServerClient implements Runnable,Closeable {
    private final Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    private final int id;

    private SocketAddress remoteIp;

    private final TCPServer server;

    private boolean inQueue;

    private boolean closed;

    public ServerClient(Socket socket, TCPServer server, boolean inQueue) {
        closed=false;
        this.id=this.hashCode();
        this.socket=socket;
        this.server=server;
        this.inQueue=inQueue;
        try {
            if(!inQueue){
                InputStreamReader reader = new InputStreamReader(socket.getInputStream());
                input = new BufferedReader(reader);
            }
            output = new PrintWriter(socket.getOutputStream(), true);
            remoteIp=socket.getRemoteSocketAddress();
            if(inQueue){
                output.println("You are in QUEUE wait to get started");
            }else{
                output.println("Welcome you can now use commands");
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
        if(inQueue){
            inQueue=false;
            try {
                long skippedNumberOfChars=socket.getInputStream().skip(socket.getInputStream().available());
                server.log("SERVER-CLIENT("+id+"): skipped "+skippedNumberOfChars+" client was in queue");
                InputStreamReader reader = new InputStreamReader(socket.getInputStream());
                input = new BufferedReader(reader);
            } catch (IOException e) {
                close();
                e.printStackTrace();
            }
            output.println("Welcome you can now use commands");
        }
        CommandManager commandManager=CommandManager.getInstance();
        while (!closed) {
            String inputString;
            try {
                inputString = input.readLine();
                if (inputString == null) {
                    close();
                    break;
                }
                server.log("SERVER-CLIENT(" + id + "): " + inputString);
                Command command = commandManager.getCommand(inputString);
                if (command != null) {
                    command.execute(this, inputString);
                }else{
                    output.println("Unknown command: " + inputString + " use 'help' to see all command available");
                }
            } catch (IOException e) {
                close();
                e.printStackTrace();
                break;
            }
        }
    }
    public void println(String message){
        output.println(message);
    }
    public String readLine() throws IOException {
        return input.readLine();
    }
    @Override
    public void close() {
        closed=true;
        if(socket!=null){
            try {
                output.println("Closing this connection bey.");
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        server.remove(this);
    }
}
