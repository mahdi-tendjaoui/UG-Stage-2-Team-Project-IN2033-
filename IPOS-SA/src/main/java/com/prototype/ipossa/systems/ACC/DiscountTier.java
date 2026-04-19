package com.prototype.ipossa.systems.ACC;

/**
 * The type Discount tier.
 */
public class DiscountTier {
    private int discountID;
    private int merchantID;
    private Double minOrderValue;
    private Double maxOrderValue;
    private double discountRate;

    /**
     * Instantiates a new Discount tier.
     *
     * @param discountID    the discount id
     * @param merchantID    the merchant id
     * @param minOrderValue the min order value
     * @param maxOrderValue the max order value
     * @param discountRate  the discount rate
     */
    public DiscountTier(int discountID, int merchantID, Double minOrderValue, Double maxOrderValue, double discountRate) {
        this.discountID = discountID;
        this.merchantID = merchantID;
        this.minOrderValue = minOrderValue;
        this.maxOrderValue = maxOrderValue;
        this.discountRate = discountRate;
    }

    /**
     * Instantiates a new Discount tier.
     *
     * @param merchantID    the merchant id
     * @param minOrderValue the min order value
     * @param maxOrderValue the max order value
     * @param discountRate  the discount rate
     */
    public DiscountTier(int merchantID, Double minOrderValue, Double maxOrderValue, double discountRate) {
        this(0, merchantID, minOrderValue, maxOrderValue, discountRate);
    }

    /**
     * Applies to boolean.
     *
     * @param orderValue the order value
     * @return the boolean
     */
    public boolean appliesTo(double orderValue) {
        boolean aboveMin = (minOrderValue == null || orderValue >= minOrderValue);
        boolean belowMax = (maxOrderValue == null || orderValue <= maxOrderValue);
        return aboveMin && belowMax;
    }

    /**
     * Calculate discount double.
     *
     * @param orderSubtotal the order subtotal
     * @return the double
     */
    public double calculateDiscount(double orderSubtotal) {
        if (!appliesTo(orderSubtotal)) return 0.0;
        return orderSubtotal * (discountRate / 100.0);
    }

    /**
     * Gets discount id.
     *
     * @return the discount id
     */
    public int getDiscountID() { return discountID; }

    /**
     * Gets merchant id.
     *
     * @return the merchant id
     */
    public int getMerchantID() { return merchantID; }

    /**
     * Gets min order value.
     *
     * @return the min order value
     */
    public Double getMinOrderValue() { return minOrderValue; }

    /**
     * Gets max order value.
     *
     * @return the max order value
     */
    public Double getMaxOrderValue() { return maxOrderValue; }

    /**
     * Gets discount rate.
     *
     * @return the discount rate
     */
    public double getDiscountRate() { return discountRate; }

    /**
     * Sets discount id.
     *
     * @param discountID the discount id
     */
    public void setDiscountID(int discountID) { this.discountID = discountID; }

    /**
     * Sets min order value.
     *
     * @param min the min
     */
    public void setMinOrderValue(Double min) { this.minOrderValue = min; }

    /**
     * Sets max order value.
     *
     * @param max the max
     */
    public void setMaxOrderValue(Double max) { this.maxOrderValue = max; }

    /**
     * Sets discount rate.
     *
     * @param rate the rate
     */
    public void setDiscountRate(double rate) { this.discountRate = rate; }

    @Override
    public String toString() {
        String min = (minOrderValue == null) ? "0"      : String.format("£%.2f", minOrderValue);
        String max = (maxOrderValue == null) ? "∞"      : String.format("£%.2f", maxOrderValue);
        return String.format("DiscountTier[%s – %s @ %.2f%%]", min, max, discountRate);
    }
}
