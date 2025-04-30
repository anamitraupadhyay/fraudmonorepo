package org.frauddetection.service;

import org.frauddetection.db.DataBaseTestCases;
import org.frauddetection.model.TransactionData;
import org.frauddetection.model.*;

import org.json.JSONObject;

public class MerchantAnalyticsHandler {
    

    public JSONObject processMerchantAnalytics(TransactionData txnData) {
        if (txnData == null) {
            return new JSONObject().put("error", "No transaction data provided");
        }

        try {
            JSONObject analytics = new JSONObject();
            
            
            boolean isHighRiskHour = isHighRiskHour(txnData.getMerchLat(), txnData.getMerchLon(), txnData.getUnixTime());
            analytics.put("isHighRiskHour", isHighRiskHour);
            
            
            JSONObject merchantRisk = getMerchantRiskScore(txnData.getMerchLat(), txnData.getMerchLon());
            
            if (merchantRisk != null && !merchantRisk.has("error")) {
                analytics.put("merchantRisk", merchantRisk);
            }
            
            return new JSONObject().put("analytics", analytics);
            
        } catch (Exception e) {
            System.err.println("Error in merchant analytics processing: " + e.getMessage());
            return new JSONObject().put("error", "Analytics processing error: " + e.getMessage());
        }
    }
    
    
    private boolean isHighRiskHour(double merchantLat, double merchantLong, long unixTime) {
        JSONObject hourData = DataBaseTestCases.getHighRiskHour(unixTime);
        
        if (hourData != null && !hourData.has("error")) {
            
            int fraudCount = hourData.optInt("fraud_count", 0);
            int totalCount = hourData.optInt("total_count", 0);
            
            
            double fraudRate = totalCount > 0 ? (fraudCount * 100.0 / totalCount) : 0;
            return fraudRate > 5.0; 
        }
        
        return false;
    }
    
    
    private JSONObject getMerchantRiskScore(double merchantLat, double merchantLong) {
        MerchDataNeeds merchantData = DataBaseTestCases.merchDataRequirements(merchantLat, merchantLong);
        
        if (merchantData == null) {
            return new JSONObject().put("error", "Could not retrieve merchant data");
        }
        
        
        JSONObject result = new JSONObject();
        result.put("merchantLocation", merchantData.getMerchantLocation());
        result.put("totalTransactions", merchantData.getTotalTransactions());
        result.put("uniqueCards", merchantData.getUniqueCards());
        result.put("fraudTransactions", merchantData.getFraudTransactions());
        result.put("fraudRatePercent", merchantData.getFraudRatePercent());
        result.put("cardDiversity", merchantData.getCardDiversity());
        result.put("averageTransactionAmount", merchantData.getAverageTransactionAmount());
        result.put("riskLevel", merchantData.getRiskLevel());
        
        return result;
    }
}