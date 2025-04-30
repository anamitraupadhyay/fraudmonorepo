package org.frauddetection.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

import org.json.JSONObject;
import org.frauddetection.model.TransactionData;
import org.frauddetection.service.MerchantAnalyticsHandler;

@WebServlet("/merchant-analytics")
public class MerchantAnalyticServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        TransactionData transactionData = (TransactionData) request.getAttribute("transactionData");
        JSONObject result = (JSONObject) request.getAttribute("result");
        
        if (transactionData == null || result == null) {
            System.err.println("MerchantAnalyticServlet: Missing transaction data or result");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(new JSONObject()
                .put("error", "Missing transaction data or result")
                .toString());
            return;
        }
        
        try {
            
            MerchantAnalyticsHandler analyticsHandler = new MerchantAnalyticsHandler();
            JSONObject analyticsResult = analyticsHandler.processMerchantAnalytics(transactionData);
            
            
            if (analyticsResult.has("analytics")) {
                result.put("analytics", analyticsResult.getJSONObject("analytics"));
            }
            
            
            response.setContentType("application/json");
            response.getWriter().write(result.toString());
            
        } catch (Exception e) {
            System.err.println("Error in merchant analytics: " + e.getMessage());
            e.printStackTrace();
            
            
            response.setContentType("application/json");
            response.getWriter().write(result.toString());
        }
    }
}