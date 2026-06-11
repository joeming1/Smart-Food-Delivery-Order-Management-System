package search_and_data_retrieval_system;

import java.lang.reflect.Field;

public class TreeVisualizer {

    public static void main(String[] args) {
        // Initialize the trees
        FoodBST bst = new FoodBST();
        FoodAVLTree avl = new FoodAVLTree();

        // Create some sample data to visualize
        FoodItem[] sampleFoods = {
                new FoodItem("Nasi Lemak", "Rice", 8.50, "A_Restaurant"),
                new FoodItem("Char Kuey Teow", "Noodle", 9.00, "B_Restaurant"),
                new FoodItem("Roti Canai", "Bread", 2.00, "C_Restaurant"),
                new FoodItem("Ayam Goreng", "Chicken", 7.00, "D_Restaurant"),
                new FoodItem("Mee Goreng", "Noodle", 7.50, "C_Restaurant"),
                new FoodItem("Laksa", "Soup", 10.00, "B_Restaurant"),
                new FoodItem("Burger Special", "Burger", 6.50, "Uncle Burger"),
                new FoodItem("Teh Tarik", "Drink", 2.50, "C_Restaurant"),
                new FoodItem("Teh Tarik1", "Drink", 2.50, "C_Restaurant"),
                new FoodItem("Teh Tarik2", "Drink", 2.50, "C_Restaurant"),
                new FoodItem("Teh Tarik3", "Drink", 2.50, "C_Restaurant"),
                new FoodItem("Teh Tarik4", "Drink", 2.50, "C_Restaurant"),
        };

        System.out.println("Inserting sample data into BST and AVL Tree...");
        for (FoodItem f : sampleFoods) {
            bst.insert(f);
            avl.insert(f);
        }

        // Run the visualizer
        visualizeTrees(bst, avl);
    }

    public static void visualizeTrees(FoodBST bst, FoodAVLTree avl) {
        System.out.println("\n========================================");
        System.out.println("          TREE VISUALIZATION            ");
        System.out.println("========================================");

        System.out.println("\n--- Binary Search Tree (BST) ---");
        System.out.println("(Notice how it might become unbalanced depending on insertion order)");
        visualizeBST(bst);

        System.out.println("\n--- AVL Tree (Balanced) ---");
        System.out.println("(Notice how it auto-rotates to maintain a balanced height)");
        visualizeAVL(avl);
        System.out.println("========================================\n");
    }

    private static void visualizeBST(FoodBST bst) {
        try {
            Field rootField = FoodBST.class.getDeclaredField("root");
            rootField.setAccessible(true);
            Object rootNode = rootField.get(bst);

            if (rootNode == null) {
                System.out.println("(Empty BST)");
                return;
            }

            Class<?> nodeClass = Class.forName("search_and_data_retrieval_system.FoodBST$BSTNode");
            printNode(rootNode, "", true, nodeClass);

        } catch (Exception e) {
            System.out.println("Could not visualize BST: " + e.getMessage());
        }
    }

    private static void visualizeAVL(FoodAVLTree avl) {
        try {
            Field rootField = FoodAVLTree.class.getDeclaredField("root");
            rootField.setAccessible(true);
            Object rootNode = rootField.get(avl);

            if (rootNode == null) {
                System.out.println("(Empty AVL Tree)");
                return;
            }

            Class<?> nodeClass = Class.forName("search_and_data_retrieval_system.FoodAVLTree$AVLNode");
            printNode(rootNode, "", true, nodeClass);

        } catch (Exception e) {
            System.out.println("Could not visualize AVL: " + e.getMessage());
        }
    }

    private static void printNode(Object node, String prefix, boolean isTail, Class<?> nodeClass) throws Exception {
        if (node == null)
            return;

        // Extract FoodItem from the node
        Field foodField = nodeClass.getDeclaredField("food");
        foodField.setAccessible(true);
        Object food = foodField.get(node);

        // Extract food name
        String foodName = "Unknown";
        if (food != null) {
            foodName = ((FoodItem) food).getName();
        }

        // Print current node
        System.out.println(prefix + (isTail ? "└── " : "├── ") + foodName);

        // Extract left and right children
        Field leftField = nodeClass.getDeclaredField("left");
        leftField.setAccessible(true);
        Object left = leftField.get(node);

        Field rightField = nodeClass.getDeclaredField("right");
        rightField.setAccessible(true);
        Object right = rightField.get(node);

        // Recursively print children
        if (left != null || right != null) {
            String childPrefix = prefix + (isTail ? "    " : "│   ");
            if (left != null && right != null) {
                printNode(left, childPrefix, false, nodeClass);
                printNode(right, childPrefix, true, nodeClass);
            } else if (left != null) {
                printNode(left, childPrefix, true, nodeClass);
            } else {
                // To keep structural accuracy if there's only a right child
                System.out.println(childPrefix + "├── (null)");
                printNode(right, childPrefix, true, nodeClass);
            }
        }
    }
}