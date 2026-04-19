package com.prototype.ipossa.ui.pages;

import com.prototype.ipossa.MyJDBC;
import com.prototype.ipossa.systems.ACC.UserAccount;
import com.prototype.ipossa.ui.UIUtil;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
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

/**
 * The type Catalogue page.
 */
public class CataloguePage {

    private final UserAccount user;
    private final boolean readOnly;
    private final ObservableList<Row> data = FXCollections.observableArrayList();
    private TableView<Row> table;

    /**
     * Instantiates a new Catalogue page.
     *
     * @param user the user
     */
    public CataloguePage(UserAccount user) { this(user, false); }

    /**
     * Instantiates a new Catalogue page.
     *
     * @param user     the user
     * @param readOnly the read only
     */
    public CataloguePage(UserAccount user, boolean readOnly) {
        this.user = user;
        this.readOnly = readOnly;
    }

    /**
     * canManage
     * @return
     */
    private boolean canManage() {
        if (readOnly || user == null) return false;
        var r = user.getRole();
        return r == com.prototype.ipossa.systems.ACC.Role.ADMINISTRATOR
                || r == com.prototype.ipossa.systems.ACC.Role.DIRECTOR_OF_OPERATIONS
                || r == com.prototype.ipossa.systems.ACC.Role.WAREHOUSE_EMPLOYEE;
    }

    /**
     * Build node.
     *
     * @return the node
     */
    public Node build() {
        VBox root = new VBox(14);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().add(UIUtil.h2("Product Catalogue"));
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        TextField search = new TextField();
        search.setPromptText("Search by ID, description or keyword…");
        search.setPrefWidth(320);

        Button addBtn = new Button("+ Add product");
        addBtn.getStyleClass().addAll("button", "button-primary");
        addBtn.setDisable(!canManage());
        addBtn.setOnAction(e -> addDialog());

        Button refreshBtn = new Button("↻ Refresh");
        refreshBtn.getStyleClass().add("button");
        refreshBtn.setOnAction(e -> reload());

        header.getChildren().addAll(sp, search, refreshBtn, addBtn);
        root.getChildren().add(header);

        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);
        root.getChildren().add(table);

        FilteredList<Row> filtered = new FilteredList<>(data, x -> true);
        search.textProperty().addListener((o, ov, nv) -> {
            String q = nv == null ? "" : nv.toLowerCase().trim();
            filtered.setPredicate(r -> q.isEmpty()
                    || r.itemId.get().toLowerCase().contains(q)
                    || r.description.get().toLowerCase().contains(q)
                    || r.packageType.get().toLowerCase().contains(q));
        });
        table.setItems(filtered);

        // Low stock summary
        Label lowLabel = new Label();
        lowLabel.getStyleClass().add("warning-banner");
        lowLabel.setMaxWidth(Double.MAX_VALUE);
        lowLabel.setVisible(false);
        lowLabel.setManaged(false);
        root.getChildren().add(lowLabel);

        data.addListener((javafx.collections.ListChangeListener<Row>) c -> updateLowStockLabel(lowLabel));

        reload();
        updateLowStockLabel(lowLabel);

        return root;
    }

    /**
     * updateLowStockLabel
     * @param l
     */
    private void updateLowStockLabel(Label l) {
        long low = data.stream().filter(r -> r.availability.get() < r.stockLimit.get()).count();
        if (low > 0) {
            l.setText("⚠  " + low + " item(s) are below their minimum stock level.");
            l.setVisible(true); l.setManaged(true);
        } else {
            l.setVisible(false); l.setManaged(false);
        }
    }

    /**
     * buildTable
     * @return
     */
    private TableView<Row> buildTable() {
        TableView<Row> t = new TableView<>();
        t.setPlaceholder(UIUtil.dim("No catalogue entries to show."));

        TableColumn<Row, String> id = col("Item ID", "itemId", 130);
        TableColumn<Row, String> desc = col("Description", "description", 220);
        TableColumn<Row, String> pt = col("Package", "packageType", 90);
        TableColumn<Row, String> unit = col("Unit", "unit", 70);
        TableColumn<Row, Number> uip = numCol("Units/pack", "unitsInPack", 90);
        TableColumn<Row, Number> cost = numCol("Cost", "packageCost", 90);
        cost.setCellFactory(com.prototype.ipossa.ui.Formats.moneyCell());
        TableColumn<Row, Number> avail = numCol("Stock", "availability", 90);
        TableColumn<Row, Number> limit = numCol("Min", "stockLimit", 80);

        TableColumn<Row, Void> actions = new TableColumn<>("Actions");
        actions.setPrefWidth(240);
        actions.setCellFactory(c -> new TableCell<>() {
            final Button edit = new Button("Edit");
            final Button stock = new Button("Stock");
            final Button del = new Button("Delete");
            final HBox box = new HBox(6, edit, stock, del);
            {
                edit.getStyleClass().add("button");
                stock.getStyleClass().add("button");
                del.getStyleClass().addAll("button", "button-danger");
                edit.setOnAction(e -> editDialog(getTableRow().getItem()));
                stock.setOnAction(e -> stockDialog(getTableRow().getItem()));
                del.setOnAction(e -> deleteRow(getTableRow().getItem()));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    boolean can = canManage();
                    edit.setDisable(!can); stock.setDisable(!can); del.setDisable(!can);
                    setGraphic(box);
                }
            }
        });

        t.getColumns().add(id);
        t.getColumns().add(desc);
        t.getColumns().add(pt);
        t.getColumns().add(unit);
        t.getColumns().add(uip);
        t.getColumns().add(cost);
        t.getColumns().add(avail);
        t.getColumns().add(limit);
        t.getColumns().add(actions);
        return t;
    }

    /**
     * col
     * @param title
     * @param prop
     * @param w
     * @return
     * @param <T>
     */
    @SuppressWarnings("unchecked")
    private <T> TableColumn<Row, String> col(String title, String prop, double w) {
        TableColumn<Row, String> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setPrefWidth(w);
        return c;
    }

    /**
     * numCol
     * @param title
     * @param prop
     * @param w
     * @return
     */
    private TableColumn<Row, Number> numCol(String title, String prop, double w) {
        TableColumn<Row, Number> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setPrefWidth(w);
        return c;
    }

    /**
     * reload
     */
    private void reload() {
        data.clear();
        try (Connection conn = MyJDBC.getConnection()) {
            ResultSet rs = conn.prepareStatement("SELECT * FROM catalogue").executeQuery();
            while (rs.next()) {
                data.add(new Row(
                        rs.getString("item_ID"),
                        rs.getString("description"),
                        rs.getString("package_type"),
                        rs.getString("unit"),
                        rs.getInt("units_in_a_pack"),
                        rs.getDouble("package_cost"),
                        rs.getInt("availability"),
                        rs.getInt("stock_limit")
                ));
            }
        } catch (Exception e) {
            UIUtil.error("Database error", "Could not load catalogue:\n" + e.getMessage());
        }
    }

    /**
     * addDialog
     */
    private void addDialog() {
        Row r = new Row("", "", "box", "Caps", 10, 0, 0, 0);
        if (openEditor(r, "Add product")) {
            try (Connection conn = MyJDBC.getConnection();
                 PreparedStatement st = conn.prepareStatement("""
                    INSERT INTO catalogue (item_ID, description, package_type, unit,
                        units_in_a_pack, package_cost, availability, stock_limit)
                    VALUES (?,?,?,?,?,?,?,?)
                    """)) {
                st.setString(1, r.itemId.get());
                st.setString(2, r.description.get());
                st.setString(3, r.packageType.get());
                st.setString(4, r.unit.get());
                st.setInt(5, r.unitsInPack.get());
                st.setDouble(6, r.packageCost.get());
                st.setInt(7, r.availability.get());
                st.setInt(8, r.stockLimit.get());
                st.executeUpdate();
                reload();
            } catch (Exception e) { UIUtil.error("Error", e.getMessage()); }
        }
    }

    /**
     * editDialog
     * @param r
     */
    private void editDialog(Row r) {
        if (r == null) return;
        if (openEditor(r, "Edit product")) {
            try (Connection conn = MyJDBC.getConnection();
                 PreparedStatement st = conn.prepareStatement("""
                    UPDATE catalogue SET description=?, package_type=?, unit=?,
                        units_in_a_pack=?, package_cost=?, availability=?, stock_limit=?
                    WHERE item_ID=?
                    """)) {
                st.setString(1, r.description.get());
                st.setString(2, r.packageType.get());
                st.setString(3, r.unit.get());
                st.setInt(4, r.unitsInPack.get());
                st.setDouble(5, r.packageCost.get());
                st.setInt(6, r.availability.get());
                st.setInt(7, r.stockLimit.get());
                st.setString(8, r.itemId.get());
                st.executeUpdate();
                reload();
            } catch (Exception e) { UIUtil.error("Error", e.getMessage()); }
        }
    }

    /**
     * stockDialog
     * @param r
     */
    private void stockDialog(Row r) {
        if (r == null) return;
        TextInputDialog d = new TextInputDialog(String.valueOf(r.availability.get()));
        d.setTitle("Update stock");
        d.setHeaderText("Update stock for: " + r.description.get());
        d.setContentText("New stock quantity:");
        d.showAndWait().ifPresent(v -> {
            try {
                int q = Integer.parseInt(v.trim());
                try (Connection conn = MyJDBC.getConnection();
                     PreparedStatement st = conn.prepareStatement(
                             "UPDATE catalogue SET availability=? WHERE item_ID=?")) {
                    st.setInt(1, q); st.setString(2, r.itemId.get()); st.executeUpdate();
                    reload();
                }
                if (q < r.stockLimit.get()) {
                    UIUtil.warn("Low stock", "Stock for '" + r.description.get()
                            + "' is now below the minimum (" + r.stockLimit.get() + ").");
                }
            } catch (Exception e) { UIUtil.error("Error", e.getMessage()); }
        });
    }

    /**
     * deleteRow
     * @param r
     */
    private void deleteRow(Row r) {
        if (r == null) return;
        if (!UIUtil.confirm("Delete product",
                "Delete '" + r.description.get() + "'? This cannot be undone.")) return;
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement st = conn.prepareStatement("DELETE FROM catalogue WHERE item_ID=?")) {
            st.setString(1, r.itemId.get()); st.executeUpdate();
            reload();
        } catch (Exception e) { UIUtil.error("Error", e.getMessage()); }
    }

    /**
     * openEditor
     * @param r
     * @param title
     * @return
     */
    private boolean openEditor(Row r, String title) {
        Dialog<Boolean> d = new Dialog<>();
        d.setTitle(title);
        d.setHeaderText(null);

        GridPane g = new GridPane();
        g.setHgap(10); g.setVgap(8); g.setPadding(new Insets(10));

        TextField id = new TextField(r.itemId.get());
        TextField desc = new TextField(r.description.get());
        TextField pkg = new TextField(r.packageType.get());
        TextField unit = new TextField(r.unit.get());
        TextField uip = new TextField(String.valueOf(r.unitsInPack.get()));
        TextField cost = new TextField(String.format("%.2f", r.packageCost.get()));
        TextField avail = new TextField(String.valueOf(r.availability.get()));
        TextField limit = new TextField(String.valueOf(r.stockLimit.get()));

        id.setDisable(!title.startsWith("Add"));

        int row = 0;
        g.add(new Label("Item ID"), 0, row); g.add(id, 1, row++);
        g.add(new Label("Description"), 0, row); g.add(desc, 1, row++);
        g.add(new Label("Package type"), 0, row); g.add(pkg, 1, row++);
        g.add(new Label("Unit"), 0, row); g.add(unit, 1, row++);
        g.add(new Label("Units / pack"), 0, row); g.add(uip, 1, row++);
        g.add(new Label("Package cost £"), 0, row); g.add(cost, 1, row++);
        g.add(new Label("Stock availability"), 0, row); g.add(avail, 1, row++);
        g.add(new Label("Minimum stock"), 0, row); g.add(limit, 1, row++);

        d.getDialogPane().setContent(g);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        com.prototype.ipossa.ui.DialogStyle.apply(d);

        d.setResultConverter(b -> {
            if (b != ButtonType.OK) return false;
            try {
                r.itemId.set(id.getText().trim());
                r.description.set(desc.getText().trim());
                r.packageType.set(pkg.getText().trim());
                r.unit.set(unit.getText().trim());
                r.unitsInPack.set(Integer.parseInt(uip.getText().trim()));
                r.packageCost.set(Double.parseDouble(cost.getText().trim()));
                r.availability.set(Integer.parseInt(avail.getText().trim()));
                r.stockLimit.set(Integer.parseInt(limit.getText().trim()));
                return true;
            } catch (Exception e) { UIUtil.error("Invalid input", e.getMessage()); return false; }
        });
        return Boolean.TRUE.equals(d.showAndWait().orElse(false));
    }

    /**
     * The type Row.
     */
    public static class Row {
        /**
         * The Item id.
         */
        public final SimpleStringProperty itemId, /**
         * The Description.
         */
        description, /**
         * The Package type.
         */
        packageType, /**
         * The Unit.
         */
        unit;
        /**
         * The Units in pack.
         */
        public final SimpleIntegerProperty unitsInPack, /**
         * The Availability.
         */
        availability, /**
         * The Stock limit.
         */
        stockLimit;
        /**
         * The Package cost.
         */
        public final SimpleDoubleProperty packageCost;

        /**
         * Instantiates a new Row.
         *
         * @param id   the id
         * @param desc the desc
         * @param pt   the pt
         * @param u    the u
         * @param uip  the uip
         * @param c    the c
         * @param av   the av
         * @param lim  the lim
         */
        public Row(String id, String desc, String pt, String u, int uip, double c, int av, int lim) {
            this.itemId = new SimpleStringProperty(id);
            this.description = new SimpleStringProperty(desc);
            this.packageType = new SimpleStringProperty(pt);
            this.unit = new SimpleStringProperty(u);
            this.unitsInPack = new SimpleIntegerProperty(uip);
            this.packageCost = new SimpleDoubleProperty(c);
            this.availability = new SimpleIntegerProperty(av);
            this.stockLimit = new SimpleIntegerProperty(lim);
        }

        /**
         * Gets item id.
         *
         * @return the item id
         */
        public String getItemId() { return itemId.get(); }

        /**
         * Gets description.
         *
         * @return the description
         */
        public String getDescription() { return description.get(); }

        /**
         * Gets unit.
         *
         * @return the unit
         */
        public String getUnit() { return unit.get(); }

        /**
         * Gets availability.
         *
         * @return the availability
         */
        public int getAvailability() { return availability.get(); }

    }
}
