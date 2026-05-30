package user_management;

import java.util.ArrayList;

public class UserManager {
    private ArrayList<Customer> customers;

    public UserManager() {
        this.customers = new ArrayList<>();
    }

    // Time Complexity: O(1)
    public void addCustomer(Customer customer) {
        customers.add(customer);
        System.out.println("Customer added successfully!");
    }

    // Time Complexity: O(n) due to searching and element shifting
    public boolean deleteCustomer(String id) {
        for (int i = 0; i < customers.size(); i++) {
            if (customers.get(i).getId().equalsIgnoreCase(id)) {
                customers.remove(i);
                return true;
            }
        }
        return false;
    }

    // Time Complexity: O(n) to traverse and print
    public void displayAllCustomers() {
        if (customers.isEmpty()) {
            System.out.println("No customers found.");
            return;
        }
        System.out.println("\n-----------------------------------------------------");
        System.out.format("| %-10s | %-20s | %-15s |\n", "Customer ID", "Name", "Phone");
        System.out.println("-----------------------------------------------------");
        for (Customer c : customers) {
            System.out.println(c);
        }
        System.out.println("-----------------------------------------------------");
    }

    public ArrayList<Customer> getCustomers() {
        return customers;
    }
}