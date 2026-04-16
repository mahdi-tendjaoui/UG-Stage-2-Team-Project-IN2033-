package com.prototype.ipossa.ui.pages;

import com.prototype.ipossa.MyJDBC;
import com.prototype.ipossa.systems.ACC.MerchantAccount;
import com.prototype.ipossa.ui.DialogStyle;
import com.prototype.ipossa.ui.UIUtil;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;

public class MerchantOrdersPage {

    private final MerchantAccount merchant;
    private final ObservableList<Row> data = FXCollections.observableArrayList();
    private TableView<Row> table;

    public MerchantOrdersPage(MerchantAccount m) { this.merchant = m; }

    public Node build() {
        VBox root = new VBox(14);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().add(UIUtil.h2("My orders"));
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        Button place = new Button("+ Place new order");
        place.getStyleClass().addAll("button", "button-primary");
        place.setDisable(merchant.getAccountState() != MerchantAccount.AccountState.NORMAL);
        if (merchant.getAccountState() != MerchantAccount.AccountState.NORMAL) {
            place.setTooltip(new Tooltip("Account is " + merchant.getAccountState().getDbValue()
                    + " — clear outstanding balance to place new orders."));
        }
        place.setOnAction(e -> placeOrderDialog());

        Button refresh = new Button("↻");
        refresh.getStyleClass().add("button");
        refresh.setOnAction(e -> reload());

        header.getChildren().addAll(sp, place, refresh);
        root.getChildren().add(header);

        table = new TableView<>();
        table.setPlaceholder(UIUtil.dim("You haven't placed any orders yet."));
        TableColumn<Row, String> id = strCol("Order #", "orderId", 90);
        TableColumn<Row, String> date = strCol("Date", "date", 120);
        TableColumn<Row, Number> amt = numCol("Total £", "total", 100);
        amt.setCellFactory(com.prototype.ipossa.ui.Formats.moneyCell());

        TableColumn<Row, String> status = new TableColumn<>("Status");
        status.setCellValueFactory(new PropertyValueFactory<>("status"));
        status.setPrefWidth(140);
        status.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); setText(null); return; }
                Label l = new Label(s); l.getStyleClass().add("badge");
                l.getStyleClass().add(switch (s.toLowerCase()) {
                    case "delivered" -> "badge-normal";
                    case "dispatched" -> "badge-suspended";
                    default -> "badge-default";
                });
                setGraphic(l); setText(null);
            }
        });

        TableColumn<Row, String> inv = strCol("Invoice", "invoiceId", 130);

        TableColumn<Row, Void> actions = new TableColumn<>("Actions");
        actions.setPrefWidth(100);
        actions.setCellFactory(c -> new TableCell<>() {
            final Button view = new Button("View");
            { view.getStyleClass().add("button");
              view.setOnAction(e -> viewOrder(getTableRow().getItem())); }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                Row r = getTableRow() == null ? null : getTableRow().getItem();
                if (empty || r == null) setGraphic(null); else setGraphic(view);
            }
        });

        table.getColumns().add(id);
        table.getColumns().add(date);
        table.getColumns().add(amt);
        table.getColumns().add(status);
        table.getColumns().add(inv);
        table.getColumns().add(actions);

        table.setItems(data);
        VBox.setVgrow(table, Priority.ALWAYS);
        root.getChildren().add(table);

        reload();
        return root;
    }

    private TableColumn<Row, String> strCol(String t, String p, double w) {
        TableColumn<Row, String> c = new TableColumn<>(t);
        c.setCellValueFactory(new PropertyValueFactory<>(p)); c.setPrefWidth(w); return c;
    }
    private TableColumn<Row, Number> numCol(String t, String p, double w) {
        TableColumn<Row, Number> c = new TableColumn<>(t);
        c.setCellValueFactory(new PropertyValueFactory<>(p)); c.setPrefWidth(w); return c;
    }

    private void reload() {
        data.clear();
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement st = conn.prepareStatement("""
                SELECT order_ID, order_date, status, total_amount, invoice_ID
                FROM orders WHERE merchant_ID=? ORDER BY order_date DESC, order_ID DESC
                """)) {
            st.setInt(1, merchant.getMerchantID());
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                data.add(new Row(
                        rs.getString("order_ID"),
                        rs.getDate("order_date") == null ? "" : rs.getDate("order_date").toString(),
                        rs.getDouble("total_amount"),
                        rs.getString("status"),
                        rs.getString("invoice_ID")));
            }
        } catch (Exception e) { UIUtil.error("Error", e.getMessage()); }
    }

    private void viewOrder(Row r) {
        if (r == null) return;
        Dialog<Void> d = new Dialog<>();
        d.setTitle("Order #" + r.orderId.get());
        d.setHeaderText(merchant.getAccountHolderName() + " — " + r.date.get());

        TableView<ItemRow> t = new TableView<>();
        t.setPrefHeight(260);
        TableColumn<ItemRow, String> c1 = new TableColumn<>("Item ID");
        c1.setCellValueFactory(new PropertyValueFactory<>("itemId")); c1.setPrefWidth(120);
        TableColumn<ItemRow, String> c2 = new TableColumn<>("Description");
        c2.setCellValueFactory(new PropertyValueFactory<>("description")); c2.setPrefWidth(220);
        TableColumn<ItemRow, Number> c3 = new TableColumn<>("Qty");
        c3.setCellValueFactory(new PropertyValueFactory<>("quantity")); c3.setPrefWidth(60);
        TableColumn<ItemRow, Number> c4 = new TableColumn<>("Unit £");
        c4.setCellValueFactory(new PropertyValueFactory<>("unitCost")); c4.setPrefWidth(80);
        c4.setCellFactory(com.prototype.ipossa.ui.Formats.moneyCell());
        TableColumn<ItemRow, Number> c5 = new TableColumn<>("Line £");
        c5.setCellValueFactory(cd -> new SimpleDoubleProperty(
                cd.getValue().quantity.get() * cd.getValue().unitCost.get())); c5.setPrefWidth(90);
        c5.setCellFactory(com.prototype.ipossa.ui.Formats.moneyCell());
        t.getColumns().add(c1); t.getColumns().add(c2); t.getColumns().add(c3);
        t.getColumns().add(c4); t.getColumns().add(c5);

        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement st = conn.prepareStatement("SELECT * FROM order_items WHERE order_ID=?")) {
            st.setString(1, r.orderId.get());
            ResultSet rs = st.executeQuery();
            while (rs.next())
                t.getItems().add(new ItemRow(
                        rs.getString("item_ID"), rs.getString("description"),
                        rs.getInt("quantity"), rs.getDouble("unit_cost")));
        } catch (Exception e) { UIUtil.error("Error", e.getMessage()); }

        Label totals = new Label(String.format("Total £%.2f   ·   Status: %s   ·   Invoice: %s",
                r.total.get(), r.status.get(),
                r.invoiceId.get() == null ? "—" : r.invoiceId.get()));
        totals.getStyleClass().add("h2");

        VBox box = new VBox(8, t, totals);
        d.getDialogPane().setContent(box);
        d.getDialogPane().setPrefWidth(680);
        d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        DialogStyle.apply(d);
        d.showAndWait();
    }

    private void placeOrderDialog() {
        Dialog<Void> d = new Dialog<>();
        d.setTitle("Place new order");
        d.setHeaderText("Add items from the catalogue and confirm.");

        VBox box = new VBox(10); box.setPadding(new Insets(4));
        DatePicker date = new DatePicker(LocalDate.now());

        TableView<CartRow> cart = new TableView<>();
        cart.setPrefHeight(240);
        cart.setPlaceholder(UIUtil.dim("No items added yet."));

        TableColumn<CartRow, String> c1 = new TableColumn<>("Item ID");
        c1.setCellValueFactory(new PropertyValueFactory<>("itemId")); c1.setPrefWidth(120);
        TableColumn<CartRow, String> c2 = new TableColumn<>("Description");
        c2.setCellValueFactory(new PropertyValueFactory<>("description")); c2.setPrefWidth(200);
        TableColumn<CartRow, Number> c3 = new TableColumn<>("Qty");
        c3.setCellValueFactory(new PropertyValueFactory<>("quantity")); c3.setPrefWidth(60);
        TableColumn<CartRow, Number> c4 = new TableColumn<>("Unit £");
        c4.setCellValueFactory(new PropertyValueFactory<>("unitCost")); c4.setPrefWidth(80);
        c4.setCellFactory(com.prototype.ipossa.ui.Formats.moneyCell());
        TableColumn<CartRow, Number> c5 = new TableColumn<>("Line £");
        c5.setCellValueFactory(cd -> new SimpleDoubleProperty(
                cd.getValue().quantity.get() * cd.getValue().unitCost.get())); c5.setPrefWidth(90);
        c5.setCellFactory(com.prototype.ipossa.ui.Formats.moneyCell());
        cart.getColumns().add(c1); cart.getColumns().add(c2); cart.getColumns().add(c3);
        cart.getColumns().add(c4); cart.getColumns().add(c5);

        ComboBox<CatRef> picker = new ComboBox<>();
        picker.setPrefWidth(280);
        try (Connection conn = MyJDBC.getConnection()) {
            ResultSet rs = conn.prepareStatement(
                    "SELECT item_ID, description, package_cost, availability FROM catalogue ORDER BY description"
            ).executeQuery();
            while (rs.next())
                picker.getItems().add(new CatRef(rs.getString(1), rs.getString(2),
                        rs.getDouble(3), rs.getInt(4)));
        } catch (Exception e) { UIUtil.error("Error", e.getMessage()); return; }

        TextField qty = new TextField("1"); qty.setPrefWidth(70);
        Button addBtn = new Button("Add");
        addBtn.getStyleClass().addAll("button", "button-primary");
        Button rmBtn = new Button("Remove");
        rmBtn.getStyleClass().add("button");

        Label total = new Label("Total £0.00");
        total.getStyleClass().add("h2");

        Runnable updateTotal = () -> total.setText(String.format("Total £%.2f",
                cart.getItems().stream().mapToDouble(r -> r.quantity.get() * r.unitCost.get()).sum()));

        addBtn.setOnAction(e -> {
            CatRef sel = picker.getValue();
            if (sel == null) return;
            try {
                int q = Integer.parseInt(qty.getText().trim());
                if (q <= 0) return;
                if (q > sel.availability) {
                    UIUtil.warn("Stock", "Only " + sel.availability + " in stock."); return;
                }
                cart.getItems().add(new CartRow(sel.id, sel.description, q, sel.cost));
                updateTotal.run();
            } catch (Exception ex) { UIUtil.warn("Invalid", ex.getMessage()); }
        });
        rmBtn.setOnAction(e -> {
            CartRow s = cart.getSelectionModel().getSelectedItem();
            if (s != null) { cart.getItems().remove(s); updateTotal.run(); }
        });

        HBox add = new HBox(8, new Label("Item:"), picker, new Label("Qty:"), qty, addBtn, rmBtn);
        add.setAlignment(Pos.CENTER_LEFT);
        box.getChildren().addAll(new Label("Order date:"), date, new Label("Items:"), cart, add, total);

        d.getDialogPane().setContent(box);
        d.getDialogPane().setPrefWidth(700);
        ButtonType ok = new ButtonType("Place order", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
        DialogStyle.apply(d);

        d.setResultConverter(b -> {
            if (b != ok) return null;
            if (cart.getItems().isEmpty()) { UIUtil.warn("Empty", "Add at least one item."); return null; }
            saveOrder(date.getValue(), cart.getItems());
            return null;
        });
        d.showAndWait();
    }

    private void saveOrder(LocalDate date, List<CartRow> items) {
        double total = items.stream().mapToDouble(r -> r.quantity.get() * r.unitCost.get()).sum();
        // credit-limit check
        double bal = 0;
        try (Connection conn = MyJDBC.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COALESCE(SUM(total_amount),0)-COALESCE((SELECT SUM(amount) FROM payments WHERE merchant_ID=?),0) FROM orders WHERE merchant_ID=?")) {
                ps.setInt(1, merchant.getMerchantID()); ps.setInt(2, merchant.getMerchantID());
                ResultSet rs = ps.executeQuery(); if (rs.next()) bal = rs.getDouble(1);
            }
        } catch (Exception ignored) {}
        if (bal + total > merchant.getCreditLimit()) {
            UIUtil.error("Credit limit exceeded",
                    String.format("This order (£%.2f) plus current balance (£%.2f) would exceed your credit limit of £%.2f.",
                            total, bal, merchant.getCreditLimit()));
            return;
        }
        try (Connection conn = MyJDBC.getConnection()) {
            conn.setAutoCommit(false);
            long nextId = 1;
            try (PreparedStatement ms = conn.prepareStatement(
                    "SELECT COALESCE(MAX(CAST(order_ID AS UNSIGNED)),0)+1 FROM orders")) {
                ResultSet mrs = ms.executeQuery(); if (mrs.next()) nextId = mrs.getLong(1);
            }
            String orderId = String.valueOf(nextId);
            try (PreparedStatement st = conn.prepareStatement(
                    "INSERT INTO orders (order_ID, merchant_ID, order_date, status, subtotal, discount, total_amount) VALUES (?,?,?,?,?,?,?)")) {
                st.setString(1, orderId); st.setInt(2, merchant.getMerchantID());
                st.setDate(3, java.sql.Date.valueOf(date));
                st.setString(4, "accepted");
                st.setDouble(5, total);   // subtotal
                st.setDouble(6, 0);       // discount
                st.setDouble(7, total);   // total_amount
                st.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO order_items (order_ID, item_ID, description, quantity, unit_cost, unit_price, subtotal, line_total, total) VALUES (?,?,?,?,?,?,?,?,?)")) {
                for (CartRow r : items) {
                    ps.setString(1, orderId); ps.setString(2, r.itemId.get());
                    ps.setString(3, r.description.get()); ps.setInt(4, r.quantity.get());
                    ps.setDouble(5, r.unitCost.get());
                    ps.setDouble(6, r.unitCost.get());                              // unit_price (legacy column)
                    double __line = r.unitCost.get() * r.quantity.get();
                    ps.setDouble(7, __line);   // subtotal
                    ps.setDouble(8, __line);   // line_total (legacy)
                    ps.setDouble(9, __line);   // total (legacy)
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
                ps.setDate(1, java.sql.Date.valueOf(date.plusDays(30)));
                ps.setInt(2, merchant.getMerchantID());
                ps.executeUpdate();
            }
            conn.commit();
            UIUtil.info("Order placed", "Order #" + orderId + " has been submitted to InfoPharma.");
            reload();
        } catch (Exception e) { UIUtil.error("Error", e.getMessage()); }
    }

    public static class Row {
        public final SimpleStringProperty orderId, date, status, invoiceId;
        public final SimpleDoubleProperty total;
        public Row(String oid, String d, double t, String s, String inv) {
            this.orderId = new SimpleStringProperty(oid);
            this.date = new SimpleStringProperty(d);
            this.total = new SimpleDoubleProperty(t);
            this.status = new SimpleStringProperty(s);
            this.invoiceId = new SimpleStringProperty(inv);
        }
        public String getOrderId() { return orderId.get(); }
        public String getDate() { return date.get(); }
        public double getTotal() { return total.get(); }
        public String getStatus() { return status.get(); }
        public String getInvoiceId() { return invoiceId.get(); }
    }
    public static class ItemRow {
        public final SimpleStringProperty itemId, description;
        public final SimpleIntegerProperty quantity;
        public final SimpleDoubleProperty unitCost;
        public ItemRow(String i, String d, int q, double c) {
            this.itemId = new SimpleStringProperty(i);
            this.description = new SimpleStringProperty(d);
            this.quantity = new SimpleIntegerProperty(q);
            this.unitCost = new SimpleDoubleProperty(c);
        }
        public String getItemId() { return itemId.get(); }
        public String getDescription() { return description.get(); }
        public int getQuantity() { return quantity.get(); }
        public double getUnitCost() { return unitCost.get(); }
    }
    public static class CartRow extends ItemRow {
        public CartRow(String i, String d, int q, double c) { super(i, d, q, c); }
    }
    private record CatRef(String id, String description, double cost, int availability) {
        @Override public String toString() {
            return description + String.format("  (£%.2f, %d in stock)", cost, availability);
        }
    }
}
