package com.prototype.ipossa;

import com.prototype.ipossa.ui.LoginScreen;
import com.prototype.ipossa.ui.MerchantStateUpdater;
import com.prototype.ipossa.ui.SchemaInit;
import javafx.application.Application;
import javafx.stage.Stage;

public class Launcher extends Application {

    public static void main(String[] args) {
        try {
            MyJDBC.getConnection().close();
            System.out.println("=== Connected to DB ===");
        } catch (Exception e) {
            System.err.println("WARNING: could not reach database: " + e.getMessage());
            System.err.println("The app will still start — log-in will fail until the DB is reachable.");
        }
        SchemaInit.ensureTables();
        MerchantStateUpdater.refreshAll();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(680);
        new LoginScreen(primaryStage).show();
    }
}
