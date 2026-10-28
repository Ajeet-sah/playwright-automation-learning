package com.albertapayments.utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * TestDataReader parses structured test data from JSON files.
 */
public class TestDataReader {

    private static final Logger logger = LogManager.getLogger(TestDataReader.class);
    private static final String DEFAULT_TESTDATA_PATH = "src/test/resources/testdata/testData.json";
    private static JsonNode rootNode;

    private TestDataReader() {
        // Prevent direct instantiation
    }

    /**
     * Loads the JSON test data file into memory.
     */
    public static synchronized void loadTestData() {
        if (rootNode == null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                rootNode = mapper.readTree(new File(DEFAULT_TESTDATA_PATH));
                logger.info("Test data loaded successfully from: {}", DEFAULT_TESTDATA_PATH);
            } catch (IOException e) {
                logger.error("Failed to load test data JSON from {}: {}", DEFAULT_TESTDATA_PATH, e.getMessage());
                throw new RuntimeException("Could not load test data from " + DEFAULT_TESTDATA_PATH, e);
            }
        }
    }

    /**
     * Retrieves a string value using a dot-delimited JSON path (e.g. "loginData.validUser.username").
     *
     * @param jsonPath Path expression.
     * @return String value, or empty string if not found.
     */
    public static String getData(String jsonPath) {
        if (rootNode == null) {
            loadTestData();
        }
        String[] tokens = jsonPath.split("\\.");
        JsonNode current = rootNode;
        for (String token : tokens) {
            if (current == null) {
                return "";
            }
            current = current.get(token);
        }
        return current != null ? current.asText() : "";
    }
}
