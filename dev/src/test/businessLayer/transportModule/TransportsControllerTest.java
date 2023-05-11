package businessLayer.transportModule;

import dataAccessLayer.dalUtils.DalException;
import dataAccessLayer.transportModule.SitesRoutesDAO;
import dataAccessLayer.transportModule.TransportsDAO;
import javafx.util.Pair;
import objects.transportObjects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import serviceLayer.employeeModule.Services.EmployeesService;
import utils.JsonUtils;
import utils.Response;
import utils.transportUtils.TransportException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransportsControllerTest {

    public static final String SOURCE_ADDRESS = "sourceAddress";
    public static final String DESTINATION_ADDRESS_1 = "destinationAddress1";
    public static final String DESTINATION_ADDRESS_2 = "destinationAddress2";
    public static final String DRIVER_ID = "123";
    public static final String TRUCK_ID = "123ABC";
    public static final int ITEM_LIST_ID_1 = 1;
    public static final int ITEM_LIST_ID_2 = 2;
    public static final LocalDateTime DEPARTURE_TIME = LocalDateTime.of(LocalDate.of(2020, 1, 1), LocalTime.of(12, 0));
    private static final int TRANSPORT_ID = 1;
    TransportsController transportController;
    ItemListsController itemListsController;
    TrucksController trucksController;
    SitesController sitesController;
    EmployeesService employeesService;
    DriversController driversController;
    TransportsDAO transportsDAO;
    SitesRoutesDAO sitesRoutesDAO;
    Transport transport;
    Site source;
    Driver driver;
    Truck truck;
    Site destination1;
    Site destination2;
    ItemList itemList1;
    ItemList itemList2;

    @BeforeEach
    void setUp() {

        itemListsController = mock(ItemListsController.class);
        trucksController = mock(TrucksController.class);
        sitesController = mock(SitesController.class);
        driversController = mock(DriversController.class);
        transportsDAO = mock(TransportsDAO.class);
        sitesRoutesDAO = mock(SitesRoutesDAO.class);

        try {
            when(transportsDAO.selectCounter()).thenReturn(1);
        } catch (DalException e) {
            fail(e);
        }

        try {
            transportController = new TransportsController(
                    trucksController,
                    driversController,
                    sitesController,
                    itemListsController,
                    transportsDAO
            );
        } catch (TransportException e) {
            fail(e);
        }

        employeesService = mock(EmployeesService.class);
        transportController.injectDependencies(employeesService);

        source = new Site("source1", SOURCE_ADDRESS, "zone1", "0545555550", "contactNameSource", Site.SiteType.LOGISTICAL_CENTER);
        destination1 = new Site("dest1", DESTINATION_ADDRESS_1, "zone1", "0545555551", "contactNameDest1", Site.SiteType.BRANCH);
        destination2 = new Site("dest2", DESTINATION_ADDRESS_2, "zone1", "0545555552", "contactNameDest2", Site.SiteType.BRANCH);
        driver = new Driver(DRIVER_ID, "driverName", Driver.LicenseType.C3);
        truck = new Truck(TRUCK_ID, "model", 2000, 25000, Truck.CoolingCapacity.FROZEN);
        itemList1 = new ItemList(ITEM_LIST_ID_1,
                new HashMap<>() {{
                    put("item1", 1);
                    put("item2", 2);
                }},
                new HashMap<>() {{
                    put("item3", 3);
                    put("item4", 4);
                }}
        );
        itemList2 = new ItemList(ITEM_LIST_ID_2,
                new HashMap<>() {{
                    put("item5", 5);
                    put("item6", 6);
                }},
                new HashMap<>() {{
                    put("item7", 7);
                    put("item8", 8);
                }}
        );

        transport = new Transport(
                source.name(),
                new LinkedList<>() {{
                    add(destination1.name());
                    add(destination2.name());
                }},
                new HashMap<>() {{
                    put(destination1.name(), itemList1.id());
                    put(destination2.name(), itemList2.id());
                }},
                driver.id(),
                truck.id(),
                DEPARTURE_TIME,
                15000
        );
    }

    @Test
    void addTransport() {

        try {
            initializeMocksToPassTransportValidation();
            Transport added = assertDoesNotThrow(() -> transportController.addTransport(transport));
            assertEquals(1, added.id());
        } catch (TransportException | DalException e) {
            fail(e);
        }
    }

    @Test
    void addTransportPredefinedId(){
        Transport transport = new Transport(
                500,
                source.name(),
                new LinkedList<>() {{
                    add(destination1.name());
                    add(destination2.name());
                }},
                new HashMap<>() {{
                    put(destination1.name(), itemList1.id());
                    put(destination2.name(), itemList2.id());
                }},
                driver.id(),
                truck.id(),
                DEPARTURE_TIME,
                15000
        );
        assertThrows(UnsupportedOperationException.class,() -> transportController.addTransport(transport));
    }

    @Test
    void updateTransport() {
        try {
            //set up
            Transport updatedTransport = new Transport(
                    1,
                    destination1.name(),
                    new LinkedList<>() {{
                        add(destination2.name());
                    }},
                    new HashMap<>() {{
                        put(destination2.name(), itemList1.id());
                    }},
                    driver.id(),
                    truck.id(),
                    DEPARTURE_TIME,
                    25000
            );
            initializeMocksToPassTransportValidation();
            List<String> route = List.of(destination1.name(), destination2.name());
            HashMap<Pair<String,String>, Double> siteDistances = new HashMap<>(){{
                put(new Pair<>(destination1.name(), destination2.name()), 100.0);
            }};
            when(sitesController.buildSitesTravelTimes(route)).thenReturn(siteDistances);
            when(transportsDAO.exists(updatedTransport)).thenReturn(true);


            //test
            assertDoesNotThrow(() -> transportController.updateTransport(updatedTransport.id(), updatedTransport));
        } catch (DalException | TransportException e) {
            fail(e);
        }
    }

    @Test
    void updateTransportDoesNotExist(){
        try{
            when(transportsDAO.exists(Transport.getLookupObject(transport.id()))).thenReturn(false);
        } catch (DalException e) {
            fail(e);
        }
        assertThrows(TransportException.class,() -> transportController.updateTransport(transport.id(), transport));
    }

    @Test
    void updateTransportManualOverride(){

        //set up
        try{
            transportController = spy(transportController);
            initializeMocksToPassTransportValidation();
            when(transportsDAO.exists(Transport.getLookupObject(TRANSPORT_ID))).thenReturn(true);
            transportController.initializeEstimatedArrivalTimes(transport);
            transport.deliveryRoute().overrideArrivalTime(destination2.name(), LocalTime.of(16,0));
            Mockito.doThrow(new RuntimeException("Entered arrival time init when not supposed to")).when(transportController).initializeEstimatedArrivalTimes(transport);
        } catch (DalException | TransportException e) {
            fail(e);
        }

        //test
        try {
            transportController.updateTransport(TRANSPORT_ID, transport);
        } catch (TransportException | RuntimeException e) {
            fail(e);
        }
    }

    @Test
    void removeTransport() {
        try{
            when(transportsDAO.exists(Transport.getLookupObject(transport.id()))).thenReturn(true);
        } catch (DalException e) {
            fail(e);
        }
        assertDoesNotThrow(() -> transportController.removeTransport(transport.id()));
    }

    @Test
    void removeTransportDoesNotExist(){
        try{
            when(transportsDAO.exists(Transport.getLookupObject(5))).thenReturn(false);
        } catch (DalException e) {
            fail(e);
        }
        assertThrows(TransportException.class,() -> transportController.removeTransport(5));
    }

    @Test
    void getTransport() {
        try{
            initializeMocksToPassTransportValidation();
            when(transportsDAO.exists(Transport.getLookupObject(1))).thenReturn(true);
            when(transportsDAO.select(Transport.getLookupObject(1))).thenReturn(transport);

            Transport fetchedTransport = transportController.getTransport(1);
            assertDeepEquals(transport, fetchedTransport);
        } catch (DalException | TransportException e) {
            fail(e);
        }
    }

    @Test
    void getTransportDoesNotExist(){
        try{
            when(transportsDAO.exists(Transport.getLookupObject(transport.id()))).thenReturn(false);
        } catch (DalException e) {
            fail(e);
        }
        assertThrows(TransportException.class,() -> transportController.getTransport(transport.id()));
    }

    @Test
    void getAllTransports() {
        try{
            //set up
            Transport transport2 = new Transport(
                    2,
                    source.name(),
                    new LinkedList<>() {{
                        add(destination1.name());
                        add(destination2.name());
                    }},
                    new HashMap<>() {{
                        put(destination1.name(), itemList1.id());
                        put(destination2.name(), itemList2.id());
                    }},
                    driver.id(),
                    truck.id(),
                    DEPARTURE_TIME,
                    15000
            );
            initializeMocksToPassTransportValidation();

            List<Transport> transports = List.of(transport, transport2);
            when(transportsDAO.selectAll()).thenReturn(transports);

            //test
            List<Transport> fetchedTransports = transportController.getAllTransports();
            assertDeepEquals(transports.get(0), fetchedTransports.get(0));
            assertDeepEquals(transports.get(1), fetchedTransports.get(1));

        } catch (DalException | TransportException e) {
            fail(e);
        }

    }

    @Test
    void createTransportWithTooMuchWeight(){

        //set up
        Transport badTransport = new Transport(
                source.name(),
                new LinkedList<>() {{
                    add(destination1.name());
                    add(destination2.name());
                }},
                new HashMap<>() {{
                    put(destination1.name(), itemList1.id());
                    put(destination2.name(), itemList2.id());
                }},
                driver.id(),
                truck.id(),
                DEPARTURE_TIME,
                50000
        );
        try{
            initializeMocksToPassTransportValidation();
        } catch (DalException | TransportException e) {
            fail(e);
        }

        //test
        try {
            transportController.addTransport(badTransport);
            fail("TransportException expected");
        } catch (TransportException e) {
            assertEquals("weight",e.getCause().getMessage());
        }
    }

    @Test
    void createTransportWithBadLicense(){
        Driver badDriver = new Driver(driver.id(), driver.name(), Driver.LicenseType.A1);
        Transport badTransport = new Transport(
                source.name(),
                new LinkedList<>() {{
                    add(destination1.name());
                    add(destination2.name());
                }},
                new HashMap<>() {{
                    put(destination1.name(), itemList1.id());
                    put(destination2.name(), itemList2.id());
                }},
                driver.id(),
                truck.id(),
                DEPARTURE_TIME,
                24000
        );
        try{
            initializeMocksToPassTransportValidation();
            when(driversController.getDriver(driver.id())).thenReturn(badDriver);
        } catch (DalException | TransportException e) {
            throw new RuntimeException(e);
        }

        //test
        try {
            transportController.addTransport(badTransport);
            fail("TransportException expected");
        } catch (TransportException e) {
            assertEquals("license",e.getCause().getMessage());
        }
    }

    @Test
    void createTransportWithBadLicenseAndTooMuchWeight(){
        Driver badDriver = new Driver(driver.id(), driver.name(), Driver.LicenseType.A1);
        Transport badTransport = new Transport(
                source.name(),
                new LinkedList<>() {{
                    add(destination1.name());
                    add(destination2.name());
                }},
                new HashMap<>() {{
                    put(destination1.name(), itemList1.id());
                    put(destination2.name(), itemList2.id());
                }},
                driver.id(),
                truck.id(),
                DEPARTURE_TIME,
                30000
        );
        try{
            initializeMocksToPassTransportValidation();
            when(driversController.getDriver(driver.id())).thenReturn(badDriver);
        } catch (DalException | TransportException e) {
            throw new RuntimeException(e);
        }

        //test
        try {
            transportController.addTransport(badTransport);
            fail("TransportException expected");
        } catch (TransportException e) {
            assertEquals("license,weight",e.getCause().getMessage());
        }
    }

    @Test
    void createTransportEverythingDoesNotExist(){

        try {
            transportController.addTransport(transport);
            fail("TransportException expected");
        } catch (TransportException e) {
            assertEquals("driver,truck,source,destination:0,itemList:0,destination:1,itemList:1",e.getCause().getMessage());
        }
    }
    @Test
    void initializeEstimatedArrivalTimes() {

        //set up
        List<String> route = new LinkedList<>();
        route.add(transport.source());
        route.addAll(transport.destinations());
        HashMap<Pair<String, String>, Double> siteRoutes = new HashMap<>(){{
            put(new Pair<>(source.name(), destination1.name()), 100.0);
            put(new Pair<>(destination1.name(), destination2.name()), 100.0);
        }};

        try {
            when(sitesController.buildSitesTravelTimes(eq(route))).thenReturn(siteRoutes);
            //test
            transportController.initializeEstimatedArrivalTimes(transport);
            assertEquals(
                    transport.deliveryRoute().getEstimatedTimeOfArrival(destination1.name()),
                    DEPARTURE_TIME.plusMinutes(100).toLocalTime());
            assertEquals(
                    transport.deliveryRoute().getEstimatedTimeOfArrival(destination2.name()),
                    DEPARTURE_TIME.plusMinutes(200+TransportsController.AVERAGE_TIME_PER_VISIT).toLocalTime());
        } catch (TransportException e) {
            throw new RuntimeException(e);
        }


    }

    private void initializeMocksToPassTransportValidation() throws DalException, TransportException {
        when(driversController.driverExists(driver.id())).thenReturn(true);
        when(driversController.getDriver(driver.id())).thenReturn(driver);
        Response r = new Response(true,List.of(driver.id()));
        when(employeesService.getAvailableDrivers(JsonUtils.serialize(DEPARTURE_TIME))).thenReturn(r.toJson());

        when(trucksController.truckExists(truck.id())).thenReturn(true);
        when(trucksController.getTruck(TRUCK_ID)).thenReturn(truck);

        when(sitesController.siteExists(source.name())).thenReturn(true);
        when(sitesController.siteExists(destination1.name())).thenReturn(true);
        when(sitesController.siteExists(destination2.name())).thenReturn(true);
        when(sitesController.getSite(source.name())).thenReturn(source);
        when(sitesController.getSite(destination1.name())).thenReturn(destination1);
        when(sitesController.getSite(destination2.name())).thenReturn(destination2);
        when(employeesService.checkStoreKeeperAvailability(JsonUtils.serialize(DEPARTURE_TIME), source.name())).thenReturn(new Response(true).toJson());
        when(employeesService.checkStoreKeeperAvailability(JsonUtils.serialize(DEPARTURE_TIME), destination1.name())).thenReturn(new Response(true).toJson());
        when(employeesService.checkStoreKeeperAvailability(JsonUtils.serialize(DEPARTURE_TIME), destination2.name())).thenReturn(new Response(true).toJson());

        when(itemListsController.listExists(itemList1.id())).thenReturn(true);
        when(itemListsController.listExists(itemList2.id())).thenReturn(true);

        List<String> route = new LinkedList<>();
        route.add(transport.source());
        route.addAll(transport.destinations());
        HashMap<Pair<String, String>, Double> siteRoutes = new HashMap<>(){{
            put(new Pair<>(source.name(), destination1.name()), 100.0);
            put(new Pair<>(destination1.name(), destination2.name()), 100.0);
        }};
        when(sitesController.buildSitesTravelTimes(route)).thenReturn(siteRoutes);
    }

    private void assertDeepEquals(Transport transport1, Transport transport2) {
        assertEquals(transport1.id(), transport2.id());
        assertEquals(transport1.source(), transport2.source());
        assertEquals(transport1.destinations(), transport2.destinations());
        assertEquals(transport1.itemLists(), transport2.itemLists());
        assertEquals(transport1.driverId(), transport2.driverId());
        assertEquals(transport1.truckId(), transport2.truckId());
        assertEquals(transport1.departureTime(), transport2.departureTime());
        assertEquals(transport1.weight(), transport2.weight());
        assertEquals(transport1.deliveryRoute().estimatedArrivalTimes(),transport2.deliveryRoute().estimatedArrivalTimes());
    }
}