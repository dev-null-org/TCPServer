import commands.CommandServerClientFactory;
import robot_controller.RobotClientFactory;
import server.ServerClientFactory;
import server.TCPServer;
import utils.ConfigManager;

public class Main {
    public static void main(String[] args) {
        try {
            ConfigManager configManager = ConfigManager.getInstance();
            TCPServer server;
            int portNumber = 666;
            int maxNumberOfClients = 5;
            boolean logging = true;
            boolean terminator = false;
            if (configManager.configFileLoaded()) {
                Object value = configManager.getProperty("portNumber");
                if (value instanceof Long) {
                    portNumber = ((Long) value).intValue();
                }
                value = configManager.getProperty("maxNumberOfClients");
                if (value instanceof Long) {
                    maxNumberOfClients = ((Long) value).intValue();
                }
                value = configManager.getProperty("logging");
                if (value instanceof Boolean) {
                    logging = (Boolean) value;
                }
                value = configManager.getProperty("BotControllerMode");
                if (value instanceof Boolean) {
                    terminator = (Boolean) value;
                }
            }
            System.out.println("Starting TCP server on port: " + portNumber + " with maximum clients: " + maxNumberOfClients);
            if (terminator) System.out.println("BotControllerMode");

            ServerClientFactory factory = terminator ? RobotClientFactory.factory : CommandServerClientFactory.factory;

            server = new TCPServer(portNumber, maxNumberOfClients, 10, 50, logging, factory);

            Thread shutDownHookThread = new Thread(server.getShutDownHook());
            Runtime.getRuntime().addShutdownHook(shutDownHookThread);

            Thread serverThread = new Thread(server);
            serverThread.start();
            serverThread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
