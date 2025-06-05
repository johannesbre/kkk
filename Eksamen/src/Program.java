import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Program {
    private final ScrapyardService scrapyardService; //håndterer databasen

    public Program() throws SQLException, IOException {
        scrapyardService = new ScrapyardService();
    }

    //Kjører programmet med meny
    public void run() {
        System.out.println("Velkommen til Skraphandleren!");
        Scanner userInput = new Scanner(System.in);
        while (true) {
            presentMainMenu(); //det som faktisk får menyen til å vise
            try {
                if (!userInput.hasNextInt()) {
                    System.out.println("skriv inn et av tallene.");
                    userInput.nextLine();
                    continue;
                }
                int choice = userInput.nextInt();
                userInput.nextLine();
                switch (choice) {
                    case 1 -> importData();
                    case 2 -> presentAllScrapyards();
                    case 3 -> presentAllVehicles();
                    case 4 -> presentTotalFuel();
                    case 5 -> presentDriveableVehicles();
                    case 6 -> presentMostUsedScrapyard();
                    case 7 -> {
                        quit();
                        return;
                    }
                    default -> System.out.println("Feil valg, prøv igjen.");
                }
            } catch (Exception e) {
                System.out.println("En feil funnet: " + e.getMessage());
                userInput.nextLine();
            }
        }
    }

    //Håndterer data fra fil til database
    private void importData() {
        try {
            scrapyardService.importDataFromFile("vehicles.txt");
            System.out.println("Data importert til databasen!");
        } catch (SQLException | IOException e) {
            System.out.println("Kunne ikke importere data?");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    //Får frem alle Scrapyards
    private void presentAllScrapyards() {
        try {
            List<Scrapyard> scrapyards = scrapyardService.getAllScrapyards();
            System.out.printf("Fant %d skraphandlere:%n", scrapyards.size());
            for (Scrapyard scrapyard : scrapyards) {
                System.out.println(scrapyard);
            }
        } catch (SQLException | IOException e) {
            System.out.println("Klarte ikke finne noen skraphandlere");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    //Får frem all kjøretøy
    private void presentAllVehicles() {
        try {
            List<Vehicle> vehicles = scrapyardService.getAllVehicles();
            System.out.printf("Fant %d kjøretøy:%n", vehicles.size());
            for (Vehicle vehicle : vehicles) {
                System.out.println(vehicle);
            }
        } catch (SQLException | IOException e) {
            System.out.println("Klarte ikke finne kjøretøy");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    //Viser total mengde drivstoff i fossilbilene
    private void presentTotalFuel() {
        try {
            int totalFuel = scrapyardService.getTotalFuelAmount();
            System.out.printf("Total drivstoffmengde i fossilbiler: %d liter%n", totalFuel);
        } catch (SQLException | IOException e) {
            System.out.println("Klarte ikke finne drivstoffmengde");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    //Viser alt av kjørbart kjøretøy
    private void presentDriveableVehicles() {
        try {
            List<Vehicle> driveableVehicles = scrapyardService.getDriveableVehicles();
            System.out.printf("Fant %d kjørbare kjøretøy:%n", driveableVehicles.size());
            for (Vehicle vehicle : driveableVehicles) {
                System.out.println(vehicle);
            }
        } catch (SQLException | IOException e) {
            System.out.println("Klarte ikke hente kjøretøy");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    //Viser den skraphandleren som er mest brukt
    private void presentMostUsedScrapyard() {
        try {
            String mostUsedScrapyard = scrapyardService.getMostUsedScrapyard();
            System.out.println("Mest brukte skraphandler:");
            System.out.println(mostUsedScrapyard);
        } catch (SQLException | IOException e) {
            System.out.println("Klarte ikke finne den mest brukte skraphandler");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    //Programmet avsluttes:
    private void quit() {
        System.out.println("Snakkes!");
    }

    //Får frem hovedmenyen:
    private void presentMainMenu() {
        System.out.println("Du har følgende valg:");
        System.out.println("1: Importer data fra fil");
        System.out.println("2: Vis alle skraphandlere");
        System.out.println("3: Vis alle kjøretøy");
        System.out.println("4: Vis total drivstoffmengde i fossilbiler");
        System.out.println("5: Vis alle kjørbare kjøretøy");
        System.out.println("6: Vis mest brukte skraphandler");
        System.out.println("7: Ferdig");
    }
}