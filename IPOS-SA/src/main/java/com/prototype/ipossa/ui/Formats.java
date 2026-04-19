package com.prototype.ipossa.ui;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 * The type Formats.
 */
public final class Formats {
    private Formats() {}

    /**
     * Money string.
     *
     * @param v the v
     * @return the string
     */
    public static String money(double v) {
        return String.format("£%.2f", v);
    }

    /**
     * Money cell callback.
     *
     * @param <S> the type parameter
     * @return the callback
     */
    public static <S> Callback<TableColumn<S, Number>, TableCell<S, Number>> moneyCell() {
        return col -> new TableCell<>() {
            @Override protected void updateItem(Number n, boolean empty) {
                super.updateItem(n, empty);
                setText(empty || n == null ? null : money(n.doubleValue()));
            }
        };
    }
}
