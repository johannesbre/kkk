//Her komemrn record for skraphandler:
public record Scrapyard(int id, String name, String address, String phoneNumber) {

    @Override //Får frem detaljene om Scrapyard
    public String toString() {

        return String.format("ID: %d, Name: %s, Address: %s, Phone: %s", id, name, address, phoneNumber);
    }
}
