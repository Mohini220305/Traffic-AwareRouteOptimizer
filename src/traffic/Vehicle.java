package traffic;

public class Vehicle {
    public final String vehicleId;
    public final String road;
    public final double speedKmh;

    public Vehicle(String vehicleId, String road, double speedKmh) {
        this.vehicleId = vehicleId;
        this.road      = road;
        this.speedKmh  = speedKmh;
    }
}
