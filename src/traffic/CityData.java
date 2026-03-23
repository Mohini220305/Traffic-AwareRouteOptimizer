package traffic;

public class CityData {

    public static Graph build() {
        Graph g = new Graph();

        g.addLocation("ISBT Dehradun");
        g.addLocation("Clock Tower");
        g.addLocation("Paltan Bazaar");
        g.addLocation("Dehradun Railway Station");
        g.addLocation("Jolly Grant Airport");
        g.addLocation("Pacific Mall");
        g.addLocation("FRI");
        g.addLocation("Sahastradhara");
        g.addLocation("Mussoorie Road");
        g.addLocation("Rispana Bridge");
        g.addLocation("Balliwala Chowk");
        g.addLocation("Prem Nagar");
        g.addLocation("Clement Town");

        g.addRoad("ISBT Dehradun",             "Clock Tower",                 4);
        g.addRoad("ISBT Dehradun",             "Dehradun Railway Station",    3);
        g.addRoad("ISBT Dehradun",             "Paltan Bazaar",               5);
        g.addRoad("Clock Tower",               "Paltan Bazaar",               1);
        g.addRoad("Clock Tower",               "FRI",                         6);
        g.addRoad("Clock Tower",               "Rispana Bridge",              4);
        g.addRoad("Paltan Bazaar",             "Dehradun Railway Station",    2);
        g.addRoad("Paltan Bazaar",             "Pacific Mall",                3);
        g.addRoad("Dehradun Railway Station",  "Balliwala Chowk",             5);
        g.addRoad("Dehradun Railway Station",  "Prem Nagar",                  7);
        g.addRoad("Jolly Grant Airport",       "ISBT Dehradun",              28);
        g.addRoad("Jolly Grant Airport",       "Balliwala Chowk",            22);
        g.addRoad("Pacific Mall",              "Sahastradhara",               8);
        g.addRoad("Pacific Mall",              "Mussoorie Road",              9);
        g.addRoad("FRI",                       "Mussoorie Road",              5);
        g.addRoad("FRI",                       "Sahastradhara",              10);
        g.addRoad("Sahastradhara",             "Mussoorie Road",              7);
        g.addRoad("Rispana Bridge",            "Balliwala Chowk",             6);
        g.addRoad("Rispana Bridge",            "Clement Town",                8);
        g.addRoad("Balliwala Chowk",           "Prem Nagar",                  4);
        g.addRoad("Balliwala Chowk",           "Clement Town",                6);
        g.addRoad("Prem Nagar",                "Clement Town",                5);

        return g;
    }
}
