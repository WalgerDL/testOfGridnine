package com.gridnine.testing.dataBase;

import com.gridnine.testing.interfaces.DataBase;
import com.gridnine.testing.domain.Flight;
import com.gridnine.testing.domain.FlightBuilder;

import java.util.List;

public class DataBaseImpl implements DataBase<Flight> {
    private final List<Flight> flightList = FlightBuilder.createFlights();

    @Override
    public List<Flight> getAll() {
        return flightList;
    }
}
