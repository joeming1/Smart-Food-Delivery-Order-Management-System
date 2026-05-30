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

    public void undoOrder() {
        ((LinkedList<order>) orderQueue).pollLast();
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
