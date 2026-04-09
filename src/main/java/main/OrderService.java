package main;

import java.util.ArrayList;
import java.util.List;

public class OrderService {
    private List<Order> orders;
    public OrderService() {

    }
//    public Order searchOrder(String orderID) {
//
//    }
    public void addOrder(Order order) {
        orders.add(order);
    }
}
