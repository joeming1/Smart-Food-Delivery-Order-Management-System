public class Rider {
    private String riderId;
    private String name;
    private double distanceKm; 
    private int deliveryTimeMins; 

    public Rider(String riderId, String name, double distanceKm, int deliveryTimeMins) {
        this.riderId = riderId;
        this.name = name;
        this.distanceKm = distanceKm;
        this.deliveryTimeMins = deliveryTimeMins;
    }

    // Getters
    public String getRiderId() { return riderId; }
    public String getName() { return name; }
    public double getDistanceKm() { return distanceKm; }
    public int getDeliveryTimeMins() { return deliveryTimeMins; }

    // Setters
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
    public void setDeliveryTimeMins(int deliveryTimeMins) { this.deliveryTimeMins = deliveryTimeMins; }

    @Override
    public String toString() {
        return String.format("%-5s | %-12s | %-6.1f km | %-4d mins", 
                              riderId, name, distanceKm, deliveryTimeMins);
    }
}