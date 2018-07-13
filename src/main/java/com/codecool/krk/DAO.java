package com.codecool.krk;

import java.util.ArrayList;
import java.util.List;

public class DAO {

    private List<User> userData;

    public DAO() {
        userData = new ArrayList<>();
    }

    public List<User> getUserData() {
        return userData;
    }
}
