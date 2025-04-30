package org.frauddetection.merchantanalytics;

import java.sql.*;
import java.util.Calendar;
import org.json.JSONObject;
import org.frauddetection.db.DataBaseTestCases;

public class MerchantAnalytics {

    // Add constants for repeated values
    private static final double MERCHANT_RADIUS = 0.01; // ~1km radius
    private static final int MIN_TRANSACTIONS = 10;     // Minimum sample size
    private static final double HIGH_RISK_THRESHOLD = 5.0;  // % for high risk
    private static final double MEDIUM_RISK_THRESHOLD = 1.0; // % for medium risk

    /**
     * Test Case 1: Check if transaction is at high-risk hour
     */
    public static boolean isHighRiskHour(double merchantLat, double merchantLong, long unixTime) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            // Extract hour from unix timestamp
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(unixTime * 1000L);
            int currentHour = cal.get(Calendar.HOUR_OF_DAY);
            
            conn = DataBaseTestCases.getConnection();
            String query = "SELECT " +
                         "COUNT(*) as total_txns, " +
                         "SUM(CASE WHEN fl.id IS NOT NULL THEN 1 ELSE 0 END) as fraud_count " +
                         "FROM transactions t " +
                         "LEFT JOIN fraud_logs fl ON t.cc_num = fl.cc_num AND " +
                         "t.unix_time = JSON_EXTRACT(fl.transaction_data, '$.unix_time') " +
                         "WHERE ABS(t.merch_lat - ?) < " + MERCHANT_RADIUS + " AND ABS(t.merch_long - ?) < " + MERCHANT_RADIUS + " " +
                         "AND HOUR(FROM_UNIXTIME(t.unix_time)) = ?";
            
            stmt = conn.prepareStatement(query);
            stmt.setDouble(1, merchantLat);
            stmt.setDouble(2, merchantLong);
            stmt.setInt(3, currentHour);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                int totalTxns = rs.getInt("total_txns");
                int fraudCount = rs.getInt("fraud_count");
                
                return (totalTxns >= MIN_TRANSACTIONS && ((double)fraudCount / totalTxns) > HIGH_RISK_THRESHOLD / 100.0);
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error in isHighRiskHour: " + e.getMessage());
            return false;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Test Case 2: Get merchant risk score
     */
    public static JSONObject getMerchantRiskScore(double merchantLat, double merchantLong) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        JSONObject result = new JSONObject();
        
        try {
            conn = DataBaseTestCases.getConnection();
            
            String query = "SELECT " +
                         "COUNT(*) as total_txns, " +
                         "COUNT(DISTINCT cc_num) as unique_cards, " +
                         "SUM(CASE WHEN fl.id IS NOT NULL THEN 1 ELSE 0 END) as fraud_count, " +
                         "AVG(amt) as avg_amount " +
                         "FROM transactions t " +
                         "LEFT JOIN fraud_logs fl ON t.cc_num = fl.cc_num AND " +
                         "t.unix_time = JSON_EXTRACT(fl.transaction_data, '$.unix_time') " +
                         "WHERE ABS(t.merch_lat - ?) < " + MERCHANT_RADIUS + " AND ABS(t.merch_long - ?) < " + MERCHANT_RADIUS;
            
            stmt = conn.prepareStatement(query);
            stmt.setDouble(1, merchantLat);
            stmt.setDouble(2, merchantLong);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                int totalTxns = rs.getInt("total_txns");
                int uniqueCards = rs.getInt("unique_cards");
                int fraudCount = rs.getInt("fraud_count");
                double avgAmount = rs.getDouble("avg_amount");
                
                // Calculate metrics
                double fraudRate = totalTxns > 0 ? (fraudCount * 100.0 / totalTxns) : 0;
                double cardDiversity = totalTxns > 0 ? ((double)uniqueCards / totalTxns) : 0;
                
                // Build response
                result.put("merchant_location", merchantLat + "," + merchantLong);
                result.put("total_transactions", totalTxns);
                result.put("unique_cards", uniqueCards);
                result.put("fraud_transactions", fraudCount);
                result.put("fraud_rate_percent", fraudRate);
                result.put("card_diversity", cardDiversity);
                result.put("average_transaction_amount", avgAmount);
                
                // Assign risk level
                if (fraudRate > HIGH_RISK_THRESHOLD) {
                    result.put("risk_level", "high");
                } else if (fraudRate > MEDIUM_RISK_THRESHOLD) {
                    result.put("risk_level", "medium");
                } else {
                    result.put("risk_level", "low");
                }
            }
            
            return result;
        } catch (SQLException e) {
            result.put("error", "Database error: " + e.getMessage());
            return result;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Helper to close database resources
     */
    private static void closeResources(ResultSet rs, Statement stmt, Connection conn) {
        try { if (rs != null) rs.close(); } catch (SQLException e) {}
        try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
        try { if (conn != null) conn.close(); } catch (SQLException e) {}
    }
}