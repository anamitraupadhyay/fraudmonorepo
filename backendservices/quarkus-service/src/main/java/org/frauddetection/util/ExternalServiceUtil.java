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
        try {
            URL url = URI.create("http://python-quarkus-service:5000/predict").toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(3000); // 3 second timeout

            // Send the JSON payload
            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestData.toString().getBytes());
            }

            // Read the FULL response (not just one line)
            StringBuilder responseBuilder = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    responseBuilder.append(line);
                }
                String fullResponse = responseBuilder.toString();
                //System.out.println("ML service full response: " + fullResponse); debug point
                return new JSONObject(fullResponse);
            }
        }
        catch (IOException e) {
            System.err.println("ML service error: " + e.getMessage());
            e.printStackTrace(); // Add this for more detailed error info
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("prediction", 0); // Default to non-fraud if service is down
            errorResponse.put("reason", "ML service unavailable - using safe default");
            return errorResponse;
        }
        finally {
            if(conn != null){
                conn.close();
            }
        }
    }
}