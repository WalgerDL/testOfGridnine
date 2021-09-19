package com.gridnine.testing.utils;

import com.gridnine.testing.interfaces.Filter;
import com.gridnine.testing.domain.Flight;
import com.gridnine.testing.domain.Segment;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Фильтр рейсов. Создан на основе наборов условий. Есть только три набора условий:
 * arrivalStatementsMap - набор условий для времени прибытия,
 * hibitedStatementsMap - набор условий для времени отправления,
 * idleStatementsMap - набор условий простоя на земле.
 * Параметр allowInvalidFlights позволяет исключить из результата полеты, в которых есть сегменты
 * со временем прибытия до времени отправления.
 * Операторы doParallel () иdoSequential позволяют переключать фильтр на использование параллельного
 * или последовательные потоковые потоки для повышения производительности в зависимости от количества данных, полученных на входе фильтра.
 */
public class FlightsFilter implements Filter<List<Flight>> {
    private final Map<Operators, Long> arrivalStatementsMap;
    private final Map<Operators, Long> departureStatementsMap;
    private final Map<Operators, Long> idleStatementsMap;
    private final boolean allowInvalidFlights;
    private boolean useParallelStream = false;
    private boolean invalidFlightsRemoved = false;

    /**
     * @param arrivalStatementsMap   set of conditions for arrival times
     * @param departureStatementsMap set of conditions for departure times
     * @param idleStatementsMap      set of conditions for downtime on the ground
     * @param allowInvalidFlights    allows to exclude from the result invalid flights (departureTime < arrivalTime)
     */
    public FlightsFilter(Map<Operators, Long> arrivalStatementsMap, Map<Operators, Long> departureStatementsMap, Map<Operators, Long> idleStatementsMap, boolean allowInvalidFlights) {
        this.arrivalStatementsMap = arrivalStatementsMap;
        this.departureStatementsMap = departureStatementsMap;
        this.idleStatementsMap = idleStatementsMap;
        this.allowInvalidFlights = allowInvalidFlights;
    }

    private Stream<Flight> getFlightStream(List<Flight> flightList) {
        return useParallelStream ? flightList.parallelStream() : flightList.stream();
    }

    private long getSegmentEpochByType(Segment segment, TypeOfFlight typeOfFlight) {
        switch (typeOfFlight) {
            case ARRIVAL:
                return segment.getArrivalDate().toEpochSecond(ZoneOffset.UTC);
            default:
            case DEPARTURE:
                return segment.getDepartureDate().toEpochSecond(ZoneOffset.UTC);
        }
    }

    private boolean filterStatementResult(Segment segment, long epochTimeToCompare, Operators operators, TypeOfFlight typeOfFlight) {

        if(operators.equals(Operators.EQUALS)){
            return getSegmentEpochByType(segment, typeOfFlight) == epochTimeToCompare;
        }else if (operators.equals(Operators.GREATER_OR_EQUALS)){
            return getSegmentEpochByType(segment, typeOfFlight) >= epochTimeToCompare;
        }else if (operators.equals(Operators.GREATER)){
            return getSegmentEpochByType(segment, typeOfFlight) > epochTimeToCompare;
        }else if (operators.equals(Operators.LESS)){
            return getSegmentEpochByType(segment, typeOfFlight) < epochTimeToCompare;
        }else if (operators.equals(Operators.LESS_OR_EQUALS)){
            return getSegmentEpochByType(segment, typeOfFlight) <= epochTimeToCompare;
        }else return false;

    }

    private boolean isValidSegment(Segment segment) {
        if (!(allowInvalidFlights || invalidFlightsRemoved)) {
            return segment.getArrivalDate().toEpochSecond(ZoneOffset.UTC) < segment.getDepartureDate().toEpochSecond(ZoneOffset.UTC);
        }
        return true;
    }

    private boolean isValidFlight(Flight flight) {
        boolean isValid = false;
        if (allowInvalidFlights && invalidFlightsRemoved) {
            return true;
        }
        for (Segment segment : flight.getSegments()) {
            isValid |= isValidSegment(segment);
        }
        return isValid;
    }

    private boolean isPassedSegment(Segment segment, long epochTimeToCompare, Operators operators, TypeOfFlight filterType) {
        if (!isValidSegment(segment))
            return false;
        return filterStatementResult(segment, epochTimeToCompare, operators, filterType);
    }

    private boolean isAllowedSegmentDiff(long differenceEpochTime, long toleranceSeconds, Operators operators) {

        if(operators.equals(Operators.EQUALS)){
            return differenceEpochTime == toleranceSeconds;
        }else if (operators.equals(Operators.GREATER_OR_EQUALS)){
            return differenceEpochTime >= toleranceSeconds;
        }else if (operators.equals(Operators.GREATER)){
            return differenceEpochTime > toleranceSeconds;
        }else if (operators.equals(Operators.LESS)){
            return differenceEpochTime < toleranceSeconds;
        }else if (operators.equals(Operators.LESS_OR_EQUALS)){
            return differenceEpochTime <= toleranceSeconds;
        }else return false;

    }

    private boolean isAllowedFlightIdleTime(Flight flight, long toleranceSeconds, Operators operators) {
        boolean isPassed = false;
        List<Segment> segmentList = flight.getSegments();
        for (int nextSegmentIndex = 1; nextSegmentIndex <= (segmentList.size() - 1); nextSegmentIndex++) {
            Segment currentSegment = segmentList.get(nextSegmentIndex - 1);
            Segment nextSegment = segmentList.get(nextSegmentIndex);
            long currentArrivalEpoch = currentSegment.getArrivalDate().toEpochSecond(ZoneOffset.UTC);
            long nextDepartureEpoch = nextSegment.getDepartureDate().toEpochSecond(ZoneOffset.UTC);
            isPassed |= (isAllowedSegmentDiff(nextDepartureEpoch - currentArrivalEpoch, toleranceSeconds, operators));
        }
        return isPassed;
    }

    private List<Flight> conditionalFilter(List<Flight> flightList, Map<Operators, Long> conditionMap, TypeOfFlight typeOfFlight) {
        List<Flight> filteredList = new ArrayList<>(flightList);
        for (Map.Entry<Operators, Long> conditions : conditionMap.entrySet()) {
            filteredList = getFlightStream(filteredList)
                    .filter(flight ->
                            flight.getSegments().stream().
                                    anyMatch(segment ->
                                            isPassedSegment(segment, conditions.getValue(), conditions.getKey(), typeOfFlight)))
                    .collect(Collectors.toList());
        }
        invalidFlightsRemoved = true;
        return filteredList;
    }

    private List<Flight> idleFlightsFilter(List<Flight> flightList, Map<Operators, Long> conditionMap) {
        List<Flight> filteredList = new ArrayList<>(flightList);
        for (Map.Entry<Operators, Long> conditions : conditionMap.entrySet()) {
            long toleranceSeconds = conditions.getValue();
            filteredList = getFlightStream(filteredList)
                    .filter(flight -> isAllowedFlightIdleTime(flight, toleranceSeconds, conditions.getKey()))
                    .filter(this::isValidFlight)
                    .collect(Collectors.toList());
        }
        invalidFlightsRemoved = true;
        return filteredList;
    }

    private List<Flight> invalidFlightsFilter(List<Flight> flightList) {
        List<Flight> filteredList = getFlightStream(flightList)
                .filter(flight -> flight.getSegments().stream().noneMatch(this::isValidSegment))
                .collect(Collectors.toList());
        invalidFlightsRemoved = true;
        return filteredList;
    }

    public FlightsFilter doParallel() {
        this.useParallelStream = true;
        return this;
    }

    public FlightsFilter doSequential() {
        this.useParallelStream = false;
        return this;
    }

    @Override
    public List<Flight> filter(final List<Flight> flightList) {
        List<Flight> filteredFlights = new ArrayList<>(flightList);
        if (!arrivalStatementsMap.isEmpty())
            filteredFlights = conditionalFilter(filteredFlights, arrivalStatementsMap, TypeOfFlight.ARRIVAL);
        if (!departureStatementsMap.isEmpty())
            filteredFlights = conditionalFilter(filteredFlights, departureStatementsMap, TypeOfFlight.DEPARTURE);
        if (!idleStatementsMap.isEmpty())
            filteredFlights = idleFlightsFilter(filteredFlights, idleStatementsMap);
        if ((arrivalStatementsMap.isEmpty() && departureStatementsMap.isEmpty() && idleStatementsMap.isEmpty()) || !allowInvalidFlights)
            filteredFlights = invalidFlightsFilter(filteredFlights);
        invalidFlightsRemoved = false;
        return filteredFlights;
    }
}
