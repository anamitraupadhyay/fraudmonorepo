package org.frauddetection.db;

import java.sql.*;
import org.frauddetection.model.TransactionData;
import org.frauddetection.model.MerchDataNeeds;
import org.json.JSONObject;

public class DataBaseTestCases {
    
    public static final String DB_URL = "jdbc:mariadb://mariadb:3306/fraud_detection?" +
                                      "allowPublicKeyRetrieval=true&useSSL=false&" +
                                      "connectTimeout=30000&socketTimeout=30000";
    public static final String DB_USER = "fraud_user";
    public static final String DB_PASSWORD = "fraud_pass";

    
    static {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MariaDB JDBC Driver not found: " + e.getMessage());
        }
    }

private static final double MERCHANT_RADIUS = 0.01; // ~1km radius
private static final double HIGH_RISK_THRESHOLD = 5.0;
private static final double MEDIUM_RISK_THRESHOLD = 1.0;

    
        public static JSONObject getHighRiskHour(long unixTime) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            String query = "SELECT " +
                "HOUR(FROM_UNIXTIME(unix_time)) AS txn_hour, " +
                "COUNT(*) AS total_count, " +
                "SUM(CASE WHEN fl.id IS NOT NULL THEN 1 ELSE 0 END) AS fraud_count " +
                "FROM transactions t " +
                "LEFT JOIN fraud_logs fl ON t.cc_num = fl.cc_num AND " +
                "t.unix_time = JSON_EXTRACT(fl.transaction_data, '$.unix_time') " +
                "WHERE HOUR(FROM_UNIXTIME(unix_time)) = HOUR(FROM_UNIXTIME(?)) " +
                "GROUP BY HOUR(FROM_UNIXTIME(unix_time))";

            pstmt = conn.prepareStatement(query);
            pstmt.setLong(1, unixTime);
            
            rs = pstmt.executeQuery();
            if (rs.next()) {
                JSONObject obj = new JSONObject();
                obj.put("txn_hour", rs.getInt("txn_hour"));
                obj.put("total_count", rs.getInt("total_count"));
                obj.put("fraud_count", rs.getInt("fraud_count"));
                return obj;
            }
            return new JSONObject().put("error", "No data found");
        } catch (SQLException e) {
            return new JSONObject().put("error", "Database error: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }
    }

    public static MerchDataNeeds merchDataRequirements(double merchLat, double merchLong) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            String query = "SELECT " +
                    "COUNT(*) AS total_transactions, " +
                    "COUNT(DISTINCT t.cc_num) AS unique_cards, " +
                    "SUM(CASE WHEN fl.id IS NOT NULL THEN 1 ELSE 0 END) AS fraud_transactions, " +
                    "AVG(amt) AS avg_transac_amt " +
                    "FROM transactions t " +
                    "LEFT JOIN fraud_logs fl ON t.cc_num = fl.cc_num AND " +
                    "t.unix_time = JSON_EXTRACT(fl.transaction_data, '$.unix_time') " +
                    "WHERE ABS(t.merch_lat - ?) < " + MERCHANT_RADIUS + " AND ABS(t.merch_long - ?) < " + MERCHANT_RADIUS;
            
            pstmt = conn.prepareStatement(query);
            pstmt.setDouble(1, merchLat);
            pstmt.setDouble(2, merchLong);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                MerchDataNeeds dataNeeds = new MerchDataNeeds();
                
                int totalTxns = rs.getInt("total_transactions");
                int uniqueCards = rs.getInt("unique_cards");
                int fraudCount = rs.getInt("fraud_transactions");
                double avgAmount = rs.getDouble("avg_transac_amt");
                
                
                double fraudRate = totalTxns > 0 ? (fraudCount * 100.0 / totalTxns) : 0;
                double cardDiversity = totalTxns > 0 ? ((double)uniqueCards / totalTxns) : 0;
                
                
                String riskLevel = "low";
                if (fraudRate > HIGH_RISK_THRESHOLD) {
                    riskLevel = "high";
                } else if (fraudRate > MEDIUM_RISK_THRESHOLD) {
                    riskLevel = "medium";
                }
                
                
                dataNeeds.setMerchantLocation(String.format("%.6f, %.6f", merchLat, merchLong));
                dataNeeds.setTotalTransactions(totalTxns);
                dataNeeds.setUniqueCards(uniqueCards);
                dataNeeds.setFraudTransactions(fraudCount);
                dataNeeds.setFraudRatePercent(fraudRate);
                dataNeeds.setCardDiversity(cardDiversity);
                dataNeeds.setAverageTransactionAmount(avgAmount);
                dataNeeds.setRiskLevel(riskLevel);
                
                return dataNeeds;
            }
            return null;
        } catch (SQLException e) {
            System.err.println("Database error in merchDataRequirements: " + e.getMessage());
            return null;
        } finally {
            closeResources(rs, pstmt, conn);
        }
    }
    
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
    
    
    private static void closeResources(ResultSet rs, Statement stmt, Connection conn) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) {}
        try {
            if (stmt != null) stmt.close();
        } catch (SQLException e) {}
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {}
    }
    
    public static JSONObject getLastTransaction(long ccNum) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            String query = "SELECT unix_time, lat, `long` FROM transactions WHERE cc_num = ? "
                    + "ORDER BY unix_time DESC LIMIT 1";
            pstmt = conn.prepareStatement(query);
            pstmt.setLong(1, ccNum);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                JSONObject obj = new JSONObject();
                obj.put("unix_time", rs.getLong("unix_time"));
                obj.put("lat", rs.getDouble("lat"));
                obj.put("long", rs.getDouble("long"));
                return obj;
            }
        } catch (SQLException e) {
            System.out.println("Database error in getLastTransaction: " + e.getMessage());
        } finally {
            try {
                if (rs != null)
                    rs.close();
            } catch (SQLException ex) {
                System.out.println("Error closing ResultSet: " + ex.getMessage());
            }
            try {
                if (pstmt != null)
                    pstmt.close();
            } catch (SQLException ex) {
                System.out.println("Error closing PreparedStatement: " + ex.getMessage());
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                System.out.println("Error closing Connection: " + ex.getMessage());
            }
        }
        return null;
    }

    
    public static boolean storeTransaction(TransactionData data) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            String query = "INSERT INTO transactions (cc_num, amt, zip, lat, `long`, city_pop, unix_time, merch_lat, merch_long) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(query);
            pstmt.setLong(1, data.getCcNum());
            pstmt.setDouble(2, data.getAmt());
            pstmt.setString(3, data.getZip());
            pstmt.setDouble(4, data.getLat());
            pstmt.setDouble(5, data.getLon());
            pstmt.setInt(6, data.getCityPop());
            pstmt.setLong(7, data.getUnixTime());
            pstmt.setDouble(8, data.getMerchLat());
            pstmt.setDouble(9, data.getMerchLon());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Database error in storeTransaction: " + e.getMessage());
            return false;
        } finally {
            try {
                if (pstmt != null)
                    pstmt.close();
            } catch (SQLException ex) {
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
            }
        }
    }

    
    public static boolean logFraud(long ccNum, String reason, JSONObject transactionData) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            String query = "INSERT INTO fraud_logs (cc_num, reason, transaction_data) VALUES (?, ?, ?)";
            pstmt = conn.prepareStatement(query);
            pstmt.setLong(1, ccNum);
            pstmt.setString(2, reason);
            pstmt.setString(3, transactionData.toString());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Database error in logFraud: " + e.getMessage());
            return false;
        } finally {
            try {
                if (pstmt != null)
                    pstmt.close();
            } catch (SQLException ex) {
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
            }
        }
    }
}