package traffic;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;

public class TrafficApp extends JFrame {

    Graph graph = CityData.build();

    JComboBox<String> fromBox;
    JComboBox<String> toBox;
    JLabel lblStatus;
    JPanel allPathsPanel;

    public TrafficApp() {

        setTitle("Dehradun Traffic Analyser");
        setSize(680, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        // ---- TOP: title ----
        JLabel titleLabel = new JLabel("Dehradun Traffic Analyser", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 6, 0));
        add(titleLabel, BorderLayout.NORTH);

        // ---- CENTER: form + results ----
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Find All Possible Routes"));

        String[] locations = graph.getAllLocations().toArray(new String[0]);

        fromBox = new JComboBox<>(locations);
        toBox   = new JComboBox<>(locations);

        JButton btnFind = new JButton("Show All Paths");

        formPanel.add(new JLabel("  From:"));
        formPanel.add(fromBox);
        formPanel.add(new JLabel("  To:"));
        formPanel.add(toBox);
        formPanel.add(new JLabel(""));
        formPanel.add(btnFind);

        // scrollable panel that will hold all path boxes
        allPathsPanel = new JPanel();
        allPathsPanel.setLayout(new BoxLayout(allPathsPanel, BoxLayout.Y_AXIS));

        JLabel hint = new JLabel("  Select From and To, then click Show All Paths.");
        hint.setForeground(Color.GRAY);
        allPathsPanel.add(hint);

        JScrollPane scroll = new JScrollPane(allPathsPanel);
        scroll.setBorder(BorderFactory.createTitledBorder("All Possible Paths  (sorted by distance)"));
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel centerWrapper = new JPanel(new BorderLayout(0, 8));
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        centerWrapper.add(formPanel, BorderLayout.NORTH);
        centerWrapper.add(scroll,    BorderLayout.CENTER);

        add(centerWrapper, BorderLayout.CENTER);

        // ---- BOTTOM: status ----
        lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("Arial", Font.PLAIN, 11));
        lblStatus.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        add(lblStatus, BorderLayout.SOUTH);

        // ---- auto-load vehicles on startup ----
        loadVehiclesOnStart();

        // ---- button action ----
        btnFind.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showAllPaths();
            }
        });
    }

    void loadVehiclesOnStart() {
        File file = new File("data/vehicles.txt");

        if (!file.exists()) {
            lblStatus.setText("vehicles.txt not found in data/ folder.");
            return;
        }

        VehicleLoader.Result loaded = VehicleLoader.load(file);

        if (!loaded.errors.isEmpty()) {
            String msg = "Some lines in vehicles.txt were skipped:\n";
            for (String err : loaded.errors) {
                msg = msg + "  " + err + "\n";
            }
            JOptionPane.showMessageDialog(this, msg, "File Warning", JOptionPane.WARNING_MESSAGE);
        }

        for (Vehicle v : loaded.vehicles) {
            graph.addVehicle(v.road, v.speedKmh);
        }

        lblStatus.setText("Loaded " + loaded.vehicles.size() + " vehicles from data/vehicles.txt");
    }

    void showAllPaths() {
        String from = (String) fromBox.getSelectedItem();
        String to   = (String) toBox.getSelectedItem();

        if (from == null || to == null || from.equals(to)) {
            JOptionPane.showMessageDialog(this,
                    "Please choose two different locations.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        allPathsPanel.removeAll();

        List<RouteResult> paths = Algorithms.allPaths(graph, from, to);

        if (paths.isEmpty()) {
            JLabel noPath = new JLabel("  No paths found between " + from + " and " + to + ".");
            noPath.setForeground(Color.RED);
            allPathsPanel.add(noPath);
            allPathsPanel.revalidate();
            allPathsPanel.repaint();
            lblStatus.setText("No paths found.");
            return;
        }

        // heading
        JLabel heading = new JLabel("  Found " + paths.size() + " path(s) from " + from + " to " + to);
        heading.setFont(new Font("Arial", Font.BOLD, 13));
        heading.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        allPathsPanel.add(heading);

        // one box per path
        for (int p = 0; p < paths.size(); p++) {
            RouteResult route = paths.get(p);

            JPanel pathBox = new JPanel();
            pathBox.setLayout(new BoxLayout(pathBox, BoxLayout.Y_AXIS));
            pathBox.setBorder(BorderFactory.createTitledBorder(
                    "Path " + (p + 1) + "   |   " + route.distanceKm + " km   |   " + route.path.size() + " stops"));

            // determine overall worst traffic on this path
            String worstTraffic = "Clear";
            for (int i = 0; i < route.path.size() - 1; i++) {
                Graph.Edge edge = graph.getEdge(route.path.get(i), route.path.get(i + 1));
                if (edge != null) {
                    if (edge.trafficLevel().equals("Heavy")) {
                        worstTraffic = "Heavy";
                        break;
                    } else if (edge.trafficLevel().equals("Moderate")) {
                        worstTraffic = "Moderate";
                    } else if (edge.trafficLevel().equals("Low") && worstTraffic.equals("Clear")) {
                        worstTraffic = "Low";
                    }
                }
            }

            // best path badge
            if (p == 0) {
                JLabel bestLabel = new JLabel("  ★ Shortest distance");
                bestLabel.setFont(new Font("Arial", Font.BOLD, 11));
                bestLabel.setForeground(new Color(0, 100, 180));
                pathBox.add(bestLabel);
            }

            // stops and road segments
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

                        if (traffic.equals("Heavy")) {
                            segLbl.setForeground(Color.RED);
                        } else if (traffic.equals("Moderate")) {
                            segLbl.setForeground(new Color(200, 100, 0));
                        } else if (traffic.equals("Low")) {
                            segLbl.setForeground(new Color(0, 140, 0));
                        } else {
                            segLbl.setForeground(Color.GRAY);
                        }

                        pathBox.add(segLbl);
                    }
                }
            }

            pathBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, pathBox.getPreferredSize().height + 10));
            allPathsPanel.add(pathBox);
            allPathsPanel.add(Box.createVerticalStrut(6));
        }

        allPathsPanel.revalidate();
        allPathsPanel.repaint();

        lblStatus.setText(paths.size() + " paths found from " + from + " to " + to + "  (sorted by distance)");
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                TrafficApp app = new TrafficApp();
                app.setVisible(true);
            }
        });
    }
}
