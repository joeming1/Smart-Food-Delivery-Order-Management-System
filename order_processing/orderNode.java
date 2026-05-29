package order_processing;
public class orderNode {
    private order order;
    private orderNode next;

    public orderNode(order order) {
        this.order = order;
        this.next = null;
    }

    public order getOrder() {
        return order;
    }
    public orderNode getNext() {
        return next;
    }
    public void setNext(orderNode next) {
        this.next = next;
    }
    public void setOrder(order order) {
        this.order = order;
    }
}
