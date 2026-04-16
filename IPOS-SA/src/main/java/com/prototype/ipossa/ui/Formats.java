package com.prototype.ipossa.ui;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 * Centralised number / currency formatting.
 *
 * Every monetary value displayed in the UI must be rounded to exactly two
 * decimal places (per the brief's amendment: prices were showing 1 dp but
 * should be 2 dp).
 */
public final class Formats {
    private Formats() {}

    /** "1234.50" → "£1234.50". */
    public static String money(double v) {
        return String.format("£%.2f", v);
    }

    /** Cell factory that formats a Number column to 2 dp (no £ sign). */
    public static <S> Callback<TableColumn<S, Number>, TableCell<S, Number>> moneyCell() {
        return col -> new TableCell<>() {
            @Override protected void updateItem(Number n, boolean empty) {
                super.updateItem(n, empty);
                setText(empty || n == null ? null : money(n.doubleValue()));
            }
        };
    }
}
