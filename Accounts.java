public class Accounts {
    private String email;
    private String password;
    
    public Accounts(String email, String password) {
        this.email = email;
        this.password = password;
    }

//editing

    public String getEmail() {
        return email;
    }

    public void setEmail(String newEmail) {
        this.email = newEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String newPassword) {
        this.password = newPassword;
    }

    public void login(email, password) {
        // Implement login logic here
    }

    public void logout() {
        // Implement logout logic here
    }

    public float getBalance() {
        // Implement logic to retrieve account balance here
    }

    public void viewAccountDetails() {
        // Implement logic to view account details here
    }

    public void manageAccount() {
        // Implement logic to manage account settings here
    }

}