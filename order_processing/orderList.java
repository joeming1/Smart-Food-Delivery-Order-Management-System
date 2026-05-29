package order_processing;
public class orderList {
    private orderNode head;
    private orderNode tail;
    private int size;

    public orderList() {
        head = null;
        tail = null;
        size = 0;
    }

    public void addOrder(order order) {
        orderNode newNode = new orderNode(order);
        if (head == null) {
            head = newNode;
            tail = newNode;
            size++;
        } else {
            tail.setNext(newNode);
            tail = newNode;
            size++;
        }
    }

    public void undoOrder() {
        if (size == 0) {
            System.out.println("No order to undo");
        } else {
            if (size == 1) {
                head = null;
                tail = null;
                size--;
            } else {
                orderNode current = head;
                while (current.getNext() != tail) {
                    current = current.getNext();
                }
                current.setNext(null);
                tail = current;
                size--;
            }
        }
    }

    public void displayOrders() {
        if (size == 0) {
            System.out.println("No orders");
        } else {
            orderNode current = head;
            while (current != null) {
                current.getOrder().displayOrder();
                current = current.getNext();
            }
        }
    }
}
