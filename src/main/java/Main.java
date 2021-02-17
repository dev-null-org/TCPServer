import server.TCPServer;

public class Main {
    public static void main(String[] args) {
        try {
            TCPServer server = new TCPServer(78, 5, true);

            Thread shutDownHookThread = new Thread(server.getShutDownHook());
            Runtime.getRuntime().addShutdownHook(shutDownHookThread);

            Thread serverThread = new Thread(server);
            serverThread.start();
            try {
                serverThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}