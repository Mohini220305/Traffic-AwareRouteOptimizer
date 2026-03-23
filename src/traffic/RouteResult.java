package traffic;

import java.util.List;

public class RouteResult {
    public final List<String> path;
    public final int          distanceKm;
    public final String       algorithm;

    public RouteResult(List<String> path, int distanceKm, String algorithm) {
        this.path       = path;
        this.distanceKm = distanceKm;
        this.algorithm  = algorithm;
    }
}
