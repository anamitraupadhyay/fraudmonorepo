package org.frauddetection.util;

import org.frauddetection.db.DataBaseTestCases;
import org.json.JSONObject;

public class FraudDetectionUtils {

    // Haversine formula: returns distance (in km) between two coordinates.
    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth’s radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // Check if the transaction is fraudulent based on simple rules.
    // Returns a FraudResult object containing a flag and reason.
    public static FraudResult isFraudulent(long ccNum, double newLat, double newLon, long newUnixTime) {
        JSONObject lastTransaction = DataBaseTestCases.getLastTransaction(ccNum);
        if (lastTransaction != null) {
            long lastUnixTime = lastTransaction.getLong("unix_time");
            double lastLat = lastTransaction.getDouble("lat");
            double lastLon = lastTransaction.getDouble("long");

            // Calculate time difference (in minutes)
            double timeDiff = (newUnixTime - lastUnixTime) / 60.0;
            double distanceKm = haversine(lastLat, lastLon, newLat, newLon);

            // Rule 1: If transaction happens too soon after the last one
            if (timeDiff > 0 && (distanceKm / timeDiff) > 0.167) {
                return new FraudResult(true, "Transaction happened too soon after the last one.");
            }
            // Rule 2: Impossible travel detected (e.g., >5 km but too little time)
            if (distanceKm > 5 && timeDiff < (distanceKm / 5.0) * 3) {
                return new FraudResult(true,
                        String.format("Impossible travel detected: %.2f km in %.2f min.", distanceKm, timeDiff));
            }
        }
        return new FraudResult(false, "Transaction looks safe.");
    }

    // Simple helper class for fraud check results.
    public static class FraudResult {
        private boolean fraud;
        private String reason;

        public FraudResult(boolean fraud, String reason) {
            this.fraud = fraud;
            this.reason = reason;
        }

        public boolean isFraud() {
            return fraud;
        }

        public String getReason() {
            return reason;
        }
    }
}
