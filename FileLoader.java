
/**
 * Loads the appropriate files
 *
 * @author Olivier Burger
 * @version 1
 */
public class FileLoader
{
    // instance variables - replace the example below with your own
    private DataLoader dataLoader;

    /**
     * Constructor for objects of class FileLoader
     */
    public FileLoader()
    {
        this.dataLoader = new DataLoader();
    }

    /**
     * Load pollutant data for the specified pollutant and year
     *
     * @param pollutant The pollutant type (NO2, PM10, PM2.5)
     * @param year The year
     */
    public DataSet loadPollutionData(String pollutant, String year) {
        String filePath = getDataFilePath(pollutant, year);
        return dataLoader.loadDataFile(filePath);
    }
    
    /**
     * Get the appropriate file path for the selected pollutant and year
     * @param pollutant The pollutant type (NO2, PM10, PM2.5)
     * @param year The year
     */
    private String getDataFilePath(String pollutant, String year) {
        String folder, filePrefix, suffix;
        
        
        if (pollutant.equalsIgnoreCase("PM2.5") || pollutant.equalsIgnoreCase("pm2.5")) {
            folder = "pm2.5";
            filePrefix = "mappm25";
            suffix = "g";
        }
        else if (pollutant.equalsIgnoreCase("PM10") || pollutant.equalsIgnoreCase("pm10")) {
            folder = "pm10";
            filePrefix = "mappm10";
            suffix = "g";
        }
        else {
            folder = "NO2";
            filePrefix = "mapno2";
            suffix = "";
        }
        
        
        String filePath = String.format("UKAirPollutionData/%s/%s%s%s.csv", folder, filePrefix, year, suffix);
        return filePath;
    }
}
