package dataAccessLayer.transportModule;

import businessLayer.employeeModule.Employee;
import businessLayer.employeeModule.Role;
import businessLayer.transportModule.*;
import dataAccessLayer.DalFactory;
import dataAccessLayer.dalAbstracts.SQLExecutor;
import dataAccessLayer.dalAssociationClasses.transportModule.SiteRoute;
import dataAccessLayer.dalUtils.OfflineResultSet;
import dataAccessLayer.employeeModule.EmployeeDAO;
import exceptions.DalException;
import objects.transportObjects.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static dataAccessLayer.DalFactory.TESTING_DB_NAME;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class DeliveryRoutesDAOTest {

    private DeliveryRoutesDAO dao;
    private Site source;
    private Site dest1;
    private Site dest2;
    private Truck truck;
    private Driver driver;
    private ItemList itemList;
    private SiteRoute route1;
    private SiteRoute route2;
    private Transport transport;
    private TransportsDAO transportsDAO;
    private Employee employee;
    private HashMap<String, LocalTime> arrivalTimes;
    private SitesDAO sitesDAO;
    private TrucksDAO trucksDAO;
    private EmployeeDAO employeeDAO;
    private DriversDAO driversDAO;
    private ItemListsDAO itemListsDAO;
    private SitesRoutesDAO routesDAO;
    private DalFactory factory;
    private DeliveryRoute deliveryRoute;

    @BeforeEach
    void setUp() {
        try {
            DalFactory.clearTestDB();

            //sites set up
            source = new Site("source1", "source1 name", "zone1", "12345","kobi", Site.SiteType.SUPPLIER);
            dest1 = new Site("dest1", "dest1 name", "zone1", "12345","kobi", Site.SiteType.SUPPLIER);
            dest2 = new Site("dest2", "dest2 name", "zone1", "12345","kobi", Site.SiteType.SUPPLIER);

            //truck set up
            truck = new Truck("1", "model1", 1000, 20000, Truck.CoolingCapacity.FROZEN);

            //driver set up
            employee = new Employee("name1","12345","Poalim",50, LocalDate.of(1999,10,10),"conditions","details");
            employee.addRole(Role.Driver);
            employee.addRole(Role.GeneralWorker);
            driver = new Driver(employee.getId(),employee.getName(), Driver.LicenseType.C3);

            //itemList set up
            HashMap<String, Integer> load = new HashMap<>(){{
                put("item1", 1);
                put("item2", 2);
                put("item3", 3);
            }};
            HashMap<String, Integer> unload = new HashMap<>(){{
                put("item4", 4);
                put("item5", 5);
                put("item6", 6);
            }};
            itemList = new ItemList(1, load, unload);

            //transport set up
            route1 = new SiteRoute(source.address(), dest1.address(), 10,10);
            route2 = new SiteRoute(dest1.address(), dest2.address(), 10,10);

            transport = new Transport(1,
                    new LinkedList<>(),
                    new HashMap<>(),
                    driver.id(),
                    truck.id(),
                    LocalDateTime.of(2023, 2, 2, 12, 0),
                    15000
            );

            //initialize transport arrival times
            arrivalTimes = new HashMap<>();
            arrivalTimes.put(source.name(), LocalTime.of(12, 0));
            arrivalTimes.put(dest1.name(), LocalTime.of(12, (int) route1.duration()));
            arrivalTimes.put(dest2.name(), LocalTime.of(12, (int)(route1.duration() + route2.duration() + TransportsController.AVERAGE_TIME_PER_VISIT)));

            List<String> route = List.of(source.name(), dest1.name(), dest2.name());
            HashMap<String,Integer> itemLists = new HashMap<>(){{
                put(source.name(),-1);
                put(dest1.name(),1);
                put(dest2.name(),1);
            }};


            deliveryRoute = new DeliveryRoute(
                    transport.id(),
                    route,
                    itemLists,
                    arrivalTimes
            );

            factory = new DalFactory(TESTING_DB_NAME);
            sitesDAO = factory.sitesDAO();
            trucksDAO = factory.trucksDAO();
            employeeDAO = factory.employeeDAO();
            driversDAO = factory.driversDAO();
            itemListsDAO = factory.itemListsDAO();
            transportsDAO = factory.transportsDAO();
            routesDAO = factory.sitesRoutesDAO();
            dao = factory.deliveryRoutesDAO();

            sitesDAO.insert(source);
            sitesDAO.insert(dest1);
            sitesDAO.insert(dest2);
            routesDAO.insert(route1);
            routesDAO.insert(route2);
            trucksDAO.insert(truck);
            employeeDAO.insert(employee);
            driversDAO.insert(driver);
            itemListsDAO.insert(itemList);
            transportsDAO.insert(transport);
        } catch (DalException e) {
            fail(e.getMessage(),e.getCause());
        }
    }

    @AfterEach
    void tearDown() {
        DalFactory.clearTestDB();
    }

    @Test
    void insert_and_select() {
        try {

            dao.insert(deliveryRoute);
            DeliveryRoute selected = dao.select(DeliveryRoute.getLookupObject(transport.id()));
            assertDeepEquals(deliveryRoute, selected);

        } catch (DalException e) {
            fail(e.getMessage(),e.getCause());
        }
    }

    @Test
    void selectAll() {
//        //set up
//        LinkedList<TransportDestination> expected = new LinkedList<>();
//        expected.add(transportDestination1);
//        expected.add(transportDestination2);
//        expected.add(transportDestination3);
//        expected.add(transportDestination4);
//        List.of(5,6,7,8).forEach(i -> {
//            try {
//                TransportDestination newDestination = new TransportDestination(TRANSPORT_ID, i, SOURCE_NAME, LIST_ID, ARRIVAL_TIME);
//                expected.add(newDestination);
//                dao.insert(newDestination);
//            } catch (DalException e) {
//                fail(e.getMessage(),e.getCause());
//            }
//        });
//
//        //test
//        try {
//            List<TransportDestination> selected = dao.selectAll();
//            assertEquals(expected.size(), selected.size());
//            for (int i = 0; i < expected.size(); i++) {
//                assertDeepEquals(expected.get(i), selected.get(i));
//            }
//        } catch (DalException e) {
//            fail(e.getMessage(),e.getCause());
//        }
    }

    @Test
    void selectAllRelated() {
//        //set up
//        LinkedList<TransportDestination> expected = new LinkedList<>();
//        expected.add(transportDestination1);
//        expected.add(transportDestination2);
//        expected.add(transportDestination3);
//        expected.add(transportDestination4);
//        List.of(5,6,7,8).forEach(i -> {
//            try {
//                TransportDestination newDestination = new TransportDestination(TRANSPORT_ID, i, SOURCE_NAME, LIST_ID, ARRIVAL_TIME);
//                dao.insert(newDestination);
//                expected.add(newDestination);
//            } catch (DalException e) {
//                fail(e.getMessage(),e.getCause());
//            }
//        });
//
//        //test
//        try {
//            List<TransportDestination> selected = dao.selectAllRelated(Transport.getLookupObject(TRANSPORT_ID));
//            assertEquals(expected.size(), selected.size());
//            for (int i = 0; i < expected.size(); i++) {
//                assertDeepEquals(expected.get(i), selected.get(i));
//            }
//        } catch (DalException e) {
//            fail(e.getMessage(),e.getCause());
//        }
    }

    @Test
    void insert() {
//        try {
//            TransportDestination newDestination = new TransportDestination(TRANSPORT_ID, 5, SOURCE_NAME, LIST_ID, ARRIVAL_TIME);
//            dao.insert(newDestination);
//            TransportDestination selected = dao.select(TransportDestination.getLookupObject(
//                    newDestination.transportId(),
//                    newDestination.destination_index()));
//            assertDeepEquals(newDestination, selected);
//        } catch (DalException e) {
//            fail(e.getMessage(),e.getCause());
//        }
    }

    @Test
    void insertAll() {
//        //set up
//        LinkedList<TransportDestination> expected = new LinkedList<>();
//        List.of(5,6,7,8).forEach(i -> {
//            TransportDestination newDestination = new TransportDestination(TRANSPORT_ID, i, SOURCE_NAME, LIST_ID, ARRIVAL_TIME);
//            expected.add(newDestination);
//        });
//        try {
//            dao.insertAll(expected);
//        } catch (DalException e) {
//            fail(e.getMessage(),e.getCause());
//        }
//        expected.addFirst(transportDestination4);
//        expected.addFirst(transportDestination3);
//        expected.addFirst(transportDestination2);
//        expected.addFirst(transportDestination1);
//
//        //test
//        try {
//            List<TransportDestination> selected = dao.selectAllRelated(Transport.getLookupObject(TRANSPORT_ID));
//            assertEquals(expected.size(), selected.size());
//            for (int i = 0; i < expected.size(); i++) {
//                assertDeepEquals(expected.get(i), selected.get(i));
//            }
//        } catch (DalException e) {
//            fail(e.getMessage(),e.getCause());
//        }
    }

    @Test
    void update() {
//        try {
//
//            TransportDestination newDestination = new TransportDestination(
//                    TRANSPORT_ID,
//                    transportDestination2.destination_index(),
//                    DEST_NAME1,
//                    LIST_ID, ARRIVAL_TIME);
//            dao.update(newDestination);
//            TransportDestination selected = dao.select(TransportDestination.getLookupObject(
//                    newDestination.transportId(),
//                    newDestination.destination_index()));
//            assertDeepEquals(newDestination, selected);
//        } catch (DalException e) {
//            fail(e.getMessage(),e.getCause());
//        }
    }

    @Test
    void delete() {
//        try {
//            dao.delete(transportDestination4);
//            List<TransportDestination> selected = dao.selectAllRelated(Transport.getLookupObject(TRANSPORT_ID));
//            assertEquals(3, selected.size());
//            assertDeepEquals(transportDestination1, selected.get(0));
//            assertDeepEquals(transportDestination2, selected.get(1));
//            assertDeepEquals(transportDestination3, selected.get(2));
//        } catch (DalException e) {
//            fail(e.getMessage(),e.getCause());
//        }
    }

    @Test
    void exists() {
//        try {
//            assertTrue(dao.exists(transportDestination2));
//            assertFalse(dao.exists(TransportDestination.getLookupObject(TRANSPORT_ID, 10)));
//        } catch (DalException e) {
//            fail(e.getMessage(),e.getCause());
//        }
    }

    @Test
    void deleteAllRelated() {
//        try {
//            List<TransportDestination> selected = dao.selectAllRelated(Transport.getLookupObject(TRANSPORT_ID));
//            assertEquals(4, selected.size());
//            dao.deleteAllRelated(Transport.getLookupObject(TRANSPORT_ID));
//            selected = dao.selectAllRelated(Transport.getLookupObject(TRANSPORT_ID));
//            assertEquals(0, selected.size());
//        } catch (DalException e) {
//            fail(e.getMessage(),e.getCause());
//        }
    }

    @Test
    void insertFromTransport() {

//        try {
//
//            //set up
//
//            Transport transport2 = new Transport(
//                    2,
//                    source.name(),
//                    new LinkedList<>() {{
//                        add(dest1.name());
//                        add(dest2.name());
//                    }},
//                    new HashMap<>() {{
//                        put(dest1.name(), 1);
//                        put(dest2.name(), 1);
//                    }},
//                    driver.id(),
//                    truck.id(),
//                    LocalDateTime.of(2020, 1, 1, 1, 1),
//                    15000
//            );
//            transportsDAO.insert(transport2);
//
//            //test
//            dao.insertFromTransport(transport2);
//            List<TransportDestination> selected = dao.selectAllRelated(transport2);
//            assertEquals(2, selected.size());
//            assertEquals(dest1.name(), selected.get(0).name());
//            assertEquals(dest2.name(), selected.get(1).name());
//
//        } catch (DalException e) {
//            fail(e.getMessage(),e.getCause());
//        }
    }

    @Test
    void getObjectFromResultSet() {
        SQLExecutor cursor = factory.cursor();
        try{
            //set up
            dao.insert(deliveryRoute);

            //test
            String query = String.format("SELECT * FROM delivery_routes WHERE transport_id = %d; ",transport.id());
            OfflineResultSet resultSet = cursor.executeRead(query);
            DeliveryRoute selected = dao.getObjectFromResultSet(resultSet);
            assertDeepEquals(deliveryRoute, selected);
        } catch (SQLException | DalException e){
            fail(e.getMessage(),e.getCause());
        }
    }

    private void assertDeepEquals(DeliveryRoute route1, DeliveryRoute route2) {
        assertEquals(route1.transportId(), route2.transportId());
        assertEquals(route1.route(), route2.route());
        assertEquals(route1.itemLists(), route2.itemLists());
        assertEquals(route1.estimatedArrivalTimes(), route2.estimatedArrivalTimes());
    }
}