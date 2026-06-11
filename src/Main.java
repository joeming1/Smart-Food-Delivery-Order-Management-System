import Route_Optimization_System.*;
import delivery_assignment_system.*;
import order_processing.*;
import search_and_data_retrieval_system.*;
import user_management.*;

import java.util.*;

public class Main {

    private static final UserManager userManager = new UserManager();
    private static final RestaurantManager restaurantManager = new RestaurantManager();
    private static final DeliveryService deliveryService = new DeliveryService();
    private static final Graph routeGraph = new Graph();
    private static final FoodBST foodBST = new FoodBST();
    private static final FoodAVLTree foodAVLTree = new FoodAVLTree();
    private static final DataStorage dataStorage = new DataStorage();

    /** Location ID -> type ("Restaurant" | "Customer" | "Hub" | "Other") */
    private static final Map<String, String> locationTypes = new LinkedHashMap<>();

    /** Master order list (mirrors GUI orders list) */
    private static final List<order> orders = new ArrayList<>();

    private static final Scanner sc = new Scanner(System.in);

    // -------------------------------------------------------------------------
    // Entry point
    // -------------------------------------------------------------------------
    public static void main(String[] args) {
        deliveryService.setRouteGraph(routeGraph);
        seedDemoData();

        int choice;
        do {
            printBanner();
            choice = readInt();
            switch (choice) {
                case 1 -> customerMenu();
                case 2 -> restaurantMenu();
                case 3 -> riderMenu();
                case 4 -> orderMenu();
                case 5 -> dispatchMenu();
                case 6 -> routeAndLocationMenu();
                case 7 -> menuAndSearchMenu();
                case 8 -> hashMapLookupMenu();
                case 9 -> viewAllData();
                case 10 -> TreeVisualizer.visualizeTrees(foodBST, foodAVLTree);
                case 0 -> System.out.println("\n  Shutting down. Goodbye!\n");
                default -> System.out.println("  [!] Invalid option. Try again.");
            }
        } while (choice != 0);
    }

    // -------------------------------------------------------------------------
    // Main menu banner
    // -------------------------------------------------------------------------
    private static void printBanner() {
        System.out.println();
        System.out.println("=============================================================");
        System.out.println("    Smart Food Delivery System  -  Terminal Edition");
        System.out.println("=============================================================");
        System.out.println("  1.  Customer Management");
        System.out.println("  2.  Restaurant Management");
        System.out.println("  3.  Rider Management");
        System.out.println("  4.  Order Management  (Stack-Undo cart + FIFO Queue)");
        System.out.println("  5.  Dispatch & Delivery  (Min-Heap + Dijkstra)");
        System.out.println("  6.  Routes & Locations  (Graph Editor)");
        System.out.println("  7.  Menu & Food Search  (BST vs AVL Benchmark)");
        System.out.println("  8.  HashMap Lookup  (O(1) vs O(n) Demo)");
        System.out.println("  9.  View All Data  (All Tables)");
        System.out.println("  10. Tree Visualiser  (BST / AVL)");
        System.out.println("  0.  Exit");
        System.out.println("=============================================================");
        System.out.print("Choice: ");
    }

    // =========================================================================
    // 1. CUSTOMER MANAGEMENT
    // =========================================================================
    private static void customerMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Customer Management ---");
            System.out.println("  1. View All Customers");
            System.out.println("  2. Add New Customer");
            System.out.println("  0. Back");
            System.out.print("Choice: ");
            int c = readInt();
            switch (c) {
                case 1 -> {
                    System.out.println("\n  --- All Customers ---");
                    System.out.printf("  %-10s %-22s %-15s %-14s%n", "ID", "Name", "Phone", "Location");
                    System.out.println("  " + "-".repeat(62));
                    for (Customer cu : userManager.getCustomers()) {
                        System.out.printf("  %-10s %-22s %-15s %-14s%n",
                                cu.getId(), cu.getName(), cu.getPhone(), cu.getLocationId());
                    }
                    if (userManager.getCustomers().isEmpty())
                        System.out.println("  (no customers yet)");
                }
                case 2 -> {
                    System.out.print("  Customer ID   : ");
                    String id = sc.nextLine().trim();
                    System.out.print("  Full Name     : ");
                    String name = sc.nextLine().trim();
                    System.out.print("  Phone Number  : ");
                    String ph = sc.nextLine().trim();
                    System.out.print("  Location ID   : ");
                    String loc = sc.nextLine().trim();
                    if (id.isEmpty() || name.isEmpty()) {
                        System.out.println("  [!] Customer ID and Name are required.");
                    } else {
                        String locFinal = loc.isEmpty() ? "UNKNOWN" : loc;
                        Customer cu = new Customer(id, name, ph, locFinal);
                        userManager.addCustomer(cu);
                        dataStorage.saveCustomer(cu);
                        if (!loc.isEmpty())
                            addLocationInternal(loc, "Customer");
                        System.out.println("  [OK] Customer added: " + name);
                    }
                }
                case 0 -> back = true;
                default -> System.out.println("  [!] Invalid option.");
            }
        }
    }

    // =========================================================================
    // 2. RESTAURANT MANAGEMENT
    // =========================================================================
    private static void restaurantMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Restaurant Management ---");
            System.out.println("  1. View All Restaurants");
            System.out.println("  2. Add New Restaurant");
            System.out.println("  0. Back");
            System.out.print("Choice: ");
            int c = readInt();
            switch (c) {
                case 1 -> {
                    System.out.println("\n  --- All Restaurants ---");
                    System.out.printf("  %-10s %-22s %-15s %-14s%n", "ID", "Name", "Cuisine", "Location");
                    System.out.println("  " + "-".repeat(62));
                    for (Restaurant r : restaurantManager.getRestaurants()) {
                        System.out.printf("  %-10s %-22s %-15s %-14s%n",
                                r.getId(), r.getName(), r.getCuisineType(), r.getLocationId());
                    }
                    if (restaurantManager.getRestaurants().isEmpty())
                        System.out.println("  (no restaurants yet)");
                }
                case 2 -> {
                    System.out.print("  Restaurant ID   : ");
                    String id = sc.nextLine().trim();
                    System.out.print("  Name            : ");
                    String name = sc.nextLine().trim();
                    System.out.print("  Cuisine Type    : ");
                    String cuis = sc.nextLine().trim();
                    System.out.print("  Location ID     : ");
                    String loc = sc.nextLine().trim();
                    if (id.isEmpty() || name.isEmpty()) {
                        System.out.println("  [!] Restaurant ID and Name are required.");
                    } else {
                        String locFinal = loc.isEmpty() ? "UNKNOWN" : loc;
                        restaurantManager.addRestaurant(new Restaurant(id, name, cuis, locFinal));
                        if (!loc.isEmpty())
                            addLocationInternal(loc, "Restaurant");
                        System.out.println("  [OK] Restaurant added: " + name);
                    }
                }
                case 0 -> back = true;
                default -> System.out.println("  [!] Invalid option.");
            }
        }
    }

    // =========================================================================
    // 3. RIDER MANAGEMENT
    // =========================================================================
    private static void riderMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Rider Management ---");
            System.out.println("  1. View All Riders");
            System.out.println("  2. Add New Rider");
            System.out.println("  0. Back");
            System.out.print("Choice: ");
            int c = readInt();
            switch (c) {
                case 1 -> printRidersTable();
                case 2 -> {
                    System.out.print("  Rider ID         : ");
                    String id = sc.nextLine().trim();
                    System.out.print("  Rider Name       : ");
                    String name = sc.nextLine().trim();
                    System.out.print("  Current Location : ");
                    String loc = sc.nextLine().trim();
                    if (id.isEmpty() || name.isEmpty()) {
                        System.out.println("  [!] Rider ID and Name are required.");
                    } else if (loc.isEmpty()) {
                        System.out.println("  [!] Current location is required.");
                    } else {
                        deliveryService.addRider(new Rider(id, name, 0, 0, loc));
                        System.out.println("  [OK] Rider added: " + name + " at " + loc);
                    }
                }
                case 0 -> back = true;
                default -> System.out.println("  [!] Invalid option.");
            }
        }
    }

    private static void printRidersTable() {
        Set<String> busy = new HashSet<>();
        for (Rider r : deliveryService.getBusyRiders())
            busy.add(r.getRiderId());

        System.out.println("\n  --- All Riders ---");
        System.out.printf("  %-8s %-14s %-14s %-14s %-10s%n",
                "ID", "Name", "To Rest(min)", "Location", "Status");
        System.out.println("  " + "-".repeat(62));
        for (Rider r : deliveryService.getAllRiders()) {
            String travelDisp = busy.contains(r.getRiderId()) ? "Busy"
                    : (r.getDeliveryTimeMins() > 0 ? r.getDeliveryTimeMins() + " min" : "-");
            String state = busy.contains(r.getRiderId()) ? "Busy" : "Available";
            System.out.printf("  %-8s %-14s %-14s %-14s %-10s%n",
                    r.getRiderId(), r.getName(), travelDisp, r.getCurrentLocationId(), state);
        }
        if (deliveryService.getAllRiders().isEmpty())
            System.out.println("  (no riders yet)");
    }

    // =========================================================================
    // 4. ORDER MANAGEMENT (Stack-Undo cart + FIFO Queue + confirm)
    // =========================================================================
    private static void orderMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Order Management ---");
            System.out.println("  1. Place New Order  (Stack-Undo Cart)");
            System.out.println("  2. Confirm a Pending Order");
            System.out.println("  3. View All Orders");
            System.out.println("  0. Back");
            System.out.print("Choice: ");
            int c = readInt();
            switch (c) {
                case 1 -> placeOrderWithCart();
                case 2 -> confirmOrder();
                case 3 -> printOrdersTable();
                case 0 -> back = true;
                default -> System.out.println("  [!] Invalid option.");
            }
        }
    }

    /**
     * Places an order using a Stack-based undo cart (mirrors GUI Order tab + Stack
     * from Main.java)
     */
    private static void placeOrderWithCart() {
        // Show available customers & restaurants to help the user pick
        System.out.println("\n  --- Available Customers ---");
        for (Customer cu : userManager.getCustomers())
            System.out.println("    " + cu.getId() + " | " + cu.getName() + " (" + cu.getLocationId() + ")");

        System.out.println("\n  --- Available Restaurants ---");
        for (Restaurant r : restaurantManager.getRestaurants())
            System.out.println("    " + r.getId() + " | " + r.getName() + " (" + r.getLocationId() + ")");

        System.out.print("\n  Customer Name / ID   : ");
        String cInput = sc.nextLine().trim();
        System.out.print("  Restaurant Name / ID : ");
        String rInput = sc.nextLine().trim();

        if (cInput.isEmpty() || rInput.isEmpty()) {
            System.out.println("  [!] Customer and Restaurant are required.");
            return;
        }

        String custName = resolveCustomerName(cInput);
        String restName = resolveRestaurantName(rInput);

        // Show restaurant menu if available
        List<FoodItem> menuItems = foodAVLTree.getAllFoods().stream()
                .filter(f -> f.getRestaurantName().equalsIgnoreCase(restName))
                .toList();
        if (!menuItems.isEmpty()) {
            System.out.println("\n  --- Menu for " + restName + " ---");
            for (FoodItem f : menuItems)
                System.out.printf("    * %-20s  RM%.2f  (%s)%n", f.getName(), f.getPrice(), f.getCategory());
        }

        // Stack-based cart (LIFO undo)
        Stack<String> cartStack = new Stack<>();
        boolean ordering = true;
        System.out.println("\n  [Cart] Use Add/Undo to build your order, then Confirm.");

        while (ordering) {
            System.out.println("\n  Cart: " + (cartStack.isEmpty() ? "(empty)" : String.join(", ", cartStack))
                    + "  [" + cartStack.size() + " item(s)]");
            System.out.println("    1. Add item to cart");
            System.out.println("    2. Undo last item  (Stack pop / LIFO)");
            System.out.println("    3. Confirm & Checkout");
            System.out.println("    0. Cancel order");
            System.out.print("  Choice: ");
            int act = readInt();
            switch (act) {
                case 1 -> {
                    System.out.print("  Item name: ");
                    String item = sc.nextLine().trim();
                    if (!item.isEmpty()) {
                        cartStack.push(item);
                        System.out.println("  [OK] \"" + item + "\" added to cart.");
                    }
                }
                case 2 -> {
                    if (cartStack.isEmpty()) {
                        System.out.println("  [!] Cart is already empty.");
                    } else {
                        System.out.println("  [Undo] Removed: \"" + cartStack.pop() + "\"");
                    }
                }
                case 3 -> {
                    if (cartStack.isEmpty()) {
                        System.out.println("  [!] Cannot checkout an empty cart!");
                    } else {
                        String content = String.join(", ", cartStack);
                        order newOrder = new order(custName, restName, content);
                        newOrder.setSourceLocation(getRestaurantLocation(restName));
                        newOrder.setTargetLocation(getCustomerLocation(custName));
                        orders.add(newOrder);
                        dataStorage.saveOrder(newOrder);
                        System.out.println("  [OK] Order #" + newOrder.getOrderID() + " placed! Status: PENDING");
                        System.out.println("       Items: " + content);
                        ordering = false;
                    }
                }
                case 0 -> {
                    System.out.println("  Order cancelled.");
                    ordering = false;
                }
                default -> System.out.println("  [!] Invalid option.");
            }
        }
    }

    /**
     * Confirms a PENDING order -> sets status to CONFIRMED (eligible for dispatch)
     */
    private static void confirmOrder() {
        List<order> pending = orders.stream()
                .filter(o -> "PENDING".equals(o.getStatus()))
                .toList();
        if (pending.isEmpty()) {
            System.out.println("  [!] No PENDING orders to confirm.");
            return;
        }
        System.out.println("\n  --- Pending Orders ---");
        for (order o : pending) {
            System.out.println("    Order #" + o.getOrderID() + " | " + o.getCustomerName()
                    + " | " + o.getRestaurantName() + " | " + o.getOrderContent());
        }
        System.out.print("  Enter Order ID to confirm: ");
        int targetId = readInt();
        order found = findOrderById(targetId);
        if (found == null) {
            System.out.println("  [!] Order #" + targetId + " not found.");
        } else if (!"PENDING".equals(found.getStatus())) {
            System.out.println("  [!] Order #" + targetId + " is not PENDING (status: " + found.getStatus() + ").");
        } else {
            found.setStatus("CONFIRMED");
            System.out.println("  [OK] Order #" + targetId + " is now CONFIRMED and ready for dispatch.");
        }
    }

    private static void printOrdersTable() {
        System.out.println("\n  --- All Orders ---");
        System.out.printf("  %-8s %-16s %-16s %-22s %-12s %-8s %-22s%n",
                "OrderID", "Customer", "Restaurant", "Content", "Status", "Rider", "Route");
        System.out.println("  " + "-".repeat(104));
        for (order o : orders) {
            String route = (o.getSourceLocation() == null) ? "-"
                    : o.getSourceLocation() + " -> " + o.getTargetLocation();
            String rider = (o.getAssignedRiderId() == null) ? "-" : o.getAssignedRiderId();
            System.out.printf("  %-8s %-16s %-16s %-22s %-12s %-8s %-22s%n",
                    o.getOrderID(), o.getCustomerName(), o.getRestaurantName(),
                    truncate(o.getOrderContent(), 20), o.getStatus(), rider, route);
        }
        if (orders.isEmpty())
            System.out.println("  (no orders yet)");
    }

    // =========================================================================
    // 5. DISPATCH & DELIVERY (Dijkstra + Min-Heap priority queue)
    // =========================================================================
    private static void dispatchMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Dispatch & Delivery ---");
            System.out.println("  1. Dispatch Best Rider for a CONFIRMED Order");
            System.out.println("  2. Complete a Delivery");
            System.out.println("  3. View Available Riders (Min-Heap snapshot)");
            System.out.println("  4. Find Shortest Path  (Dijkstra)");
            System.out.println("  0. Back");
            System.out.print("Choice: ");
            int c = readInt();
            switch (c) {
                case 1 -> dispatchOrder();
                case 2 -> completeDelivery();
                case 3 -> {
                    System.out.println("\n  --- Available Riders (Min-Heap snapshot) ---");
                    List<Rider> avail = deliveryService.getAvailableRiders();
                    if (avail.isEmpty()) {
                        System.out.println("  (none available)");
                    } else {
                        System.out.printf("  %-8s %-14s %-10s %-14s%n", "ID", "Name", "Time(min)", "Location");
                        System.out.println("  " + "-".repeat(50));
                        for (Rider r : avail)
                            System.out.printf("  %-8s %-14s %-10d %-14s%n",
                                    r.getRiderId(), r.getName(), r.getDeliveryTimeMins(), r.getCurrentLocationId());
                    }
                }
                case 4 -> dijkstraPathMenu();
                case 0 -> back = true;
                default -> System.out.println("  [!] Invalid option.");
            }
        }
    }

    private static void dispatchOrder() {
        List<order> confirmed = orders.stream()
                .filter(o -> "CONFIRMED".equals(o.getStatus()))
                .toList();
        if (confirmed.isEmpty()) {
            System.out.println("  [!] No CONFIRMED orders ready for dispatch.");
            return;
        }
        System.out.println("\n  --- CONFIRMED Orders ---");
        for (order o : confirmed) {
            System.out.println("    Order #" + o.getOrderID() + " | " + o.getCustomerName()
                    + " @ " + o.getRestaurantName()
                    + " | Route: " + o.getSourceLocation() + " -> " + o.getTargetLocation());
        }
        System.out.print("  Enter Order ID to dispatch: ");
        int targetId = readInt();
        order target = findOrderById(targetId);
        if (target == null || !"CONFIRMED".equals(target.getStatus())) {
            System.out.println("  [!] Invalid order or order is not CONFIRMED.");
            return;
        }

        String source = target.getSourceLocation();
        String dest = target.getTargetLocation();

        if (source == null || dest == null || source.equals("UNKNOWN") || dest.equals("UNKNOWN")) {
            System.out.println("  [!] Order source or target location is unknown.");
            return;
        }

        // Update rider priorities using Dijkstra from each rider to the restaurant
        deliveryService.updateRiderPrioritiesForRestaurant(source);

        // Poll best rider from Min-Heap
        Rider bestRider = deliveryService.assignBestRiderForRestaurant(source);
        if (bestRider == null) {
            System.out.println("  [!] No available riders found.");
            return;
        }

        String riderStart = bestRider.getCurrentLocationId();

        // Rider -> Restaurant (pickup)
        DijkstraAlgorithm.Result pickupResult = DijkstraAlgorithm.shortestPath(routeGraph, riderStart, source);
        // Restaurant -> Customer (delivery)
        DijkstraAlgorithm.Result deliveryResult = DijkstraAlgorithm.shortestPath(routeGraph, source, dest);

        double pickupTime = (pickupResult.getDistance() == Double.POSITIVE_INFINITY) ? 0 : pickupResult.getDistance();
        double deliveryTime = (deliveryResult.getDistance() == Double.POSITIVE_INFINITY) ? 0
                : deliveryResult.getDistance();
        double totalTime = pickupTime + deliveryTime;

        target.assignRider(bestRider.getRiderId(), source, dest, totalTime);

        // Build full path string
        List<String> fullPath = new ArrayList<>();
        if (pickupResult.getPath() != null && !pickupResult.getPath().isEmpty()) {
            for (Node n : pickupResult.getPath())
                fullPath.add(n.getId());
        } else {
            fullPath.add(source);
        }
        if (deliveryResult.getPath() != null) {
            for (int i = 0; i < deliveryResult.getPath().size(); i++) {
                String nId = deliveryResult.getPath().get(i).getId();
                if (i == 0 && !fullPath.isEmpty() && fullPath.get(fullPath.size() - 1).equals(nId))
                    continue;
                fullPath.add(nId);
            }
        }

        System.out.println("\n  [DISPATCHED!]");
        System.out.println("  Rider       : " + bestRider.getName() + " (" + bestRider.getRiderId() + ")");
        System.out.println("  Rider Start : " + riderStart);
        System.out.println("  Pickup Time : " + pickupTime + " min  (" + riderStart + " -> " + source + ")");
        System.out.println("  Delivery    : " + deliveryTime + " min  (" + source + " -> " + dest + ")");
        System.out.println("  Total Time  : " + totalTime + " min");
        System.out.println("  Full Route  : " + String.join(" -> ", fullPath));
    }

    private static void completeDelivery() {
        Collection<Rider> busy = deliveryService.getBusyRiders();
        if (busy.isEmpty()) {
            System.out.println("  [!] No riders are currently on a delivery.");
            return;
        }
        System.out.println("\n  --- Busy Riders ---");
        for (Rider r : busy) {
            System.out.println("    " + r.getRiderId() + " | " + r.getName()
                    + " | Location: " + r.getCurrentLocationId());
        }
        System.out.print("  Enter Rider ID to mark delivery as completed: ");
        String riderId = sc.nextLine().trim();

        // Find destination from associated dispatched order
        String finalLoc = orders.stream()
                .filter(o -> riderId.equalsIgnoreCase(o.getAssignedRiderId())
                        && "DISPATCHED".equals(o.getStatus()))
                .map(order::getTargetLocation)
                .findFirst().orElse("UNKNOWN");

        if (deliveryService.completeDelivery(riderId, finalLoc)) {
            orders.stream()
                    .filter(o -> riderId.equalsIgnoreCase(o.getAssignedRiderId()))
                    .forEach(o -> o.setStatus("DELIVERED"));
            System.out.println("  [OK] Delivery completed. Rider " + riderId
                    + " is now available at " + finalLoc + ".");
        } else {
            System.out.println("  [!] Rider " + riderId + " not found in busy list.");
        }
    }

    private static void dijkstraPathMenu() {
        System.out.println("\n  --- Dijkstra Shortest Path ---");
        printLocationsTable();
        System.out.print("  Source Location ID : ");
        String src = sc.nextLine().trim();
        System.out.print("  Target Location ID : ");
        String tgt = sc.nextLine().trim();

        DijkstraAlgorithm.Result res = DijkstraAlgorithm.shortestPath(routeGraph, src, tgt);
        if (res.getPath().isEmpty() || res.getDistance() == Double.POSITIVE_INFINITY) {
            System.out.println("  [!] No path found between \"" + src + "\" and \"" + tgt + "\".");
        } else {
            StringJoiner sj = new StringJoiner(" -> ");
            for (Node n : res.getPath())
                sj.add(n.getId());
            System.out.println("  Shortest Path    : " + sj);
            System.out.println("  Total Cost/Dist  : " + res.getDistance());
        }
    }

    // =========================================================================
    // 6. ROUTES & LOCATIONS (Graph editor - mirrors GUI Routes & Loc tab)
    // =========================================================================
    private static void routeAndLocationMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Routes & Locations ---");
            System.out.println("  1. View All Locations");
            System.out.println("  2. Add Location Node");
            System.out.println("  3. View All Routes");
            System.out.println("  4. Add Route (Edge between two locations)");
            System.out.println("  0. Back");
            System.out.print("Choice: ");
            int c = readInt();
            switch (c) {
                case 1 -> printLocationsTable();
                case 2 -> {
                    System.out.print("  Location ID                      : ");
                    String locId = sc.nextLine().trim();
                    System.out.print("  Type [Customer/Restaurant/Hub/Other]: ");
                    String type = sc.nextLine().trim();
                    if (locId.isEmpty()) {
                        System.out.println("  [!] Location ID is required.");
                    } else {
                        if (type.isEmpty())
                            type = inferType(locId);
                        addLocationInternal(locId, type);
                        System.out.println("  [OK] Location \"" + locId + "\" (" + type + ") added.");
                    }
                }
                case 3 -> printRoutesTable();
                case 4 -> {
                    printLocationsTable();
                    System.out.print("  From Location ID : ");
                    String from = sc.nextLine().trim();
                    System.out.print("  To   Location ID : ");
                    String to = sc.nextLine().trim();
                    System.out.print("  Weight / Cost    : ");
                    String wStr = sc.nextLine().trim();
                    if (from.isEmpty() || to.isEmpty()) {
                        System.out.println("  [!] Both From and To locations are required.");
                    } else {
                        double w = parseDouble(wStr, 1.0);
                        addRouteInternal(from, to, w);
                        System.out.println("  [OK] Route " + from + " <-> " + to + " (weight " + w + ") added.");
                    }
                }
                case 0 -> back = true;
                default -> System.out.println("  [!] Invalid option.");
            }
        }
    }

    private static void printLocationsTable() {
        System.out.println("\n  --- Locations ---");
        System.out.printf("  %-20s %-12s%n", "Location ID", "Type");
        System.out.println("  " + "-".repeat(33));
        for (Map.Entry<String, String> e : locationTypes.entrySet())
            System.out.printf("  %-20s %-12s%n", e.getKey(), e.getValue());
        if (locationTypes.isEmpty())
            System.out.println("  (no locations yet)");
    }

    private static void printRoutesTable() {
        System.out.println("\n  --- Routes ---");
        System.out.printf("  %-20s %-20s %-8s%n", "From", "To", "Weight");
        System.out.println("  " + "-".repeat(48));
        boolean any = false;
        for (Node from : routeGraph.getNodes()) {
            for (Edge e : routeGraph.getEdges(from)) {
                System.out.printf("  %-20s %-20s %-8s%n",
                        from.getId(), e.getTarget().getId(), e.getWeight());
                any = true;
            }
        }
        if (!any)
            System.out.println("  (no routes yet)");
    }

    // =========================================================================
    // 7. MENU & FOOD SEARCH (BST vs AVL + add food item)
    // =========================================================================
    private static void menuAndSearchMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Menu & Food Search ---");
            System.out.println("  1. View Food Menu  (A-Z via AVL in-order)");
            System.out.println("  2. Search Food  (BST vs AVL Benchmark)");
            System.out.println("  3. Add New Food Item to Menu");
            System.out.println("  0. Back");
            System.out.print("Choice: ");
            int c = readInt();
            switch (c) {
                case 1 -> {
                    System.out.println("\n  --- Food Menu (A-Z) ---");
                    System.out.printf("  %-22s %-14s %-10s %-16s%n", "Name", "Category", "Price", "Restaurant");
                    System.out.println("  " + "-".repeat(65));
                    List<FoodItem> all = foodAVLTree.getAllFoods();
                    if (all.isEmpty()) {
                        System.out.println("  (no food items yet)");
                    } else {
                        for (FoodItem f : all)
                            System.out.printf("  %-22s %-14s RM%-8.2f %-16s%n",
                                    f.getName(), f.getCategory(), f.getPrice(), f.getRestaurantName());
                    }
                }
                case 2 -> {
                    System.out.print("  Food name to search: ");
                    String name = sc.nextLine().trim();
                    if (name.isEmpty()) {
                        System.out.println("  [!] Please enter a food name.");
                    } else {
                        long t1 = System.nanoTime();
                        FoodItem bstRes = foodBST.search(name);
                        long t2 = System.nanoTime();
                        long t3 = System.nanoTime();
                        FoodItem avlRes = foodAVLTree.search(name);
                        long t4 = System.nanoTime();

                        System.out.println("\n  --- Search Results ---");
                        System.out.println("  BST Result : "
                                + (bstRes != null ? bstRes.toString() : "Not Found")
                                + "  [" + (t2 - t1) + " ns]");
                        System.out.println("  AVL Result : "
                                + (avlRes != null ? avlRes.toString() : "Not Found")
                                + "  [" + (t4 - t3) + " ns]");
                    }
                }
                case 3 -> {
                    System.out.println("  --- Available Restaurants ---");
                    for (Restaurant r : restaurantManager.getRestaurants())
                        System.out.println("    " + r.getId() + " | " + r.getName());
                    System.out.print("  Food Name      : ");
                    String fname = sc.nextLine().trim();
                    System.out.print("  Category       : ");
                    String cat = sc.nextLine().trim();
                    System.out.print("  Price (RM)     : ");
                    String pStr = sc.nextLine().trim();
                    System.out.print("  Restaurant Name: ");
                    String rname = sc.nextLine().trim();
                    if (fname.isEmpty() || cat.isEmpty() || pStr.isEmpty() || rname.isEmpty()) {
                        System.out.println("  [!] All fields are required.");
                    } else {
                        double price = parseDouble(pStr, -1);
                        if (price < 0) {
                            System.out.println("  [!] Invalid price.");
                        } else {
                            FoodItem food = new FoodItem(fname, cat, price, rname);
                            foodBST.insert(food);
                            foodAVLTree.insert(food);
                            System.out.println("  [OK] Food item \"" + fname + "\" added to BST and AVL.");
                        }
                    }
                }
                case 0 -> back = true;
                default -> System.out.println("  [!] Invalid option.");
            }
        }
    }

    // =========================================================================
    // 8. HASHMAP LOOKUP (O(1) vs O(n) + 10k speed demo)
    // =========================================================================
    private static void hashMapLookupMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- HashMap Lookup & Speed Demo ---");
            System.out.println("  1. Customer Lookup  (O(1) HashMap vs O(n) Linear)");
            System.out.println("  2. Order Lookup     (O(1) HashMap vs O(n) Linear)");
            System.out.println("  3. Run 10,000-Item Speed Demo");
            System.out.println("  0. Back");
            System.out.print("Choice: ");
            int c = readInt();
            switch (c) {
                case 1 -> {
                    System.out.print("  Enter Customer ID (e.g. C001): ");
                    String key = sc.nextLine().trim();
                    if (key.isEmpty()) {
                        System.out.println("  [!] ID required.");
                        break;
                    }

                    // O(1) HashMap lookup
                    long t1 = System.nanoTime();
                    Customer hashResult = dataStorage.getCustomer(key);
                    long t2 = System.nanoTime();

                    // O(n) Linear scan
                    long t3 = System.nanoTime();
                    Customer linearResult = null;
                    for (Customer cu : userManager.getCustomers()) {
                        if (cu.getId().equalsIgnoreCase(key)) {
                            linearResult = cu;
                            break;
                        }
                    }
                    long t4 = System.nanoTime();

                    boolean found = hashResult != null;
                    System.out.println("\n  --- Results for key: " + key + " ---");
                    System.out.println("  O(1) HashMap  : "
                            + (found ? hashResult.getName() + " (" + hashResult.getPhone() + ")" : "Not Found")
                            + "  [" + (t2 - t1) + " ns]");
                    System.out.println("  O(n) Linear   : "
                            + (linearResult != null ? linearResult.getName() : "Not Found")
                            + "  [" + (t4 - t3) + " ns]");
                    if (found)
                        System.out.printf("  HashMap ~%dx faster than linear scan%n",
                                Math.max(1, (t4 - t3) / Math.max(1, t2 - t1)));
                }
                case 2 -> {
                    System.out.print("  Enter Order ID number (e.g. 1): ");
                    String raw = sc.nextLine().trim();
                    if (raw.isEmpty()) {
                        System.out.println("  [!] ID required.");
                        break;
                    }
                    String orderKey = raw.startsWith("ORD-") ? raw : "ORD-" + raw;

                    // O(1) HashMap lookup
                    long t1 = System.nanoTime();
                    order hashResult = dataStorage.getOrder(orderKey);
                    long t2 = System.nanoTime();

                    // O(n) linear scan
                    int numericId = -1;
                    try {
                        numericId = Integer.parseInt(raw.replace("ORD-", ""));
                    } catch (Exception ignored) {
                    }
                    final int finalNumericId = numericId;
                    long t3 = System.nanoTime();
                    order linearResult = null;
                    for (order o : orders) {
                        if (o.getOrderID() == finalNumericId) {
                            linearResult = o;
                            break;
                        }
                    }
                    long t4 = System.nanoTime();

                    boolean found = hashResult != null;
                    System.out.println("\n  --- Results for key: " + orderKey + " ---");
                    System.out.println("  O(1) HashMap : "
                            + (found ? hashResult.getCustomerName() + " -> " + hashResult.getOrderContent()
                                    : "Not Found")
                            + "  [" + (t2 - t1) + " ns]");
                    System.out.println("  O(n) Linear  : "
                            + (linearResult != null
                                    ? linearResult.getCustomerName() + " -> " + linearResult.getOrderContent()
                                    : "Not Found")
                            + "  [" + (t4 - t3) + " ns]");
                    if (found)
                        System.out.printf("  HashMap ~%dx faster than linear scan%n",
                                Math.max(1, (t4 - t3) / Math.max(1, t2 - t1)));
                }
                case 3 -> runSpeedDemo();
                case 0 -> back = true;
                default -> System.out.println("  [!] Invalid option.");
            }
        }
    }

    /**
     * Mirrors OnePageDashboard.runSpeedDemo() - 10k item benchmark printed to
     * terminal
     */
    private static void runSpeedDemo() {
        System.out.println("\n  [Building 10,000-item dataset...]");
        HashMap<String, Customer> bigMap = new HashMap<>();
        List<Customer> bigList = new ArrayList<>();
        for (int i = 0; i < 10_000; i++) {
            String id = "C" + String.format("%05d", i);
            Customer cu = new Customer(id, "User " + i, "010000" + i);
            bigMap.put(id, cu);
            bigList.add(cu);
        }
        String target = "C09999";

        // O(1) HashMap
        long t1 = System.nanoTime();
        Customer hashResult = bigMap.get(target);
        long t2 = System.nanoTime();

        // O(n) Linear
        long t3 = System.nanoTime();
        Customer linearResult = null;
        for (Customer cu : bigList) {
            if (cu.getId().equals(target)) {
                linearResult = cu;
                break;
            }
        }
        long t4 = System.nanoTime();

        long hashTime = t2 - t1;
        long linearTime = t4 - t3;
        long ratio = linearTime / Math.max(1, hashTime);

        System.out.println("\n  ===  Speed Comparison (n = 10,000 items)  ===");
        System.out.println("  Target key      : " + target);
        System.out.println("  Found (HashMap) : " + (hashResult != null ? hashResult.getName() : "Not Found"));
        System.out.println("  Found (Linear)  : " + (linearResult != null ? linearResult.getName() : "Not Found"));
        System.out.println("  " + "-".repeat(45));
        System.out.println("  1. HashMap get()    [O(1)] : " + hashTime + " ns");
        System.out.println("  2. ArrayList linear [O(n)] : " + linearTime + " ns");
        System.out.println("  " + "-".repeat(45));
        System.out.println("  HashMap is ~" + ratio + "x faster for n=10,000.");
        System.out.println("  As n grows: HashMap stays O(1), Linear grows O(n).");
        System.out.println("  =============================================");
    }

    // =========================================================================
    // 9. VIEW ALL DATA (summary of every table)
    // =========================================================================
    private static void viewAllData() {
        System.out.println("\n=============================================");
        System.out.println("           SYSTEM DATA SNAPSHOT             ");
        System.out.println("=============================================");

        // Customers
        System.out.println("\n  --- Customers ---");
        System.out.printf("  %-10s %-22s %-15s %-14s%n", "ID", "Name", "Phone", "Location");
        for (Customer cu : userManager.getCustomers())
            System.out.printf("  %-10s %-22s %-15s %-14s%n",
                    cu.getId(), cu.getName(), cu.getPhone(), cu.getLocationId());
        if (userManager.getCustomers().isEmpty())
            System.out.println("  (none)");

        // Restaurants
        System.out.println("\n  --- Restaurants ---");
        System.out.printf("  %-10s %-22s %-15s %-14s%n", "ID", "Name", "Cuisine", "Location");
        for (Restaurant r : restaurantManager.getRestaurants())
            System.out.printf("  %-10s %-22s %-15s %-14s%n",
                    r.getId(), r.getName(), r.getCuisineType(), r.getLocationId());
        if (restaurantManager.getRestaurants().isEmpty())
            System.out.println("  (none)");

        // Riders
        System.out.println();
        printRidersTable();

        // Orders
        System.out.println();
        printOrdersTable();

        // Locations
        System.out.println();
        printLocationsTable();

        // Routes
        System.out.println();
        printRoutesTable();

        // Food menu
        System.out.println("\n  --- Food Menu (A-Z) ---");
        System.out.printf("  %-22s %-14s %-10s %-16s%n", "Name", "Category", "Price", "Restaurant");
        System.out.println("  " + "-".repeat(65));
        for (FoodItem f : foodAVLTree.getAllFoods())
            System.out.printf("  %-22s %-14s RM%-8.2f %-16s%n",
                    f.getName(), f.getCategory(), f.getPrice(), f.getRestaurantName());
        if (foodAVLTree.getAllFoods().isEmpty())
            System.out.println("  (none)");
    }

    // =========================================================================
    // Seed demo data (richer data-set from OnePageDashboard.seedDemoData)
    // =========================================================================
    private static void seedDemoData() {
        // Customers
        Customer c1 = new Customer("C001", "Ali Ahmad", "0123456789", "E_Customer");
        Customer c2 = new Customer("C002", "Sarah Kaur", "0187654321", "F_Customer");
        Customer c3 = new Customer("C003", "Bobby Lim", "0112233445", "G_Customer");
        userManager.addCustomer(c1);
        userManager.addCustomer(c2);
        userManager.addCustomer(c3);
        dataStorage.saveCustomer(c1);
        dataStorage.saveCustomer(c2);
        dataStorage.saveCustomer(c3);

        // Restaurants
        restaurantManager.addRestaurant(new Restaurant("R001", "A_Restaurant", "Rice", "A_Restaurant"));
        restaurantManager.addRestaurant(new Restaurant("R002", "B_Restaurant", "Noodle", "B_Restaurant"));
        restaurantManager.addRestaurant(new Restaurant("R003", "C_Restaurant", "Local", "C_Restaurant"));

        // Locations & edges (bidirectional)
        addLocationInternal("A_Restaurant", "Restaurant");
        addLocationInternal("B_Restaurant", "Restaurant");
        addLocationInternal("C_Restaurant", "Restaurant");
        addLocationInternal("D_Hub", "Hub");
        addLocationInternal("E_Customer", "Customer");
        addLocationInternal("F_Customer", "Customer");
        addLocationInternal("G_Customer", "Customer");

        addRouteInternal("A_Restaurant", "D_Hub", 4);
        addRouteInternal("B_Restaurant", "D_Hub", 3);
        addRouteInternal("C_Restaurant", "D_Hub", 5);
        addRouteInternal("D_Hub", "E_Customer", 2);
        addRouteInternal("D_Hub", "F_Customer", 4);
        addRouteInternal("D_Hub", "G_Customer", 6);
        addRouteInternal("A_Restaurant", "E_Customer", 7);
        addRouteInternal("B_Restaurant", "F_Customer", 6);

        // Food items
        FoodItem[] foods = {
                new FoodItem("Nasi Lemak", "Rice", 8.50, "A_Restaurant"),
                new FoodItem("Char Kuey Teow", "Noodle", 9.00, "B_Restaurant"),
                new FoodItem("Roti Canai", "Bread", 2.00, "C_Restaurant"),
                new FoodItem("Mee Goreng", "Noodle", 7.50, "C_Restaurant"),
                new FoodItem("Laksa", "Soup", 10.00, "B_Restaurant"),
                new FoodItem("Teh Tarik", "Drink", 2.50, "C_Restaurant"),
        };
        for (FoodItem f : foods) {
            foodBST.insert(f);
            foodAVLTree.insert(f);
        }

        // Seed demo orders
        order first = new order("Ali Ahmad", "A_Restaurant", "Nasi Lemak x2");
        first.setSourceLocation("A_Restaurant");
        first.setTargetLocation("E_Customer");
        first.setStatus("CONFIRMED");
        orders.add(first);
        dataStorage.saveOrder(first);

        order second = new order("Sarah Kaur", "B_Restaurant", "Char Kuey Teow x1");
        second.setSourceLocation("B_Restaurant");
        second.setTargetLocation("F_Customer");
        // stays PENDING by default
        orders.add(second);
        dataStorage.saveOrder(second);

        order third = new order("Bobby Lim", "C_Restaurant", "Roti Canai x3, Teh Tarik x2");
        third.setSourceLocation("C_Restaurant");
        third.setTargetLocation("G_Customer");
        third.setStatus("CONFIRMED");
        orders.add(third);
        dataStorage.saveOrder(third);

        System.out.println("  [OK] Demo data loaded. " + orders.size() + " sample orders ready.");
    }

    // =========================================================================
    // Internal helpers
    // =========================================================================

    private static void addLocationInternal(String locationId, String type) {
        if (locationId == null || locationId.isBlank())
            return;
        String key = locationId.trim();
        locationTypes.put(key, (type == null || type.isBlank()) ? inferType(key) : type);
        if (routeGraph.getNodeById(key) == null)
            routeGraph.addNode(new Node(key));
    }

    private static void addRouteInternal(String fromId, String toId, double weight) {
        addLocationInternal(fromId, inferType(fromId));
        addLocationInternal(toId, inferType(toId));
        routeGraph.addEdge(new Node(fromId), new Node(toId), weight);
        routeGraph.addEdge(new Node(toId), new Node(fromId), weight);
    }

    private static String inferType(String id) {
        String lower = (id == null) ? "" : id.toLowerCase();
        if (lower.contains("rest"))
            return "Restaurant";
        if (lower.contains("cust"))
            return "Customer";
        if (lower.contains("hub"))
            return "Hub";
        return "Other";
    }

    private static order findOrderById(int id) {
        for (order o : orders)
            if (o.getOrderID() == id)
                return o;
        return null;
    }

    private static String getRestaurantLocation(String name) {
        for (Restaurant r : restaurantManager.getRestaurants()) {
            if (r.getName().equalsIgnoreCase(name) || r.getId().equalsIgnoreCase(name))
                return r.getLocationId();
        }
        return "UNKNOWN";
    }

    private static String getCustomerLocation(String name) {
        for (Customer cu : userManager.getCustomers()) {
            if (cu.getName().equalsIgnoreCase(name) || cu.getId().equalsIgnoreCase(name))
                return cu.getLocationId();
        }
        return "UNKNOWN";
    }

    private static String resolveCustomerName(String input) {
        for (Customer cu : userManager.getCustomers()) {
            if (cu.getId().equalsIgnoreCase(input) || cu.getName().equalsIgnoreCase(input))
                return cu.getName();
        }
        return input;
    }

    private static String resolveRestaurantName(String input) {
        for (Restaurant r : restaurantManager.getRestaurants()) {
            if (r.getId().equalsIgnoreCase(input) || r.getName().equalsIgnoreCase(input))
                return r.getName();
        }
        return input;
    }

    private static int readInt() {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static double parseDouble(String text, double def) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static String truncate(String s, int max) {
        if (s == null)
            return "";
        return s.length() <= max ? s : s.substring(0, max - 2) + "..";
    }
}
