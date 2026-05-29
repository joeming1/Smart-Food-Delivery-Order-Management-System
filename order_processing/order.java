package order_processing;
import java.time.LocalDateTime;

public class order {
    private int thisOrderID;
    private String customerName;
    private LocalDateTime orderDateTime;
    private String orderContent;
    static int orderID = 0;

    public order(String customerName, String orderContent) {
        this.thisOrderID = ++orderID;
        this.customerName = customerName;
        this.orderDateTime = LocalDateTime.now();
        this.orderContent = orderContent;
    }

    public void displayOrder() {
        System.out.println("Order ID: " + thisOrderID);
        System.out.println("Customer Name: " + customerName);
        System.out.println("Order DateTime: " + orderDateTime);
        System.out.println("Order Content: " + orderContent);
    }
}
