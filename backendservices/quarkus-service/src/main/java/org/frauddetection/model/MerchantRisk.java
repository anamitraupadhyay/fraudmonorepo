package org.frauddetection.model;

public class MerchantRisk {
    private String merchantLocation;
    private int totalTransactions;
    private int uniqueCards;
    private int fraudTransactions;
    private double fraudRatePercent;
    private double cardDiversity;
    private double averageTransactionAmount;
    private String riskLevel;

    private MerchantRisk() {}

    public static class Builder {
        private String merchantLocation;
        private int totalTransactions;
        private int uniqueCards;
        private int fraudTransactions;
        private double fraudRatePercent;
        private double cardDiversity;
        private double averageTransactionAmount;
        private String riskLevel;

        public Builder merchantLocation(String merchantLocation) {
            this.merchantLocation = merchantLocation;
            return this;
        }

        public Builder totalTransactions(int totalTransactions) {
            this.totalTransactions = totalTransactions;
            return this;
        }

        public Builder uniqueCards(int uniqueCards) {
            this.uniqueCards = uniqueCards;
            return this;
        }

        public Builder fraudTransactions(int fraudTransactions) {
            this.fraudTransactions = fraudTransactions;
            return this;
        }

        public Builder fraudRatePercent(double fraudRatePercent) {
            this.fraudRatePercent = fraudRatePercent;
            return this;
        }

        public Builder cardDiversity(double cardDiversity) {
            this.cardDiversity = cardDiversity;
            return this;
        }

        public Builder averageTransactionAmount(double averageTransactionAmount) {
            this.averageTransactionAmount = averageTransactionAmount;
            return this;
        }

        public Builder riskLevel(String riskLevel) {
            this.riskLevel = riskLevel;
            return this;
        }

        public MerchantRisk build() {
            MerchantRisk risk = new MerchantRisk();
            risk.merchantLocation = this.merchantLocation;
            risk.totalTransactions = this.totalTransactions;
            risk.uniqueCards = this.uniqueCards;
            risk.fraudTransactions = this.fraudTransactions;
            risk.fraudRatePercent = this.fraudRatePercent;
            risk.cardDiversity = this.cardDiversity;
            risk.averageTransactionAmount = this.averageTransactionAmount;
            risk.riskLevel = this.riskLevel;
            return risk;
        }
    }

    public String getMerchantLocation() {
        return merchantLocation;
    }

    public int getTotalTransactions() {
        return totalTransactions;
    }

    public int getUniqueCards() {
        return uniqueCards;
    }

    public int getFraudTransactions() {
        return fraudTransactions;
    }

    public double getFraudRatePercent() {
        return fraudRatePercent;
    }

    public double getCardDiversity() {
        return cardDiversity;
    }

    public double getAverageTransactionAmount() {
        return averageTransactionAmount;
    }

    public String getRiskLevel() {
        return riskLevel;
    }
}
