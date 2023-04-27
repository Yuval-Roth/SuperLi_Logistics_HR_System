package PresentationLayer.transportModule;

import ServiceLayer.transportModule.TransportsService;
import objects.transportObjects.Site;
import objects.transportObjects.Transport;
import utils.JsonUtils;
import utils.Response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.LinkedList;

public class TransportsManagement {

    private final TransportAppData transportAppData;
    private final TransportsService ts;

    public TransportsManagement(TransportAppData transportAppData, TransportsService ts) {
        this.transportAppData = transportAppData;
        this.ts = ts;
    }

    void manageTransports() {
        while(true){
            System.out.println("=========================================");
            System.out.println("Transports management");
            System.out.println("Please select an option:");
            System.out.println("1. Create new transport");
            System.out.println("2. Update existing transport");
            System.out.println("3. Delete transport");
            System.out.println("4. View full transport information");
            System.out.println("5. View all transports");
            System.out.println("6. Return to previous menu");
            int option = transportAppData.readInt();
            switch (option) {
                case 1 -> createTransport();
                case 2 -> updateTransport();
                case 3 -> removeTransport();
                case 4 -> viewTransport();
                case 5 -> viewAllTransports();
                case 6 -> {
                    return;
                }
                default -> System.out.println("\nInvalid option!");
            }
        }
    }

    private void createTransport() {

        if (verifyDataAvailability()) {
            return;
        }

        System.out.println("=========================================");
        System.out.println("Enter transport details:");

        // date/time
        LocalDate departureDate = LocalDate.parse(transportAppData.readLine("Departure date (format: yyyy-mm-dd): "));
        LocalTime departureTime = LocalTime.parse(transportAppData.readLine("Departure time (format: hh:mm): "));
        LocalDateTime departureDateTime = LocalDateTime.of(departureDate, departureTime);

        // truck
        System.out.println("Truck: ");
        String truckId = transportAppData.pickTruck(false).id();

        // driver
        System.out.println("Driver: ");
        String driverID = transportAppData.pickDriver(false).id();

        // source
        System.out.println("Source: ");
        Site source = transportAppData.pickSite(false);

        // destinations and items lists
        LinkedList<String> destinations = new LinkedList<>();
        HashMap<String, Integer> itemsList = new HashMap<>();
        destinationsMaker(destinations, itemsList);

        //weight
        int truckWeight = transportAppData.readInt("Truck weight: ");

        Transport newTransport = new Transport(
                source.address(),
                destinations,
                itemsList,
                truckId,
                driverID,
                departureDateTime,
                truckWeight
        );

        Response response = createTransportHelperMethod(newTransport);

        if(response.success() == false){
            pickAnOptionIfFail(newTransport);
        }
    }

    private void pickAnOptionIfFail(Transport newTransport) {
        while(true){
            System.out.println("current weight: "+newTransport.weight());
            System.out.println("Select one of the following options:");
            System.out.println("1. Pick new truck and driver");
            System.out.println("2. Pick new destinations and item lists");
            System.out.println("3. Cancel and return to previous menu");
            int option = transportAppData.readInt();
            switch (option) {
                case 1 -> {
                    System.out.println("Truck: ");
                    String truckId = transportAppData.pickTruck(false).id();
                    System.out.println("Driver: ");
                    String driverID = transportAppData.pickDriver(false).id();
                    newTransport = new Transport(
                            newTransport.source(),
                            newTransport.destinations(),
                            newTransport.itemLists(),
                            truckId,
                            driverID,
                            newTransport.scheduledTime(),
                            newTransport.weight()
                    );
                    Response response2 = createTransportHelperMethod(newTransport);
                    if(response2.success()) {
                        return;
                    }
                }
                case 2 -> {
                    LinkedList<String> destinations = new LinkedList<>();
                    HashMap<String, Integer> itemsList = new HashMap<>();
                    destinationsMaker(destinations, itemsList);
                    System.out.println("New weight :");
                    int weight = transportAppData.readInt();
                    newTransport = new Transport(
                            newTransport.source(),
                            destinations,
                            itemsList,
                            newTransport.truckId(),
                            newTransport.driverId(),
                            newTransport.scheduledTime(),
                            weight
                    );
                    Response response2 = createTransportHelperMethod(newTransport);
                    if(response2.success()) {
                        return;
                    }
                }
                case 3 -> {
                    System.out.println("Transport creation cancelled");
                    return;
                }
                default -> System.out.println("\nInvalid option!");
            }
        }
    }

    private void updateTransport() {
        while(true) {
            System.out.println("=========================================");
            System.out.println("Select transport to update:");
            Transport transport = getTransport();
            if(transport == null) {
                return;
            }
            printTransportDetails(transport);
            System.out.println("=========================================");
            System.out.println("Please select an option:");
            System.out.println("1. Update date");
            System.out.println("2. Update time");
            System.out.println("3. Update driver");
            System.out.println("4. Update truck");
            System.out.println("5. Update source");
            System.out.println("6. Update destinations");
            System.out.println("7. Update weight");
            System.out.println("8. Return to previous menu");
            int option = transportAppData.readInt();
            switch (option) {
                case 1 -> {
                    LocalDate departureDate = LocalDate.parse(transportAppData.readLine("Departure date (format: yyyy-mm-dd): "));
                    LocalDateTime departureDateTime = LocalDateTime.of(departureDate, transport.scheduledTime().toLocalTime());
                    updateTransportHelperMethod(
                            transport.id(),
                            transport.source(),
                            transport.destinations(),
                            transport.itemLists(),
                            transport.truckId(),
                            transport.driverId(),
                            departureDateTime,
                            transport.weight()
                    );
                }
                case 2 -> {
                    LocalTime departureTime = LocalTime.parse(transportAppData.readLine("Departure time (format: hh:mm): "));
                    LocalDateTime departureDateTime = LocalDateTime.of(transport.scheduledTime().toLocalDate(), departureTime);
                    updateTransportHelperMethod(
                            transport.id(),
                            transport.source(),
                            transport.destinations(),
                            transport.itemLists(),
                            transport.truckId(),
                            transport.driverId(),
                            departureDateTime,
                            transport.weight()
                    );
                }
                case 3 -> {
                    System.out.println("Select driver: ");
                    String driverID = transportAppData.pickDriver(false).id();
                    updateTransportHelperMethod(
                            transport.id(),
                            transport.source(),
                            transport.destinations(),
                            transport.itemLists(),
                            transport.truckId(),
                            driverID,
                            transport.scheduledTime(),
                            transport.weight()
                    );
                }
                case 4 -> {
                    System.out.println("Select truck: ");
                    String truckId = transportAppData.pickTruck(false).id();
                    updateTransportHelperMethod(
                            transport.id(),
                            transport.source(),
                            transport.destinations(),
                            transport.itemLists(),
                            truckId,
                            transport.driverId(),
                            transport.scheduledTime(),
                            transport.weight()
                    );
                }
                case 5 -> {
                    System.out.println("Select source: ");
                    Site source = transportAppData.pickSite(false);
                    updateTransportHelperMethod(
                            transport.id(),
                            source.address(),
                            transport.destinations(),
                            transport.itemLists(),
                            transport.truckId(),
                            transport.driverId(),
                            transport.scheduledTime(),
                            transport.weight()
                    );
                }
                case 6 ->{
                    System.out.println("Select new destinations and item lists: ");
                    HashMap<String, Integer> itemsList = new HashMap<>();
                    LinkedList<String> destinations = new LinkedList<>();
                    destinationsMaker(destinations, itemsList);
                    updateTransportHelperMethod(
                            transport.id(),
                            transport.source(),
                            destinations,
                            itemsList,
                            transport.truckId(),
                            transport.driverId(),
                            transport.scheduledTime(),
                            transport.weight()
                    );
                }
                case 7 -> {
                    int truckWeight = transportAppData.readInt("Truck weight: ");
                    updateTransportHelperMethod(
                            transport.id(),
                            transport.source(),
                            transport.destinations(),
                            transport.itemLists(),
                            transport.truckId(),
                            transport.driverId(),
                            transport.scheduledTime(),
                            truckWeight
                    );
                }
                case 8 -> {
                    return;
                }
                default -> {
                    System.out.println("\nInvalid option!");
                    continue;
                }
            }
            break;
        }
    }

    private void removeTransport() {
        while(true) {
            System.out.println("=========================================");
            Transport transport = getTransport();
            if(transport == null) {
                return;
            }
            printTransportDetails(transport);
            System.out.println("=========================================");
            System.out.println("Are you sure you want to delete this transport? (y/n)");
            String option = transportAppData.readLine();
            switch (option) {
                case "y" -> {
                    String json = transport.toJson();
                    String responseJson = ts.removeTransport(json);
                    Response response = JsonUtils.deserialize(responseJson, Response.class);
                    if (response.success()) {
                        transportAppData.transports().remove(transport.id());
                    }
                    System.out.println("\nTransport deleted successfully!");
                }
                case "n"-> {}
                default -> System.out.println("\nInvalid option!");
            }
        }
    }

    private void viewTransport() {
        System.out.println("=========================================");
        Transport transport = getTransport();
        if(transport == null) {
            return;
        }
        printTransportDetails(transport);
        System.out.println("\nEnter 'done!' to return to previous menu");
    }

    private void viewAllTransports() {
        System.out.println("=========================================");
        System.out.println("All transports:");
        for(Transport transport : transportAppData.transports().values()){
            printTransportDetails(transport);
            System.out.println("-----------------------------------------");
        }
        System.out.println("\nEnter 'done!' to return to previous menu");
        transportAppData.readLine();
    }

    private void printTransportDetails(Transport transport) {
        System.out.println("Transport id: "+ transport.id());
        System.out.println("Date:         "+ transport.scheduledTime().toLocalDate());
        System.out.println("Time:         "+ transport.scheduledTime().toLocalTime());
        System.out.println("DriverId:     "+ transport.driverId());
        System.out.println("TruckId:      "+ transport.truckId());
        System.out.println("Weight:       "+ transport.weight());
        System.out.println("Source:       "+ transport.source());
        System.out.println("Destinations: ");
        for(String address : transport.destinations()){
            Site destination = transportAppData.sites().get(address);
            System.out.println("   "+ destination + " (items list id: "+ transport.itemLists().get(address)+")");
        }
    }

    private void updateTransportHelperMethod(int id, String source, LinkedList<String> destinations, HashMap<String, Integer> itemLists, String truckId, String driverId, LocalDateTime departureDateTime, int weight) {
        Transport newTransport = new Transport(
                id,
                source,
                destinations,
                itemLists,
                truckId,
                driverId,
                departureDateTime,
                weight
        );

        String json = newTransport.toJson();
        String responseJson = ts.updateTransport(json);
        Response response = JsonUtils.deserialize(responseJson, Response.class);
        if(response.success()){
            transportAppData.transports().put(id, newTransport);
        }
        System.out.println("\n"+response.message());
    }

    private Transport getTransport() {
        int transportId = transportAppData.readInt("Enter transport ID (enter '-1' to return to previous menu): ");
        if(transportId == -1) {
            return null;
        }
        if(transportAppData.transports().containsKey(transportId) == false) {
            System.out.println("Transport with ID "+transportId+" does not exist!");
            return null;
        }
        return transportAppData.transports().get(transportId);
    }

    private void destinationsMaker(LinkedList<String> destinations, HashMap<String, Integer> itemsList) {
        int destinationId = 1;
        System.out.println("Pick destinations and items lists:");
        while(true){
            System.out.println("Destination number "+destinationId+": ");
            Site site = transportAppData.pickSite(true);
            if(site == null) {
                break;
            }
            int listId = transportAppData.readInt("Items list id: ");
            if (transportAppData.itemLists().containsKey(listId) == false) {
                System.out.println("\nItems list with ID " + listId + " does not exist!");
                System.out.println();
                continue;
            }
            itemsList.put(site.address(), listId);
            destinations.add(site.address());
            destinationId++;
        }
    }

    private Response createTransportHelperMethod(Transport newTransport) {

        // send to server
        String json = newTransport.toJson();
        String responseJson = ts.addTransport(json);
        Response response = JsonUtils.deserialize(responseJson, Response.class);
        System.out.println("\n"+response.message());

        if(response.success()){
            int id = response.dataToInt();
            newTransport = newTransport.newId(id);
            transportAppData.transports().put(id, newTransport);
        }
        return response;
    }

    private boolean verifyDataAvailability() {

        boolean isMissingData = false;
        StringBuilder errorMessage = new StringBuilder("\nData not found for ");

        if(transportAppData.trucks().isEmpty()){
            errorMessage.append("trucks");
            isMissingData = true;
        }
        if(transportAppData.drivers().isEmpty()){
            if(isMissingData) {
                errorMessage.append(", ");
            }
            errorMessage.append("drivers");
            isMissingData = true;
        }
        if(transportAppData.sites().isEmpty()){
            if(isMissingData) {
                errorMessage.append(", ");
            }
            errorMessage.append("sites");
            isMissingData = true;
        }
        if(transportAppData.itemLists().isEmpty()){
            if(isMissingData) {
                errorMessage.append(", ");
            }
            errorMessage.append("item lists");
            isMissingData = true;
        }

        if(isMissingData) {
            errorMessage.append("\nAborting.....\n");
            System.out.println(errorMessage);
        }
        return isMissingData;
    }
}