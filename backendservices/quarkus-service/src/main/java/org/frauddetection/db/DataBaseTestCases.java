package org.frauddetection.db;

import java.sql.*;
import org.frauddetection.model.TransactionData;
import org.json.JSONObject;

public class DataBaseTestCases {
    // DB configuration
    public static final String DB_URL = "jdbc:mariadb://localhost:3307/fraud_detection";
    public static final String DB_USER = "fraud_user";
    public static final String DB_PASSWORD = "fraud_pass";

    // Load MariaDB JDBC Driver
    static {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MariaDB JDBC Driver not found: " + e.getMessage());
        }
    }

    // Initialize the database (create database and tables if not exist)
    public static boolean initDatabase() {
        Connection conn = null;
        Statement stmt = null;
        try {
            // Connect to MySQL without specifying a database
            conn = DriverManager.getConnection(DB_URL+"?useSSL=false", DB_USER, DB_PASSWORD);
            stmt = conn.createStatement();
            // Create database if it doesn’t exist
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS fraud_detection");
            // Switch to the database
            stmt.executeUpdate("USE fraud_detection");

            // Create transactions table
            String createTransactions = "CREATE TABLE IF NOT EXISTS transactions ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "cc_num BIGINT NOT NULL,"
                    + "amt DECIMAL(10,2) NOT NULL,"
                    + "zip VARCHAR(10),"
                    + "lat FLOAT NOT NULL,"
                    + "`long` FLOAT NOT NULL,"
                    + "city_pop INT NOT NULL,"
                    + "unix_time BIGINT NOT NULL,"
                    + "merch_lat FLOAT NOT NULL,"
                    + "merch_long FLOAT NOT NULL,"
                    + "INDEX idx_cc_num (cc_num),"
                    + "INDEX idx_unix_time (unix_time)"
                    + ")";
            stmt.executeUpdate(createTransactions);

            // Create fraud_logs table
            String createFraudLogs = "CREATE TABLE IF NOT EXISTS fraud_logs ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "cc_num BIGINT NOT NULL,"
                    + "reason TEXT NOT NULL,"
                    + "detected_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                    + "transaction_data JSON,"
                    + "INDEX idx_cc_num (cc_num)"
                    + ")";
            stmt.executeUpdate(createFraudLogs);
            System.out.println("Database initialized successfully");
            return true;
        } catch (SQLException e) {
            System.out.println("Database initialization error: " + e.getMessage());
            return false;
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException ex) {
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
            }
        }
    }

    // Get the last transaction for a given credit card number
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
            }
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
        return null;
    }

    // Store the transaction into the database
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

    // Log fraudulent transactions into the fraud_logs table
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
