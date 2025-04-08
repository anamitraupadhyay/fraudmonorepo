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
    public static void callKtorDataAnonymizationMicroservice(JSONObject data) throws IOException {
     System.out.println("Calling Ktor data anonymization microservce with no payload:" +data.toString());
     URI uriobj = URI.create("http://.../anonymize"); //no need to use .toURL() here as newBuilder works with only URI
     HttpRequest request = HttpRequest.newBuilder()
             .uri(uriobj)
             .header("Content-Type", "application/json")
             //.setDoOutput(false) nope wont work, its of HttpURLConnection not Http
             .POST(HttpRequest.BodyPublishers.ofString(data.toString()))
             .build();
             HttpClient client = HttpClient.newHttpClient();
             client.sendAsync(request, HttpResponse.Bodyhandler.discarding());
             //this is a fire and forget request, no need to wait for response
             //if you want to wait for response, use send() method instead of sendAsync()
             //and handle the response accordingly
             //also return type is void of the method no need to return anything, also If you're doing payload retrieval, then ofString() or ofByteArray() is required — and it’s already concise.
    }
}
