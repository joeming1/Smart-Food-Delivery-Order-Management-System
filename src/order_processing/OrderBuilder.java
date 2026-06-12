package order_processing;

import java.util.Stack;
import java.util.Map;
import java.util.LinkedHashMap;
import search_and_data_retrieval_system.FoodItem;

public class OrderBuilder {
    private String customerName;
    private String restaurantName;
    private final Stack<FoodItem> itemsStack;

    public OrderBuilder() {
        this.itemsStack = new Stack<>();
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setRestaurantName(String restaurantName) {
        if (this.restaurantName != null && !this.restaurantName.equalsIgnoreCase(restaurantName)) {
            clear();
        }
        this.restaurantName = restaurantName;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void addItem(FoodItem item) {
        if (item != null) {
            itemsStack.push(item);
        }
    }

    public FoodItem undoLastItem() {
        if (!itemsStack.isEmpty()) {
            return itemsStack.pop();
        }
        return null;
    }

    public void clear() {
        itemsStack.clear();
    }

    public boolean isEmpty() {
        return itemsStack.isEmpty();
    }

    public Stack<FoodItem> getItemsStack() {
        return itemsStack;
    }

    public double getTotalPrice() {
        double total = 0.0;
        for (FoodItem item : itemsStack) {
            total += item.getPrice();
        }
        return total;
    }

    public String buildContentString() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (FoodItem item : itemsStack) {
            counts.put(item.getName(), counts.getOrDefault(item.getName(), 0) + 1);
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(entry.getKey()).append(" x").append(entry.getValue());
        }
        return sb.toString();
    }

    public order buildOrder() {
        if (customerName == null || restaurantName == null || itemsStack.isEmpty()) {
            return null;
        }
        return new order(customerName, restaurantName, buildContentString());
    }
}
