package com.prototype.ipossa.ui;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;


//formats monetary value to 2dp
public final class Formats {
    private Formats() {}

    public static String money(double v) {
        return String.format("£%.2f", v);
    }

    // Cell factory that formats a Number column to 2 dp
    public static <S> Callback<TableColumn<S, Number>, TableCell<S, Number>> moneyCell() {
        return col -> new TableCell<>() {
            @Override protected void updateItem(Number n, boolean empty) {
                super.updateItem(n, empty);
                setText(empty || n == null ? null : money(n.doubleValue()));
            }
        };
    }
}
