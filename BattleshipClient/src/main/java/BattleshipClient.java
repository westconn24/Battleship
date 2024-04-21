import java.util.HashMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.image.Image;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class BattleshipClient extends Application{

	private TextField textField;
	private BorderPane mainPane;
	private Button sendButton, sendAllButton, createGroupButton, viewMembersButton, viewGroupsButton;
	private Label label;
	private HashMap<String, Scene> scenes;
	private VBox mainBox;
	private HBox buttonBox;
	private Stage primaryStage;
    Client clientConnection;
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage; // Store primaryStage in the instance variable
		primaryStage.setScene(openScene());
		primaryStage.setTitle("Battleship Client");
		primaryStage.show();
	}

	private Scene openScene() {
		VBox layout = new VBox(10);
		layout.setAlignment(Pos.CENTER);


		mainPane = new BorderPane();
		setBackground(mainPane, "spaceback.jpg");


		Image battle = new Image("battleship_banner.png"); // Replace with your actual image path
		ImageView imageView = new ImageView(battle);
		imageView.setPreserveRatio(true);
		imageView.setFitHeight(500); // Set the height of the image (you can adjust this as needed)
		imageView.setFitWidth(600); // Set the width of the image (you can adjust this as needed)

		StackPane imageContainer = new StackPane(imageView);
		StackPane.setAlignment(imageView, Pos.TOP_CENTER); // Aligns the ImageView to the top of the StackPane

		// Adjusting padding to position the image higher
		imageContainer.setPadding(new Insets(100, 0, 0, 0)); // Negative top padding to move it upwards

		// Set the StackPane as the center of the BorderPane
		mainPane.setCenter(imageContainer);

		Button playButton = new Button("PLAY");
		playButton.setOnAction(e -> primaryStage.setScene(playerChoiceScene(primaryStage)));

		playButton.setStyle(
				"-fx-font-size: 38px; " +  // Larger font size
						"-fx-font-weight: bold; " +  // Bold font
						"-fx-background-color: black; " +  // Background color
						"-fx-text-fill: white; " +  // Text color
						"-fx-padding: 10 20 10 20; " +  // Padding around text
						"-fx-background-radius: 15; " +  // Rounded corners
						"-fx-border-color: #afb0b3; " +  // Border color
						"-fx-border-width: 4; " +  // Border width
						"-fx-border-radius: 5; " +  // Ensure this matches the background radius
						"-fx-font-family: 'Lucida Fax'; " +
						"-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);"  // Shadow effect
		);

		playButton.setMinSize(150, 75);
		HBox buttonContainer = new HBox(playButton);
		buttonContainer.setAlignment(Pos.CENTER); // Aligns the button to the center of the HBox

		// Add a padding to position the button slightly off the bottom edge if needed
		buttonContainer.setPadding(new Insets(-200, 0, 20, 0)); // Adds padding to the bottom

		// Set the HBox with the button as the bottom of the BorderPane
		mainPane.setBottom(buttonContainer);

		return new Scene(mainPane, 1350, 650);
	}
	private void setBackground(BorderPane pane, String imagePath) {
		// Load the image
		Image image = new Image(imagePath);

		if (image.isError()) {
			System.out.println("Error loading image: " + image.getException());
		}
		BackgroundImage backgroundImage = new BackgroundImage(image,
				BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
				new BackgroundSize(1.0, 1.0, true, true, false, true));
		pane.setBackground(new Background(backgroundImage));
	}

	private Scene playerChoiceScene(Stage primaryStage) {
		BorderPane pane = new BorderPane();
		setBackground(pane, "spaceback.jpg");

		// Create label and style it
		Label choiceLabel = new Label("Choose who to play");
		choiceLabel.setFont(new Font("Constantia", 30));
		choiceLabel.setStyle("-fx-text-fill: white; -fx-padding: 10;");


		Button leftButton = new Button("Player");
		leftButton.setOnAction(e -> {

			connectToServer();
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }

			clientConnection.send("getClientCount");
		});
		Button rightButton = new Button("CPU");
		rightButton.setOnAction(e -> {
			primaryStage.setScene(chooseScene());
		});
		leftButton.setStyle(
				"-fx-font-size: 34px; " +  // Larger font size
						"-fx-font-weight: bold; " +  // Bold font
						"-fx-background-color: #03268f; " +  // Background color
						"-fx-text-fill: white; " +  // Text color
						"-fx-padding: 10 20 10 20; " +  // Padding around text
						"-fx-background-radius: 15; " +  // Rounded corners

						"-fx-font-family: 'Constantia'; " +  // Font family
						"-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);"  // Shadow effect
		);
		BorderStroke borderStroke = new BorderStroke(
				Paint.valueOf("black"), // Main border color
				new BorderStrokeStyle(
						StrokeType.OUTSIDE, // Stroke type
						StrokeLineJoin.MITER, // Line join
						StrokeLineCap.SQUARE, // Line cap
						10, // Miter limit
						0, // Dash offset
						null // Dash array
				),
				new CornerRadii(15), // Corner radii
				new BorderWidths(4) // Border widths
		);

		// Set the border to the button
		leftButton.setBorder(new Border(borderStroke));
		rightButton.setBorder(new Border(borderStroke));
		rightButton.setStyle(
				"-fx-font-size: 34px; " +
						"-fx-font-weight: bold; " +
						"-fx-background-color: #26bc1e; " +  // Bright green background for visibility
						"-fx-text-fill: white; " +
						"-fx-padding: 10 20 10 20; " +  // Slightly increased padding
						"-fx-background-radius: 15; " +
						"-fx-font-family: 'Constantia'; " +

						"-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);"
		);
		// Configure buttons with the same style as the PLAY button
		leftButton.setMinSize(150, 75);
		rightButton.setMinSize(150, 75);

		// Add buttons to an HBox
		HBox buttonContainer = new HBox(20); // 20 pixels spacing between buttons
		buttonContainer.getChildren().addAll(leftButton, rightButton);
		buttonContainer.setAlignment(Pos.CENTER);

		// Use a VBox to stack label and button container
		VBox centerContainer = new VBox(10);  // 10 pixels spacing between label and buttons
		centerContainer.getChildren().addAll(choiceLabel, buttonContainer);
		centerContainer.setAlignment(Pos.CENTER);
		centerContainer.setPadding(new Insets(100, 0, 0, 0));  // Move the entire container down by 100 pixels

		// Set the VBox in the center of the BorderPane
		pane.setCenter(centerContainer);

		// Back button at the bottom
		Button backButton = new Button("Back to Main");
		backButton.setOnAction(e -> primaryStage.setScene(openScene()));
		backButton.setStyle(
				"-fx-font-size: 15px; " +  // Larger font size
						"-fx-font-weight: bold; " +  // Bold font
						"-fx-background-color: black; " +  // Background color
						"-fx-text-fill: white; " +  // Text color
						"-fx-padding: 10 20 10 20; " +  // Padding around text
						"-fx-background-radius: 15; " +  // Rounded corners
						"-fx-border-color: #afb0b3; " +  // Border color
						"-fx-border-width: 4; " +  // Border width
						"-fx-border-radius: 5; " +  // Ensure this matches the background radius
						"-fx-font-family: 'Lucida Fax'; " +
						"-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);"  // Shadow effect
		);
		pane.setBottom(backButton);
		BorderPane.setAlignment(backButton, Pos.CENTER);

		return new Scene(pane, 1350, 650);
	}




	private void connectToServer() {
		// Create the client with a callback to handle messages received from the server
		clientConnection = new Client(data -> {
			// Since the data is of type Serializable, ensure proper handling
			if (data != null) {
				if (data.equals("OddCount")) {
					Platform.runLater(() -> {
						primaryStage.setScene(createWaitingScene());
					});
				}
				// Handle other messages if needed
			}
		}, this);

		// Start the client thread to listen for messages from the server
		clientConnection.start();
	}



	Scene createWaitingScene() {
		BorderPane waitingPane = new BorderPane();
		setBackground(waitingPane, "spaceback.jpg");

		Label waitingLabel = new Label("Please wait for an opponent to connect...");
		waitingLabel.setFont(new Font("Arial", 24));
		waitingLabel.setStyle("-fx-text-fill: white;");

		StackPane.setAlignment(waitingLabel, Pos.CENTER);
		waitingPane.setCenter(waitingLabel);

		return new Scene(waitingPane, 1350, 650);
	}
	public void showWaitingScene() {
		Platform.runLater(() -> {
			primaryStage.setScene(createWaitingScene());
		});
	}

	private Scene chooseScene() {
		// Set the background for the pane
		BorderPane pane = new BorderPane();
		setBackground(pane, "spaceback.jpg");

		GridPane grid = new GridPane();
		grid.setPadding(new Insets(10, 10, 10, 10)); // Margin around the grid
		grid.setVgap(5); // Vertical gap between buttons
		grid.setHgap(5); // Horizontal gap between buttons
		grid.setAlignment(Pos.CENTER); // Center alignment for the GridPane within the BorderPane

		// Create buttons and add them to the grid
		for (int row = 0; row < 10; row++) {
			for (int col = 0; col < 10; col++) {
				Button button = new Button();
				button.setPrefSize(45, 45); // Increased size by 15pt
				button.setStyle("-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 2px;"); // Transparent background with a white outline
				int finalRow = row;
				int finalCol = col;
				button.setOnAction(e -> handleButtonAction(finalRow, finalCol));
				grid.add(button, col, row);
			}
		}

		// Adding the grid to the center of the BorderPane
		pane.setCenter(grid);

		// Back button at the bottom
		Button backButton = new Button("Back to Main");
		backButton.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-background-color: black; -fx-text-fill: white; -fx-padding: 10 20 10 20; -fx-background-radius: 15; -fx-border-color: #afb0b3; -fx-border-width: 4; -fx-border-radius: 5; -fx-font-family: 'Lucida Fax'; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");
		backButton.setOnAction(e -> primaryStage.setScene(openScene()));
		pane.setBottom(backButton);
		BorderPane.setAlignment(backButton, Pos.BOTTOM_LEFT);

		return new Scene(pane, 1350, 650);
	}

	private void handleButtonAction(int row, int col) {
		// Implement your action on button click, e.g., placing a ship or making a guess
		System.out.println("Button clicked at row " + row + ", col " + col);
	}


}
