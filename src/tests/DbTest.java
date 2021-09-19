package tests;

import com.gridnine.testing.dataBase.DataBaseImpl;
import com.gridnine.testing.dataBase.FlightsDB;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;


public class DbTest {
    private FlightsDB flightsDao = FlightsDB.getInstance(new DataBaseImpl());

    @Test
    public void getAllTest() {
        assertFalse(flightsDao.getAll().isEmpty());
        System.out.println(flightsDao.getAll());
    }
}
