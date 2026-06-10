package user_management;

public class Customer {
    private String id;
    private String name;
    private String phone;
    private String locationId; // optional location/node id for routing

    public Customer(String id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.locationId = "UNKNOWN";
    }

    public Customer(String id, String name, String phone, String locationId) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.locationId = locationId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getLocationId() {
        return locationId;
    }

    @Override
    public String toString() {
        return String.format("| %-10s | %-20s | %-15s | %-10s |", id, name, phone, locationId);
    }
}