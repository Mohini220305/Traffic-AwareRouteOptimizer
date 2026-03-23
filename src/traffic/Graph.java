package traffic;

import java.util.*;

public class Graph {

    public static class Edge {
        public final String from;
        public final String to;
        public final int distanceKm;
        public int vehicleCount;
        public double totalSpeed;

        public Edge(String from, String to, int distanceKm) {
            this.from        = from;
            this.to          = to;
            this.distanceKm  = distanceKm;
            this.vehicleCount = 0;
            this.totalSpeed   = 0.0;
        }

        public double avgSpeed() {
            return vehicleCount == 0 ? 60.0 : totalSpeed / vehicleCount;
        }

        public String trafficLevel() {
            if (vehicleCount == 0) return "Clear";
            double s = avgSpeed();
            if (s >= 50) return "Low";
            if (s >= 30) return "Moderate";
            return "Heavy";
        }

        public double congestionCost() {
            return distanceKm * (60.0 / Math.max(avgSpeed(), 5.0));
        }
    }

    private final Map<String, List<Edge>> adj   = new LinkedHashMap<>();
    private final Map<String, Edge>       edges = new LinkedHashMap<>();

    public void addLocation(String name) {
        adj.putIfAbsent(name, new ArrayList<>());
    }

    public void addRoad(String a, String b, int km) {
        Edge fwd = new Edge(a, b, km);
        Edge bwd = new Edge(b, a, km);
        adj.get(a).add(fwd);
        adj.get(b).add(bwd);
        edges.put(a + "|" + b, fwd);
        edges.put(b + "|" + a, bwd);
    }

    public void addVehicle(String road, double speed) {
        Edge e = edges.get(road);
        if (e == null) {
            for (String key : edges.keySet()) {
                if (key.equalsIgnoreCase(road)) { e = edges.get(key); break; }
            }
        }
        if (e != null) {
            e.vehicleCount++;
            e.totalSpeed += speed;
            String rev = e.to + "|" + e.from;
            Edge r = edges.get(rev);
            if (r != null) { r.vehicleCount++; r.totalSpeed += speed; }
        }
    }

    public void clearVehicles() {
        for (Edge e : edges.values()) { e.vehicleCount = 0; e.totalSpeed = 0; }
    }

    public List<Edge>       getNeighbors(String loc)   { return adj.getOrDefault(loc, Collections.emptyList()); }
    public Set<String>      getAllLocations()           { return adj.keySet(); }
    public Collection<Edge> getAllEdges()               { return edges.values(); }
    public Edge             getEdge(String a, String b) { return edges.get(a + "|" + b); }
    public boolean          hasLocation(String n)       { return adj.containsKey(n); }
}
