package org.goflex.wp2.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by bijay on 8/30/17.
 */
public class FOAConfig {
    private static final Logger logger = LoggerFactory.getLogger(FOAConfig.class);
    private final Properties properties;

    public FOAConfig(String key, String def) {
        String filename = System.getProperty(key, def);
        properties = new Properties();

        try {
            properties.load(new FileInputStream(filename));
        } catch (FileNotFoundException ex) {
            logger.info("Properties file not found loading default properties");
        } catch (IOException e) {
            logger.info("Fail to read property file {}", filename, e);
        }

    }

    public String getString(String key, String def) {
        return properties.getProperty(key, def);
    }

    public int getInt(String key, int def) {
        int val = def;
        try {
            val = Integer.parseInt(properties.getProperty(key, Integer.toString(def)));
        } catch (NumberFormatException e) {
            logger.info("Unable to parse integer value for key {}", key, e);
        }

        return val;
    }

    public boolean getBoolean(String key, String def) {
        return Boolean.parseBoolean(properties.getProperty(key, def));
    }
}
