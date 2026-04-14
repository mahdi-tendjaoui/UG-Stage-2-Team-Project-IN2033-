package com.prototype.ipossa.systems.Accounts;

// Represents a merchant account from the merchants table
public class MerchantAccount {

    private int    merchantID;
    private String accountHolderName;
    private String accountNumber;
    private String contactName;
    private String address;
    private String phoneNumber;
    private double creditLimit;
    private String agreedDiscount;  // "Fixed" or "Variable"
    private String login;
    private String password;
    private String accountState;    // "normal", "suspended", "in_default"

    public MerchantAccount(int merchantID, String accountHolderName, String accountNumber,
                           String contactName, String address, String phoneNumber,
                           double creditLimit, String agreedDiscount,
                           String login, String password, String accountState) {
        this.merchantID        = merchantID;
        this.accountHolderName = accountHolderName;
        this.accountNumber     = accountNumber;
        this.contactName       = contactName;
        this.address           = address;
        this.phoneNumber       = phoneNumber;
        this.creditLimit       = creditLimit;
        this.agreedDiscount    = agreedDiscount;
        this.login             = login;
        this.password          = password;
        this.accountState      = accountState;
    }

    // Simple state checks
    public boolean isNormal()     { return accountState.equals("normal"); }
    public boolean isSuspended()  { return accountState.equals("suspended"); }
    public boolean isInDefault()  { return accountState.equals("in_default"); }
    public boolean canPlaceOrders() { return isNormal(); }

    // Getters
    public int    getMerchantID()        { return merchantID; }
    public String getAccountHolderName() { return accountHolderName; }
    public String getAccountNumber()     { return accountNumber; }
    public String getContactName()       { return contactName; }
    public String getAddress()           { return address; }
    public String getPhoneNumber()       { return phoneNumber; }
    public double getCreditLimit()       { return creditLimit; }
    public String getAgreedDiscount()    { return agreedDiscount; }
    public String getLogin()             { return login; }
    public String getPassword()          { return password; }
    public String getAccountState()      { return accountState; }

    // Setters
    public void setMerchantID(int merchantID)               { this.merchantID = merchantID; }
    public void setAccountHolderName(String name)           { this.accountHolderName = name; }
    public void setAccountNumber(String accountNumber)      { this.accountNumber = accountNumber; }
    public void setContactName(String contactName)          { this.contactName = contactName; }
    public void setAddress(String address)                  { this.address = address; }
    public void setPhoneNumber(String phoneNumber)          { this.phoneNumber = phoneNumber; }
    public void setCreditLimit(double creditLimit)          { this.creditLimit = creditLimit; }
    public void setAgreedDiscount(String agreedDiscount)    { this.agreedDiscount = agreedDiscount; }
    public void setLogin(String login)                      { this.login = login; }
    public void setPassword(String password)                { this.password = password; }
    public void setAccountState(String accountState)        { this.accountState = accountState; }

    @Override
    public String toString() {
        return accountHolderName + " (" + accountNumber + ") - " + accountState;
    }
}