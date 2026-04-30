package traffic;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;

public class TrafficApp extends JFrame {

    Graph graph = CityData.build();

    JComboBox<String> fromBox;
    JComboBox<String> toBox;
    JComboBox<String> algoBox;
    JLabel lblStatus;
    JPanel allPathsPanel;

    static final Color COL_HEAVY    = new Color(200, 40,  40);
    static final Color COL_MODERATE = new Color(200, 100,  0);
    static final Color COL_LOW      = new Color(  0, 140,  0);
    static final Color COL_CLEAR    = Color.GRAY;
    static final Color COL_BEST_BG  = new Color(232, 245, 233);
    static final Color COL_BEST_BOR = new Color(  0, 140,  0);

    public TrafficApp() {

        setTitle("Dehradun Traffic Analyser");
        setSize(720, 660);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        JLabel titleLabel = new JLabel("Traffic Analyser", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 6, 0));
        add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 8));
        formPanel.setBorder(BorderFactory.createTitledBorder("Find Routes"));

        String[] locations = graph.getAllLocations().toArray(new String[0]);

        fromBox = new JComboBox<>(locations);
        toBox   = new JComboBox<>(locations);

        LocationRenderer locRenderer = new LocationRenderer();
        fromBox.setRenderer(locRenderer);
        toBox.setRenderer(locRenderer);

        algoBox = new JComboBox<>(new String[]{
                "All Paths ", "Dijkstra'", "BFS", "DFS"
        });

        JButton btnFind   = new JButton("Find Route");

        JButton btnReload = new JButton("Reload Traffic");
        //btnReload.setToolTipText("Clear vehicles and reload data/vehicles.txt");

        formPanel.add(new JLabel("  From:"));
        formPanel.add(fromBox);
        formPanel.add(new JLabel("  To:"));
        formPanel.add(toBox);
        formPanel.add(new JLabel("  Algorithm:"));
        formPanel.add(algoBox);

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 8, 0));
        btnRow.add(btnFind);
        btnRow.add(btnReload);
        formPanel.add(new JLabel(""));
        formPanel.add(btnRow);

        allPathsPanel = new JPanel();
        allPathsPanel.setLayout(new BoxLayout(allPathsPanel, BoxLayout.Y_AXIS));

        JLabel hint = new JLabel("  Select From, To and Algorithm, then click Find Route.");
        hint.setForeground(Color.GRAY);
        allPathsPanel.add(hint);

        JScrollPane scroll = new JScrollPane(allPathsPanel);
        scroll.setBorder(BorderFactory.createTitledBorder("Results"));
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel centerWrapper = new JPanel(new BorderLayout(0, 8));
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        centerWrapper.add(formPanel, BorderLayout.NORTH);
        centerWrapper.add(scroll,    BorderLayout.CENTER);

        add(centerWrapper, BorderLayout.CENTER);

        lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("Arial", Font.PLAIN, 11));
        lblStatus.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        add(lblStatus, BorderLayout.SOUTH);

        loadVehicles();
        btnFind.addActionListener(e -> showResults());
        btnReload.addActionListener(e -> {
            graph.clearVehicles();
            loadVehicles();
            fromBox.repaint();
            toBox.repaint();
        });
    }

    class LocationRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            JLabel lbl = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            if (value instanceof String) {
                String loc = (String) value;
                Color dot = trafficColorForLocation(loc);
                lbl.setIcon(new DotIcon(dot));
                lbl.setIconTextGap(6);
            }
            return lbl;
        }
    }

    static class DotIcon implements javax.swing.Icon {
        private final Color color;
        DotIcon(Color c) { this.color = c; }
        public int getIconWidth()  { return 10; }
        public int getIconHeight() { return 10; }
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(color);
            g.fillOval(x, y, 10, 10);
        }
    }

    Color trafficColorForLocation(String loc) {
        String worst = "Clear";
        for (Graph.Edge e : graph.getNeighbors(loc)) {
            String t = e.trafficLevel();
            if (t.equals("Heavy"))                                   { worst = "Heavy";    break; }
            if (t.equals("Moderate"))                                  worst = "Moderate";
            if (t.equals("Low") && worst.equals("Clear"))              worst = "Low";
        }
        switch (worst) {
            case "Heavy":    return COL_HEAVY;
            case "Moderate": return COL_MODERATE;
            case "Low":      return COL_LOW;
            default:         return COL_CLEAR;
        }
    }

    void loadVehicles() {
        File file = new File("data/vehicles.txt");
        if (!file.exists()) {
            lblStatus.setText("vehicles.txt not found in data/ folder.");
            return;
        }
        VehicleLoader.Result loaded = VehicleLoader.load(file);
        if (!loaded.errors.isEmpty()) {
            StringBuilder msg = new StringBuilder("Some lines in vehicles.txt were skipped:\n");
            for (String err : loaded.errors) msg.append("  ").append(err).append("\n");
            JOptionPane.showMessageDialog(this, msg.toString(), "File Warning", JOptionPane.WARNING_MESSAGE);
        }
        for (Vehicle v : loaded.vehicles) graph.addVehicle(v.road, v.speedKmh);
        lblStatus.setText("Loaded " + loaded.vehicles.size() + " vehicles from data/vehicles.txt");
    }

    void showResults() {
        String from = (String) fromBox.getSelectedItem();
        String to   = (String) toBox.getSelectedItem();
        String algo = (String) algoBox.getSelectedItem();

        if (from == null || to == null || from.equals(to)) {
            JOptionPane.showMessageDialog(this,
                    "Please choose two different locations.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        allPathsPanel.removeAll();

        if ("All Paths (Backtracking)".equals(algo)) {
            showAllPaths(from, to);
        } else {
            showSingleAlgo(from, to, algo);
        }

        allPathsPanel.revalidate();
        allPathsPanel.repaint();
    }

    void showSingleAlgo(String from, String to, String algo) {
        RouteResult result;
        switch (algo) {
            case "Dijkstra's": result = Algorithms.dijkstra(graph, from, to); break;
            case "BFS":        result = Algorithms.bfs(graph, from, to);      break;
            case "DFS":        result = Algorithms.dfs(graph, from, to);      break;
            default:           return;
        }

        if (result.path.isEmpty()) {
            JLabel noPath = new JLabel("  No path found between " + from + " and " + to + " using " + algo + ".");
            noPath.setForeground(Color.RED);
            allPathsPanel.add(noPath);
            lblStatus.setText("No path found using " + algo + ".");
            return;
        }

        allPathsPanel.add(buildSummaryBar(List.of(result), from, to, algo));
        allPathsPanel.add(Box.createVerticalStrut(6));

        JPanel pathBox = buildPathBox(result, false, false);
        pathBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, pathBox.getPreferredSize().height + 10));
        allPathsPanel.add(pathBox);

        lblStatus.setText(algo + " path: " + result.distanceKm + " km · "
                + result.path.size() + " stops · Traffic: " + worstTraffic(result));
    }

    void showAllPaths(String from, String to) {
        List<RouteResult> paths = Algorithms.allPaths(graph, from, to);

        if (paths.isEmpty()) {
            JLabel noPath = new JLabel("  No paths found between " + from + " and " + to + ".");
            noPath.setForeground(Color.RED);
            allPathsPanel.add(noPath);
            lblStatus.setText("No paths found.");
            return;
        }

        RouteResult dijkstraBest = Algorithms.dijkstra(graph, from, to);

        allPathsPanel.add(buildSummaryBar(paths, from, to, "All Paths"));
        allPathsPanel.add(Box.createVerticalStrut(6));

        for (int p = 0; p < paths.size(); p++) {
            RouteResult route = paths.get(p);

            boolean isBestRoute = route.path.equals(dijkstraBest.path);
            boolean isShortest  = (p == 0);

            JPanel pathBox = buildPathBox(route, isShortest, isBestRoute);
            pathBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, pathBox.getPreferredSize().height + 10));
            allPathsPanel.add(pathBox);
            allPathsPanel.add(Box.createVerticalStrut(6));
        }

        int minKm = paths.stream().mapToInt(r -> r.distanceKm).min().orElse(0);
        int maxKm = paths.stream().mapToInt(r -> r.distanceKm).max().orElse(0);
        int avgKm = (int) paths.stream().mapToInt(r -> r.distanceKm).average().orElse(0);
        long clearPaths = paths.stream().filter(r -> worstTraffic(r).equals("Clear")).count();

        lblStatus.setText(paths.size() + " paths found  ·  Shortest: " + minKm
                + " km  ·  Longest: " + maxKm + " km  ·  Avg: " + avgKm
                + " km  ·  Traffic-free: " + clearPaths);
    }

    JPanel buildSummaryBar(List<RouteResult> paths, String from, String to, String algo) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        bar.setBackground(new Color(245, 245, 250));
        bar.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 210), 1, true),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)));

        int total = paths.size();
        long heavyCount    = paths.stream().filter(r -> worstTraffic(r).equals("Heavy")).count();
        long moderateCount = paths.stream().filter(r -> worstTraffic(r).equals("Moderate")).count();
        long clearCount    = paths.stream().filter(r -> worstTraffic(r).equals("Clear") || worstTraffic(r).equals("Low")).count();
        int  minKm         = paths.stream().mapToInt(r -> r.distanceKm).min().orElse(0);

        bar.add(summaryChip(from + "  →  " + to, Color.DARK_GRAY, new Color(235, 235, 245)));
        bar.add(summaryChip(algo, new Color(50, 80, 160), new Color(230, 235, 255)));
        if (total > 1) {
            bar.add(summaryChip(total + " paths", Color.DARK_GRAY, new Color(240, 240, 240)));
            bar.add(summaryChip("Shortest: " + minKm + " km", new Color(0, 100, 0), new Color(232, 245, 233)));
        }
        if (clearCount > 0)
            bar.add(summaryChip(clearCount + " clear", COL_LOW, new Color(232, 245, 233)));
        if (moderateCount > 0)
            bar.add(summaryChip(moderateCount + " moderate", COL_MODERATE, new Color(255, 243, 224)));
        if (heavyCount > 0)
            bar.add(summaryChip(heavyCount + " heavy", COL_HEAVY, new Color(255, 235, 235)));

        return bar;
    }

    JLabel summaryChip(String text, Color fg, Color bg) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.BOLD, 11));
        lbl.setForeground(fg);
        lbl.setBackground(bg);
        lbl.setOpaque(true);
        lbl.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(fg.brighter(), 1, true),
                BorderFactory.createEmptyBorder(2, 7, 2, 7)));
        return lbl;
    }

    JPanel buildPathBox(RouteResult route, boolean isShortest, boolean isBestRoute) {
        JPanel pathBox = new JPanel();
        pathBox.setLayout(new BoxLayout(pathBox, BoxLayout.Y_AXIS));

        String title = route.distanceKm + " km   |   " + route.path.size() + " stops";
        if (!route.algorithm.startsWith("Path"))
            title = route.algorithm + "   |   " + title;

        if (isBestRoute) {
            pathBox.setBackground(COL_BEST_BG);
            pathBox.setOpaque(true);
            pathBox.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(COL_BEST_BOR, 2, true),
                    BorderFactory.createTitledBorder(
                            new LineBorder(COL_BEST_BOR, 0),
                            title)));
        } else {
            pathBox.setBorder(BorderFactory.createTitledBorder(title));
        }

        JPanel badges = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 1));
        badges.setOpaque(false);

        if (isBestRoute) {
            JLabel bestBadge = new JLabel("  ★ Best route (Dijkstra)");
            bestBadge.setFont(new Font("Arial", Font.BOLD, 11));
            bestBadge.setForeground(COL_BEST_BOR);
            badges.add(bestBadge);
        }
        if (isShortest && !isBestRoute) {
            JLabel shortBadge = new JLabel("  ★ Shortest distance");
            shortBadge.setFont(new Font("Arial", Font.BOLD, 11));
            shortBadge.setForeground(new Color(0, 100, 180));
            badges.add(shortBadge);
        }
        if (isBestRoute || isShortest)
            pathBox.add(badges);

        for (int i = 0; i < route.path.size(); i++) {
            String location = route.path.get(i);

            JPanel stopRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 1));
            stopRow.setOpaque(false);

            JLabel numLbl = new JLabel((i + 1) + ".");
            numLbl.setFont(new Font("Monospaced", Font.BOLD, 12));
            numLbl.setPreferredSize(new Dimension(26, 16));

            JLabel locLbl = new JLabel(location);
            locLbl.setFont(new Font("Arial", Font.PLAIN, 12));

            stopRow.add(numLbl);
            stopRow.add(locLbl);
            pathBox.add(stopRow);

            if (i < route.path.size() - 1) {
                Graph.Edge edge = graph.getEdge(route.path.get(i), route.path.get(i + 1));
                if (edge != null) {
                    String traffic = edge.trafficLevel();
                    String segText = "       |  " + edge.distanceKm + " km  --  " + traffic;
                    JLabel segLbl = new JLabel(segText);
                    segLbl.setFont(new Font("Monospaced", Font.PLAIN, 11));
                    segLbl.setForeground(trafficColor(traffic));
                    pathBox.add(segLbl);
                }
            }
        }

        return pathBox;
    }

    String worstTraffic(RouteResult route) {
        String worst = "Clear";
        for (int i = 0; i < route.path.size() - 1; i++) {
            Graph.Edge edge = graph.getEdge(route.path.get(i), route.path.get(i + 1));
            if (edge == null) continue;
            String t = edge.trafficLevel();
            if (t.equals("Heavy"))                               { return "Heavy"; }
            if (t.equals("Moderate"))                              worst = "Moderate";
            if (t.equals("Low") && worst.equals("Clear"))          worst = "Low";
        }
        return worst;
    }

    Color trafficColor(String level) {
        switch (level) {
            case "Heavy":    return COL_HEAVY;
            case "Moderate": return COL_MODERATE;
            case "Low":      return COL_LOW;
            default:         return COL_CLEAR;
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new TrafficApp().setVisible(true));
    }
}