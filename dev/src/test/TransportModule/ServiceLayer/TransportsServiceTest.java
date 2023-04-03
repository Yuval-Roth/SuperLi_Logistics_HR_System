package TransportModule.ServiceLayer;

import TransportModule.BusinessLayer.Records.*;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransportsServiceTest {

    Transport transport = null;

    @BeforeEach
    void setUp() {
        Site site1 = new Site("zone a", "123 main st", "(555) 123-4567", "john smith", Site.SiteType.BRANCH);
        Site site2 = new Site("zone b", "456 oak ave", "(555) 234-5678", "jane doe", Site.SiteType.LOGISTICAL_CENTER);
        Driver driver1 = new Driver(1234, "megan smith", "class b");
        Truck truck1 = new Truck("abc123", "ford", 1500, 10000);
        HashMap<String, Integer> load1 = new HashMap<>();
        load1.put("shirts", 20);
        load1.put("pants", 15);
        load1.put("socks", 30);
        HashMap<String, Integer> unload1 = new HashMap<>();
        unload1.put("jackets", 10);
        unload1.put("hats", 5);
        unload1.put("gloves", 20);
        ItemList itemList1 = new ItemList(1001, load1, unload1);

        ItemListsService ils = ModuleFactory.getInstance().getItemListsService();
        ils.addItemList(JSON.serialize(itemList1));

        ResourceManagementService rms = ModuleFactory.getInstance().getResourceManagementService();
        rms.addDriver(JSON.serialize(driver1));
        rms.addTruck(JSON.serialize(truck1));
        rms.addSite(JSON.serialize(site1));
        rms.addSite(JSON.serialize(site2));

        HashMap<Site,ItemList> hm = new HashMap<>();
        hm.put(site2, itemList1);

        TransportsService ts = ModuleFactory.getInstance().getTransportsService();
        transport = new Transport(
                1,
                site1,
                new LinkedList<>(List.of(site2)),
                hm,
                truck1.id(),
                driver1.id(),
                LocalDateTime.of(2020, 1, 1, 0, 0),
                100
        );
        ts.createTransport(JSON.serialize(transport));
    }

    @AfterEach
    void tearDown() {
        transport = null;
    }

    @Test
    void createTransport() {
        HashMap<String, Integer> load1 = new HashMap<>();
        load1.put("shirts", 20);
        load1.put("pants", 15);
        load1.put("socks", 30);
        HashMap<String, Integer> unload1 = new HashMap<>();
        unload1.put("jackets", 10);
        unload1.put("hats", 5);
        unload1.put("gloves", 20);

        ItemList itemList = new ItemList(1001, load1, unload1);

        HashMap<Site,ItemList> hm = new HashMap<>();

        Site source = new Site("zone b", "456 oak ave", "(555) 234-5678", "jane doe", Site.SiteType.LOGISTICAL_CENTER);
        Site destination = new Site("zone a", "123 main st", "(555) 123-4567", "john smith", Site.SiteType.BRANCH);
        hm.put(destination, itemList);
        LinkedList<Site> destinations = new LinkedList<>(List.of(destination));
        TransportsService ts = ModuleFactory.getInstance().getTransportsService();
        Transport newTransport = new Transport(
                50,
                source,
                destinations,
                hm,
                "abc123",
                1234,
                LocalDateTime.of(2020, 1, 1, 0, 0),
                2000
        );
        String json = ts.createTransport(JSON.serialize(newTransport));
        Response<String> response = JSON.deserialize(json, Response.class);
        assertTrue(response.isSuccess());
        String updatedJson = ModuleFactory.getInstance()
                .getTransportsService()
                .getTransport(
                        JSON.serialize(Transport.getLookupObject(newTransport.id()))
                );
        Type type = new TypeToken<Response<Transport>>(){}.getType();
        Response<Transport> updatedResponse = JSON.deserialize(updatedJson, type);
        Transport updatedTransport = updatedResponse.getData();
        assertEquals(newTransport.id(), updatedTransport.id());
        assertEquals(newTransport.source(), updatedTransport.source());
        assertEquals(newTransport.destinations(), updatedTransport.destinations());
        assertEquals(newTransport.itemLists(), updatedTransport.itemLists());
        assertEquals(newTransport.truckId(), updatedTransport.truckId());
        assertEquals(newTransport.driverId(), updatedTransport.driverId());
        assertEquals(newTransport.scheduledTime(), updatedTransport.scheduledTime());
        assertEquals(newTransport.weight(), updatedTransport.weight());
    }

    @Test
    void updateTransport() {

        HashMap<String, Integer> load1 = new HashMap<>();
        load1.put("shirts", 20);
        load1.put("pants", 15);
        load1.put("socks", 30);
        HashMap<String, Integer> unload1 = new HashMap<>();
        unload1.put("jackets", 10);
        unload1.put("hats", 5);
        unload1.put("gloves", 20);

        ItemList itemList = new ItemList(1001, load1, unload1);

        HashMap<Site,ItemList> hm = new HashMap<>();

        Site source = new Site("zone b", "456 oak ave", "(555) 234-5678", "jane doe", Site.SiteType.LOGISTICAL_CENTER);
        Site destination = new Site("zone a", "123 main st", "(555) 123-4567", "john smith", Site.SiteType.BRANCH);
        hm.put(destination, itemList);
        LinkedList<Site> destinations = new LinkedList<>(List.of(destination));
        TransportsService ts = ModuleFactory.getInstance().getTransportsService();
        Transport newTransport = new Transport(
                1,
                source,
                destinations,
                hm,
                "abc123",
                1234,
                LocalDateTime.of(2020, 1, 1, 0, 0),
                2000
        );
        String json = ts.updateTransport(JSON.serialize(newTransport));
        Response<String> response = JSON.deserialize(json, Response.class);
        assertTrue(response.isSuccess());
        String updatedJson = ModuleFactory.getInstance()
                .getTransportsService()
                .getTransport(
                        JSON.serialize(Transport.getLookupObject(newTransport.id()))
                );
        Type type = new TypeToken<Response<Transport>>(){}.getType();
        Response<Transport> updatedResponse = JSON.deserialize(updatedJson, type);
        Transport updatedTransport = updatedResponse.getData();
        assertEquals(newTransport.id(), updatedTransport.id());
        assertEquals(newTransport.source(), updatedTransport.source());
        assertEquals(newTransport.destinations(), updatedTransport.destinations());
        assertEquals(newTransport.itemLists(), updatedTransport.itemLists());
        assertEquals(newTransport.truckId(), updatedTransport.truckId());
        assertEquals(newTransport.driverId(), updatedTransport.driverId());
        assertEquals(newTransport.scheduledTime(), updatedTransport.scheduledTime());
        assertEquals(newTransport.weight(), updatedTransport.weight());
    }

    @Test
    void removeTransport() {
    }

    @Test
    void getTransport() {
    }

    @Test
    void getAllTransports() {
    }
}