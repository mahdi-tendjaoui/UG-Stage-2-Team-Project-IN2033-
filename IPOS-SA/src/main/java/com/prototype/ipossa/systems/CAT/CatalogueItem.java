package com.prototype.ipossa.systems.CAT;


/**
 * The type Catalogue item.
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

    /**
     * Instantiates a new Catalogue item.
     */
    public CatalogueItem() {
    }

    /**
     * Instantiates a new Catalogue item.
     *
     * @param itemId       the item id
     * @param description  the description
     * @param packageType  the package type
     * @param unit         the unit
     * @param unitsInPack  the units in pack
     * @param packageCost  the package cost
     * @param availability the availability
     * @param stockLimit   the stock limit
     */
    public CatalogueItem(String itemId, String description,
                         String packageType, String unit,
                         int unitsInPack, double packageCost,
                         int availability, int stockLimit) {
        this.itemId = itemId;
        this.description = description;
        this.packageType = packageType;
        this.unit = unit;
        this.unitsInPack = unitsInPack;
        this.packageCost = packageCost;
        this.availability = availability;
        this.stockLimit = stockLimit;
    }

    /**
     * Gets item id.
     *
     * @return the item id
     */
    public String getItemId() { return itemId; }

    /**
     * Gets description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets package type.
     *
     * @return the package type
     */
    public String getPackageType() {
        return packageType;
    }

    /**
     * Gets unit.
     *
     * @return the unit
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Gets units in pack.
     *
     * @return the units in pack
     */
    public int getUnitsInPack() {
        return unitsInPack;
    }

    /**
     * Gets package cost.
     *
     * @return the package cost
     */
    public double getPackageCost() {
        return packageCost;
    }

    /**
     * Gets availability.
     *
     * @return the availability
     */
    public int getAvailability() {
        return availability;
    }

    /**
     * Gets stock limit.
     *
     * @return the stock limit
     */
    public int getStockLimit() {
        return stockLimit;
    }

    /**
     * Sets availability.
     *
     * @param availability the availability
     */
    public void setAvailability(int availability) {
        this.availability = availability;
    }

    /**
     * Sets stock limit.
     *
     * @param stockLimit the stock limit
     */
    public void setStockLimit(int stockLimit) {
        this.stockLimit = stockLimit;
    }

    /**
     * Sets package cost.
     *
     * @param packageCost the package cost
     */
    public void setPackageCost(double packageCost) {
        this.packageCost = packageCost;
    }
}
