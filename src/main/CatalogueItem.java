public class CatalogueItem {

    private String itemId;
    private String itemName;
    private String description;
    private double unitPrice;
    private int stockQuantity;

    
    public CatalogueItem() {
    }

    /**
     * Constructor with the main catalogue item details
     */
    public CatalogueItem(String itemId, String itemName, String description, double unitPrice, int stockQuantity) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.description = description;
        this.unitPrice = unitPrice;
        this.stockQuantity = stockQuantity;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
}