package com.codecool.krk;

public class User {

    private String login;
    private String pass;
    private String sessionId;

    public User(String login, String pass, String sessionId) {
        this.login = login;
        this.pass = pass;
        this.sessionId = sessionId;
    }

    public User(String login, String pass) {
        this.login = login;
        this.pass = pass;
        this.sessionId = null;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
