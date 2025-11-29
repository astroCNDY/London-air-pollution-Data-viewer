import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The StatistcsApp class displays a chart of the pollution over time.
 * you can swap between the different pollutants, get the average pollution level and find the highest pollution value
 * 
 *
 * @author Ahmet Deha Kayaturk, Olivier Burger
 * @version 1
 */
public class StatisticsApp {

    private String pollutant = "NO2";
    private FileLoader fileLoader = new FileLoader();
    private LineChart<Number, Number> chart;

    //Map coordinates
    private int leftX = 510394; //Left edge of map
    private int rightX = 553297; //Right edge of map
    private int bottomY = 168504; //Bottom edge of map
    private int topY = 193305; //Top edge of map
    
    public StatisticsApp() {
        
    }
    
    private String getFileFormatPollutant(String uiPollutant) {
        if (uiPollutant.equals("PM10")) {
            return "pm10";
        } else if (uiPollutant.equals("PM2.5")) {
            return "pm2.5";
        }
        return uiPollutant; 
    }
    
     private String getDisplayPollutant(String filePollutant) {
        if (filePollutant.equals("pm10")) {
            return "PM10";
        } else if (filePollutant.equals("pm2.5")) {
            return "PM2.5";
        }
        return filePollutant; 
    }
    
    /**
     * creates the control panel of the chart
     */
    public VBox createControlPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: white; -fx-border-color: gray;");
        
        Label titleLabel = new Label("Statistics");
        titleLabel.setStyle("-fx-font-weight: bold;");

        Button avgButton = new Button("Average");
        Button highestButton = new Button("Highest Levels");

        avgButton.setOnAction(e -> returnAverage());
        highestButton.setOnAction(e -> showHighestLevels());

        HBox buttonBox = new HBox(10, avgButton, highestButton);
        buttonBox.setStyle("-fx-padding: 10; -fx-alignment: center;");
        
        //Pollutant selection
        Label pollutantLabel = new Label("Select Pollutatnt:");
        ComboBox<String> pollutantComboBox = new ComboBox<>();
        pollutantComboBox.getItems().addAll("NO2", "PM10", "PM2.5");
        pollutantComboBox.setValue(pollutant);
        pollutantComboBox.setOnAction(e -> {
            String selectedValue = pollutantComboBox.getValue();
            pollutant = getFileFormatPollutant(selectedValue);
            
            updateChart();
        });
        
        panel.getChildren().addAll(titleLabel, pollutantLabel, pollutantComboBox, buttonBox);
        
        return panel;
    }
    
    /**
     * creates the Vbox that holds the chart
     */
    public VBox createChart() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: white; -fx-border-color: gray;");
        chart = generateChart();
        panel.getChildren().add(chart);
        
        return panel;
    }
    
    /**
     * makes sure that the dataPoint is not missing, not smaller than 0 and within our map
     */
    private boolean isValidDataPoint(DataPoint point) {
        if (point == null) {
            return false;
        }
        
        if (point.value() < 0) {
            return false;
        }
        
        if (!isInMap(point.x(), point.y())) {
            return false;
        }
        return true;
    }
    
    /**
     * Calculate average pollution from valid data points only
     */
    private double calculateValidAverage(DataSet dataSet) {
        if (dataSet == null) {
            return 0.0;
        }
        
        List<DataPoint> allPoints = dataSet.getData();
        double sum = 0.0;
        int count = 0;
        
        for (DataPoint point : allPoints) {
            if (isValidDataPoint(point)) {
                sum += point.value();
                count++;
            }
        }
        return count > 0 ? sum / count : 0.0;
    }
    
    /**
     * Find highest valid pollution point
     */
    private DataPoint findValidHighestPoint(DataSet dataSet) {
        if (dataSet == null) {
            return null;
        }
        
        List<DataPoint> allPoints = dataSet.getData();
        DataPoint highest = null;
        
        for (DataPoint point : allPoints) {
            if (isValidDataPoint(point)) {
                if (highest == null || point.value() > highest.value()) {
                    highest = point;
                }
            }
        }
        return highest;
    }
    
    /**
     * creates a line chart
     */
    public LineChart<Number, Number> generateChart() {
        NumberAxis xAxis = new NumberAxis(2018, 2023, 1);
        NumberAxis yAxis = new NumberAxis(0, 50, 10);
        
        xAxis.setLabel("Years");
        xAxis.setAutoRanging(false);
        xAxis.setForceZeroInRange(false);
        
        //this removes the "," from the years in the x axis
        xAxis.setTickLabelFormatter(new javafx.util.StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                return String.valueOf(object.intValue());
            }
            
            @Override
            public Number fromString(String string) {
                try {
                    return Integer.parseInt(string);
                }
                catch (Exception e) {
                    return 0;
                }
            }
        });
        
        yAxis.setLabel("Pollution Level (µg/m³)");
        yAxis.setAutoRanging(false);

        LineChart<Number, Number> newChart = new LineChart<>(xAxis, yAxis);
        newChart.setTitle("Pollution Trends");
        newChart.setAnimated(false);
        
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Air Quality Index");
        System.out.println("Generating chart for: " + pollutant);
        
        double [] values = new double[6];
        String[] years = {"2018", "2019", "2020", "2021", "2022", "2023"};
        
        for (int i = 0; i < years.length; i++) {
            DataSet dataSet = fileLoader.loadPollutionData(pollutant, years[i]);
            values[i] = calculateValidAverage(dataSet);
            series.getData().add(new XYChart.Data<>(Integer.parseInt(years[i]), values[i]));
        }

        newChart.getData().add(series);
        
        double maxValue = 0;
        for (double val : values) {
            if (val > maxValue) maxValue = val;
        }
        
        double upperBound = Math.ceil((maxValue * 1.2) /10) * 10;
        ((NumberAxis)newChart.getYAxis()).setUpperBound(upperBound);
        ((NumberAxis)newChart.getYAxis()).setTickUnit(upperBound / 10);
        
        return newChart;
    }

    /**
     * updates the chart with new data
     */
    public void updateChart() {
        
        // Get the current X and Y axes from the chart
        NumberAxis xAxis = (NumberAxis) chart.getXAxis();
        NumberAxis yAxis = (NumberAxis) chart.getYAxis();
        
        
        // Ensure the axis settings remain fixed
        xAxis.setLowerBound(2018);
        xAxis.setUpperBound(2023);
        xAxis.setTickUnit(1);
        xAxis.setAutoRanging(false);
        xAxis.setForceZeroInRange(false);
        
        
        //this removes the "," from the years in the x axis
        xAxis.setTickLabelFormatter(new javafx.util.StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                return String.valueOf(object.intValue());
            }

            @Override
            public Number fromString(String string) {
                try {
                    return Integer.parseInt(string);
                } catch (Exception e) {
                    return 0;
                }
            }
        });
        
        chart.getData().clear(); // Clear the existing data
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(getPollutantDisplayName(pollutant) + " Air Quality Index");

        System.out.println("Updating chart for pollutant: " + pollutant);
        
        double[] values = new double[6];
        String[] years = {"2018", "2019", "2020", "2021", "2022", "2023"};
        
        for (int i  = 0; i < years.length; i++) {
        DataSet dataSet = fileLoader.loadPollutionData(pollutant, years[i]);
        values[i] = calculateValidAverage(dataSet);
        series.getData().add(new XYChart.Data<>(Integer.parseInt(years[i]), values[i]));
        }
    
        chart.getData().add(series);
        chart.setTitle(pollutant + " Pollution Trends");
        
        // Calculate good Y-axis scale based on data
        double maxValue = 0;
        for (double val : values) {
            if (val > maxValue) maxValue = val;
        }
        
        // Add 20% padding to max value and round up to nearest 10
        double upperBound = Math.ceil((maxValue * 1.2) / 10) * 10;
        yAxis.setUpperBound(upperBound);
        yAxis.setTickUnit(upperBound / 10);
    }
    
    /**
     * Displays an error message if something goes wrong
     */
    private void showErrorAlert(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Data Loading Error");
        alert.setHeaderText("No Data Available");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Get display name for pollutant for labels and titles
     */
    private String getPollutantDisplayName(String pollutant) {
        if ("pm10".equalsIgnoreCase(pollutant)) {
            return "PM10";
        }
        else if ("pm2.5".equalsIgnoreCase(pollutant)) {
            return "PM2.5";
        }
        else {
            return pollutant.toUpperCase();
        }
    }
    
    /**
     * returns the average pollution level over all years
     */
    public void returnAverage() {
        List<String> cache = new ArrayList<>();
        Collections.addAll(cache, "2018", "2019", "2020", "2021", "2022", "2023");
        double total = 0;
        int validYears = 0;
        
        for (String year : cache) {
            DataSet dataSet = fileLoader.loadPollutionData(pollutant, year);
            double value = calculateValidAverage(dataSet);
            
            if (value > 0) {
                total += value;
                validYears++;
            }
        }
        double average = validYears > 0 ? total / validYears : 0;

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(getPollutantDisplayName(pollutant) + " Average Pollution Level");
        alert.setHeaderText("Data Summary");
        alert.setContentText(String.format("The average pollution level from 2018 to 2023 is: %.2f µg/m³", average));
        alert.showAndWait();
    }
    
    /**
     * returns the highest pollution level over all years
     */
    public void showHighestLevels() {
        List<String> cache2 = new ArrayList<>();
        Collections.addAll(cache2, "2018", "2019", "2020", "2021", "2022", "2023");
        DataPoint highest = null;
        String highestYear = "";
        
        for (String year : cache2) {
            DataSet dataSet = fileLoader.loadPollutionData(pollutant, year);
            DataPoint potential = findValidHighestPoint(dataSet);
            if (potential != null && (highest == null || potential.value() > highest.value())) {
                highest = potential;
                highestYear = year;
            }
        }
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(getPollutantDisplayName(pollutant) + " Highest Pollution Level");
        alert.setHeaderText("Data Summary");
        if (highest != null) {
            alert.setContentText(String.format("The highest pollution level from 2018 to 2023 was: %.2f µg/m³ at location (%.2f, %.2f)", highest.value(), highestYear, highest.x(), highest.y()));
        } else {
            alert.setContentText("No data available for the highest pollution level.");
        }
        alert.showAndWait();
    }
    
    /**
     * Check if point is in map boundaries
     */
    private boolean isInMap(int x, int y) {
        return x >= leftX && x <= rightX && y >= bottomY && y <= topY;
    }
}