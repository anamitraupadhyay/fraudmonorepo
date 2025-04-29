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
        long ccNum = transactionData.getCcNum();
        double lat = transactionData.getLat();
        double lon = transactionData.getLon();
        long unixTime = transactionData.getUnixTime();

        JSONObject responseJson = new JSONObject();
        JSONObject requestData = convertToJson(transactionData);
        JSONObject lastTransaction = DataBaseTestCases.getLastTransaction(ccNum);

        if (lastTransaction == null) {
            responseJson = ExternalServiceUtil.callPythonModel(requestData);
            if (responseJson.getInt("prediction") == 1) {
                DataBaseTestCases.logFraud(
                    ccNum,
                    responseJson.optString("reason", "ML model detected potential fraud"),
                    requestData
                );
            }
        } else {
            long lastUnixTime = lastTransaction.getLong("unix_time");

            if (unixTime <= lastUnixTime) {
                responseJson.put("prediction", 0);
                responseJson.put("reason", "Transaction timestamp valid");
            } else {
                double lastLat = lastTransaction.getDouble("lat");
                double lastLon = lastTransaction.getDouble("long");
                double distanceKm = FraudDetectionUtils.haversine(lastLat, lastLon, lat, lon);

                if (distanceKm > 500.0) {
                    double timeDiffHours = (unixTime - lastUnixTime) / 3600.0;
                    double speedKmh = distanceKm / timeDiffHours;

                    if (speedKmh > MAX_SPEED_KMH) {
                        String reason = String.format("Impossible travel speed: %.2f km/h", speedKmh);
                        responseJson.put("prediction", 1);
                        responseJson.put("reason", reason);
                        DataBaseTestCases.logFraud(ccNum, reason, requestData);
                    } else {
                        responseJson.put("prediction", 0);
                        responseJson.put("reason", "Transaction speed normal");
                    }
                } else {
                    responseJson = ExternalServiceUtil.callPythonModel(requestData);
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

        DataBaseTestCases.storeTransaction(transactionData);
        return responseJson;
    }
}
