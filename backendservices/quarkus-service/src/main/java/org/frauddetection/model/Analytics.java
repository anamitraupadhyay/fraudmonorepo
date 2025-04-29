package org.frauddetection.model;

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