package config;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

public class ConfigManager {
    private static ConfigManager instance;

    private final JSONObject configData;

    private ConfigManager() {
        JSONObject data = null;
        ClassLoader classLoader = getClass().getClassLoader();
        URL url = classLoader.getResource("config.json");
        if (url != null) {
            File configFile = new File(url.getFile());
            System.out.println(configFile);
            if (configFile.exists()) {
                JSONParser parser = new JSONParser();
                try {
                    data = (JSONObject) parser.parse(new FileReader(configFile));
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        configData = data;
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    public boolean configFileLoaded() {
        return configData != null;
    }

    public Object getProperty(Object key) {
        if (configData != null) {
            return configData.get(key);
        }
        return null;
    }
}
