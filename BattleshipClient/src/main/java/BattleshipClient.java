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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Shape;
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

		Label instructions = new Label("Enter your username:");
		TextField usernameInput = new TextField();
		Button submitButton = new Button("Submit");
		layout.getChildren().addAll(instructions, usernameInput, submitButton);

		return new Scene(layout, 1300, 650);
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
