package org.frauddetection.model;

public class MerchDataNeeds {
    private String merchantLocation;
    private int totalTransactions;
    private int uniqueCards;
    private int fraudTransactions;
    private double fraudRatePercent;
    private double cardDiversity;
    private double averageTransactionAmount;
    private String riskLevel;

    
    public MerchDataNeeds() {}

    public MerchDataNeeds(String merchantLocation, int totalTransactions, int uniqueCards, int fraudTransactions,
                          double fraudRatePercent, double cardDiversity, double averageTransactionAmount, String riskLevel) {
        this.merchantLocation = merchantLocation;
        this.totalTransactions = totalTransactions;
        this.uniqueCards = uniqueCards;
        this.fraudTransactions = fraudTransactions;
        this.fraudRatePercent = fraudRatePercent;
        this.cardDiversity = cardDiversity;
        this.averageTransactionAmount = averageTransactionAmount;
        this.riskLevel = riskLevel;
    }


    public String getMerchantLocation() {
        return merchantLocation;
    }

    public void setMerchantLocation(String merchantLocation) {
        this.merchantLocation = merchantLocation;
    }

    public int getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(int totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public int getUniqueCards() {
        return uniqueCards;
    }

    public void setUniqueCards(int uniqueCards) {
        this.uniqueCards = uniqueCards;
    }

    public int getFraudTransactions() {
        return fraudTransactions;
    }

    public void setFraudTransactions(int fraudTransactions) {
        this.fraudTransactions = fraudTransactions;
    }

    public double getFraudRatePercent() {
        return fraudRatePercent;
    }

    public void setFraudRatePercent(double fraudRatePercent) {
        this.fraudRatePercent = fraudRatePercent;
    }

    public double getCardDiversity() {
        return cardDiversity;
    }

    public void setCardDiversity(double cardDiversity) {
        this.cardDiversity = cardDiversity;
    }

    public double getAverageTransactionAmount() {
        return averageTransactionAmount;
    }

    public void setAverageTransactionAmount(double averageTransactionAmount) {
        this.averageTransactionAmount = averageTransactionAmount;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }
}
