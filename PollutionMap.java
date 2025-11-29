import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.image.ImageView;
import java.util.*;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

/**
 * The PollutionMap class displays pollution data on a map of London.
 * It is shown as colored circles on the map and lets users 
 * select different types of pollution data.
 *
 * @author Olivier Burger
 * @version 1
 */
public class PollutionMap
{
    private ImageView mapView;
    private Canvas canvas;
    private String pollutant = "NO2";
    private String year = "2023";
    
    private FileLoader fileLoader = new FileLoader();
    
    //Map coordinates
    private int leftX = 510394; //Left edge of map
    private int rightX = 553297; //Right edge of map
    private int bottomY = 168504; //Bottom edge of map
    private int topY = 193305; //Top edge of map
    
    private Tooltip dataTooltip = new Tooltip();
    private boolean tooltipVisible = false;
    private boolean tooltipLocked = false;
    private Map<String, DataPoint> dataPointCache = new HashMap<>();
    private String lockedPositionKey = null;
    
    

    /**
     * Create a new PollutionMap
     * @param mapView The ImageView showing the london map
     */
    public PollutionMap(ImageView mapView)
    {
       this.mapView = mapView;
       
       double width = mapView.getFitWidth();
       double height = width / mapView.getImage().getWidth() * mapView.getImage().getHeight();
       this.canvas = new Canvas(width, height);
       
       setupMouseEvents();
       loadAndShowData();
    }
    
    private void setupMouseEvents() {
          dataTooltip.setShowDelay(Duration.millis(100));
          dataTooltip.setHideDelay(Duration.millis(200));
          
          canvas.setOnMouseMoved(this::handleMouseMove);
          canvas.setOnMouseClicked(this::handleMouseClick);
          
          canvas.setOnMouseExited(e -> {
              if (!tooltipLocked) {
                  hideTooltip();
              }
          });
    }
    
    /**
     * Handle mouse movement to show tooltips
     */
    private void handleMouseMove(MouseEvent event) {
        if (tooltipLocked) {
            return;
        }
        
        double mouseX = event.getX();
        double mouseY = event.getY();
        
        String key = getDataPointKeyAt(mouseX, mouseY);
        
        if (key != null) {
            DataPoint point = dataPointCache.get(key);
            
            String tooltipText = String.format("%s: %.2f\nLocation: %d, %d", pollutant, point.value(), point.x(), point.y());
            
            dataTooltip.setText(tooltipText);
            
            if (!tooltipVisible) {
                dataTooltip.show(canvas, event.getScreenX(), event.getScreenY() + 15);
                tooltipVisible = true;
            }
            else {
                dataTooltip.setAnchorX(event.getScreenX());
                dataTooltip.setAnchorY(event.getScreenY() + 15);
            }
        }
        else {
            if (!tooltipLocked) {
                hideTooltip();
            }
        }
    }
    
    /**
     * Handle mouse clicks to lock/unlock tooltips
     */
    private void handleMouseClick(MouseEvent event) {
        double mouseX = event.getX();
        double mouseY = event.getY();
        
        String key = getDataPointKeyAt(mouseX, mouseY);
        
        if (key == null) {
            if (tooltipLocked) {
                tooltipLocked = false;
                lockedPositionKey = null;
                hideTooltip();
            }
            return;
        }
        if (tooltipLocked && key.equals(lockedPositionKey)) {
            tooltipLocked = false;
            lockedPositionKey = null;
            hideTooltip();
            return;
        }
        
        if (tooltipLocked) {
            tooltipLocked = false;
            lockedPositionKey = null;
            hideTooltip();
        }
        
        
        lockTooltip(key, event.getScreenX(), event.getScreenY());
        
    }
    
    /**
     * Lock tooltip to position
     */
    private void lockTooltip(String key, double screenX, double screenY) {
        DataPoint point = dataPointCache.get(key);
        String tooltipText = String.format("%s: %.2f\nLocation: %d, %d", pollutant, point.value(), point.x(), point.y());
        dataTooltip.setText(tooltipText);
        
        if (tooltipVisible) {
            dataTooltip.hide();
        }
        
        dataTooltip.show(canvas, screenX, screenY + 15);
        tooltipVisible = true;
        tooltipLocked = true;
        lockedPositionKey = key;
    }
    
    /**
     * Hide the tooltip
     */
    private void hideTooltip() {
        if (tooltipVisible && !tooltipLocked) {
            dataTooltip.hide();
            tooltipVisible = false;
        }
    }
    
    /**
     * Generate a key for a position on the canvas
     */
    private String getPositionKey(double x, double y) {
        return (int)x + "," + (int)y;
    }
    
    /**
     * Check if point (x,y) is within drawn data point
     */
    private boolean isWithinDataPoint(double x, double y) {
        for (Map.Entry<String, DataPoint> entry : dataPointCache.entrySet()) {
            String[] coords = entry.getKey().split(",");
            int dpX = Integer.parseInt(coords[0]);
            int dpY = Integer.parseInt(coords[1]);
            
            if (x >= dpX-2 && x <= dpX-2+15 && y >= dpY -2 && y <= dpY-2+15) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get the closest data point key to the given coordinates
     */
    private String getDataPointKeyAt(double x, double y) {
        String closestKey = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Map.Entry<String, DataPoint> entry : dataPointCache.entrySet()) {
            String[] coords = entry.getKey().split(",");
            int dpX = Integer.parseInt(coords[0]);
            int dpY = Integer.parseInt(coords[1]);
            
            int centerX = dpX-2 + 7;
            int centerY = dpY-2 + 7;
            
            if (x >= dpX-2 && x <= dpX-2+15 && y >= dpY-2 && y <= dpY-2+15) {
                double distance = Math.sqrt(Math.pow(centerX - x, 2) + Math.pow(centerY - y, 2));
                if (distance < minDistance) {
                    minDistance = distance;
                    closestKey = entry.getKey();
                }
            }
        }
        
        return closestKey;
    }
    
    /**
     * Get the canvas with the pollution data
     */
    public Canvas getCanvas() {
        return canvas;
    }
    
    
    /**
     * Create a control panel with options
     */
    public VBox createControlPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: white; -fx-border-color: gray;");
        
        Label titleLabel = new Label("Pollution Controls");
        titleLabel.setStyle("-fx-font-weight: bold;");
        
        //Pollutant selection
        Label pollutantLabel = new Label("Select Pollutatnt:");
        ComboBox<String> pollutantComboBox = new ComboBox<>();
        pollutantComboBox.getItems().addAll("NO2", "PM10", "PM2.5");
        pollutantComboBox.setValue(pollutant);
        pollutantComboBox.setOnAction(e -> {
            pollutant = pollutantComboBox.getValue();
            loadAndShowData();
        });
        
        //Year selection
        Label yearLabel = new Label("Select year:");
        ComboBox<String> yearComboBox = new ComboBox<>();
        yearComboBox.getItems().addAll("2018", "2019", "2020", "2021", "2022", "2023");
        yearComboBox.setValue(year);
        yearComboBox.setOnAction(e -> {
            year = yearComboBox.getValue();
            loadAndShowData();
        });
        
        Label legendLabel = new Label("Color Legend:");
        
        HBox legendBox = new HBox(10);
        legendBox.getChildren().addAll(
        createColorBox(Color.GREEN, "Low"),
        createColorBox(Color.YELLOW, "Medium"),
        createColorBox(Color.ORANGE, "High"),
        createColorBox(Color.RED, "Very High"));
        
        panel.getChildren().addAll(titleLabel, pollutantLabel, pollutantComboBox, yearLabel, yearComboBox, legendLabel, legendBox);
        
        return panel;
    }
    
    
    /**
     * Create a colored box with label for the legend
     */
    private HBox createColorBox(Color color, String text) {
        HBox box = new HBox(5);
        
        Pane colorSquare = new Pane();
        colorSquare.setPrefSize(15, 15);
        colorSquare.setStyle(String.format("-fx-background-color: %s; -fx-border-color: black;", toRGBCode(color)));
        
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 10px;");
        
        box.getChildren().addAll(colorSquare, label);
        return box;
    }
    
    /**
     * Convert a Color to CSS RGB string
     */
    private String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X", (int)(color.getRed() * 255), (int)(color.getGreen() * 255), (int)(color.getBlue() * 255));
    }
    
    /**
     * Load data from file and show on canvas
     */
    public void loadAndShowData() {
        dataPointCache.clear();
        resizeCanvas();
        
        
        DataSet dataSet = fileLoader.loadPollutionData(pollutant, year);
        if (dataSet == null) {
            return;
        }
        
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        List<DataPoint> points = dataSet.getData();
        
        for (DataPoint point : points) {
            if (!isInMap(point.x(), point.y())) {
                continue;
            }
            if (point.value() < 0) {
                continue;
            }
             
             
            double[] pos = gridToScreen(point.x(), point.y());
            double screenX = pos[0];
            double screenY = pos[1];
            
            Color color = getColorForValue(point.value());
            
            gc.setFill(color);
            gc.fillRect(screenX-2, screenY-2, 15, 15);
            
            String posKey = getPositionKey(screenX, screenY);
            dataPointCache.put(posKey, point);
        }
        
        
    }
    
    /**
     * Check if point is in map boundaries
     */
    private boolean isInMap(int x, int y) {
        return x >= leftX && x <= rightX && y >= bottomY && y <= topY;
    }
    
    /**
     * Convert grid coordinates to screen position
     */
    private double[] gridToScreen(int x, int y) {
        double screenX = (x - leftX) * canvas.getWidth() / (rightX - leftX);
        double screenY = canvas.getHeight() - (y - bottomY) * canvas.getHeight() / (topY - bottomY);
        return new double [] {screenX, screenY};
    }
    
    private Color getColorForValue(double value) {
        double low, medium, high;
        
        if (pollutant.equals("PM10")) {
            low = 15;
            medium = 30;
            high = 45;
        }
        else if (pollutant.equals("PM2.5")) {
            low = 10;
            medium = 20;
            high = 30;
        }
        else {
            low = 20;
            medium = 40;
            high = 60;
        }
        
        if (value < low) {
            return Color.rgb(0, 255, 0, 0.4);
        }
        else if (value < medium) {
            return Color.rgb(255, 255, 0, 0.4);
        }
        else if (value < high) {
            return Color.rgb(255, 165, 0, 0.4);
        }
        else {
            return Color.rgb(255, 0, 0, 0.4);
        }
    }
       
    /**
     * Resize the canvas to match the map
     */
    private void resizeCanvas() {
        double width = mapView.getFitWidth();
        double height = width / mapView.getImage().getWidth() * mapView.getImage().getHeight();
        
        canvas.setWidth(width);
        canvas.setHeight(height);
    }
    
    /**
     * Update when the map size changes
     */
    public void updateMapSize() {
        resizeCanvas();
        loadAndShowData();
    }
}