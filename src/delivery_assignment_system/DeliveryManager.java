import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DeliveryManager {

    private final PriorityRiderQueue priorityQueue;
    private final List<Rider> linearList; 
    private final Scanner scanner;

    public DeliveryManager(Scanner scanner) {
        this.scanner = scanner;

        this.priorityQueue = new PriorityRiderQueue(PriorityRiderQueue.PriorityMode.SHORTEST_TIME);
        this.linearList = new ArrayList<>();
        loadSampleData();
    }

    public void run() {
        int choice;
        do {
            printMenu();
            choice = readInt();
            switch (choice) {
                case 1 -> addRider();
                case 2 -> updateRider();
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
        System.out.print("  Distance (km) : ");
        double distance = readDouble();
        System.out.print("  Time (mins)   : ");
        int time = readInt();

        Rider rider = new Rider(id, name, distance, time);
        priorityQueue.addRider(rider);
        linearList.add(rider);
        System.out.println("  ✓ Rider \"" + name + "\" added to the dispatch queue.");
    }

    private void updateRider() {
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
            System.out.print("  New Distance (km) : ");
            double newDist = readDouble();
            System.out.print("  New Time (mins)   : ");
            int newTime = readInt();
            
            priorityQueue.updateRiderPriority(target, newDist, newTime);
            System.out.println("  ✓ Priority updated for Rider: " + target.getName());
        } else {
            System.out.println("  Rider not found in the active queue.");
        }
    }

    private void dispatchBestRider() {
        System.out.println("\n  [Assigning Best Rider via Min Heap]");
        Rider best = priorityQueue.assignBestRider();
        if (best != null) {
            System.out.println("  ✓ Dispatched: " + best);
            linearList.remove(best);
        } else {
            System.out.println("  No riders available in the queue.");
        }
    }

    private void displayRiders() {
        System.out.println("\n  === Active Delivery Riders ===");
        priorityQueue.displayAllRiders();
    }

    private void compareAssignmentSpeed() {
        System.out.println("\n  === Priority Queue vs Linear Search Speed Test ===");
        if (linearList.isEmpty()) {
            System.out.println("  Please add riders first to run comparison.");
            return;
        }

        // 1. Min Heap Speed Test (O(1) to peek the minimum value)
        long t1 = System.nanoTime();
        Rider heapBest = priorityQueue.peekBestRider();
        long t2 = System.nanoTime();
        System.out.printf("  Min Heap peek() : %5d ns  → O(1)%n", (t2 - t1));

        // 2. Linear Search Speed Test (O(n) scanning the entire list)
        t1 = System.nanoTime();
        Rider linearBest = linearList.get(0);
        for (Rider r : linearList) {
            if (r.getDeliveryTimeMins() < linearBest.getDeliveryTimeMins()) {
                linearBest = r;
            }
        }
        t2 = System.nanoTime();
        System.out.printf("  Linear search   : %5d ns  → O(n) where n=%d%n", (t2 - t1), linearList.size());

        System.out.println("\n  Min Heap found  : " + (heapBest != null ? heapBest.getName() : "none"));
        System.out.println("  Linear found    : " + (linearBest != null ? linearBest.getName() : "none"));
        System.out.println("\n  As 'n' grows, the Min Heap stays instantly fast at O(1),");
        System.out.println("  while Linear Search degrades linearly.");
    }

    private void loadSampleData() {
        Rider[] sampleRiders = {
            new Rider("R01", "Ali", 5.2, 15),
            new Rider("R02", "Bala", 1.5, 5),
            new Rider("R03", "Chong", 3.0, 10),
            new Rider("R04", "David", 8.0, 25)
        };
        for (Rider r : sampleRiders) {
            priorityQueue.addRider(r);
            linearList.add(r);
        }
    }

    private void printMenu() {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║  Member 3 — Delivery Assignment System   ║");
        System.out.println("╠══════════════════════════════════════════╣");
        System.out.println("║  1. Add new rider to queue               ║");
        System.out.println("║  2. Update rider location/priority       ║");
        System.out.println("║  3. Dispatch best available rider        ║");
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

    private double readDouble() {
        try {
            return Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("  Invalid number, defaulting to 0.0");
            return 0.0;
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        new DeliveryManager(sc).run();
        sc.close();
    }
}
