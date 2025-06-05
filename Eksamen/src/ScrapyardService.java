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

    //Setter opp databaseforbindelse
    public ScrapyardService() throws IOException, SQLException {
        Properties props = loadProperties();
        String dbUrl = buildJdbcUrl(props);
        String user = props.getProperty("uname");
        String password = props.getProperty("pwd");
        
        if (user == null || password == null) {
            throw new IllegalArgumentException("Manglende brukerdata i properties-fil");
        }
        
        this.conn = DriverManager.getConnection(dbUrl, user, password);
    }

    //Laster properties-filen
    private Properties loadProperties() throws IOException {
        Properties props = new Properties();
        
        // Prøver flere steder hvor filen kan være
        String[] muligeSti = {
            PROPERTIES_FILE,
            "src/" + PROPERTIES_FILE,
            "../" + PROPERTIES_FILE,
            "./" + PROPERTIES_FILE
        };
        
        for (String sti : muligeSti) {
            try (FileInputStream input = new FileInputStream(sti)) {
                props.load(input);
                return props;
            } catch (IOException e) {
                // Prøver neste sti
            }
        }
        
        throw new IOException("Finner ikke " + PROPERTIES_FILE + " - sjekket flere mapper");
    }

    //Bygger jdbc url fra properties
    private String buildJdbcUrl(Properties props) {
        String host = props.getProperty("host");
        String port = props.getProperty("port");
        String dbName = props.getProperty("db_name");
        
        if (host == null || port == null || dbName == null) {
            throw new IllegalArgumentException("Manglende database info i properties");
        }
        
        return String.format("jdbc:mysql://%s:%s/%s", host, port, dbName);
    }

    //Importerer data fra tekstfil til database
    public void importDataFromFile(String filePath) throws SQLException, IOException {
        //Sletter gamle data først
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("DELETE FROM FossilCar");
        stmt.executeUpdate("DELETE FROM ElectricCar");
        stmt.executeUpdate("DELETE FROM Motorcycle");
        stmt.executeUpdate("DELETE FROM Scrapyard");

        List<Scrapyard> scrapyards = new ArrayList<>();
        List<Vehicle> vehicles = new ArrayList<>();

        //Leser filen
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            int antallScrapyards = Integer.parseInt(reader.readLine().trim());
            
            //Leser skraphandlere
            for (int i = 0; i < antallScrapyards; i++) {
                int id = Integer.parseInt(reader.readLine().trim());
                String navn = reader.readLine().trim();
                String adresse = reader.readLine().trim();
                String telefon = reader.readLine().trim();
                reader.readLine(); //hopper over ---
                scrapyards.add(new Scrapyard(id, navn, adresse, telefon));
            }

            int antallVehicles = Integer.parseInt(reader.readLine().trim());
            
            //Leser kjøretøy
            for (int i = 0; i < antallVehicles; i++) {
                int vehicleId = Integer.parseInt(reader.readLine().trim());
                int scrapyardId = Integer.parseInt(reader.readLine().trim());
                String type = reader.readLine().trim();
                String merke = reader.readLine().trim();
                String modell = reader.readLine().trim();
                int aar = Integer.parseInt(reader.readLine().trim());
                String regNr = reader.readLine().trim();
                String chassis = reader.readLine().trim();
                boolean kjorbar = Boolean.parseBoolean(reader.readLine().trim());
                int hjul = Integer.parseInt(reader.readLine().trim());

                Vehicle vehicle = null;
                
                if (type.equals("FossilCar")) {
                    String drivstoffType = reader.readLine().trim();
                    int mengde = Integer.parseInt(reader.readLine().trim());
                    vehicle = new FossilCar(vehicleId, merke, modell, aar, regNr, chassis, kjorbar, hjul, scrapyardId, drivstoffType, mengde);
                    
                } else if (type.equals("ElectricCar")) {
                    int batteriKap = Integer.parseInt(reader.readLine().trim());
                    int ladeNivaa = Integer.parseInt(reader.readLine().trim());
                    vehicle = new ElectricCar(vehicleId, merke, modell, aar, regNr, chassis, kjorbar, hjul, scrapyardId, batteriKap, ladeNivaa);
                    
                } else if (type.equals("Motorcycle")) {
                    boolean sidevogn = Boolean.parseBoolean(reader.readLine().trim());
                    int motorKap = Integer.parseInt(reader.readLine().trim());
                    boolean modifisert = Boolean.parseBoolean(reader.readLine().trim());
                    int antallHjul = Integer.parseInt(reader.readLine().trim());
                    vehicle = new Motorcycle(vehicleId, merke, modell, aar, regNr, chassis, kjorbar, hjul, scrapyardId, sidevogn, motorKap, modifisert, antallHjul);
                    
                } else {
                    throw new IOException("Ukjent kjøretøytype: " + type);
                }
                
                vehicles.add(vehicle);
                reader.readLine(); //hopper over ---
            }
        }

        //Setter inn i database
        insertScrapyards(scrapyards);
        insertVehicles(vehicles);
    }

    //Setter inn skraphandlere
    private void insertScrapyards(List<Scrapyard> scrapyards) throws SQLException {
        String sql = "INSERT INTO Scrapyard (ScrapyardID, Name, Address, PhoneNumber) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Scrapyard scrapyard : scrapyards) {
                pstmt.setInt(1, scrapyard.getId());
                pstmt.setString(2, scrapyard.getName());
                pstmt.setString(3, scrapyard.getAddress());
                pstmt.setString(4, scrapyard.getPhoneNumber());
                pstmt.executeUpdate();
            }
        }
    }

    //Setter inn kjøretøy
    private void insertVehicles(List<Vehicle> vehicles) throws SQLException {
        for (Vehicle vehicle : vehicles) {
            if (vehicle instanceof FossilCar) {
                insertFossilCar((FossilCar) vehicle);
            } else if (vehicle instanceof ElectricCar) {
                insertElectricCar((ElectricCar) vehicle);
            } else if (vehicle instanceof Motorcycle) {
                insertMotorcycle((Motorcycle) vehicle);
            }
        }
    }

    //Setter inn fossilbil
    private void insertFossilCar(FossilCar car) throws SQLException {
        String sql = "INSERT INTO FossilCar (VehicleID, Brand, Model, YearModel, RegistrationNumber, ChassisNumber, Driveable, NumberOfSellableWheels, ScrapyardID, FuelType, FuelAmount) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, car.getId());
            pstmt.setString(2, car.getBrand());
            pstmt.setString(3, car.getModel());
            pstmt.setInt(4, car.getYearModel());
            pstmt.setString(5, car.getRegistrationNumber());
            pstmt.setString(6, car.getChassisNumber());
            pstmt.setBoolean(7, car.isDriveable());
            pstmt.setInt(8, car.getNumberOfSellableWheels());
            pstmt.setInt(9, car.getScrapyardId());
            pstmt.setString(10, car.getFuelType());
            pstmt.setInt(11, car.getFuelAmount());
            pstmt.executeUpdate();
        }
    }

    //Setter inn elbil
    private void insertElectricCar(ElectricCar car) throws SQLException {
        String sql = "INSERT INTO ElectricCar (VehicleID, Brand, Model, YearModel, RegistrationNumber, ChassisNumber, Driveable, NumberOfSellableWheels, ScrapyardID, BatteryCapacity, ChargeLevel) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, car.getId());
            pstmt.setString(2, car.getBrand());
            pstmt.setString(3, car.getModel());
            pstmt.setInt(4, car.getYearModel());
            pstmt.setString(5, car.getRegistrationNumber());
            pstmt.setString(6, car.getChassisNumber());
            pstmt.setBoolean(7, car.isDriveable());
            pstmt.setInt(8, car.getNumberOfSellableWheels());
            pstmt.setInt(9, car.getScrapyardId());
            pstmt.setInt(10, car.getBatteryCapacity());
            pstmt.setInt(11, car.getChargeLevel());
            pstmt.executeUpdate();
        }
    }

    //Setter inn motorsykkel
    private void insertMotorcycle(Motorcycle bike) throws SQLException {
        String sql = "INSERT INTO Motorcycle (VehicleID, Brand, Model, YearModel, RegistrationNumber, ChassisNumber, Driveable, NumberOfSellableWheels, ScrapyardID, HasSidecar, EngineCapacity, IsModified, NumberOfWheels) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bike.getId());
            pstmt.setString(2, bike.getBrand());
            pstmt.setString(3, bike.getModel());
            pstmt.setInt(4, bike.getYearModel());
            pstmt.setString(5, bike.getRegistrationNumber());
            pstmt.setString(6, bike.getChassisNumber());
            pstmt.setBoolean(7, bike.isDriveable());
            pstmt.setInt(8, bike.getNumberOfSellableWheels());
            pstmt.setInt(9, bike.getScrapyardId());
            pstmt.setBoolean(10, bike.hasSidecar());
            pstmt.setInt(11, bike.getEngineCapacity());
            pstmt.setBoolean(12, bike.isModified());
            pstmt.setInt(13, bike.getNumberOfWheels());
            pstmt.executeUpdate();
        }
    }

    //Henter alle skraphandlere
    public List<Scrapyard> getAllScrapyards() throws SQLException {
        List<Scrapyard> scrapyards = new ArrayList<>();
        String sql = "SELECT * FROM Scrapyard";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                int id = rs.getInt("ScrapyardID");
                String navn = rs.getString("Name");
                String adresse = rs.getString("Address");
                String telefon = rs.getString("PhoneNumber");
                scrapyards.add(new Scrapyard(id, navn, adresse, telefon));
            }
        }
        return scrapyards;
    }

    //Henter alle kjøretøy
    public List<Vehicle> getAllVehicles() throws SQLException {
        List<Vehicle> vehicles = new ArrayList<>();
        
        //Henter fossilbiler
        String sql = "SELECT * FROM FossilCar";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                vehicles.add(new FossilCar(
                    rs.getInt("VehicleID"), 
                    rs.getString("Brand"), 
                    rs.getString("Model"),
                    rs.getInt("YearModel"), 
                    rs.getString("RegistrationNumber"), 
                    rs.getString("ChassisNumber"),
                    rs.getBoolean("Driveable"), 
                    rs.getInt("NumberOfSellableWheels"), 
                    rs.getInt("ScrapyardID"),
                    rs.getString("FuelType"), 
                    rs.getInt("FuelAmount")
                ));
            }
        }

        //Henter elbiler
        sql = "SELECT * FROM ElectricCar";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                vehicles.add(new ElectricCar(
                    rs.getInt("VehicleID"), 
                    rs.getString("Brand"), 
                    rs.getString("Model"),
                    rs.getInt("YearModel"), 
                    rs.getString("RegistrationNumber"), 
                    rs.getString("ChassisNumber"),
                    rs.getBoolean("Driveable"), 
                    rs.getInt("NumberOfSellableWheels"), 
                    rs.getInt("ScrapyardID"),
                    rs.getInt("BatteryCapacity"), 
                    rs.getInt("ChargeLevel")
                ));
            }
        }

        //Henter motorsykler
        sql = "SELECT * FROM Motorcycle";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                vehicles.add(new Motorcycle(
                    rs.getInt("VehicleID"), 
                    rs.getString("Brand"), 
                    rs.getString("Model"),
                    rs.getInt("YearModel"), 
                    rs.getString("RegistrationNumber"), 
                    rs.getString("ChassisNumber"),
                    rs.getBoolean("Driveable"), 
                    rs.getInt("NumberOfSellableWheels"), 
                    rs.getInt("ScrapyardID"),
                    rs.getBoolean("HasSidecar"), 
                    rs.getInt("EngineCapacity"), 
                    rs.getBoolean("IsModified"),
                    rs.getInt("NumberOfWheels")
                ));
            }
        }
        
        return vehicles;
    }

    //Regner total drivstoff
    public int getTotalFuelAmount() throws SQLException {
        String sql = "SELECT SUM(FuelAmount) AS total FROM FossilCar";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;
        }
    }

    //Henter kjørbare kjøretøy
    public List<Vehicle> getDriveableVehicles() throws SQLException {
        List<Vehicle> vehicles = new ArrayList<>();
        
        //Kjørbare fossilbiler
        String sql = "SELECT * FROM FossilCar WHERE Driveable = TRUE";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                vehicles.add(new FossilCar(
                    rs.getInt("VehicleID"), rs.getString("Brand"), rs.getString("Model"),
                    rs.getInt("YearModel"), rs.getString("RegistrationNumber"), rs.getString("ChassisNumber"),
                    rs.getBoolean("Driveable"), rs.getInt("NumberOfSellableWheels"), rs.getInt("ScrapyardID"),
                    rs.getString("FuelType"), rs.getInt("FuelAmount")
                ));
            }
        }

        //Kjørbare elbiler
        sql = "SELECT * FROM ElectricCar WHERE Driveable = TRUE";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                vehicles.add(new ElectricCar(
                    rs.getInt("VehicleID"), rs.getString("Brand"), rs.getString("Model"),
                    rs.getInt("YearModel"), rs.getString("RegistrationNumber"), rs.getString("ChassisNumber"),
                    rs.getBoolean("Driveable"), rs.getInt("NumberOfSellableWheels"), rs.getInt("ScrapyardID"),
                    rs.getInt("BatteryCapacity"), rs.getInt("ChargeLevel")
                ));
            }
        }

        //Kjørbare motorsykler
        sql = "SELECT * FROM Motorcycle WHERE Driveable = TRUE";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                vehicles.add(new Motorcycle(
                    rs.getInt("VehicleID"), rs.getString("Brand"), rs.getString("Model"),
                    rs.getInt("YearModel"), rs.getString("RegistrationNumber"), rs.getString("ChassisNumber"),
                    rs.getBoolean("Driveable"), rs.getInt("NumberOfSellableWheels"), rs.getInt("ScrapyardID"),
                    rs.getBoolean("HasSidecar"), rs.getInt("EngineCapacity"), rs.getBoolean("IsModified"),
                    rs.getInt("NumberOfWheels")
                ));
            }
        }
        
        return vehicles;
    }

    //Finner mest brukte skraphandler
    public String getMostUsedScrapyard() throws SQLException {
        Map<Integer, Integer> tellere = new HashMap<>();
        Map<Integer, String> navn = new HashMap<>();

        //Henter skraphandlernavn
        String sql = "SELECT ScrapyardID, Name FROM Scrapyard";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                navn.put(rs.getInt("ScrapyardID"), rs.getString("Name"));
            }
        }

        //Teller fossilbiler
        sql = "SELECT ScrapyardID, COUNT(*) AS antall FROM FossilCar GROUP BY ScrapyardID";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                int id = rs.getInt("ScrapyardID");
                int antall = rs.getInt("antall");
                tellere.put(id, tellere.getOrDefault(id, 0) + antall);
            }
        }

        //Teller elbiler
        sql = "SELECT ScrapyardID, COUNT(*) AS antall FROM ElectricCar GROUP BY ScrapyardID";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                int id = rs.getInt("ScrapyardID");
                int antall = rs.getInt("antall");
                tellere.put(id, tellere.getOrDefault(id, 0) + antall);
            }
        }

        //Teller motorsykler
        sql = "SELECT ScrapyardID, COUNT(*) AS antall FROM Motorcycle GROUP BY ScrapyardID";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                int id = rs.getInt("ScrapyardID");
                int antall = rs.getInt("antall");
                tellere.put(id, tellere.getOrDefault(id, 0) + antall);
            }
        }

        //Finner største
        int maxAntall = 0;
        String resultat = "Ingen skraphandlere funnet";
        
        for (Map.Entry<Integer, Integer> entry : tellere.entrySet()) {
            if (entry.getValue() > maxAntall) {
                maxAntall = entry.getValue();
                String handlerNavn = navn.get(entry.getKey());
                if (handlerNavn != null) {
                    resultat = String.format("Skraphandler: %s, Antall kjøretøy: %d", handlerNavn, maxAntall);
                }
            }
        }

        return resultat;
    }
}