package com.prototype.ipossa.ui.pages;

import com.prototype.ipossa.MyJDBC;
import com.prototype.ipossa.systems.ACC.Role;
import com.prototype.ipossa.systems.ACC.UserAccount;
import com.prototype.ipossa.ui.DialogStyle;
import com.prototype.ipossa.ui.Formats;
import com.prototype.ipossa.ui.UIUtil;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;

public class OrdersPage {

    private final UserAccount user;
    private final ObservableList<OrderRow> orders = FXCollections.observableArrayList();
    private TableView<OrderRow> table;
    private CheckBox incompleteOnly;

    private static final String[] STATUSES = {"accepted", "ready to dispatch", "dispatched", "delivered"};

    public OrdersPage(UserAccount user) { this.user = user; }

    public Node build() {
        VBox root = new VBox(14);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().add(UIUtil.h2("Orders"));
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        TextField search = new TextField();
        search.setPromptText("Search by merchant or order ID…");
        search.setPrefWidth(280);

        incompleteOnly = new CheckBox("Incomplete only");

        Button newOrderBtn = new Button("+ New order");
        newOrderBtn.getStyleClass().addAll("button", "button-primary");
        newOrderBtn.setDisable(!user.canManageOrders());
        newOrderBtn.setOnAction(e -> newOrderDialog());

        Button paymentBtn = new Button("Record payment");
        paymentBtn.getStyleClass().add("button");
        paymentBtn.setDisable(!user.canRecordPayments());
        paymentBtn.setOnAction(e -> paymentDialog());

        Button refresh = new Button("↻");
        refresh.getStyleClass().add("button");
        refresh.setOnAction(e -> reload());

        header.getChildren().addAll(sp, incompleteOnly, search, paymentBtn, newOrderBtn, refresh);
        root.getChildren().add(header);

        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);
        root.getChildren().add(table);

        FilteredList<OrderRow> filtered = new FilteredList<>(orders, x -> true);
        Runnable updateFilter = () -> {
            String q = search.getText() == null ? "" : search.getText().toLowerCase().trim();
            boolean inc = incompleteOnly.isSelected();
            filtered.setPredicate(r -> {
                boolean matchQ = q.isEmpty()
                        || String.valueOf(r.orderId.get()).contains(q)
                        || r.merchantName.get().toLowerCase().contains(q);
                boolean matchS = !inc || !"delivered".equalsIgnoreCase(r.status.get());
                return matchQ && matchS;
            });
        };
        search.textProperty().addListener((o, ov, nv) -> updateFilter.run());
        incompleteOnly.selectedProperty().addListener((o, ov, nv) -> updateFilter.run());
        table.setItems(filtered);

        reload();
        return root;
    }

    private TableView<OrderRow> buildTable() {
        TableView<OrderRow> t = new TableView<>();
        t.setPlaceholder(UIUtil.dim("No orders to show."));

        TableColumn<OrderRow, String> id = strCol("Order #", "orderId", 80);
        TableColumn<OrderRow, String> date = strCol("Date / Time", "date", 150);
        TableColumn<OrderRow, String> merch = strCol("Merchant", "merchantName", 200);
        TableColumn<OrderRow, Number> total = numCol("Total", "total", 100);
        total.setCellFactory(Formats.moneyCell());

        TableColumn<OrderRow, String> status = new TableColumn<>("Status");
        status.setCellValueFactory(new PropertyValueFactory<>("status"));
        status.setPrefWidth(140);
        status.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); setText(null); return; }
                Label l = new Label(s);
                l.getStyleClass().add("badge");
                l.getStyleClass().add(switch (s.toLowerCase()) {
                    case "delivered" -> "badge-normal";
                    case "dispatched" -> "badge-suspended";
                    default -> "badge-default";
                });
                setGraphic(l); setText(null);
            }
        });

        TableColumn<OrderRow, String> inv = strCol("Invoice", "invoiceId", 110);

        TableColumn<OrderRow, Void> actions = new TableColumn<>("Actions");
        actions.setPrefWidth(560);
        actions.setCellFactory(c -> new TableCell<>() {
            final Button view = new Button("View");
            final ComboBox<String> statusBox = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(
                    "accepted", "ready to dispatch", "dispatched", "delivered", "archived"));
            final Button invoice = new Button("Invoice");
            final Button delete = new Button("Delete");
            final HBox box = new HBox(6, view, statusBox, invoice, delete);
            {
                view.getStyleClass().add("button");
                invoice.getStyleClass().addAll("button", "button-primary");
                delete.getStyleClass().addAll("button", "button-danger");
                statusBox.setPrefWidth(170);
                view.setOnAction(e -> viewOrder(getTableRow().getItem()));
                invoice.setOnAction(e -> generateInvoice(getTableRow().getItem()));
                delete.setOnAction(e -> deleteOrder(getTableRow().getItem()));
                statusBox.setOnAction(e -> {
                    OrderRow r = getTableRow() == null ? null : getTableRow().getItem();
                    if (r == null || statusBox.getValue() == null) return;
                    if (!statusBox.getValue().equalsIgnoreCase(r.status.get())) {
                        updateStatus(r, statusBox.getValue());
                    }
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                OrderRow r = getTableRow() == null ? null : getTableRow().getItem();
                if (empty || r == null) { setGraphic(null); return; }
                boolean canStatus = user.getRole() == com.prototype.ipossa.systems.ACC.Role.ADMINISTRATOR
                        || user.getRole() == com.prototype.ipossa.systems.ACC.Role.DIRECTOR_OF_OPERATIONS
                        || user.getRole() == com.prototype.ipossa.systems.ACC.Role.DELIVERY_EMPLOYEE;
                statusBox.setValue(r.status.get());
                statusBox.setDisable(!canStatus);

                invoice.setDisable(!user.canGenerateInvoice());
                delete.setDisable(!user.canManageUserAccounts());
                setGraphic(box);
            }
        });

        t.getColumns().add(id);
        t.getColumns().add(date);
        t.getColumns().add(merch);
        t.getColumns().add(total);
        t.getColumns().add(status);
        t.getColumns().add(inv);
        t.getColumns().add(actions);
        return t;
    }

    private TableColumn<OrderRow, String> strCol(String title, String prop, double w) {
        TableColumn<OrderRow, String> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(prop)); c.setPrefWidth(w); return c;
    }
    private TableColumn<OrderRow, Number> numCol(String title, String prop, double w) {
        TableColumn<OrderRow, Number> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(prop)); c.setPrefWidth(w); return c;
    }

    private void reload() {
        orders.clear();
        try (Connection conn = MyJDBC.getConnection()) {
            ResultSet rs = conn.prepareStatement("""
                SELECT o.order_ID, o.order_date, o.created_at, o.status, o.total_amount, o.invoice_ID,
                       m.account_holder_name, o.merchant_ID
                FROM orders o LEFT JOIN merchants m ON m.merchant_ID = o.merchant_ID
                ORDER BY COALESCE(o.created_at, o.order_date) DESC, o.order_ID DESC
                """).executeQuery();
            while (rs.next()) {
                String when;
                java.sql.Timestamp ts = null;
                try { ts = rs.getTimestamp("created_at"); } catch (Exception ignored) {}
                if (ts != null) {
                    when = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(ts);
                } else {
                    when = rs.getDate("order_date") == null ? "" : rs.getDate("order_date").toString();
                }
                orders.add(new OrderRow(
                        rs.getString("order_ID"),
                        rs.getString("merchant_ID"),
                        rs.getString("account_holder_name") == null ? "(unknown)" : rs.getString("account_holder_name"),
                        when,
                        rs.getString("status"),
                        rs.getDouble("total_amount"),
                        rs.getString("invoice_ID")
                ));
            }
        } catch (Exception e) {
            UIUtil.error("Database error", "Could not load orders:\n" + e.getMessage());
        }
    }

    private void viewOrder(OrderRow r) {
        if (r == null) return;
        Dialog<Void> d = new Dialog<>();
        d.setTitle("Order #" + r.orderId.get());
        d.setHeaderText(r.merchantName.get() + " — " + r.date.get());

        VBox box = new VBox(8);
        TableView<ItemRow> t = new TableView<>();
        t.setPrefHeight(240);
        TableColumn<ItemRow, String> c1 = new TableColumn<>("Item ID");
        c1.setCellValueFactory(new PropertyValueFactory<>("itemId")); c1.setPrefWidth(120);
        TableColumn<ItemRow, String> c2 = new TableColumn<>("Description");
        c2.setCellValueFactory(new PropertyValueFactory<>("description")); c2.setPrefWidth(220);
        TableColumn<ItemRow, Number> c3 = new TableColumn<>("Qty");
        c3.setCellValueFactory(new PropertyValueFactory<>("quantity")); c3.setPrefWidth(70);
        TableColumn<ItemRow, Number> c4 = new TableColumn<>("Unit");
        c4.setCellValueFactory(new PropertyValueFactory<>("unitCost")); c4.setPrefWidth(90);
        c4.setCellFactory(Formats.moneyCell());
        TableColumn<ItemRow, Number> c5 = new TableColumn<>("Line");
        c5.setCellValueFactory(cd -> new SimpleDoubleProperty(
                cd.getValue().quantity.get() * cd.getValue().unitCost.get())); c5.setPrefWidth(100);
        c5.setCellFactory(Formats.moneyCell());
        t.getColumns().add(c1);
        t.getColumns().add(c2);
        t.getColumns().add(c3);
        t.getColumns().add(c4);
        t.getColumns().add(c5);

        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement st = conn.prepareStatement(
                     "SELECT * FROM order_items WHERE order_ID=?")) {
            st.setString(1, r.orderId.get());
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                t.getItems().add(new ItemRow(
                        rs.getString("item_ID"), rs.getString("description"),
                        rs.getInt("quantity"), rs.getDouble("unit_cost")));
            }
        } catch (Exception e) { UIUtil.error("Error", e.getMessage()); }

        Label totals = new Label(String.format("Total: £%.2f   •   Status: %s   •   Invoice: %s",
                r.total.get(), r.status.get(),
                r.invoiceId.get() == null ? "—" : r.invoiceId.get()));
        totals.getStyleClass().add("h2");

        box.getChildren().addAll(t, totals);
        d.getDialogPane().setContent(box);
        d.getDialogPane().setPrefWidth(700);
        d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        DialogStyle.apply(d);
        d.showAndWait();
    }

    private void advanceStatus(OrderRow r) {
        if (r == null) return;
        int idx = indexOfStatus(r.status.get());
        if (idx < 0 || idx >= STATUSES.length - 1) {
            UIUtil.info("Order", "Order is already delivered.");
            return;
        }
        String next = STATUSES[idx + 1];
        updateStatus(r, next);
    }

    private void markDelivered(OrderRow r) {
        if (r == null) return;
        updateStatus(r, "delivered");
    }

    private void archiveOrder(OrderRow r) {
        if (r == null) return;
        if (!UIUtil.confirm("Archive order",
                "Archive order #" + r.orderId.get() + "? Archived orders are excluded from active operations.")) return;
        updateStatus(r, "archived");
    }

    private void deleteOrder(OrderRow r) {
        if (r == null) return;
        if (!UIUtil.confirm("Delete order",
                "Permanently delete order #" + r.orderId.get() + " and all its line items? This cannot be undone.")) return;
        try (Connection conn = MyJDBC.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement st = conn.prepareStatement("DELETE FROM order_items WHERE order_ID=?")) {
                st.setString(1, r.orderId.get()); st.executeUpdate();
            }
            try (PreparedStatement st = conn.prepareStatement("DELETE FROM orders WHERE order_ID=?")) {
                st.setString(1, r.orderId.get()); st.executeUpdate();
            }
            conn.commit();
            reload();
        } catch (Exception e) { UIUtil.error("Error", e.getMessage()); }
    }

    private void updateStatus(OrderRow r, String newStatus) {
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement st = conn.prepareStatement(
                     "UPDATE orders SET status=? WHERE order_ID=?")) {
            st.setString(1, newStatus); st.setString(2, r.orderId.get()); st.executeUpdate();
            reload();
        } catch (Exception e) { UIUtil.error("Error", e.getMessage()); }
    }

    private int indexOfStatus(String s) {
        if (s == null) return -1;
        for (int i = 0; i < STATUSES.length; i++)
            if (STATUSES[i].equalsIgnoreCase(s)) return i;
        return -1;
    }

    private void generateInvoice(OrderRow r) {
        if (r == null) return;

        String invId = (r.invoiceId.get() == null || r.invoiceId.get().isBlank())
                ? "INV-" + (System.currentTimeMillis() % 10_000_000) + "-" + r.orderId.get()
                : r.invoiceId.get();

        java.util.List<com.prototype.ipossa.ui.InvoicePdfWriter.LineItem> items = new java.util.ArrayList<>();
        String address = "";
        try (Connection conn = MyJDBC.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT address FROM merchants WHERE merchant_ID=?")) {
                ps.setString(1, r.merchantId.get());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) address = rs.getString(1);
            } catch (Exception ignored) {}
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT item_ID, description, quantity, unit_cost FROM order_items WHERE order_ID=?")) {
                ps.setString(1, r.orderId.get());
                ResultSet rs = ps.executeQuery();
                while (rs.next())
                    items.add(new com.prototype.ipossa.ui.InvoicePdfWriter.LineItem(
                            rs.getString("item_ID"), rs.getString("description"),
                            rs.getInt("quantity"), rs.getDouble("unit_cost")));
            }
        } catch (Exception e) { UIUtil.error("Error loading order", e.getMessage()); return; }

        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Save invoice PDF");
        fc.setInitialFileName(invId + ".pdf");
        fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("PDF", "*.pdf"));
        java.io.File f = fc.showSaveDialog(null);
        if (f == null) return;

        try {
            com.prototype.ipossa.ui.InvoicePdfWriter.write(
                    f, invId, r.orderId.get(), r.date.get(),
                    r.merchantName.get(), address, items, r.total.get());
        } catch (Exception e) { UIUtil.error("PDF error", e.getMessage()); return; }

        if (r.invoiceId.get() == null || r.invoiceId.get().isBlank()) {
            try (Connection conn = MyJDBC.getConnection();
                 PreparedStatement st = conn.prepareStatement(
                         "UPDATE orders SET invoice_ID=? WHERE order_ID=?")) {
                st.setString(1, invId); st.setString(2, r.orderId.get()); st.executeUpdate();
            } catch (Exception ignored) {}
        }
        UIUtil.info("Invoice saved",
                "Invoice " + invId + " written to:\n" + f.getAbsolutePath());
        reload();
    }

    private void newOrderDialog() {
        Dialog<Void> d = new Dialog<>();
        d.setTitle("New order");
        d.setHeaderText("Create a new order for a merchant");

        VBox box = new VBox(10); box.setPadding(new Insets(4));

        ComboBox<MerchantRef> merchants = new ComboBox<>();
        try (Connection conn = MyJDBC.getConnection()) {
            ResultSet rs = conn.prepareStatement(
                    "SELECT merchant_ID, account_holder_name FROM merchants ORDER BY account_holder_name"
            ).executeQuery();
            while (rs.next())
                merchants.getItems().add(new MerchantRef(rs.getInt(1), rs.getString(2)));
        } catch (Exception e) { UIUtil.error("Error", e.getMessage()); return; }

        DatePicker date = new DatePicker(LocalDate.now());

        TableView<CartRow> cart = new TableView<>();
        cart.setPrefHeight(220);
        cart.setPlaceholder(UIUtil.dim("Add items from the catalogue below."));
        TableColumn<CartRow, String> ccId = new TableColumn<>("Item ID");
        ccId.setCellValueFactory(new PropertyValueFactory<>("itemId")); ccId.setPrefWidth(110);
        TableColumn<CartRow, String> ccDesc = new TableColumn<>("Description");
        ccDesc.setCellValueFactory(new PropertyValueFactory<>("description")); ccDesc.setPrefWidth(200);
        TableColumn<CartRow, Number> ccQty = new TableColumn<>("Qty");
        ccQty.setCellValueFactory(new PropertyValueFactory<>("quantity")); ccQty.setPrefWidth(70);
        TableColumn<CartRow, Number> ccCost = new TableColumn<>("Unit");
        ccCost.setCellValueFactory(new PropertyValueFactory<>("unitCost")); ccCost.setPrefWidth(80);
        ccCost.setCellFactory(Formats.moneyCell());
        TableColumn<CartRow, Number> ccLine = new TableColumn<>("Line");
        ccLine.setCellValueFactory(cd -> new SimpleDoubleProperty(
                cd.getValue().quantity.get() * cd.getValue().unitCost.get())); ccLine.setPrefWidth(90);
        ccLine.setCellFactory(Formats.moneyCell());
        cart.getColumns().add(ccId);
        cart.getColumns().add(ccDesc);
        cart.getColumns().add(ccQty);
        cart.getColumns().add(ccCost);
        cart.getColumns().add(ccLine);

        HBox addRow = new HBox(6);
        ComboBox<CatRef> catalogue = new ComboBox<>();
        catalogue.setPrefWidth(280);
        try (Connection conn = MyJDBC.getConnection()) {
            ResultSet rs = conn.prepareStatement(
                    "SELECT item_ID, description, package_cost FROM catalogue ORDER BY description"
            ).executeQuery();
            while (rs.next())
                catalogue.getItems().add(new CatRef(rs.getString(1), rs.getString(2), rs.getDouble(3)));
        } catch (Exception e) { UIUtil.error("Error", e.getMessage()); return; }

        TextField qty = new TextField("1"); qty.setPrefWidth(80);
        Button addItem = new Button("Add item");
        addItem.getStyleClass().addAll("button", "button-primary");
        Button removeItem = new Button("Remove selected");
        removeItem.getStyleClass().add("button");

        Label totalLbl = new Label("Total: £0.00");
        totalLbl.getStyleClass().add("h2");

        addItem.setOnAction(e -> {
            CatRef sel = catalogue.getValue();
            if (sel == null) return;
            try {
                int q = Integer.parseInt(qty.getText().trim());
                if (q <= 0) return;
                cart.getItems().add(new CartRow(sel.id, sel.description, q, sel.cost));
                updateTotal(cart, totalLbl);
            } catch (Exception ex) { UIUtil.warn("Invalid qty", ex.getMessage()); }
        });
        removeItem.setOnAction(e -> {
            CartRow sel = cart.getSelectionModel().getSelectedItem();
            if (sel != null) { cart.getItems().remove(sel); updateTotal(cart, totalLbl); }
        });

        addRow.getChildren().addAll(new Label("Item:"), catalogue, new Label("Qty:"), qty, addItem, removeItem);

        box.getChildren().addAll(
                new Label("Merchant:"), merchants,
                new Label("Order date:"), date,
                new Label("Items:"), cart, addRow, totalLbl);

        d.getDialogPane().setContent(box);
        d.getDialogPane().setPrefWidth(700);
        ButtonType createBtn = new ButtonType("Create order", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(createBtn, ButtonType.CANCEL);
        DialogStyle.apply(d);

        d.setResultConverter(b -> {
            if (b != createBtn) return null;
            if (merchants.getValue() == null) { UIUtil.warn("Missing", "Select a merchant."); return null; }
            if (cart.getItems().isEmpty()) { UIUtil.warn("Missing", "Add at least one item."); return null; }
            saveOrder(merchants.getValue(), date.getValue(), cart.getItems());
            return null;
        });
        d.showAndWait();
    }

    private void updateTotal(TableView<CartRow> cart, Label lbl) {
        double total = cart.getItems().stream()
                .mapToDouble(r -> r.quantity.get() * r.unitCost.get()).sum();
        lbl.setText(String.format("Total: £%.2f", total));
    }

    private void saveOrder(MerchantRef m, LocalDate date, java.util.List<CartRow> items) {
        double total = items.stream().mapToDouble(r -> r.quantity.get() * r.unitCost.get()).sum();
        try (Connection conn = MyJDBC.getConnection()) {
            conn.setAutoCommit(false);

            long orderId;
            try (PreparedStatement st = conn.prepareStatement(
                    "INSERT INTO orders (merchant_ID, order_date, status, subtotal, discount, total_amount) " +
                    "VALUES (?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                st.setInt(1, m.id);
                st.setDate(2, java.sql.Date.valueOf(date));
                st.setString(3, "accepted");
                st.setDouble(4, total);
                st.setDouble(5, 0);
                st.setDouble(6, total);
                st.executeUpdate();
                ResultSet keys = st.getGeneratedKeys();
                if (!keys.next()) throw new RuntimeException("Could not obtain new order ID");
                orderId = keys.getLong(1);
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO order_items (order_ID, item_ID, description, quantity, unit_cost, unit_price, subtotal, line_total, total) VALUES (?,?,?,?,?,?,?,?,?)")) {
                for (CartRow r : items) {
                    ps.setLong(1, orderId); ps.setString(2, r.itemId.get());
                    ps.setString(3, r.description.get()); ps.setInt(4, r.quantity.get());
                    ps.setDouble(5, r.unitCost.get());
                    ps.setDouble(6, r.unitCost.get());
                    double __line = r.unitCost.get() * r.quantity.get();
                    ps.setDouble(7, __line);
                    ps.setDouble(8, __line);
                    ps.setDouble(9, __line);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE catalogue SET availability = availability - ? WHERE item_ID = ?")) {
                for (CartRow r : items) {
                    ps.setInt(1, r.quantity.get()); ps.setString(2, r.itemId.get()); ps.addBatch();
                }
                ps.executeBatch();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE merchants SET last_payment_due=? WHERE merchant_ID=? AND last_payment_due IS NULL")) {
                ps.setDate(1, java.sql.Date.valueOf(endOfNextMonth(date)));
                ps.setInt(2, m.id);
                ps.executeUpdate();
            }
            conn.commit();

            com.prototype.ipossa.ui.MerchantStateUpdater.refreshOne(m.id);
            UIUtil.info("Order created", "Order #" + orderId + " created for " + m.name + ".");
            reload();
        } catch (Exception e) { UIUtil.error("Error", e.getMessage()); }
    }

    private static LocalDate endOfNextMonth(LocalDate orderDate) {

        LocalDate firstOfNextMonth = orderDate.plusMonths(1).withDayOfMonth(1);
        return firstOfNextMonth.withDayOfMonth(firstOfNextMonth.lengthOfMonth());
    }

    private void paymentDialog() {
        Dialog<Void> d = new Dialog<>();
        d.setTitle("Record payment");
        d.setHeaderText("Record a payment received from a merchant");

        VBox box = new VBox(10); box.setPadding(new Insets(4));

        ComboBox<MerchantRef> merchants = new ComboBox<>();
        try (Connection conn = MyJDBC.getConnection()) {
            ResultSet rs = conn.prepareStatement(
                    "SELECT merchant_ID, account_holder_name FROM merchants ORDER BY account_holder_name"
            ).executeQuery();
            while (rs.next())
                merchants.getItems().add(new MerchantRef(rs.getInt(1), rs.getString(2)));
        } catch (Exception e) { UIUtil.error("Error", e.getMessage()); return; }

        TextField amount = new TextField();
        amount.setPromptText("0.00");
        ComboBox<String> method = new ComboBox<>(
                FXCollections.observableArrayList("Bank transfer", "Credit card", "Debit card", "Cash", "Cheque"));
        method.setValue("Bank transfer");
        DatePicker date = new DatePicker(LocalDate.now());
        TextField notes = new TextField();

        Label balance = new Label("Select a merchant to see balance.");
        balance.getStyleClass().add("dim");
        merchants.valueProperty().addListener((o, ov, nv) -> {
            if (nv == null) return;
            balance.setText(String.format("Outstanding balance: £%.2f", outstandingBalance(nv.id)));
        });

        box.getChildren().addAll(
                new Label("Merchant:"), merchants, balance,
                new Label("Amount:"), amount,
                new Label("Method:"), method,
                new Label("Date:"), date,
                new Label("Notes:"), notes);

        d.getDialogPane().setContent(box);
        d.getDialogPane().setPrefWidth(450);
        ButtonType rec = new ButtonType("Record", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(rec, ButtonType.CANCEL);
        DialogStyle.apply(d);

        d.setResultConverter(b -> {
            if (b != rec) return null;
            if (merchants.getValue() == null) return null;
            try {
                double amt = Double.parseDouble(amount.getText().trim());
                savePayment(merchants.getValue().id, amt, method.getValue(), date.getValue(), notes.getText());
            } catch (Exception ex) { UIUtil.error("Invalid amount", ex.getMessage()); }
            return null;
        });
        d.showAndWait();
    }

    private double outstandingBalance(int merchantId) {
        double orders = 0, paid = 0;
        try (Connection conn = MyJDBC.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COALESCE(SUM(total_amount),0) FROM orders WHERE merchant_ID=?")) {
                ps.setInt(1, merchantId);
                ResultSet rs = ps.executeQuery(); if (rs.next()) orders = rs.getDouble(1);
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COALESCE(SUM(amount),0) FROM payments WHERE merchant_ID=?")) {
                ps.setInt(1, merchantId);
                ResultSet rs = ps.executeQuery(); if (rs.next()) paid = rs.getDouble(1);
            }
        } catch (Exception e) {  }
        return orders - paid;
    }

    private void savePayment(int merchantId, double amount, String method, LocalDate date, String notes) {
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO payments (merchant_ID, amount, payment_date, method, payment_method, notes) " +
                     "VALUES (?,?,?,?,?,?)")) {
            ps.setInt(1, merchantId); ps.setDouble(2, amount);
            ps.setDate(3, java.sql.Date.valueOf(date));
            ps.setString(4, method);
            ps.setString(5, method);
            ps.setString(6, notes);
            ps.executeUpdate();

            double bal = outstandingBalance(merchantId);

            if (bal <= 0) {
                try (PreparedStatement up = conn.prepareStatement(
                        "UPDATE merchants SET account_state='normal' WHERE merchant_ID=? AND account_state='suspended'")) {
                    up.setInt(1, merchantId); up.executeUpdate();
                }

                try (PreparedStatement up = conn.prepareStatement(
                        "UPDATE merchants SET last_payment_due=NULL WHERE merchant_ID=?")) {
                    up.setInt(1, merchantId); up.executeUpdate();
                }
            }

            com.prototype.ipossa.ui.MerchantStateUpdater.refreshOne(merchantId);
            UIUtil.info("Payment recorded",
                    String.format("Payment of £%.2f recorded. New balance: £%.2f", amount, bal));
        } catch (Exception e) { UIUtil.error("Error", e.getMessage()); }
    }

    public static class OrderRow {
        public final SimpleStringProperty orderId, merchantId;
        public final SimpleStringProperty merchantName, date, status, invoiceId;
        public final SimpleDoubleProperty total;
        public OrderRow(String oid, String mid, String mname, String date, String status, double total, String invId) {
            this.orderId = new SimpleStringProperty(oid);
            this.merchantId = new SimpleStringProperty(mid);
            this.merchantName = new SimpleStringProperty(mname);
            this.date = new SimpleStringProperty(date);
            this.status = new SimpleStringProperty(status);
            this.total = new SimpleDoubleProperty(total);
            this.invoiceId = new SimpleStringProperty(invId);
        }
        public String getOrderId() { return orderId.get(); }
        public String getMerchantId() { return merchantId.get(); }
        public String getMerchantName() { return merchantName.get(); }
        public String getDate() { return date.get(); }
        public String getStatus() { return status.get(); }
        public double getTotal() { return total.get(); }
        public String getInvoiceId() { return invoiceId.get(); }
    }

    public static class ItemRow {
        public final SimpleStringProperty itemId, description;
        public final SimpleIntegerProperty quantity;
        public final SimpleDoubleProperty unitCost;
        public ItemRow(String id, String desc, int q, double c) {
            this.itemId = new SimpleStringProperty(id);
            this.description = new SimpleStringProperty(desc);
            this.quantity = new SimpleIntegerProperty(q);
            this.unitCost = new SimpleDoubleProperty(c);
        }
        public String getItemId() { return itemId.get(); }
        public String getDescription() { return description.get(); }
        public int getQuantity() { return quantity.get(); }
        public double getUnitCost() { return unitCost.get(); }
    }

    public static class CartRow extends ItemRow {
        public CartRow(String id, String d, int q, double c) { super(id, d, q, c); }
    }

    private record MerchantRef(int id, String name) {
        @Override public String toString() { return name; }
    }
    private record CatRef(String id, String description, double cost) {
        @Override public String toString() { return description + String.format("  (£%.2f)", cost); }
    }
}
