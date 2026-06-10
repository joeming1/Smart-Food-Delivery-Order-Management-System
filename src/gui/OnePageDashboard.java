package gui;

import Route_Optimization_System.DijkstraAlgorithm;
import Route_Optimization_System.Edge;
import Route_Optimization_System.Graph;
import Route_Optimization_System.Node;
import delivery_assignment_system.DeliveryService;
import delivery_assignment_system.Rider;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import order_processing.order;
import user_management.Customer;
import user_management.Restaurant;
import user_management.RestaurantManager;
import user_management.UserManager;

public class OnePageDashboard extends JFrame {

    private static final long MINUTE_MS = 5000L; // demo scale: 1 minute = 5 seconds

    private final UserManager userManager = new UserManager();
    private final RestaurantManager restaurantManager = new RestaurantManager();
    private final DeliveryService deliveryService = new DeliveryService();
    private final Graph routeGraph = new Graph();
    private final Map<String, String> locationTypes = new LinkedHashMap<>();
    private final List<order> orders = new ArrayList<>();
    private final List<DeliveryTrack> activeTracks = new ArrayList<>();

    private final DefaultTableModel usersModel = tableModel("ID", "Name", "Phone", "Location");
    private final DefaultTableModel restaurantsModel = tableModel("ID", "Name", "Cuisine", "Location");
    private final DefaultTableModel ridersModel = tableModel("ID", "Name", "Distance", "Location", "State");
    private final DefaultTableModel ordersModel = tableModel("Order ID", "Customer", "Restaurant", "Content", "Status", "Rider", "Route");
    private final DefaultTableModel locationsModel = tableModel("Location ID", "Type");
    private final DefaultTableModel routesModel = tableModel("From", "To", "Weight");

    private final JComboBox<String> userSelect = new JComboBox<>();
    private final JComboBox<String> restaurantSelect = new JComboBox<>();
    private final JComboBox<String> riderSelect = new JComboBox<>();
    private final JComboBox<String> orderSelect = new JComboBox<>();
    private final JComboBox<String> locationSelect = new JComboBox<>();
    private final JComboBox<String> routeFromSelect = new JComboBox<>();
    private final JComboBox<String> routeToSelect = new JComboBox<>();
    private final JComboBox<String> dispatchSourceSelect = new JComboBox<>();
    private final JComboBox<String> dispatchTargetSelect = new JComboBox<>();
    private final JComboBox<String> dispatchOrderSelect = new JComboBox<>();

    private final JTextField userIdField = new JTextField(10);
    private final JTextField userNameField = new JTextField(10);
    private final JTextField userPhoneField = new JTextField(10);
    private final JComboBox<String> userLocationSelect = new JComboBox<>();

    private final JTextField restaurantIdField = new JTextField(10);
    private final JTextField restaurantNameField = new JTextField(10);
    private final JTextField restaurantCuisineField = new JTextField(10);
    private final JComboBox<String> restaurantLocationSelect = new JComboBox<>();

    private final JTextField riderIdField = new JTextField(10);
    private final JTextField riderNameField = new JTextField(10);
    private final JTextField riderDistanceField = new JTextField(10);
    private final JTextField riderTimeField = new JTextField(10);

    private final JTextField orderContentField = new JTextField(14);
    private final JTextField routeWeightField = new JTextField(10);
    private final JTextField locationIdField = new JTextField(10);
    private final JComboBox<String> locationTypeSelect = new JComboBox<>(new String[]{"Customer", "Restaurant", "Hub", "Other"});

    private final JTextField dispatchMinutesField = new JTextField(10);
    private final JLabel statusLabel = new JLabel("Ready.");
    private final JLabel riderHintLabel = new JLabel("Select an order and locations to dispatch a rider.");

    private final RouteMapPanel mapPanel = new RouteMapPanel();
    private final Timer animationTimer;

    public OnePageDashboard() {
        super("Smart Food Delivery One-Page Dashboard");
        userLocationSelect.setEditable(true);
        restaurantLocationSelect.setEditable(true);
        dispatchSourceSelect.setEnabled(false);
        dispatchTargetSelect.setEnabled(false);
        seedDemoData();
        buildUi();
        refreshAllViews();

        animationTimer = new Timer(100, e -> {
            updateActiveTracks();
            refreshRiders();
            mapPanel.repaint();
        });
        animationTimer.start();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1800, 1200));
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new OnePageDashboard().setVisible(true));
    }

    private void buildUi() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 246, 249));

        JLabel title = new JLabel("Smart Food Delivery Dashboard", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(14, 14, 6, 14));

        JPanel north = new JPanel(new BorderLayout());
        north.add(title, BorderLayout.NORTH);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        toolbar.add(button("Refresh All Data", e -> refreshAllViews()));
        north.add(toolbar, BorderLayout.CENTER);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 14, 10, 14));
        riderHintLabel.setBorder(BorderFactory.createEmptyBorder(4, 14, 10, 14));
        riderHintLabel.setForeground(new Color(90, 96, 105));
        statusPanel.add(statusLabel, BorderLayout.NORTH);
        statusPanel.add(riderHintLabel, BorderLayout.SOUTH);
        north.add(statusPanel, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);

        // Right-aligned Tabbed Action Panel
        JTabbedPane actionTabs = new JTabbedPane();
        actionTabs.setPreferredSize(new Dimension(420, 720));
        actionTabs.addTab("Dispatch", scrollablePanel(buildDispatchTab()));
        actionTabs.addTab("Customer", scrollablePanel(buildUserTab()));
        actionTabs.addTab("Restaurant", scrollablePanel(buildRestaurantTab()));
        actionTabs.addTab("Rider", scrollablePanel(buildRiderTab()));
        actionTabs.addTab("Order", scrollablePanel(buildOrderTab()));
        actionTabs.addTab("Routes & Loc", scrollablePanel(buildRouteTab()));

        dispatchOrderSelect.addActionListener(e -> {
            String selected = (String) dispatchOrderSelect.getSelectedItem();
            if (selected == null || selected.isEmpty()) return;
            int orderId = parseOrderId(selected);
            order o = findOrder(orderId);
            if (o != null) {
                String restLoc = o.getSourceLocation();
                String custLoc = o.getTargetLocation();
                if (restLoc != null) dispatchSourceSelect.setSelectedItem(restLoc);
                if (custLoc != null) dispatchTargetSelect.setSelectedItem(custLoc);
            }
        });

        JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));
        centerPanel.add(mapPanel, BorderLayout.CENTER);
        centerPanel.add(legendPanel(), BorderLayout.SOUTH);

        JPanel tablesGrid = new JPanel(new GridLayout(2, 3, 8, 8));
        tablesGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tablesGrid.add(tableCard("Users", usersModel));
        tablesGrid.add(tableCard("Restaurants", restaurantsModel));
        tablesGrid.add(tableCard("Riders", ridersModel));
        tablesGrid.add(tableCard("Orders", ordersModel));
        tablesGrid.add(tableCard("Locations", locationsModel));
        tablesGrid.add(tableCard("Routes", routesModel));
        tablesGrid.setPreferredSize(new Dimension(1400, 240));

        JPanel middle = new JPanel(new BorderLayout(10, 10));
        middle.add(centerPanel, BorderLayout.CENTER);
        middle.add(actionTabs, BorderLayout.EAST);

        add(middle, BorderLayout.CENTER);
        add(tablesGrid, BorderLayout.SOUTH);

        mapPanel.setBorder(BorderFactory.createTitledBorder("Live Route Map"));
    }

    private JPanel buildDispatchTab() {
        return section("Dispatch Control Center",
            section("Dispatch Order",
                formRow("Select Order", dispatchOrderSelect),
                formRow("Source (Rest)", dispatchSourceSelect),
                formRow("Target (Cust)", dispatchTargetSelect),
                formRow("Travel Time (m)", dispatchMinutesField),
                Box.createVerticalStrut(6),
                buttonRow(button("Dispatch Selected Order", e -> dispatchSelectedOrder()))
            ),
            Box.createVerticalStrut(10),
            section("Complete Active Delivery",
                formRow("Active Rider", riderSelect),
                Box.createVerticalStrut(6),
                buttonRow(button("Complete Selected Delivery", e -> completeSelectedDelivery()))
            ),
            Box.createVerticalStrut(30)
        );
    }

    private JPanel buildUserTab() {
        return section("Add New Customer",
            formRow("Customer ID", userIdField),
            formRow("Full Name", userNameField),
            formRow("Phone Number", userPhoneField),
            formRow("Location ID", userLocationSelect),
            Box.createVerticalStrut(12),
            buttonRow(button("Add Customer", e -> addUserFromFields())),
            Box.createVerticalStrut(30)
        );
    }

    private JPanel buildRestaurantTab() {
        return section("Add New Restaurant",
            formRow("Restaurant ID", restaurantIdField),
            formRow("Name", restaurantNameField),
            formRow("Cuisine Type", restaurantCuisineField),
            formRow("Location ID", restaurantLocationSelect),
            Box.createVerticalStrut(12),
            buttonRow(button("Add Restaurant", e -> addRestaurantFromFields())),
            Box.createVerticalStrut(30)
        );
    }

    private JPanel buildRiderTab() {
        return section("Add New Rider",
            formRow("Rider ID", riderIdField),
            formRow("Name", riderNameField),
            formRow("Distance (km)", riderDistanceField),
            formRow("Time (mins)", riderTimeField),
            Box.createVerticalStrut(12),
            buttonRow(button("Add Rider", e -> addRiderFromFields())),
            Box.createVerticalStrut(30)
        );
    }

    private JPanel buildOrderTab() {
        return section("Place New Order",
            formRow("Customer", userSelect),
            formRow("Restaurant", restaurantSelect),
            formRow("Content", orderContentField),
            Box.createVerticalStrut(12),
            buttonRow(button("Add Order", e -> addOrderFromFields())),
            Box.createVerticalStrut(30)
        );
    }

    private JPanel buildRouteTab() {
        return section("Routes & Locations Control",
            section("Add Location Node",
                formRow("Location ID", locationIdField),
                formRow("Type", locationTypeSelect),
                Box.createVerticalStrut(6),
                buttonRow(button("Add Location", e -> addLocationFromFields()))
            ),
            Box.createVerticalStrut(10),
            section("Add Connected Route",
                formRow("From Location", routeFromSelect),
                formRow("To Location", routeToSelect),
                formRow("Weight / Cost", routeWeightField),
                Box.createVerticalStrut(6),
                buttonRow(button("Add Route", e -> addRouteFromFields()))
            ),
            Box.createVerticalStrut(30)
        );
    }

    private JPanel buttonRow(java.awt.Component... components) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        p.setOpaque(false);
        for (java.awt.Component c : components) p.add(c);
        return p;
    }

    private JPanel legendPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        panel.add(new JLabel("Green circle = active rider"));
        panel.add(new JLabel("Highlighted path = selected route"));
        panel.add(new JLabel("Demo speed: 5 seconds = 1 minute"));
        return panel;
    }

    private JPanel section(String title, java.awt.Component... components) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)));
        for (java.awt.Component component : components) {
            panel.add(component);
            panel.add(Box.createVerticalStrut(6));
        }
        return panel;
    }

    private JPanel formRow(String label, java.awt.Component component) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        JLabel text = new JLabel(label + ":");
        text.setPreferredSize(new Dimension(95, 24));
        row.add(text, BorderLayout.WEST);
        row.add(component, BorderLayout.CENTER);
        return row;
    }

    private JScrollPane tableCard(String title, DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(24);
        table.setFillsViewportHeight(true);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder(title));
        return scroll;
    }

    private JButton button(String text, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.addActionListener(listener);
        return button;
    }

    private JScrollPane scrollablePanel(JPanel panel) {
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        return scroll;
    }

    private void seedDemoData() {
        userManager.addCustomer(new Customer("C001", "Ali Ahmad", "0123456789", "E_Customer"));
        userManager.addCustomer(new Customer("C002", "Sarah Kaur", "0187654321", "F_Customer"));
        userManager.addCustomer(new Customer("C003", "Bobby Lim", "0112233445", "G_Customer"));

        restaurantManager.addRestaurant(new Restaurant("R001", "Warung Mak Jah", "Rice", "A_Restaurant"));
        restaurantManager.addRestaurant(new Restaurant("R002", "Penang Corner", "Noodle", "B_Restaurant"));
        restaurantManager.addRestaurant(new Restaurant("R003", "Mamak Selera", "Local", "C_Restaurant"));

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

        order first = new order("Ali Ahmad", "Warung Mak Jah", "Nasi Lemak x2");
        first.setSourceLocation("A_Restaurant");
        first.setTargetLocation("E_Customer");
        first.setStatus("CONFIRMED");
        orders.add(first);

        order second = new order("Sarah Kaur", "Penang Corner", "Char Kuey Teow x1");
        second.setSourceLocation("B_Restaurant");
        second.setTargetLocation("F_Customer");
        second.setStatus("PENDING");
        orders.add(second);

        order third = new order("Bobby Lim", "Mamak Selera", "Roti Canai x3, Teh Tarik x2");
        third.setSourceLocation("C_Restaurant");
        third.setTargetLocation("G_Customer");
        third.setStatus("CONFIRMED");
        orders.add(third);
    }

    private void addUserFromFields() {
        String id = userIdField.getText().trim();
        String name = userNameField.getText().trim();
        String phone = userPhoneField.getText().trim();
        String location = selectedValue(userLocationSelect);
        if (id.isEmpty() || name.isEmpty()) {
            setStatus("Please enter Customer ID and Name.");
            return;
        }
        userManager.addCustomer(new Customer(id, name, phone, location.isEmpty() ? "UNKNOWN" : location));
        if (!location.isEmpty()) {
            addLocationInternal(location, "Customer");
        }
        userIdField.setText("");
        userNameField.setText("");
        userPhoneField.setText("");
        refreshAllViews();
        setStatus("Customer added.");
    }

    private void addRestaurantFromFields() {
        String id = restaurantIdField.getText().trim();
        String name = restaurantNameField.getText().trim();
        String cuisine = restaurantCuisineField.getText().trim();
        String location = selectedValue(restaurantLocationSelect);
        if (id.isEmpty() || name.isEmpty()) {
            setStatus("Please enter Restaurant ID and Name.");
            return;
        }
        restaurantManager.addRestaurant(new Restaurant(id, name, cuisine, location.isEmpty() ? "UNKNOWN" : location));
        if (!location.isEmpty()) {
            addLocationInternal(location, "Restaurant");
        }
        restaurantIdField.setText("");
        restaurantNameField.setText("");
        restaurantCuisineField.setText("");
        refreshAllViews();
        setStatus("Restaurant added.");
    }

    private void addRiderFromFields() {
        String id = riderIdField.getText().trim();
        String name = riderNameField.getText().trim();
        if (id.isEmpty() || name.isEmpty()) {
            setStatus("Please enter Rider ID and Name.");
            return;
        }
        double distance = parseDouble(riderDistanceField.getText().trim(), 0.0);
        int time = parseInt(riderTimeField.getText().trim(), 5);
        deliveryService.addRider(new Rider(id, name, distance, time));
        riderIdField.setText("");
        riderNameField.setText("");
        riderDistanceField.setText("");
        riderTimeField.setText("");
        refreshAllViews();
        setStatus("Rider added.");
    }

    private void addOrderFromFields() {
        String customer = selectedValue(userSelect);
        String restaurant = selectedValue(restaurantSelect);
        String content = orderContentField.getText().trim();
        if (customer.isEmpty() || restaurant.isEmpty() || content.isEmpty()) {
            setStatus("Please select Customer, Restaurant, and enter Content.");
            return;
        }
        String custName = stripLeadingId(customer);
        String restName = stripLeadingId(restaurant);
        order newOrder = new order(custName, restName, content);
        
        String restLoc = getRestaurantLocation(restName);
        String custLoc = getCustomerLocation(custName);
        newOrder.setSourceLocation(restLoc != null ? restLoc : "UNKNOWN");
        newOrder.setTargetLocation(custLoc != null ? custLoc : "UNKNOWN");
        
        orders.add(newOrder);
        orderContentField.setText("");
        refreshAllViews();
        setStatus("Order added.");
    }

    private void addLocationFromFields() {
        String id = locationIdField.getText().trim();
        if (id.isEmpty()) {
            setStatus("Please enter Location ID.");
            return;
        }
        String type = selectedValue(locationTypeSelect);
        if (type.isEmpty()) type = "Other";
        addLocationInternal(id, type);
        locationIdField.setText("");
        refreshAllViews();
        setStatus("Location added.");
    }

    private void addRouteFromFields() {
        String from = selectedValue(routeFromSelect);
        String to = selectedValue(routeToSelect);
        if (from.isEmpty() || to.isEmpty()) {
            setStatus("Please select 'From' and 'To' locations.");
            return;
        }
        double weight = parseDouble(routeWeightField.getText().trim(), 1.0);
        addRouteInternal(from, to, weight);
        routeWeightField.setText("");
        refreshAllViews();
        setStatus("Route added.");
    }

    private void dispatchSelectedOrder() {
        String orderLabel = selectedValue(dispatchOrderSelect);
        if (orderLabel.isEmpty()) return;
        order targetOrder = findOrder(parseOrderId(orderLabel));
        if (targetOrder == null) return;

        String source = targetOrder.getSourceLocation();
        String target = targetOrder.getTargetLocation();
        if (source == null || target == null || source.equals("UNKNOWN") || target.equals("UNKNOWN")) {
            setStatus("Order source or target is unknown.");
            return;
        }

        Rider rider = deliveryService.assignBestRider();
        if (rider == null) {
            setStatus("No riders available.");
            return;
        }

        double travelMinutes = parseDouble(dispatchMinutesField.getText().trim(), -1);
        String riderStart = rider.getCurrentLocationId();
        
        // Shortest path from Rider's current location to Restaurant (source)
        DijkstraAlgorithm.Result pickupResult = DijkstraAlgorithm.shortestPath(routeGraph, riderStart, source);
        // Shortest path from Restaurant (source) to Customer (target)
        DijkstraAlgorithm.Result deliveryResult = DijkstraAlgorithm.shortestPath(routeGraph, source, target);
        
        List<String> path = new ArrayList<>();
        double pickupTime = 0.0;
        if (pickupResult.getDistance() != Double.POSITIVE_INFINITY && !pickupResult.getPath().isEmpty()) {
            for (Node node : pickupResult.getPath()) {
                path.add(node.getId());
            }
            pickupTime = pickupResult.getDistance();
        } else {
            path.add(source);
        }
        
        List<Node> deliveryPathNodes = deliveryResult.getPath();
        if (deliveryPathNodes != null) {
            for (int i = 0; i < deliveryPathNodes.size(); i++) {
                if (i == 0 && !path.isEmpty() && path.get(path.size() - 1).equals(deliveryPathNodes.get(i).getId())) {
                    continue;
                }
                path.add(deliveryPathNodes.get(i).getId());
            }
        }
        
        double deliveryTime = (deliveryResult.getDistance() != Double.POSITIVE_INFINITY) ? deliveryResult.getDistance() : 0.0;
        if (travelMinutes < 0) {
            travelMinutes = pickupTime + deliveryTime;
        }

        targetOrder.setStatus("DISPATCHED");
        targetOrder.assignRider(rider.getRiderId(), source, target, travelMinutes);
        activeTracks.add(new DeliveryTrack(rider, path, source, target, travelMinutes, System.currentTimeMillis()));
        orders.remove(targetOrder);
        
        mapPanel.setHighlightedPath(path);
        refreshAllViews();
        setStatus("Dispatched " + rider.getName() + " for order #" + targetOrder.getOrderID() + " using " + travelMinutes + " minutes.");
        riderHintLabel.setText(rider.getName() + " is travelling from " + riderStart + " -> " + source + " -> " + target + " (~" + travelMinutes + " min).");
    }

    private void completeSelectedDelivery() {
        if (activeTracks.isEmpty()) {
            setStatus("No active deliveries.");
            return;
        }
        String selected = selectedValue(riderSelect);
        if (selected.isEmpty()) {
            setStatus("Please select an active rider to complete.");
            return;
        }
        String riderId = parseToken(selected);
        DeliveryTrack track = activeTracks.stream()
                .filter(t -> t.rider.getRiderId().equalsIgnoreCase(riderId))
                .findFirst().orElse(null);
        String finalLoc = track != null ? track.target : "UNKNOWN";
        if (deliveryService.completeDelivery(riderId, finalLoc)) {
            activeTracks.removeIf(t -> t.rider.getRiderId().equalsIgnoreCase(riderId));
            orders.stream()
                    .filter(o -> riderId.equalsIgnoreCase(o.getAssignedRiderId()))
                    .forEach(o -> o.setStatus("DELIVERED"));
            mapPanel.setHighlightedPath(Collections.emptyList());
            refreshAllViews();
            setStatus("Delivery completed for rider " + riderId + ".");
        } else {
            setStatus("Failed to complete delivery for rider " + riderId + ".");
        }
    }

    private void refreshAllViews() {
        refreshUsers();
        refreshRestaurants();
        refreshRiders();
        refreshOrders();
        refreshLocations();
        refreshRoutes();
        refreshSelections();
        mapPanel.setGraph(routeGraph, locationTypes);
        mapPanel.setActiveTracks(activeTracks);
        mapPanel.repaint();
    }

    private void refreshUsers() {
        usersModel.setRowCount(0);
        for (Customer customer : userManager.getCustomers()) {
            usersModel.addRow(new Object[]{customer.getId(), customer.getName(), customer.getPhone(), customer.getLocationId()});
        }
    }

    private void refreshRestaurants() {
        restaurantsModel.setRowCount(0);
        for (Restaurant restaurant : restaurantManager.getRestaurants()) {
            restaurantsModel.addRow(new Object[]{restaurant.getId(), restaurant.getName(), restaurant.getCuisineType(), restaurant.getLocationId()});
        }
    }

    private void refreshRiders() {
        ridersModel.setRowCount(0);
        Set<String> busy = new HashSet<>();
        for (Rider rider : deliveryService.getBusyRiders()) {
            busy.add(rider.getRiderId());
        }
        for (Rider rider : deliveryService.getAllRiders()) {
            ridersModel.addRow(new Object[]{
                rider.getRiderId(), 
                rider.getName(), 
                String.format("%.1f km", rider.getDistanceKm()), 
                "Currently at " + rider.getCurrentLocationId(), 
                busy.contains(rider.getRiderId()) ? "Busy" : "Available"
            });
        }
    }

    private void refreshOrders() {
        ordersModel.setRowCount(0);
        for (order o : orders) {
            String route = o.getSourceLocation() == null ? "-" : o.getSourceLocation() + " -> " + o.getTargetLocation();
            ordersModel.addRow(new Object[]{o.getOrderID(), o.getCustomerName(), o.getRestaurantName(), o.getOrderContent(), o.getStatus(), o.getAssignedRiderId() == null ? "-" : o.getAssignedRiderId(), route});
        }
    }

    private void refreshLocations() {
        locationsModel.setRowCount(0);
        for (Map.Entry<String, String> entry : locationTypes.entrySet()) {
            locationsModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
    }

    private void refreshRoutes() {
        routesModel.setRowCount(0);
        for (Node from : routeGraph.getNodes()) {
            for (Edge edge : routeGraph.getEdges(from)) {
                routesModel.addRow(new Object[]{from.getId(), edge.getTarget().getId(), edge.getWeight()});
            }
        }
    }

    private void refreshSelections() {
        fillCombo(userSelect, userManager.getCustomers().stream().map(c -> c.getId() + " | " + c.getName()).toList());
        fillCombo(restaurantSelect, restaurantManager.getRestaurants().stream().map(r -> r.getId() + " | " + r.getName()).toList());
        fillCombo(riderSelect, deliveryService.getBusyRiders().stream().map(r -> r.getRiderId() + " | " + r.getName()).toList());
        fillCombo(orderSelect, orders.stream().map(o -> o.getOrderID() + " | " + o.getCustomerName() + " | " + o.getRestaurantName()).toList());
        
        List<String> pendingOrders = orders.stream()
                .filter(o -> "PENDING".equals(o.getStatus()) || "CONFIRMED".equals(o.getStatus()))
                .map(this::formatOrderForCombo)
                .toList();
        fillCombo(dispatchOrderSelect, pendingOrders);

        List<String> locations = new ArrayList<>(locationTypes.keySet());
        Collections.sort(locations);
        fillCombo(locationSelect, locations);
        fillCombo(routeFromSelect, locations);
        fillCombo(routeToSelect, locations);
        fillCombo(dispatchSourceSelect, locations);
        fillCombo(dispatchTargetSelect, locations);
        fillCombo(userLocationSelect, locations);
        fillCombo(restaurantLocationSelect, locations);
    }

    private void fillCombo(JComboBox<String> combo, List<String> values) {
        String current = (String) combo.getSelectedItem();
        combo.removeAllItems();
        for (String value : values) {
            combo.addItem(value);
        }
        if (current != null && values.contains(current)) {
            combo.setSelectedItem(current);
        } else if (!values.isEmpty()) {
            combo.setSelectedIndex(0);
        }
    }

    private void addLocationInternal(String locationId, String type) {
        if (locationId == null || locationId.isBlank()) {
            return;
        }
        String key = locationId.trim();
        locationTypes.put(key, type == null || type.isBlank() ? inferType(key) : type);
        if (routeGraph.getNodeById(key) == null) {
            routeGraph.addNode(new Node(key));
        }
    }

    private void addRouteInternal(String fromId, String toId, double weight) {
        addLocationInternal(fromId, inferType(fromId));
        addLocationInternal(toId, inferType(toId));
        routeGraph.addEdge(new Node(fromId), new Node(toId), weight);
        routeGraph.addEdge(new Node(toId), new Node(fromId), weight);
    }

    private String inferType(String id) {
        String lower = id == null ? "" : id.toLowerCase();
        if (lower.contains("rest")) return "Restaurant";
        if (lower.contains("cust")) return "Customer";
        if (lower.contains("hub")) return "Hub";
        return "Other";
    }

    private order findOrder(int id) {
        for (order o : orders) {
            if (o.getOrderID() == id) return o;
        }
        return null;
    }

    private int parseOrderId(String label) {
        if (label == null || label.isEmpty()) return -1;
        try {
            String token = parseToken(label);
            token = token.replace("Order #", "").trim();
            return Integer.parseInt(token);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private String parseToken(String label) {
        if (label == null || label.isBlank()) {
            return "";
        }
        String[] parts = label.split("\\|", 2);
        return parts.length > 0 ? parts[0].trim() : label.trim();
    }

    private String stripLeadingId(String label) {
        if (label == null) return "";
        String[] parts = label.split("\\|", 2);
        return parts.length == 0 ? label.trim() : parts[0].trim().isEmpty() ? label.trim() : label.substring(label.indexOf('|') + 1).trim();
    }

    private String selectedValue(JComboBox<String> combo) {
        Object item = combo.getSelectedItem();
        return item == null ? "" : item.toString();
    }

    private double parseDouble(String text, double defaultValue) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private int parseInt(String text, int defaultValue) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private void updateActiveTracks() {
        long now = System.currentTimeMillis();
        List<DeliveryTrack> finished = new ArrayList<>();
        for (DeliveryTrack track : activeTracks) {
            if (track.isFinished(now)) {
                finished.add(track);
                deliveryService.completeDelivery(track.rider.getRiderId(), track.target);
                orders.stream()
                        .filter(o -> track.rider.getRiderId().equalsIgnoreCase(o.getAssignedRiderId()))
                        .forEach(o -> o.setStatus("DELIVERED"));
            }
        }
        if (!finished.isEmpty()) {
            activeTracks.removeAll(finished);
            refreshAllViews();
            setStatus("One or more deliveries completed.");
        }
    }

    private static DefaultTableModel tableModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private final class RouteMapPanel extends JPanel {
        private Graph graph;
        private Map<String, String> types = Collections.emptyMap();
        private List<String> highlightedPath = Collections.emptyList();
        private List<DeliveryTrack> tracks = Collections.emptyList();

        RouteMapPanel() {
            setPreferredSize(new Dimension(1200, 800));
            setBackground(Color.WHITE);
        }

        void setGraph(Graph graph, Map<String, String> types) {
            this.graph = graph;
            this.types = new LinkedHashMap<>(types);
        }

        void setHighlightedPath(List<String> path) {
            this.highlightedPath = path == null ? Collections.emptyList() : new ArrayList<>(path);
        }

        void setActiveTracks(List<DeliveryTrack> tracks) {
            this.tracks = tracks == null ? Collections.emptyList() : tracks;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (graph == null || graph.getNodes().isEmpty()) {
                g2.setColor(Color.DARK_GRAY);
                g2.drawString("No route data yet.", 24, 30);
                g2.dispose();
                return;
            }

            Map<String, Point2D.Double> positions = layoutNodes();
            drawEdges(g2, positions);
            drawPath(g2, positions);
            drawNodes(g2, positions);
            drawRiders(g2, positions);
            g2.dispose();
        }

        private Map<String, Point2D.Double> layoutNodes() {
            List<Node> nodes = new ArrayList<>(graph.getNodes());
            nodes.sort((a, b) -> a.getId().compareToIgnoreCase(b.getId()));
            Map<String, Point2D.Double> positions = new HashMap<>();
            double radius = Math.min(getWidth(), getHeight()) * 0.42;
            double centerX = getWidth() * 0.5;
            double centerY = getHeight() * 0.5;
            for (int i = 0; i < nodes.size(); i++) {
                double angle = (2 * Math.PI * i / Math.max(nodes.size(), 1)) - Math.PI / 2;
                double x = centerX + radius * Math.cos(angle);
                double y = centerY + radius * Math.sin(angle);
                positions.put(nodes.get(i).getId(), new Point2D.Double(x, y));
            }
            return positions;
        }

        private void drawEdges(Graphics2D g2, Map<String, Point2D.Double> positions) {
            g2.setColor(new Color(190, 196, 205));
            g2.setStroke(new BasicStroke(2f));
            for (Node from : graph.getNodes()) {
                Point2D.Double p1 = positions.get(from.getId());
                if (p1 == null) continue;
                for (Edge edge : graph.getEdges(from)) {
                    Point2D.Double p2 = positions.get(edge.getTarget().getId());
                    if (p2 == null) continue;
                    g2.drawLine((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
                    g2.setColor(new Color(120, 126, 135));
                    g2.drawString(String.valueOf(edge.getWeight()), (int) ((p1.x + p2.x) / 2) + 4, (int) ((p1.y + p2.y) / 2) - 4);
                    g2.setColor(new Color(190, 196, 205));
                }
            }
        }

        private void drawPath(Graphics2D g2, Map<String, Point2D.Double> positions) {
            if (highlightedPath.size() < 2) return;
            g2.setColor(new Color(70, 170, 95));
            g2.setStroke(new BasicStroke(4f));
            for (int i = 0; i < highlightedPath.size() - 1; i++) {
                Point2D.Double a = positions.get(highlightedPath.get(i));
                Point2D.Double b = positions.get(highlightedPath.get(i + 1));
                if (a != null && b != null) {
                    g2.drawLine((int) a.x, (int) a.y, (int) b.x, (int) b.y);
                }
            }
        }

        private void drawNodes(Graphics2D g2, Map<String, Point2D.Double> positions) {
            for (Node node : graph.getNodes()) {
                Point2D.Double p = positions.get(node.getId());
                if (p == null) continue;
                Color fill = colorFor(types.getOrDefault(node.getId(), inferType(node.getId())));
                int diameter = 44;
                int x = (int) p.x - diameter / 2;
                int y = (int) p.y - diameter / 2;
                g2.setColor(fill);
                g2.fillOval(x, y, diameter, diameter);
                g2.setColor(Color.DARK_GRAY);
                g2.drawOval(x, y, diameter, diameter);
                g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                int textWidth = g2.getFontMetrics().stringWidth(node.getId());
                g2.drawString(node.getId(), (int) p.x - textWidth / 2, y - 7);
            }
        }

        private void drawRiders(Graphics2D g2, Map<String, Point2D.Double> positions) {
            long now = System.currentTimeMillis();
            Set<String> busyRiderIds = new HashSet<>();
            for (DeliveryTrack track : tracks) {
                busyRiderIds.add(track.rider.getRiderId());
                Point2D.Double p = track.positionAt(now, positions);
                if (p == null) continue;
                g2.setColor(new Color(44, 180, 74));
                g2.fillOval((int) p.x - 10, (int) p.y - 10, 20, 20);
                g2.setColor(Color.BLACK);
                String label = track.rider.getName() + " (" + Math.max(0, Math.round(track.remainingMinutes(now))) + "m left)";
                int width = g2.getFontMetrics().stringWidth(label);
                g2.drawString(label, (int) p.x - width / 2, (int) p.y - 16);
            }

            // Draw available stationary riders at their current locations
            for (Rider rider : deliveryService.getAllRiders()) {
                if (busyRiderIds.contains(rider.getRiderId())) {
                    continue;
                }
                String loc = rider.getCurrentLocationId();
                Point2D.Double p = positions.get(loc);
                if (p == null) continue;

                g2.setColor(new Color(51, 102, 204));
                g2.fillOval((int) p.x - 7, (int) p.y - 7, 14, 14);

                g2.setColor(new Color(51, 102, 204));
                Font oldFont = g2.getFont();
                g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                String label = rider.getName() + " (Idle)";
                int width = g2.getFontMetrics().stringWidth(label);
                g2.drawString(label, (int) p.x - width / 2, (int) p.y + 20);
                g2.setFont(oldFont);
            }
        }

        private Color colorFor(String type) {
            return switch (type) {
                case "Restaurant" -> new Color(255, 210, 135);
                case "Customer" -> new Color(165, 206, 255);
                case "Hub" -> new Color(230, 230, 230);
                default -> new Color(210, 210, 210);
            };
        }

        private String inferType(String id) {
            if (id == null) return "Other";
            String lower = id.toLowerCase();
            if (lower.contains("rest")) return "Restaurant";
            if (lower.contains("cust")) return "Customer";
            if (lower.contains("hub")) return "Hub";
            return "Other";
        }
    }

    private final class DeliveryTrack {
        private final Rider rider;
        private final List<String> path;
        private final String source;
        private final String target;
        private final double totalMinutes;
        private final long startMillis;

        DeliveryTrack(Rider rider, List<String> path, String source, String target, double totalMinutes, long startMillis) {
            this.rider = rider;
            this.path = path == null ? Collections.emptyList() : new ArrayList<>(path);
            this.source = source;
            this.target = target;
            this.totalMinutes = Math.max(1.0, totalMinutes);
            this.startMillis = startMillis;
        }

        boolean isFinished(long now) {
            return now - startMillis >= totalMinutes * MINUTE_MS;
        }

        double remainingMinutes(long now) {
            double elapsed = (now - startMillis) / (double) MINUTE_MS;
            return Math.max(0.0, totalMinutes - elapsed);
        }

        Point2D.Double positionAt(long now, Map<String, Point2D.Double> positions) {
            if (path.isEmpty()) {
                return positions.get(source);
            }
            if (path.size() == 1) {
                rider.setCurrentLocationId(path.get(0));
                return positions.get(path.get(0));
            }
            double elapsed = (now - startMillis) / (double) MINUTE_MS;
            double progress = Math.min(1.0, elapsed / totalMinutes);
            double scaled = progress * (path.size() - 1);
            int index = Math.min((int) Math.floor(scaled), path.size() - 2);
            
            // Update rider's current location dynamically as they travel
            int currentIdx = Math.min((int) Math.floor(scaled), path.size() - 1);
            if (currentIdx >= 0 && currentIdx < path.size()) {
                rider.setCurrentLocationId(path.get(currentIdx));
            }

            double local = scaled - index;
            Point2D.Double a = positions.get(path.get(index));
            Point2D.Double b = positions.get(path.get(index + 1));
            if (a == null || b == null) return null;
            double x = a.x + (b.x - a.x) * local;
            double y = a.y + (b.y - a.y) * local;
            return new Point2D.Double(x, y);
        }
    }

    private String formatOrderForCombo(order o) {
        String fromWho = o.getCustomerName();
        String toWho = o.getTargetLocation() != null ? o.getTargetLocation() : "UNKNOWN";
        String restLoc = o.getSourceLocation() != null ? o.getSourceLocation() : "UNKNOWN";
        
        return String.format("Order #%d | %s | %s -> %s",
                o.getOrderID(), fromWho, restLoc, toWho);
    }

    private String getCustomerLocation(String name) {
        for (Customer c : userManager.getCustomers()) {
            if (c.getName().equalsIgnoreCase(name) || c.getId().equalsIgnoreCase(name)) {
                return c.getLocationId();
            }
        }
        return null;
    }

    private String getRestaurantLocation(String name) {
        for (Restaurant r : restaurantManager.getRestaurants()) {
            if (r.getName().equalsIgnoreCase(name) || r.getId().equalsIgnoreCase(name)) {
                return r.getLocationId();
            }
        }
        return null;
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }
}
