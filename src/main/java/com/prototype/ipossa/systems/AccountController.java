package com.prototype.ipossa.systems;

import java.util.ArrayList;
import java.util.List;

public class AccountController {
    private List<Accounts> accounts;

    public AccountController() {
        this.accounts = new ArrayList<>();
    }

    //Create an account, assuming the user is new
    public void createAccount(String username, String password, String role) {
    }

    public void searchAccount(String username) {
    }
}
