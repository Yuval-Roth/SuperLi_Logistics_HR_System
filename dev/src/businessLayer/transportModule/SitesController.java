package businessLayer.transportModule;

import businessLayer.transportModule.bingApi.Point;
import dataAccessLayer.dalUtils.DalException;
import dataAccessLayer.transportModule.SiteRoute;
import dataAccessLayer.transportModule.SitesDAO;
import dataAccessLayer.transportModule.SitesRoutesDAO;
import javafx.util.Pair;
import objects.transportObjects.Site;
import serviceLayer.employeeModule.Services.EmployeesService;
import utils.transportUtils.TransportException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import static serviceLayer.employeeModule.Services.UserService.TRANSPORT_MANAGER_USERNAME;

/**
 * The `SitesController` class is responsible for managing the sites in the TransportModule.
 * It provides methods for adding, removing, updating, and retrieving sites.
 */
public class SitesController {
    private final SitesDAO dao;
    private final SitesRoutesDAO sitesRoutesDAO;
    private EmployeesService employeesService;
    private final SitesRoutesController distancesController;

    public SitesController(SitesDAO dao, SitesRoutesDAO sitesRoutesDAO, SitesRoutesController distancesController){
        this.dao = dao;
        this.sitesRoutesDAO = sitesRoutesDAO;
        this.distancesController = distancesController;
    }

    public void injectDependencies(EmployeesService employeesService) {
        this.employeesService = employeesService;
    }

    /**
     * Adds a site to the `SitesController`.
     *
     * @param site The site to be added.
     * @throws TransportException If the site already exists.
     */
    public void addSite(Site site) throws TransportException {

        if (siteExists(site.name()) != false) {
            throw new TransportException("Site already exists");
        }

        validateSite(site);

        try {
            List<Site> otherSites = getAllSites();

            // get latitude and longitude
            Point coordinates = distancesController.getCoordinates(site);
            site = new Site(site,
                    coordinates.coordinates()[0],
                    coordinates.coordinates()[1]);
            dao.insert(site);

            if(otherSites.isEmpty() == false) {
                sitesRoutesDAO.insertAll(distancesController.createDistanceObjects(site,otherSites));
            } else {
                sitesRoutesDAO.insert(new SiteRoute(site.name(),site.name(),0,0));
            }
            if(site.siteType() == Site.SiteType.BRANCH){
                employeesService.createBranch(TRANSPORT_MANAGER_USERNAME,site.name());
            }

        } catch (DalException e) {
            throw new TransportException(e.getMessage(),e);
        }
    }

    private void validateSite(Site site) throws TransportException {
        try {
            if(dao.isAddressUnique(site.address()) == false){
                throw new TransportException("Site address already exists");
            }
        } catch (DalException e) {
            throw new TransportException(e.getMessage(),e);
        }
    }

    /**
     * Removes a site from the `SitesController`.
     *
     * @param name The name of the site to be removed.
     * @throws TransportException If the site is not found.
     */
    public void removeSite(String name) throws TransportException {
        if (siteExists(name) == false) {
            throw new TransportException("Site not found");
        }

        try {
            dao.delete(Site.getLookupObject(name));
        } catch (DalException e) {
            throw new TransportException(e.getMessage(),e);
        }
    }

    /**
     * Retrieves a site from the `SitesController`.
     *
     * @param name The name of the site to be retrieved.
     * @return The retrieved site.
     * @throws TransportException If the site is not found.
     */
    public Site getSite(String name) throws TransportException {
        if (siteExists(name) == false) {
            throw new TransportException("Site not found");
        }

        try {
            return dao.select(Site.getLookupObject(name));
        } catch (DalException e) {
            throw new TransportException(e.getMessage(),e);
        }
    }

    /**
     * Updates a site in the `SitesController`.
     *
     * @param address The name of the site to be updated.
     * @param newSite The updated site.
     * @throws TransportException If the site is not found.
     */
    public void updateSite(String address, Site newSite) throws TransportException{
        if(siteExists(address) == false) {
            throw new TransportException("Site not found");
        }

        try {
            dao.update(newSite);
        } catch (DalException e) {
            throw new TransportException(e.getMessage(),e);
        }
    }

    /**
     * Retrieves all sites from the `SitesController`.
     *
     * @return A linked list of all sites.
     */
    public List<Site> getAllSites() throws TransportException {
        try {
            return dao.selectAll();
        } catch (DalException e) {
            throw new TransportException(e.getMessage(),e);
        }
    }

    public boolean siteExists(String name) throws TransportException{
        Site lookupObject = Site.getLookupObject(name);
        try {
            return dao.exists(lookupObject);
        } catch (DalException e) {
            throw new TransportException(e.getMessage(),e);
        }
    }

    public Map<Pair<String,String>,Double> buildSitesTravelTimes(List<String> route) throws TransportException {

        HashMap<Pair<String,String>,Double> distances = new HashMap<>();
        ListIterator<String> destinationsIterator = route.listIterator();
        String curr = destinationsIterator.next();
        String next;

        // map distances between following sites
        while (destinationsIterator.hasNext()) {
            next = destinationsIterator.next();

            String currAddress = getSite(curr).address();
            String nextAddress = getSite(next).address();

            SiteRoute lookUpObject = SiteRoute.getLookupObject(currAddress,nextAddress);
            double distance;
            try {
                distance = sitesRoutesDAO.select(lookUpObject).duration();
            } catch (DalException e) {
                throw new TransportException(e.getMessage(),e);
            }
            distances.put(new Pair<>(curr,next),distance);
            curr = next;
        }
        return distances;
    }

    /**
     * this method is used only for the first time the system is loaded.
     * this method assumes that there are no sites in the system.
     */
    public void addAllSitesFirstTimeSystemLoad(List<Site> sites) throws TransportException {

        try {
            // get latitude and longitude
            sites = sites.stream().map(site -> {
                try {
                    Point p = distancesController.getCoordinates(site);
                    return new Site(site, p.latitude(), p.longitude());
                } catch (TransportException e) {
                    throw new RuntimeException(e);
                }
            }).toList();

            sites.forEach(site -> {
                try {
                    dao.insert(site);
                } catch (DalException e) {
                    throw new RuntimeException(e);
                }
            });
            List<SiteRoute> distanceObjects = distancesController.createAllDistanceObjectsFirstTimeLoad(sites);
            sitesRoutesDAO.insertAll(distanceObjects);

            for(Site site : sites){
                if(site.siteType() == Site.SiteType.BRANCH){
                    employeesService.createBranch(TRANSPORT_MANAGER_USERNAME,site.name());
                }
            }
        } catch (DalException | IOException e) {
            throw new TransportException(e.getMessage(),e);
        }

    }
}
