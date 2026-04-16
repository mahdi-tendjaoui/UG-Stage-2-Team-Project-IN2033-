package com.prototype.ipossa.ui;

import com.prototype.ipossa.MyJDBC;
import com.prototype.ipossa.systems.ACC.*;
import com.prototype.ipossa.ui.pages.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MainApp {

    private final Stage stage;
    private final UserAccount currentUser;     // null when merchant logged in
    private final MerchantAccount currentMerchant; // null when staff logged in

    private VBox sidebar;
    private static final double SIDEBAR_WIDTH = 240;

    private final List<NavButton> navButtons = new ArrayList<>();
    private StackPane contentArea;
    private Label pageTitleLabel;
    private BorderPane root;

    /** Staff session */
    public MainApp(Stage stage, UserAccount user) {
        this.stage = stage;
        this.currentUser = user;
        this.currentMerchant = null;
    }

    /** Merchant session */
    public MainApp(Stage stage, MerchantAccount merchant) {
        this.stage = stage;
        this.currentUser = null;
        this.currentMerchant = merchant;
    }

    public void show() {
        root = new BorderPane();
        root.getStyleClass().add("root");

        sidebar = buildSidebar();
        root.setLeft(sidebar);
        root.setTop(buildTopBar());

        contentArea = new StackPane();
        contentArea.setPadding(new Insets(20));
        root.setCenter(contentArea);

        Scene scene = new Scene(root, 1320, 840);
        ThemeManager.get().register(scene);
        stage.setScene(scene);
        stage.setTitle(isMerchant()
                ? "IPOS-SA — " + currentMerchant.getAccountHolderName() + " (Merchant)"
                : "IPOS-SA — " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        stage.show();

        showPage(navButtons.get(0).label);

        if (!isMerchant()) showStockWarnings();
        else showMerchantPaymentReminder();
    }

    private boolean isMerchant() { return currentMerchant != null; }

    // ─── TOP BAR ────────────────────────────────────────────────────────
    private HBox buildTopBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("topbar");

        Label hi = new Label(isMerchant()
                ? "Hello, " + currentMerchant.getAccountHolderName()
                : "Hello, " + currentUser.getUsername());
        hi.getStyleClass().add("dim");

        pageTitleLabel = new Label("Dashboard");
        pageTitleLabel.getStyleClass().add("h1");

        VBox titleBox = new VBox(2, hi, pageTitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ToggleButton themeBtn = new ToggleButton(ThemeManager.get().isDark() ? "☀  Light mode" : "🌙  Dark mode");
        themeBtn.setSelected(ThemeManager.get().isDark());
        themeBtn.getStyleClass().add("toggle-button");
        themeBtn.setOnAction(e -> {
            ThemeManager.get().toggle();
            themeBtn.setText(ThemeManager.get().isDark() ? "☀  Light mode" : "🌙  Dark mode");
        });

        Label userLabel = new Label(isMerchant()
                ? "Merchant"
                : currentUser.getRole().getDbValue());
        userLabel.getStyleClass().add("dim");

        bar.getChildren().addAll(titleBox, spacer, themeBtn, userLabel);
        return bar;
    }

    private void logout() {
        if (!UIUtil.confirm("Logout", "Are you sure you want to log out?")) return;
        new AccountController().logout();
        ThemeManager.get().unregister(stage.getScene());
        new LoginScreen(stage).show();
    }

    // ─── SIDEBAR ────────────────────────────────────────────────────────
    private VBox buildSidebar() {
        VBox bar = new VBox();
        bar.getStyleClass().add("sidebar");
        bar.setPrefWidth(SIDEBAR_WIDTH);
        bar.setMinWidth(SIDEBAR_WIDTH);
        bar.setMaxWidth(SIDEBAR_WIDTH);

        Label brand = new Label("IPOS-SA");
        brand.getStyleClass().add("sidebar-brand");
        bar.getChildren().add(brand);

        navButtons.clear();

        if (isMerchant()) {
            // ── MERCHANT NAV ──
            section(bar, "MERCHANT");
            addNav(bar, "Dashboard");
            addNav(bar, "Catalogue");
            addNav(bar, "My Orders");
            addNav(bar, "Account");
        } else {
            // ── STAFF NAV ── role-gated so non-applicable tabs don't appear
            section(bar, "MAIN");
            addNav(bar, "Dashboard");

            if (currentUser.canManageCatalogue()) addNav(bar, "Catalogue");
            if (currentUser.canManageOrders() || currentUser.canRecordPayments())
                addNav(bar, "Orders");
            if (currentUser.canGenerateReports()) addNav(bar, "Reports");
            if (currentUser.canManageMerchantAccounts() || currentUser.canManageUserAccounts())
                addNav(bar, "Applications");

            // Admin section visible only to admins / Director of Operations
            if (currentUser.canManageUserAccounts() || currentUser.canManageMerchantAccounts()) {
                section(bar, "ADMIN");
                addNav(bar, "Users");
            }

            section(bar, "ACCOUNT");
            addNav(bar, "Settings");
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        bar.getChildren().add(spacer);

        Button logoutBtn = new Button("⏻   Logout");
        logoutBtn.getStyleClass().addAll("nav-button", "sidebar-logout");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setAlignment(Pos.CENTER_LEFT);
        logoutBtn.setOnAction(e -> logout());
        bar.getChildren().add(logoutBtn);

        return bar;
    }

    private void section(VBox bar, String title) {
        Label l = new Label(title);
        l.getStyleClass().add("sidebar-section");
        bar.getChildren().add(l);
    }

    private void addNav(VBox bar, String label) {
        NavButton nb = new NavButton(label);
        nb.button.setOnAction(e -> showPage(label));
        navButtons.add(nb);
        bar.getChildren().add(nb.button);
    }

    // ─── PAGE NAVIGATION ─────────────────────────────────────────────────
    private void showPage(String name) {
        for (NavButton nb : navButtons) nb.setActive(nb.label.equals(name));
        pageTitleLabel.setText(name);
        contentArea.getChildren().clear();
        Node page = buildPage(name);
        if (page != null) contentArea.getChildren().add(page);
    }

    private Node buildPage(String name) {
        if (isMerchant()) {
            return switch (name) {
                case "Dashboard" -> new MerchantDashboardPage(currentMerchant).build();
                case "Catalogue" -> new CataloguePage(null, true).build();
                case "My Orders" -> new MerchantOrdersPage(currentMerchant).build();
                case "Account"   -> new MerchantAccountPage(currentMerchant).build();
                default          -> new Label("Unknown page");
            };
        }
        return switch (name) {
            case "Dashboard"    -> new DashboardPage(currentUser).build();
            case "Catalogue"    -> new CataloguePage(currentUser, false).build();
            case "Orders"       -> new OrdersPage(currentUser).build();
            case "Reports"      -> new ReportsPage(currentUser).build();
            case "Applications" -> new ApplicationsPage(currentUser).build();
            case "Users"        -> new UsersPage(currentUser, stage).build();
            case "Settings"     -> new SettingsPage(currentUser).build();
            default             -> new Label("Unknown page");
        };
    }

    // ─── STOCK WARNINGS ─────────────────────────────────────────────────
    private void showStockWarnings() {
        try (Connection conn = MyJDBC.getConnection()) {
            ResultSet rs = conn.prepareStatement(
                    "SELECT description, availability, stock_limit FROM catalogue WHERE availability < stock_limit"
            ).executeQuery();
            List<String> lowStock = new ArrayList<>();
            while (rs.next()) {
                lowStock.add(rs.getString("description") +
                        " (stock: " + rs.getInt("availability") +
                        ", min: " + rs.getInt("stock_limit") + ")");
            }
            if (!lowStock.isEmpty()) {
                String msg = "The following items are below their minimum stock level:\n\n• "
                        + String.join("\n• ", lowStock);
                UIUtil.warn("Low stock warning", msg);
            }
        } catch (Exception e) { /* non-critical */ }
    }

    private void showMerchantPaymentReminder() {
        // Placeholder: in a real system, calculate balance vs. payment due date.
        // The existing AccountService.shouldShowPaymentReminder handles the logic;
        // we keep this hook so the brief's "reminder on every login" requirement
        // can be wired up in MerchantOrdersPage where balance is computed.
    }

    // ─── Inner nav button helper ────────────────────────────────────────
    private static class NavButton {
        final Button button;
        final String label;
        NavButton(String label) {
            this.label = label;
            button = new Button(label);
            button.setMaxWidth(Double.MAX_VALUE);
            button.setAlignment(Pos.CENTER_LEFT);
            button.getStyleClass().add("nav-button");
        }
        void setActive(boolean active) {
            button.getStyleClass().remove("nav-button-active");
            if (active) button.getStyleClass().add("nav-button-active");
        }
    }
}
