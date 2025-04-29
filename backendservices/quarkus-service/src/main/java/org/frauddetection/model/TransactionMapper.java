package org.frauddetection.model;

class MerchDataNeedsMapper {
    public static MerchantRisk toMerchantRisk(MerchDataNeeds data) {
        return new MerchantRisk.Builder()
            .merchantLocation(data.getMerchantLocation())
            .totalTransactions(data.getTotalTransactions())
            .uniqueCards(data.getUniqueCards())
            .fraudTransactions(data.getFraudTransactions())
            .fraudRatePercent(data.getFraudRatePercent())
            .cardDiversity(data.getCardDiversity())
            .averageTransactionAmount(data.getAverageTransactionAmount())
            //.riskLevel(data.getRiskLevel())
            .build();
    }
}

class AnalyticsMapper {
    public static Analytics toAnalytics(boolean isHighRiskHour, MerchantRisk merchantRisk) {
        return new Analytics.Builder()
            .highRiskHour(isHighRiskHour)
            .merchantRisk(merchantRisk)
            .build();
    }
}

class FraudResponseMapper {
    public static FraudResponse toFraudResponse(int prediction, String reason, String warning, Analytics analytics) {
        return new FraudResponse.Builder()
            .prediction(prediction)
            .reason(reason)
            .warning(warning)
            .analytics(analytics)
            .build();
    }
}
