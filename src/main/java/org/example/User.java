package org.example;

import org.json.JSONObject;

public class User {

    private String uuid;
    private String login;
    private String password;


    public static User fromJsonObject(JSONObject obj) {
        String uuid = obj.getString("uuid");
        String login = obj.getString("login");
        String password = obj.getString("password");
        return new User(uuid, login, password);
    }

    public User(String uuid, String login, String password) {
        this.uuid = uuid;
        this.login = login;
        this.password = password;
    }

    public JSONObject toJsonObject() {
        JSONObject userObj = new JSONObject();
        userObj.put("uuid", uuid);
        userObj.put("login", login);
        userObj.put("password", password);
        return userObj;
    }

    public String getUuid() {
        return uuid;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
