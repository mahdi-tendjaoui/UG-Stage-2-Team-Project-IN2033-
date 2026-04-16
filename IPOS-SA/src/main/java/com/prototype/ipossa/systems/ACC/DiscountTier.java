package com.prototype.ipossa.systems.ACC;

/**
 * Represents a single discount tier stored in the merchants_discounts table
 * A merchant's discount plan is made up of one or more tiers:
 *  - Fixed plan --> one tier with minOrderValue = 0 and maxOrderValue = null
 *  - Variable plan --> multiple tiers, each covering a range of order values
 */
public class DiscountTier {
    private int discountID;
    private int merchantID;
    private Double minOrderValue;
    private Double maxOrderValue;
    private double discountRate;

    //constructor used when reading from the database
    public DiscountTier(int discountID, int merchantID, Double minOrderValue, Double maxOrderValue, double discountRate) {
        this.discountID = discountID;
        this.merchantID = merchantID;
        this.minOrderValue = minOrderValue;
        this.maxOrderValue = maxOrderValue;
        this.discountRate = discountRate;
    }

    //constructor when creating a new tier before saving to the database
    public DiscountTier(int merchantID, Double minOrderValue, Double maxOrderValue, double discountRate) {
        this(0, merchantID, minOrderValue, maxOrderValue, discountRate);
    }
    //Returns true if the order value falls within this tier's range
    //maxOrderValue = null means the tier covers everything above minOrderValue
    public boolean appliesTo(double orderValue) {
        boolean aboveMin = (minOrderValue == null || orderValue >= minOrderValue);
        boolean belowMax = (maxOrderValue == null || orderValue <= maxOrderValue);
        return aboveMin && belowMax;
    }
    //Calculates the discount amount for a given order subtotal
    //Returns 0 if this tier does not apply to the order value
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