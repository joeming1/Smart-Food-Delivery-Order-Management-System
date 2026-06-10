package delivery_assignment_system;

public class Rider {
    private final String riderId;
    private final String name;
    private double distanceKm; 
    private int deliveryTimeMins; 
    private String currentLocationId;

    public Rider(String riderId, String name, double distanceKm, int deliveryTimeMins) {
        this.riderId = riderId;
        this.name = name;
        this.distanceKm = distanceKm;
        this.deliveryTimeMins = deliveryTimeMins;
        this.currentLocationId = "UNKNOWN";
    }

    public Rider(String riderId, String name, double distanceKm, int deliveryTimeMins, String currentLocationId) {
        this.riderId = riderId;
        this.name = name;
        this.distanceKm = distanceKm;
        this.deliveryTimeMins = deliveryTimeMins;
        this.currentLocationId = currentLocationId;
    }

    // Getters
    public String getRiderId() { return riderId; }
    public String getName() { return name; }
    public double getDistanceKm() { return distanceKm; }
    public int getDeliveryTimeMins() { return deliveryTimeMins; }
    public String getCurrentLocationId() { return currentLocationId; }

    // Setters
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
    public void setDeliveryTimeMins(int deliveryTimeMins) { this.deliveryTimeMins = deliveryTimeMins; }
    public void setCurrentLocationId(String currentLocationId) { this.currentLocationId = currentLocationId; }

    @Override
    public String toString() {
        return String.format("%-5s | %-12s | %-6.1f km | %-4d mins | %-12s", 
                              riderId, name, distanceKm, deliveryTimeMins, currentLocationId);
    }
}