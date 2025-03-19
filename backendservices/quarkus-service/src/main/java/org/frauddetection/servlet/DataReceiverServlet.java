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
        StringBuilder jsonPayloadfromrequest = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonPayloadfromrequest.append(line);
            }
        }
        // susbtitute this by JSON.parse as my ambition was json->object->json and in obj format the db and testcases will be done
        // Step 2: Parse JSON data and process
        try {
            JSONObject requestData = new JSONObject(jsonPayloadfromrequest.toString());

            // Step 3: Process the transaction using FraudDetectionHandler
            FraudDetectionHandler handler = new FraudDetectionHandler();
            JSONObject result = handler.processTransaction(requestData); //main method which processes the transaction and returns the result

            // Step 4: Send the result back to the client
            response.setContentType("application/json");
            response.getWriter().write(result.toString());

        } catch (Exception e) {
            // Step 5: Handle exceptions and send error response
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            /**
             * // Create a single error response object with appropriate details
             *  // improv code by llm
            String errorType = e instanceof org.json.JSONException ? "JSON parsing error" : "Processing error";
            JSONObject errorResponse = new JSONObject()
                .put("error", errorType)
                .put("message", e.getMessage());
                
            response.getWriter().write(errorResponse.toString());
             */
        }
    }
}