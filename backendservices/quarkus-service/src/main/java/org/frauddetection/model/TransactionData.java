package org.frauddetection.model;


public class TransactionData {
    private long ccNum;
    private double amt;
    private String zip;
    private double lat;
    private double lon; // renamed from "long"
    private int cityPop;
    private long unixTime;
    private double merchLat;
    private double merchLon;

    public TransactionData(long ccNum, double amt, String zip, double lat, double lon,
            int cityPop, long unixTime, double merchLat, double merchLon) {
        this.ccNum = ccNum;
        this.amt = amt;
        this.zip = zip;
        this.lat = lat;
        this.lon = lon;
        this.cityPop = cityPop;
        this.unixTime = unixTime;
        this.merchLat = merchLat;
        this.merchLon = merchLon;
    }

    
    public long getCcNum() {
        return ccNum;
    }

    public double getAmt() {
        return amt;
    }

    public String getZip() {
        return zip;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public int getCityPop() {
        return cityPop;
    }

    public long getUnixTime() {
        return unixTime;
    }

    public double getMerchLat() {
        return merchLat;
    }

    public double getMerchLon() {
        return merchLon;
    }

}
