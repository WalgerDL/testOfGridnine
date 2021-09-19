package com.gridnine.testing.dataBase;

import com.gridnine.testing.interfaces.DataBase;
import com.gridnine.testing.domain.Flight;

import java.util.List;

public class FlightsDB {
    private static FlightsDB instance;
    DataBase<Flight> db;

    private FlightsDB(DataBase<Flight> db) {
        this.db = db;
    }

    public static synchronized FlightsDB getInstance(DataBase<Flight> db) {
        if (instance == null) {
            instance = new FlightsDB(db);
        }
        return instance;
    }

    public List<Flight> getAll() {
        return db.getAll();
    }
}
