package com.prototype.ipossa.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

import java.util.Optional;

public class UIUtil {

    public static void info(String title, String msg) { show(Alert.AlertType.INFORMATION, title, msg); }
    public static void warn(String title, String msg) { show(Alert.AlertType.WARNING, title, msg); }
    public static void error(String title, String msg) { show(Alert.AlertType.ERROR, title, msg); }

    public static boolean confirm(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.OK, ButtonType.CANCEL);
        a.setTitle(title);
        a.setHeaderText(null);
        styleAlert(a);
        Optional<ButtonType> r = a.showAndWait();
        return r.isPresent() && r.get() == ButtonType.OK;
    }

    private static void show(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type, msg, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        styleAlert(a);
        a.showAndWait();
    }

    private static void styleAlert(Alert a) {
        var dp = a.getDialogPane();
        dp.getScene().getStylesheets().add("data:text/css;base64," +
                java.util.Base64.getEncoder().encodeToString(dialogCss().getBytes()));
        dp.getScene().getRoot().getStyleClass().add(
                ThemeManager.get().isDark() ? "theme-dark" : "theme-light");
        dp.setMinHeight(Region.USE_PREF_SIZE);
    }

    private static String dialogCss() {

        return """
            .root.theme-light { -bg:#f4f6f9; -panel:#ffffff; -panel-2:#f9fafb; -text:#1a1d21; -text-dim:#5c6470; -border:#e3e7ec; -accent:#bbfa34; -accent-text:#000000; }
            .root.theme-dark  { -bg:#000000; -panel:#000000; -panel-2:#22262e; -text:#e7eaf0; -text-dim:#9aa3b2; -border:#2d323b; -accent:#bbfa34; -accent-text:#000000; }
            .dialog-pane { -fx-background-color:-panel; }
            .dialog-pane .label, .dialog-pane .content.label, .dialog-pane .header-panel .label { -fx-text-fill:-text; }
            .dialog-pane .header-panel { -fx-background-color:-panel-2; }
            .dialog-pane .button { -fx-background-color:-panel-2; -fx-text-fill:-text; -fx-border-color:-border; -fx-border-radius:6; -fx-background-radius:6; -fx-padding:6 14 6 14; }
            .dialog-pane .button:default { -fx-background-color:-accent; -fx-text-fill:-accent-text; -fx-border-color:-accent; }
            """;
    }

    public static Label h1(String s) { Label l = new Label(s); l.getStyleClass().add("h1"); return l; }
    public static Label h2(String s) { Label l = new Label(s); l.getStyleClass().add("h2"); return l; }
    public static Label dim(String s) { Label l = new Label(s); l.getStyleClass().add("dim"); return l; }
}
