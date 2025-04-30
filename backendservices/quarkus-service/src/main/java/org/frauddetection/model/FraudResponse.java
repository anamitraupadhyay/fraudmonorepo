package org.frauddetection.model;

public class FraudResponse {
    private int prediction;
    private String reason;
    private String warning;
    private Analytics analytics;

    
    private FraudResponse() {}

    
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