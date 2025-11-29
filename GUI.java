import javafx.application.Application;
import javafx.event.*;
import javafx.stage.Stage;
import javafx.scene.layout.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;



import javafx.geometry.Pos;
import javafx.geometry.Insets;

//Added to fix
import javafx.animation.PauseTransition;
import javafx.util.Duration;



/**
 * Write a description of JavaFX class Map here.
 *
 * @author Aditya Chandrasekar, Olivier Burger 
 * @version 1
 */
public class GUI extends Application
{
    private Stage primaryStage;
    private PollutionMap pollutionMap;
    private StatisticsApp statisticsApp;
    /**
     * The start method is the main entry point for every JavaFX application.
     * It is called after the init() method has returned and after
     * the system is ready for the application to begin running.
     *
     * @param  stage the primary stage for this application.
     */
    @Override
    public void start(Stage primaryStage)
    {
        this.primaryStage = primaryStage;
        showWelcomePanel();
        primaryStage.setMaximized(true);

    }
    
    /**
     * Creates the welcome Panel
     */
    private void showWelcomePanel(){
        Label titleLabel = new Label("London Air Pollution Explorer");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");
        
        Label infoLabel = new Label("In this program you can see the air pollution in London, filtered by region and pollutants.");
        infoLabel.setStyle("-fx-font-size: 14px;");
        infoLabel.setWrapText(true);
        
        Label instructionsLabel = new Label(
                "Welcome to the London Air Pollution Explorer!\n" +
                "• Click \"Start Map Explorer\" to load the map of London.\n" +
                "• Use the dropdown menus to select a pollutant (NO2, PM10, PM2.5) and a year (2018–2023).\n" +
                "• Hover over or click on markers to see detailed pollution data for that area.\n" +
                "• Switch to the Statistics view to see summary figures and a trend graph over the years."
        );
        instructionsLabel.setStyle("-fx-font-size: 14px;");
        instructionsLabel.setWrapText(true);
        
        Button startButton = new Button("Start Map Explorer");
        startButton.setPrefWidth(200);
        startButton.setPrefHeight(40);
        startButton.setStyle(
                "-fx-font-size: 16px; " +
                "-fx-background-color: #4285f4; " +   
                "-fx-text-fill: white; " +
                "-fx-padding: 10 20; " +
                "-fx-background-radius: 8px;"
        );
        startButton.setOnAction(e -> showMap());
        
        
        VBox contentBox = new VBox(20, titleLabel, infoLabel, instructionsLabel, startButton);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(40));
        
        BorderPane mainLayout = new BorderPane();
        mainLayout.setCenter(contentBox);
        
        Scene scene = new Scene(mainLayout);
        
        primaryStage.setTitle("Map of London");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    
    /**
     * creates the map panel
     */
    private void showMap(){
        BorderPane root = new BorderPane();

        MenuBar menuBar  = createMenuBar();
        root.setTop(menuBar);

        StackPane mapContainer = new StackPane();

        Image mapImage = new Image("/london.png");
        ImageView mapView = new ImageView(mapImage);
        mapView.setPreserveRatio(true);
        mapView.setFitWidth(800);

        pollutionMap = new PollutionMap(mapView);

        mapContainer.getChildren().addAll(mapView, pollutionMap.getCanvas());

        VBox controlPanel = pollutionMap.createControlPanel();

        root.setCenter(mapContainer);
        root.setLeft(controlPanel);

        Scene scene = new Scene(root);

        scene.widthProperty().addListener((obs, oldVal, newVal) -> {
            double width = newVal.doubleValue() - controlPanel.getWidth();
            mapView.setFitWidth(width);
            pollutionMap.updateMapSize();
        });

        primaryStage.setTitle("London Air Pollution Map");
        primaryStage.setScene(scene);
        //primaryStage.setMaximized(true);
    }

    /**
     * Create the menu bar
     */
    private MenuBar createMenuBar (){
        MenuBar menuBar = new MenuBar();


        Menu fileMenu = new Menu("File");

        MenuItem welcomePageItem = new MenuItem("Welcome Page");
        welcomePageItem.setOnAction(this::welcomePageAction);
        welcomePageItem.setAccelerator(new KeyCodeCombination(KeyCode.B, KeyCombination.SHORTCUT_DOWN));

        MenuItem quitItem = new MenuItem("Quit");
        quitItem.setOnAction(this::quitAction);
        quitItem.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN));

        fileMenu.getItems().addAll(welcomePageItem, quitItem);

        Menu switchViewMenu = new Menu("Switch View");

        MenuItem viewMap = new MenuItem("View Map");
        viewMap.setOnAction(this::viewMapAction);
        viewMap.setAccelerator(new KeyCodeCombination(KeyCode.M, KeyCombination.SHORTCUT_DOWN));

        MenuItem viewStatistics = new MenuItem("View Statistics");
        viewStatistics.setOnAction(this::viewStatisticsAction);
        viewStatistics.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN));

        switchViewMenu.getItems().addAll(viewMap, viewStatistics);
        menuBar.getMenus().addAll(fileMenu, switchViewMenu);

        return menuBar;
    }

    private void viewStatisticsAction(ActionEvent event){
        showStatistics();
    }

    private void viewMapAction(ActionEvent event){
        showMap();
    }
    
    /**
     * creates the statistics panel
     */
    private void showStatistics(){
        double currentWidth = primaryStage.getWidth();
        double currentHeight = primaryStage.getHeight();

        
        primaryStage.setTitle("Pollution Statistics");
        
        statisticsApp = new StatisticsApp();
        BorderPane root = new BorderPane();
        
        MenuBar menuBar  = createMenuBar();
        root.setTop(menuBar);
        

        VBox controlPanel = statisticsApp.createControlPanel();
        VBox chart = statisticsApp.createChart();
        root.setLeft(controlPanel);
        root.setCenter(chart);


        Scene scene = new Scene(root, currentWidth, currentHeight);

        primaryStage.setScene(scene);
        primaryStage.show();

    }
    private void welcomePageAction(ActionEvent event) {
        showWelcomePanel();
    }

    private void quitAction(ActionEvent event) {
        System.exit(0);
    }
}
