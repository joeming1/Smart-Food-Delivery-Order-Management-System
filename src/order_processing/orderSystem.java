package order_processing;

import search_and_data_retrieval_system.FoodItem;

public class orderSystem {
    private orderList orderSystemList;
    private final OrderBuilder currentBuilder;

    public orderSystem() {
        this.orderSystemList = new orderList();
        this.currentBuilder = new OrderBuilder();
    }

    public void startOrder(String customerName, String restaurantName) {
        currentBuilder.setCustomerName(customerName);
        currentBuilder.setRestaurantName(restaurantName);
    }

    public void addItem(FoodItem item) {
        currentBuilder.addItem(item);
    }

    public FoodItem undoLastItem() {
        return currentBuilder.undoLastItem();
    }

    public void clearCart() {
        currentBuilder.clear();
    }

    public String getCartContentString() {
        return currentBuilder.buildContentString();
    }

    public double getCartTotalPrice() {
        return currentBuilder.getTotalPrice();
    }

    public boolean isCartEmpty() {
        return currentBuilder.isEmpty();
    }

    public order confirmOrder() {
        order newOrder = currentBuilder.buildOrder();
        if (newOrder != null) {
            orderSystemList.addOrder(newOrder);
            currentBuilder.clear();
        }
        return newOrder;
    }

    public order undoLastOrder() {
        return orderSystemList.undoOrder();
    }

    public orderList getOrderList() {
        return orderSystemList;
    }

    public OrderBuilder getCurrentBuilder() {
        return currentBuilder;
    }
}
