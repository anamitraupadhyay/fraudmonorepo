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
        System.out.println("Calling ML service with payload: " + requestData.toString());

        URL url = URI.create("http://python-quarkus-service:5000/predict").toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Send JSON body
        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestData.toString().getBytes());
        }

        // Read response
        StringBuilder responseBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                responseBuilder.append(line);
            }
        }

        conn.disconnect();
        return new JSONObject(responseBuilder.toString());
    }
}
