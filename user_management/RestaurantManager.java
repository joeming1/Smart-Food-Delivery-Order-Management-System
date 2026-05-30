import java.util.ArrayList;

public class RestaurantManager {
    private ArrayList<Restaurant> restaurants;

    public RestaurantManager() {
        this.restaurants = new ArrayList<>();
    }

    // Time Complexity: O(1)
    public void addRestaurant(Restaurant restaurant) {
        restaurants.add(restaurant);
        System.out.println("Restaurant added successfully!");
    }

    // Time Complexity: O(n)
    public boolean deleteRestaurant(String id) {
        for (int i = 0; i < restaurants.size(); i++) {
            if (restaurants.get(i).getId().equalsIgnoreCase(id)) {
                restaurants.remove(i);
                return true;
            }
        }
        return false;
    }

    // Time Complexity: O(n)
    public void displayAllRestaurants() {
        if (restaurants.isEmpty()) {
            System.out.println("No restaurants found.");
            return;
        }
        System.out.println("\n-----------------------------------------------------");
        System.out.format("| %-10s | %-20s | %-15s |\n", "Rest ID", "Name", "Cuisine");
        System.out.println("-----------------------------------------------------");
        for (Restaurant r : restaurants) {
            System.out.println(r);
        }
        System.out.println("-----------------------------------------------------");
    }

    public ArrayList<Restaurant> getRestaurants() {
        return restaurants;
    }
}