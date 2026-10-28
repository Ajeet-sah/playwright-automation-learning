package com.albertapayments.utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ConfigReader loads test execution properties from config.properties
 * and allows command-line system property overrides (-Dbrowser=firefox, -Dheadless=false).
 */
public class ConfigReader {

    private static final Logger logger = LogManager.getLogger(ConfigReader.class);
    private static Properties properties;
    private static final String CONFIG_FILE_PATH = "src/test/resources/config.properties";

    private ConfigReader() {
        // Private constructor to prevent direct instantiation
    }

    /**
     * Initializes and loads the properties file.
     *
     * @return Loaded Properties instance.
     */
    public static Properties initProperties() {
        if (properties == null) {
            properties = new Properties();
            try (InputStream input = new FileInputStream(CONFIG_FILE_PATH)) {
                properties.load(input);
                logger.info("Configuration properties loaded successfully from: {}", CONFIG_FILE_PATH);
            } catch (IOException e) {
                logger.error("Failed to load configuration properties from: {}. Error: {}", CONFIG_FILE_PATH, e.getMessage());
                throw new RuntimeException("Could not load config.properties from " + CONFIG_FILE_PATH, e);
            }
        }
        return properties;
    }

    /**
     * Retrieves property value with CLI system property priority.
     * If -Dkey=value is passed, it takes precedence over config.properties.
     *
     * @param key Property key name.
     * @return String value of the property, or null if not found.
     */
    public static String getProperty(String key) {
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.trim().isEmpty()) {
            return systemValue.trim();
        }
        if (properties == null) {
            initProperties();
        }
        String fileValue = properties.getProperty(key);
        return fileValue != null ? fileValue.trim() : null;
    }

    /**
     * Retrieves property value with fallback default value.
     *
     * @param key          Property key name.
     * @param defaultValue Default value if property is not found.
     * @return String value of property or defaultValue.
     */
    public static String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    /**
     * Retrieves boolean property value with fallback.
     *
     * @param key          Property key name.
     * @param defaultValue Default boolean value.
     * @return Boolean value.
     */
    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    /**
     * Retrieves integer property value with fallback.
     *
     * @param key          Property key name.
     * @param defaultValue Default int value.
     * @return Integer value.
     */
    public static int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key);
        try {
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer value for key '{}': {}. Falling back to default: {}", key, value, defaultValue);
            return defaultValue;
        }
    }
}
