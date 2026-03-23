package traffic;

import java.util.*;

public class Algorithms {

    public static RouteResult dijkstra(Graph g, String start, String end) {
        List<String> locs = new ArrayList<>(g.getAllLocations());
        Map<String, Integer> idx = new HashMap<>();
        for (int i = 0; i < locs.size(); i++) idx.put(locs.get(i), i);

        Map<String, Double> cost = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        for (String l : locs) cost.put(l, Double.MAX_VALUE);
        cost.put(start, 0.0);

        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingDouble(a -> cost.get(locs.get(a[0]))));
        pq.offer(new int[]{ idx.get(start) });

        while (!pq.isEmpty()) {
            String cur = locs.get(pq.poll()[0]);
            if (cur.equals(end)) break;
            for (Graph.Edge e : g.getNeighbors(cur)) {
                double nc = cost.get(cur) + e.congestionCost();
                if (nc < cost.get(e.to)) {
                    cost.put(e.to, nc);
                    prev.put(e.to, cur);
                    pq.offer(new int[]{ idx.get(e.to) });
                }
            }
        }
        return build(g, prev, start, end, "Dijkstra's");
    }

    public static RouteResult bfs(Graph g, String start, String end) {
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        Map<String, String> prev = new HashMap<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            String cur = queue.poll();
            if (cur.equals(end)) break;
            List<Graph.Edge> nb = new ArrayList<>(g.getNeighbors(cur));
            nb.sort(Comparator.comparingDouble(Graph.Edge::congestionCost));
            for (Graph.Edge e : nb) {
                if (!visited.contains(e.to)) {
                    visited.add(e.to);
                    prev.put(e.to, cur);
                    queue.add(e.to);
                }
            }
        }
        return build(g, prev, start, end, "BFS");
    }

    public static RouteResult dfs(Graph g, String start, String end) {
        Deque<String> stack = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        Map<String, String> prev = new HashMap<>();

        stack.push(start);
        while (!stack.isEmpty()) {
            String cur = stack.pop();
            if (visited.contains(cur)) continue;
            visited.add(cur);
            if (cur.equals(end)) break;
            List<Graph.Edge> nb = new ArrayList<>(g.getNeighbors(cur));
            nb.sort(Comparator.comparingDouble(Graph.Edge::congestionCost));
            Collections.reverse(nb);
            for (Graph.Edge e : nb) {
                if (!visited.contains(e.to)) {
                    prev.putIfAbsent(e.to, cur);
                    stack.push(e.to);
                }
            }
        }
        return build(g, prev, start, end, "DFS");
    }

    // finds every simple path (no repeated nodes) using backtracking DFS
    public static List<RouteResult> allPaths(Graph g, String start, String end) {
        List<RouteResult> results = new ArrayList<>();
        List<String> currentPath = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        currentPath.add(start);
        visited.add(start);

        findAllPaths(g, start, end, currentPath, visited, results);

        // sort by total distance so shortest shows first
        results.sort(Comparator.comparingInt(r -> r.distanceKm));

        return results;
    }

    private static void findAllPaths(Graph g, String current, String end,
                                     List<String> currentPath, Set<String> visited,
                                     List<RouteResult> results) {
        if (current.equals(end)) {
            List<String> pathCopy = new ArrayList<>(currentPath);
            int dist = 0;
            for (int i = 0; i < pathCopy.size() - 1; i++) {
                Graph.Edge e = g.getEdge(pathCopy.get(i), pathCopy.get(i + 1));
                if (e != null) dist += e.distanceKm;
            }
            results.add(new RouteResult(pathCopy, dist, "Path " + (results.size() + 1)));
            return;
        }

        for (Graph.Edge edge : g.getNeighbors(current)) {
            if (!visited.contains(edge.to)) {
                visited.add(edge.to);
                currentPath.add(edge.to);

                findAllPaths(g, edge.to, end, currentPath, visited, results);

                currentPath.remove(currentPath.size() - 1);
                visited.remove(edge.to);
            }
        }
    }

    private static RouteResult build(Graph g, Map<String, String> prev,
                                     String start, String end, String name) {
        if (!prev.containsKey(end) && !end.equals(start))
            return new RouteResult(Collections.emptyList(), 0, name);

        List<String> path = new ArrayList<>();
        for (String c = end; c != null; c = prev.get(c)) path.add(0, c);

        int dist = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            Graph.Edge e = g.getEdge(path.get(i), path.get(i + 1));
            if (e != null) dist += e.distanceKm;
        }
        return new RouteResult(path, dist, name);
    }
}