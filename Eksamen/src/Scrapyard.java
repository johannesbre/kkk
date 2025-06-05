//Klasse for skraphandler
public class Scrapyard {
    
    private int id;
    private String name;
    private String address;
    private String phoneNumber;

    //Konstruktør for Scrapyard
    public Scrapyard(int id, String name, String address, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
    }

    // Gettere og settere
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override //Får frem detaljene om Scrapyard
    public String toString() {
        return String.format("ID: %d, Name: %s, Address: %s, Phone: %s", id, name, address, phoneNumber);
    }
}