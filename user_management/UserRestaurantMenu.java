package user_management;

import java.util.Scanner;

public class UserRestaurantMenu {
    private UserManager userManager;
    private RestaurantManager restaurantManager;
    private Scanner scanner;

    public UserRestaurantMenu(UserManager userManager, RestaurantManager restaurantManager) {
        this.userManager = userManager;
        this.restaurantManager = restaurantManager;
        this.scanner = new Scanner(System.in);
    }

    public void runMenu() {
        int choice;
        do {
            System.out.println("\n=== User & Restaurant Management System ===");
            System.out.println("1. Add Customer");
            System.out.println("2. Delete Customer");
            System.out.println("3. Display All Customers");
            System.out.println("4. Add Restaurant");
            System.out.println("5. Delete Restaurant");
            System.out.println("6. Display All Restaurants");
            System.out.println("0. Back to Main Menu");
            System.out.print("Enter choice: ");
            
            while (!scanner.hasNextInt()) {
                System.out.print("Invalid input. Enter a number: ");
                scanner.next();
            }
            choice = scanner.nextInt();
            scanner.nextLine(); // Clear buffer

            switch (choice) {
                case 1:
                    System.out.print("Enter Customer ID: ");
                    String cId = scanner.nextLine();
                    System.out.print("Enter Customer Name: ");
                    String cName = scanner.nextLine();
                    System.out.print("Enter Phone Number: ");
                    String cPhone = scanner.nextLine();
                    userManager.addCustomer(new Customer(cId, cName, cPhone));
                    break;
                case 2:
                    System.out.print("Enter Customer ID to delete: ");
                    String delCId = scanner.nextLine();
                    if (userManager.deleteCustomer(delCId)) {
                        System.out.println("Customer deleted successfully.");
                    } else {
                        System.out.println("Customer ID not found.");
                    }
                    break;
                case 3:
                    userManager.displayAllCustomers();
                    break;
                case 4:
                    System.out.print("Enter Restaurant ID: ");
                    String rId = scanner.nextLine();
                    System.out.print("Enter Restaurant Name: ");
                    String rName = scanner.nextLine();
                    System.out.print("Enter Cuisine Type: ");
                    String rCuisine = scanner.nextLine();
                    restaurantManager.addRestaurant(new Restaurant(rId, rName, rCuisine));
                    break;
                case 5:
                    System.out.print("Enter Restaurant ID to delete: ");
                    String delRId = scanner.nextLine();
                    if (restaurantManager.deleteRestaurant(delRId)) {
                        System.out.println("Restaurant deleted successfully.");
                    } else {
                        System.out.println("Restaurant ID not found.");
                    }
                    break;
                case 6:
                    restaurantManager.displayAllRestaurants();
                    break;
                case 0:
                    System.out.println("Returning to main system...");
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        } while (choice != 0);
    }
}