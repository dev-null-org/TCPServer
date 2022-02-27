package server;

import utils.ColorManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public abstract class BasicServerClient extends ServerClient {

    protected BufferedReader input;
    protected PrintWriter output;

    public BasicServerClient(Socket socket, TCPServer server, boolean inQueue, ColorManager.Color logColor) {
        super(socket, server, inQueue, logColor);
    }

    public void initiateVariables(Socket socket, TCPServer server, boolean inQueue, ColorManager.Color logColor) {
        super.initiateVariables(socket, server, inQueue, logColor);
        try {
            if (!inQueue) {
                InputStreamReader reader = new InputStreamReader(socket.getInputStream());
                input = new BufferedReader(reader);
            }
            output = new PrintWriter(socket.getOutputStream(), true);
            String welcomeMessage = welcomeMessage();
            if (welcomeMessage != null) {
                println(welcomeMessage);
            }
        } catch (IOException e) {
            close();
            e.printStackTrace();
        }
    }

    protected abstract String goodBeyMessage();

    protected abstract String welcomeMessage();


    @Override
    public void run() {
        this.fromQueueStart();
    }

    public void println(String message) {
        server.log(getIdentifier() + " printing: " + message + "\u001B[0m");
        output.println(message);
    }

    public void print(String message) {
        server.log(getIdentifier() + " printing: " + message + "\u001B[0m");
        output.print(message);
    }

    public String readLine() throws IOException {
        IOException exception;
        try {
            String in = input.readLine();
            server.log(getIdentifier() + ": " + in + "\u001B[0m");
            return in;
        } catch (IOException e) {
            exception = e;
            this.close();
        }
        throw exception;
    }

    protected void fromQueueStart() {
        if (inQueue) {
            inQueue = false;
            try {
                long skippedNumberOfChars = socket.getInputStream().skip(socket.getInputStream().available());
                server.log(getIdentifier() + ": skipped " + skippedNumberOfChars + " client was in queue");
                InputStreamReader reader = new InputStreamReader(socket.getInputStream());
                input = new BufferedReader(reader);
            } catch (IOException e) {
                close();
                e.printStackTrace();
            }
            String welcomeMessage = welcomeMessage();
            if (welcomeMessage != null) {
                println(welcomeMessage);
            }
        }
    }

    @Override
    public void close() {
        String goodBeyMessage = goodBeyMessage();
        if (goodBeyMessage != null) {
            println(goodBeyMessage);
        }
        super.close();
    }
}
