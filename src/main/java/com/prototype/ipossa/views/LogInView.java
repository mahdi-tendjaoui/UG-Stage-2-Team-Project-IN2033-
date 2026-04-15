package com.prototype.ipossa.views;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import com.prototype.ipossa.systems.Accounts.AccountService;


public class LogInView
{
    @FXML private TextField username;
    @FXML private PasswordField password;

    @FXML
    private void login()
    {
        String user = username.getText();
        String pw = password.getText();
        AccountService.login(user, pw);
    }

}
