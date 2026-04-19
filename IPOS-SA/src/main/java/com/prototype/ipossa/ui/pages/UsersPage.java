package com.prototype.ipossa.ui.pages;

import com.prototype.ipossa.systems.ACC.AccountController;
import com.prototype.ipossa.systems.ACC.Role;
import com.prototype.ipossa.systems.ACC.UserAccount;
import com.prototype.ipossa.ui.DialogStyle;
import com.prototype.ipossa.ui.UIUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;

/**
 * The type Users page.
 */
public class UsersPage {

    private final UserAccount currentUser;
    private final Stage stage;
    private final AccountController controller = new AccountController();
    private final ObservableList<Row> data = FXCollections.observableArrayList();
    private TableView<Row> table;

    /**
     * Instantiates a new Users page.
     *
     * @param currentUser the current user
     * @param stage       the stage
     */
    public UsersPage(UserAccount currentUser, Stage stage) {
        this.currentUser = currentUser;
        this.stage = stage;
    }

    /**
     * Build node.
     *
     * @return the node
     */
    public Node build() {

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab staffTab = new Tab("Staff users");
        staffTab.setContent(staffPane());
        tabs.getTabs().add(staffTab);

        if (currentUser.canManageMerchantAccounts()) {
            Tab merchantsTab = new Tab("Merchants");
            ScrollPane sp = new ScrollPane(new MerchantManagementPanel(currentUser).build());
            sp.setFitToWidth(true);
            sp.setPadding(new Insets(0));
            merchantsTab.setContent(sp);
            tabs.getTabs().add(merchantsTab);
        }

        VBox root = new VBox(12);
        root.getChildren().add(tabs);
        VBox.setVgrow(tabs, Priority.ALWAYS);
        return root;
    }

    private Node staffPane() {
        VBox root = new VBox(12); root.setPadding(new Insets(12));

        if (!currentUser.canManageUserAccounts()) {
            root.getChildren().add(UIUtil.dim("Only administrators can manage staff accounts."));
            return root;
        }

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().add(UIUtil.h2("Staff accounts"));
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button addBtn = new Button("+ New user");
        addBtn.getStyleClass().addAll("button", "button-primary");
        addBtn.setOnAction(e -> addDialog());
        Button refresh = new Button("↻");
        refresh.getStyleClass().add("button");
        refresh.setOnAction(e -> reload());
        header.getChildren().addAll(sp, addBtn, refresh);
        root.getChildren().add(header);

        Label help = new Label("You cannot change or delete your own account here. To change your own password, use Settings → Account.");
        help.getStyleClass().add("dim");
        root.getChildren().add(help);

        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);
        root.getChildren().add(table);
        table.setItems(data);

        reload();
        return root;
    }

    private TableView<Row> buildTable() {
        TableView<Row> t = new TableView<>();
        t.setPlaceholder(UIUtil.dim("No staff accounts."));

        TableColumn<Row, String> u = new TableColumn<>("Username");
        u.setCellValueFactory(new PropertyValueFactory<>("username")); u.setPrefWidth(220);
        TableColumn<Row, String> r = new TableColumn<>("Role");
        r.setCellValueFactory(new PropertyValueFactory<>("role")); r.setPrefWidth(260);

        TableColumn<Row, Void> actions = new TableColumn<>("Actions");
        actions.setPrefWidth(280);
        actions.setCellFactory(c -> new TableCell<>() {
            final Button changeRole = new Button("Change role");
            final Button delete = new Button("Delete");
            final HBox box = new HBox(6, changeRole, delete);
            {
                changeRole.getStyleClass().add("button");
                delete.getStyleClass().addAll("button", "button-danger");
                changeRole.setOnAction(e -> changeRoleDialog(getTableRow().getItem()));
                delete.setOnAction(e -> deleteUser(getTableRow().getItem()));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                Row row = getTableRow() == null ? null : getTableRow().getItem();
                if (empty || row == null) { setGraphic(null); return; }
                boolean isSelf = row.username.get().equalsIgnoreCase(currentUser.getUsername());
                changeRole.setDisable(isSelf);
                delete.setDisable(isSelf);
                Tooltip tip = isSelf ? new Tooltip("You cannot modify your own account.") : null;
                Tooltip.install(changeRole, tip);
                Tooltip.install(delete, tip);
                setGraphic(box);
            }
        });

        t.getColumns().add(u);
        t.getColumns().add(r);
        t.getColumns().add(actions);
        return t;
    }

    private void reload() {
        data.clear();
        List<UserAccount> users = controller.getAllStaffAccounts();
        for (UserAccount ua : users)
            data.add(new Row(ua.getUsername(), ua.getRole().getDbValue()));
    }

    private List<String> selectableRoles() {
        return Arrays.stream(Role.values()).filter(Role::isStaffRole).map(Role::getDbValue).toList();
    }

    private void addDialog() {
        Dialog<Void> d = new Dialog<>();
        d.setTitle("New staff user");
        d.setHeaderText(null);

        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(8); g.setPadding(new Insets(4));
        TextField u = new TextField();
        PasswordField p = new PasswordField();
        ComboBox<String> role = new ComboBox<>(FXCollections.observableArrayList(selectableRoles()));
        role.setValue(Role.ACCOUNTANT.getDbValue());

        int r = 0;
        g.add(new Label("Username:"), 0, r); g.add(u, 1, r++);
        g.add(new Label("Password:"), 0, r); g.add(p, 1, r++);
        g.add(new Label("Role:"), 0, r); g.add(role, 1, r++);

        d.getDialogPane().setContent(g);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        DialogStyle.apply(d);

        d.setResultConverter(b -> {
            if (b != ButtonType.OK) return null;
            if (u.getText().isBlank() || p.getText().isBlank()) {
                UIUtil.warn("Missing", "Username and password are required."); return null;
            }
            boolean ok = controller.createStaffAccount(u.getText().trim(), p.getText(), role.getValue());
            if (ok) reload();
            else UIUtil.error("Error", "Could not create user. Username may already exist.");
            return null;
        });
        d.showAndWait();
    }

    private void changeRoleDialog(Row row) {
        if (row == null) return;
        if (row.username.get().equalsIgnoreCase(currentUser.getUsername())) {
            UIUtil.warn("Not allowed", "You cannot change your own role."); return;
        }
        ChoiceDialog<String> d = new ChoiceDialog<>(row.role.get(), selectableRoles());
        d.setTitle("Change role");
        d.setHeaderText("Change role for " + row.username.get());
        d.setContentText("New role:");
        DialogStyle.apply(d);
        d.showAndWait().ifPresent(newRole -> {
            if (!newRole.equals(row.role.get())) {
                if (controller.changeStaffRole(row.username.get(), newRole)) reload();
                else UIUtil.error("Error", "Could not change role.");
            }
        });
    }

    private void deleteUser(Row row) {
        if (row == null) return;
        if (row.username.get().equalsIgnoreCase(currentUser.getUsername())) {
            UIUtil.warn("Not allowed", "You cannot delete your own account."); return;
        }
        if (!UIUtil.confirm("Delete user", "Delete user '" + row.username.get() + "'? This cannot be undone.")) return;
        if (controller.deleteStaffAccount(row.username.get())) reload();
        else UIUtil.error("Error", "Could not delete user.");
    }

    /**
     * The type Row.
     */
    public static class Row {
        /**
         * The Username.
         */
        public final SimpleStringProperty username, /**
         * The Role.
         */
        role;

        /**
         * Instantiates a new Row.
         *
         * @param u the u
         * @param r the r
         */
        public Row(String u, String r) {
            this.username = new SimpleStringProperty(u);
            this.role = new SimpleStringProperty(r);
        }

        /**
         * Gets username.
         *
         * @return the username
         */
        public String getUsername() { return username.get(); }

        /**
         * Gets role.
         *
         * @return the role
         */
        public String getRole() { return role.get(); }
    }
}
