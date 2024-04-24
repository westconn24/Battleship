import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.image.Image;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.stage.Stage;

class ShipStatus {
	int size;
	int hits;
	boolean isVertical = false;

	int rowTop;
	int colTop;

	ShipStatus(int size, boolean vertical) {
		this.size = size;
		this.hits = 0;
		this.isVertical = vertical;
	}

	boolean isDestroyed() {
		return hits >= size;
	}
}



public class BattleshipClient extends Application {
	boardSetup game;
	private TextField textField;
	private BorderPane mainPane;
	private Button sendButton, sendAllButton, createGroupButton, viewMembersButton, viewGroupsButton;
	private Label label;
	private HashMap<String, Scene> scenes;
	private VBox mainBox;
	private HBox buttonBox;
	private Stage primaryStage;
	Client clientConnection;
	public Ship[] ships = new Ship[5];
	private ShipStatus[] shipsStatus;

	public static void main(String[] args) {
		launch(args);
	}

	public class cellButton extends Button {
		String shipName;
		Boolean emptyStatus;

		public cellButton() {
			super();
			shipName = "";
			emptyStatus = true;
		}

		Boolean isEmpty() {
			return emptyStatus;
		}
	}

	private class boardSetup {
		int rows = 10;
		int cols = 10;

		int currRowHover;
		int currColHover;
		int currPieceIndex = 0;
		boolean isVertical = false;
		ArrayList<String> pieceSetup;

		void toggleOrientation() {
			isVertical = !isVertical;
		}

		public boardSetup() {
			pieceSetup = new ArrayList<>();
			pieceSetup.add("5-cell");
			pieceSetup.add("4-cell");
			pieceSetup.add("3-cell");
			pieceSetup.add("3-cell");
			pieceSetup.add("2-cell");
		}

		void piecePlaced() {
			currPieceIndex++;
		}

		void setCurrHover(int row, int col) {
			currRowHover = row;
			currColHover = col;
		}
	}

	;

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
		setBackground(mainPane, "battlebackground.png");


		Image battle = new Image("Banner.png");
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

	private void setGridCellBackground(GridPane grid, int row, int col) {
		cellButton currButton = (cellButton) grid.getChildren().get((row * 10) + col);
		if (!currButton.emptyStatus) {
			return;
		}
		if (game.isVertical) {
			if (game.currPieceIndex == 0) { //we're setting a 5-cell piece
				if (row >= 2 && row <= 7) {
					cellButton upOne = (cellButton) grid.getChildren().get(((row - 1) * 10) + col);
					cellButton upTwo = (cellButton) grid.getChildren().get(((row - 2) * 10) + col);
					cellButton downOne = (cellButton) grid.getChildren().get(((row + 1) * 10) + col);
					cellButton downTwo = (cellButton) grid.getChildren().get(((row + 2) * 10) + col);
					if ((!upOne.emptyStatus) || (!upTwo.emptyStatus) || (!downOne.emptyStatus) || (!downTwo.emptyStatus) || (!currButton.emptyStatus)) { //Collision with other cell occupied by a boat
						grid.getChildren().get((row * 10) + col).setStyle("-fx-background-image: url('BadCell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
					} else {
						grid.getChildren().get(((row - 2) * 10) + col).setStyle("-fx-background-image: url('VertFront-cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
						grid.getChildren().get(((row - 1) * 10) + col).setStyle("-fx-background-image: url('VertMidGrey-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
						grid.getChildren().get((row * 10) + col).setStyle("-fx-background-image: url('VertMidRedStar-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
						grid.getChildren().get(((row + 1) * 10) + col).setStyle("-fx-background-image: url('VertMidCannon-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
						grid.getChildren().get(((row + 2) * 10) + col).setStyle("-fx-background-image: url('VertBack-cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
					}
				} else { //too close to border to place 5-cell boat
					grid.getChildren().get((row * 10) + col).setStyle("-fx-background-image: url('BadCell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
				}
			} else if (game.currPieceIndex == 1) {
				if (row >= 2 && row <= 8) {
					cellButton upOne = (cellButton) grid.getChildren().get(((row - 1) * 10) + col);
					cellButton upTwo = (cellButton) grid.getChildren().get(((row - 2) * 10) + col);
					cellButton downOne = (cellButton) grid.getChildren().get(((row + 1) * 10) + col);
					if ((!upOne.emptyStatus) || (!upTwo.emptyStatus) || (!downOne.emptyStatus) || (!currButton.emptyStatus)) { //Collision with other cell occupied by a boat
						grid.getChildren().get((row * 10) + col).setStyle("-fx-background-image: url('BadCell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
					} else {
						grid.getChildren().get(((row - 2) * 10) + col).setStyle("-fx-background-image: url('VertFront-cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
						grid.getChildren().get(((row - 1) * 10) + col).setStyle("-fx-background-image: url('VertMidRed-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
						grid.getChildren().get((row * 10) + col).setStyle("-fx-background-image: url('VertMidGrey-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
						grid.getChildren().get(((row + 1) * 10) + col).setStyle("-fx-background-image: url('VertBack-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
					}
				} else { //to close to border to place 4-cell boat
					grid.getChildren().get((row * 10) + col).setStyle("-fx-background-image: url('BadCell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
				}
			} else if (game.currPieceIndex == 2 || game.currPieceIndex == 3) {
				if (row >= 1 && row <= 8) {
					cellButton upOne = (cellButton) grid.getChildren().get(((row - 1) * 10) + col);
					cellButton downOne = (cellButton) grid.getChildren().get(((row + 1) * 10) + col);
					if ((!upOne.emptyStatus) || (!downOne.emptyStatus) || (!currButton.emptyStatus)) { //Collision with other cell occupied by a boat
						grid.getChildren().get((row * 10) + col).setStyle("-fx-background-image: url('BadCell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
					} else {
						grid.getChildren().get(((row - 1) * 10) + col).setStyle("-fx-background-image: url('VertFront-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
						grid.getChildren().get((row * 10) + col).setStyle("-fx-background-image: url('VertMidGrey-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
						grid.getChildren().get(((row + 1) * 10) + col).setStyle("-fx-background-image: url('VertBack-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
					}
				} else { //to close to border to place 3-cell boat
					grid.getChildren().get((row * 10) + col).setStyle("-fx-background-image: url('BadCell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
				}
			} else if (game.currPieceIndex == 4) {
				if (row >= 1) {
					cellButton upOne = (cellButton) grid.getChildren().get(((row - 1) * 10) + col);
					if ((!upOne.emptyStatus) || (!currButton.emptyStatus)) { //Collision with other cell occupied by a boat
						grid.getChildren().get((row * 10) + col).setStyle("-fx-background-image: url('BadCell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
					} else {
						grid.getChildren().get(((row - 1) * 10) + col).setStyle("-fx-background-image: url('VertFront-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
						grid.getChildren().get((row * 10) + col).setStyle("-fx-background-image: url('VertBackFlag-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
					}
				} else { //to close to border to place 2-cell boat
					grid.getChildren().get((row * 10) + col).setStyle("-fx-background-image: url('BadCell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
				}
			}

		} else {
			if (game.currPieceIndex == 0) { //we're setting a 5-cell piece
				if (col >= 2 && col <= 7) {
					grid.getChildren().get((row * 10) + col).setStyle("-fx-background-image: url('BadCell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
					cellButton leftOne = (cellButton) grid.getChildren().get((row * 10) + col - 1);
					cellButton leftTwo = (cellButton) grid.getChildren().get((row * 10) + col - 2);
					cellButton rightOne = (cellButton) grid.getChildren().get((row * 10) + col + 1);
					cellButton rightTwo = (cellButton) grid.getChildren().get((row * 10) + col + 2);
					if ((!leftOne.emptyStatus) || (!leftTwo.emptyStatus) || (!rightTwo.emptyStatus) || (!rightOne.emptyStatus)) {
						grid.getChildren().get((row * 10) + col).setStyle("-fx-background-image: url('BadCell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
					} else {
						grid.getChildren().get((row * 10) + col - 2).setStyle("-fx-background-image: url('HorizontalBack-cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
						grid.getChildren().get((row * 10) + col - 1).setStyle("-fx-background-image: url('MidCannon-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
						grid.getChildren().get((row * 10) + col).setStyle("-fx-background-image: url('MidRedStar-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
						grid.getChildren().get((row * 10) + col + 1).setStyle("-fx-background-image: url('MidCannon-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
						grid.getChildren().get((row * 10) + col + 2).setStyle("-fx-background-image: url('HorizontalFront-cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
					}
				} else {
					grid.getChildren().get((row * 10) + col).setStyle("-fx-background-image: url('BadCell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
				}
			} else if (game.currPieceIndex == 1) { //we're setting a 4-cell piece
				if (col >= 2 && col <= 8) {
					cellButton leftOne = (cellButton) grid.getChildren().get((row * 10) + col - 1);
					cellButton leftTwo = (cellButton) grid.getChildren().get((row * 10) + col - 2);
					cellButton rightOne = (cellButton) grid.getChildren().get((row * 10) + col + 1);
					if ((!leftOne.emptyStatus) || (!leftTwo.emptyStatus) || (!rightOne.emptyStatus)) {
						grid.getChildren().get((row * 10) + col).setStyle("-fx-background-image: url('BadCell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
					} else {
						grid.getChildren().get((row * 10) + col - 2).setStyle("-fx-background-image: url('HorizontalBack-cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
						grid.getChildren().get((row * 10) + col - 1).setStyle("-fx-background-image: url('MidGrey-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
						grid.getChildren().get((row * 10) + col).setStyle("-fx-background-image: url('MidRed-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
						grid.getChildren().get((row * 10) + col + 1).setStyle("-fx-background-image: url('HorizontalFront-cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
					}
				} else {
					grid.getChildren().get((row * 10) + col).setStyle("-fx-background-image: url('BadCell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
				}
			} else if (game.currPieceIndex == 2 || game.currPieceIndex == 3) { //we're setting a 3-cell piece
				if (col >= 1 && col <= 8) {
					cellButton leftOne = (cellButton) grid.getChildren().get((row * 10) + col - 1);
					cellButton rightOne = (cellButton) grid.getChildren().get((row * 10) + col + 1);
					if ((!leftOne.emptyStatus) || (!rightOne.emptyStatus)) {
						grid.getChildren().get((row * 10) + col).setStyle("-fx-background-image: url('BadCell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
					} else {
						grid.getChildren().get((row * 10) + col - 1).setStyle("-fx-background-image: url('HorizontalBack-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
						grid.getChildren().get((row * 10) + col).setStyle("-fx-background-image: url('MidGrey-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
						grid.getChildren().get((row * 10) + col + 1).setStyle("-fx-background-image: url('HorizontalFront-cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
					}
				} else {
					grid.getChildren().get((row * 10) + col).setStyle("-fx-background-image: url('BadCell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
				}
			} else if (game.currPieceIndex == 4) { //we're setting a 2-cell piece
				cellButton rightOne = (cellButton) grid.getChildren().get((row * 10) + col + 1);
				if (col <= 8) {
					if ((!rightOne.emptyStatus)) {
						grid.getChildren().get((row * 10) + col).setStyle("-fx-background-image: url('BadCell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
					} else {
						grid.getChildren().get((row * 10) + col).setStyle("-fx-background-image: url('HorizontalBackFlag-CEll.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
						grid.getChildren().get((row * 10) + col + 1).setStyle("-fx-background-image: url('HorizontalFront-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;  -fx-background-color: transparent;");
					}
				} else {
					grid.getChildren().get((row * 10) + col).setStyle("-fx-background-image: url('BadCell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
				}
			}
		}
	}

	public void removeGridCellBackground(int row, int col, GridPane grid) {
		cellButton currButton = (cellButton) grid.getChildren().get((row * 10) + col);
		if (!currButton.emptyStatus) {
			return;
		}
		cellButton temp = (cellButton) grid.getChildren().get((row * 10) + col);
		String backgroundImage = temp.getStyle();
		if (backgroundImage != null && backgroundImage.contains("BadCell.png")) { //only once cell has image so we only need to remove one cell background
			System.out.println("Current background image is BadCell.png");
			grid.getChildren().get((row * 10) + col).setStyle("-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 2px;");
		} else if (game.isVertical) {
			if (game.currPieceIndex == 0) { //we're setting a 5-cell piece
				grid.getChildren().get(((row - 2) * 10) + col).setStyle("-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 2px;");
				grid.getChildren().get(((row - 1) * 10) + col).setStyle("-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 2px;");
				grid.getChildren().get((row * 10) + col).setStyle("-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 2px;");
				grid.getChildren().get(((row + 1) * 10) + col).setStyle("-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 2px;");
				grid.getChildren().get(((row + 2) * 10) + col).setStyle("-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 2px;");
			} else if (game.currPieceIndex == 1) {
				grid.getChildren().get(((row - 2) * 10) + col).setStyle("-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 2px;");
				grid.getChildren().get(((row - 1) * 10) + col).setStyle("-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 2px;");
				grid.getChildren().get((row * 10) + col).setStyle("-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 2px;");
				grid.getChildren().get(((row + 1) * 10) + col).setStyle("-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 2px;");
			} else if (game.currPieceIndex == 2 || game.currPieceIndex == 3) {
				grid.getChildren().get(((row - 1) * 10) + col).setStyle("-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 2px;");
				grid.getChildren().get((row * 10) + col).setStyle("-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 2px;");
				grid.getChildren().get(((row + 1) * 10) + col).setStyle("-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 2px;");
			} else if (game.currPieceIndex == 4) {
				grid.getChildren().get(((row - 1) * 10) + col).setStyle("-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 2px;");
				grid.getChildren().get((row * 10) + col).setStyle("-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 2px;");
			}
		} else {
			if (game.currPieceIndex == 0) { //we're setting a 5-cell piece
				grid.getChildren().get((row * 10) + col - 2).setStyle("-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 2px;");
				grid.getChildren().get((row * 10) + col - 1).setStyle("-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 2px;");
				grid.getChildren().get((row * 10) + col).setStyle("-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 2px;");
				grid.getChildren().get((row * 10) + col + 1).setStyle("-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 2px;");
				grid.getChildren().get((row * 10) + col + 2).setStyle("-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 2px;");
			} else if (game.currPieceIndex == 1) {
				grid.getChildren().get((row * 10) + col - 2).setStyle("-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 2px;");
				grid.getChildren().get((row * 10) + col - 1).setStyle("-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 2px;");
				grid.getChildren().get((row * 10) + col).setStyle("-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 2px;");
				grid.getChildren().get((row * 10) + col + 1).setStyle("-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 2px;");
			} else if (game.currPieceIndex == 2 || game.currPieceIndex == 3) {
				grid.getChildren().get((row * 10) + col - 1).setStyle("-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 2px;");
				grid.getChildren().get((row * 10) + col).setStyle("-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 2px;");
				grid.getChildren().get((row * 10) + col + 1).setStyle("-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 2px;");
			} else if (game.currPieceIndex == 4) {
				grid.getChildren().get((row * 10) + col).setStyle("-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 2px;");
				grid.getChildren().get((row * 10) + col + 1).setStyle("-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 2px;");
			}
		}
	}

	private Scene playerChoiceScene(Stage primaryStage) {
		BorderPane pane = new BorderPane();
		setBackground(pane, "battlebackground.png");

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
		setBackground(waitingPane, "battlebackground.png");

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
		game = new boardSetup();
		setBackground(pane, "battlebackground.png");

		GridPane grid = new GridPane();
		grid.setPadding(new Insets(2, 2, 2, 2)); // Margin around the grid
		grid.setVgap(5); // Vertical gap between buttons
		grid.setHgap(5); // Horizontal gap between buttons
		grid.setAlignment(Pos.CENTER); // Center alignment for the GridPane within the BorderPane
		pane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
			if (event.getCode() == KeyCode.Z) {
				removeGridCellBackground(game.currRowHover, game.currColHover, grid);
				game.toggleOrientation();
				setGridCellBackground(grid, game.currRowHover, game.currColHover);
				System.out.println("hi");
				event.consume();
			}
		});
		// Create buttons and add them to the grid
		for (int row = 0; row < 10; row++) {
			for (int col = 0; col < 10; col++) {
				cellButton button = new cellButton();
				button.setPrefSize(45, 45); // Increased size by 15pt
				button.setStyle("-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 2px;"); // Transparent background with a white outline
				int finalRow = row;
				int finalCol = col;
				button.setOnMouseClicked(e -> handleButtonClick(finalRow, finalCol, grid));
				button.setOnMouseEntered(e -> handleButtonHover(finalRow, finalCol, grid));
				button.setOnMouseExited(e -> handleButtonExit(finalRow, finalCol, grid));
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

	private void handleButtonHover(int row, int col, GridPane grid) {
		game.setCurrHover(row, col);
		if (((cellButton) grid.getChildren().get((row * 10) + col)).emptyStatus) {
			setGridCellBackground(grid, row, col);
		}
	}

	private void handleButtonExit(int row, int col, GridPane grid) {
		if (((cellButton) grid.getChildren().get((row * 10) + col)).emptyStatus) {
			removeGridCellBackground(row, col, grid);
		}
	}

	private void handleButtonClick(int row, int col, GridPane grid) {
		System.out.println("Button clicked at row " + row + ", col " + col);
		cellButton checkEmpty = (cellButton) grid.getChildren().get(((row) * 10) + col);
		if (!checkEmpty.emptyStatus) {//button clicked already has a boat nothing to do
			return;
		}

		cellButton checkBadCell = (cellButton) grid.getChildren().get((row * 10) + col);
		String backgroundImage = checkBadCell.getStyle();
		if (backgroundImage != null && backgroundImage.contains("BadCell.png")) { //BadCell so do nothing
			return;
		}

		if (game.isVertical) {
			if (game.currPieceIndex == 0) { //disabling button action while preserving css
				cellButton temp = (cellButton) grid.getChildren().get(((row - 2) * 10) + col);
				temp.emptyStatus = false;
				temp = (cellButton) grid.getChildren().get(((row - 1) * 10) + col);
				temp.emptyStatus = false;
				temp = (cellButton) grid.getChildren().get((row * 10) + col);
				temp.emptyStatus = false;
				temp = (cellButton) grid.getChildren().get(((row + 1) * 10) + col);
				temp.emptyStatus = false;
				temp = (cellButton) grid.getChildren().get(((row + 2) * 10) + col);
				temp.emptyStatus = false;
				ships[0] = new Ship(5, ((row - 2) * 10) + col, ((row - 1) * 10) + col, (row * 10) + col, ((row + 1) * 10) + col, ((row + 2) * 10) + col, true);
				game.piecePlaced();
			} else if (game.currPieceIndex == 1) {
				cellButton temp = (cellButton) grid.getChildren().get(((row - 2) * 10) + col);
				temp.emptyStatus = false;
				temp = (cellButton) grid.getChildren().get(((row - 1) * 10) + col);
				temp.emptyStatus = false;
				temp = (cellButton) grid.getChildren().get((row * 10) + col);
				temp.emptyStatus = false;
				temp = (cellButton) grid.getChildren().get(((row + 1) * 10) + col);
				temp.emptyStatus = false;
				ships[1] = new Ship(4, ((row - 2) * 10) + col, ((row - 1) * 10) + col, (row * 10) + col, ((row + 1) * 10) + col, true);
				game.piecePlaced();
			} else if (game.currPieceIndex == 2 || game.currPieceIndex == 3) {
				cellButton temp = (cellButton) grid.getChildren().get(((row - 1) * 10) + col);
				temp.emptyStatus = false;
				temp = (cellButton) grid.getChildren().get((row * 10) + col);
				temp.emptyStatus = false;
				temp = (cellButton) grid.getChildren().get(((row + 1) * 10) + col);
				temp.emptyStatus = false;
				if (ships[2] == null) {
					ships[2] = new Ship(3, ((row - 1) * 10) + col, (row * 10) + col, ((row + 1) * 10) + col, true);
				} else {
					ships[3] = new Ship(3, ((row - 1) * 10) + col, (row * 10) + col, ((row + 1) * 10) + col, true);
				}
				game.piecePlaced();
			} else if (game.currPieceIndex == 4) {
				cellButton temp = (cellButton) grid.getChildren().get((row * 10) + col);
				temp.emptyStatus = false;
				temp = (cellButton) grid.getChildren().get(((row - 1) * 10) + col);
				temp.emptyStatus = false;
				ships[4] = new Ship(2, ((row - 1) * 10) + col, (row * 10) + col, true);
				game.piecePlaced();
			}
		} else {
			if (game.currPieceIndex == 0) {
				cellButton temp = (cellButton) grid.getChildren().get((row * 10) + col - 2);
				temp.emptyStatus = false;
				temp = (cellButton) grid.getChildren().get((row * 10) + col - 1);
				temp.emptyStatus = false;
				temp = (cellButton) grid.getChildren().get((row * 10) + col);
				temp.emptyStatus = false;
				temp = (cellButton) grid.getChildren().get((row * 10) + col + 1);
				temp.emptyStatus = false;
				temp = (cellButton) grid.getChildren().get((row * 10) + col + 2);
				temp.emptyStatus = false;
				ships[0] = new Ship(5, (row * 10) + col - 2, (row * 10) + col - 1, (row * 10) + col, (row * 10) + col + 1, (row * 10) + col + 2, false);
				game.piecePlaced();
			} else if (game.currPieceIndex == 1) {
				cellButton temp = (cellButton) grid.getChildren().get((row * 10) + col - 2);
				temp.emptyStatus = false;
				temp = (cellButton) grid.getChildren().get((row * 10) + col - 1);
				temp.emptyStatus = false;
				temp = (cellButton) grid.getChildren().get((row * 10) + col);
				temp.emptyStatus = false;
				temp = (cellButton) grid.getChildren().get((row * 10) + col + 1);
				temp.emptyStatus = false;
				ships[1] = new Ship(4, (row * 10) + col - 2, (row * 10) + col - 1, (row * 10) + col, (row * 10) + col + 1, false);
				game.piecePlaced();
			} else if (game.currPieceIndex == 2 || game.currPieceIndex == 3) {
				cellButton temp = (cellButton) grid.getChildren().get((row * 10) + col - 1);
				temp.emptyStatus = false;
				temp = (cellButton) grid.getChildren().get((row * 10) + col);
				temp.emptyStatus = false;
				temp = (cellButton) grid.getChildren().get((row * 10) + col + 1);
				temp.emptyStatus = false;
				if (ships[2] == null) {
					ships[2] = new Ship(3, (row * 10) + col - 1, (row * 10) + col, (row * 10) + col + 1, false);
				} else {
					ships[3] = new Ship(3, (row * 10) + col - 1, (row * 10) + col, (row * 10) + col + 1, false);
				}
				game.piecePlaced();
			} else if (game.currPieceIndex == 4) {
				cellButton temp = (cellButton) grid.getChildren().get((row * 10) + col);
				temp.emptyStatus = false;
				temp = (cellButton) grid.getChildren().get((row * 10) + col + 1);
				temp.emptyStatus = false;
				ships[4] = new Ship(2, (row * 10) + col, (row * 10) + col + 1, false);
				game.piecePlaced();

			}
		}
		if (game.currPieceIndex == 5) {
			primaryStage.setScene(createGameScene());
		}

	}

	private Scene createGameScene() {

		BorderPane mainLayout = new BorderPane();
		setBackground(mainLayout, "battlebackground.png");


		HBox boardsContainer = new HBox(80);
		boardsContainer.setAlignment(Pos.CENTER);
		boardsContainer.setPadding(new Insets(50, 0, 0, 0));


		GridPane leftBoard = new GridPane();
		leftBoard.setHgap(5);
		leftBoard.setVgap(5);
		leftBoard.setAlignment(Pos.CENTER);

		GridPane rightBoard = new GridPane();
		rightBoard.setHgap(5);
		rightBoard.setVgap(5);
		rightBoard.setAlignment(Pos.CENTER);

		// initializes enemy grid and ships
		initializeShipStatuses();
		initializeEnemyGrid(rightBoard);


		for (int row = 0; row < 10; row++) {
			for (int col = 0; col < 10; col++) {
				cellButton leftButton = new cellButton();
				leftButton.setPrefSize(40, 40);
				leftButton.setStyle("-fx-background-color: transparent; -fx-border-color: black; -fx-border-width: 2px;");

				leftBoard.add(leftButton, col, row);
			}
		}
		if (ships[0].isVertical()) {
			int back = ships[0].getBack();
			int back2 = ships[0].getBack2();
			int mid = ships[0].getMid();
			int front2 = ships[0].getFront2();
			int front = ships[0].getFront();
			leftBoard.getChildren().get(back).setStyle("-fx-background-image: url('VertFront-cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
			leftBoard.getChildren().get(back2).setStyle("-fx-background-image: url('VertMidGrey-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
			leftBoard.getChildren().get(mid).setStyle("-fx-background-image: url('VertMidRedStar-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
			leftBoard.getChildren().get(front2).setStyle("-fx-background-image: url('VertMidCannon-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
			leftBoard.getChildren().get(front).setStyle("-fx-background-image: url('VertBack-cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
		} else {
			int back = ships[0].getBack();
			int back2 = ships[0].getBack2();
			int mid = ships[0].getMid();
			int front2 = ships[0].getFront2();
			int front = ships[0].getFront();
			leftBoard.getChildren().get(back).setStyle("-fx-background-image: url('HorizontalBack-cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
			leftBoard.getChildren().get(back2).setStyle("-fx-background-image: url('MidCannon-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
			leftBoard.getChildren().get(mid).setStyle("-fx-background-image: url('MidRedStar-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
			leftBoard.getChildren().get(front2).setStyle("-fx-background-image: url('MidCannon-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
			leftBoard.getChildren().get(front).setStyle("-fx-background-image: url('HorizontalFront-cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");

		}
		if (ships[1].isVertical()) {
			int back = ships[1].getBack();
			int back2 = ships[1].getBack2();
			int mid = ships[1].getMid();
			int front = ships[1].getFront();
			leftBoard.getChildren().get(back).setStyle("-fx-background-image: url('VertFront-cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
			leftBoard.getChildren().get(back2).setStyle("-fx-background-image: url('VertMidRed-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
			leftBoard.getChildren().get(mid).setStyle("-fx-background-image: url('VertMidGrey-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
			leftBoard.getChildren().get(front).setStyle("-fx-background-image: url('VertBack-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
		} else {
			int back = ships[1].getBack();
			int back2 = ships[1].getBack2();
			int mid = ships[1].getMid();
			int front = ships[1].getFront();
			leftBoard.getChildren().get(back).setStyle("-fx-background-image: url('HorizontalBack-cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
			leftBoard.getChildren().get(back2).setStyle("-fx-background-image: url('MidGrey-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
			leftBoard.getChildren().get(mid).setStyle("-fx-background-image: url('MidRed-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
			leftBoard.getChildren().get(front).setStyle("-fx-background-image: url('HorizontalFront-cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
		}
		for (int i = 2; i < 4; i++) {
			if (ships[i].isVertical()) {
				int back = ships[i].getBack();
				int mid = ships[i].getMid();
				int front = ships[i].getFront();
				leftBoard.getChildren().get(back).setStyle("-fx-background-image: url('VertFront-cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
				leftBoard.getChildren().get(mid).setStyle("-fx-background-image: url('VertMidGrey-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
				leftBoard.getChildren().get(front).setStyle("-fx-background-image: url('VertBack-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
			} else {
				int back = ships[i].getBack();
				int mid = ships[i].getMid();
				int front = ships[i].getFront();
				leftBoard.getChildren().get(back).setStyle("-fx-background-image: url('HorizontalBack-cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
				leftBoard.getChildren().get(mid).setStyle("-fx-background-image: url('MidGrey-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
				leftBoard.getChildren().get(front).setStyle("-fx-background-image: url('HorizontalFront-cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
			}
		}
		if (ships[4].isVertical()) {
			int back = ships[4].getBack();
			int front = ships[4].getFront();
			leftBoard.getChildren().get(back).setStyle("-fx-background-image: url('VertFront-cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
			leftBoard.getChildren().get(front).setStyle("-fx-background-image: url('VertBackFlag-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
		} else {
			int back = ships[4].getBack();
			int front = ships[4].getFront();
			leftBoard.getChildren().get(back).setStyle("-fx-background-image: url('HorizontalBackFlag-CEll.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
			leftBoard.getChildren().get(front).setStyle("-fx-background-image: url('HorizontalFront-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
		}
		Button backButton = new Button("Forfeit");
		backButton.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-background-color: black; -fx-text-fill: white; -fx-padding: 10 20 10 20; -fx-background-radius: 15; -fx-border-color: #afb0b3; -fx-border-width: 4; -fx-border-radius: 5; -fx-font-family: 'Lucida Fax'; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");
		backButton.setOnAction(e -> {
			primaryStage.setScene(openScene());
			resetGrid(rightBoard); // reset enemy cpu grid

		});
		mainLayout.setBottom(backButton);
		BorderPane.setAlignment(backButton, Pos.BOTTOM_LEFT);
		boardsContainer.getChildren().addAll(leftBoard, rightBoard);
		mainLayout.setTop(boardsContainer);

		return new Scene(mainLayout, 1350, 650);
	}

	private void initializeEnemyGrid(GridPane grid) {
		placeEnemyShips(); // Ensure ships are placed before initializing UI

		for (int row = 0; row < 10; row++) {
			for (int col = 0; col < 10; col++) {
				Button enemyCell = new Button();
				enemyCell.setPrefSize(40, 40);
				enemyCell.setStyle("-fx-background-color: transparent; -fx-border-color: black; -fx-border-width: 2px;");
				if (enemyCpuGrid[row][col]) {
					enemyCell.setStyle("-fx-background-color: darkgray;"); // Visual cue for debugging
				}
				int finalRow = row;
				int finalCol = col;
				enemyCell.setOnAction(e -> handleEnemyCellAction(finalRow, finalCol, enemyCell, grid));
				grid.add(enemyCell, col, row);
			}
		}
	}


	private void handleEnemyCellAction(int row, int col, Button cell, GridPane grid) {
		boolean hit = checkHit(row, col, grid); // Directly check the grid
		if (hit) {
			if (!boatDestroyed){
				cell.setStyle("-fx-background-color: red; -fx-border-color: white; -fx-border-width: 1px;");
				System.out.println("Player hit at " + row + ", " + col);
			} else {
				boatDestroyed = false;
			}
		} else {
			cell.setStyle("-fx-background-color: lightgray; -fx-border-color: white; -fx-border-width: 1px;");
			System.out.println("Player miss at " + row + ", " + col);
		}
		cell.setDisable(true); // Disable the button after it's been clicked
	}

	// This grid keeps track of whether a cell has part of a ship
	private boolean[][] enemyCpuGrid = new boolean[10][10];

	private boolean[][] enemyCpuGridBoatCenters = new boolean[10][10]; //keeps track of the center of boats
	// Declare this array to track which ship occupies which cell
	private int[][] shipAtPosition = new int[10][10]; // Default value of 0 means no ship

	int numBoatsDestroyed = 0;
	Boolean boatDestroyed = false;
	private void placeEnemyShips() {
		int[] shipSizes = {5, 4, 3, 3, 2}; // Sizes of ships
		Random random = new Random();
		for (int shipIndex = 0; shipIndex < shipSizes.length; shipIndex++) {
			int size = shipSizes[shipIndex];
			boolean placed = false;
			while (!placed) {
				int row = random.nextInt(10);
				int col = random.nextInt(10);
				boolean horizontal = random.nextBoolean();
				if (canPlaceShip(size, row, col, horizontal)) {
					if (horizontal) { //needed to keep track of where the start of a boat is for showing the boat cell images later
						if (size == 2) {
							shipsStatus[4].isVertical = false;
							shipsStatus[4].colTop = col;
						} else if (size == 3 && shipIndex == 2) {
							shipsStatus[2].isVertical = false;
							shipsStatus[2].colTop = col;
						} else if (size == 3 && shipIndex == 3) {
							shipsStatus[3].isVertical = false;
							shipsStatus[3].colTop = col;
						} else if (size == 4) {
							shipsStatus[1].isVertical = false;
							shipsStatus[1].colTop = col;
						} else if (size == 5) {
							shipsStatus[0].isVertical = false;
							shipsStatus[0].colTop = col;
						}
					} else {
						if (size == 2) {
							shipsStatus[4].isVertical = true;
							shipsStatus[4].rowTop = row;
						} else if (size == 3 && shipIndex == 2) {
							System.out.println("Vertical 3-cell placed");
							shipsStatus[2].isVertical = true;
							shipsStatus[2].rowTop = row;
						} else if (size == 3 && shipIndex == 3) {
							System.out.println("Vertical 3-cell placed");
							shipsStatus[3].isVertical = true;
							shipsStatus[3].rowTop = row;
						} else if (size == 4) {
							shipsStatus[1].isVertical = true;
							shipsStatus[1].rowTop = row;
						} else if (size == 5) {
							shipsStatus[0].isVertical = true;
							shipsStatus[0].rowTop = row;
						}
					}
					for (int i = 0; i < size; i++) { //loops until each cell of (size) boat has been placed
						if (horizontal) {
							enemyCpuGrid[row][col + i] = true;
							shipAtPosition[row][col + i] = shipIndex + 1; // Store ship index (1-based for clarity)
						} else { //placing vertical boat
							enemyCpuGrid[row + i][col] = true;
							shipAtPosition[row + i][col] = shipIndex + 1; // Store ship index (1-based for clarity)
						}
					}
					placed = true;
				}
			}
		}
	}


	private boolean canPlaceShip(int size, int row, int col, boolean horizontal) {
		if (horizontal) {
			if (col + size > 10) return false;
			for (int i = 0; i < size; i++) {
				if (enemyCpuGrid[row][col + i]) return false;
			}
		} else {
			if (row + size > 10) return false;
			for (int i = 0; i < size; i++) {
				if (enemyCpuGrid[row + i][col]) return false;
			}
		}
		return true;
	}
	private boolean checkHit(int row, int col, GridPane grid) {
		if (enemyCpuGrid[row][col]) {
			updateShipStatus(row, col, grid);
			return true;
		}
		return false;
	}

	private void resetGrid(GridPane grid) {
		// Clear the state of the enemy grid's ship placements
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				enemyCpuGrid[i][j] = false;
				shipAtPosition[i][j] = 0; // Reset ship tracking

			}
		}
	}


	// following functions intializes ships (enemies for now)


	private void initializeShipStatuses() { //Index 0 holds 5, 1 holds 4-cell, 2 holds first 3-cell, 3 holds second 3-cell, 4 holds 2-cell
		int[] shipSizes = {5, 4, 3, 3, 2};
		shipsStatus = new ShipStatus[shipSizes.length];
		for (int i = 0; i < shipSizes.length; i++) {
			shipsStatus[i] = new ShipStatus(shipSizes[i], false);
		}

	}
	private void updateShipStatus(int row, int col, GridPane grid) {
		int shipIndex = shipAtPosition[row][col] - 1; // Adjust for 0-based index in the array
		if (shipIndex >= 0) { // Check if there's a ship at this position
			shipsStatus[shipIndex].hits++;

			// prints whenever a ship is destroyed along with its number (index + 1)
			if (shipsStatus[shipIndex].isDestroyed()) {
				numBoatsDestroyed++;
				boatDestroyed = true;
				System.out.println("Ship " + (shipIndex + 1) + " is fully destroyed!");

				if (shipsStatus[shipIndex].isVertical){ //Vertical image placement needed
					row = shipsStatus[shipIndex].rowTop;
					if (shipIndex == 0){
						grid.getChildren().get(((row + 0) * 10) + col).setStyle("-fx-background-image: url('VertFront-cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
						grid.getChildren().get(((row + 1) * 10) + col).setStyle("-fx-background-image: url('VertMidGrey-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
						grid.getChildren().get(((row + 2) * 10) + col).setStyle("-fx-background-image: url('VertMidRedStar-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
						grid.getChildren().get(((row + 3) * 10) + col).setStyle("-fx-background-image: url('VertMidCannon-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
						grid.getChildren().get(((row + 4) * 10) + col).setStyle("-fx-background-image: url('VertBack-cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");

					} else if (shipIndex == 1){
						grid.getChildren().get(((row + 0) * 10) + col).setStyle("-fx-background-image: url('VertFront-cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
						grid.getChildren().get(((row + 1) * 10) + col).setStyle("-fx-background-image: url('VertMidRed-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
						grid.getChildren().get(((row + 2) * 10) + col).setStyle("-fx-background-image: url('VertMidGrey-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
						grid.getChildren().get(((row + 3) * 10) + col).setStyle("-fx-background-image: url('VertBack-cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");

					} else if (shipIndex == 2 || shipIndex == 3){
						System.out.println("3-cell reimage");
						grid.getChildren().get(((row  + 0 ) * 10) + col).setStyle("-fx-background-image: url('VertFront-cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
						grid.getChildren().get(((row + 1) * 10) + col).setStyle("-fx-background-image: url('VertMidRed-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
						grid.getChildren().get(((row + 2) * 10) + col).setStyle("-fx-background-image: url('VertBack-cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
					} else if (shipIndex == 4){
						grid.getChildren().get(((row + 0) * 10) + col).setStyle("-fx-background-image: url('VertFront-cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
						grid.getChildren().get(((row + 1) * 10) + col).setStyle("-fx-background-image: url('VertBackFlag-cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");

					}
				} else { //horizontal image placement needed
					col = shipsStatus[shipIndex].colTop;
					if (shipIndex == 0){
						grid.getChildren().get(((row) * 10) + col +4).setStyle("-fx-background-image: url('HorizontalFront-cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
						grid.getChildren().get(((row) * 10) + col + 3).setStyle("-fx-background-image: url('MidGrey-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
						grid.getChildren().get(((row) * 10) + col + 2).setStyle("-fx-background-image: url('MidRedStar-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
						grid.getChildren().get(((row) * 10) + col + 1).setStyle("-fx-background-image: url('MidCannon-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
						grid.getChildren().get(((row) * 10) + col).setStyle("-fx-background-image: url('HorizontalBack-cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");

					} else if (shipIndex == 1){
						grid.getChildren().get(((row) * 10) + col + 3).setStyle("-fx-background-image: url('HorizontalFront-cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
						grid.getChildren().get(((row) * 10) + col + 2).setStyle("-fx-background-image: url('MidRed-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
						grid.getChildren().get(((row) * 10) + col + 1).setStyle("-fx-background-image: url('MidGrey-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
						grid.getChildren().get(((row) * 10) + col).setStyle("-fx-background-image: url('HorizontalBack-cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");

					} else if (shipIndex == 2 || shipIndex == 3){
						System.out.println("3-cell reimage");
						grid.getChildren().get(((row) * 10) + col + 2).setStyle("-fx-background-image: url('HorizontalFront-cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
						grid.getChildren().get(((row) * 10) + col + 1).setStyle("-fx-background-image: url('MidRed-Cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center;");
						grid.getChildren().get(((row) * 10) + col).setStyle("-fx-background-image: url('HorizontalBack-cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
					} else if (shipIndex == 4){
						grid.getChildren().get(((row) * 10) + col + 1).setStyle("-fx-background-image: url('HorizontalFront-cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");
						grid.getChildren().get(((row) * 10) + col).setStyle("-fx-background-image: url('HorizontalBackFlag-cell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: transparent;");

					}
				}
			}
		}
	}

	private boolean isPartOfShip(int shipIndex, int row, int col) {
		// Check if the cell belongs to the specified shipIndex
		return shipAtPosition[row][col] == shipIndex + 1;
	}

}
