import java.util.Comparator;
import java.util.PriorityQueue;

public class PriorityRiderQueue {
    
    private final PriorityQueue<Rider> riderQueue;

    public enum PriorityMode {
        SHORTEST_TIME,
        NEAREST_DISTANCE
    }

    public PriorityRiderQueue(PriorityMode mode) {
        Comparator<Rider> comparator;

        if (mode == PriorityMode.SHORTEST_TIME) {
            comparator = Comparator.comparingInt(Rider::getDeliveryTimeMins);
        } else {
            comparator = Comparator.comparingDouble(Rider::getDistanceKm);
        }

        riderQueue = new PriorityQueue<>(comparator);
    }

    public void addRider(Rider rider) {
        riderQueue.offer(rider); 
    }

    public void updateRiderPriority(Rider rider, double newDistance, int newTime) {
        if (riderQueue.remove(rider)) {
            rider.setDistanceKm(newDistance);
            rider.setDeliveryTimeMins(newTime);
            riderQueue.offer(rider);
        }
    }

    public Rider assignBestRider() {
        if (riderQueue.isEmpty()) return null;
        return riderQueue.poll(); 
    }

    public Rider peekBestRider() {
        if (riderQueue.isEmpty()) return null;
        return riderQueue.peek();
    }

    public boolean isEmpty() {
        return riderQueue.isEmpty();
    }

    // Displays the queue 
    public void displayAllRiders() {
        if (riderQueue.isEmpty()) {
            System.out.println("  (no riders currently in queue)");
            return;
        }
        System.out.println("  " + "-".repeat(50));
        System.out.format("  | %-5s | %-12s | %-9s | %-9s |%n", "ID", "Name", "Distance", "Time");
        System.out.println("  " + "-".repeat(50));
        for (Rider r : riderQueue) {
            System.out.println("  | " + r + " |");
        }
        System.out.println("  " + "-".repeat(50));
    }
}