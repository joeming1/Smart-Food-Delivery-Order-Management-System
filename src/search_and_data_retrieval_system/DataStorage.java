package search_and_data_retrieval_system;

import user_management.Customer;
import order_processing.order;
import java.util.HashMap;
import java.util.Collection;
import java.util.Map;

public class DataStorage {

    private final HashMap<String, Customer> customerStore = new HashMap<>();

    private final HashMap<String, order> orderStore = new HashMap<>();
    private int orderKeyCounter = 0;

    public void saveCustomer(Customer customer) {
        customerStore.put(customer.getId(), customer);
    }

    public Customer getCustomer(String id) {
        return customerStore.get(id);
    }

    public boolean deleteCustomer(String id) {
        return customerStore.remove(id) != null;
    }

    public Collection<Customer> getAllCustomers() {
        return customerStore.values();
    }

    public int getCustomerCount() {
        return customerStore.size();
    }

    public String saveOrder(order o) {
        String key = "ORD-" + (++orderKeyCounter);
        orderStore.put(key, o);
        return key;
    }

    public order getOrder(String orderKey) {
        return orderStore.get(orderKey);
    }

    public boolean deleteOrder(String orderKey) {
        return orderStore.remove(orderKey) != null;
    }

    public Collection<order> getAllOrders() {
        return orderStore.values();
    }

    public int getOrderCount() {
        return orderStore.size();
    }

    public void displayAllCustomers() {
        if (customerStore.isEmpty()) {
            System.out.println("  (no customers in HashMap)");
            return;
        }
        System.out.println("  " + "-".repeat(56));
        System.out.format("  | %-10s | %-20s | %-15s |%n", "ID", "Name", "Phone");
        System.out.println("  " + "-".repeat(56));
        for (Map.Entry<String, Customer> entry : customerStore.entrySet()) {
            System.out.println("  " + entry.getValue());
        }
        System.out.println("  " + "-".repeat(56));
    }

    public void displayAllOrders() {
        if (orderStore.isEmpty()) {
            System.out.println("  (no orders in HashMap)");
            return;
        }
        System.out.println("  " + "-".repeat(40));
        for (Map.Entry<String, order> entry : orderStore.entrySet()) {
            System.out.println("  [Key: " + entry.getKey() + "]");
            entry.getValue().displayOrder();
            System.out.println("  " + "-".repeat(40));
        }
    }

    public void demonstrateRetrievalSpeed() {
        System.out.println("\n  Populating HashMap with 10,000 dummy customers...");
        HashMap<String, Customer> bigMap = new HashMap<>();
        for (int i = 0; i < 10_000; i++) {
            String id = "C" + String.format("%05d", i);
            bigMap.put(id, new Customer(id, "User " + i, "010000" + i));
        }
        String target = "C09999";

        long t1 = System.nanoTime();
        Customer hashResult = bigMap.get(target);
        long t2 = System.nanoTime();
        System.out.printf("  HashMap get()  : %5d ns  → O(1)%n", (t2 - t1));

        t1 = System.nanoTime();
        Customer linearResult = null;
        for (Customer c : bigMap.values()) {
            if (c.getId().equals(target)) {
                linearResult = c;
                break;
            }
        }
        t2 = System.nanoTime();
        System.out.printf("  Linear search  : %5d ns  → O(n) where n=10,000%n", (t2 - t1));

        System.out.println("  HashMap found  : " + (hashResult != null ? hashResult.getId() : "not found"));
        System.out.println("  Linear found   : " + (linearResult != null ? linearResult.getId() : "not found"));
        System.out.println("\n  As n grows, HashMap stays flat while linear grows linearly.");
    }
}
