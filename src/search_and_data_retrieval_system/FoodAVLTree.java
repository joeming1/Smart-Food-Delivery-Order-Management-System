package search_and_data_retrieval_system;

public class FoodAVLTree {

    private static class AVLNode {
        FoodItem food;
        AVLNode left, right;
        int height;

        AVLNode(FoodItem food) {
            this.food   = food;
            this.height = 1;
        }
    }

    private AVLNode root;

    private int height(AVLNode n) {
        return (n == null) ? 0 : n.height;
    }

    private int balanceFactor(AVLNode n) {
        return (n == null) ? 0 : height(n.left) - height(n.right);
    }

    private void updateHeight(AVLNode n) {
        n.height = 1 + Math.max(height(n.left), height(n.right));
    }


    private AVLNode rotateRight(AVLNode y) {
        AVLNode x  = y.left;
        AVLNode T2 = x.right;

        x.right = y;
        y.left  = T2;

        updateHeight(y);
        updateHeight(x);
        return x;
    }


    private AVLNode rotateLeft(AVLNode x) {
        AVLNode y  = x.right;
        AVLNode T2 = y.left;

        y.left  = x;
        x.right = T2;

        updateHeight(x);
        updateHeight(y);
        return y;
    }

    public void insert(FoodItem food) {
        root = insertRec(root, food);
    }

    private AVLNode insertRec(AVLNode node, FoodItem food) {

        if (node == null) return new AVLNode(food);

        int cmp = food.getName().compareToIgnoreCase(node.food.getName());
        if      (cmp < 0) node.left  = insertRec(node.left,  food);
        else if (cmp > 0) node.right = insertRec(node.right, food);
        else {
            System.out.println("[AVL] Duplicate name ignored: " + food.getName());
            return node;
        }

        updateHeight(node);

        int bf = balanceFactor(node);

        if (bf > 1 && food.getName().compareToIgnoreCase(node.left.food.getName()) < 0)
            return rotateRight(node);

        if (bf < -1 && food.getName().compareToIgnoreCase(node.right.food.getName()) > 0)
            return rotateLeft(node);

        if (bf > 1 && food.getName().compareToIgnoreCase(node.left.food.getName()) > 0) {
            node.left = rotateLeft(node.left);
            return rotateRight(node);
        }

        if (bf < -1 && food.getName().compareToIgnoreCase(node.right.food.getName()) < 0) {
            node.right = rotateRight(node.right);
            return rotateLeft(node);
        }

        return node;
    }

    public FoodItem search(String name) {
        AVLNode result = searchRec(root, name);
        return (result == null) ? null : result.food;
    }

    private AVLNode searchRec(AVLNode node, String name) {
        if (node == null) return null;

        int cmp = name.compareToIgnoreCase(node.food.getName());
        if      (cmp == 0) return node;
        else if (cmp < 0)  return searchRec(node.left,  name);
        else               return searchRec(node.right, name);
    }

    public void displayInOrder() {
        if (root == null) {
            System.out.println("  (no food items in AVL tree)");
            return;
        }
        System.out.println("  Name                 | Category     | Price   | Restaurant");
        System.out.println("  " + "-".repeat(68));
        inOrderRec(root);
    }

    private void inOrderRec(AVLNode node) {
        if (node == null) return;
        inOrderRec(node.left);
        System.out.println("  " + node.food);
        inOrderRec(node.right);
    }

    public int getTreeHeight() {
        return height(root);
    }

    public int size() {
        return sizeRec(root);
    }

    private int sizeRec(AVLNode node) {
        if (node == null) return 0;
        return 1 + sizeRec(node.left) + sizeRec(node.right);
    }

    public java.util.List<FoodItem> getAllFoods() {
        java.util.List<FoodItem> list = new java.util.ArrayList<>();
        collectInOrder(root, list);
        return list;
    }

    private void collectInOrder(AVLNode node, java.util.List<FoodItem> list) {
        if (node == null) return;
        collectInOrder(node.left, list);
        list.add(node.food);
        collectInOrder(node.right, list);
    }
}
