package TransportModule.BusinessLayer;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;

public class Transport {
    private final int id;
    private Site source;
    private LinkedList<Site> destinations;
    private HashMap<Site,ItemList> itemLists;
    private int truckId;
    private int driverId;
    private LocalDateTime scheduledTime;
    private int weight;

    public Transport(int id, Site source, LinkedList<Site> destinations, HashMap<Site,ItemList> itemLists,
                     int truckId, int driverId, LocalDateTime scheduledTime/*, int weight*/){
        this.id = id;
        this.source = source;
        this.destinations = destinations;
        this.itemLists = itemLists;
        this.truckId = truckId;
        this.driverId = driverId;
        this.scheduledTime = scheduledTime;
//        this.weight = weight;
    }

    public int getId() {
        return id;
    }

    public Site getSource() {
        return source;
    }

    public LinkedList<Site> getDestinations() {
        return destinations;
    }

    public HashMap<Site, ItemList> getItemLists() {
        return itemLists;
    }

    public int getTruckId() {
        return truckId;
    }

    public int getDriverId() {
        return driverId;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public int getWeight() {
        return weight;
    }

    public void setSource(Site source) {
        this.source = source;
    }

    public void setDestinations(LinkedList<Site> destinations) {
        this.destinations = destinations;
    }

    public void setItemLists(HashMap<Site, ItemList> itemLists) {
        this.itemLists = itemLists;
    }

    public void setTruckId(int truckId) {
        this.truckId = truckId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
