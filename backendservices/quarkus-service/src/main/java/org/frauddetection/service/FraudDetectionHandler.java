package org.frauddetection.service;

import org.json.JSONObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;

import org.frauddetection.db.DataBaseTestCases;
import org.frauddetection.model.TransactionData;
import org.frauddetection.util.FraudDetectionUtils;
import org.frauddetection.util.ExternalServiceUtil;


@ApplicationScoped
public class FraudDetectionHandler {
    // Single speed threshold (300 km/h - high-speed train)
    private static final double MAX_SPEED_KMH = 300.0;

    @Inject
    FraudDetectionHandler handler;

    /**
     * Process a transaction and check for fraud
     * @param requestData the transaction data
     * @return JSONObject with fraud prediction and reason
     */
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
        
        // Prepare response
        JSONObject responseJson = new JSONObject();

        // Convert transaction data to JSON for logging
        JSONObject requestData = convertToJson(transactionData);
        
        // Get last transaction (if any)
        JSONObject lastTransaction = DataBaseTestCases.getLastTransaction(ccNum);
        
        // Simple decision flow: new user -> ML model, existing user → speed check, for later existing users-> ML model if under speed threshold...ohkay done this too
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
        } else {
            // Existing user - check speed between transactions
            long lastUnixTime = lastTransaction.getLong("unix_time");
            
            // Skip impossible time differences
            if (unixTime <= lastUnixTime) {
                responseJson.put("prediction", 0);
                responseJson.put("reason", "Transaction timestamp valid");
            } else {
                // Calculate travel metrics
                double lastLat = lastTransaction.getDouble("lat");
                double lastLon = lastTransaction.getDouble("long");
                double distanceKm = FraudDetectionUtils.haversine(lastLat, lastLon, lat, lon);
                
                // Only check speed if distance is significant (>1km)
                if (distanceKm > 1.0) {
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
        
        // Always store the transaction
        DataBaseTestCases.storeTransaction(transactionData);
        return responseJson;
    }
}