package com.prototype.ipossa.ui.pages;

import com.prototype.ipossa.MyJDBC;
import com.prototype.ipossa.systems.ACC.UserAccount;
import com.prototype.ipossa.ui.Formats;
import com.prototype.ipossa.ui.UIUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reports page implementing the six §8.1 / IPOS-SA-RPT reports:
 *
 *   (i)   Turnover for a given period (line chart by month + bar by item)
 *   (ii)  List of orders received from a particular merchant for a period
 *   (iii) Activity report for an individual merchant for a period
 *   (iv)  List of invoices raised against a merchant for a period
 *   (v)   List of all invoices raised by InfoPharma for a period
 *   (vi)  Stock turnover within a given period (bar chart, in vs out)
 *
 * Each report has appropriate visual representations:
 *   - Line graph for turnover over time
 *   - Pie chart for revenue share by merchant
 *   - Bar charts for item-level breakdowns and stock movement
 *   - Tables for invoice/order listings
 */
public class ReportsPage {

    private final UserAccount user;
    private VBox visualArea;
    private TextArea textOutput;
    private DatePicker startPicker, endPicker;
    private ComboBox<MerchantRef> merchantPicker;
    private String currentReportTitle = "Report";

    public ReportsPage(UserAccount user) { this.user = user; }

    public Node build() {
        VBox root = new VBox(14);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().add(UIUtil.h2("Reports"));
        root.getChildren().add(header);

        if (!user.canGenerateReports()) {
            root.getChildren().add(UIUtil.dim(
                    "Your role does not permit generating reports. Contact an administrator."));
            return root;
        }

        // ── Report picker + date range + merchant picker ─────────────────
        ComboBox<String> picker = new ComboBox<>();
        picker.getItems().addAll(
                "(i) Turnover for period",
                "(ii) Orders by merchant for period",
                "(iii) Merchant activity report",
                "(iv) Invoices for a merchant for period",
                "(v) All invoices for period",
                "(vi) Stock turnover for period"
        );
        picker.setValue("(i) Turnover for period");
        picker.setPrefWidth(300);

        startPicker = new DatePicker(LocalDate.now().minusMonths(3));
        endPicker   = new DatePicker(LocalDate.now());

        merchantPicker = new ComboBox<>();
        merchantPicker.setPrefWidth(220);
        merchantPicker.setPromptText("Merchant…");
        loadMerchants();

        Button generate = new Button("Generate");
        generate.getStyleClass().addAll("button", "button-primary");
        generate.setOnAction(e -> {
            currentReportTitle = picker.getValue();
            renderReport(picker.getValue());
        });

        Button printBtn = new Button("🖨 Print");
        printBtn.getStyleClass().add("button");
        printBtn.setOnAction(e -> printReport());

        Button saveBtn = new Button("💾 Save as file");
        saveBtn.getStyleClass().add("button");
        saveBtn.setOnAction(e -> saveReport());

        Button remindersBtn = new Button("📧 Generate debtor reminders");
        remindersBtn.getStyleClass().add("button");
        remindersBtn.setOnAction(e -> generateReminders());

        // Two control rows
        HBox row1 = new HBox(10, picker, generate, printBtn, saveBtn, remindersBtn);
        row1.setAlignment(Pos.CENTER_LEFT);
        HBox row2 = new HBox(10,
                new Label("From:"), startPicker,
                new Label("To:"), endPicker,
                new Label("Merchant (for ii/iii/iv):"), merchantPicker);
        row2.setAlignment(Pos.CENTER_LEFT);

        VBox controls = new VBox(8, row1, row2);
        controls.getStyleClass().add("card");
        root.getChildren().add(controls);

        visualArea = new VBox(12);
        visualArea.getStyleClass().add("card");
        visualArea.setMinHeight(420);
        Label placeholder = new Label("Select a report and click Generate.");
        placeholder.getStyleClass().add("dim");
        visualArea.getChildren().add(placeholder);

        textOutput = new TextArea();
        textOutput.setEditable(false);
        textOutput.setPrefRowCount(10);
        textOutput.setPromptText("Generated report data will appear here.");
        textOutput.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace;");

        ScrollPane vSp = new ScrollPane(visualArea);
        vSp.setFitToWidth(true);
        VBox.setVgrow(vSp, Priority.ALWAYS);

        SplitPane split = new SplitPane();
        split.setOrientation(javafx.geometry.Orientation.VERTICAL);
        split.getItems().addAll(vSp, textOutput);
        split.setDividerPositions(0.62);
        VBox.setVgrow(split, Priority.ALWAYS);
        root.getChildren().add(split);

        return root;
    }

    private void loadMerchants() {
        try (Connection conn = MyJDBC.getConnection()) {
            ResultSet rs = conn.prepareStatement(
                    "SELECT merchant_ID, account_holder_name FROM merchants ORDER BY account_holder_name")
                    .executeQuery();
            while (rs.next())
                merchantPicker.getItems().add(new MerchantRef(rs.getInt(1), rs.getString(2)));
        } catch (Exception ignored) {}
    }

    private void renderReport(String name) {
        visualArea.getChildren().clear();
        textOutput.clear();
        Label title = new Label(name);
        title.getStyleClass().add("h2");
        visualArea.getChildren().add(title);
        Label sub = new Label("Period: " + startPicker.getValue() + " to " + endPicker.getValue()
                + "  ·  Generated " + LocalDate.now());
        sub.getStyleClass().add("dim");
        visualArea.getChildren().add(sub);

        if (name.startsWith("(i)"))   reportTurnover();
        else if (name.startsWith("(ii)"))  reportOrdersByMerchant();
        else if (name.startsWith("(iii)")) reportMerchantActivity();
        else if (name.startsWith("(iv)"))  reportInvoicesForMerchant();
        else if (name.startsWith("(v)"))   reportAllInvoices();
        else if (name.startsWith("(vi)"))  reportStockTurnover();
    }

    // ─── (i) Turnover for given period ──────────────────────────────
    // Quantity sold + revenue: line chart of revenue by month + pie of
    // revenue share by merchant + raw text data table.
    private void reportTurnover() {
        java.sql.Date s = java.sql.Date.valueOf(startPicker.getValue());
        java.sql.Date e = java.sql.Date.valueOf(endPicker.getValue());

        // Revenue by month (line)
        Map<String, Double> byMonth = new LinkedHashMap<>();
        // Revenue by merchant (pie)
        Map<String, Double> byMerchant = new LinkedHashMap<>();
        double totalRevenue = 0;
        long totalQty = 0;

        try (Connection conn = MyJDBC.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("""
                SELECT DATE_FORMAT(o.order_date,'%Y-%m') AS ym,
                       COALESCE(SUM(o.total_amount),0) AS rev
                FROM orders o
                WHERE o.order_date BETWEEN ? AND ?
                GROUP BY ym ORDER BY ym
            """)) {
                ps.setDate(1, s); ps.setDate(2, e);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) byMonth.put(rs.getString("ym"), rs.getDouble("rev"));
            }
            try (PreparedStatement ps = conn.prepareStatement("""
                SELECT m.account_holder_name AS name,
                       COALESCE(SUM(o.total_amount),0) AS rev,
                       COALESCE(SUM(oi.quantity),0) AS qty
                FROM merchants m
                LEFT JOIN orders o ON o.merchant_ID = m.merchant_ID
                                  AND o.order_date BETWEEN ? AND ?
                LEFT JOIN order_items oi ON oi.order_ID = o.order_ID
                GROUP BY m.merchant_ID, m.account_holder_name
                HAVING rev > 0
                ORDER BY rev DESC
            """)) {
                ps.setDate(1, s); ps.setDate(2, e);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String name = rs.getString("name");
                    double rev = rs.getDouble("rev");
                    long qty = rs.getLong("qty");
                    byMerchant.put(name, rev);
                    totalRevenue += rev;
                    totalQty += qty;
                }
            }
        } catch (Exception ex) { showError(ex); return; }

        if (byMerchant.isEmpty()) { visualArea.getChildren().add(emptyMsg()); return; }

        // Headline numbers
        HBox headline = new HBox(20);
        headline.getChildren().addAll(
                statBlock("Total revenue", Formats.pound(totalRevenue)),
                statBlock("Total units sold", String.valueOf(totalQty)),
                statBlock("Active merchants", String.valueOf(byMerchant.size())));
        visualArea.getChildren().add(headline);

        // Line chart: revenue over time
        if (!byMonth.isEmpty()) {
            CategoryAxis x = new CategoryAxis(); x.setLabel("Month");
            NumberAxis y = new NumberAxis(); y.setLabel("Revenue (£)");
            LineChart<String, Number> line = new LineChart<>(x, y);
            line.setTitle("Revenue by month");
            line.setAnimated(false); line.setLegendVisible(false);
            line.setPrefHeight(280);
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            for (var en : byMonth.entrySet())
                series.getData().add(new XYChart.Data<>(en.getKey(), en.getValue()));
            line.getData().add(series);
            visualArea.getChildren().add(line);
        }

        // Pie: revenue share by merchant
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        for (var en : byMerchant.entrySet())
            pieData.add(new PieChart.Data(en.getKey(), en.getValue()));
        PieChart pie = new PieChart(pieData);
        pie.setTitle("Revenue share by merchant");
        pie.setPrefHeight(320);
        visualArea.getChildren().add(pie);

        // Text data
        StringBuilder txt = new StringBuilder();
        txt.append("Turnover summary\n");
        txt.append(String.format("Period: %s to %s%n%n", startPicker.getValue(), endPicker.getValue()));
        txt.append(String.format("%-30s %14s%n", "Merchant", "Revenue £"));
        txt.append("-".repeat(50)).append("\n");
        for (var en : byMerchant.entrySet())
            txt.append(String.format("%-30s %14s%n",
                    trunc(en.getKey(), 30), Formats.money(en.getValue())));
        txt.append("-".repeat(50)).append("\n");
        txt.append(String.format("%-30s %14s%n", "TOTAL", Formats.money(totalRevenue)));
        textOutput.setText(txt.toString());
    }

    // ─── (ii) Orders received from a particular merchant for period ──
    private void reportOrdersByMerchant() {
        MerchantRef m = merchantPicker.getValue();
        if (m == null) { visualArea.getChildren().add(warnMsg("Select a merchant.")); return; }

        java.sql.Date s = java.sql.Date.valueOf(startPicker.getValue());
        java.sql.Date e = java.sql.Date.valueOf(endPicker.getValue());

        ObservableList<OrderRow> rows = FXCollections.observableArrayList();
        double totalValue = 0;
        int count = 0, paid = 0;
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                SELECT o.order_ID, o.order_date, o.total_amount, o.status, o.invoice_ID,
                       (SELECT COALESCE(SUM(amount),0) FROM payments p
                          WHERE p.merchant_ID = o.merchant_ID
                            AND p.payment_date <= ?
                       ) AS cumulative_paid
                FROM orders o
                WHERE o.merchant_ID = ? AND o.order_date BETWEEN ? AND ?
                ORDER BY o.order_date
             """)) {
            ps.setDate(1, e);
            ps.setInt(2, m.id); ps.setDate(3, s); ps.setDate(4, e);
            ResultSet rs = ps.executeQuery();
            double cumOrders = 0;
            while (rs.next()) {
                count++;
                double amt = rs.getDouble("total_amount");
                cumOrders += amt;
                totalValue += amt;
                double cumPaid = rs.getDouble("cumulative_paid");
                String paymentStatus = cumPaid >= cumOrders ? "Paid" : "Pending";
                if ("Paid".equals(paymentStatus)) paid++;
                rows.add(new OrderRow(
                        String.valueOf(rs.getInt("order_ID")),
                        String.valueOf(rs.getDate("order_date")),
                        Formats.money(amt),
                        rs.getString("status"),
                        rs.getString("invoice_ID") == null ? "—" : rs.getString("invoice_ID"),
                        paymentStatus));
            }
        } catch (Exception ex) { showError(ex); return; }

        if (rows.isEmpty()) { visualArea.getChildren().add(emptyMsg()); return; }

        // Headline
        HBox headline = new HBox(20,
                statBlock("Merchant", m.name),
                statBlock("Orders in period", String.valueOf(count)),
                statBlock("Total value", Formats.pound(totalValue)),
                statBlock("Paid / Pending", paid + " / " + (count - paid)));
        visualArea.getChildren().add(headline);

        // Table
        TableView<OrderRow> table = new TableView<>(rows);
        table.setPrefHeight(320);
        table.getColumns().add(strCol("Order #",  "orderId",  90));
        table.getColumns().add(strCol("Date",     "date",     110));
        table.getColumns().add(strCol("Value £",  "amount",   100));
        table.getColumns().add(strCol("Status",   "status",   140));
        table.getColumns().add(strCol("Invoice",  "invoiceId",120));
        table.getColumns().add(strCol("Payment",  "payment",  100));
        visualArea.getChildren().add(table);

        // Bar chart: order values
        CategoryAxis x = new CategoryAxis(); x.setLabel("Order #");
        NumberAxis y = new NumberAxis(); y.setLabel("Value (£)");
        BarChart<String, Number> bar = new BarChart<>(x, y);
        bar.setTitle("Order values");
        bar.setLegendVisible(false); bar.setAnimated(false); bar.setPrefHeight(280);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (OrderRow r : rows) {
            try { series.getData().add(new XYChart.Data<>("#" + r.orderId.get(),
                    Double.parseDouble(r.amount.get()))); } catch (Exception ignored) {}
        }
        bar.getData().add(series);
        visualArea.getChildren().add(bar);

        // Text
        StringBuilder txt = new StringBuilder();
        txt.append("Orders for ").append(m.name).append("\n");
        txt.append("Period: ").append(startPicker.getValue()).append(" to ").append(endPicker.getValue()).append("\n\n");
        txt.append(String.format("%-10s %-12s %12s %-18s %-12s %-10s%n",
                "Order #", "Date", "Value £", "Status", "Invoice", "Payment"));
        txt.append("-".repeat(80)).append("\n");
        for (OrderRow r : rows) {
            txt.append(String.format("%-10s %-12s %12s %-18s %-12s %-10s%n",
                    r.orderId.get(), r.date.get(), r.amount.get(),
                    r.status.get(), r.invoiceId.get(), r.payment.get()));
        }
        txt.append("-".repeat(80)).append("\n");
        txt.append(String.format("Totals: %d orders   Value £%s   Paid: %d   Pending: %d%n",
                count, Formats.money(totalValue), paid, count - paid));
        textOutput.setText(txt.toString());
    }

    // ─── (iii) Activity report for a merchant ────────────────────────
    private void reportMerchantActivity() {
        MerchantRef m = merchantPicker.getValue();
        if (m == null) { visualArea.getChildren().add(warnMsg("Select a merchant.")); return; }

        java.sql.Date s = java.sql.Date.valueOf(startPicker.getValue());
        java.sql.Date e = java.sql.Date.valueOf(endPicker.getValue());

        // Header card with merchant contact info
        try (Connection conn = MyJDBC.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT contact_name, address, phone_number, agreed_discount, account_state " +
                    "FROM merchants WHERE merchant_ID=?")) {
                ps.setInt(1, m.id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    VBox card = new VBox(4);
                    card.getStyleClass().add("card");
                    card.getChildren().addAll(
                            new Label("Merchant: " + m.name),
                            new Label("Contact: " + nz(rs.getString(1))),
                            new Label("Address: " + nz(rs.getString(2))),
                            new Label("Phone: "   + nz(rs.getString(3))),
                            new Label("Discount: " + nz(rs.getString(4)) +
                                    "   ·   State: " + nz(rs.getString(5))));
                    visualArea.getChildren().add(card);
                }
            }
        } catch (Exception ex) { showError(ex); return; }

        // Items sold breakdown
        ObservableList<ItemRow> rows = FXCollections.observableArrayList();
        Map<String, Double> revByItem = new LinkedHashMap<>();
        double grandTotal = 0;
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                SELECT oi.item_ID, oi.description,
                       SUM(oi.quantity) AS qty,
                       AVG(oi.unit_cost) AS unit,
                       SUM(oi.quantity * oi.unit_cost) AS line_total
                FROM order_items oi
                JOIN orders o ON o.order_ID = oi.order_ID
                WHERE o.merchant_ID = ? AND o.order_date BETWEEN ? AND ?
                GROUP BY oi.item_ID, oi.description
                ORDER BY line_total DESC
             """)) {
            ps.setInt(1, m.id); ps.setDate(2, s); ps.setDate(3, e);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                double lt = rs.getDouble("line_total");
                grandTotal += lt;
                rows.add(new ItemRow(
                        rs.getString("item_ID"),
                        rs.getString("description"),
                        String.valueOf(rs.getInt("qty")),
                        Formats.money(rs.getDouble("unit")),
                        Formats.money(lt)));
                revByItem.put(rs.getString("description"), lt);
            }
        } catch (Exception ex) { showError(ex); return; }

        if (rows.isEmpty()) { visualArea.getChildren().add(emptyMsg()); return; }

        TableView<ItemRow> table = new TableView<>(rows);
        table.setPrefHeight(280);
        table.getColumns().add(strCol("Item ID",     "itemId",      120));
        table.getColumns().add(strCol("Description", "description", 220));
        table.getColumns().add(strCol("Qty",         "qty",         70));
        table.getColumns().add(strCol("Unit £",      "unit",        90));
        table.getColumns().add(strCol("Line £",      "lineTotal",   100));
        visualArea.getChildren().add(table);

        // Bar chart of item revenue
        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis(); y.setLabel("Revenue (£)");
        BarChart<String, Number> bar = new BarChart<>(x, y);
        bar.setTitle("Revenue by item");
        bar.setLegendVisible(false); bar.setAnimated(false); bar.setPrefHeight(300);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (var en : revByItem.entrySet())
            series.getData().add(new XYChart.Data<>(trunc(en.getKey(), 14), en.getValue()));
        bar.getData().add(series);
        visualArea.getChildren().add(bar);

        StringBuilder txt = new StringBuilder();
        txt.append("Activity report — ").append(m.name).append("\n");
        txt.append("Period: ").append(startPicker.getValue()).append(" to ").append(endPicker.getValue()).append("\n\n");
        txt.append(String.format("%-12s %-30s %6s %10s %12s%n",
                "Item ID", "Description", "Qty", "Unit £", "Line £"));
        txt.append("-".repeat(75)).append("\n");
        for (ItemRow r : rows) {
            txt.append(String.format("%-12s %-30s %6s %10s %12s%n",
                    r.itemId.get(), trunc(r.description.get(), 30),
                    r.qty.get(), r.unit.get(), r.lineTotal.get()));
        }
        txt.append("-".repeat(75)).append("\n");
        txt.append(String.format("Grand total: £%s%n", Formats.money(grandTotal)));
        textOutput.setText(txt.toString());
    }

    // ─── (iv) Invoices raised against a merchant ─────────────────────
    private void reportInvoicesForMerchant() {
        MerchantRef m = merchantPicker.getValue();
        if (m == null) { visualArea.getChildren().add(warnMsg("Select a merchant.")); return; }
        renderInvoiceList(m, "Invoices for " + m.name);
    }

    // ─── (v) All invoices for period ─────────────────────────────────
    private void reportAllInvoices() {
        renderInvoiceList(null, "All invoices");
    }

    private void renderInvoiceList(MerchantRef m, String headline) {
        java.sql.Date s = java.sql.Date.valueOf(startPicker.getValue());
        java.sql.Date e = java.sql.Date.valueOf(endPicker.getValue());

        ObservableList<InvoiceRow> rows = FXCollections.observableArrayList();
        double total = 0;
        Map<String, Double> byMerchant = new LinkedHashMap<>();
        try (Connection conn = MyJDBC.getConnection()) {
            String sql = """
                SELECT o.invoice_ID, o.order_ID, o.order_date, o.total_amount, o.status,
                       m.account_holder_name
                FROM orders o LEFT JOIN merchants m ON m.merchant_ID = o.merchant_ID
                WHERE o.invoice_ID IS NOT NULL AND o.order_date BETWEEN ? AND ?
                """ + (m == null ? "" : "AND o.merchant_ID = ? ") +
                "ORDER BY o.order_date";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDate(1, s); ps.setDate(2, e);
                if (m != null) ps.setInt(3, m.id);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    double amt = rs.getDouble("total_amount");
                    total += amt;
                    String mn = nz(rs.getString("account_holder_name"));
                    byMerchant.merge(mn, amt, Double::sum);
                    rows.add(new InvoiceRow(
                            rs.getString("invoice_ID"),
                            String.valueOf(rs.getInt("order_ID")),
                            String.valueOf(rs.getDate("order_date")),
                            mn,
                            Formats.money(amt),
                            rs.getString("status")));
                }
            }
        } catch (Exception ex) { showError(ex); return; }

        if (rows.isEmpty()) { visualArea.getChildren().add(emptyMsg()); return; }

        HBox head = new HBox(20,
                statBlock(headline, ""),
                statBlock("Invoices", String.valueOf(rows.size())),
                statBlock("Total value", Formats.pound(total)));
        visualArea.getChildren().add(head);

        TableView<InvoiceRow> table = new TableView<>(rows);
        table.setPrefHeight(320);
        table.getColumns().add(strCol("Invoice",   "invoiceId",  130));
        table.getColumns().add(strCol("Order #",   "orderId",    80));
        table.getColumns().add(strCol("Date",      "date",       110));
        table.getColumns().add(strCol("Merchant",  "merchant",   200));
        table.getColumns().add(strCol("Amount £",  "amount",     110));
        table.getColumns().add(strCol("Status",    "status",     140));
        visualArea.getChildren().add(table);

        // For "all invoices" add a pie of value share
        if (m == null && !byMerchant.isEmpty()) {
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            for (var en : byMerchant.entrySet())
                pieData.add(new PieChart.Data(en.getKey(), en.getValue()));
            PieChart pie = new PieChart(pieData);
            pie.setTitle("Invoiced value share by merchant");
            pie.setPrefHeight(280);
            visualArea.getChildren().add(pie);
        }

        StringBuilder txt = new StringBuilder();
        txt.append(headline).append("\n");
        txt.append("Period: ").append(startPicker.getValue()).append(" to ").append(endPicker.getValue()).append("\n\n");
        txt.append(String.format("%-14s %-8s %-12s %-25s %12s %-14s%n",
                "Invoice", "Order #", "Date", "Merchant", "Amount £", "Status"));
        txt.append("-".repeat(90)).append("\n");
        for (InvoiceRow r : rows) {
            txt.append(String.format("%-14s %-8s %-12s %-25s %12s %-14s%n",
                    r.invoiceId.get(), r.orderId.get(), r.date.get(),
                    trunc(r.merchant.get(), 25), r.amount.get(), r.status.get()));
        }
        txt.append("-".repeat(90)).append("\n");
        txt.append(String.format("Total: £%s%n", Formats.money(total)));
        textOutput.setText(txt.toString());
    }

    // ─── (vi) Stock turnover ─────────────────────────────────────────
    // Goods sold (out) vs new stock added (in) per item — bar chart.
    // "New stock" is approximated by the gap between current availability
    // and (initial availability - sold), assuming any positive delta is
    // restocking. For simplicity we report sold quantities and current stock.
    private void reportStockTurnover() {
        java.sql.Date s = java.sql.Date.valueOf(startPicker.getValue());
        java.sql.Date e = java.sql.Date.valueOf(endPicker.getValue());

        Map<String, long[]> data = new LinkedHashMap<>(); // desc -> [sold, currentStock]
        try (Connection conn = MyJDBC.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("""
                SELECT c.description,
                       COALESCE((SELECT SUM(oi.quantity)
                                 FROM order_items oi JOIN orders o ON o.order_ID = oi.order_ID
                                 WHERE oi.item_ID = c.item_ID
                                   AND o.order_date BETWEEN ? AND ?),0) AS sold,
                       c.availability
                FROM catalogue c
                ORDER BY sold DESC
            """)) {
                ps.setDate(1, s); ps.setDate(2, e);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    long sold = rs.getLong("sold");
                    long stock = rs.getLong("availability");
                    if (sold == 0 && stock == 0) continue;
                    data.put(rs.getString("description"), new long[]{sold, stock});
                }
            }
        } catch (Exception ex) { showError(ex); return; }

        if (data.isEmpty()) { visualArea.getChildren().add(emptyMsg()); return; }

        long totalSold = data.values().stream().mapToLong(v -> v[0]).sum();
        long totalStock = data.values().stream().mapToLong(v -> v[1]).sum();

        HBox head = new HBox(20,
                statBlock("Total sold (period)", String.valueOf(totalSold)),
                statBlock("Current stock total", String.valueOf(totalStock)),
                statBlock("Items tracked", String.valueOf(data.size())));
        visualArea.getChildren().add(head);

        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis(); y.setLabel("Packs");
        BarChart<String, Number> bar = new BarChart<>(x, y);
        bar.setTitle("Sold (in period) vs current stock");
        bar.setAnimated(false); bar.setPrefHeight(360);
        XYChart.Series<String, Number> sold = new XYChart.Series<>(); sold.setName("Sold");
        XYChart.Series<String, Number> stock = new XYChart.Series<>(); stock.setName("Stock");
        StringBuilder txt = new StringBuilder();
        txt.append("Stock turnover\n");
        txt.append("Period: ").append(startPicker.getValue()).append(" to ").append(endPicker.getValue()).append("\n\n");
        txt.append(String.format("%-30s %10s %10s%n", "Item", "Sold", "In stock"));
        txt.append("-".repeat(54)).append("\n");
        for (var en : data.entrySet()) {
            String d = trunc(en.getKey(), 14);
            sold.getData().add(new XYChart.Data<>(d, en.getValue()[0]));
            stock.getData().add(new XYChart.Data<>(d, en.getValue()[1]));
            txt.append(String.format("%-30s %10d %10d%n",
                    trunc(en.getKey(), 30), en.getValue()[0], en.getValue()[1]));
        }
        bar.getData().add(sold); bar.getData().add(stock);
        visualArea.getChildren().add(bar);
        textOutput.setText(txt.toString());
    }

    // ─── DEBTOR REMINDERS (per IPOS-SA-RPT requirement) ──────────────
    private void generateReminders() {
        StringBuilder all = new StringBuilder();
        try (Connection conn = MyJDBC.getConnection()) {
            ResultSet rs = conn.prepareStatement("""
                SELECT m.account_holder_name, m.account_number, m.address,
                  COALESCE((SELECT SUM(total_amount) FROM orders WHERE merchant_ID = m.merchant_ID),0) -
                  COALESCE((SELECT SUM(amount) FROM payments WHERE merchant_ID = m.merchant_ID),0) AS balance
                FROM merchants m HAVING balance > 0
                """).executeQuery();
            int count = 0;
            while (rs.next()) {
                count++;
                all.append(reminderLetter(
                        rs.getString("account_holder_name"),
                        rs.getString("account_number"),
                        rs.getString("address"),
                        rs.getDouble("balance")));
                all.append("\n").append("=".repeat(80)).append("\n\n");
            }
            if (count == 0) { UIUtil.info("Reminders", "No debtors found."); return; }
            visualArea.getChildren().clear();
            Label title = new Label("Debtor reminder letters"); title.getStyleClass().add("h2");
            visualArea.getChildren().add(title);
            visualArea.getChildren().add(UIUtil.dim(count + " letter(s) generated. Use Print or Save."));
            currentReportTitle = "Debtor reminders";
            textOutput.setText(all.toString());
        } catch (Exception e) { showError(e); }
    }

    private String reminderLetter(String name, String acc, String addr, double balance) {
        return String.format("""
            InfoPharma Ltd
            %s

            Dear %s,

            Our records show that your account (%s) currently has an outstanding
            balance of £%s.

            Please arrange for payment at your earliest convenience to avoid your
            account being suspended.

            If you have already made this payment, please disregard this reminder.

            Kind regards,
            InfoPharma Accounts Department
            """, LocalDate.now(), name, acc, Formats.money(balance)) +
                "Address on file: " + (addr == null ? "(not recorded)" : addr) + "\n";
    }

    // ─── PRINT / SAVE ────────────────────────────────────────────────
    private void printReport() {
        if (visualArea.getChildren().isEmpty() && textOutput.getText().isEmpty()) {
            UIUtil.warn("Nothing to print", "Generate a report first."); return;
        }
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) { UIUtil.warn("Printing", "No printer available. Use 'Save as file' instead."); return; }
        WritableImage img = visualArea.snapshot(new SnapshotParameters(), null);
        ImageView iv = new ImageView(img);
        iv.setFitWidth(595); iv.setPreserveRatio(true);
        if (job.showPrintDialog(visualArea.getScene().getWindow()) && job.printPage(iv)) job.endJob();
    }

    private void saveReport() {
        if (textOutput.getText().isEmpty()) { UIUtil.warn("Nothing to save", "Generate a report first."); return; }
        FileChooser fc = new FileChooser();
        fc.setTitle("Save report");
        fc.setInitialFileName(currentReportTitle.toLowerCase().replaceAll("[^a-z0-9]+", "_") + ".txt");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text file", "*.txt"),
                new FileChooser.ExtensionFilter("All files", "*.*"));
        var f = fc.showSaveDialog(textOutput.getScene().getWindow());
        if (f == null) return;
        try (FileWriter w = new FileWriter(f)) {
            w.write(currentReportTitle + " — generated " + LocalDate.now() + "\n\n");
            w.write(textOutput.getText());
            UIUtil.info("Saved", "Report saved to:\n" + f.getAbsolutePath());
        } catch (Exception e) { UIUtil.error("Error", e.getMessage()); }
    }

    // ─── HELPERS ─────────────────────────────────────────────────────
    private static <T> TableColumn<T, String> strCol(String title, String prop, double w) {
        TableColumn<T, String> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(prop)); c.setPrefWidth(w); return c;
    }
    private VBox statBlock(String label, String value) {
        Label l = new Label(label); l.getStyleClass().add("dim");
        Label v = new Label(value); v.getStyleClass().add("h2");
        VBox b = new VBox(2, l, v); b.getStyleClass().add("stat-card");
        return b;
    }
    private Label emptyMsg() {
        Label l = new Label("No data available for this report."); l.getStyleClass().add("dim"); return l;
    }
    private Label warnMsg(String msg) {
        Label l = new Label(msg); l.setStyle("-fx-text-fill: -danger;"); return l;
    }
    private void showError(Exception e) {
        Label l = new Label("Error: " + e.getMessage());
        l.setStyle("-fx-text-fill: -danger;");
        visualArea.getChildren().add(l);
    }
    private String trunc(String s, int len) {
        if (s == null) return "";
        return s.length() <= len ? s : s.substring(0, len - 1) + "…";
    }
    private String nz(String s) { return s == null ? "—" : s; }

    // ─── ROW DTOs ────────────────────────────────────────────────────
    public static class OrderRow {
        public final SimpleStringProperty orderId, date, amount, status, invoiceId, payment;
        public OrderRow(String o, String d, String a, String s, String i, String p) {
            this.orderId = new SimpleStringProperty(o); this.date = new SimpleStringProperty(d);
            this.amount = new SimpleStringProperty(a); this.status = new SimpleStringProperty(s);
            this.invoiceId = new SimpleStringProperty(i); this.payment = new SimpleStringProperty(p);
        }
        public String getOrderId() { return orderId.get(); }
        public String getDate() { return date.get(); }
        public String getAmount() { return amount.get(); }
        public String getStatus() { return status.get(); }
        public String getInvoiceId() { return invoiceId.get(); }
        public String getPayment() { return payment.get(); }
    }
    public static class ItemRow {
        public final SimpleStringProperty itemId, description, qty, unit, lineTotal;
        public ItemRow(String i, String d, String q, String u, String lt) {
            this.itemId = new SimpleStringProperty(i);
            this.description = new SimpleStringProperty(d);
            this.qty = new SimpleStringProperty(q);
            this.unit = new SimpleStringProperty(u);
            this.lineTotal = new SimpleStringProperty(lt);
        }
        public String getItemId() { return itemId.get(); }
        public String getDescription() { return description.get(); }
        public String getQty() { return qty.get(); }
        public String getUnit() { return unit.get(); }
        public String getLineTotal() { return lineTotal.get(); }
    }
    public static class InvoiceRow {
        public final SimpleStringProperty invoiceId, orderId, date, merchant, amount, status;
        public InvoiceRow(String inv, String o, String d, String m, String a, String s) {
            this.invoiceId = new SimpleStringProperty(inv);
            this.orderId = new SimpleStringProperty(o);
            this.date = new SimpleStringProperty(d);
            this.merchant = new SimpleStringProperty(m);
            this.amount = new SimpleStringProperty(a);
            this.status = new SimpleStringProperty(s);
        }
        public String getInvoiceId() { return invoiceId.get(); }
        public String getOrderId() { return orderId.get(); }
        public String getDate() { return date.get(); }
        public String getMerchant() { return merchant.get(); }
        public String getAmount() { return amount.get(); }
        public String getStatus() { return status.get(); }
    }
    private record MerchantRef(int id, String name) {
        @Override public String toString() { return name; }
    }
}
