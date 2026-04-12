package main;

import java.util.ArrayList;
import java.util.List;

public class AccountService {
    private List<Accounts> accounts;

    public AccountService() {
        this.accounts = new ArrayList<>();
    }

    public boolean addAccount(Accounts account) {
        if(account == null) {
            return false;
        }
        if (searchAccount(account.getUsername()) != null) {
            return false;
        }
        accounts.add(account);
        return true;
    }
    public Accounts searchAccount(String username) {
        for (Accounts account : accounts) {
            if (account.getUsername().equals(username)) {
                return account;
            }
        }
        return null;
    }
    public boolean removeAccount(String username) {
        Accounts account = searchAccount(username);

        if (account != null) {
            accounts.remove(account);
            return true;
        }
        return false;
    }
    public boolean changeRole(String username, String newRole) {
        Accounts accounts = searchAccount(username);
        if (accounts != null && newRole != null) {
            accounts.setRole(newRole);
            return true;
        }
        return false;
    }
}
