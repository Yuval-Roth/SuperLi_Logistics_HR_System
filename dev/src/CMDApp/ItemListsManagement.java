package CMDApp;

import CMDApp.Records.ItemList;
import TransportModule.ServiceLayer.ItemListsService;

import java.util.HashMap;

import static CMDApp.Main.*;

public class ItemListsManagement {

    static ItemListsService ils = factory.getItemListsService();

    static void manageItemLists() {
        while(true){
            System.out.println("=========================================");
            System.out.println("Item lists management");
            System.out.println("Please select an option:");
            System.out.println("1. Create new item list");
            System.out.println("2. Update item list");
            System.out.println("3. Remove item list");
            System.out.println("4. View full item list information");
            System.out.println("5. View all item lists");
            System.out.println("6. Return to previous menu");
            int option = getInt();
            switch (option) {
                case 1 -> createItemList();
                case 2 -> updateItemList();
                case 3 -> removeItemList();
                case 4 -> viewItemList();
                case 5 -> viewAllItemLists();
                case 6 -> {
                    return;
                }
                default -> System.out.println("\nInvalid option!");
            }
        }
    }

    private static void createItemList() {
        System.out.println("=========================================");
        System.out.println("Item list ID: "+itemListIdCounter);
        System.out.println("Enter items details:");
        ItemList list = new ItemList(itemListIdCounter, new HashMap<>(), new HashMap<>());
        System.out.println("\nItem loading list:");
        itemEditor(list.load());
        System.out.println("\nItem unloading list:");
        itemEditor(list.unload());
        String json = JSON.serialize(list);
        String responseJson = ils.addItemList(json);
        Response<String> response = JSON.deserialize(responseJson, Response.class);
        if(response.isSuccess()) {
            itemLists.put(itemListIdCounter,list);
            itemListIdCounter++;
        }
        System.out.println("\n"+response.getMessage());
    }

    private static void updateItemList() {
        while (true) {
            System.out.println("=========================================");
            Integer id = getListID();
            if (id == null) return;

            ItemList oldList = itemLists.get(id);
            ItemList newList = new ItemList(id, oldList.load(), oldList.unload());

            //unloading item list
            System.out.println("\nItem unloading list:");
            for (String key  : newList.load().keySet()){
                System.out.println("  "+key+" : "+newList.load().get(key));
            }
            itemEditor(newList.load());

            //loading item list
            System.out.println("\nItem loading list:");
            for (String key  : newList.unload().keySet()){
                System.out.println("  "+key+" : "+newList.unload().get(key));
            }
            itemEditor(newList.unload());
            String json = JSON.serialize(newList);
            String responseJson = ils.updateItemList(json);
            Response<String> response = JSON.deserialize(responseJson, Response.class);
            if(response.isSuccess()) itemLists.put(id,newList);
            System.out.println("\n"+response.getMessage());
        }
    }

    private static void removeItemList() {
        while (true) {
            System.out.println("=========================================");
            Integer id = getListID();
            if (id == null) continue;
            ItemList list = itemLists.get(id);
            printItemList(list);
            System.out.println("Are you sure you want to remove this item list? (y/n)");
            String option = getLine();
            switch(option){
                case "y" ->{
                    String json = JSON.serialize(list);
                    String responseJson = ils.removeItemList(json);
                    Response<String> response = JSON.deserialize(responseJson, Response.class);
                    if(response.isSuccess()) itemLists.remove(id);
                    System.out.println("\n"+response.getMessage());
                }
                case "n" ->{}
                default -> System.out.println("\nInvalid option!");
            }
            break;
        }
    }

    private static void viewItemList() {
        System.out.println("=========================================");
        System.out.println("Select item list to view:");
        Integer id = getListID();
        if (id == null) return;
        ItemList list = itemLists.get(id);
        printItemList(list);
        System.out.println("\nEnter 'done!' to return to previous menu");
        getLine();
    }

    private static void viewAllItemLists() {
        System.out.println("=========================================");
        System.out.println("All item lists:");
        for (ItemList list : itemLists.values()) {
            System.out.println("-----------------------------------------");
            printItemList(list);
        }
        System.out.println("\nEnter 'done!' to return to previous menu");
        getWord();
    }

    private static void itemEditor(HashMap<String,Integer> items) {
        System.out.println("\nTo remove items from the list enter the item name and 0 in the quantity");
        System.out.println("To finish adding items, enter \"done!\" in the item name");
        while (true) {
            String itemQuantity = getLine("<Item name> <Quantity>: ").toLowerCase();

            if(itemQuantity.equalsIgnoreCase("done!")){
                break;
            }

            String[] itemQuantityArray = itemQuantity.split(" ");
            String item = "";
            for(int i = 0; i < itemQuantityArray.length-1; i++){
                item += itemQuantityArray[i];
                if(i != itemQuantityArray.length-2) item += " ";
            }
            int quantity = Integer.parseInt(itemQuantityArray[itemQuantityArray.length-1]);

            if(quantity <= 0) {
                items.remove(item);
            }
            else {
                items.put(item,quantity);
            }
        }
    }

    private static Integer getListID() {
        System.out.println();
        int id = getInt("Enter item list ID (enter '-1' to return to the previous menu): ");
        if (id == -1) return null;
        if(itemLists.containsKey(id) == false){
            System.out.println("Invalid item list ID!");
            return null;
        }
        return id;
    }

    private static void printItemList(ItemList list) {
        System.out.println("Item list ID: " + list.id());
        System.out.println("Items loading list:");
        for (String key : list.load().keySet()) {
            System.out.println("  "+key + " : " + list.load().get(key));
        }
        System.out.println("Items unloading list:");
        for (String key : list.unload().keySet()) {
            System.out.println("  " + key + " : " + list.unload().get(key));
        }
    }
}
