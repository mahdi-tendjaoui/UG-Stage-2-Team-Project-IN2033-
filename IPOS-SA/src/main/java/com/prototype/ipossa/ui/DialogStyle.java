package com.prototype.ipossa.ui;

import javafx.scene.control.Dialog;

public class DialogStyle {

    public static void apply(Dialog<?> d) {
        var dp = d.getDialogPane();
        dp.getScene().getStylesheets().add("data:text/css;base64," +
                java.util.Base64.getEncoder().encodeToString(css().getBytes()));
        dp.getScene().getRoot().getStyleClass().add(
                ThemeManager.get().isDark() ? "theme-dark" : "theme-light");

        dp.setGraphic(null);
    }

    public static String css() {
        return """
            .root.theme-light { -bg:#f4f6f9; -panel:#ffffff; -panel-2:#f3f5f8; -text:#0e1116; -text-dim:#5c6470; -border:#e3e7ec; -accent:#bbfa34; -accent-text:#0a0c10; -danger:#dc2626; -warn:#d97706; -success:#16a34a; }
            .root.theme-dark  { -bg:#000000; -panel:#0a0a0a; -panel-2:#121212; -text:#f0f0f0; -text-dim:#9aa0a8; -border:#1f1f1f; -accent:#bbfa34; -accent-text:#0a0c10; -danger:#ef4444; -warn:#f59e0b; -success:#22c55e; }
            .dialog-pane { -fx-background-color:-panel; }
            .dialog-pane * { -fx-text-fill:-text; }
            .dialog-pane .label, .dialog-pane .content.label, .dialog-pane .header-panel .label { -fx-text-fill:-text; }
            .dialog-pane .header-panel { -fx-background-color:-panel-2; }
            .dialog-pane .button { -fx-background-color:-panel-2; -fx-text-fill:-text; -fx-border-color:-border; -fx-border-radius:8; -fx-background-radius:8; -fx-padding:7 14 7 14; }
            .dialog-pane .button:default { -fx-background-color:-accent; -fx-text-fill:-accent-text; -fx-border-color:-accent; -fx-font-weight:bold; }
            .dialog-pane .text-field, .dialog-pane .password-field, .dialog-pane .combo-box, .dialog-pane .text-area, .dialog-pane .date-picker {
                -fx-background-color:-panel; -fx-border-color:-border; -fx-border-radius:8; -fx-background-radius:8;
                -fx-text-fill:-text; -fx-prompt-text-fill:-text-dim; -fx-padding:7 11 7 11;
                -fx-control-inner-background:-panel;
            }
            .dialog-pane .text-area .content { -fx-background-color:-panel; }
            .dialog-pane .combo-box .list-cell { -fx-text-fill:-text; -fx-background-color:-panel; }
            .dialog-pane .combo-box-popup .list-view { -fx-background-color:-panel; -fx-border-color:-border; }
            .dialog-pane .combo-box-popup .list-view .list-cell { -fx-text-fill:-text; -fx-background-color:-panel; }
            .dialog-pane .combo-box-popup .list-view .list-cell:hover,
            .dialog-pane .combo-box-popup .list-view .list-cell:filled:selected { -fx-background-color:-accent; -fx-text-fill:-accent-text; }
            .dialog-pane .table-view { -fx-background-color:-panel; -fx-border-color:-border; }
            .dialog-pane .table-view .column-header, .dialog-pane .table-view .filler { -fx-background-color:-panel-2; }
            .dialog-pane .table-view .column-header .label { -fx-text-fill:-text; -fx-font-weight:bold; }
            .dialog-pane .table-row-cell { -fx-background-color:-panel; }
            .dialog-pane .table-row-cell:odd { -fx-background-color: derive(-panel,-2%); }
            .dialog-pane .table-cell { -fx-text-fill:-text; }
            .dialog-pane .table-row-cell:selected { -fx-background-color:-accent; }
            .dialog-pane .table-row-cell:selected .table-cell { -fx-text-fill:-accent-text; }
            .dialog-pane .scroll-pane, .dialog-pane .scroll-pane > .viewport { -fx-background-color:-panel; }
            """;
    }
}
