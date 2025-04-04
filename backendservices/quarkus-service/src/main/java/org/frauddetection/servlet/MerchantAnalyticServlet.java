package org.frauddetection.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import org.json.JSONObject;
import org.frauddetection.merchantanalytics.MerchantAnalytics;

@WebServlet("/merchant-analytics")
public class MerchantAnalyticServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            String latParam = request.getParameter("lat");
            String lonParam = request.getParameter("lon");
            JSONObject result = new JSONObject();
            
            // Validate required parameters
            if (latParam == null || lonParam == null) {
                result.put("error", "Missing required parameters: lat and lon");
                out.print(result.toString());
                return;
            }
            
            double lat = Double.parseDouble(latParam);
            double lon = Double.parseDouble(lonParam);
            
            // Get merchant risk score (Test Case 2)
            JSONObject riskScore = MerchantAnalytics.getMerchantRiskScore(lat, lon);
            result.put("merchant_risk", riskScore);
            
            // Check if current hour is high risk (Test Case 1)
            String timeParam = request.getParameter("time");
            long unixTime = timeParam != null ? Long.parseLong(timeParam) 
                           : System.currentTimeMillis() / 1000; // Default to now
            
            boolean isHighRisk = MerchantAnalytics.isHighRiskHour(lat, lon, unixTime);
            result.put("is_high_risk_hour", isHighRisk);
            
            out.print(result.toString());
        } catch (NumberFormatException e) {
            JSONObject error = new JSONObject();
            error.put("error", "Invalid parameter format: " + e.getMessage());
            out.print(error.toString());
        } catch (Exception e) {
            JSONObject error = new JSONObject();
            error.put("error", "Error processing request: " + e.getMessage());
            out.print(error.toString());
        }
    }
}
