package gui;

import Route_Optimization_System.DijkstraAlgorithm;
import Route_Optimization_System.Edge;
import Route_Optimization_System.Graph;
import Route_Optimization_System.Node;
import delivery_assignment_system.DeliveryService;
import delivery_assignment_system.Rider;
import java.awt.Component;
import java.awt.Toolkit;
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
import javax.swing.JComponent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import order_processing.order;
import order_processing.orderSystem;
import user_management.Customer;
import user_management.Restaurant;
import user_management.RestaurantManager;
import user_management.UserManager;
import search_and_data_retrieval_system.*;

public class OnePageDashboard extends JFrame {

    private static final long MINUTE_MS = 5000L; // demo scale: 1 minute = 5 seconds

    private final UserManager userManager = new UserManager();
    private final RestaurantManager restaurantManager = new RestaurantManager();
    private final DeliveryService deliveryService = new DeliveryService();
    private final Graph routeGraph = new Graph();
    private final Map<String, String> locationTypes = new LinkedHashMap<>();
    
    private final orderSystem orderSys = new orderSystem();
    private List<order> getOrdersList() {
        return orderSys.getOrderList().getOrdersList();
    }
    
    private final List<DeliveryTrack> activeTracks = new ArrayList<>();

    // Search & Data Retrieval System
    private final FoodBST foodBST = new FoodBST();
    private final FoodAVLTree foodAVLTree = new FoodAVLTree();
    private final DataStorage dataStorage = new DataStorage();

    private final DefaultTableModel usersModel = tableModel("ID", "Name", "Phone", "Location");
    private final DefaultTableModel restaurantsModel = tableModel("ID", "Name", "Cuisine", "Location");
    private final DefaultTableModel ridersModel = tableModel("ID", "Name", "To Rest (min)", "At", "State");
    private final List<JTable> dataTables = new ArrayList<>();
    private final DefaultTableModel ordersModel = tableModel("Order ID", "Customer", "Restaurant", "Content", "Status", "Rider", "Route");
    private final DefaultTableModel locationsModel = tableModel("Location ID", "Type");
    private final DefaultTableModel routesModel = tableModel("From", "To", "Weight");
    private final DefaultTableModel menuModel = tableModel("Name", "Category", "Price", "Restaurant");
    private final DefaultTableModel searchLogsModel = tableModel("Operation", "Target", "Structure", "Time (ns)", "Result");

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
    private final JComboBox<String> pendingOrderSelect = new JComboBox<>();

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
    private final JComboBox<String> riderLocationSelect = new JComboBox<>();

    private final JComboBox<String> foodItemSelect = new JComboBox<>();
    private final javax.swing.JTextArea cartArea = new javax.swing.JTextArea(3, 20);
    private final JTextField routeWeightField = new JTextField(10);
    private final JTextField locationIdField = new JTextField(10);
    private final JComboBox<String> locationTypeSelect = new JComboBox<>(new String[]{"Customer", "Restaurant", "Hub", "Other"});

    private final JTextField foodSearchField = new JTextField(10);
    private final JTextField foodNameField = new JTextField(10);
    private final JTextField foodCategoryField = new JTextField(10);
    private final JTextField foodPriceField = new JTextField(10);
    private final JComboBox<String> foodRestaurantSelect = new JComboBox<>();

    private final JTextField lookupIdField = new JTextField(10);
    private final JComboBox<String> lookupTypeSelect = new JComboBox<>(new String[]{"Customer ID", "Order ID"});
    private final javax.swing.JTextArea restaurantMenuHintArea = new javax.swing.JTextArea(5, 20);

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
        deliveryService.setRouteGraph(routeGraph);
        buildUi();
        configureInputControls();
        refreshAllViews();

        animationTimer = new Timer(100, e -> {
            updateActiveTracks();
            refreshRiders();
            mapPanel.repaint();
        });
        animationTimer.start();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         // Dynamically set the size to fit the screen
         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         setSize(screenSize.width, screenSize.height);
 
         // Alternatively, maximize the frame
         // setExtendedState(JFrame.MAXIMIZED_BOTH);
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

        JTabbedPane actionTabs = new JTabbedPane();
        actionTabs.setPreferredSize(new Dimension(380, 500));
        actionTabs.addTab("Dispatch", scrollablePanel(buildDispatchTab()));
        actionTabs.addTab("Customer", scrollablePanel(buildUserTab()));
        actionTabs.addTab("Restaurant", scrollablePanel(buildRestaurantTab()));
        actionTabs.addTab("Rider", scrollablePanel(buildRiderTab()));
        actionTabs.addTab("Order", scrollablePanel(buildOrderTab()));
        actionTabs.addTab("Routes & Loc", scrollablePanel(buildRouteTab()));
        actionTabs.addTab("Menu & Search", scrollablePanel(buildMenuSearchTab()));

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
                if (restLoc != null && !restLoc.equals("UNKNOWN")) {
                    deliveryService.updateRiderPrioritiesForRestaurant(restLoc);
                    refreshRiders();
                }
            }
        });

        restaurantSelect.addActionListener(e -> updateRestaurantMenuHint());

        JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));
        centerPanel.add(mapPanel, BorderLayout.CENTER);
        centerPanel.add(legendPanel(), BorderLayout.SOUTH);

        JPanel tablesGrid = new JPanel(new GridLayout(2, 4, 8, 8));
        tablesGrid.setBorder(BorderFactory.createEmptyBorder(4, 10, 10, 10));
        tablesGrid.add(tableCard("Customers", usersModel));
        tablesGrid.add(tableCard("Restaurants", restaurantsModel));
        tablesGrid.add(tableCard("Riders", ridersModel));
        tablesGrid.add(tableCard("Orders", ordersModel));
        tablesGrid.add(tableCard("Locations", locationsModel));
        tablesGrid.add(tableCard("Routes", routesModel));
        tablesGrid.add(tableCard("Food Menu (A-Z)", menuModel));
        tablesGrid.add(tableCard("Search & Retrieval Logs", searchLogsModel));

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(tablesGrid, BorderLayout.CENTER);
        southPanel.setPreferredSize(new Dimension(0, 260));
        southPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        JPanel middle = new JPanel(new BorderLayout(10, 10));
        middle.add(centerPanel, BorderLayout.CENTER);
        middle.add(actionTabs, BorderLayout.EAST);

        add(middle, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);

        mapPanel.setBorder(BorderFactory.createTitledBorder("Live Route Map"));
    }

    private JPanel buildDispatchTab() {
        return section("Dispatch",
            formRow("Order", dispatchOrderSelect),
            formRow("Restaurant", dispatchSourceSelect),
            formRow("Customer", dispatchTargetSelect),
            formRow("Mins (optional)", dispatchMinutesField),
            buttonRow(button("Dispatch Order", e -> dispatchSelectedOrder())),
            Box.createVerticalStrut(10),
            formRow("Active Rider", riderSelect),
            buttonRow(button("Complete Delivery", e -> completeSelectedDelivery()))
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
            formRow("Current Location", riderLocationSelect),
            Box.createVerticalStrut(12),
            buttonRow(button("Add Rider", e -> addRiderFromFields())),
            Box.createVerticalStrut(30)
        );
    }

    private JPanel buildOrderTab() {
        restaurantMenuHintArea.setEditable(false);
        restaurantMenuHintArea.setLineWrap(true);
        restaurantMenuHintArea.setWrapStyleWord(true);
        restaurantMenuHintArea.setBackground(new Color(245, 246, 249));
        restaurantMenuHintArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JScrollPane menuScroll = new JScrollPane(restaurantMenuHintArea);
        menuScroll.setPreferredSize(new Dimension(300, 100));
        menuScroll.setBorder(BorderFactory.createTitledBorder("Restaurant Menu"));

        cartArea.setEditable(false);
        cartArea.setLineWrap(true);
        cartArea.setWrapStyleWord(true);
        cartArea.setBackground(new Color(245, 246, 249));
        cartArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        cartArea.setText("Cart: (empty)");
        JScrollPane cartScroll = new JScrollPane(cartArea);
        cartScroll.setPreferredSize(new Dimension(300, 60));
        cartScroll.setBorder(BorderFactory.createTitledBorder("Current Cart"));
 
        return section("Order Management",
            section("Place New Order",
                formRow("Customer", userSelect),
                formRow("Restaurant", restaurantSelect),
                formRow("Select Food Item", foodItemSelect),
                Box.createVerticalStrut(6),
                buttonRow(
                    button("Add Food Item", e -> addFoodItemToCart()),
                    button("Undo Last Food", e -> undoLastFoodItem())
                ),
                Box.createVerticalStrut(6),
                cartScroll,
                Box.createVerticalStrut(6),
                menuScroll,
                Box.createVerticalStrut(12),
                buttonRow(
                    button("Place Order", e -> placeOrderFromCart()),
                    button("Undo Last Order", e -> undoLastOrder())
                )
            ),
            Box.createVerticalStrut(10),
            section("Confirm Pending Order",
                formRow("Pending Order", pendingOrderSelect),
                Box.createVerticalStrut(6),
                buttonRow(button("Confirm Order", e -> confirmSelectedOrder()))
            )
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
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(SIDEBAR_CONTENT_WIDTH, 44));
        for (java.awt.Component c : components) p.add(c);
        return p;
    }

    private static final int SIDEBAR_CONTENT_WIDTH = 320;

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
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)));
        for (java.awt.Component component : components) {
            alignLeft(component);
            panel.add(component);
            panel.add(Box.createVerticalStrut(6));
        }
        return panel;
    }

    private JPanel formRow(String label, java.awt.Component component) {
        JPanel row = new JPanel(new BorderLayout(0, 4));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(SIDEBAR_CONTENT_WIDTH, 80));
        JLabel text = new JLabel(label);
        row.add(text, BorderLayout.NORTH);
        JPanel fieldPanel = new JPanel(new BorderLayout());
        fieldPanel.add(component, BorderLayout.CENTER);
        if (component instanceof JComboBox<?> combo) {
            combo.setMaximumRowCount(12);
            combo.setMaximumSize(new Dimension(SIDEBAR_CONTENT_WIDTH, combo.getPreferredSize().height));
        } else if (component instanceof JTextField field) {
            field.setMaximumSize(new Dimension(SIDEBAR_CONTENT_WIDTH, field.getPreferredSize().height));
        }
        row.add(fieldPanel, BorderLayout.CENTER);
        return row;
    }

    private void configureInputControls() {
        setComboPrototype(dispatchOrderSelect, "Order #99 | Name | A -> G");
        setComboPrototype(pendingOrderSelect, "Order #99 | Name | A -> G");
        setComboPrototype(userSelect, "C999 | Customer Name");
        setComboPrototype(restaurantSelect, "R999 | Restaurant Name");
        setComboPrototype(riderSelect, "R99 | Rider Name");
        setComboPrototype(dispatchSourceSelect, "A_Restaurant");
        setComboPrototype(dispatchTargetSelect, "G_Customer");
        setComboPrototype(userLocationSelect, "A_Restaurant");
        setComboPrototype(restaurantLocationSelect, "A_Restaurant");
        setComboPrototype(riderLocationSelect, "A_Restaurant");
        setComboPrototype(routeFromSelect, "A_Restaurant");
        setComboPrototype(routeToSelect, "G_Customer");
        setComboPrototype(foodRestaurantSelect, "R999 | Restaurant Name");
        setComboPrototype(foodItemSelect, "Char Kuey Teow");
        configureTruncatedCombo(dispatchOrderSelect);
        configureTruncatedCombo(pendingOrderSelect);
        configureTruncatedCombo(dispatchSourceSelect);
        configureTruncatedCombo(dispatchTargetSelect);
        configureTruncatedCombo(foodRestaurantSelect);
        configureTruncatedCombo(foodItemSelect);
        dispatchMinutesField.setToolTipText("Leave blank to auto-calculate from route optimization.");
    }

    private void setComboPrototype(JComboBox<String> combo, String sample) {
        combo.setPrototypeDisplayValue(sample);
    }

    private void configureTruncatedCombo(JComboBox<String> combo) {
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value != null) {
                    String text = value.toString();
                    label.setToolTipText(text);
                    if (index < 0 && text.length() > 30) {
                        label.setText(text.substring(0, 27) + "...");
                    }
                }
                return label;
            }
        });
    }

    private JScrollPane tableCard(String title, DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(24);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getTableHeader().setReorderingAllowed(false);
        dataTables.add(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setPreferredSize(new Dimension(220, 110));
        scroll.setBorder(BorderFactory.createTitledBorder(title));
        return scroll;
    }

    private void resizeAllTables() {
        for (JTable table : dataTables) {
            resizeTableColumns(table);
        }
    }

    private void resizeTableColumns(JTable table) {
        final int margin = 12;
        final int maxColWidth = 160;
        for (int col = 0; col < table.getColumnCount(); col++) {
            TableColumn column = table.getColumnModel().getColumn(col);
            int maxWidth = 0;

            Component header = table.getTableHeader().getDefaultRenderer()
                    .getTableCellRendererComponent(table, column.getHeaderValue(), false, false, 0, col);
            maxWidth = Math.max(maxWidth, header.getPreferredSize().width);

            int rows = table.getRowCount();
            for (int row = 0; row < rows; row++) {
                Object value = table.getValueAt(row, col);
                if (value == null) continue;
                Component cell = table.getCellRenderer(row, col)
                        .getTableCellRendererComponent(table, value, false, false, row, col);
                maxWidth = Math.max(maxWidth, cell.getPreferredSize().width);
            }

            int width = Math.min(maxWidth + margin, maxColWidth);
            column.setPreferredWidth(width);
        }
    }

    private JButton button(String text, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.addActionListener(listener);
        return button;
    }

    private JScrollPane scrollablePanel(JPanel panel) {
        constrainPanelWidth(panel, SIDEBAR_CONTENT_WIDTH);
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private void alignLeft(Component component) {
        if (component instanceof JComponent jc) {
            jc.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
    }

    private void constrainPanelWidth(JPanel panel, int width) {
        Dimension max = new Dimension(width, Integer.MAX_VALUE);
        panel.setMaximumSize(max);
        alignLeft(panel);
        for (Component child : panel.getComponents()) {
            alignLeft(child);
            if (child instanceof JPanel childPanel) {
                childPanel.setMaximumSize(max);
            }
        }
    }

    private void seedDemoData() {
        Customer c1 = new Customer("C001", "Ali Ahmad", "0123456789", "E_Customer");
        Customer c2 = new Customer("C002", "Sarah Kaur", "0187654321", "F_Customer");
        Customer c3 = new Customer("C003", "Bobby Lim", "0112233445", "G_Customer");
        
        userManager.addCustomer(c1);
        userManager.addCustomer(c2);
        userManager.addCustomer(c3);
        
        dataStorage.saveCustomer(c1);
        dataStorage.saveCustomer(c2);
        dataStorage.saveCustomer(c3);

        restaurantManager.addRestaurant(new Restaurant("R001", "A_Restaurant", "Rice", "A_Restaurant"));
        restaurantManager.addRestaurant(new Restaurant("R002", "B_Restaurant", "Noodle", "B_Restaurant"));
        restaurantManager.addRestaurant(new Restaurant("R003", "C_Restaurant", "Local", "C_Restaurant"));

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

        order first = new order("E_Customer", "A_Restaurant", "Nasi Lemak x2");
        first.setSourceLocation("A_Restaurant");
        first.setTargetLocation("E_Customer");
        first.setStatus("CONFIRMED");
        orderSys.getOrderList().addOrder(first);
        dataStorage.saveOrder(first);

        order second = new order("F_Customer", "B_Restaurant", "Char Kuey Teow x1");
        second.setSourceLocation("B_Restaurant");
        second.setTargetLocation("F_Customer");
        second.setStatus("PENDING");
        orderSys.getOrderList().addOrder(second);
        dataStorage.saveOrder(second);

        order third = new order("G_Customer", "C_Restaurant", "Roti Canai x3, Teh Tarik x2");
        third.setSourceLocation("C_Restaurant");
        third.setTargetLocation("G_Customer");
        third.setStatus("CONFIRMED");
        orderSys.getOrderList().addOrder(third);
        dataStorage.saveOrder(third);
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
        Customer customer = new Customer(id, name, phone, location.isEmpty() ? "UNKNOWN" : location);
        userManager.addCustomer(customer);
        dataStorage.saveCustomer(customer);
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
        String location = selectedValue(riderLocationSelect);
        if (location.isEmpty()) {
            setStatus("Please select the rider's current location.");
            return;
        }
        deliveryService.addRider(new Rider(id, name, 0, 0, location));
        riderIdField.setText("");
        riderNameField.setText("");
        refreshAllViews();
        setStatus("Rider added.");
    }

    private void addFoodItemToCart() {
        String customer = selectedValue(userSelect);
        String restaurant = selectedValue(restaurantSelect);
        String foodName = selectedValue(foodItemSelect);
        if (customer.isEmpty() || restaurant.isEmpty() || foodName.isEmpty()) {
            setStatus("Please select Customer, Restaurant, and Food Item.");
            return;
        }
        String custName = stripLeadingId(customer);
        String restName = stripLeadingId(restaurant);
        orderSys.startOrder(custName, restName);
        
        FoodItem item = foodAVLTree.search(foodName);
        if (item == null) {
            setStatus("Food item not found: " + foodName);
            return;
        }
        orderSys.addItem(item);
        updateCartDisplay();
        setStatus("Added \"" + item.getName() + "\" to cart.");
    }

    private void undoLastFoodItem() {
        FoodItem item = orderSys.undoLastItem();
        if (item != null) {
            setStatus("Undone last item: \"" + item.getName() + "\".");
        } else {
            setStatus("Cart is already empty.");
        }
        updateCartDisplay();
    }

    private void placeOrderFromCart() {
        if (orderSys.isCartEmpty()) {
            setStatus("Cannot place order: Cart is empty.");
            return;
        }
        String customer = selectedValue(userSelect);
        String restaurant = selectedValue(restaurantSelect);
        String custName = stripLeadingId(customer);
        String restName = stripLeadingId(restaurant);
        
        orderSys.startOrder(custName, restName);
        order newOrder = orderSys.confirmOrder();
        if (newOrder != null) {
            String restLoc = getRestaurantLocation(restName);
            String custLoc = getCustomerLocation(custName);
            newOrder.setSourceLocation(restLoc != null ? restLoc : "UNKNOWN");
            newOrder.setTargetLocation(custLoc != null ? custLoc : "UNKNOWN");
            dataStorage.saveOrder(newOrder);
            updateCartDisplay();
            refreshAllViews();
            setStatus("Order placed: Order #" + newOrder.getOrderID() + ". Status: PENDING.");
        } else {
            setStatus("Failed to build order.");
        }
    }

    private void undoLastOrder() {
        order undone = orderSys.undoLastOrder();
        if (undone != null) {
            dataStorage.deleteOrder("ORD-" + undone.getOrderID());
            refreshAllViews();
            setStatus("Undone last placed Order #" + undone.getOrderID() + ".");
        } else {
            setStatus("No orders to undo.");
        }
    }

    private void updateCartDisplay() {
        if (orderSys.isCartEmpty()) {
            cartArea.setText("Cart: (empty)");
        } else {
            String content = orderSys.getCartContentString();
            double total = orderSys.getCartTotalPrice();
            cartArea.setText("Content: " + content + "\nTotal: RM" + String.format("%.2f", total));
        }
    }

    private void confirmSelectedOrder() {
        String selected = selectedValue(pendingOrderSelect);
        if (selected.isEmpty()) return;
        int orderId = parseOrderId(selected);
        order o = findOrder(orderId);
        if (o != null) {
            o.setStatus("CONFIRMED");
            refreshAllViews();
            setStatus("Order #" + orderId + " has been CONFIRMED.");
        }
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

        Rider rider = deliveryService.assignBestRiderForRestaurant(source);
        if (rider == null) {
            setStatus("No riders available or no route to restaurant.");
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
        // orders.remove(targetOrder); // Retain order to display status transitions
        
        mapPanel.setHighlightedPath(path);
        refreshAllViews();
        setStatus("Dispatched " + rider.getName() + " (nearest to " + source + ", " + (int) pickupTime + " min pickup) for order #" + targetOrder.getOrderID() + ".");
        riderHintLabel.setText(rider.getName() + " is travelling from " + riderStart + " -> " + source + " -> " + target + " (~" + travelMinutes + " min total).");
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
            getOrdersList().stream()
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
        refreshMenuTable();
        refreshSelections();
        mapPanel.setGraph(routeGraph, locationTypes);
        mapPanel.setActiveTracks(activeTracks);
        mapPanel.repaint();
        resizeAllTables();
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
        String restaurantId = selectedDispatchRestaurant();
        if (restaurantId != null) {
            deliveryService.updateRiderPrioritiesForRestaurant(restaurantId);
        }
        for (Rider rider : deliveryService.getAllRiders()) {
            String travelDisplay = busy.contains(rider.getRiderId())
                    ? "-"
                    : (rider.getDeliveryTimeMins() > 0
                            ? rider.getDeliveryTimeMins() + " min"
                            : "-");
            ridersModel.addRow(new Object[]{
                rider.getRiderId(),
                rider.getName(),
                travelDisplay,
                rider.getCurrentLocationId(),
                busy.contains(rider.getRiderId()) ? "Busy" : "Available"
            });
        }
    }

    private void refreshOrders() {
        ordersModel.setRowCount(0);
        for (order o : getOrdersList()) {
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
        fillCombo(orderSelect, getOrdersList().stream().map(o -> o.getOrderID() + " | " + o.getCustomerName() + " | " + o.getRestaurantName()).toList());
        
        List<String> pendingOnly = getOrdersList().stream()
                .filter(o -> "PENDING".equals(o.getStatus()))
                .map(this::formatOrderForCombo)
                .toList();
        fillCombo(pendingOrderSelect, pendingOnly);

        List<String> confirmedOnly = getOrdersList().stream()
                .filter(o -> "CONFIRMED".equals(o.getStatus()))
                .map(this::formatOrderForCombo)
                .toList();
        fillCombo(dispatchOrderSelect, confirmedOnly);

        List<String> locations = new ArrayList<>(locationTypes.keySet());
        Collections.sort(locations);
        fillCombo(locationSelect, locations);
        fillCombo(routeFromSelect, locations);
        fillCombo(routeToSelect, locations);
        fillCombo(dispatchSourceSelect, locations);
        fillCombo(dispatchTargetSelect, locations);
        fillCombo(userLocationSelect, locations);
        fillCombo(restaurantLocationSelect, locations);
        fillCombo(riderLocationSelect, locations);

        fillCombo(foodRestaurantSelect, restaurantManager.getRestaurants().stream().map(r -> r.getId() + " | " + r.getName()).toList());
    }

    private String selectedDispatchRestaurant() {
        String selected = selectedValue(dispatchOrderSelect);
        if (selected.isEmpty()) return null;
        order o = findOrder(parseOrderId(selected));
        if (o == null) return null;
        String source = o.getSourceLocation();
        if (source == null || source.equals("UNKNOWN")) return null;
        return source;
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
        for (order o : getOrdersList()) {
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
                getOrdersList().stream()
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
            setPreferredSize(new Dimension(1200, 700));
            setMinimumSize(new Dimension(300, 240));
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

            double centerX = getWidth() * 0.5;
            double centerY = getHeight() * 0.48;
            Map<String, Point2D.Double> positions = layoutNodes(centerX, centerY);
            drawEdges(g2, positions, centerX, centerY);
            drawPath(g2, positions);
            drawNodes(g2, positions, centerX, centerY);
            drawRiders(g2, positions, centerX, centerY);
            g2.dispose();
        }

        private Map<String, Point2D.Double> layoutNodes(double centerX, double centerY) {
            List<Node> nodes = new ArrayList<>(graph.getNodes());
            nodes.sort((a, b) -> a.getId().compareToIgnoreCase(b.getId()));
            Map<String, Point2D.Double> positions = new HashMap<>();
            double labelPadding = 80;
            double usableWidth = Math.max(160, getWidth() - labelPadding * 2);
            double usableHeight = Math.max(160, getHeight() - labelPadding * 2);
            double spread = Math.min(0.80, 0.40 + nodes.size() * 0.02);
            double radius = Math.min(usableWidth, usableHeight) * spread;
            for (int i = 0; i < nodes.size(); i++) {
                double angle = (2 * Math.PI * i / Math.max(nodes.size(), 1)) - Math.PI / 2;
                double x = centerX + radius * Math.cos(angle);
                double y = centerY + radius * Math.sin(angle);
                positions.put(nodes.get(i).getId(), new Point2D.Double(x, y));
            }
            return positions;
        }

        private void drawEdges(Graphics2D g2, Map<String, Point2D.Double> positions, double centerX, double centerY) {
            g2.setColor(new Color(190, 196, 205));
            g2.setStroke(new BasicStroke(2f));
            Font weightFont = new Font("SansSerif", Font.PLAIN, 10);
            Font oldFont = g2.getFont();
            g2.setFont(weightFont);
            for (Node from : graph.getNodes()) {
                Point2D.Double p1 = positions.get(from.getId());
                if (p1 == null) continue;
                for (Edge edge : graph.getEdges(from)) {
                    Point2D.Double p2 = positions.get(edge.getTarget().getId());
                    if (p2 == null) continue;
                    if (from.getId().compareTo(edge.getTarget().getId()) > 0) {
                        continue;
                    }
                    g2.setColor(new Color(190, 196, 205));
                    g2.drawLine((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
                    String weight = String.valueOf(edge.getWeight());
                    double midX = (p1.x + p2.x) / 2;
                    double midY = (p1.y + p2.y) / 2;
                    double dx = p2.x - p1.x;
                    double dy = p2.y - p1.y;
                    double len = Math.hypot(dx, dy);
                    double perpX = len == 0 ? 0 : -dy / len * 10;
                    double perpY = len == 0 ? 0 : dx / len * 10;
                    int textX = (int) (midX + perpX);
                    int textY = (int) (midY + perpY);
                    int textWidth = g2.getFontMetrics().stringWidth(weight);
                    g2.setColor(new Color(255, 255, 255, 220));
                    g2.fillRoundRect(textX - 2, textY - g2.getFontMetrics().getAscent(), textWidth + 4, g2.getFontMetrics().getHeight() - 2, 4, 4);
                    g2.setColor(new Color(90, 96, 105));
                    g2.drawString(weight, textX, textY);
                }
            }
            g2.setFont(oldFont);
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

        private void drawNodes(Graphics2D g2, Map<String, Point2D.Double> positions, double centerX, double centerY) {
            Font labelFont = new Font("SansSerif", Font.BOLD, 12);
            g2.setFont(labelFont);
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
                drawNodeLabel(g2, node.getId(), p, centerX, centerY, diameter);
            }
        }

        private String formatMapLabel(String id) {
            if (id == null || id.isBlank()) return "";
            int split = id.indexOf('_');
            if (split > 0) {
                String prefix = id.substring(0, split);
                String suffix = id.substring(split + 1);
                if (suffix.length() > 4) {
                    suffix = suffix.substring(0, 4);
                }
                return prefix + " " + suffix;
            }
            return id.length() > 8 ? id.substring(0, 8) : id;
        }

        private void drawNodeLabel(Graphics2D g2, String fullId, Point2D.Double p,
                double centerX, double centerY, int diameter) {
            String label = formatMapLabel(fullId);
            int textWidth = g2.getFontMetrics().stringWidth(label);
            int textHeight = g2.getFontMetrics().getHeight();
            double angle = Math.atan2(p.y - centerY, p.x - centerX);
            double offset = diameter / 2.0 + 20;
            int textX = (int) (p.x + Math.cos(angle) * offset) - textWidth / 2;
            int textY = (int) (p.y + Math.sin(angle) * offset) + textHeight / 3;
            textX = Math.max(4, Math.min(textX, getWidth() - textWidth - 4));
            textY = Math.max(textHeight, Math.min(textY, getHeight() - 4));
            g2.setColor(new Color(255, 255, 255, 210));
            g2.fillRoundRect(textX - 3, textY - textHeight + 4, textWidth + 6, textHeight, 6, 6);
            g2.setColor(new Color(45, 52, 60));
            g2.drawString(label, textX, textY);
        }

        private void drawRiders(Graphics2D g2, Map<String, Point2D.Double> positions, double centerX, double centerY) {
            long now = System.currentTimeMillis();
            Set<String> busyRiderIds = new HashSet<>();
            for (DeliveryTrack track : tracks) {
                busyRiderIds.add(track.rider.getRiderId());
                Point2D.Double p = track.positionAt(now, positions);
                if (p == null) continue;
                g2.setColor(new Color(44, 180, 74));
                g2.fillOval((int) p.x - 8, (int) p.y - 8, 16, 16);
                g2.setColor(Color.BLACK);
                String label = track.rider.getName() + " (" + Math.max(0, Math.round(track.remainingMinutes(now))) + "m)";
                drawRadialLabel(g2, label, p, centerX, centerY, 22);
            }

            for (Rider rider : deliveryService.getAllRiders()) {
                if (busyRiderIds.contains(rider.getRiderId())) {
                    continue;
                }
                String loc = rider.getCurrentLocationId();
                Point2D.Double p = positions.get(loc);
                if (p == null) continue;

                double angle = Math.atan2(p.y - centerY, p.x - centerX);
                int dotX = (int) (p.x + Math.cos(angle) * 1);
                int dotY = (int) (p.y + Math.sin(angle) * 1);
                g2.setColor(new Color(51, 102, 204));
                g2.fillOval(dotX - 6, dotY - 6, 12, 12);

                Font oldFont = g2.getFont();
                g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                g2.setColor(new Color(51, 102, 204));
                drawRadialLabel(g2, rider.getName(), p, centerX, centerY, 15);
                g2.setFont(oldFont);
            }
        }

        private void drawRadialLabel(Graphics2D g2, String label, Point2D.Double p,
                double centerX, double centerY, double offset) {
            int textWidth = g2.getFontMetrics().stringWidth(label);
            int textHeight = g2.getFontMetrics().getHeight();
            double angle = Math.atan2(p.y - centerY, p.x - centerX);
            int textX = (int) (p.x + Math.cos(angle) * offset) - textWidth / 2;
            int textY = (int) (p.y + Math.sin(angle) * offset) + textHeight / 3;
            textX = Math.max(4, Math.min(textX, getWidth() - textWidth - 4));
            textY = Math.max(textHeight, Math.min(textY, getHeight() - 4));
            g2.drawString(label, textX, textY);
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

    private JPanel buildMenuSearchTab() {
        return section("Menu & Search Engine",
            section("Search Food by Name (BST vs AVL)",
                formRow("Food Name", foodSearchField),
                buttonRow(
                    button("Search AVL & BST", e -> searchFoodByName()),
                    button("Clear Logs", e -> searchLogsModel.setRowCount(0))
                )
            ),
            section("Profile & Order Retrieval (O(1) HashMap)",
                formRow("ID / Key (e.g. C001, 1)", lookupIdField),
                formRow("Type", lookupTypeSelect),
                buttonRow(
                    button("HashMap Lookup", e -> lookupDataHashMap())
                ),
                Box.createVerticalStrut(6),
                buttonRow(
                    button("Run 10k HashMap Speed Demo", e -> runSpeedDemo())
                )
            ),
            section("Add New Food Item to Menu",
                formRow("Food Name", foodNameField),
                formRow("Category", foodCategoryField),
                formRow("Price (RM)", foodPriceField),
                formRow("Restaurant", foodRestaurantSelect),
                buttonRow(button("Add Food Item", e -> addFoodFromFields()))
            )
        );
    }

    private void searchFoodByName() {
        String name = foodSearchField.getText().trim();
        if (name.isEmpty()) {
            setStatus("Please enter a food name to search.");
            return;
        }

        // Measure BST Search
        long t1 = System.nanoTime();
        FoodItem bstResult = foodBST.search(name);
        long t2 = System.nanoTime();
        long bstTime = t2 - t1;

        // Measure AVL Search
        long t3 = System.nanoTime();
        FoodItem avlResult = foodAVLTree.search(name);
        long t4 = System.nanoTime();
        long avlTime = t4 - t3;

        boolean found = (avlResult != null);
        String resultStr = found ? "Found: RM" + String.format("%.2f", avlResult.getPrice()) + " at " + avlResult.getRestaurantName() : "Not Found";

        // Log results to table
        searchLogsModel.addRow(new Object[]{"BST Search", name, "Binary Search Tree", bstTime + " ns", found ? "Found" : "Not Found"});
        searchLogsModel.addRow(new Object[]{"AVL Search", name, "AVL Tree (Balanced)", avlTime + " ns", found ? "Found" : "Not Found"});
        
        if (found) {
            setStatus("Found \"" + name + "\". BST: " + bstTime + " ns | AVL: " + avlTime + " ns.");
        } else {
            setStatus("Food \"" + name + "\" not found. BST: " + bstTime + " ns | AVL: " + avlTime + " ns.");
        }
        foodSearchField.setText("");
        resizeAllTables();
    }

    private void lookupDataHashMap() {
        String key = lookupIdField.getText().trim();
        String type = selectedValue(lookupTypeSelect);
        if (key.isEmpty()) {
            setStatus("Please enter an ID / Key to lookup.");
            return;
        }

        if ("Customer ID".equals(type)) {
            // O(1) HashMap lookup
            long t1 = System.nanoTime();
            Customer customer = dataStorage.getCustomer(key);
            long t2 = System.nanoTime();
            long hashTime = t2 - t1;

            // O(n) Linear lookup from userManager
            long t3 = System.nanoTime();
            Customer linearResult = null;
            for (Customer c : userManager.getCustomers()) {
                if (c.getId().equalsIgnoreCase(key)) {
                    linearResult = c;
                    break;
                }
            }
            long t4 = System.nanoTime();
            long linearTime = t4 - t3;

            boolean found = (customer != null);
            String details = found ? customer.getName() + " (" + customer.getPhone() + ")" : "Not Found";
            searchLogsModel.addRow(new Object[]{"Cust Lookup (O(1))", key, "HashMap", hashTime + " ns", found ? "Found" : "Not Found"});
            searchLogsModel.addRow(new Object[]{"Cust Lookup (O(n))", key, "ArrayList (Linear)", linearTime + " ns", found ? "Found" : "Not Found"});
            
            if (found) {
                setStatus("Customer found: " + details + ". HashMap: " + hashTime + " ns | Linear: " + linearTime + " ns.");
            } else {
                setStatus("Customer \"" + key + "\" not found. HashMap: " + hashTime + " ns | Linear: " + linearTime + " ns.");
            }
        } else {
            // Order ID lookup
            String orderKey = key;
            if (!key.startsWith("ORD-")) {
                orderKey = "ORD-" + key;
            }

            // O(1) HashMap lookup
            long t1 = System.nanoTime();
            order o = dataStorage.getOrder(orderKey);
            long t2 = System.nanoTime();
            long hashTime = t2 - t1;

            // O(n) Linear lookup from orders
            long t3 = System.nanoTime();
            order linearResult = null;
            int targetId = -1;
            try {
                targetId = Integer.parseInt(orderKey.replace("ORD-", ""));
            } catch (Exception e) {}
            for (order ord : getOrdersList()) {
                if (ord.getOrderID() == targetId) {
                    linearResult = ord;
                    break;
                }
            }
            long t4 = System.nanoTime();
            long linearTime = t4 - t3;

            boolean found = (o != null);
            String details = found ? o.getCustomerName() + " -> " + o.getOrderContent() + " (" + o.getStatus() + ")" : "Not Found";
            searchLogsModel.addRow(new Object[]{"Order Lookup (O(1))", orderKey, "HashMap", hashTime + " ns", found ? "Found" : "Not Found"});
            searchLogsModel.addRow(new Object[]{"Order Lookup (O(n))", orderKey, "ArrayList (Linear)", linearTime + " ns", found ? "Found" : "Not Found"});

            if (found) {
                setStatus("Order found: " + details + ". HashMap: " + hashTime + " ns | Linear: " + linearTime + " ns.");
            } else {
                setStatus("Order \"" + orderKey + "\" not found. HashMap: " + hashTime + " ns | Linear: " + linearTime + " ns.");
            }
        }
        lookupIdField.setText("");
        resizeAllTables();
    }

    private void runSpeedDemo() {
        setStatus("Running HashMap O(1) vs Linear O(n) search speed demo...");
        SwingUtilities.invokeLater(() -> {
            HashMap<String, Customer> bigMap = new HashMap<>();
            List<Customer> bigList = new ArrayList<>();
            for (int i = 0; i < 10_000; i++) {
                String id = "C" + String.format("%05d", i);
                Customer c = new Customer(id, "User " + i, "010000" + i);
                bigMap.put(id, c);
                bigList.add(c);
            }
            String target = "C09999";

            // HashMap O(1) Search
            long t1 = System.nanoTime();
            Customer hashResult = bigMap.get(target);
            long t2 = System.nanoTime();
            long hashTime = t2 - t1;

            // Linear O(n) Search
            long t3 = System.nanoTime();
            Customer linearResult = null;
            for (Customer c : bigList) {
                if (c.getId().equals(target)) {
                    linearResult = c;
                    break;
                }
            }
            long t4 = System.nanoTime();
            long linearTime = t4 - t3;

            searchLogsModel.addRow(new Object[]{"10k Demo (O(1))", target, "HashMap", hashTime + " ns", "Found User"});
            searchLogsModel.addRow(new Object[]{"10k Demo (O(n))", target, "ArrayList (Linear)", linearTime + " ns", "Found User"});
            
            String msg = "Speed Comparison for n = 10,000 items:\n\n" +
                         "1. HashMap get() [O(1)]:\n" +
                         "    Time: " + hashTime + " ns\n\n" +
                         "2. ArrayList linear search [O(n)]:\n" +
                         "    Time: " + linearTime + " ns\n\n" +
                         "HashMap is approximately " + (linearTime / Math.max(1, hashTime)) + "x faster!\n" +
                         "As n grows, HashMap search time remains flat O(1), while linear search grows O(n).";
            
            javax.swing.JOptionPane.showMessageDialog(this, msg, "HashMap vs Linear Search Demo", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            setStatus("Speed demo finished. HashMap: " + hashTime + " ns | Linear: " + linearTime + " ns.");
            resizeAllTables();
        });
    }

    private void addFoodFromFields() {
        String name = foodNameField.getText().trim();
        String category = foodCategoryField.getText().trim();
        String priceText = foodPriceField.getText().trim();
        String restSelected = selectedValue(foodRestaurantSelect);

        if (name.isEmpty() || category.isEmpty() || priceText.isEmpty() || restSelected.isEmpty()) {
            setStatus("Please fill in all food details.");
            return;
        }

        double price = parseDouble(priceText, -1.0);
        if (price < 0) {
            setStatus("Please enter a valid price.");
            return;
        }

        String restName = stripLeadingId(restSelected);
        FoodItem food = new FoodItem(name, category, price, restName);
        foodBST.insert(food);
        foodAVLTree.insert(food);

        foodNameField.setText("");
        foodCategoryField.setText("");
        foodPriceField.setText("");
        
        refreshAllViews();
        updateRestaurantMenuHint(); // update hint display if it was showing this restaurant
        setStatus("Food item \"" + name + "\" added to menu.");
    }

    private void updateRestaurantMenuHint() {
        String selected = selectedValue(restaurantSelect);
        foodItemSelect.removeAllItems();
        if (selected.isEmpty()) {
            restaurantMenuHintArea.setText("(No restaurant selected)");
            orderSys.clearCart();
            updateCartDisplay();
            return;
        }
        String restName = stripLeadingId(selected);
        
        // Clear cart if the restaurant changed
        if (orderSys.getCurrentBuilder().getRestaurantName() != null && 
            !orderSys.getCurrentBuilder().getRestaurantName().equalsIgnoreCase(restName)) {
            orderSys.clearCart();
            updateCartDisplay();
        }

        List<FoodItem> foods = foodAVLTree.getAllFoods().stream()
                .filter(f -> f.getRestaurantName().equalsIgnoreCase(restName))
                .toList();
        if (foods.isEmpty()) {
            restaurantMenuHintArea.setText("No menu items registered for " + restName + ".");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Menu for ").append(restName).append(":\n");
            for (FoodItem f : foods) {
                sb.append("• ").append(f.getName()).append(" - RM").append(String.format("%.2f", f.getPrice())).append(" (").append(f.getCategory()).append(")\n");
                foodItemSelect.addItem(f.getName());
            }
            restaurantMenuHintArea.setText(sb.toString());
        }
    }

    private void refreshMenuTable() {
        menuModel.setRowCount(0);
        for (FoodItem food : foodAVLTree.getAllFoods()) {
            menuModel.addRow(new Object[]{food.getName(), food.getCategory(), "RM" + String.format("%.2f", food.getPrice()), food.getRestaurantName()});
        }
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }
}
