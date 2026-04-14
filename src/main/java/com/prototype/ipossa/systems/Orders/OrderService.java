package com.prototype.ipossa.systems.Orders;

import java.util.ArrayList;
import java.util.List;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.prototype.ipossa.MyJDBC;

public class OrderService {
    private List<Order> orders = new ArrayList<>();

    public OrderService() {


    }

    public boolean orderExists(String orderID) {
        return searchOrder(orderID) != null;
    }

    public List<Order> getMerchantOrders(String merchantID) {
        List<Order> merchantOrders = new ArrayList<>();

        if (merchantID != null) {
            for (Order order : orders) {
                if (order.getMerchantID().equals(merchantID)) {
                    merchantOrders.add(order);
                }
            }
        }
        return merchantOrders;
    }

    public List<Order> getIncompleteOrders() {
        List<Order> inompleteOrders = new ArrayList<>();

        for (Order order : orders) {
            if (order.getStatus() != Order.OrderStatus.DELIVERED) {
                inompleteOrders.add(order);
            }
        }
        return inompleteOrders;
    }

    public boolean updateOrderStatus(String orderID, Order.OrderStatus newStatus) {
        Order order = searchOrder(orderID);

        if(order == null || newStatus == null) {
            return false;
        }
        Order.OrderStatus currentStatus = order.getStatus();

        if (
            (currentStatus == Order.OrderStatus.ACCEPTED &&
            newStatus == Order.OrderStatus.BEING_PROCESSED) ||
            (currentStatus == Order.OrderStatus.BEING_PROCESSED &&
            newStatus == Order.OrderStatus.DISPATCHED) ||
            (currentStatus == Order.OrderStatus.DISPATCHED &&
            newStatus == Order.OrderStatus.DELIVERED)
        ) {
        order.setStatus(newStatus);
        return true;
        }
        return false;
    }

    public Order searchOrder(String orderID) {
        for (Order order : orders) {
            if (order.getOrderID().equals(orderID)) {
                return order;
            }
        }
        return null;
    }

    public boolean removeOrder(String orderID) {
        Order order = searchOrder(orderID);
        if (order != null) {
            orders.remove(order);
            return true;
        }
        return false;
    }

    public void addOrder(String orderID, String merchantID, String merchantName,
                            double totalAmount, Order.OrderStatus status, String invoiceID, String orderDate,
                            List<OrderItem> items) {
        orders.add(new Order(orderID, merchantID, merchantName, totalAmount, invoiceID, orderDate));
    }
}
