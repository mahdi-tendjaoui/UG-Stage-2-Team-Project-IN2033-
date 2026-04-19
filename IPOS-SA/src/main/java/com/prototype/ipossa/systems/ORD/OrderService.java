package com.prototype.ipossa.systems.ORD;

import java.util.ArrayList;
import java.util.List;

/**
 * The class Order service.
 */
public class OrderService {
    private List<Order> orders;

    /**
     * Instantiates a new Order service.
     */
    public OrderService() {
        this.orders = new ArrayList<>();

    }

    /**
     * Order exists boolean.
     *
     * @param orderID the order id
     * @return the boolean
     */
    public boolean orderExists(String orderID) {
        return searchOrder(orderID) != null;
    }

    /**
     * Gets merchant orders.
     *
     * @param merchantID the merchant id
     * @return the merchant orders
     */
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

    /**
     * Gets incomplete orders.
     *
     * @return the incomplete orders
     */
    public List<Order> getIncompleteOrders() {
        List<Order> inompleteOrders = new ArrayList<>();

        for (Order order : orders) {
            if (order.getStatus() != Order.OrderStatus.DELIVERED) {
                inompleteOrders.add(order);
            }
        }
        return inompleteOrders;
    }

    /**
     * Update order status.
     *
     * @param orderID   the order id
     * @param newStatus the new status
     * @return the boolean
     */
    public boolean updateOrderStatus(String orderID, Order.OrderStatus newStatus) {
        Order order = searchOrder(orderID);

        if(order == null || newStatus == null) {
            return false;
        }
        Order.OrderStatus currentStatus = order.getStatus();

        if (
            (currentStatus == Order.OrderStatus.ACCEPTED &&
            newStatus == Order.OrderStatus.READY_TO_DISPATCH) ||
            (currentStatus == Order.OrderStatus.READY_TO_DISPATCH &&
            newStatus == Order.OrderStatus.DISPATCHED) ||
            (currentStatus == Order.OrderStatus.DISPATCHED &&
            newStatus == Order.OrderStatus.DELIVERED)
        ) {
        order.setStatus(newStatus);
        return true;
        }
        return false;
    }

    /**
     * Search order.
     *
     * @param orderID the order id
     * @return the order
     */
    public Order searchOrder(String orderID) {
        for (Order order : orders) {
            if (order.getOrderID().equals(orderID)) {
                return order;
            }
        }
        return null;
    }

    /**
     * Remove order boolean.
     *
     * @param orderID the order id
     * @return the boolean
     */
    public boolean removeOrder(String orderID) {
        Order order = searchOrder(orderID);
        if (order != null) {
            orders.remove(order);
            return true;
        }
        return false;
    }

    /**
     * Add order boolean.
     *
     * @param order the order
     * @return the boolean
     */
    public boolean addOrder(Order order) {
        if (order == null ||orderExists(order.getOrderID())) {
            return false;
        }
        else {
            orders.add(order);
            return true;
        }
    }
}
