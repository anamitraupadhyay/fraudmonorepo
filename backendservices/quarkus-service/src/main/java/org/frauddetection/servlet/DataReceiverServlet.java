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
import jakarta.servlet.RequestDispatcher;

@WebServlet("/data-handler")
public class DataReceiverServlet extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        StringBuilder jsonPayload = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonPayload.append(line);
            }
        }

        try {
            
            JSONObject requestJson = new JSONObject(jsonPayload.toString());
            
            
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
            
            FraudDetectionHandler handler = new FraudDetectionHandler();
            JSONObject result = handler.processTransaction(transactionData);

            
            request.setAttribute("requestJson",requestJson);
            request.setAttribute("transactionData",transactionData);
            request.setAttribute("result", result);
            

            
            RequestDispatcher dispatcherobj = request.getRequestDispatcher("/merchant-analytics");
            dispatcherobj.forward(request, response);
            
            
            response.setContentType("application/json");
            response.getWriter().write(result.toString());
        } catch (Exception e) {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            
            JSONObject errorResponse = new JSONObject()
                .put("error", e instanceof JSONException ? "JSON parsing error" : "Processing error")
                .put("message", e.getMessage());
                
            response.getWriter().write(errorResponse.toString());
        }
    }
}