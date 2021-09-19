package tests;

import com.gridnine.testing.dataBase.DataBaseImpl;
import com.gridnine.testing.dataBase.FlightsDB;
import com.gridnine.testing.utils.FlightsFilter;
import com.gridnine.testing.domain.Flight;
import com.gridnine.testing.utils.FlightsFilterBuilder;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

public class FilterTest {
    private final List<Flight> flightList = FlightsDB.getInstance(new DataBaseImpl()).getAll();
    private final long threeDaysFromNowTime = flightList.get(0).getSegments().get(0).getArrivalDate().toEpochSecond(ZoneOffset.UTC);

    @Test
    public void arrivalEqFilterTest() {
        int[] resultId = {0, 1, 4, 5};
        FlightsFilter eqFilter = new FlightsFilterBuilder().arrival().equals(threeDaysFromNowTime).build();
        List<Flight> filteredFlights = eqFilter.filter(flightList);
        assertEquals(resultId.length, filteredFlights.size());
        for (int i = 0; i < resultId.length; i++) {
            assertEquals(filteredFlights.get(i).getSegments().get(0).getArrivalDate(), flightList.get(resultId[i]).getSegments().get(0).getArrivalDate());
        }
        System.out.println("arrivalEqFilterTest: " + filteredFlights);
    }

    @Test
    public void arrivalGteFilterTest() {
        int[] resultId = {0, 1, 4, 5};
        FlightsFilter gteFilter = new FlightsFilterBuilder().arrival().greater_or_equals(threeDaysFromNowTime).build();
        List<Flight> filteredFlights = gteFilter.filter(flightList);
        assertEquals(filteredFlights.size(), resultId.length);
        for (int i = 0; i < resultId.length; i++) {
            assertEquals(filteredFlights.get(i).getSegments().get(0).getArrivalDate(), flightList.get(resultId[i]).getSegments().get(0).getArrivalDate());
        }
        System.out.println("arrivalGteFilterTest: " + filteredFlights);
    }

    @Test
    public void arrivalGtFilterTest() {
        int[] resultId = {1, 4, 5};
        FlightsFilter gtFilter = new FlightsFilterBuilder().arrival().greater(threeDaysFromNowTime).build();
        List<Flight> filteredFlights = gtFilter.filter(flightList);
        assertEquals(filteredFlights.size(), resultId.length);
        for (int i = 0; i < resultId.length; i++) {
            assertEquals(filteredFlights.get(i).getSegments().get(0).getArrivalDate(), flightList.get(resultId[i]).getSegments().get(0).getArrivalDate());
        }
        System.out.println("arrivalGtFilterTest: " + filteredFlights);
    }

    @Test
    public void arrivalLtFilterTest() {
        int[] resultId = {2, 3};
        FlightsFilter ltFilter = new FlightsFilterBuilder().arrival().less(threeDaysFromNowTime).build();
        List<Flight> filteredFlights = ltFilter.filter(flightList);
        assertEquals(filteredFlights.size(), resultId.length);
        for (int i = 0; i < resultId.length; i++) {
            assertEquals(filteredFlights.get(i).getSegments().get(0).getArrivalDate(), flightList.get(resultId[i]).getSegments().get(0).getArrivalDate());
        }
        System.out.println("arrivalLtFilterTest: " + filteredFlights);
    }

    @Test
    public void arrivalLteFilterTest() {
        int[] resultId = {0, 1, 2, 3, 4, 5};

        FlightsFilter ltFilter = new FlightsFilterBuilder().arrival().less_or_equals(threeDaysFromNowTime).build();
        List<Flight> filteredFlights = ltFilter.filter(flightList);
        assertEquals(filteredFlights.size(), resultId.length);
        for (int i = 0; i < resultId.length; i++) {
            assertEquals(filteredFlights.get(i).getSegments().get(0).getArrivalDate(), flightList.get(resultId[i]).getSegments().get(0).getArrivalDate());
        }
        System.out.println("arrivalLteFilterTest: " + filteredFlights);
    }

    @Test
    public void combineArriveFilterTest() {
        long ltTime = LocalDateTime.ofEpochSecond(threeDaysFromNowTime, 0, ZoneOffset.UTC).plusHours(5).toEpochSecond(ZoneOffset.UTC);
        long gtTime = threeDaysFromNowTime;
        FlightsFilter ltArriveFilter = new FlightsFilterBuilder().arrival().less(ltTime).greater(gtTime).build();
        List<Flight> filteredFlights = ltArriveFilter.filter(flightList);
        assertEquals( 3,filteredFlights.size());
        assertEquals(flightList.get(1).getSegments().get(1).getArrivalDate(),filteredFlights.get(0).getSegments().get(1).getArrivalDate());
        assertEquals(flightList.get(4).getSegments().get(1).getArrivalDate(),filteredFlights.get(1).getSegments().get(1).getArrivalDate());
        assertEquals(flightList.get(5).getSegments().get(1).getArrivalDate(),filteredFlights.get(2).getSegments().get(1).getArrivalDate());
        System.out.println("combineArriveFilterTest: " + filteredFlights);

        ltTime = threeDaysFromNowTime;
        gtTime = LocalDateTime.ofEpochSecond(threeDaysFromNowTime, 0, ZoneOffset.UTC).plusHours(5).toEpochSecond(ZoneOffset.UTC);
        FlightsFilter wrongArriveFilter = new FlightsFilterBuilder().arrival().less(ltTime).greater(gtTime).build();
        filteredFlights = wrongArriveFilter.filter(flightList);
        assertTrue(filteredFlights.isEmpty());
    }

    @Test
    public void idleFilterTest() {
        long gteHours = Duration.ofHours(2).toSeconds();
        FlightsFilter flightsFilter = new FlightsFilterBuilder().idleOnTheGround().greater_or_equals(gteHours).build();
        List<Flight> filteredFlights = flightsFilter.filter(flightList);
        assertEquals(filteredFlights.get(0), flightList.get(4));
        assertEquals(filteredFlights.get(1), flightList.get(5));
        System.out.println("combineArriveDepartureFilterTest gte=2: " + filteredFlights);

        gteHours = Duration.ofHours(1).toSeconds();
        long ltHours = Duration.ofHours(3).toSeconds();
        flightsFilter = new FlightsFilterBuilder().idleOnTheGround().greater(gteHours).less(ltHours).build();
        filteredFlights = flightsFilter.filter(flightList);
        assertEquals(filteredFlights.get(0), flightList.get(5));
        System.out.println("combineArriveDepartureFilterTest gte=1, lt=3: " + filteredFlights);
    }

    @Test
    public void combineArriveDepartureTest() {
        long iddleGteTime = Duration.ofHours(2).toSeconds();
        FlightsFilter flightsFilter = new FlightsFilterBuilder()
                .departure().greater_or_equals(threeDaysFromNowTime + Duration.ofHours(2).toSeconds()).less(threeDaysFromNowTime + Duration.ofHours(6).toSeconds())
                .arrival().greater(threeDaysFromNowTime + Duration.ofHours(4).toSeconds())
                .idleOnTheGround().greater_or_equals(iddleGteTime)
                .build();
        List<Flight> filteredFlights = flightsFilter.filter(flightList);
        assertEquals(filteredFlights.get(0), flightList.get(5));
    }
    @Test
    void removeOnlyInvalidFlightsTest(){
        FlightsFilter flightsFilter = new FlightsFilterBuilder().removeInvalidFlights().build();
        List<Flight> filteredFlightList = flightsFilter.filter(flightList);
        assertFalse(filteredFlightList.contains(flightList.get(3)));
    }

    @Test
    void wrongUsageFilterBuilderTest(){
        assertThrows(IllegalStateException.class,()->new FlightsFilterBuilder().greater(threeDaysFromNowTime).build());
        assertThrows(IllegalStateException.class,()->new FlightsFilterBuilder().less(threeDaysFromNowTime).build());
        assertThrows(IllegalStateException.class,()->new FlightsFilterBuilder().build());
        assertDoesNotThrow(()->new FlightsFilterBuilder().arrival().greater(threeDaysFromNowTime).build());
    }

    @Test
    void doInParallelTest(){
        long ltTime = LocalDateTime.ofEpochSecond(threeDaysFromNowTime, 0, ZoneOffset.UTC).plusHours(5).toEpochSecond(ZoneOffset.UTC);
        long gtTime = threeDaysFromNowTime;
        FlightsFilter ltArriveFilter = new FlightsFilterBuilder().arrival().less(ltTime).greater(gtTime).build();
        List<Flight> filteredFlights = ltArriveFilter.doParallel().filter(flightList);
        assertEquals( 3,filteredFlights.size());
        assertEquals(flightList.get(1).getSegments().get(1).getArrivalDate(),filteredFlights.get(0).getSegments().get(1).getArrivalDate());
        assertEquals(flightList.get(4).getSegments().get(1).getArrivalDate(),filteredFlights.get(1).getSegments().get(1).getArrivalDate());
        assertEquals(flightList.get(5).getSegments().get(1).getArrivalDate(),filteredFlights.get(2).getSegments().get(1).getArrivalDate());
        System.out.println("combineArriveFilterTest: " + filteredFlights);

        ltTime = threeDaysFromNowTime;
        gtTime = LocalDateTime.ofEpochSecond(threeDaysFromNowTime, 0, ZoneOffset.UTC).plusHours(5).toEpochSecond(ZoneOffset.UTC);
        FlightsFilter wrongArriveFilter = new FlightsFilterBuilder().arrival().less(ltTime).greater(gtTime).build();
        filteredFlights = wrongArriveFilter.filter(flightList);
        assertTrue(filteredFlights.isEmpty());

    }

}
