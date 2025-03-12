package org.frauddetection.servlet;

import org.frauddetection.service.FraudDetectionHandler;
import org.json.JSONObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

@WebServlet("/data-handler")
public class DataReceiverServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Step 1: Read JSON data from request body
        StringBuilder jsonPayload = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonPayload.append(line);
            }
        }

        // Step 2: Parse JSON data and process
        try {
            JSONObject requestData = new JSONObject(jsonPayload.toString());

            // Step 3: Process the transaction using FraudDetectionHandler
            FraudDetectionHandler handler = new FraudDetectionHandler();
            JSONObject result = handler.processTransaction(requestData);

            // Step 4: Send the result back to the client
            response.setContentType("application/json");
            response.getWriter().write(result.toString());

        } catch (Exception e) {
            // Step 5: Handle errors
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("error", "Invalid input data: " + e.getMessage());
            response.setContentType("application/json");
            response.getWriter().write(errorResponse.toString());
        }
    }
}