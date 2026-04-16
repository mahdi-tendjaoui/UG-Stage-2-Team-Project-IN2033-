package com.prototype.ipossa.systems.ACC;

public class DiscountTier {
    private int discountID;
    private int merchantID;
    private Double minOrderValue;
    private Double maxOrderValue;
    private double discountRate;

    public DiscountTier(int discountID, int merchantID, Double minOrderValue, Double maxOrderValue, double discountRate) {
        this.discountID = discountID;
        this.merchantID = merchantID;
        this.minOrderValue = minOrderValue;
        this.maxOrderValue = maxOrderValue;
        this.discountRate = discountRate;
    }

    public DiscountTier(int merchantID, Double minOrderValue, Double maxOrderValue, double discountRate) {
        this(0, merchantID, minOrderValue, maxOrderValue, discountRate);
    }

    public boolean appliesTo(double orderValue) {
        boolean aboveMin = (minOrderValue == null || orderValue >= minOrderValue);
        boolean belowMax = (maxOrderValue == null || orderValue <= maxOrderValue);
        return aboveMin && belowMax;
    }

    public double calculateDiscount(double orderSubtotal) {
        if (!appliesTo(orderSubtotal)) return 0.0;
        return orderSubtotal * (discountRate / 100.0);
    }

    public int getDiscountID() { return discountID; }
    public int getMerchantID() { return merchantID; }
    public Double getMinOrderValue() { return minOrderValue; }
    public Double getMaxOrderValue() { return maxOrderValue; }
    public double getDiscountRate() { return discountRate; }

    public void setDiscountID(int discountID) { this.discountID = discountID; }
    public void setMinOrderValue(Double min) { this.minOrderValue = min; }
    public void setMaxOrderValue(Double max) { this.maxOrderValue = max; }
    public void setDiscountRate(double rate) { this.discountRate = rate; }

    @Override
    public String toString() {
        String min = (minOrderValue == null) ? "0"      : String.format("£%.2f", minOrderValue);
        String max = (maxOrderValue == null) ? "∞"      : String.format("£%.2f", maxOrderValue);
        return String.format("DiscountTier[%s – %s @ %.2f%%]", min, max, discountRate);
    }
}
