package delivery_assignment_system;

import Route_Optimization_System.DijkstraAlgorithm;
import Route_Optimization_System.Graph;
import java.util.*;

public class DeliveryService {
    private final PriorityRiderQueue priorityQueue;
    private final List<Rider> riderList;
    private final Map<String, Rider> busyRiders; // riderId -> Rider
    private Graph routeGraph;

    public DeliveryService() {
        this.priorityQueue = new PriorityRiderQueue(PriorityRiderQueue.PriorityMode.SHORTEST_TIME);
        this.riderList = new ArrayList<>();
        this.busyRiders = new HashMap<>();
        loadSampleRiders();
    }

    public void setRouteGraph(Graph routeGraph) {
        this.routeGraph = routeGraph;
    }

    public Graph getRouteGraph() {
        return routeGraph;
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

    public Rider peekBestRiderForRestaurant(String restaurantLocationId) {
        return findBestRiderForRestaurant(restaurantLocationId, false);
    }

    public Rider assignBestRider() {
        Rider r = priorityQueue.assignBestRider();
        if (r != null) busyRiders.put(r.getRiderId(), r);
        return r;
    }

    public Rider assignBestRiderForRestaurant(String restaurantLocationId) {
        return findBestRiderForRestaurant(restaurantLocationId, true);
    }

    public void updateRiderPrioritiesForRestaurant(String restaurantLocationId) {
        if (restaurantLocationId == null || restaurantLocationId.isBlank()) return;
        for (Rider rider : priorityQueue.snapshot()) {
            applyTravelMetrics(rider, restaurantLocationId);
        }
    }

    public double travelTimeTo(String fromLocationId, String toLocationId) {
        if (routeGraph == null || fromLocationId == null || toLocationId == null) {
            return Double.POSITIVE_INFINITY;
        }
        if (fromLocationId.equals("UNKNOWN") || toLocationId.equals("UNKNOWN")) {
            return Double.POSITIVE_INFINITY;
        }
        return DijkstraAlgorithm.shortestPath(routeGraph, fromLocationId, toLocationId).getDistance();
    }

    public boolean completeDelivery(String riderId) {
        return completeDelivery(riderId, "UNKNOWN");
    }

    public boolean completeDelivery(String riderId, String newLocation) {
        Rider r = busyRiders.remove(riderId);
        if (r == null) return false;
        r.setCurrentLocationId(newLocation);
        r.setDistanceKm(0);
        r.setDeliveryTimeMins(0);
        priorityQueue.addRider(r);
        return true;
    }

    private Rider findBestRiderForRestaurant(String restaurantLocationId, boolean assign) {
        if (restaurantLocationId == null || restaurantLocationId.isBlank()) {
            return assign ? assignBestRider() : peekBestRider();
        }

        List<Rider> available = priorityQueue.snapshot();
        if (available.isEmpty()) return null;

        Rider best = null;
        double bestTravelTime = Double.POSITIVE_INFINITY;

        for (Rider rider : available) {
            double travelTime = applyTravelMetrics(rider, restaurantLocationId);
            if (travelTime < bestTravelTime) {
                bestTravelTime = travelTime;
                best = rider;
            }
        }

        if (best == null || bestTravelTime == Double.POSITIVE_INFINITY) {
            return null;
        }

        if (assign) {
            priorityQueue.remove(best);
            busyRiders.put(best.getRiderId(), best);
        }
        return best;
    }

    private double applyTravelMetrics(Rider rider, String restaurantLocationId) {
        String riderLocation = rider.getCurrentLocationId();
        double travelTime = travelTimeTo(riderLocation, restaurantLocationId);
        if (travelTime != Double.POSITIVE_INFINITY) {
            rider.setDistanceKm(travelTime);
            rider.setDeliveryTimeMins((int) Math.ceil(travelTime));
            priorityQueue.updateRiderPriority(rider, travelTime, (int) Math.ceil(travelTime));
        }
        return travelTime;
    }

    private void loadSampleRiders() {
        addRider(new Rider("R01", "Ali", 0, 0, "E_Customer"));
        addRider(new Rider("R02", "Bala", 0, 0, "D_Hub"));
        addRider(new Rider("R03", "Chong", 0, 0, "C_Restaurant"));
        addRider(new Rider("R04", "David", 0, 0, "G_Customer"));
    }
}
