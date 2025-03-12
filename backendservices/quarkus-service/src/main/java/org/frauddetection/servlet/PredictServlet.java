package org.frauddetection.servlet;

import org.json.JSONObject;
import org.frauddetection.service.FraudDetectionHandler;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;

@WebServlet("/predict")
public class PredictServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Read JSON data from request
        StringBuilder buffer = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
        }

        JSONObject requestData = new JSONObject(buffer.toString());

        // Delegate to FraudDetectionHandler for processing
        FraudDetectionHandler handler = new FraudDetectionHandler();
        JSONObject jsonResponse = handler.processTransaction(requestData);

        // Send the response
        response.setContentType("application/json");
        response.getWriter().write(jsonResponse.toString());
    }
}