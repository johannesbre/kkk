/*Abstrakt superklasse for kjøretøy*/
public abstract class Vehicle {

    private final int id;
    private final String brand;
    private final String model;
    private final int yearModel;
    private final String registrationNumber;
    private final String chassisNumber;
    private final boolean driveable;
    private final int numberOfSellableWheels;
    private final int scrapyardId;

    //Felles attributter for kjøretøy
    public Vehicle(int id, String brand, String model, int yearModel, String registrationNumber,
       String chassisNumber, boolean driveable, int numberOfSellableWheels, int scrapyardId) {

        this.id = id;
        this.brand = brand;
        this.model = model;
        this.yearModel = yearModel;
        this.registrationNumber = registrationNumber;
        this.chassisNumber = chassisNumber;
        this.driveable = driveable;
        this.numberOfSellableWheels = numberOfSellableWheels;
        this.scrapyardId = scrapyardId;
    }

    // Gettere for attributtene
    public int getId() {
        return id; 
    }

    public String getBrand() {
        return brand; 
    }

    public String getModel() {
        return model; 
    }

    public int getYearModel() {
        return yearModel; 
    }

    public String getRegistrationNumber() {
        return registrationNumber; 
    }

    public String getChassisNumber() {
        return chassisNumber; 
    }

    public boolean isDriveable() {
        return driveable; 
    }

    public int getNumberOfSellableWheels() {
        return numberOfSellableWheels; 
    }

    public int getScrapyardId() {
        return scrapyardId; 
    }

    public abstract String getVehicleType();

    //Felles detaljer om alle kjøretøy
    @Override
    public String toString() {
        return String.format("ID: %d, Brand: %s, Model: %s, Year: %d, Reg: %s, Drivable: %b",
                id, brand, model, yearModel, registrationNumber, driveable);
    }
}