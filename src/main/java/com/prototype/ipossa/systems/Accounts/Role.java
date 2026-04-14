package com.prototype.ipossa.systems.Accounts;

/**
 * This file defines all roles, and their priviliges
 * The hierarchy goes from:
 *   Administrator > Director of Operations > Senior Accountant > Accountant
 *   Warehouse Employee / Delivery Department Employee
 */

public enum Role {

    //Roles
    ADMINISTRATOR("Administrator"),
    DIRECTOR_OF_OPERATIONS("Director of Operations"),
    SENIOR_ACCOUNTANT("Senior accountant"),
    ACCOUNTANT("Accountant"),
    WAREHOUSE_EMPLOYEE("Warehouse employee"),
    DELIVERY_EMPLOYEE("Delivery department employee"),
    //Merchant role
    MERCHANT("Merchant"),
    //If the role is not known, this is what is returned
    UNKNOWN("Unknown");

    private final String dbValue;

    Role(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    //Converts a raw database string to the matching Role enum constant.
    //Returns unknown if role not recognised
    public static Role fromDbValue(String value) {
        if (value == null) return UNKNOWN;
        for (Role r : values()) {
            if (r.dbValue.equalsIgnoreCase(value.trim())) return r;
        }
        return UNKNOWN;
    }

    //Below are methods to check if the accounts can do certain privileges based on what type they are
    //create / delete / change role
    public boolean canManageUserAccounts() {
        return this == ADMINISTRATOR;
    }
    //create, edit or delete merchant accounts
    public boolean canManageMerchantAccounts() {
        return this == ADMINISTRATOR || this == DIRECTOR_OF_OPERATIONS;
    }
    //set or change a merchant's credit limit
    public boolean canSetCreditLimit() {
        return this == ADMINISTRATOR || this == DIRECTOR_OF_OPERATIONS;
    }
    //set or change a merchant's discount plan
    public boolean canManageDiscountPlans() {
        return this == ADMINISTRATOR || this == DIRECTOR_OF_OPERATIONS;
    }
    //When payment delay is longer than 30 days, the user merchant account is flagged as in default
    public boolean canReactivateDefaultAccount() {
        return this == DIRECTOR_OF_OPERATIONS || this == ADMINISTRATOR;
    }
    //view and update the product catalogue
    public boolean canManageCatalogue() {
        return this == ADMINISTRATOR || this == DIRECTOR_OF_OPERATIONS || this == WAREHOUSE_EMPLOYEE;
    }
    //view, update status, generate invoice
    public boolean canManageOrders() {
        return this == ADMINISTRATOR || this == DIRECTOR_OF_OPERATIONS  || this == WAREHOUSE_EMPLOYEE  || this == DELIVERY_EMPLOYEE;
    }
    //record payments made by merchants
    public boolean canRecordPayments() {
        return this == ADMINISTRATOR || this == SENIOR_ACCOUNTANT || this == ACCOUNTANT;
    }
    //generate reports
    public boolean canGenerateReports() {
        return this == ADMINISTRATOR || this == DIRECTOR_OF_OPERATIONS;
    }
    //can the role access the system?
    public boolean isStaffRole() {
        return this != MERCHANT && this != UNKNOWN;
    }

    @Override
    public String toString() {
        return dbValue;
    }
}
