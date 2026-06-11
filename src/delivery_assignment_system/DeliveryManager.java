package delivery_assignment_system;

import Route_Optimization_System.Graph;
import Route_Optimization_System.Node;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DeliveryManager {

    private final DeliveryService deliveryService;
    private final List<Rider> linearList;
    private final Scanner scanner;

    public DeliveryManager(Scanner scanner) {
        this.scanner = scanner;
        this.deliveryService = new DeliveryService();
        this.linearList = new ArrayList<>();
        loadSampleGraph();
        loadSampleData();
    }

    public void run() {
        int choice;
        do {
            printMenu();
            choice = readInt();
            switch (choice) {
                case 1 -> addRider();
                case 2 -> updateRiderLocation();
                case 3 -> dispatchBestRider();
                case 4 -> displayRiders();
                case 5 -> compareAssignmentSpeed();
                case 0 -> System.out.println("  Returning to main menu...");
                default -> System.out.println("  Invalid option. Please try again.");
            }
        } while (choice != 0);
    }

    private void addRider() {
        System.out.print("\n  Rider ID      : ");
        String id = scanner.nextLine().trim();
        System.out.print("  Name          : ");
        String name = scanner.nextLine().trim();
        System.out.print("  Location ID   : ");
        String location = scanner.nextLine().trim();

        Rider rider = new Rider(id, name, 0, 0, location.isEmpty() ? "UNKNOWN" : location);
        deliveryService.addRider(rider);
        linearList.add(rider);
        System.out.println("  ✓ Rider \"" + name + "\" added to the dispatch queue.");
    }

    private void updateRiderLocation() {
        System.out.print("\n  Enter Rider ID to update: ");
        String id = scanner.nextLine().trim();

        Rider target = null;
        for (Rider r : linearList) {
            if (r.getRiderId().equalsIgnoreCase(id)) {
                target = r;
                break;
            }
        }

        if (target != null) {
            System.out.print("  New Location ID : ");
            String newLocation = scanner.nextLine().trim();
            target.setCurrentLocationId(newLocation.isEmpty() ? "UNKNOWN" : newLocation);
            target.setDistanceKm(0);
            target.setDeliveryTimeMins(0);
            System.out.println("  ✓ Location updated for Rider: " + target.getName());
        } else {
            System.out.println("  Rider not found in the active queue.");
        }
    }

    private void dispatchBestRider() {
        System.out.print("\n  Restaurant Location ID : ");
        String restaurantId = scanner.nextLine().trim();
        if (restaurantId.isEmpty()) {
            System.out.println("  Restaurant location is required.");
            return;
        }

        System.out.println("  [Assigning nearest rider via route optimization]");
        deliveryService.updateRiderPrioritiesForRestaurant(restaurantId);
        Rider best = deliveryService.assignBestRiderForRestaurant(restaurantId);
        if (best != null) {
            System.out.printf("  ✓ Dispatched: %s (%.1f min from %s to %s)%n",
                    best, best.getDeliveryTimeMins(), best.getCurrentLocationId(), restaurantId);
            linearList.remove(best);
        } else {
            System.out.println("  No riders available or no route to restaurant.");
        }
    }

    private void displayRiders() {
        System.out.println("\n  === Active Delivery Riders ===");
        System.out.print("  Show travel time to restaurant (location ID, blank to skip): ");
        String restaurantId = scanner.nextLine().trim();
        if (!restaurantId.isEmpty()) {
            deliveryService.updateRiderPrioritiesForRestaurant(restaurantId);
        }
        if (deliveryService.getAvailableRiders().isEmpty()) {
            System.out.println("  (no riders currently in queue)");
            return;
        }
        System.out.println("  " + "-".repeat(62));
        System.out.format("  | %-5s | %-12s | %-9s | %-9s | %-12s |%n", "ID", "Name", "Mins", "Location", "State");
        System.out.println("  " + "-".repeat(62));
        for (Rider r : deliveryService.getAvailableRiders()) {
            System.out.println("  | " + r + " | Available |");
        }
        for (Rider r : deliveryService.getBusyRiders()) {
            System.out.printf("  | %-5s | %-12s | %-9s | %-12s | Busy |%n",
                    r.getRiderId(), r.getName(), "-", r.getCurrentLocationId());
        }
        System.out.println("  " + "-".repeat(62));
    }

    private void compareAssignmentSpeed() {
        System.out.println("\n  === Priority Queue vs Linear Search Speed Test ===");
        if (linearList.isEmpty()) {
            System.out.println("  Please add riders first to run comparison.");
            return;
        }

        String restaurantId = "C_Restaurant";
        deliveryService.updateRiderPrioritiesForRestaurant(restaurantId);

        long t1 = System.nanoTime();
        Rider heapBest = deliveryService.peekBestRiderForRestaurant(restaurantId);
        long t2 = System.nanoTime();
        System.out.printf("  Route-aware heap peek() : %5d ns  → O(1)%n", (t2 - t1));

        t1 = System.nanoTime();
        Rider linearBest = linearList.get(0);
        for (Rider r : linearList) {
            double travel = deliveryService.travelTimeTo(r.getCurrentLocationId(), restaurantId);
            double bestTravel = deliveryService.travelTimeTo(linearBest.getCurrentLocationId(), restaurantId);
            if (travel < bestTravel) {
                linearBest = r;
            }
        }
        t2 = System.nanoTime();
        System.out.printf("  Linear route search     : %5d ns  → O(n) where n=%d%n", (t2 - t1), linearList.size());

        System.out.println("\n  Heap found    : " + (heapBest != null ? heapBest.getName() : "none"));
        System.out.println("  Linear found  : " + (linearBest != null ? linearBest.getName() : "none"));
        System.out.println("\n  Distances are computed dynamically from each rider to the restaurant.");
    }

    private void loadSampleGraph() {
        Graph graph = new Graph();
        String[] locations = {
            "A_Restaurant", "B_Restaurant", "C_Restaurant",
            "D_Hub", "E_Customer", "F_Customer", "G_Customer"
        };
        for (String location : locations) {
            graph.addNode(new Node(location));
        }
        addRoute(graph, "A_Restaurant", "D_Hub", 4);
        addRoute(graph, "B_Restaurant", "D_Hub", 3);
        addRoute(graph, "C_Restaurant", "D_Hub", 5);
        addRoute(graph, "D_Hub", "E_Customer", 2);
        addRoute(graph, "D_Hub", "F_Customer", 4);
        addRoute(graph, "D_Hub", "G_Customer", 6);
        addRoute(graph, "A_Restaurant", "E_Customer", 7);
        addRoute(graph, "B_Restaurant", "F_Customer", 6);
        deliveryService.setRouteGraph(graph);
    }

    private void addRoute(Graph graph, String from, String to, double weight) {
        graph.addEdge(new Node(from), new Node(to), weight);
        graph.addEdge(new Node(to), new Node(from), weight);
    }

    private void loadSampleData() {
        linearList.addAll(deliveryService.getAllRiders());
    }

    private void printMenu() {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║        Delivery Assignment System        ║");
        System.out.println("╠══════════════════════════════════════════╣");
        System.out.println("║  1. Add new rider to queue               ║");
        System.out.println("║  2. Update rider location                ║");
        System.out.println("║  3. Dispatch nearest rider to restaurant ║");
        System.out.println("║  4. Display all active riders            ║");
        System.out.println("║  5. O(log n) vs O(n) Speed Demo          ║");
        System.out.println("║  0. Back                                 ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.print("  Choice: ");
    }

    private int readInt() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        new DeliveryManager(sc).run();
        sc.close();
    }
}
