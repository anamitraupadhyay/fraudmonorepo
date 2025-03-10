package org.frauddetection.servlet;

import org.frauddetection.client.HttpClient;
import org.frauddetection.model.FormData;
import org.frauddetection.util.JsonUtils;
import org.frauddetection.config.AppConfig;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Servlet that processes form data and communicates with Flask
 */
@WebServlet("/process-data")
public class DataProcessorServlet extends HttpServlet {

    // Flask server endpoint removed as taken from AppConfig

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Retrieve the form data from the request
        FormData formData = (FormData) request.getAttribute("formData");
        if (formData == null) {
            // Handle error case
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"No form data found\"}");
            return;
        }

        // Convert form data to JSON
        String jsonData = JsonUtils.toJson(formData);

        // Send data to Flask
        HttpClient httpClient = new HttpClient();
        try {
            String flaskResponse = httpClient.sendJsonPost(AppConfig.FLASK_ENDPOINT, jsonData);

            // Send Flask's response back to the client
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(flaskResponse);
        } catch (IOException | URISyntaxException e) {
            // Log the error
            getServletContext().log("Error communicating with Flask", e);

            // Send error response
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Error communicating with Flask server\"}");
        }
    }
}