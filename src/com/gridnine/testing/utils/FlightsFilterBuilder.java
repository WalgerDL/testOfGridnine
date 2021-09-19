package com.gridnine.testing.utils;

import java.util.EnumMap;
import java.util.Map;

public class FlightsFilterBuilder {
    private final static String ERROR_MESSAGE = "FlightsFilterBuilder: One of the following operators was not used before calling the method: arrival(), departure(), idle()";
    private final Map<Operators, Long> arrivalStatementsMap = new EnumMap<>(Operators.class);
    private final Map<Operators, Long> departureStatementsMap = new EnumMap<>(Operators.class);
    private final Map<Operators, Long> idleStatementsMap = new EnumMap<>(Operators.class);
    private Map<Operators, Long> targetStatementsMap;
    private boolean allowInvalidFlights = true;

    private void stateCheck() {
        if (targetStatementsMap == null)
            throw new IllegalStateException(ERROR_MESSAGE, new NullPointerException("targetStatementsMap is null"));
    }

    public FlightsFilterBuilder equals (long epochTime) {
        stateCheck();
        targetStatementsMap.put(Operators.EQUALS, epochTime);
        return this;
    }

    public FlightsFilterBuilder greater_or_equals(long epochTime) {
        stateCheck();
        targetStatementsMap.put(Operators.GREATER_OR_EQUALS, epochTime);
        return this;
    }

    public FlightsFilterBuilder greater(long epochTime) {
        stateCheck();
        targetStatementsMap.put(Operators.GREATER, epochTime);
        return this;
    }

    public FlightsFilterBuilder less(long epochTime) {
        stateCheck();
        targetStatementsMap.put(Operators.LESS, epochTime);
        return this;
    }

    public FlightsFilterBuilder less_or_equals(long epochTime) {
        stateCheck();
        targetStatementsMap.put(Operators.LESS_OR_EQUALS, epochTime);
        return this;
    }

    public FlightsFilterBuilder idleOnTheGround() {
        targetStatementsMap = idleStatementsMap;
        return this;
    }

    public FlightsFilterBuilder removeInvalidFlights() {
        allowInvalidFlights = false;
        return this;
    }


    public FlightsFilterBuilder arrival() {
        targetStatementsMap = arrivalStatementsMap;
        return this;
    }


    public FlightsFilterBuilder departure() {
        targetStatementsMap = departureStatementsMap;
        return this;
    }

    public FlightsFilter build() {
        if (allowInvalidFlights && arrivalStatementsMap.isEmpty() && departureStatementsMap.isEmpty() && idleStatementsMap.isEmpty())
            throw new IllegalStateException(ERROR_MESSAGE, new NullPointerException("All statement maps is null"));
        return new FlightsFilter(arrivalStatementsMap, departureStatementsMap, idleStatementsMap, allowInvalidFlights);
    }

}
