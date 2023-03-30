package CMDApp;

import TransportModule.ServiceLayer.*;
import java.util.*;

import static CMDApp.DriversManagement.manageDrivers;
import static CMDApp.ItemListsManagement.manageItemLists;
import static CMDApp.SitesManagement.manageSites;
import static CMDApp.TransportsManagement.manageTransports;
import static CMDApp.TrucksManagement.manageTrucks;

public class Main {

    static ModuleFactory factory = ModuleFactory.getInstance();
    static Scanner scanner = new Scanner(System.in);
    static int transportIdCounter = 1;
    static String[] shippingZones = {"North", "South", "East", "West"};
    static String[] sites = {"Site1", "Site2", "Site3", "Site4"};
    static String[] drivers = {"Driver1", "Driver2", "Driver3", "Driver4"};
    static String[] trucks = {"Truck1", "Truck2", "Truck3", "Truck4"};


    public static void main(String[] args) {

        while(true){
            System.out.println("=========================================");
            System.out.println("Welcome to the Transport Module!");
            System.out.println("Please select an option:");
            System.out.println("1. Manage transports");
            System.out.println("2. Manage transport module resources");
            System.out.println("3. Manage item lists");
            System.out.println("4. Exit");
            int option = getInt();
            switch (option){
                case 1:
                    manageTransports();
                    break;
                case 2:
                    manageResources();
                    break;
                case 3:
                    manageItemLists();
                    break;
                case 4:
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid option!");
                    continue;
            }

        }
    }

    private static void manageResources() {
        while(true){
            System.out.println("=========================================");
            System.out.println("Transport module resources management");
            System.out.println("Please select an option:");
            System.out.println("1. Manage sites");
            System.out.println("2. Manage drivers");
            System.out.println("3. Manage trucks");
            System.out.println("4. Return to main menu");
            int option = getInt();
            switch (option){
                case 1:
                    manageSites();
                    break;
                case 2:
                    manageDrivers();
                    break;
                case 3:
                    manageTrucks();
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid option!");
                    continue;
            }
        }
    }

    //==========================================================================|
    //============================== HELPER METHODS ============================|
    //==========================================================================|

    static int getInt(){
        return getInt(">> ");
    }

    static int getInt(String prefix){
        System.out.print(prefix);
        int option = 0;
        try{
            option = scanner.nextInt();
        }catch(InputMismatchException e){
            scanner = new Scanner(System.in);
        }
        return option;
    }

    static String getString(){
        return getString(">> ");
    }

    static String getString(String prefix) {
        System.out.print(prefix);
        return scanner.next();
    }

    static int pickSite(boolean allowDone) {
        int i = 1;
        for(String site : sites){
            System.out.println((i++)+". "+site);
        }
        if(allowDone) System.out.println(i+". Done");
        int option = getInt()-1;
        if( (allowDone && (option < 0 || option > sites.length))
                || (!allowDone && (option < 0 || option > sites.length-1))){
            System.out.println("Invalid option!");
            return pickSite(allowDone);
        }
        if(allowDone && option == sites.length) return -1;
        return option;
    }
}

