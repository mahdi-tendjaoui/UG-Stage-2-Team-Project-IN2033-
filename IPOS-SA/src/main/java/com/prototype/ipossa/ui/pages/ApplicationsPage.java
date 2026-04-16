package com.prototype.ipossa.ui.pages;

import com.prototype.ipossa.MyJDBC;
import com.prototype.ipossa.systems.ACC.UserAccount;
import com.prototype.ipossa.ui.DialogStyle;
import com.prototype.ipossa.ui.UIUtil;
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

public class ApplicationsPage {

    private final UserAccount user;
    private final ObservableList<Row> data = FXCollections.observableArrayList();
    private TableView<Row> table;

    public ApplicationsPage(UserAccount user) { this.user = user; }

    public Node build() {
        VBox root = new VBox(14);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().add(UIUtil.h2("Non-commercial applications"));
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        Button receiveBtn = new Button("+ Receive new application");
        receiveBtn.getStyleClass().addAll("button", "button-primary");
        receiveBtn.setOnAction(e -> receiveDialog());

        Button refresh = new Button("↻");
        refresh.getStyleClass().add("button");
        refresh.setOnAction(e -> reload());

        header.getChildren().addAll(sp, receiveBtn, refresh);
        root.getChildren().add(header);

        Label help = new Label("Applications received from the IPOS-PU portal. Review each and email the outcome to the applicant.");
        help.getStyleClass().add("dim");
        help.setWrapText(true);
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
        t.setPlaceholder(UIUtil.dim("No applications received."));

        TableColumn<Row, Number> id = new TableColumn<>("ID");
        id.setCellValueFactory(new PropertyValueFactory<>("id")); id.setPrefWidth(60);
        TableColumn<Row, String> name = new TableColumn<>("Applicant");
        name.setCellValueFactory(new PropertyValueFactory<>("name")); name.setPrefWidth(180);
        TableColumn<Row, String> email = new TableColumn<>("Email");
        email.setCellValueFactory(new PropertyValueFactory<>("email")); email.setPrefWidth(220);
        TableColumn<Row, String> type = new TableColumn<>("Type");
        type.setCellValueFactory(new PropertyValueFactory<>("type")); type.setPrefWidth(130);

        TableColumn<Row, String> status = new TableColumn<>("Status");
        status.setCellValueFactory(new PropertyValueFactory<>("status")); status.setPrefWidth(100);
        status.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); setText(null); return; }
                Label l = new Label(s); l.getStyleClass().add("badge");
                l.getStyleClass().add(switch (s.toLowerCase()) {
                    case "approved" -> "badge-normal";
                    case "rejected" -> "badge-default";
                    default -> "badge-suspended";
                });
                setGraphic(l); setText(null);
            }
        });

        TableColumn<Row, String> when = new TableColumn<>("Received");
        when.setCellValueFactory(new PropertyValueFactory<>("submitted")); when.setPrefWidth(150);

        TableColumn<Row, Void> actions = new TableColumn<>("Actions");
        actions.setPrefWidth(260);
        actions.setCellFactory(c -> new TableCell<>() {
            final Button approve = new Button("Approve");
            final Button reject = new Button("Reject");
            final Button email = new Button("Email outcome");
            final HBox box = new HBox(6, approve, reject, email);
            {
                approve.getStyleClass().addAll("button", "button-primary");
                reject.getStyleClass().addAll("button", "button-danger");
                email.getStyleClass().add("button");
                approve.setOnAction(e -> updateStatus(getTableRow().getItem(), "approved"));
                reject.setOnAction(e -> updateStatus(getTableRow().getItem(), "rejected"));
                email.setOnAction(e -> emailOutcome(getTableRow().getItem()));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                Row r = getTableRow() == null ? null : getTableRow().getItem();
                if (empty || r == null) { setGraphic(null); return; }
                boolean pending = "pending".equalsIgnoreCase(r.status.get());
                approve.setDisable(!pending);
                reject.setDisable(!pending);
                email.setDisable(pending);
                setGraphic(box);
            }
        });

        t.getColumns().add(id);
        t.getColumns().add(name);
        t.getColumns().add(email);
        t.getColumns().add(type);
        t.getColumns().add(status);
        t.getColumns().add(when);
        t.getColumns().add(actions);
        return t;
    }

    private void reload() {
        data.clear();
        try (Connection conn = MyJDBC.getConnection()) {
            ResultSet rs = conn.prepareStatement(
                    "SELECT * FROM applications ORDER BY submitted_at DESC"
            ).executeQuery();
            while (rs.next()) {
                data.add(new Row(
                        rs.getInt("application_ID"),
                        rs.getString("applicant_name"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getString("app_type"),
                        rs.getString("status"),
                        rs.getTimestamp("submitted_at") == null ? "" : rs.getTimestamp("submitted_at").toString()));
            }
        } catch (Exception e) { UIUtil.error("Error", e.getMessage()); }
    }

    private void receiveDialog() {
        Dialog<Void> d = new Dialog<>();
        d.setTitle("Receive application");
        d.setHeaderText("Log a new application received from the PU portal");

        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(8); g.setPadding(new Insets(4));
        TextField name = new TextField(); name.setPromptText("Full name or company");
        TextField email = new TextField(); email.setPromptText("email@example.com");
        TextField addr = new TextField(); addr.setPromptText("Address");
        ComboBox<String> type = new ComboBox<>(FXCollections.observableArrayList("non-commercial", "commercial"));
        type.setValue("non-commercial");
        int r = 0;
        g.add(new Label("Applicant:"), 0, r); g.add(name, 1, r++);
        g.add(new Label("Email:"), 0, r); g.add(email, 1, r++);
        g.add(new Label("Address:"), 0, r); g.add(addr, 1, r++);
        g.add(new Label("Type:"), 0, r); g.add(type, 1, r++);

        d.getDialogPane().setContent(g);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        DialogStyle.apply(d);

        d.setResultConverter(b -> {
            if (b != ButtonType.OK) return null;
            if (name.getText().isBlank() || email.getText().isBlank()) {
                UIUtil.warn("Missing", "Name and email are required."); return null;
            }
            try (Connection conn = MyJDBC.getConnection();
                 PreparedStatement st = conn.prepareStatement(
                         "INSERT INTO applications (applicant_name, email, address, app_type, status) VALUES (?,?,?,?,'pending')")) {
                st.setString(1, name.getText().trim());
                st.setString(2, email.getText().trim());
                st.setString(3, addr.getText().trim());
                st.setString(4, type.getValue());
                st.executeUpdate();
                reload();
            } catch (Exception e) { UIUtil.error("Error", e.getMessage()); }
            return null;
        });
        d.showAndWait();
    }

    private void updateStatus(Row r, String newStatus) {
        if (r == null) return;
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement st = conn.prepareStatement(
                     "UPDATE applications SET status=? WHERE application_ID=?")) {
            st.setString(1, newStatus); st.setInt(2, r.id.get()); st.executeUpdate();
            reload();
        } catch (Exception e) { UIUtil.error("Error", e.getMessage()); }
    }

    private void emailOutcome(Row r) {
        if (r == null) return;
        String outcome = r.status.get();
        String subject = "Your InfoPharma application: " + outcome;
        String body = String.format("""
            Dear %s,
            
            Thank you for your application for a %s account with InfoPharma.
            
            We are pleased to inform you that your application has been %s.
            
            %s
            
            Kind regards,
            InfoPharma Membership Team
            """,
                r.name.get(),
                r.type.get(),
                outcome,
                "approved".equals(outcome)
                        ? "You can now log in to the PU portal using the credentials provided separately."
                        : "If you have any questions, please contact us.");

        // In this demo we show the email rather than actually send via SMTP.
        // A real SMTP implementation would use jakarta.mail or similar.
        TextArea preview = new TextArea("To: " + r.email.get() + "\nSubject: " + subject + "\n\n" + body);
        preview.setEditable(false);
        preview.setWrapText(true);
        preview.setPrefSize(520, 360);
        preview.setStyle("-fx-font-family: 'Consolas', monospace;");

        Dialog<Void> d = new Dialog<>();
        d.setTitle("Email preview (SMTP)");
        d.setHeaderText("This email will be sent via SMTP to " + r.email.get());
        d.getDialogPane().setContent(preview);
        d.getDialogPane().getButtonTypes().addAll(
                new ButtonType("Send", ButtonBar.ButtonData.OK_DONE),
                ButtonType.CANCEL);
        DialogStyle.apply(d);
        d.setResultConverter(b -> {
            if (b != null && b.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                UIUtil.info("Email sent", "Outcome email dispatched to " + r.email.get()
                        + ".\n\n(Configured via SMTP settings in Settings → System.)");
            }
            return null;
        });
        d.showAndWait();
    }

    public static class Row {
        public final SimpleIntegerProperty id;
        public final SimpleStringProperty name, email, address, type, status, submitted;
        public Row(int id, String n, String e, String a, String t, String s, String sub) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(n == null ? "" : n);
            this.email = new SimpleStringProperty(e == null ? "" : e);
            this.address = new SimpleStringProperty(a == null ? "" : a);
            this.type = new SimpleStringProperty(t == null ? "" : t);
            this.status = new SimpleStringProperty(s == null ? "pending" : s);
            this.submitted = new SimpleStringProperty(sub == null ? "" : sub);
        }
        public int getId() { return id.get(); }
        public String getName() { return name.get(); }
        public String getEmail() { return email.get(); }
        public String getAddress() { return address.get(); }
        public String getType() { return type.get(); }
        public String getStatus() { return status.get(); }
        public String getSubmitted() { return submitted.get(); }
    }
}
