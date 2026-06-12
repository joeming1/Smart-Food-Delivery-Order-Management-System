package order_processing;

import java.util.LinkedList;
import java.util.Queue;

public class orderList {
    Queue<order> orderQueue = new LinkedList<>();

    public orderList() {
    }

    public void addOrder(order order) {
        orderQueue.offer(order);
    }

    public order undoOrder() {
        return ((LinkedList<order>) orderQueue).pollLast();
    }

    public java.util.List<order> getOrdersList() {
        return new java.util.ArrayList<>(orderQueue);
    }

    public void displayOrders() {
        if (orderQueue.isEmpty()) {
            System.out.println("No orders");
        } else {
            for (order order : orderQueue) {
                order.displayOrder();
            }
        }
    }
}
