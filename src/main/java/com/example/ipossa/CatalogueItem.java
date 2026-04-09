package com.example.ipossa;

/**
 * Represents one product from the catalogue table.
 */
public class CatalogueItem {

    private String itemId;
    private String description;
    private String packageType;
    private String unit;
    private int unitsInPack;
    private double packageCost;
    private int availability;
    private int stockLimit;

    public CatalogueItem() {
    }

    public CatalogueItem(String itemId, String description, String packageType, String unit,
                         int unitsInPack, double packageCost, int availability, int stockLimit) {
        this.itemId = itemId;
        this.description = description;
        this.packageType = packageType;
        this.unit = unit;
        this.unitsInPack = unitsInPack;
        this.packageCost = packageCost;
        this.availability = availability;
        this.stockLimit = stockLimit;
    }

    public String getItemId() {
        return itemId;
    }

    public String getDescription() {
        return description;
    }

    public String getPackageType() {
        return packageType;
    }

    public String getUnit() {
        return unit;
    }

    public int getUnitsInPack() {
        return unitsInPack;
    }

    public double getPackageCost() {
        return packageCost;
    }

    public int getAvailability() {
        return availability;
    }

    public int getStockLimit() {
        return stockLimit;
    }

    public void setAvailability(int availability) {
        this.availability = availability;
    }

    public void setStockLimit(int stockLimit) {
        this.stockLimit = stockLimit;
    }

    public void setPackageCost(double packageCost) {
        this.packageCost = packageCost;
    }
}