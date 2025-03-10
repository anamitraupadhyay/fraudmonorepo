package org.frauddetection.config;

/**
 * Configuration constants for the application
 */
public class AppConfig {
    // Flask server configuration
    public static final String FLASK_SERVER_URL = "http://localhost:5000";
    public static final String FLASK_ENDPOINT = FLASK_SERVER_URL + "/receive-data";

    // Content types
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String ENCODING_UTF8 = "UTF-8";

    // Private constructor to prevent instantiation
    private AppConfig() {
        // Utility class, no instantiation
    }
}