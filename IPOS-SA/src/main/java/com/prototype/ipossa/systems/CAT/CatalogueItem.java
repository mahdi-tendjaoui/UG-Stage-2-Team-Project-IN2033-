package com.prototype.ipossa.systems.CAT;

public class CatalogueItem {

    private String categoryId;
    private String medicineId;
    private String description;
    private String packageType;
    private String unit;
    private int unitsInPack;
    private double packageCost;
    private int availability;
    private int stockLimit;

    public CatalogueItem() {
    }

    public CatalogueItem(String categoryId, String medicineId, String description,
                         String packageType, String unit, int unitsInPack,
                         double packageCost, int availability, int stockLimit) {
        this.categoryId = categoryId;
        this.medicineId = medicineId;
        this.description = description;
        this.packageType = packageType;
        this.unit = unit;
        this.unitsInPack = unitsInPack;
        this.packageCost = packageCost;
        this.availability = availability;
        this.stockLimit = stockLimit;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getMedicineId() {
        return medicineId;
    }

    public String getFullItemId() {
        return categoryId + " " + medicineId;
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
