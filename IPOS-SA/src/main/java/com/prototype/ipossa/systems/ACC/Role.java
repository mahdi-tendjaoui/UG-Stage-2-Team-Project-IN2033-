package com.prototype.ipossa.systems.ACC;

/**
 * The enum Role.
 */
public enum Role {

    /**
     * Administrator role.
     */
    ADMINISTRATOR("Administrator"),
    /**
     * The Director of operations.
     */
    DIRECTOR_OF_OPERATIONS("Director of Operations"),
    /**
     * The Senior accountant.
     */
    SENIOR_ACCOUNTANT("Senior accountant"),
    /**
     * Accountant role.
     */
    ACCOUNTANT("Accountant"),
    /**
     * Manager role.
     */
    MANAGER("Manager"),
    /**
     * The Warehouse employee.
     */
    WAREHOUSE_EMPLOYEE("Warehouse employee"),
    /**
     * The Delivery employee.
     */
    DELIVERY_EMPLOYEE("Delivery department employee"),

    /**
     * Merchant role.
     */
    MERCHANT("Merchant"),

    /**
     * Unknown role.
     */
    UNKNOWN("Unknown");

    private final String dbValue;

    Role(String dbValue) {
        this.dbValue = dbValue;
    }

    /**
     * Gets db value.
     *
     * @return the db value
     */
    public String getDbValue() {
        return dbValue;
    }

    /**
     * From db value role.
     *
     * @param value the value
     * @return the role
     */
    public static Role fromDbValue(String value) {
        if (value == null) return UNKNOWN;
        for (Role r : values()) {
            if (r.dbValue.equalsIgnoreCase(value.trim())) return r;
        }
        return UNKNOWN;
    }

    /**
     * Can manage user accounts boolean.
     *
     * @return the boolean
     */
    public boolean canManageUserAccounts() {
        return this == ADMINISTRATOR;
    }

    /**
     * Can manage merchant accounts boolean.
     *
     * @return the boolean
     */
    public boolean canManageMerchantAccounts() {
        return this == ADMINISTRATOR || this == DIRECTOR_OF_OPERATIONS;
    }

    /**
     * Can set credit limit boolean.
     *
     * @return the boolean
     */
    public boolean canSetCreditLimit() {
        return this == ADMINISTRATOR || this == DIRECTOR_OF_OPERATIONS;
    }

    /**
     * Can manage discount plans boolean.
     *
     * @return the boolean
     */
    public boolean canManageDiscountPlans() {
        return this == ADMINISTRATOR || this == DIRECTOR_OF_OPERATIONS;
    }

    /**
     * Can reactivate default account boolean.
     *
     * @return the boolean
     */
    public boolean canReactivateDefaultAccount() {
        return this == DIRECTOR_OF_OPERATIONS || this == ADMINISTRATOR;
    }

    /**
     * Can manage catalogue boolean.
     *
     * @return the boolean
     */
    public boolean canManageCatalogue() {
        return this == ADMINISTRATOR || this == DIRECTOR_OF_OPERATIONS || this == WAREHOUSE_EMPLOYEE;
    }

    /**
     * Can manage orders boolean.
     *
     * @return the boolean
     */
    public boolean canManageOrders() {
        return this == ADMINISTRATOR || this == DIRECTOR_OF_OPERATIONS  || this == WAREHOUSE_EMPLOYEE  || this == DELIVERY_EMPLOYEE;
    }

    /**
     * Can generate invoice boolean.
     *
     * @return the boolean
     */
    public boolean canGenerateInvoice() {
        return this == ADMINISTRATOR
                || this == DIRECTOR_OF_OPERATIONS
                || this == SENIOR_ACCOUNTANT
                || this == ACCOUNTANT
                || this == MANAGER
                || this == MERCHANT;
    }

    /**
     * Can record payments boolean.
     *
     * @return the boolean
     */
    public boolean canRecordPayments() {
        return this == ADMINISTRATOR
                || this == SENIOR_ACCOUNTANT
                || this == ACCOUNTANT
                || this == MERCHANT;
    }

    /**
     * Can generate reports boolean.
     *
     * @return the boolean
     */
    public boolean canGenerateReports() {
        return this == ADMINISTRATOR || this == DIRECTOR_OF_OPERATIONS;
    }

    /**
     * Is staff role boolean
     *
     * @return the boolean
     */
    public boolean isStaffRole() {
        return this != MERCHANT && this != UNKNOWN;
    }

    /**
     * toString
     * @return
     */
    @Override
    public String toString() {
        return dbValue;
    }
}
