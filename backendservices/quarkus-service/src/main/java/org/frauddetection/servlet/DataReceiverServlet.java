package org.frauddetection.servlet;

import org.frauddetection.service.FraudDetectionHandler;
import org.frauddetection.model.TransactionData;
import org.json.JSONObject;
import org.json.JSONException;

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
        // Read JSON data
        StringBuilder jsonPayload = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonPayload.append(line);
            }
        }

        try {
            // Parse to JSONObject
            JSONObject requestJson = new JSONObject(jsonPayload.toString());
            
            // Convert to TransactionData manually
            TransactionData transactionData = new TransactionData(
                requestJson.getLong("cc_num"),
                requestJson.getDouble("amt"),
                requestJson.getString("zip"),
                requestJson.getDouble("lat"),
                requestJson.getDouble("long"),
                requestJson.getInt("city_pop"),
                requestJson.getLong("unix_time"),
                requestJson.getDouble("merch_lat"),
                requestJson.getDouble("merch_long")
            );
            
            // Process using handler with TransactionData
            FraudDetectionHandler handler = new FraudDetectionHandler();
            JSONObject result = handler.processTransaction(transactionData);
            
            // Send result back
            response.setContentType("application/json");
            response.getWriter().write(result.toString());
        } catch (Exception e) {
            // Error handling
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            
            JSONObject errorResponse = new JSONObject()
                .put("error", e instanceof JSONException ? "JSON parsing error" : "Processing error")
                .put("message", e.getMessage());
                
            response.getWriter().write(errorResponse.toString());
        }
    }
}