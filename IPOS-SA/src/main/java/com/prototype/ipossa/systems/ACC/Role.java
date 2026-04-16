package com.prototype.ipossa.systems.ACC;

public enum Role {

    ADMINISTRATOR("Administrator"),
    DIRECTOR_OF_OPERATIONS("Director of Operations"),
    SENIOR_ACCOUNTANT("Senior accountant"),
    ACCOUNTANT("Accountant"),
    MANAGER("Manager"),
    WAREHOUSE_EMPLOYEE("Warehouse employee"),
    DELIVERY_EMPLOYEE("Delivery department employee"),

    MERCHANT("Merchant"),

    UNKNOWN("Unknown");

    private final String dbValue;

    Role(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static Role fromDbValue(String value) {
        if (value == null) return UNKNOWN;
        for (Role r : values()) {
            if (r.dbValue.equalsIgnoreCase(value.trim())) return r;
        }
        return UNKNOWN;
    }

    public boolean canManageUserAccounts() {
        return this == ADMINISTRATOR;
    }

    public boolean canManageMerchantAccounts() {
        return this == ADMINISTRATOR || this == DIRECTOR_OF_OPERATIONS;
    }

    public boolean canSetCreditLimit() {
        return this == ADMINISTRATOR || this == DIRECTOR_OF_OPERATIONS;
    }

    public boolean canManageDiscountPlans() {
        return this == ADMINISTRATOR || this == DIRECTOR_OF_OPERATIONS;
    }

    public boolean canReactivateDefaultAccount() {
        return this == DIRECTOR_OF_OPERATIONS || this == ADMINISTRATOR;
    }

    public boolean canManageCatalogue() {
        return this == ADMINISTRATOR || this == DIRECTOR_OF_OPERATIONS || this == WAREHOUSE_EMPLOYEE;
    }

    public boolean canManageOrders() {
        return this == ADMINISTRATOR || this == DIRECTOR_OF_OPERATIONS  || this == WAREHOUSE_EMPLOYEE  || this == DELIVERY_EMPLOYEE;
    }

    public boolean canGenerateInvoice() {
        return this == ADMINISTRATOR
                || this == DIRECTOR_OF_OPERATIONS
                || this == SENIOR_ACCOUNTANT
                || this == ACCOUNTANT
                || this == MANAGER
                || this == MERCHANT;
    }

    public boolean canRecordPayments() {
        return this == ADMINISTRATOR
                || this == SENIOR_ACCOUNTANT
                || this == ACCOUNTANT
                || this == MERCHANT;
    }

    public boolean canGenerateReports() {
        return this == ADMINISTRATOR || this == DIRECTOR_OF_OPERATIONS;
    }

    public boolean isStaffRole() {
        return this != MERCHANT && this != UNKNOWN;
    }

    @Override
    public String toString() {
        return dbValue;
    }
}
