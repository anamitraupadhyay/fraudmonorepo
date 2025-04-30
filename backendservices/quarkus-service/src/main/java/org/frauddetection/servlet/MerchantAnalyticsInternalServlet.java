package org.frauddetection.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.json.JSONObject;
import org.frauddetection.model.TransactionData;
import org.frauddetection.merchantanalytics.MerchantAnalytics;

@WebServlet("/merchant-analytics-internal")
public class MerchantAnalyticsInternalServlet extends HttpServlet {
    
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Get transaction data and result from request
        TransactionData txnData = (TransactionData) request.getAttribute("transaction_data");
        JSONObject result = (JSONObject) request.getAttribute("transaction_result");
        
        // Safety checks - make sure we have valid data
        if (txnData == null || result == null) {
            System.err.println("MerchantAnalyticsInternalServlet: Missing transaction data or result");
            return;  // Exit early if data is missing
        }
        
        // Only add analytics if transaction isn't flagged as fraud
        if (result.optInt("prediction", 0) == 0) {
            try {
                double merchantLat = txnData.getMerchLat();
                double merchantLong = txnData.getMerchLon();
                long unixTime = txnData.getUnixTime();
                
                // Create analytics object
                JSONObject analytics = new JSONObject();
                
                // Test Case 1: Check if this is a high-risk hour
                boolean isHighRiskHour = MerchantAnalytics.isHighRiskHour(
                    merchantLat, merchantLong, unixTime);
                    
                analytics.put("is_high_risk_hour", isHighRiskHour);
                
                if (isHighRiskHour) {
                    // Test Case 2: Get merchant risk score for additional context
                    JSONObject riskScore = MerchantAnalytics.getMerchantRiskScore(
                        merchantLat, merchantLong);
                    
                    if (riskScore != null && !riskScore.has("error")) {
                        analytics.put("merchant_risk", riskScore);
                        
                        // Add warning based on merchant risk level
                        String riskLevel = riskScore.optString("risk_level", "low");
                        if ("high".equals(riskLevel)) {
                            result.put("warning", "High-risk merchant during high-risk hour");
                        } else {
                            result.put("warning", "Transaction occurring during historically high-risk hour");
                        }
                    } else {
                        // Just set the basic warning if we couldn't get a risk score
                        result.put("warning", "Transaction occurring during historically high-risk hour");
                    }
                }
                
                // Add analytics to result
                result.put("analytics", analytics);
            } catch (Exception e) {
                System.err.println("Error in merchant analytics processing: " + e.getMessage());
                // Don't let analytics errors break the main flow
            }
        }
    }
}