package org.frauddetection.client;

import org.frauddetection.config.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * HTTP Client for sending requests to external services
 */
public class HttpClient {

    /**
     * Sends a POST request with JSON data to the specified URL
     * 
     * @param targetUrl The URL to send the request to
     * @param jsonData  The JSON data to send
     * @param jsonData2 wtf? later changed it to outputEndPointString
     * @return The response from the server
     * @throws IOException If communication fails
     */
   public String sendJsonPost(String outputEndpointString, String jsonData) throws IOException, URISyntaxException {
       // Create and configure the connection using the constant from AppConfig
       URL url = new URI(AppConfig.FLASK_ENDPOINT).toURL();
       HttpURLConnection connection = (HttpURLConnection) url.openConnection();
       connection.setRequestMethod("POST");
       connection.setRequestProperty("Content-Type", AppConfig.CONTENT_TYPE_JSON);
       connection.setDoOutput(true);
   
       // Send the data
       try (OutputStream outputStream = connection.getOutputStream()) {
           outputStream.write(jsonData.getBytes(AppConfig.ENCODING_UTF8));
           outputStream.flush();
       }
   
       // Get the response
       StringBuilder responseBuilder = new StringBuilder();
       try (BufferedReader reader = new BufferedReader(
               new InputStreamReader(connection.getInputStream(), AppConfig.ENCODING_UTF8))) {
   
           String line;
           while ((line = reader.readLine()) != null) {
               responseBuilder.append(line);
           }
       }
   
       return responseBuilder.toString();
   }
}