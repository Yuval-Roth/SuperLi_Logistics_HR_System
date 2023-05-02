package dataAccessLayer.transportModule;

import dataAccessLayer.dalUtils.DalException;
import dataAccessLayer.dalUtils.OfflineResultSet;
import dataAccessLayer.transportModule.abstracts.DAO;
import objects.transportObjects.Truck;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class TrucksDAO extends DAO<Truck> {

    private static final String[] types = new String[]{"TEXT", "TEXT", "INTEGER", "INTEGER", "TEXT"};
    private static final String[] primary_keys = {"id"};

    public TrucksDAO() throws DalException {
        super("trucks",
                types,
                primary_keys,
                "id",
                "model",
                "base_weight",
                "max_weight",
                "cooling_capacity");
        initTable();
    }

    public TrucksDAO(String dbName) throws DalException {
        super(dbName,
                "trucks",
                types,
                primary_keys,
                "id",
                "model",
                "base_weight",
                "max_weight",
                "cooling_capacity");
        initTable();
    }

    /**
     * @param object@return the object with the given identifier
     * @throws DalException if an error occurred while trying to select the object
     */
    @Override
    public Truck select(Truck object) throws DalException {

        if(cache.contains(object)) {
            return cache.get(object);
        }

        String query = String.format("SELECT * FROM %s WHERE id = '%s';", TABLE_NAME, object.id());
        OfflineResultSet resultSet;
        try {
            resultSet = cursor.executeRead(query);
        } catch (SQLException e) {
            throw new DalException("Failed to select Truck", e);
        }
        if (resultSet.next()) {
            Truck selected = getObjectFromResultSet(resultSet);
            cache.put(selected);
            return selected;
        } else {
            throw new DalException("No truck with id " + object.id() + " was found");
        }
    }

    /**
     * @return All the objects in the table
     * @throws DalException if an error occurred while trying to select the objects
     */
    @Override
    public List<Truck> selectAll() throws DalException {
        String query = String.format("SELECT * FROM %s;", TABLE_NAME);
        OfflineResultSet resultSet;
        try {
            resultSet = cursor.executeRead(query);
        } catch (SQLException e) {
            throw new DalException("Failed to select all Trucks", e);
        }
        List<Truck> trucks = new LinkedList<>();
        while (resultSet.next()) {
            trucks.add(getObjectFromResultSet(resultSet));
        }
        cache.putAll(trucks);
        return trucks;
    }

    /**
     * @param object - the object to insert
     * @throws DalException if an error occurred while trying to insert the object
     */
    @Override
    public void insert(Truck object) throws DalException {
        String query = String.format("INSERT INTO %s VALUES ('%s', '%s', %d, %d, '%s');",
                TABLE_NAME,
                object.id(),
                object.model(),
                object.baseWeight(),
                object.maxWeight(),
                object.coolingCapacity().toString());
        try {
            if(cursor.executeWrite(query) == 1){
                cache.put(object);
            } else {
                throw new RuntimeException("Unexpected error while trying to insert Truck");
            }
        } catch (SQLException e) {
            throw new DalException("Failed to insert Truck", e);
        }
    }

    /**
     * @param object - the object to update
     * @throws DalException if an error occurred while trying to update the object
     */
    @Override
    public void update(Truck object) throws DalException {
        String query = String.format("UPDATE %s SET model = '%s', base_weight = %d, max_weight = %d, cooling_capacity = '%s' WHERE id = '%s';",
                TABLE_NAME,
                object.model(),
                object.baseWeight(),
                object.maxWeight(),
                object.coolingCapacity().toString(),
                object.id());
        try {
            if(cursor.executeWrite(query) == 1){
                cache.put(object);
            } else {
                throw new DalException("No truck with id " + object.id() + " was found");
            }
        } catch (SQLException e) {
            throw new DalException("Failed to update Truck", e);
        }
    }

    /**
     * @param object@throws DalException if an error occurred while trying to delete the object
     */
    @Override
    public void delete(Truck object) throws DalException {
        String query = String.format("DELETE FROM %s WHERE id = '%s';", TABLE_NAME, object.id());
        try {
            if(cursor.executeWrite(query) == 1){
                cache.remove(object);
            } else {
                throw new DalException("No truck with id " + object.id() + " was found");
            }
        } catch (SQLException e) {
            throw new DalException("Failed to delete Truck", e);
        }
    }

    @Override
    public boolean exists(Truck object) throws DalException {

        if(cache.contains(object)) {
            return true;
        }

        String query = String.format("SELECT * FROM %s WHERE id = '%s';", TABLE_NAME, object.id());
        OfflineResultSet resultSet;
        try {
            resultSet = cursor.executeRead(query);
            if(resultSet.next()) {
                Truck selected = getObjectFromResultSet(resultSet);
                cache.put(selected);
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            throw new DalException("Failed to check if Truck exists", e);
        }
    }

    protected Truck getObjectFromResultSet(OfflineResultSet resultSet) {
        return new Truck(
                resultSet.getString("id"),
                resultSet.getString("model"),
                resultSet.getInt("base_weight"),
                resultSet.getInt("max_weight"),
                Truck.CoolingCapacity.valueOf(resultSet.getString("cooling_capacity"))
        );
    }
}