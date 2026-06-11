package search_and_data_retrieval_system;

import user_management.Customer;
import order_processing.order;
import java.util.Scanner;

public class SearchManager {

    private final FoodBST bst = new FoodBST();
    private final FoodAVLTree avl = new FoodAVLTree();
    private final DataStorage storage = new DataStorage();
    private final Scanner scanner;

    public SearchManager(Scanner scanner) {
        this.scanner = scanner;
        loadSampleData();
    }

    public void run() {
        int choice;
        do {
            printMainMenu();
            choice = readInt();
            switch (choice) {
                case 1 -> menuPartA();
                case 2 -> menuPartB();
                case 0 -> System.out.println("  Returning to main menu...");
                default -> System.out.println("  Invalid option. Please try again.");
            }
        } while (choice != 0);
    }

    private void menuPartA() {
        int choice;
        do {
            printPartAMenu();
            choice = readInt();
            switch (choice) {
                case 1 -> insertFood();
                case 2 -> searchFood();
                case 3 -> displayFoodsSorted();
                case 4 -> compareTreeStats();
                case 0 -> {
                }
                default -> System.out.println("  Invalid option.");
            }
        } while (choice != 0);
    }

    private void insertFood() {
        System.out.print("\n  Food name     : ");
        String name = scanner.nextLine().trim();
        System.out.print("  Category      : ");
        String category = scanner.nextLine().trim();
        System.out.print("  Price (RM)    : ");
        double price = readDouble();
        System.out.print("  Restaurant    : ");
        String restaurant = scanner.nextLine().trim();

        FoodItem food = new FoodItem(name, category, price, restaurant);
        bst.insert(food);
        avl.insert(food);
        System.out.println("  ✓ \"" + name + "\" inserted into both BST and AVL Tree.");
    }

    private void searchFood() {
        System.out.print("\n  Enter food name to search: ");
        String name = scanner.nextLine().trim();

        System.out.println("\n  [BST Search]");
        long t1 = System.nanoTime();
        FoodItem bstResult = bst.search(name);
        long t2 = System.nanoTime();
        System.out.println(bstResult != null ? "  Found  : " + bstResult : "  Not found in BST.");
        System.out.printf("  Time   : %d ns%n", (t2 - t1));

        System.out.println("\n  [AVL Tree Search]");
        t1 = System.nanoTime();
        FoodItem avlResult = avl.search(name);
        t2 = System.nanoTime();
        System.out.println(avlResult != null ? "  Found  : " + avlResult : "  Not found in AVL Tree.");
        System.out.printf("  Time   : %d ns%n", (t2 - t1));
    }

    private void displayFoodsSorted() {
        System.out.println("\n  === BST In-Order Traversal (A → Z) ===");
        bst.displayInOrder();
        System.out.println("\n  === AVL Tree In-Order Traversal (A → Z) ===");
        avl.displayInOrder();
    }

    private void compareTreeStats() {
        System.out.println("\n  === BST vs AVL Comparison ===");
        System.out.printf("  BST : %d nodes  (height not self-tracked — can degrade to O(n))%n", bst.size());
        System.out.printf("  AVL : %d nodes, guaranteed height = %d  (log2(%d) ~ %.1f)%n",
                avl.size(), avl.getTreeHeight(), avl.size(),
                avl.size() > 0 ? Math.log(avl.size()) / Math.log(2) : 0);
        System.out.println("\n  Key difference:");
        System.out.println("  - BST can become a linked list (O(n)) if items are inserted in sorted order.");
        System.out.println("  - AVL auto-rotates to keep height balanced, guaranteeing O(log n) always.");
    }

    private void menuPartB() {
        int choice;
        do {
            printPartBMenu();
            choice = readInt();
            switch (choice) {
                case 1 -> addCustomer();
                case 2 -> lookupCustomer();
                case 3 -> deleteCustomer();
                case 4 -> storage.displayAllCustomers();
                case 5 -> addOrder();
                case 6 -> lookupOrder();
                case 7 -> deleteOrder();
                case 8 -> storage.displayAllOrders();
                case 9 -> storage.demonstrateRetrievalSpeed();
                case 0 -> {
                }
                default -> System.out.println("  Invalid option.");
            }
        } while (choice != 0);
    }

    private void addCustomer() {
        System.out.print("\n  Customer ID   : ");
        String id = scanner.nextLine().trim();
        System.out.print("  Name          : ");
        String name = scanner.nextLine().trim();
        System.out.print("  Phone         : ");
        String phone = scanner.nextLine().trim();

        storage.saveCustomer(new Customer(id, name, phone));
        System.out.println("  ✓ Customer \"" + id + "\" saved to HashMap. O(1) put().");
    }

    private void lookupCustomer() {
        System.out.print("\n  Enter Customer ID: ");
        String id = scanner.nextLine().trim();

        long t1 = System.nanoTime();
        Customer c = storage.getCustomer(id);
        long t2 = System.nanoTime();

        System.out.println(c != null ? "  Found  : " + c : "  Customer \"" + id + "\" not found.");
        System.out.printf("  Retrieval time : %d ns  (O(1) average)%n", (t2 - t1));
    }

    private void deleteCustomer() {
        System.out.print("\n  Enter Customer ID to delete: ");
        String id = scanner.nextLine().trim();
        System.out.println(storage.deleteCustomer(id)
                ? "  ✓ Customer \"" + id + "\" deleted."
                : "  Customer \"" + id + "\" not found.");
    }

    private void addOrder() {
        System.out.print("\n  Customer name  : ");
        String name = scanner.nextLine().trim();
        System.out.print("  Order content  : ");
        String content = scanner.nextLine().trim();

        order o = new order(name, content);
        String key = storage.saveOrder(o);
        System.out.println("  ✓ Order saved with key \"" + key + "\". O(1) put().");
        System.out.println("  Use key \"" + key + "\" to look up or delete this order.");
    }

    private void lookupOrder() {
        System.out.print("\n  Enter Order key (e.g. ORD-1): ");
        String key = scanner.nextLine().trim();

        long t1 = System.nanoTime();
        order o = storage.getOrder(key);
        long t2 = System.nanoTime();

        if (o != null) {
            System.out.println("  Found:");
            o.displayOrder();
        } else
            System.out.println("  Order \"" + key + "\" not found.");
        System.out.printf("  Retrieval time : %d ns  (O(1) average)%n", (t2 - t1));
    }

    private void deleteOrder() {
        System.out.print("\n  Enter Order key to delete (e.g. ORD-1): ");
        String key = scanner.nextLine().trim();
        System.out.println(storage.deleteOrder(key)
                ? "  ✓ Order \"" + key + "\" deleted."
                : "  Order \"" + key + "\" not found.");
    }

    private void loadSampleData() {

        FoodItem[] foods = {
                new FoodItem("Nasi Lemak", "Rice", 8.50, "Warung Mak Jah"),
                new FoodItem("Char Kuey Teow", "Noodle", 9.00, "Penang Corner"),
                new FoodItem("Roti Canai", "Bread", 2.00, "Mamak Selera"),
                new FoodItem("Ayam Goreng", "Chicken", 7.00, "KFC-style Stall"),
                new FoodItem("Mee Goreng", "Noodle", 7.50, "Mamak Selera"),
                new FoodItem("Laksa", "Soup", 10.00, "Penang Corner"),
                new FoodItem("Burger Special", "Burger", 6.50, "Uncle Burger"),
                new FoodItem("Teh Tarik", "Drink", 2.50, "Mamak Selera"),
        };
        for (FoodItem f : foods) {
            bst.insert(f);
            avl.insert(f);
        }

        storage.saveCustomer(new Customer("C001", "Ali Ahmad", "0123456789"));
        storage.saveCustomer(new Customer("C002", "Sarah Kaur", "0187654321"));
        storage.saveCustomer(new Customer("C003", "Bobby Lim", "0112233445"));

        storage.saveOrder(new order("Ali Ahmad", "Nasi Lemak x2"));
        storage.saveOrder(new order("Sarah Kaur", "Char Kuey Teow x1"));
        storage.saveOrder(new order("Bobby Lim", "Roti Canai x3, Teh Tarik x2"));
    }

    private void printMainMenu() {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║         Search & Data Retrieval          ║");
        System.out.println("╠══════════════════════════════════════════╣");
        System.out.println("║  1. Part A — Food Search (BST / AVL)     ║");
        System.out.println("║  2. Part B — Data Retrieval (HashMap)    ║");
        System.out.println("║  0. Back                                 ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.print("  Choice: ");
    }

    private void printPartAMenu() {
        System.out.println("\n  --- Part A : Food Search ---");
        System.out.println("  1. Insert food item");
        System.out.println("  2. Search food by name");
        System.out.println("  3. Display all foods (sorted A-Z)");
        System.out.println("  4. Compare BST vs AVL Tree stats");
        System.out.println("  0. Back");
        System.out.print("  Choice: ");
    }

    private void printPartBMenu() {
        System.out.println("\n  --- Part B : Data Retrieval (HashMap) ---");
        System.out.println("  --- Customers ---");
        System.out.println("  1. Add customer to HashMap");
        System.out.println("  2. Look up customer by ID");
        System.out.println("  3. Delete customer");
        System.out.println("  4. Display all customers");
        System.out.println("  --- Orders ---");
        System.out.println("  5. Add order to HashMap");
        System.out.println("  6. Look up order by key (e.g. ORD-1)");
        System.out.println("  7. Delete order by key");
        System.out.println("  8. Display all orders");
        System.out.println("  --- Demo ---");
        System.out.println("  9. O(1) vs O(n) speed demo");
        System.out.println("  0. Back");
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
        new SearchManager(sc).run();
        sc.close();
    }
}
