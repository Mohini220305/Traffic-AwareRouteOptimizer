DEHRADUN TRAFFIC ANALYSER
=========================

COMPILE & RUN
-------------
  mkdir out
  javac -d out src/traffic/*.java
  java -cp out traffic.TrafficApp

  Requires Java 17 or later. No external libraries.


HOW TO USE
----------
1. Run the app.
2. Click "Load vehicles.txt" in the toolbar.
3. Select data/vehicles.txt (or your own file).
4. Vehicles stream into the dashboard one by one — watch the table update.
5. Select From, To, and Algorithm on the left, then click "Find Route".
6. The result box shows the least-congested path, distance, and traffic per segment.
7. Click "Clear" to reset everything.


VEHICLE FILE FORMAT (.txt)
--------------------------
One vehicle per line, three fields separated by spaces:

  VehicleID   Road   SpeedKmh

  - VehicleID : any label, no spaces (V001, BUS3, TRUCK12)
  - Road      : two location names joined by a hyphen, spaces replaced by underscores
  - SpeedKmh  : a number >= 0

Example:
  V001  ISBT_Dehradun-Clock_Tower  52
  BUS3  Clock_Tower-Paltan_Bazaar  18
  TR01  Rispana_Bridge-Balliwala_Chowk  25

Lines starting with # are comments and are ignored.
Empty lines are ignored.


VALID ROADS
-----------
  ISBT_Dehradun-Clock_Tower
  ISBT_Dehradun-Dehradun_Railway_Station
  ISBT_Dehradun-Paltan_Bazaar
  Clock_Tower-Paltan_Bazaar
  Clock_Tower-FRI
  Clock_Tower-Rispana_Bridge
  Paltan_Bazaar-Dehradun_Railway_Station
  Paltan_Bazaar-Pacific_Mall
  Dehradun_Railway_Station-Balliwala_Chowk
  Dehradun_Railway_Station-Prem_Nagar
  Jolly_Grant_Airport-ISBT_Dehradun
  Jolly_Grant_Airport-Balliwala_Chowk
  Pacific_Mall-Sahastradhara
  Pacific_Mall-Mussoorie_Road
  FRI-Mussoorie_Road
  FRI-Sahastradhara
  Sahastradhara-Mussoorie_Road
  Rispana_Bridge-Balliwala_Chowk
  Rispana_Bridge-Clement_Town
  Balliwala_Chowk-Prem_Nagar
  Balliwala_Chowk-Clement_Town
  Prem_Nagar-Clement_Town

Both directions work: A-B is the same road as B-A.


TRAFFIC LEVELS
--------------
  Clear     no vehicles on the road
  Low       avg speed >= 50 km/h   (green)
  Moderate  avg speed >= 30 km/h   (amber/dark yellow)
  Heavy     avg speed <  30 km/h   (red)


ALGORITHMS
----------
  Dijkstra's  Finds the minimum congestion-cost path. Always optimal.
  BFS         Explores layer by layer, neighbours sorted by congestion.
  DFS         Explores depth-first, sorted by congestion. Not always optimal.

  Congestion cost of a road = distance x (60 / avg speed)
  Slower roads cost more, so the route avoids congested segments.


PROJECT FILES
-------------
  src/traffic/
    TrafficApp.java     main window (Swing GUI)
    Graph.java          graph with live vehicle tracking
    Algorithms.java     Dijkstra's, BFS, DFS
    VehicleLoader.java  reads and validates .txt files
    Vehicle.java        data class
    RouteResult.java    data class
    CityData.java       13 Dehradun locations, 22 roads

  data/
    vehicles.txt        50 sample vehicles
