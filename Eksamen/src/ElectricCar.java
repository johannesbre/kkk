//Klasse for Elbilen, som arver fra Vehicle:
public class ElectricCar extends Vehicle {

    private final int batteryCapacity;
    private final int chargeLevel;

    //Elbilens konstruktør
    public ElectricCar(int id, String brand, String model, int yearModel, String registrationNumber,
      String chassisNumber, boolean driveable, int numberOfSellableWheels, int scrapyardId,
       int batteryCapacity, int chargeLevel) {

        super(id, brand, model, yearModel, registrationNumber, chassisNumber, driveable, numberOfSellableWheels, scrapyardId);
        this.batteryCapacity = batteryCapacity;
        this.chargeLevel = chargeLevel;
    }

    public int getBatteryCapacity() {
        return batteryCapacity; 
    }

    public int getChargeLevel() {
        return chargeLevel; 
    }

    @Override
    public String getVehicleType() {
        return "ElectricCar";
    }

    //Informasjon om elbilen i en streng
    @Override
    public String toString() {
        return super.toString() + String.format(", Battery: %dkWh, Charge: %d%%", batteryCapacity, chargeLevel);
    }
}