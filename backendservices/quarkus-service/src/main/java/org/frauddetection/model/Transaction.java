package org.frauddetection.model;


//3 classes format pojo for data logic travel and json data nesting for problem solving
//Transacion data is the final form of the json we will send to the frontend
public class FraudResponse {
    private int prediction;
    private String reason;
    private String warning;
    private Analytics analytics;

    // Private constructor
    private FraudResponse() {}

    // Builder Class
    public static class Builder {
        private int prediction;
        private String reason;
        private String warning;
        private Analytics analytics;

        public Builder prediction(int prediction) {
            this.prediction = prediction;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder warning(String warning) {
            this.warning = warning;
            return this;
        }

        public Builder analytics(Analytics analytics) {
            this.analytics = analytics;
            return this;
        }

        public FraudResponse build() {
            FraudResponse response = new FraudResponse();
            response.prediction = this.prediction;
            response.reason = this.reason;
            response.warning = this.warning;
            response.analytics = this.analytics;
            return response;
        }
    }

    // Getters
    public int getPrediction() {
        return prediction;
    }

    public String getReason() {
        return reason;
    }

    public String getWarning() {
        return warning;
    }

    public Analytics getAnalytics() {
        return analytics;
    }
}

public class Analytics {
    private boolean isHighRiskHour;
    private MerchantRisk merchantRisk;

    private Analytics() {}

    public static class Builder {
        private boolean isHighRiskHour;
        private MerchantRisk merchantRisk;

        public Builder highRiskHour(boolean isHighRiskHour) {
            this.isHighRiskHour = isHighRiskHour;
            return this;
        }

        public Builder merchantRisk(MerchantRisk merchantRisk) {
            this.merchantRisk = merchantRisk;
            return this;
        }

        public Analytics build() {
            Analytics analytics = new Analytics();
            analytics.isHighRiskHour = this.isHighRiskHour;
            analytics.merchantRisk = this.merchantRisk;
            return analytics;
        }
    }

    public boolean isHighRiskHour() {
        return isHighRiskHour;
    }

    public MerchantRisk getMerchantRisk() {
        return merchantRisk;
    }
}

public class MerchantRisk {
    private String merchantLocation;
    private int totalTransactions;
    private int uniqueCards;
    private int fraudTransactions;
    private double fraudRatePercent;
    private double cardDiversity;
    private double averageTransactionAmount;
    //private String riskLevel;

    private MerchantRisk() {}

    public static class Builder {
        private String merchantLocation;
        private int totalTransactions;
        private int uniqueCards;
        private int fraudTransactions;
        private double fraudRatePercent;
        private double cardDiversity;
        private double averageTransactionAmount;
        //private String riskLevel;

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

        /*public Builder riskLevel(String riskLevel) {
            this.riskLevel = riskLevel;
            return this;
        }*/

        public MerchantRisk build() {
            MerchantRisk risk = new MerchantRisk();
            risk.merchantLocation = this.merchantLocation;
            risk.totalTransactions = this.totalTransactions;
            risk.uniqueCards = this.uniqueCards;
            risk.fraudTransactions = this.fraudTransactions;
            risk.fraudRatePercent = this.fraudRatePercent;
            risk.cardDiversity = this.cardDiversity;
            risk.averageTransactionAmount = this.averageTransactionAmount;
            //risk.riskLevel = this.riskLevel;
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

    /*public String getRiskLevel() {
        return riskLevel;
    }*/
}


//premade class for above class processing as it needs to be extracted from Transaction data since we dont need all hte 9 fields
//on second thought i may not need this as it can be obtained from sql and...no no actually i need to store the data in class format because in here i can store all these datas and the MerchantRisk is for the json response so no data storing for the cases am gonna write
public class MerchDataNeeds {
    private String merchantLocation;
    private int totalTransactions;
    private int uniqueCards;
    private int fraudTransactions;
    private double fraudRatePercent;
    private double cardDiversity;
    private double averageTransactionAmount;
    //private String riskLevel;

    // Constructors
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
        //this.riskLevel = riskLevel;
    }

    // Getters and Setters
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

    /*public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }*/
}
