package org.frauddetection.model;

public class TransactionMapper {
    
    public static MerchantRisk toMerchantRisk(MerchDataNeeds data) {
        return MerchDataNeedsMapper.toMerchantRisk(data);
    }
    
    public static Analytics toAnalytics(boolean isHighRiskHour, MerchantRisk merchantRisk) {
        return AnalyticsMapper.toAnalytics(isHighRiskHour, merchantRisk);
    }
    
    public static FraudResponse toFraudResponse(int prediction, String reason, String warning, Analytics analytics) {
        return FraudResponseMapper.toFraudResponse(prediction, reason, warning, analytics);
    }
}