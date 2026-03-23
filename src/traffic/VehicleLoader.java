package traffic;

import java.io.*;
import java.util.*;

public class VehicleLoader {

    public static class Result {
        public final List<Vehicle> vehicles;
        public final List<String>  errors;
        public Result(List<Vehicle> v, List<String> e) { vehicles = v; errors = e; }
    }

    public static Result load(File file) {
        List<Vehicle> vehicles = new ArrayList<>();
        List<String>  errors   = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNum = 0;

            while ((line = br.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\s+", 3);
                if (parts.length != 3) {
                    errors.add("Line " + lineNum + ": need 3 fields, got " + parts.length + " -> \"" + line + "\"");
                    continue;
                }

                String id    = parts[0];
                String road  = parts[1].replace("_", " ");
                double speed;

                try {
                    speed = Double.parseDouble(parts[2]);
                    if (speed < 0) throw new NumberFormatException("negative");
                } catch (NumberFormatException e) {
                    errors.add("Line " + lineNum + ": bad speed \"" + parts[2] + "\"");
                    continue;
                }

                vehicles.add(new Vehicle(id, road, speed));
            }
        } catch (IOException e) {
            errors.add("Cannot read file: " + e.getMessage());
        }

        return new Result(vehicles, errors);
    }
}
