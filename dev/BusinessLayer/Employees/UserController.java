package dev.BusinessLayer.Employees;

import java.util.*;

public class UserController {

    private Map<String,User> users;
    private static UserController instance;

    private UserController(){
        users = new HashMap<>();
    }

    public static UserController getInstance() {
        if(instance == null){
            instance = new UserController();
        }
        return instance;
    }

    public User getUser(String username) throws Exception {
        if (users.containsKey(username))
            return users.get(username);
        throw new Exception ("The given username doesn't exist.");
    }

    public void createUser(String username, String password, Employee linkedEmployee) throws Exception {
        if (users.containsKey(username))
            throw new Exception("Username already exists.");
        User user = new User(this, username, password, linkedEmployee, false);
        users.put(username, user);
    }

    public void createManagerUser(String username, String password, Employee linkedEmployee) throws Exception {
        if (users.containsKey(username))
            throw new Exception("Username already exists.");
        User user = new User(this, username, password, linkedEmployee, true);
        users.put(username,user);
    }

    public void login(String username, String password) throws Exception {
        User user = getUser(username);
        boolean success = user.login(password);
        if(!success)
            throw new Exception("Invalid password.");
    }

    public void logout(String username) throws Exception {
        getUser(username).logout();
    }

    public boolean isLoggedIn(String username) throws Exception {
        return getUser(username).isLoggedIn();
    }
}