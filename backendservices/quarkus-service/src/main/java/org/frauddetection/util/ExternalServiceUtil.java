package org.frauddetection.util;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class ExternalServiceUtil {

    public static JSONObject callPythonModel(JSONObject requestData) throws IOException {
        System.out.println("Sending request to ML service: " + requestData.toString());
        //console debug point
        
        // Add retry logic
        int maxRetries = 3;
        int retryDelayMs = 1000;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            HttpURLConnection conn = null;
            try {
                URL url = URI.create("http://python-quarkus-service:5000/predict").toURL();
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(3000);

                // Send the JSON payload
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(requestData.toString().getBytes());
                }

                // Read the response
                StringBuilder responseBuilder = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        responseBuilder.append(line);
                    }
                    String fullResponse = responseBuilder.toString();
                    return new JSONObject(fullResponse);
                }
            } catch (IOException e) {
                System.err.println("ML service error (attempt " + attempt + "): " + e.getMessage());
                //console debug point
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(retryDelayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    // Last attempt failed
                    JSONObject errorResponse = new JSONObject();
                    errorResponse.put("prediction", 0);
                    errorResponse.put("reason", "ML service unavailable - using safe default");
                    return errorResponse;
                }
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
    }
}
