1. First, Create a MerchantAnalytics Package
package org.frauddetection.merchantanalytics;

import java.sql.*;
import java.util.Calendar;
import org.json.JSONObject;
import org.json.JSONArray;
import org.frauddetection.db.DataBaseTestCases;

public class MerchantAnalytics {

    /**
     * Test Case 1: Time Pattern Analysis - Checks if transaction is at high-risk hour
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
            
            // Get DB connection (using existing connection method)
            conn = DataBaseTestCases.getConnection();
            
            // Query to check if this hour has high fraud rate for this merchant location
            String query = "SELECT " +
                           "COUNT(*) as total_txns, " +
                           "SUM(CASE WHEN fl.id IS NOT NULL THEN 1 ELSE 0 END) as fraud_count " +
                           "FROM transactions t " +
                           "LEFT JOIN fraud_logs fl ON t.cc_num = fl.cc_num AND " +
                           "t.unix_time = JSON_EXTRACT(fl.transaction_data, '$.unix_time') " +
                           "WHERE ABS(t.merch_lat - ?) < 0.01 AND ABS(t.merch_long - ?) < 0.01 " +
                           "AND HOUR(FROM_UNIXTIME(t.unix_time)) = ?";
            
            stmt = conn.prepareStatement(query);
            stmt.setDouble(1, merchantLat);
            stmt.setDouble(2, merchantLong);
            stmt.setInt(3, currentHour);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                int totalTxns = rs.getInt("total_txns");
                int fraudCount = rs.getInt("fraud_count");
                
                // Consider high risk if >5% fraud rate with at least 10 transactions
                return (totalTxns >= 10 && ((double)fraudCount / totalTxns) > 0.05);
            }
            
            return false;
        } catch (SQLException e) {
            System.err.println("Error in isHighRiskHour: " + e.getMessage());
            return false;
        } finally {
            // Close resources
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
    }

    /**
     * Test Case 2: Merchant Risk Score
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
                           "WHERE ABS(t.merch_lat - ?) < 0.01 AND ABS(t.merch_long - ?) < 0.01";
            
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
                if (fraudRate > 5.0) {
                    result.put("risk_level", "high");
                } else if (fraudRate > 1.0) {
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
            // Close resources
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
    }

    /**
     * Test Case 3: Hourly Transaction Pattern Analysis
     */
    public static JSONArray getHourlyTransactionPattern(double merchantLat, double merchantLong) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        JSONArray result = new JSONArray();
        
        try {
            conn = DataBaseTestCases.getConnection();
            
            String query = "SELECT " +
                          "HOUR(FROM_UNIXTIME(unix_time)) as hour_of_day, " +
                          "COUNT(*) as txn_count, " +
                          "SUM(CASE WHEN fl.id IS NOT NULL THEN 1 ELSE 0 END) as fraud_count, " +
                          "AVG(amt) as avg_amount " +
                          "FROM transactions t " +
                          "LEFT JOIN fraud_logs fl ON t.cc_num = fl.cc_num AND " +
                          "t.unix_time = JSON_EXTRACT(fl.transaction_data, '$.unix_time') " +
                          "WHERE ABS(t.merch_lat - ?) < 0.01 AND ABS(t.merch_long - ?) < 0.01 " +
                          "GROUP BY hour_of_day " +
                          "ORDER BY hour_of_day";
            
            stmt = conn.prepareStatement(query);
            stmt.setDouble(1, merchantLat);
            stmt.setDouble(2, merchantLong);
            rs = stmt.executeQuery();
            
            // Create a full 24-hour dataset
            for (int hour = 0; hour < 24; hour++) {
                JSONObject hourData = new JSONObject();
                hourData.put("hour", hour);
                hourData.put("transaction_count", 0);
                hourData.put("fraud_count", 0);
                hourData.put("average_amount", 0.0);
                hourData.put("fraud_rate", 0.0);
                result.put(hourData);
            }
            
            // Fill in the actual data
            while (rs.next()) {
                int hour = rs.getInt("hour_of_day");
                int txnCount = rs.getInt("txn_count");
                int fraudCount = rs.getInt("fraud_count");
                double avgAmount = rs.getDouble("avg_amount");
                
                // Update the corresponding hour object
                JSONObject hourData = result.getJSONObject(hour);
                hourData.put("transaction_count", txnCount);
                hourData.put("fraud_count", fraudCount);
                hourData.put("average_amount", avgAmount);
                
                // Add fraud rate
                double fraudRate = txnCount > 0 ? (fraudCount * 100.0 / txnCount) : 0;
                hourData.put("fraud_rate", fraudRate);
                hourData.put("high_risk", fraudRate > 5.0);
            }
            
            return result;
        } catch (SQLException e) {
            // Return error info as first element
            JSONObject error = new JSONObject();
            error.put("error", "Database error: " + e.getMessage());
            result.put(error);
            return result;
        } finally {
            // Close resources
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
    }
}
2. Fix the MerchantAnalyticServlet
package org.frauddetection.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.PrintWriter;

import org.json.JSONObject;
import org.json.JSONArray;
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
            // Get query parameters
            String action = request.getParameter("action");
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
            
            // Process based on action parameter
            if ("risk".equals(action)) {
                // Test Case 2: Get merchant risk score
                JSONObject riskScore = MerchantAnalytics.getMerchantRiskScore(lat, lon);
                result.put("merchant_risk", riskScore);
            }
            else if ("hourly".equals(action)) {
                // Test Case 3: Get hourly transaction pattern
                JSONArray hourlyPattern = MerchantAnalytics.getHourlyTransactionPattern(lat, lon);
                result.put("hourly_pattern", hourlyPattern);
            }
            else if ("check-hour".equals(action)) {
                // Test Case 1: Check if current hour is high risk
                String timeParam = request.getParameter("time");
                long unixTime = timeParam != null ? Long.parseLong(timeParam) 
                               : System.currentTimeMillis() / 1000; // Default to now
                               
                boolean isHighRisk = MerchantAnalytics.isHighRiskHour(lat, lon, unixTime);
                result.put("is_high_risk_hour", isHighRisk);
            }
            else {
                // Default: return all analytics
                JSONObject riskScore = MerchantAnalytics.getMerchantRiskScore(lat, lon);
                JSONArray hourlyPattern = MerchantAnalytics.getHourlyTransactionPattern(lat, lon);
                
                result.put("merchant_risk", riskScore);
                result.put("hourly_pattern", hourlyPattern);
            }
            
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
3. Enhance FraudDetectionHandler to Use Time Pattern Analysis
package org.frauddetection.service;

import org.json.JSONObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;

import org.frauddetection.db.DataBaseTestCases;
import org.frauddetection.model.TransactionData;
import org.frauddetection.util.FraudDetectionUtils;
import org.frauddetection.util.ExternalServiceUtil;
import org.frauddetection.merchantanalytics.MerchantAnalytics;

@ApplicationScoped
public class FraudDetectionHandler {
    // Single speed threshold (300 km/h - high-speed train)
    private static final double MAX_SPEED_KMH = 300.0;

    @Inject
    FraudDetectionHandler handler;

    private JSONObject convertToJson(TransactionData data) {
        JSONObject json = new JSONObject();
        json.put("cc_num", data.getCcNum());
        json.put("amt", data.getAmt());
        json.put("zip", data.getZip());
        json.put("lat", data.getLat());
        json.put("long", data.getLon());
        json.put("city_pop", data.getCityPop());
        json.put("unix_time", data.getUnixTime());
        json.put("merch_lat", data.getMerchLat());
        json.put("merch_long", data.getMerchLon());
        return json;
    }

    public JSONObject processTransaction(TransactionData transactionData) throws IOException {
        // Extract transaction fields for test cases
        long ccNum = transactionData.getCcNum();
        double lat = transactionData.getLat();
        double lon = transactionData.getLon();
        long unixTime = transactionData.getUnixTime();
        double merchLat = transactionData.getMerchLat();
        double merchLon = transactionData.getMerchLon();
        
        // Prepare response
        JSONObject responseJson = new JSONObject();

        // Convert transaction data to JSON for logging
        JSONObject requestData = convertToJson(transactionData);
        
        // Get last transaction (if any)
        JSONObject lastTransaction = DataBaseTestCases.getLastTransaction(ccNum);
        
        // Simple decision flow: new user -> ML model, existing user → speed check, for later existing users-> ML model if under speed threshold
        if (lastTransaction == null) {
            // New user - use ML model
            responseJson = ExternalServiceUtil.callPythonModel(requestData);
            
            // Log if ML model predicts fraud
            if (responseJson.getInt("prediction") == 1) {
                DataBaseTestCases.logFraud(
                    ccNum,
                    responseJson.optString("reason", "ML model detected potential fraud"),
                    requestData
                );
            }
        }
        else {
            // Existing user - check speed between transactions
            long lastUnixTime = lastTransaction.getLong("unix_time");
            
            // Skip impossible time differences
            if (unixTime <= lastUnixTime) {
                responseJson.put("prediction", 0);
                responseJson.put("reason", "Transaction timestamp valid");
            } 
            else {
                // Calculate travel metrics
                double lastLat = lastTransaction.getDouble("lat");
                double lastLon = lastTransaction.getDouble("long");
                double distanceKm = FraudDetectionUtils.haversine(lastLat, lastLon, lat, lon);
                
                // Only check speed if distance is significant (>500km)
                if (distanceKm > 500.0) {
                    double timeDiffHours = (unixTime - lastUnixTime) / 3600.0;
                    double speedKmh = distanceKm / timeDiffHours;
                    
                    // Simple speed check - only rule we care about
                    if (speedKmh > MAX_SPEED_KMH) {
                        String reason = String.format("Impossible travel speed: %.2f km/h", speedKmh);
                        responseJson.put("prediction", 1);
                        responseJson.put("reason", reason);
                        DataBaseTestCases.logFraud(ccNum, reason, requestData);
                    } 
                    else {
                        responseJson.put("prediction", 0);
                        responseJson.put("reason", "Transaction speed normal");
                    }
                } 
                else {
                    // Distance too small to calculate meaningful speed
                    responseJson = ExternalServiceUtil.callPythonModel(requestData);
            
                    // Log if ML model predicts fraud
                    if (responseJson.getInt("prediction") == 1) {
                        DataBaseTestCases.logFraud(
                            ccNum,
                            responseJson.optString("reason", "ML model detected potential fraud"),
                            requestData
                        );
                    }
                }
            }
        }
        
        // Test Case 1: Check for high-risk hour if not already flagged as fraud
        if (responseJson.optInt("prediction", 0) == 0) {
            boolean isHighRiskHour = MerchantAnalytics.isHighRiskHour(
                transactionData.getMerchLat(), 
                transactionData.getMerchLon(), 
                transactionData.getUnixTime()
            );
            
            if (isHighRiskHour) {
                responseJson.put("warning", "Transaction occurring during historically high-risk hour");
            }
        }
        
        // Always store the transaction
        DataBaseTestCases.storeTransaction(transactionData);
        return responseJson;
    }
}
------
Implementing Merchant Analytics: A Java-centric Approach
Since the SQL queries seem daunting, let's take a Java-focused approach to implementing merchant analytics. I'll show you how to simplify implementation by moving logic from SQL to Java, similar to how you've used haversine() in FraudDetectionUtils.

Core Concepts to Understand First
Data Retrieval vs. Processing: We'll retrieve simpler data sets with basic SQL and handle analytics in Java
Merchant Location Approximation: Using a simple radius approach (like you've seen with ABS(lat - ?) < 0.01)
In-Memory Analytics: Processing data after it's retrieved rather than in SQL
Simplified Java-centric Implementation Approach
Test Case 1: Time Pattern Analysis
Instead of complex SQL:
public static boolean isHighRiskHour(double merchantLat, double merchantLong, long unixTime) {
    // Step 1: Get hour from transaction time
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(unixTime * 1000L);
    int currentHour = cal.get(Calendar.HOUR_OF_DAY);
    
    // Step 2: Fetch relevant transactions with simple SQL
    List<Transaction> transactions = fetchNearbyTransactions(merchantLat, merchantLong);
    
    // Step 3: Process in Java
    int totalCount = 0;
    int fraudCount = 0;
    
    for (Transaction txn : transactions) {
        // Extract hour from each transaction
        Calendar txnCal = Calendar.getInstance();
        txnCal.setTimeInMillis(txn.getUnixTime() * 1000L);
        int txnHour = txnCal.get(Calendar.HOUR_OF_DAY);
        
        // Only count transactions at the current hour
        if (txnHour == currentHour) {
            totalCount++;
            if (txn.isFraud()) {
                fraudCount++;
            }
        }
    }
    
    // Calculate fraud rate for this hour
    double fraudRate = totalCount > 0 ? (fraudCount * 100.0 / totalCount) : 0;
    
    // Consider high risk if >5% fraud with at least 10 transactions
    return (totalCount >= 10 && fraudRate > 5.0);
}

// Helper method to fetch transactions
private static List<Transaction> fetchNearbyTransactions(double lat, double lon) {
    List<Transaction> transactions = new ArrayList<>();
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    
    try {
        conn = DataBaseTestCases.getConnection();
        
        // Simpler SQL just gets basic transaction data within radius
        String query = "SELECT t.*, " +
                     "(SELECT COUNT(*) > 0 FROM fraud_logs fl " +
                     "WHERE t.cc_num = fl.cc_num AND " +
                     "t.unix_time = JSON_EXTRACT(fl.transaction_data, '$.unix_time')) as is_fraud " +
                     "FROM transactions t " +
                     "WHERE ABS(t.merch_lat - ?) < 0.01 " +
                     "AND ABS(t.merch_long - ?) < 0.01";
        
        pstmt = conn.prepareStatement(query);
        pstmt.setDouble(1, lat);
        pstmt.setDouble(2, lon);
        rs = pstmt.executeQuery();
        
        while (rs.next()) {
            Transaction txn = new Transaction();
            txn.setUnixTime(rs.getLong("unix_time"));
            txn.setFraud(rs.getBoolean("is_fraud"));
            transactions.add(txn);
        }
    } catch (SQLException e) {
        System.err.println("Error fetching transactions: " + e.getMessage());
    } finally {
        // Close resources
        // ...
    }
    
    return transactions;
}
Test Case 2: Merchant Risk Score
public static JSONObject getMerchantRiskScore(double merchantLat, double merchantLong) {
    JSONObject result = new JSONObject();
    
    // Step 1: Fetch transactions with simple query
    List<Transaction> transactions = fetchNearbyTransactions(merchantLat, merchantLong);
    
    // Step 2: Calculate metrics in Java
    Set<Long> uniqueCards = new HashSet<>();
    int totalTxns = transactions.size();
    int fraudCount = 0;
    double totalAmount = 0.0;
    
    for (Transaction txn : transactions) {
        uniqueCards.add(txn.getCcNum());
        if (txn.isFraud()) {
            fraudCount++;
        }
        totalAmount += txn.getAmount();
    }
    
    // Step 3: Calculate risk metrics
    double fraudRate = totalTxns > 0 ? (fraudCount * 100.0 / totalTxns) : 0;
    double cardDiversity = totalTxns > 0 ? ((double)uniqueCards.size() / totalTxns) : 0;
    double avgAmount = totalTxns > 0 ? (totalAmount / totalTxns) : 0;
    
    // Step 4: Build response
    result.put("merchant_location", merchantLat + "," + merchantLong);
    result.put("total_transactions", totalTxns);
    result.put("unique_cards", uniqueCards.size());
    result.put("fraud_transactions", fraudCount);
    result.put("fraud_rate_percent", fraudRate);
    result.put("card_diversity", cardDiversity);
    result.put("average_transaction_amount", avgAmount);
    
    // Assign risk level
    if (fraudRate > 5.0) {
        result.put("risk_level", "high");
    } else if (fraudRate > 1.0) {
        result.put("risk_level", "medium");
    } else {
        result.put("risk_level", "low");
    }
    
    return result;
}
Test Case 3: Hourly Transaction Pattern
public static JSONArray getHourlyTransactionPattern(double merchantLat, double merchantLong) {
    JSONArray result = new JSONArray();
    
    // Step 1: Initialize 24-hour data structure
    JSONObject[] hourlyData = new JSONObject[24];
    for (int i = 0; i < 24; i++) {
        JSONObject hourData = new JSONObject();
        hourData.put("hour", i);
        hourData.put("transaction_count", 0);
        hourData.put("fraud_count", 0);
        hourData.put("average_amount", 0.0);
        hourData.put("fraud_rate", 0.0);
        hourlyData[i] = hourData;
    }
    
    // Step 2: Fetch transactions with simple query
    List<Transaction> transactions = fetchNearbyTransactions(merchantLat, merchantLong);
    
    // Step 3: Process in Java
    double[] hourlyTotalAmount = new double[24];
    
    for (Transaction txn : transactions) {
        // Extract hour
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(txn.getUnixTime() * 1000L);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        
        // Update counts
        JSONObject hourData = hourlyData[hour];
        int currentCount = hourData.getInt("transaction_count");
        hourData.put("transaction_count", currentCount + 1);
        
        if (txn.isFraud()) {
            int fraudCount = hourData.getInt("fraud_count");
            hourData.put("fraud_count", fraudCount + 1);
        }
        
        // Update amount
        hourlyTotalAmount[hour] += txn.getAmount();
    }
    
    // Step 4: Calculate rates and averages
    for (int hour = 0; hour < 24; hour++) {
        JSONObject hourData = hourlyData[hour];
        int txnCount = hourData.getInt("transaction_count");
        int fraudCount = hourData.getInt("fraud_count");
        
        if (txnCount > 0) {
            double avgAmount = hourlyTotalAmount[hour] / txnCount;
            double fraudRate = (fraudCount * 100.0) / txnCount;
            
            hourData.put("average_amount", avgAmount);
            hourData.put("fraud_rate", fraudRate);
            hourData.put("high_risk", fraudRate > 5.0);
        }
        
        result.put(hourData);
    }
    
    return result;
}
Implementing Transaction Class
To support this approach, create a simple POJO to hold transaction data:
package org.frauddetection.merchantanalytics;

public class Transaction {
    private long ccNum;
    private double amount;
    private long unixTime;
    private boolean fraud;
    
    // Getters and setters
    public long getCcNum() { return ccNum; }
    public void setCcNum(long ccNum) { this.ccNum = ccNum; }
    
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    
    public long getUnixTime() { return unixTime; }
    public void setUnixTime(long unixTime) { this.unixTime = unixTime; }
    
    public boolean isFraud() { return fraud; }
    public void setFraud(boolean fraud) { this.fraud = fraud; }
}
-----
Implementing Merchant Analytics Without Complex SQL or Generics
Since you prefer the approach of using Java for handling logic (like your haversine() method) rather than complex SQL, and mentioned you can't use Java generics, here's how to implement the 3 test cases with simpler SQL and more Java processing:

1. Transaction Class (No Generics)
package org.frauddetection.merchantanalytics;

public class Transaction {
    private long ccNum;
    private double amount;
    private long unixTime;
    private boolean fraud;
    
    // Getters and setters
    public long getCcNum() { return ccNum; }
    public void setCcNum(long ccNum) { this.ccNum = ccNum; }
    
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    
    public long getUnixTime() { return unixTime; }
    public void setUnixTime(long unixTime) { this.unixTime = unixTime; }
    
    public boolean isFraud() { return fraud; }
    public void setFraud(boolean fraud) { this.fraud = fraud; }
}
2. Modified MerchantAnalytics Class
package org.frauddetection.merchantanalytics;

import java.sql.*;
import java.util.Calendar;
import org.json.JSONObject;
import org.json.JSONArray;
import org.frauddetection.db.DataBaseTestCases;

public class MerchantAnalytics {

    /**
     * Helper method to fetch transactions without using generics
     * Returns array of Transaction objects
     */
    private static Transaction[] fetchNearbyTransactions(double lat, double lon) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DataBaseTestCases.getConnection();
            
            // First count the transactions to size our array (avoiding ArrayList<>)
            String countQuery = "SELECT COUNT(*) as cnt FROM transactions t " +
                              "WHERE ABS(t.merch_lat - ?) < 0.01 " +
                              "AND ABS(t.merch_long - ?) < 0.01";
            
            pstmt = conn.prepareStatement(countQuery);
            pstmt.setDouble(1, lat);
            pstmt.setDouble(2, lon);
            rs = pstmt.executeQuery();
            
            int count = 0;
            if (rs.next()) {
                count = rs.getInt("cnt");
            }
            
            // Close first query resources
            rs.close();
            pstmt.close();
            
            // Early return if no transactions
            if (count == 0) {
                return new Transaction[0];
            }
            
            // Now fetch the transactions
            Transaction[] transactions = new Transaction[count];
            
            // Simpler SQL just gets basic transaction data within radius
            String query = "SELECT t.cc_num, t.amt, t.unix_time, " +
                         "(SELECT COUNT(*) > 0 FROM fraud_logs fl " +
                         "WHERE t.cc_num = fl.cc_num AND " +
                         "t.unix_time = JSON_EXTRACT(fl.transaction_data, '$.unix_time')) as is_fraud " +
                         "FROM transactions t " +
                         "WHERE ABS(t.merch_lat - ?) < 0.01 " +
                         "AND ABS(t.merch_long - ?) < 0.01";
            
            pstmt = conn.prepareStatement(query);
            pstmt.setDouble(1, lat);
            pstmt.setDouble(2, lon);
            rs = pstmt.executeQuery();
            
            int i = 0;
            while (rs.next() && i < transactions.length) {
                Transaction txn = new Transaction();
                txn.setCcNum(rs.getLong("cc_num"));
                txn.setAmount(rs.getDouble("amt"));
                txn.setUnixTime(rs.getLong("unix_time"));
                txn.setFraud(rs.getBoolean("is_fraud"));
                transactions[i++] = txn;
            }
            
            return transactions;
            
        } catch (SQLException e) {
            System.err.println("Error fetching transactions: " + e.getMessage());
            return new Transaction[0];
        } finally {
            // Close resources
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
    }

    /**
     * Test Case 1: Time Pattern Analysis - Checks if transaction is at high-risk hour
     * Uses Java processing rather than complex SQL
     */
    public static boolean isHighRiskHour(double merchantLat, double merchantLong, long unixTime) {
        // Get hour from transaction time
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(unixTime * 1000L);
        int currentHour = cal.get(Calendar.HOUR_OF_DAY);
        
        // Fetch all transactions for this merchant
        Transaction[] transactions = fetchNearbyTransactions(merchantLat, merchantLong);
        
        // No data = no risk
        if (transactions.length == 0) {
            return false;
        }
        
        // Process in Java
        int totalCount = 0;
        int fraudCount = 0;
        
        for (int i = 0; i < transactions.length; i++) {
            Transaction txn = transactions[i];
            
            // Extract hour from each transaction
            Calendar txnCal = Calendar.getInstance();
            txnCal.setTimeInMillis(txn.getUnixTime() * 1000L);
            int txnHour = txnCal.get(Calendar.HOUR_OF_DAY);
            
            // Only count transactions at the current hour
            if (txnHour == currentHour) {
                totalCount++;
                if (txn.isFraud()) {
                    fraudCount++;
                }
            }
        }
        
        // Calculate fraud rate for this hour
        double fraudRate = totalCount > 0 ? (fraudCount * 100.0 / totalCount) : 0;
        
        // Consider high risk if >5% fraud with at least 10 transactions
        return (totalCount >= 10 && fraudRate > 5.0);
    }

    /**
     * Test Case 2: Merchant Risk Score
     * Uses Java processing instead of complex SQL aggregate functions
     */
    public static JSONObject getMerchantRiskScore(double merchantLat, double merchantLong) {
        JSONObject result = new JSONObject();
        
        // Step 1: Fetch transactions with simple query
        Transaction[] transactions = fetchNearbyTransactions(merchantLat, merchantLong);
        
        // Step 2: Calculate metrics in Java
        // Using arrays instead of generic collections
        int totalTxns = transactions.length;
        int fraudCount = 0;
        double totalAmount = 0.0;
        
        // For unique cards (avoiding HashSet<Long>), use array of longs and track count
        long[] cardNumbers = new long[totalTxns]; // Worst case: all cards are unique
        int uniqueCardCount = 0;
        
        for (int i = 0; i < transactions.length; i++) {
            Transaction txn = transactions[i];
            
            // Add to total amount
            totalAmount += txn.getAmount();
            
            // Count frauds
            if (txn.isFraud()) {
                fraudCount++;
            }
            
            // Check if this card is already counted
            long currentCard = txn.getCcNum();
            boolean isNewCard = true;
            
            for (int j = 0; j < uniqueCardCount; j++) {
                if (cardNumbers[j] == currentCard) {
                    isNewCard = false;
                    break;
                }
            }
            
            if (isNewCard) {
                cardNumbers[uniqueCardCount++] = currentCard;
            }
        }
        
        // Step 3: Calculate risk metrics
        double fraudRate = totalTxns > 0 ? (fraudCount * 100.0 / totalTxns) : 0;
        double cardDiversity = totalTxns > 0 ? ((double)uniqueCardCount / totalTxns) : 0;
        double avgAmount = totalTxns > 0 ? (totalAmount / totalTxns) : 0;
        
        // Step 4: Build response
        result.put("merchant_location", merchantLat + "," + merchantLong);
        result.put("total_transactions", totalTxns);
        result.put("unique_cards", uniqueCardCount);
        result.put("fraud_transactions", fraudCount);
        result.put("fraud_rate_percent", fraudRate);
        result.put("card_diversity", cardDiversity);
        result.put("average_transaction_amount", avgAmount);
        
        // Assign risk level
        if (fraudRate > 5.0) {
            result.put("risk_level", "high");
        } else if (fraudRate > 1.0) {
            result.put("risk_level", "medium");
        } else {
            result.put("risk_level", "low");
        }
        
        return result;
    }

    /**
     * Test Case 3: Hourly Transaction Pattern
     * Uses Java processing instead of SQL GROUP BY
     */
    public static JSONArray getHourlyTransactionPattern(double merchantLat, double merchantLong) {
        JSONArray result = new JSONArray();
        
        // Step 1: Initialize 24-hour data structure
        JSONObject[] hourlyData = new JSONObject[24];
        for (int i = 0; i < 24; i++) {
            JSONObject hourData = new JSONObject();
            hourData.put("hour", i);
            hourData.put("transaction_count", 0);
            hourData.put("fraud_count", 0);
            hourData.put("average_amount", 0.0);
            hourData.put("fraud_rate", 0.0);
            hourlyData[i] = hourData;
        }
        
        // Step 2: Fetch transactions
        Transaction[] transactions = fetchNearbyTransactions(merchantLat, merchantLong);
        
        // Step 3: Process in Java
        double[] hourlyTotalAmount = new double[24];
        
        for (int i = 0; i < transactions.length; i++) {
            Transaction txn = transactions[i];
            
            // Extract hour
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(txn.getUnixTime() * 1000L);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            
            // Update counts
            JSONObject hourData = hourlyData[hour];
            int currentCount = hourData.getInt("transaction_count");
            hourData.put("transaction_count", currentCount + 1);
            
            if (txn.isFraud()) {
                int currentFraudCount = hourData.getInt("fraud_count");
                hourData.put("fraud_count", currentFraudCount + 1);
            }
            
            // Update amount
            hourlyTotalAmount[hour] += txn.getAmount();
        }
        
        // Step 4: Calculate rates and averages
        for (int hour = 0; hour < 24; hour++) {
            JSONObject hourData = hourlyData[hour];
            int txnCount = hourData.getInt("transaction_count");
            int fraudCount = hourData.getInt("fraud_count");
            
            if (txnCount > 0) {
                double avgAmount = hourlyTotalAmount[hour] / txnCount;
                double fraudRate = (fraudCount * 100.0) / txnCount;
                
                hourData.put("average_amount", avgAmount);
                hourData.put("fraud_rate", fraudRate);
                hourData.put("high_risk", fraudRate > 5.0);
            }
            
            result.put(hourData);
        }
        
        return result;
    }
}
How to Integrate with your FraudDetectionHandler
You can keep the same integration you already have:

// Test Case 1: Check for high-risk hour if not already flagged as fraud
if (responseJson.optInt("prediction", 0) == 0) {
    boolean isHighRiskHour = MerchantAnalytics.isHighRiskHour(
        transactionData.getMerchLat(), 
        transactionData.getMerchLon(), 
        transactionData.getUnixTime()
    );
    
    if (isHighRiskHour) {
        responseJson.put("warning", "Transaction occurring during historically high-risk hour");
    }
}
Merchant Analytics Implementation: Step-by-Step Guide
Let me walk you through our merchant analytics approach in a clear, step-by-step way that focuses on the data flow and logic.

Overall Architecture & Data Flow
TRANSACTION DATA → MERCHANT LOCATION GROUPING → ANALYTICS CALCULATION → RESULTS
Our Current Implementation Approach
We have two implementations to choose from:

SQL-focused approach: More database processing, less Java code
Java-focused approach: Simpler SQL, more Java processing logic
The current implementation in your codebase is using the SQL-focused approach which is more optimized for performance. Let me explain how each test case works:

Test Case 1: High-Risk Hour Analysis
Purpose: Check if a transaction is happening during a historically risky hour for this merchant.

Data Flow:
Input:

Merchant location (lat/long)
Transaction timestamp (unixTime)
Processing Steps:

Step 1: Convert timestamp to hour of day

Calendar cal = Calendar.getInstance();
cal.setTimeInMillis(unixTime * 1000L);
int currentHour = cal.get(Calendar.HOUR_OF_DAY);

Step 2: Run SQL query to calculate fraud rate for this hour at this location

SELECT 
  COUNT(*) as total_txns, 
  SUM(CASE WHEN fl.id IS NOT NULL THEN 1 ELSE 0 END) as fraud_count 
FROM transactions t 
LEFT JOIN fraud_logs fl ON... 
WHERE ABS(t.merch_lat - ?) < 0.01 
  AND ABS(t.merch_long - ?) < 0.01 
  AND HOUR(FROM_UNIXTIME(t.unix_time)) = ?

Step 3: Calculate fraud rate and check risk threshold
return (totalTxns >= 10 && ((double)fraudCount / totalTxns) > 0.05);

Output: Boolean flag indicating if current hour is high-risk

Test Case 2: Merchant Risk Score
Purpose: Calculate a comprehensive risk score for a merchant location.

Data Flow:
Input:

Merchant location (lat/long)
Processing Steps:

Step 1: Run SQL query to get aggregate statistics
SELECT 
  COUNT(*) as total_txns, 
  COUNT(DISTINCT cc_num) as unique_cards, 
  SUM(CASE WHEN fl.id IS NOT NULL THEN 1 ELSE 0 END) as fraud_count, 
  AVG(amt) as avg_amount 
FROM transactions t 
LEFT JOIN fraud_logs fl ON...
WHERE ABS(t.merch_lat - ?) < 0.01 
  AND ABS(t.merch_long - ?) < 0.01

Step 2: Calculate risk metrics in Java

double fraudRate = totalTxns > 0 ? (fraudCount * 100.0 / totalTxns) : 0;
double cardDiversity = totalTxns > 0 ? ((double)uniqueCards / totalTxns) : 0;

Step 3: Determine risk level based on fraud rate
if (fraudRate > 5.0) {
    result.put("risk_level", "high");
} else if (fraudRate > 1.0) {
    result.put("risk_level", "medium");
} else {
    result.put("risk_level", "low");
}

Output: JSON object with merchant risk metrics and risk level

Test Case 3: Hourly Transaction Pattern
Purpose: Analyze transaction patterns across all hours of the day.

Data Flow:
Input:

Merchant location (lat/long)
Processing Steps:

Step 1: Create empty 24-hour data structure
for (int hour = 0; hour < 24; hour++) {
    JSONObject hourData = new JSONObject();
    hourData.put("hour", hour);
    hourData.put("transaction_count", 0);
    // ... initialize other fields
    result.put(hourData);
}
Step 2: Run SQL query to get transaction stats grouped by hour
SELECT 
  HOUR(FROM_UNIXTIME(unix_time)) as hour_of_day, 
  COUNT(*) as txn_count, 
  SUM(CASE WHEN fl.id IS NOT NULL THEN 1 ELSE 0 END) as fraud_count, 
  AVG(amt) as avg_amount 
FROM transactions t 
LEFT JOIN fraud_logs fl ON...
WHERE ABS(t.merch_lat - ?) < 0.01 
  AND ABS(t.merch_long - ?) < 0.01 
GROUP BY hour_of_day 
ORDER BY hour_of_day

Step 3: Fill in result array with actual data

while (rs.next()) {
    int hour = rs.getInt("hour_of_day");
    int txnCount = rs.getInt("txn_count");
    int fraudCount = rs.getInt("fraud_count");
    double avgAmount = rs.getDouble("avg_amount");
    
    // Update the corresponding hour object
    JSONObject hourData = result.getJSONObject(hour);
    hourData.put("transaction_count", txnCount);
    // ... set other values
}
Step 4: Calculate fraud rates for each hour
double fraudRate = txnCount > 0 ? (fraudCount * 100.0 / txnCount) : 0;
hourData.put("fraud_rate", fraudRate);
hourData.put("high_risk", fraudRate > 5.0);

Output: JSON array with 24 hour entries containing transaction stats

Key Optimization Technique: Merchant Location Grouping
The most important optimization in our code is how we define "same merchant":
WHERE ABS(t.merch_lat - ?) < 0.01 AND ABS(t.merch_long - ?) < 0.01

This creates a roughly 1km x 1km "box" around the merchant coordinates. We consider all transactions within this box to be from the same merchant. This:

Handles slight GPS variations in the same physical location
Avoids needing a separate merchant table
Allows for spatial analysis without complex geospatial indexes
Alternate Java-Centric Approach
The alternative version moves most logic to Java by:

Using a simple SQL query to fetch all transaction data:
SELECT t.cc_num, t.amt, t.unix_time, (...) as is_fraud 
FROM transactions t 
WHERE ABS(t.merch_lat - ?) < 0.01 AND ABS(t.merch_long - ?) < 0.01

Processing the entire dataset in Java to:

Count transactions by hour
Calculate fraud rates
Determine risk scores
This approach is easier to understand and debug but less efficient with large datasets.

Integration with Fraud Detection
The implementation is integrated at the end of the fraud detection process:
// After ML model and other fraud checks
if (responseJson.optInt("prediction", 0) == 0) {
    // Only check for high-risk hour if transaction isn't already flagged
    boolean isHighRiskHour = MerchantAnalytics.isHighRiskHour(...);
    if (isHighRiskHour) {
        responseJson.put("warning", "Transaction during high-risk hour");
    }
}
