//Klasse for Motorsykkel som arver fra Vehicle
public class Motorcycle extends Vehicle {
    private final boolean hasSidecar;
    private final int engineCapacity;
    private final boolean isModified;
    private final int numberOfWheels;

    //Kontruktør!!
    public Motorcycle(int id, String brand, String model, int yearModel, String registrationNumber,
     String chassisNumber, boolean driveable, int numberOfSellableWheels, int scrapyardId,
      boolean hasSidecar, int engineCapacity, boolean isModified, int numberOfWheels) {

        super(id, brand, model, yearModel, registrationNumber, chassisNumber, driveable, numberOfSellableWheels, scrapyardId);
        this.hasSidecar = hasSidecar;
        this.engineCapacity = engineCapacity;
        this.isModified = isModified;
        this.numberOfWheels = numberOfWheels;
    }

    public boolean hasSidecar() {
        return hasSidecar; }

    public int engineCapacity() {
        return engineCapacity; }

    public boolean isModified() {
        return isModified; }

    public int numberOfWheels() {
        return numberOfWheels; }

    @Override
    public String getVehicleType() {
        return "Motorcycle"; //Returnerer at det er en motorsykkel
    }

    //Legger detaljer om motorsykkel i en streng:
    @Override
    public String toString() {
        return super.toString() + String.format(", Sidecar: %b, Engine: %dcc, Modified: %b, Wheels: %d",
                hasSidecar, engineCapacity, isModified, numberOfWheels);
    }
}