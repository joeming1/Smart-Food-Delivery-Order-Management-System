package search_and_data_retrieval_system;

public class FoodItem {
    private String name;
    private String category;
    private double price;
    private String restaurantName;

    public FoodItem(String name, String category, double price, String restaurantName) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.restaurantName = restaurantName;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    @Override
    public String toString() {
        return String.format("%-20s | %-12s | RM%-6.2f | %s", name, category, price, restaurantName);
    }
}
