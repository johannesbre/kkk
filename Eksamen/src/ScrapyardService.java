import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ScrapyardService {
    private Connection conn; //databaseforbindelse

    //Filnavn for konfigurasjon
    private static final String PROPERTIES_FILE = "Scrapyard.properties";

    //Dette er det som setter opp hele databaseforbindelsen
    public ScrapyardService() throws IOException, SQLException {
        Properties props = loadProperties();
        String dbUrl = constructJdbcUrl(props);
        String user = props.getProperty("uname");
        String password = props.getProperty("pwd");
        if (user == null || password == null) {
            throw new IllegalArgumentException("Manglende egenskaper i Scrapyard.properties");
        }
        this.conn = DriverManager.getConnection(dbUrl, user, password);
    }

    //Det som laster scrapyard.properties egenskaper:
    private Properties loadProperties() throws IOException {
        Properties props = new Properties();
        try (FileInputStream input = new FileInputStream(PROPERTIES_FILE)) {
            props.load(input);
        } catch (IOException e) {
            throw new IOException("Klarer ikke finne/lese " + PROPERTIES_FILE + " i prosjektroten (" + System.getProperty("user.dir") + "). Sørg for at filen eksisterer.", e);
        }
        return props;
    }

    //Denne lager jdbc url til forbindelsen til databasen
    private String constructJdbcUrl(Properties props) {
        String host = props.getProperty("host");
        String port = props.getProperty("port");
        String dbName = props.getProperty("db_name");
        if (host == null || port == null || dbName == null) {
            throw new IllegalArgumentException("Manglende egenskaper i Scrapyard.properties");
        }
        return String.format("jdbc:mysql://%s:%s/%s", host, port, dbName);
    }

    //får data fra fil over til databasen
    public void importDataFromFile(String filePath) throws SQLException, IOException {
        Statement stmt = conn.createStatement();
        //Gjør at det ikke kommer konflikt ved kjøring over og over igjen
        stmt.executeUpdate("DELETE FROM FossilCar");
        stmt.executeUpdate("DELETE FROM ElectricCar");
        stmt.executeUpdate("DELETE FROM Motorcycle");
        stmt.executeUpdate("DELETE FROM Scrapyard");

        List<Scrapyard> scrapyards = new ArrayList<>();
        List<Vehicle> vehicles = new ArrayList<>();

        //Det som leser gjennom data fra filen
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            int numScrapyards = Integer.parseInt(reader.readLine().trim());
            for (int i = 0; i < numScrapyards; i++) {
                int id = Integer.parseInt(reader.readLine().trim());
                String name = reader.readLine().trim();
                String address = reader.readLine().trim();
                String phoneNumber = reader.readLine().trim();
                reader.readLine();
                scrapyards.add(new Scrapyard(id, name, address, phoneNumber));
            }

            int numVehicles = Integer.parseInt(reader.readLine().trim());
            for (int i = 0; i < numVehicles; i++) {
                int vehicleId = Integer.parseInt(reader.readLine().trim());
                int scrapyardId = Integer.parseInt(reader.readLine().trim());
                String vehicleType = reader.readLine().trim();
                String brand = reader.readLine().trim();
                String model = reader.readLine().trim();
                int yearModel = Integer.parseInt(reader.readLine().trim());
                String registrationNumber = reader.readLine().trim();
                String chassisNumber = reader.readLine().trim();
                boolean driveable = Boolean.parseBoolean(reader.readLine().trim());
                int numberOfSellableWheels = Integer.parseInt(reader.readLine().trim());

                Vehicle vehicle;
                if (vehicleType.equals("FossilCar")) {
                    String fuelType = reader.readLine().trim();
                    int fuelAmount = Integer.parseInt(reader.readLine().trim());
                    vehicle = new FossilCar(vehicleId, brand, model, yearModel, registrationNumber, chassisNumber,
                            driveable, numberOfSellableWheels, scrapyardId, fuelType, fuelAmount);

                } else if (vehicleType.equals("ElectricCar")) {
                    int batteryCapacity = Integer.parseInt(reader.readLine().trim());
                    int chargeLevel = Integer.parseInt(reader.readLine().trim());
                    vehicle = new ElectricCar(vehicleId, brand, model, yearModel, registrationNumber, chassisNumber,
                            driveable, numberOfSellableWheels, scrapyardId, batteryCapacity, chargeLevel);

                } else if (vehicleType.equals("Motorcycle")) {
                    boolean hasSidecar = Boolean.parseBoolean(reader.readLine().trim());
                    int engineCapacity = Integer.parseInt(reader.readLine().trim());
                    boolean isModified = Boolean.parseBoolean(reader.readLine().trim());
                    int numberOfWheels = Integer.parseInt(reader.readLine().trim());
                    vehicle = new Motorcycle(vehicleId, brand, model, yearModel, registrationNumber, chassisNumber,
                            driveable, numberOfSellableWheels, scrapyardId, hasSidecar, engineCapacity, isModified, numberOfWheels);

                } else {
                    throw new IOException("Ukjent type kjøretøy: " + vehicleType);
                }
                vehicles.add(vehicle);
                reader.readLine();
            }
        }

        // Putter data inn i databasen
        try (Statement st = conn.createStatement()) {
            for (Scrapyard scrapyard : scrapyards) {
                String sql = String.format(INSERT_SCRAPYARD_SQL, scrapyard.getId(), scrapyard.getName(),
                        scrapyard.getAddress(), scrapyard.getPhoneNumber());
                st.executeUpdate(sql);
            }
            for (Vehicle vehicle : vehicles) {
                String sql;
                if (vehicle instanceof FossilCar fossilCar) {
                    sql = String.format(INSERT_FOSSILCAR_SQL, vehicle.getId(), vehicle.getBrand(), vehicle.getModel(),
                            vehicle.getYearModel(), vehicle.getRegistrationNumber(), vehicle.getChassisNumber(),
                            vehicle.isDriveable(), vehicle.getNumberOfSellableWheels(), vehicle.getScrapyardId(),
                            fossilCar.getFuelType(), fossilCar.getFuelAmount());

                } else if (vehicle instanceof ElectricCar electricCar) {
                    sql = String.format(INSERT_ELECTRICCAR_SQL, vehicle.getId(), vehicle.getBrand(), vehicle.getModel(),
                            vehicle.getYearModel(), vehicle.getRegistrationNumber(), vehicle.getChassisNumber(),
                            vehicle.isDriveable(), vehicle.getNumberOfSellableWheels(), vehicle.getScrapyardId(),
                            electricCar.getBatteryCapacity(), electricCar.getChargeLevel());

                } else {
                    Motorcycle motorcycle = (Motorcycle) vehicle;
                    sql = String.format(INSERT_MOTORCYCLE_SQL, vehicle.getId(), vehicle.getBrand(), vehicle.getModel(),
                            vehicle.getYearModel(), vehicle.getRegistrationNumber(), vehicle.getChassisNumber(),
                            vehicle.isDriveable(), vehicle.getNumberOfSellableWheels(), vehicle.getScrapyardId(),
                            motorcycle.getHasSidecar(), motorcycle.getEngineCapacity(), motorcycle.getIsModified(),
                            motorcycle.getNumberOfWheels());
                }
                st.executeUpdate(sql);
            }
        }
    }
//får frem alle skraphandlere fra databasen
    public List<Scrapyard> getAllScrapyards() throws SQLException, IOException {
        List<Scrapyard> scrapyards = new ArrayList<>();
        Properties props = loadProperties();
        String dbUrl = constructJdbcUrl(props);
        String user = props.getProperty("uname");
        String password = props.getProperty("pwd");

        if (user == null || password == null) {
            throw new IllegalArgumentException("Manglende egenskaper i Scrapyard.properties");
        }

        try (Connection conn = DriverManager.getConnection(dbUrl, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Scrapyard")) {
            while (rs.next()) {
                int id = rs.getInt("ScrapyardID");
                String name = rs.getString("Name");
                String address = rs.getString("Address");
                String phoneNumber = rs.getString("PhoneNumber");
                scrapyards.add(new Scrapyard(id, name, address, phoneNumber));
            }
        }
        return scrapyards;
    }

    //Pluker ut alle kjøretøy fra databasen min
    public List<Vehicle> getAllVehicles() throws SQLException, IOException {
        List<Vehicle> vehicles = new ArrayList<>();
        Properties props = loadProperties();
        String dbUrl = constructJdbcUrl(props);
        String user = props.getProperty("uname");
        String password = props.getProperty("pwd");

        if (user == null || password == null) {
            throw new IllegalArgumentException("Manglende egenskaper i Scrapyard.properties");
        }

        try (Connection conn = DriverManager.getConnection(dbUrl, user, password);
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT * FROM FossilCar");
            while (rs.next()) {
                vehicles.add(new FossilCar(
                        rs.getInt("VehicleID"), rs.getString("Brand"), rs.getString("Model"),
                        rs.getInt("YearModel"), rs.getString("RegistrationNumber"), rs.getString("ChassisNumber"),
                        rs.getBoolean("Driveable"), rs.getInt("NumberOfSellableWheels"), rs.getInt("ScrapyardID"),
                        rs.getString("FuelType"), rs.getInt("FuelAmount")));
            }

            rs = stmt.executeQuery("SELECT * FROM ElectricCar");
            while (rs.next()) {
                vehicles.add(new ElectricCar(
                        rs.getInt("VehicleID"), rs.getString("Brand"), rs.getString("Model"),
                        rs.getInt("YearModel"), rs.getString("RegistrationNumber"), rs.getString("ChassisNumber"),
                        rs.getBoolean("Driveable"), rs.getInt("NumberOfSellableWheels"), rs.getInt("ScrapyardID"),
                        rs.getInt("BatteryCapacity"), rs.getInt("ChargeLevel")));
            }

            rs = stmt.executeQuery("SELECT * FROM Motorcycle");
            while (rs.next()) {
                vehicles.add(new Motorcycle(
                        rs.getInt("VehicleID"), rs.getString("Brand"), rs.getString("Model"),
                        rs.getInt("YearModel"), rs.getString("RegistrationNumber"), rs.getString("ChassisNumber"),
                        rs.getBoolean("Driveable"), rs.getInt("NumberOfSellableWheels"), rs.getInt("ScrapyardID"),
                        rs.getBoolean("HasSidecar"), rs.getInt("EngineCapacity"), rs.getBoolean("IsModified"),
                        rs.getInt("NumberOfWheels")));
            }
        }
        return vehicles;
    }

    //Regner seg frem til drivstoff totalt i fossilbilene
    public int getTotalFuelAmount() throws SQLException, IOException {
        Properties props = loadProperties();
        String dbUrl = constructJdbcUrl(props);
        String user = props.getProperty("uname");
        String password = props.getProperty("pwd");

        if (user == null || password == null) {
            throw new IllegalArgumentException("Manglende egenskaper i Scrapyard.properties");
        }

        try (Connection conn = DriverManager.getConnection(dbUrl, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT SUM(FuelAmount) AS TotalFuel FROM FossilCar")) {
            if (rs.next()) {
                return rs.getInt("TotalFuel");
            }
            return 0;
        }
    }

    //Får frem alle kjørbare kjøretøy:
    public List<Vehicle> getDriveableVehicles() throws SQLException, IOException {
        List<Vehicle> vehicles = new ArrayList<>();
        Properties props = loadProperties();
        String dbUrl = constructJdbcUrl(props);
        String user = props.getProperty("uname");
        String password = props.getProperty("pwd");

        if (user == null || password == null) {
            throw new IllegalArgumentException("Manglende egenskaper i Scrapyard.properties");
        }

        try (Connection conn = DriverManager.getConnection(dbUrl, user, password);
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT * FROM FossilCar WHERE Driveable = TRUE");
            while (rs.next()) {
                vehicles.add(new FossilCar(
                        rs.getInt("VehicleID"), rs.getString("Brand"), rs.getString("Model"),
                        rs.getInt("YearModel"), rs.getString("RegistrationNumber"), rs.getString("ChassisNumber"),
                        rs.getBoolean("Driveable"), rs.getInt("NumberOfSellableWheels"), rs.getInt("ScrapyardID"),
                        rs.getString("FuelType"), rs.getInt("FuelAmount")));
            }

            rs = stmt.executeQuery("SELECT * FROM ElectricCar WHERE Driveable = TRUE");
            while (rs.next()) {
                vehicles.add(new ElectricCar(
                        rs.getInt("VehicleID"), rs.getString("Brand"), rs.getString("Model"),
                        rs.getInt("YearModel"), rs.getString("RegistrationNumber"), rs.getString("ChassisNumber"),
                        rs.getBoolean("Driveable"), rs.getInt("NumberOfSellableWheels"), rs.getInt("ScrapyardID"),
                        rs.getInt("BatteryCapacity"), rs.getInt("ChargeLevel")));
            }

            rs = stmt.executeQuery("SELECT * FROM Motorcycle WHERE Driveable = TRUE");
            while (rs.next()) {
                vehicles.add(new Motorcycle(
                        rs.getInt("VehicleID"), rs.getString("Brand"), rs.getString("Model"),
                        rs.getInt("YearModel"), rs.getString("RegistrationNumber"), rs.getString("ChassisNumber"),
                        rs.getBoolean("Driveable"), rs.getInt("NumberOfSellableWheels"), rs.getInt("ScrapyardID"),
                        rs.getBoolean("HasSidecar"), rs.getInt("EngineCapacity"), rs.getBoolean("IsModified"),
                        rs.getInt("NumberOfWheels")));
            }
        }
        return vehicles;
    }

    //Finner de skraphandlerene med mest kjøretøy:
    public String getMostUsedScrapyard() throws SQLException, IOException {
        Properties props = loadProperties();
        String dbUrl = constructJdbcUrl(props);
        String user = props.getProperty("uname");
        String password = props.getProperty("pwd");

        if (user == null || password == null) {
            throw new IllegalArgumentException("Manglende egenskaper i Scrapyard.properties");
        }

        Map<Integer, Integer> vehicleCounts = new HashMap<>();
        Map<Integer, String> scrapyardNames = new HashMap<>();

        try (Connection conn = DriverManager.getConnection(dbUrl, user, password);
             Statement stmt = conn.createStatement()) {
            // Finner navn på skraphandlere
            ResultSet rs = stmt.executeQuery("SELECT ScrapyardID, Name FROM Scrapyard");
            while (rs.next()) {
                scrapyardNames.put(rs.getInt("ScrapyardID"), rs.getString("Name"));
            }

            // Teller antall fossilbiler
            rs = stmt.executeQuery("SELECT ScrapyardID, COUNT(*) AS Count FROM FossilCar GROUP BY ScrapyardID");
            while (rs.next()) {
                int scrapyardId = rs.getInt("ScrapyardID");
                int count = rs.getInt("Count");
                vehicleCounts.put(scrapyardId, vehicleCounts.getOrDefault(scrapyardId, 0) + count);
            }

            // Teller antall elbiler
            rs = stmt.executeQuery("SELECT ScrapyardID, COUNT(*) AS Count FROM ElectricCar GROUP BY ScrapyardID");
            while (rs.next()) {
                int scrapyardId = rs.getInt("ScrapyardID");
                int count = rs.getInt("Count");
                vehicleCounts.put(scrapyardId, vehicleCounts.getOrDefault(scrapyardId, 0) + count);
            }

            // Teller antall motorsykler
            rs = stmt.executeQuery("SELECT ScrapyardID, COUNT(*) AS Count FROM Motorcycle GROUP BY ScrapyardID");
            while (rs.next()) {
                int scrapyardId = rs.getInt("ScrapyardID");
                int count = rs.getInt("Count");
                vehicleCounts.put(scrapyardId, vehicleCounts.getOrDefault(scrapyardId, 0) + count);
            }
        }

        // Finn skraphandleren med flest kjøretøy som da blir den mest populære:
        int maxCount = 0;
        String mostUsedScrapyard = "Ingen skraphandlere funnet";
        for (Map.Entry<Integer, Integer> entry : vehicleCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                String name = scrapyardNames.get(entry.getKey());
                if (name != null) {
                    mostUsedScrapyard = String.format("Skraphandler: %s, Antall kjøretøy: %d", name, maxCount);
                }
            }
        }

        return mostUsedScrapyard;
    }
}