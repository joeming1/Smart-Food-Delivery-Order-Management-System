package search_and_data_retrieval_system;

public class FoodBST {

    private static class BSTNode {
        FoodItem food;
        BSTNode left, right;

        BSTNode(FoodItem food) {
            this.food = food;
        }
    }

    private BSTNode root;

    public void insert(FoodItem food) {
        root = insertRec(root, food);
    }

    private BSTNode insertRec(BSTNode node, FoodItem food) {
        if (node == null) return new BSTNode(food);

        int cmp = food.getName().compareToIgnoreCase(node.food.getName());
        if (cmp < 0)       node.left  = insertRec(node.left,  food);
        else if (cmp > 0)  node.right = insertRec(node.right, food);
        else System.out.println("[BST] Duplicate name ignored: " + food.getName());

        return node;
    }

    public FoodItem search(String name) {
        BSTNode result = searchRec(root, name);
        return (result == null) ? null : result.food;
    }

    private BSTNode searchRec(BSTNode node, String name) {
        if (node == null) return null;

        int cmp = name.compareToIgnoreCase(node.food.getName());
        if (cmp == 0)      return node;
        else if (cmp < 0)  return searchRec(node.left,  name);
        else               return searchRec(node.right, name);
    }

    public void displayInOrder() {
        if (root == null) {
            System.out.println("  (no food items in BST)");
            return;
        }
        System.out.println("  Name                 | Category     | Price   | Restaurant");
        System.out.println("  " + "-".repeat(68));
        inOrderRec(root);
    }

    private void inOrderRec(BSTNode node) {
        if (node == null) return;
        inOrderRec(node.left);
        System.out.println("  " + node.food);
        inOrderRec(node.right);
    }

    public int size() {
        return sizeRec(root);
    }

    private int sizeRec(BSTNode node) {
        if (node == null) return 0;
        return 1 + sizeRec(node.left) + sizeRec(node.right);
    }
}
