package org.frauddetection.service;

import org.json.JSONObject;

import jakarta.enterprise.context.ApplicationScoped;

import org.frauddetection.db.DataBaseTestCases;
import org.frauddetection.model.TransactionData;
import org.frauddetection.util.FraudDetectionUtils;
import org.frauddetection.util.FraudDetectionUtils.FraudResult;
import org.frauddetection.util.ExternalServiceUtil;

import java.io.IOException;

@ApplicationScoped
public class FraudDetectionHandler {

    // Distance threshold (in km) to decide whether to bypass the ML model
    private static final double MAX_DISTANCE_THRESHOLD = 1000;

    // Use the centralized threshold from FraudDetectionUtils
    private static final double MAX_SPEED_KMH = 300;

    /**
     * Process a transaction represented as JSON.
     *
     * @param requestData the incoming transaction JSON data.
     * @return the JSON response with fraud prediction and reason.
     * @throws IOException if a network or IO error occurs.
     */
    public JSONObject processTransaction(JSONObject requestData) throws IOException {
        // Extract transaction fields
        long ccNum = requestData.getLong("cc_num");
        double amt = requestData.getDouble("amt");
        String zip = requestData.getString("zip");
        double lat = requestData.getDouble("lat");
        double lon = requestData.getDouble("long");
        int cityPop = requestData.getInt("city_pop");
        long unixTime = requestData.getLong("unix_time");
        double merchLat = requestData.getDouble("merch_lat");
        double merchLong = requestData.getDouble("merch_long");

        // Create a transaction data object
        TransactionData transactionData = new TransactionData(
                ccNum, amt, zip, lat, lon, cityPop, unixTime, merchLat, merchLong);

        // Check if user exists (i.e. has previous transactions)
        JSONObject lastTransaction = DataBaseTestCases.getLastTransaction(ccNum);
        boolean isNewUser = (lastTransaction == null);

        // Prepare the response JSON object
        JSONObject responseJson = new JSONObject();

        // Calculate the customer-to-merchant distance using Haversine formula
        double customerToMerchantDistance = FraudDetectionUtils.haversine(lat, lon, merchLat, merchLong);

        if (customerToMerchantDistance > MAX_DISTANCE_THRESHOLD) {
            // Condition 1: Distance issue detected → Bypass ML model
            String reason = "Customer-merchant distance exceeds threshold: " +
                    String.format("%.2f km", customerToMerchantDistance);
            responseJson.put("prediction", 1);
            responseJson.put("reason", reason);
            // Log fraud in the database
            DataBaseTestCases.logFraud(ccNum, reason, requestData);
        } else if (!isNewUser) {
            // Calculate speed based on previous transaction
            double lastLat = lastTransaction.getDouble("lat");
            double lastLon = lastTransaction.getDouble("long");
            long lastUnixTime = lastTransaction.getLong("unix_time");

            // Calculate distance between current and previous transaction locations
            double distanceKm = FraudDetectionUtils.haversine(lastLat, lastLon, lat, lon);

            // Calculate time difference in hours
            double timeDiffHours = (unixTime - lastUnixTime) / 3600.0;

            // Only check speed if time difference is positive (to avoid division by zero or
            // negative time)
            if (timeDiffHours > 0) {
                double speedKmh = distanceKm / timeDiffHours;

                if (speedKmh > MAX_SPEED_KMH) {
                    // Condition 1: Speed exceeds maximum possible travel speed
                    String reason = String.format("Impossible travel speed: %.2f km/h (%.2f km in %.2f hours)",
                            speedKmh, distanceKm, timeDiffHours);
                    responseJson.put("prediction", 1);
                    responseJson.put("reason", reason);
                    // Log fraud in the database
                    DataBaseTestCases.logFraud(ccNum, reason, requestData);

                    // Store transaction and return result
                    DataBaseTestCases.storeTransaction(transactionData);
                    return responseJson;
                }
            }

            // Existing user: perform additional fraud checks (e.g., impossible travel)
            FraudResult fraudResult = FraudDetectionUtils.isFraudulent(ccNum, lat, lon, unixTime);

            if (fraudResult.isFraud()) {
                responseJson.put("prediction", 1); // Fraud detected
                responseJson.put("reason", fraudResult.getReason());
                DataBaseTestCases.logFraud(ccNum, fraudResult.getReason(), requestData);
            } else {
                responseJson.put("prediction", 0); // Not fraud
                responseJson.put("reason", fraudResult.getReason());
            }
        } else {
            // New user: call Python ML model and log if fraud is predicted
            responseJson = ExternalServiceUtil.callPythonModel(requestData);

            // Log if ML model predicts fraud
            if (responseJson.getInt("prediction") == 1) {
                DataBaseTestCases.logFraud(ccNum,
                        responseJson.optString("reason", "ML model detected potential fraud"),
                        requestData);
            }
        }

        // Store the transaction in the database regardless of fraud status
        DataBaseTestCases.storeTransaction(transactionData);

        return responseJson;
    }
}
