import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.List;

public class DataSetTest {

    @Test
    public void testAddDataAndGetData() {
        DataSet ds = new DataSet("NO2", "2023", "Metric", "ug/m3"); 

        ds.addData(new String[]{"123", "511000", "170000", "42.5"});

        List<DataPoint> points = ds.getData();
        assertEquals(1, points.size());

        DataPoint dp = points.get(0);
        assertEquals(123, dp.gridCode());
        assertEquals(511000, dp.x());
        assertEquals(170000, dp.y());
        assertEquals(42.5, dp.value(), 0.001);
    }

    @Test
    public void testAverageCalculationSkipsInvalidValues() {
        DataSet ds = new DataSet("PM10", "2021", "Metric", "ug/m3");

        ds.addData(new String[]{"101", "510000", "170000", "10"});
        ds.addData(new String[]{"102", "510001", "170001", "-1"}); 
        ds.addData(new String[]{"103", "510002", "170002", "20"});

        double avg = ds.CalculateValidAverage();
        assertEquals(15.0, avg, 0.001);
    }

    @Test
    public void testGetHighestDataPoint() {
        DataSet ds = new DataSet("NO2", "2022", "Metric", "ug/m3");

        ds.addData(new String[]{"201", "510000", "170000", "40"});
        ds.addData(new String[]{"202", "510001", "170001", "45"});
        ds.addData(new String[]{"203", "510002", "170002", "30"});

        DataPoint highest = ds.getHighestDataPoint();
        assertNotNull(highest);
        assertEquals(45.0, highest.value(), 0.001);
        assertEquals(202, highest.gridCode());
    }
}
