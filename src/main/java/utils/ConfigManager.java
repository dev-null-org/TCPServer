package utils;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ConfigManager {
    private static ConfigManager instance;

    private final JSONObject configData;

    private ConfigManager() {
        JSONObject data = null;
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("config.json");

        if (inputStream != null) {
            JSONParser parser = new JSONParser();
            try {
                data = (JSONObject) parser.parse(new BufferedReader(new InputStreamReader(inputStream)));
            } catch (IOException | ParseException e) {
                e.printStackTrace();
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
