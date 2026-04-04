package com.example.ipossa;

public class CatalogueItem {

    private String catalogueId;
    private String description;
    private String packageType;
    private String unit;
    private int unitsInPack;
    private double packageCost;
    private int availability;
    private int stockLimit;


    public CatalogueItem() {
    }


    public CatalogueItem(String catalogueId, String description, String packageType, String unit,
                         int unitsInPack, double packageCost, int availability, int stockLimit) {
        this.catalogueId = catalogueId;
        this.description = description;
        this.packageType = packageType;
        this.unit = unit;
        this.unitsInPack = unitsInPack;
        this.packageCost = packageCost;
        this.availability = availability;
        this.stockLimit = stockLimit;
    }

    public String getCatalogueId() {
        return catalogueId;
    }

    public void setCatalogueId(String catalogueId) {
        this.catalogueId = catalogueId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPackageType() {
        return packageType;
    }

    public void setPackageType(String packageType) {
        this.packageType = packageType;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getUnitsInPack() {
        return unitsInPack;
    }

    public void setUnitsInPack(int unitsInPack) {
        this.unitsInPack = unitsInPack;
    }

    public double getPackageCost() {
        return packageCost;
    }

    public void setPackageCost(double packageCost) {
        this.packageCost = packageCost;
    }

    public int getAvailability() {
        return availability;
    }

    public void setAvailability(int availability) {
        this.availability = availability;
    }

    public int getStockLimit() {
        return stockLimit;
    }

    public void setStockLimit(int stockLimit) {
        this.stockLimit = stockLimit;
    }
}