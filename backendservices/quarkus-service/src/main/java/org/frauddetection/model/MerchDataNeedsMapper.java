package org.frauddetection.model;

public class MerchDataNeedsMapper {
    public static MerchantRisk toMerchantRisk(MerchDataNeeds data) {
        return new MerchantRisk.Builder()
            .merchantLocation(data.getMerchantLocation())
            .totalTransactions(data.getTotalTransactions())
            .uniqueCards(data.getUniqueCards())
            .fraudTransactions(data.getFraudTransactions())
            .fraudRatePercent(data.getFraudRatePercent())
            .cardDiversity(data.getCardDiversity())
            .averageTransactionAmount(data.getAverageTransactionAmount())
            .riskLevel(data.getRiskLevel())
            .build();
    }
}