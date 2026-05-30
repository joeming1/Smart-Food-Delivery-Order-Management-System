public class Restaurant {
    private String id;
    private String name;
    private String cuisineType;

    public Restaurant(String id, String name, String cuisineType) {
        this.id = id;
        this.name = name;
        this.cuisineType = cuisineType;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCuisineType() {
        return cuisineType;
    }

    @Override
    public String toString() {
        return String.format("| %-10s | %-20s | %-15s |", id, name, cuisineType);
    }
}