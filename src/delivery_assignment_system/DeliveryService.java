package delivery_assignment_system;

import java.util.*;

public class DeliveryService {
    private final PriorityRiderQueue priorityQueue;
    private final List<Rider> riderList;
    private final Map<String, Rider> busyRiders; // riderId -> Rider

    public DeliveryService() {
        this.priorityQueue = new PriorityRiderQueue(PriorityRiderQueue.PriorityMode.SHORTEST_TIME);
        this.riderList = new ArrayList<>();
        this.busyRiders = new HashMap<>();
        loadSampleRiders();
    }

    public void addRider(Rider r) {
        riderList.add(r);
        priorityQueue.addRider(r);
    }

    public List<Rider> getAllRiders() {
        return Collections.unmodifiableList(riderList);
    }

    public List<Rider> getAvailableRiders() {
        return priorityQueue.snapshot();
    }

    public Collection<Rider> getBusyRiders() {
        return Collections.unmodifiableCollection(busyRiders.values());
    }

    public Rider peekBestRider() {
        return priorityQueue.peekBestRider();
    }

    public Rider assignBestRider() {
        Rider r = priorityQueue.assignBestRider();
        if (r != null) busyRiders.put(r.getRiderId(), r);
        return r;
    }

    public boolean completeDelivery(String riderId) {
        return completeDelivery(riderId, "UNKNOWN");
    }

    public boolean completeDelivery(String riderId, String newLocation) {
        Rider r = busyRiders.remove(riderId);
        if (r == null) return false;
        r.setCurrentLocationId(newLocation);
        priorityQueue.addRider(r);
        return true;
    }

    private void loadSampleRiders() {
        addRider(new Rider("R01", "Ali", 5.2, 15, "E_Customer"));
        addRider(new Rider("R02", "Bala", 1.5, 5, "D_Hub"));
        addRider(new Rider("R03", "Chong", 3.0, 10, "C_Restaurant"));
        addRider(new Rider("R04", "David", 8.0, 25, "G_Customer"));
    }
}
