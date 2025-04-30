package org.frauddetection.model;

public class AnalyticsMapper {
    public static Analytics toAnalytics(boolean isHighRiskHour, MerchantRisk merchantRisk) {
        return new Analytics.Builder()
            .highRiskHour(isHighRiskHour)
            .merchantRisk(merchantRisk)
            .build();
    }
}