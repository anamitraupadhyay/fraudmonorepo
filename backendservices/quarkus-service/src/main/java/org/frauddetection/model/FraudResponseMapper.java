package org.frauddetection.model;

public class FraudResponseMapper {
    public static FraudResponse toFraudResponse(int prediction, String reason, String warning, Analytics analytics) {
        return new FraudResponse.Builder()
            .prediction(prediction)
            .reason(reason)
            .warning(warning)
            .analytics(analytics)
            .build();
    }
}
