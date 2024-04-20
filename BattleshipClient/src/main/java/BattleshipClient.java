import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Shape;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;


import static jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyle.circle;

public class BattleshipClient extends Application{

	private TextField textField;
	private BorderPane mainPane;
	private Button sendButton, sendAllButton, createGroupButton, viewMembersButton, viewGroupsButton;
	private Label label;
	private HashMap<String, Scene> scenes;
	private VBox mainBox;
	private HBox buttonBox;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
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
		imageView.setFitHeight(400); // Set the height of the image (you can adjust this as needed)
		imageView.setFitWidth(500); // Set the width of the image (you can adjust this as needed)

		StackPane imageContainer = new StackPane(imageView);
		StackPane.setAlignment(imageView, Pos.TOP_CENTER); // Aligns the ImageView to the top of the StackPane

		// Adjusting padding to position the image higher
		imageContainer.setPadding(new Insets(100, 0, 0, 0)); // Negative top padding to move it upwards

		// Set the StackPane as the center of the BorderPane
		mainPane.setCenter(imageContainer);

		Button playButton = new Button("PLAY");
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

	private Scene createClientScene() {
		mainBox = new VBox(10);
		label = new Label("not a server");
		textField = new TextField();


		mainPane = new BorderPane();

		ListView<String> listView = new ListView<>();
		mainPane.setCenter(listView);

		mainBox.getChildren().addAll(label, mainPane, textField, buttonBox);
		mainBox.setAlignment(Pos.CENTER);

		return new Scene(mainBox, 800, 600);
	}

}
