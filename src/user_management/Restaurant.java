package user_management;

public class Restaurant {
    private String id;
    private String name;
    private String cuisineType;
    private String locationId;

    public Restaurant(String id, String name, String cuisineType) {
        this.id = id;
        this.name = name;
        this.cuisineType = cuisineType;
        this.locationId = "UNKNOWN";
    }

    public Restaurant(String id, String name, String cuisineType, String locationId) {
        this.id = id;
        this.name = name;
        this.cuisineType = cuisineType;
        this.locationId = locationId;
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

    public String getLocationId() {
        return locationId;
    }

    @Override
    public String toString() {
        return String.format("| %-10s | %-20s | %-15s | %-10s |", id, name, cuisineType, locationId);
    }
}