package com.prototype.ipossa.ui.pages;

import com.prototype.ipossa.MyJDBC;
import com.prototype.ipossa.systems.ACC.*;
import com.prototype.ipossa.ui.DialogStyle;
import com.prototype.ipossa.ui.UIUtil;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Merchant management panel.
 */
public class MerchantManagementPanel {

    private final UserAccount user;
    private final AccountController controller = new AccountController();
    private final ObservableList<MerchRow> data = FXCollections.observableArrayList();
    private TableView<MerchRow> table;

    /**
     * Instantiates a new Merchant management panel.
     *
     * @param user the user
     */
    public MerchantManagementPanel(UserAccount user) { this.user = user; }

    /**
     * Build node.
     *
     * @return the node
     */
    public Node build() {
        VBox box = new VBox(12);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label h = UIUtil.h2("Merchant accounts");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button add = new Button("+ New merchant");
        add.getStyleClass().addAll("button", "button-primary");
        add.setDisable(!user.canManageMerchantAccounts());
        add.setOnAction(e -> merchantDialog(null));
        Button refresh = new Button("↻");
        refresh.getStyleClass().add("button");
        refresh.setOnAction(e -> reload());
        header.getChildren().addAll(h, sp, add, refresh);
        box.getChildren().add(header);

        table = new TableView<>();
        table.setPlaceholder(UIUtil.dim("No merchants."));
        table.setPrefHeight(420);

        table.getColumns().add(strCol("Name", "name", 180));
        table.getColumns().add(strCol("Account #", "accountNo", 110));
        var creditCol = numCol("Credit", "creditLimit", 110);
        creditCol.setCellFactory(com.prototype.ipossa.ui.Formats.moneyCell());
        table.getColumns().add(creditCol);
        table.getColumns().add(strCol("Discount", "discountType", 100));

        TableColumn<MerchRow, String> stateCol = new TableColumn<>("State");
        stateCol.setCellValueFactory(new PropertyValueFactory<>("state"));
        stateCol.setPrefWidth(110);
        stateCol.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); setText(null); return; }
                Label l = new Label(s); l.getStyleClass().add("badge");
                l.getStyleClass().add(switch (s.toLowerCase()) {
                    case "suspended" -> "badge-suspended";
                    case "in_default", "in default" -> "badge-default";
                    default -> "badge-normal";
                });
                setGraphic(l); setText(null);
            }
        });
        table.getColumns().add(stateCol);

        TableColumn<MerchRow, Void> actions = new TableColumn<>("Actions");
        actions.setPrefWidth(440);
        actions.setCellFactory(c -> new TableCell<>() {
            final Button edit = new Button("Edit");
            final Button credit = new Button("Credit");
            final Button disc = new Button("Discount");
            final Button state = new Button("State");
            final Button del = new Button("Delete");
            final HBox row = new HBox(6, edit, credit, disc, state, del);
            {
                edit.getStyleClass().add("button");
                credit.getStyleClass().add("button");
                disc.getStyleClass().add("button");
                state.getStyleClass().add("button");
                del.getStyleClass().addAll("button", "button-danger");
                edit.setOnAction(e -> merchantDialog(getTableRow().getItem()));
                credit.setOnAction(e -> creditDialog(getTableRow().getItem()));
                disc.setOnAction(e -> discountDialog(getTableRow().getItem()));
                state.setOnAction(e -> stateDialog(getTableRow().getItem()));
                del.setOnAction(e -> deleteMerchant(getTableRow().getItem()));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                MerchRow r = getTableRow() == null ? null : getTableRow().getItem();
                if (empty || r == null) { setGraphic(null); return; }
                edit.setDisable(!user.canManageMerchantAccounts());
                credit.setDisable(!user.canSetCreditLimit());
                disc.setDisable(!user.canManageDiscountPlans());
                state.setDisable(!user.canManageMerchantAccounts());
                del.setDisable(!user.canManageUserAccounts());
                setGraphic(row);
            }
        });
        table.getColumns().add(actions);
        table.setItems(data);

        box.getChildren().add(table);
        reload();
        return box;
    }

    private TableColumn<MerchRow, String> strCol(String t, String p, double w) {
        TableColumn<MerchRow, String> c = new TableColumn<>(t);
        c.setCellValueFactory(new PropertyValueFactory<>(p)); c.setPrefWidth(w); return c;
    }
    private TableColumn<MerchRow, Number> numCol(String t, String p, double w) {
        TableColumn<MerchRow, Number> c = new TableColumn<>(t);
        c.setCellValueFactory(new PropertyValueFactory<>(p)); c.setPrefWidth(w); return c;
    }

    private void reload() {
        data.clear();
        List<MerchantAccount> all = controller.getAllMerchants();
        for (MerchantAccount m : all)
            data.add(new MerchRow(m.getMerchantID(), m.getAccountHolderName(), m.getAccountNumber(),
                    m.getCreditLimit(),
                    m.getDiscountType() == null ? "" : m.getDiscountType().getDbValue(),
                    m.getAccountState() == null ? "normal" : m.getAccountState().getDbValue()));
    }

    private void merchantDialog(MerchRow existing) {
        Dialog<Void> d = new Dialog<>();
        d.setTitle(existing == null ? "New merchant" : "Edit merchant");

        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(8); g.setPadding(new Insets(4));

        MerchantAccount loaded = null;
        if (existing != null) {
            try (Connection conn = MyJDBC.getConnection()) {
                loaded = new AccountService().getMerchant(conn, existing.id);
            } catch (Exception ignored) {}
        }
        final MerchantAccount cur = loaded;

        TextField holder = new TextField(cur == null ? "" : cur.getAccountHolderName());
        TextField accno = new TextField(cur == null ? "" : cur.getAccountNumber());
        TextField contact = new TextField(cur == null ? "" : cur.getContactName());
        TextField addr = new TextField(cur == null ? "" : cur.getAddress());
        TextField phone = new TextField(cur == null ? "" : cur.getPhoneNumber());
        TextField credit = new TextField(cur == null ? "0.00" : String.format("%.2f", cur.getCreditLimit()));
        ComboBox<String> discType = new ComboBox<>(FXCollections.observableArrayList("Fixed", "Variable"));
        discType.setValue(cur == null ? "Fixed"
                : (cur.getDiscountType() == null ? "Fixed" : cur.getDiscountType().getDbValue()));
        TextField login = new TextField(cur == null ? "" : cur.getLogin());
        PasswordField pw = new PasswordField();
        if (cur != null) pw.setPromptText("(leave blank to keep unchanged)");

        if (existing != null) accno.setDisable(true);

        int r = 0;
        g.add(new Label("Holder name:"), 0, r); g.add(holder, 1, r++);
        g.add(new Label("Account #:"), 0, r); g.add(accno, 1, r++);
        g.add(new Label("Contact:"), 0, r); g.add(contact, 1, r++);
        g.add(new Label("Address:"), 0, r); g.add(addr, 1, r++);
        g.add(new Label("Phone:"), 0, r); g.add(phone, 1, r++);
        g.add(new Label("Credit limit:"), 0, r); g.add(credit, 1, r++);
        g.add(new Label("Discount type:"), 0, r); g.add(discType, 1, r++);
        g.add(new Label("Login:"), 0, r); g.add(login, 1, r++);
        g.add(new Label("Password:"), 0, r); g.add(pw, 1, r++);

        d.getDialogPane().setContent(g);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        DialogStyle.apply(d);

        d.setResultConverter(b -> {
            if (b != ButtonType.OK) return null;
            try {
                double limit = Double.parseDouble(credit.getText().trim());
                if (existing == null) {
                    boolean ok = controller.createMerchantAccount(
                            holder.getText().trim(), accno.getText().trim(),
                            contact.getText().trim(), addr.getText().trim(),
                            phone.getText().trim(), limit,
                            discType.getValue(), login.getText().trim(), pw.getText());
                    if (!ok) UIUtil.error("Error", "Could not create merchant.");
                } else {
                    controller.updateMerchantDetails(existing.id,
                            contact.getText().trim(), addr.getText().trim(), phone.getText().trim());
                    controller.setCreditLimit(existing.id, limit);
                }
                reload();
            } catch (Exception ex) { UIUtil.error("Error", ex.getMessage()); }
            return null;
        });
        d.showAndWait();
    }

    private void creditDialog(MerchRow r) {
        TextInputDialog d = new TextInputDialog(String.format("%.2f", r.creditLimit.get()));
        d.setTitle("Credit limit");
        d.setHeaderText("Set credit limit for " + r.name.get());
        d.setContentText("Amount:");
        DialogStyle.apply(d);
        d.showAndWait().ifPresent(v -> {
            try {
                controller.setCreditLimit(r.id, Double.parseDouble(v.trim()));
                reload();
            } catch (Exception e) { UIUtil.error("Invalid", e.getMessage()); }
        });
    }

    private void discountDialog(MerchRow r) {
        Dialog<Void> d = new Dialog<>();
        d.setTitle("Discount plan");
        d.setHeaderText("Manage discount tiers for " + r.name.get());

        VBox box = new VBox(10); box.setPadding(new Insets(4));

        ObservableList<TierRow> tierData = FXCollections.observableArrayList();
        try (Connection conn = MyJDBC.getConnection()) {

        } catch (Exception ignored) {}

        TableView<TierRow> tbl = new TableView<>(tierData);
        tbl.setPrefHeight(220);
        tbl.setPlaceholder(UIUtil.dim("No tiers - click 'Add tier'."));

        TableColumn<TierRow, String> c1 = new TableColumn<>("Min");
        c1.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().min == null ? "0" : String.valueOf(cd.getValue().min)));
        TableColumn<TierRow, String> c2 = new TableColumn<>("Max (blank=unlimited)");
        c2.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().max == null ? "" : String.valueOf(cd.getValue().max)));
        TableColumn<TierRow, String> c3 = new TableColumn<>("Rate %");
        c3.setCellValueFactory(cd -> new SimpleStringProperty(String.valueOf(cd.getValue().rate)));
        c1.setPrefWidth(120); c2.setPrefWidth(160); c3.setPrefWidth(110);
        tbl.getColumns().add(c1); tbl.getColumns().add(c2); tbl.getColumns().add(c3);

        Button addTier = new Button("+ Add tier");
        addTier.getStyleClass().add("button");
        addTier.setOnAction(e -> {
            TextInputDialog d1 = inputDialog("New tier", "Min (blank = 0):");
            d1.showAndWait().ifPresent(minV -> {
                TextInputDialog d2 = inputDialog("New tier", "Max (blank = unlimited):");
                d2.showAndWait().ifPresent(maxV -> {
                    TextInputDialog d3 = inputDialog("New tier", "Rate %:");
                    d3.showAndWait().ifPresent(rateV -> {
                        try {
                            Double min = minV.isBlank() ? null : Double.parseDouble(minV);
                            Double max = maxV.isBlank() ? null : Double.parseDouble(maxV);
                            tierData.add(new TierRow(min, max, Double.parseDouble(rateV)));
                        } catch (Exception ex) { UIUtil.error("Invalid", ex.getMessage()); }
                    });
                });
            });
        });
        Button removeTier = new Button("− Remove selected");
        removeTier.getStyleClass().add("button");
        removeTier.setOnAction(e -> {
            TierRow s = tbl.getSelectionModel().getSelectedItem();
            if (s != null) tierData.remove(s);
        });
        Button deletePlan = new Button("Delete entire plan");
        deletePlan.getStyleClass().addAll("button", "button-danger");
        deletePlan.setOnAction(e -> {
            if (UIUtil.confirm("Delete plan", "Remove the discount plan for " + r.name.get() + "?")) {
                controller.deleteDiscountPlan(r.id);
                tierData.clear();
            }
        });

        HBox btns = new HBox(8, addTier, removeTier, deletePlan);
        box.getChildren().addAll(tbl, btns);

        d.getDialogPane().setContent(box);
        d.getDialogPane().setPrefWidth(540);
        ButtonType save = new ButtonType("Save plan", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);
        DialogStyle.apply(d);

        d.setResultConverter(b -> {
            if (b == save) {
                List<DiscountTier> out = new ArrayList<>();
                for (TierRow t : tierData) out.add(new DiscountTier(r.id, t.min, t.max, t.rate));
                if (out.isEmpty()) {
                    UIUtil.warn("Empty", "Add at least one tier or use 'Delete entire plan'."); return null;
                }
                if (!controller.setDiscountPlan(r.id, out))
                    UIUtil.error("Error", "Could not save plan.");
            }
            return null;
        });
        d.showAndWait();
    }

    private TextInputDialog inputDialog(String title, String prompt) {
        TextInputDialog d = new TextInputDialog();
        d.setTitle(title); d.setHeaderText(null); d.setContentText(prompt);
        DialogStyle.apply(d);
        return d;
    }

    private void stateDialog(MerchRow r) {
        List<String> opts = List.of("normal", "suspended", "in_default");
        ChoiceDialog<String> d = new ChoiceDialog<>(r.state.get(), opts);
        d.setTitle("Account state");
        d.setHeaderText("Change state of " + r.name.get());
        d.setContentText("State:");
        DialogStyle.apply(d);
        d.showAndWait().ifPresent(newState -> {
            if ("normal".equals(newState) && "in_default".equalsIgnoreCase(r.state.get())) {
                if (!user.canReactivateDefaultAccount()) {
                    UIUtil.error("Not allowed",
                            "Only the Director of Operations (or Administrator) can reactivate 'in default' accounts."); return;
                }
                controller.reactivateDefaultAccount(r.id);
            } else {
                try (Connection conn = MyJDBC.getConnection()) {
                    AccountSQL.updateMerchantStatus(conn, r.id, newState);
                } catch (Exception ex) { UIUtil.error("Error", ex.getMessage()); }
            }
            reload();
        });
    }

    private void deleteMerchant(MerchRow r) {
        if (r == null) return;
        if (!UIUtil.confirm("Delete merchant",
                "Delete '" + r.name.get() + "'? Their discount tiers will also be removed (cascaded).")) return;
        if (controller.deleteMerchantAccount(r.id)) reload();
        else UIUtil.error("Error", "Could not delete merchant. (Check that related orders are archived.)");
    }

    /**
     * The type Merch row.
     */
    public static class MerchRow {
        /**
         * The Id.
         */
        public final int id;
        /**
         * The Name.
         */
        public final SimpleStringProperty name, /**
         * The Account no.
         */
        accountNo, /**
         * The Discount type.
         */
        discountType, /**
         * The State.
         */
        state;
        /**
         * The Credit limit.
         */
        public final SimpleDoubleProperty creditLimit;

        /**
         * Instantiates a new Merch row.
         *
         * @param id the id
         * @param n  the n
         * @param a  the a
         * @param cl the cl
         * @param dt the dt
         * @param s  the s
         */
        public MerchRow(int id, String n, String a, double cl, String dt, String s) {
            this.id = id;
            this.name = new SimpleStringProperty(n == null ? "" : n);
            this.accountNo = new SimpleStringProperty(a == null ? "" : a);
            this.creditLimit = new SimpleDoubleProperty(cl);
            this.discountType = new SimpleStringProperty(dt);
            this.state = new SimpleStringProperty(s == null ? "normal" : s);
        }

        /**
         * Gets name.
         *
         * @return the name
         */
        public String getName() { return name.get(); }

        /**
         * Gets account no.
         *
         * @return the account no
         */
        public String getAccountNo() { return accountNo.get(); }

        /**
         * Gets credit limit.
         *
         * @return the credit limit
         */
        public double getCreditLimit() { return creditLimit.get(); }

        /**
         * Gets discount type.
         *
         * @return the discount type
         */
        public String getDiscountType() { return discountType.get(); }

        /**
         * Gets state.
         *
         * @return the state
         */
        public String getState() { return state.get(); }
    }

    private static class TierRow {
        /**
         * The Min.
         */
        Double min, /**
         * The Max.
         */
        max;
        /**
         * The Rate.
         */
        double rate;

        /**
         * Instantiates a new Tier row.
         *
         * @param min  the min
         * @param max  the max
         * @param rate the rate
         */
        TierRow(Double min, Double max, double rate) { this.min = min; this.max = max; this.rate = rate; }
    }
}
