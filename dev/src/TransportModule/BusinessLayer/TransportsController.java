package TransportModule.BusinessLayer;

import TransportModule.BusinessLayer.Records.Driver;
import TransportModule.BusinessLayer.Records.Transport;
import TransportModule.BusinessLayer.Records.Truck;

import java.io.IOException;
import java.util.LinkedList;
import java.util.TreeMap;

public class TransportsController {

    private TrucksController tc;
    private DriversController dc;
    private TreeMap<Integer, Transport> transports;
    private int idCounter;

    public TransportsController(TrucksController tc, DriversController dc){
        transports = new TreeMap<>();
        idCounter = 0; // currently not in use. this will have to be restored from the DB in the future
        this.tc = tc;
        this.dc = dc;
    }

    public void addTransport(Transport transport)throws IOException{
        validateTransport(transport);

        transports.put(transport.id(), transport);
    }

    public Transport getTransport(int id) throws IOException {
        if (transports.containsKey(id) == false)
            throw new IOException("Transport not found");

        return transports.get(id);
    }

    public Transport removeTransport(int id) throws IOException {
        if (transports.containsKey(id) == false)
            throw new IOException("Transport not found");

        return transports.remove(id);
    }
    public void updateTransport(int id, Transport newTransport) throws IOException{
        if(transports.containsKey(id) == false)
            throw new IOException("Transport not found");

        validateTransport(newTransport);

        transports.put(id, newTransport);
    }

    public LinkedList<Transport> getAllTransports(){
        LinkedList<Transport> list = new LinkedList<>();
        for (Transport t : transports.values())
            list.add(t);
        return list;
    }

    private void validateTransport(Transport transport) throws IOException{
        Truck truck = tc.getTruck(transport.truckId());
        Driver driver = dc.getDriver(transport.driverId());

        // used to return information about all the errors
        String toThrow = "";
        boolean throwException = false;
        //==================================================

        // weight validation
        int weight = transport.weight();
        if (truck.maxWeight() < weight) {
            toThrow += "The truck's maximum weight has been exceeded";
            throwException = true;
        }

        // truck - driver validation
        Truck.CoolingCapacity coolingCapacity = truck.coolingCapacity();

        // {weight, cooling capacity}
        int[] requiredLicense = switch(coolingCapacity) {
            case NONE -> {
                if(weight <= 10000) yield new int[]{10000,1};
                else if (weight > 10000 && weight <= 20000) yield new int[]{20000,1};
                else yield new int[]{30000,1};
            }
            case COLD -> {
                if(weight <= 10000) yield new int[]{10000,2};
                else if (weight > 10000 && weight <= 20000) yield new int[]{20000,2};
                else yield new int[]{30000,2};
            }
            case FROZEN -> {
                if(weight <= 10000) yield new int[]{10000,3};
                else if (weight > 10000 && weight <= 20000) yield new int[]{20000,3};
                else yield new int[]{30000,3};
            }
        };

        if(weight < requiredLicense[0] || requiredLicense[1] < coolingCapacity.ordinal()+1){
            if(throwException) toThrow += "\n";
            toThrow += "A driver with license type "+driver.licenseType()+
                            " is not permitted to drive this truck";
            throwException = true;
        }
        
        if(throwException) throw new IOException(toThrow);
    }
}
