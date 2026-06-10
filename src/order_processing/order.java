package order_processing;

import java.time.LocalDateTime;

public class order {
    private int thisOrderID;
    private String customerName;
    private LocalDateTime orderDateTime;
    private String orderContent;
    private String restaurantName;
    static int orderID = 0;
    private String status = "PENDING"; // PENDING, CONFIRMED, DISPATCHED, DELIVERED
    private String assignedRiderId = null;
    private String sourceLocation = null;
    private String targetLocation = null;
    private double routeDistance = 0.0;

    public order(String customerName, String orderContent) {
        this.thisOrderID = ++orderID;
        this.customerName = customerName;
        this.orderDateTime = LocalDateTime.now();
        this.orderContent = orderContent;
        this.restaurantName = "UNKNOWN";
    }

    public order(String customerName, String restaurantName, String orderContent) {
        this.thisOrderID = ++orderID;
        this.customerName = customerName;
        this.orderDateTime = LocalDateTime.now();
        this.orderContent = orderContent;
        this.restaurantName = restaurantName;
    }

    public int getOrderID() { return thisOrderID; }

    public String getStatus() { return status; }

    public String getCustomerName() { return customerName; }

    public String getOrderContent() { return orderContent; }

    public String getRestaurantName() { return restaurantName; }

    public LocalDateTime getOrderDateTime() { return orderDateTime; }

    public void setStatus(String status) { this.status = status; }

    public void assignRider(String riderId, String source, String target, double distance) {
        this.assignedRiderId = riderId;
        this.sourceLocation = source;
        this.targetLocation = target;
        this.routeDistance = distance;
        this.status = "DISPATCHED";
    }

    public String getAssignedRiderId() { return assignedRiderId; }

    public String getSourceLocation() { return sourceLocation; }

    public void setSourceLocation(String sourceLocation) { this.sourceLocation = sourceLocation; }

    public String getTargetLocation() { return targetLocation; }

    public void setTargetLocation(String targetLocation) { this.targetLocation = targetLocation; }

    public double getRouteDistance() { return routeDistance; }

    public void displayOrder() {
        System.out.println("Order ID: " + thisOrderID);
        System.out.println("Customer Name: " + customerName);
        System.out.println("Restaurant: " + restaurantName);
        System.out.println("Order DateTime: " + orderDateTime);
        System.out.println("Order Content: " + orderContent);
        System.out.println("Status: " + status + (assignedRiderId != null ? " (Rider: " + assignedRiderId + ")" : ""));
        if (sourceLocation != null || targetLocation != null) {
            System.out.println("Route: " + sourceLocation + " -> " + targetLocation + " (" + routeDistance + ")");
        }
    }
}
