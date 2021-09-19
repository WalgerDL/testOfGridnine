package com.gridnine.testing;

import com.gridnine.testing.dataBase.FlightsDB;
import com.gridnine.testing.dataBase.DataBaseImpl;
import com.gridnine.testing.utils.FlightsFilter;
import com.gridnine.testing.domain.Flight;
import com.gridnine.testing.utils.FlightsFilterBuilder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    private static String listToString(List<?> list) {
        return list.stream()
                .map(element -> element.toString() + "\r\n")
                .collect(Collectors.joining());
    }

    public static void main(String[] args) {
        long currentEpoch = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        List<Flight> flightList = FlightsDB.getInstance(new DataBaseImpl()).getAll();
        FlightsFilter currentTimeFlightsFilter = new FlightsFilterBuilder().departure().less(currentEpoch).build();
        FlightsFilter invalidFlightsFilter = new FlightsFilterBuilder().removeInvalidFlights().build();
        FlightsFilter idleFlightsFilter = new FlightsFilterBuilder().idleOnTheGround().greater(Duration.ofHours(2).toSeconds()).build();
        System.out.println("[Текущее время: " + LocalDateTime.now() + "]");
        System.out.println("--------------------------------Все вылеты--------------------------------\r\n" + listToString(flightList));
        System.out.println("--------------------Вылеты до текущего момента времени--------------------\r\n" + listToString(currentTimeFlightsFilter.filter(flightList)));
        System.out.println("------------Без сегментов с датой прилёта раньше даты вылета--------------\r\n" + listToString(invalidFlightsFilter.filter(flightList)));
        System.out.println("----------Общее время, проведённое на земле превышает два часа------------\r\n" + listToString(idleFlightsFilter.filter(flightList)));
    }
}
