package serviceLayer.transportModule;

import businessLayer.transportModule.DriversController;
import businessLayer.transportModule.SitesController;
import businessLayer.transportModule.TrucksController;
import objects.transportObjects.Driver;
import objects.transportObjects.Site;
import objects.transportObjects.Truck;
import utils.JsonUtils;
import utils.Response;
import utils.transportUtils.TransportException;

public class ResourceManagementService {

    private SitesController sitesController;
    private DriversController driversController;
    private TrucksController trucksController;

    public ResourceManagementService(SitesController sitesController,
                                     DriversController driversController,
                                     TrucksController trucksController)
    {
        this.sitesController = sitesController;
        this.driversController = driversController;
        this.trucksController = trucksController;
    }

    public String addDriver(String json){
        Driver driver = JsonUtils.deserialize(json, Driver.class);
        try{
            driversController.addDriver(driver);
        }catch(TransportException e){
            return Response.getErrorResponse(e).toJson();
        }
        return new Response("Driver added successfully", true).toJson();
    }

    /**
     * @param json serialized {@link Driver#getLookupObject(String)}
     */
    public String removeDriver(String json){
        Driver driver = JsonUtils.deserialize(json, Driver.class);
        try{
            driversController.removeDriver(driver.id());
        }catch(TransportException e){
            return Response.getErrorResponse(e).toJson();
        }
        return new Response("Driver removed successfully", true).toJson();
    }

    public String updateDriver(String json){
        Driver driver = JsonUtils.deserialize(json, Driver.class);
        try{
            driversController.updateDriver(driver.id(), driver);
        }catch(TransportException e){
            return Response.getErrorResponse(e).toJson();
        }
        return new Response("Driver updated successfully", true).toJson();
    }

    /**
     * @param json serialized {@link Driver#getLookupObject(String)}
     */
    public String getDriver(String json){
        Driver driver = JsonUtils.deserialize(json, Driver.class);
        try{
            driver = driversController.getDriver(driver.id());
        }catch(TransportException e){
            return Response.getErrorResponse(e).toJson();
        }
        return new Response("Driver found successfully", true, driver).toJson();
    }

    public String getAllDrivers(){
        try {
            return new Response("Drivers found successfully", true, driversController.getAllDrivers()).toJson();
        } catch (TransportException e) {
            return Response.getErrorResponse(e).toJson();
        }
    }

    public String addTruck(String json){
        Truck truck = JsonUtils.deserialize(json, Truck.class);
        try{
            trucksController.addTruck(truck);
        }catch(TransportException e){
            return Response.getErrorResponse(e).toJson();
        }
        return new Response("Truck added successfully", true).toJson();
    }

    /**
     * @param json serialized {@link Truck#getLookupObject(String)}
     */
    public String removeTruck(String json){
        Truck truck = JsonUtils.deserialize(json, Truck.class);
        try{
            trucksController.removeTruck(truck.id());
        }catch(TransportException e){
            return Response.getErrorResponse(e).toJson();
        }
        return new Response("Truck removed successfully", true).toJson();
    }

    public String updateTruck(String json){
        Truck truck = JsonUtils.deserialize(json, Truck.class);
        try{
            trucksController.updateTruck(truck.id(), truck);
        }catch(TransportException e){
            return Response.getErrorResponse(e).toJson();
        }
        return new Response("Truck updated successfully", true).toJson();
    }

    /**
     * @param json serialized {@link Truck#getLookupObject(String)}
     */
    public String getTruck(String json){
        Truck truck = JsonUtils.deserialize(json, Truck.class);
        try{
            truck = trucksController.getTruck(truck.id());
        }catch(Exception e){
            return Response.getErrorResponse(e).toJson();
        }
        return new Response("Truck found successfully", true, truck).toJson();
    }

    public String getAllTrucks(){
        try {
            return new Response("Trucks found successfully", true, trucksController.getAllTrucks()).toJson();
        } catch (TransportException e) {
            return Response.getErrorResponse(e).toJson();
        }
    }

    public String addSite(String json){
        Site site = JsonUtils.deserialize(json, Site.class);
        try{
            sitesController.addSite(site);
        }catch(TransportException e){
            return Response.getErrorResponse(e).toJson();
        }
        return new Response("Site added successfully", true).toJson();
    }

    /**
     * @param json serialized {@link Site#getLookupObject(String)}
     */
    public String removeSite(String json){
        Site site = JsonUtils.deserialize(json, Site.class);
        try{
            sitesController.removeSite(site.address());
        }catch(TransportException e){
            return Response.getErrorResponse(e).toJson();
        }
        return new Response("Site removed successfully", true).toJson();
    }

    public String updateSite(String json){
        Site site = JsonUtils.deserialize(json, Site.class);
        try{
            sitesController.updateSite(site.address(), site);
        }catch(TransportException e){
            return Response.getErrorResponse(e).toJson();
        }
        return new Response("Site updated successfully", true).toJson();
    }

    /**
     * @param json serialized {@link Site#getLookupObject(String)}
     */
    public String getSite(String json){
        Site site = JsonUtils.deserialize(json, Site.class);
        try{
            site = sitesController.getSite(site.address());
        }catch(Exception e){
            return Response.getErrorResponse(e).toJson();
        }
        return new Response("Site found successfully", true, site).toJson();
    }

    public String getAllSites(){
        try {
            return new Response("Sites found successfully", true, sitesController.getAllSites()).toJson();
        } catch (TransportException e) {
            return Response.getErrorResponse(e).toJson();
        }
    }

}