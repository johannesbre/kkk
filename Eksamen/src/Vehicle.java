/*Abstrakt superklasse for kjørtøy*/
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

    //Felles antributter for kjørtøy
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
    public int id() {
        return id; }

    public String brand() {
        return brand; }

    public String model() {
        return model; }

    public int yearModel() {
        return yearModel; }

    public String registrationNumber() {
        return registrationNumber; }

    public String chassisNumber() {
        return chassisNumber; }

    public boolean driveable() {
        return driveable; }

    public int numberOfSellableWheels() {
        return numberOfSellableWheels; }

    public int scrapyardId() {
        return scrapyardId; }


    public abstract String getVehicleType();

    //felles detaljer om alle kjøretøyt
    @Override
    public String toString() {
        return String.format("ID: %d, Brand: %s, Model: %s, Year: %d, Reg: %s, Drivable: %b",
                id, brand, model, yearModel, registrationNumber, driveable);
    }
}
