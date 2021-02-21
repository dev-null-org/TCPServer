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
            }

            server = new TCPServer(portNumber, maxNumberOfClients, logging);

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
