//Klasse for fossilbil, som arver fra Vehicle:
public class FossilCar extends Vehicle {

 private final String fuelType;

 private final int fuelAmount;

    public FossilCar(int id, String brand, String model, int yearModel, String registrationNumber,
       String chassisNumber, boolean driveable, int numberOfSellableWheels, int scrapyardId,
       String fuelType, int fuelAmount) {

        super(id, brand, model, yearModel, registrationNumber, chassisNumber, driveable, numberOfSellableWheels, scrapyardId);
        this.fuelType = fuelType;
        this.fuelAmount = fuelAmount;
    }

    public String fuelType() {
        return fuelType; }

    public int fuelAmount() {
        return fuelAmount; }

    @Override
    public String getVehicleType() {
        return "FossilCar";
    }
  //Detaljer om fossilbilen i en streng
    @Override
    public String toString() {
        return super.toString() + String.format(", Fuel: %s, Amount: %dL", fuelType, fuelAmount);
    }
}