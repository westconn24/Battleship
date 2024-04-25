import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
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
import javafx.util.Duration;

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

	private boolean[][] playerGrid = new boolean[10][10];
	private boolean[] availableCells = new boolean[100];


	{
		for (int i = 0; i < 100; i++) {
			availableCells[i] = true;
		}
	}

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
		this.primaryStage = primaryStage;
		primaryStage.setScene(openScene());
		primaryStage.setTitle("Battleship Client");
		primaryStage.show();
	}

	private Scene openScene() {
		BorderPane mainPane = new BorderPane();
		setBackground(mainPane, "battlebackground.png");

		Image battle = new Image("Banner.png");
		ImageView imageView = new ImageView(battle);
		imageView.setPreserveRatio(true);
		imageView.setFitHeight(500);
		imageView.setFitWidth(600);

		StackPane imageContainer = new StackPane(imageView);
		StackPane.setAlignment(imageView, Pos.TOP_CENTER);
		imageContainer.setPadding(new Insets(100, 0, 0, 0));

		VBox centerLayout = new VBox();
		centerLayout.getChildren().addAll(imageContainer);

		TranslateTransition t = new TranslateTransition(Duration.seconds(2), centerLayout);

		t.setToY(-600);

		Button playButton = new Button("PLAY");
		playButton.setStyle(
				"-fx-font-size: 45px; " +
						"-fx-font-weight: bold; " +
						"-fx-background-color: #333232; " +
						"-fx-text-fill: #f1f1f1; " +
						"-fx-padding: 10 20 10 20; " +
						"-fx-background-radius: 15; " +
						"-fx-border-color: #000000; " +
						"-fx-border-width: 4; " +
						"-fx-border-radius: 5; " +
						"-fx-font-family: 'Super Foods'; " +
						"-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);"
		);
		playButton.setOnAction(e -> {

			t.play();
		});
		t.setOnFinished(e -> primaryStage.setScene(playerChoiceScene(primaryStage)));


		HBox buttonContainer = new HBox(playButton);
		buttonContainer.setAlignment(Pos.CENTER);
		buttonContainer.setPadding(new Insets(-50, 0, 20, 0));
		centerLayout.getChildren().addAll(buttonContainer);
		mainPane.setCenter(centerLayout);

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
		Font.loadFont(Objects.requireNonNull(BattleshipClient.class.getResource("Super_Foods.ttf")).toExternalForm(), 30);
		// Create label and style it
		Label choiceLabel = new Label("Choose who to play");
		choiceLabel.setFont(Font.font("Super Foods", 60));
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
				"-fx-font-size: 40px; " +
						"-fx-font-weight: bold; " +
						"-fx-background-color: #03268f; " +
						"-fx-text-fill: white; " +
						"-fx-padding: 10 20 10 20; " +
						"-fx-background-radius: 15; " +

						"-fx-font-family: 'Super Foods'; " +  // Font family
						"-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);"
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
				"-fx-font-size: 40px; " +
						"-fx-font-weight: bold; " +
						"-fx-background-color: #26bc1e; " +
						"-fx-text-fill: white; " +
						"-fx-padding: 10 20 10 20; " +
						"-fx-background-radius: 15; " +
						"-fx-font-family: 'Super Foods'; " +

						"-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);"
		);

		leftButton.setMinSize(150, 75);
		rightButton.setMinSize(150, 75);


		HBox buttonContainer = new HBox(20);
		buttonContainer.getChildren().addAll(leftButton, rightButton);
		buttonContainer.setAlignment(Pos.CENTER);


		VBox centerContainer = new VBox(10);
		centerContainer.getChildren().addAll(choiceLabel, buttonContainer);
		centerContainer.setAlignment(Pos.CENTER);
		centerContainer.setPadding(new Insets(100, 0, 0, 0));

		pane.setCenter(centerContainer);


		Button backButton = new Button("Back to Main");
		backButton.setOnAction(e -> primaryStage.setScene(openScene()));
		backButton.setStyle(
				"-fx-font-size: 15px; " +
						"-fx-font-weight: bold; " +
						"-fx-background-color: #333232; " +
						"-fx-text-fill: #f1f1f1; " +
						"-fx-padding: 10 20 10 20; " +
						"-fx-background-radius: 15; " +
						"-fx-border-color: #000000; " +
						"-fx-border-width: 4; " +
						"-fx-border-radius: 5; " +
						"-fx-font-family: 'Super Foods'; " +
						"-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);"
		);
		TranslateTransition transition = new TranslateTransition(Duration.seconds(2), centerContainer);
		transition.setFromY(-650);
		transition.setToY(0);
		transition.play();
		pane.setBottom(backButton);
		BorderPane.setAlignment(backButton, Pos.CENTER);

		return new Scene(pane, 1350, 650);
	}


	private void connectToServer() {
		// Create the client with a callback to handle messages received from the server
		// Set the background for the pane
		BorderPane pane = new BorderPane();
		game = new boardSetup();
		setBackground(pane, "battlebackground.png");

		GridPane grid = new GridPane();
		grid.setPadding(new Insets(2, 2, 2, 2)); // Margin around the grid
		grid.setVgap(5); // Vertical gap between buttons
		grid.setHgap(5); // Horizontal gap between buttons
		grid.setAlignment(Pos.CENTER); // Center alignment for the GridPane within the BorderPane
		HBox boards = new HBox(grid);
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
		for (int r = 0; r < 10; r++) {
			for (int c = 0; c < 10; c++) {
				cellButton button = new cellButton();
				button.setPrefSize(45, 45); // Increased size by 15pt
				button.setStyle("-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 2px;"); // Transparent background with a white outline
				int finalRow = r;
				int finalCol = c;
				button.setOnMouseClicked(e -> handleOnlineButtonClick(finalRow, finalCol, grid ));
				button.setOnMouseEntered(e -> handleButtonHover(finalRow, finalCol, grid));
				button.setOnMouseExited(e -> handleButtonExit(finalRow, finalCol, grid));
				grid.add(button, finalCol, finalRow);
			}
		}

		// Adding the grid to the center of the BorderPane
		boards.setAlignment(Pos.CENTER);
		boards.setSpacing(15);
		pane.setCenter(boards);

		Scene settPieces = new Scene(pane, 1350, 650);

		AtomicReference<Boolean> turn = new AtomicReference<>(true);
		GridPane attackBoard = new GridPane();
		attackBoard.setPadding(new Insets(2, 2, 2, 2)); // Margin around the grid
		attackBoard.setVgap(5); // Vertical gap between buttons
		attackBoard.setHgap(5); // Horizontal gap between buttons
		attackBoard.setAlignment(Pos.CENTER); // Center alignment for the GridPane within the BorderPane
		for (int r = 0; r < 10; r++) {
			for (int c = 0; c < 10; c++) {
				cellButton button = new cellButton();
				button.setPrefSize(45, 45); // Increased size by 15pt
				button.setStyle("-fx-background-color: transparent; -fx-border-color: black; -fx-border-width: 2px;"); // Transparent background with a white outline
				int finalRow = r;
				int finalCol = c;
				button.setOnMouseClicked(e -> {
					if (turn.get() == true){
						clientConnection.send("cord" + finalRow + finalCol);
					}
				});
				attackBoard.add(button, finalCol, finalRow);
			}
		}

		AtomicInteger hits = new AtomicInteger();

		clientConnection = new Client(data -> {
			// Since the data is of type Serializable, ensure proper handling
			if (data != null) {
				String dataStr = data.toString();
				String opReady = "no";
				String pReady = "no";
				System.out.println(dataStr);
				int opponent;
				if (data.equals("OddCount")) {
					Platform.runLater(() -> {
						primaryStage.setScene(createWaitingScene());
					});
				} else if (dataStr.charAt(0) == 'o' && dataStr.charAt(1) == 'p'){
					String num = "";
					for (int i = 2; i < dataStr.length();i++){
						num += dataStr.charAt(i);
					}
					opponent = Integer.parseInt(num);

					Platform.runLater(() -> {
						primaryStage.setScene(settPieces);
					});
				} else if (dataStr.equals("self")){ //Ignore this it's just to add the attack board once all players have set their boats
					pReady = "yes";
				} else if (data.equals("wait")){
					turn.set(false);
				} else if (dataStr.charAt(0) == 'c' && dataStr.charAt(1) == 'o' && dataStr.charAt(2) == 'r' && dataStr.charAt(3) == 'd'){ //let opponent know if they missed or hit
					turn.set(true);
					int row = Character.getNumericValue(dataStr.charAt(4));
					int col = Character.getNumericValue(dataStr.charAt(5));
					cellButton temp = (cellButton) grid.getChildren().get( (row * 10) + col);
					if (temp.emptyStatus){
						clientConnection.send("miss" + row + col);
						temp.setStyle("-fx-background-image: url('misscell.png'); -fx-background-position: center; -fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 1px;");
					} else {
						 clientConnection.send("hit" + row + col);
						temp.setStyle("-fx-background-image: url('deadcell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: white;  -fx-border-color: blue; -fx-border-width: 2px;");
					}
				}  else if (dataStr.charAt(0) == 'm' && dataStr.charAt(1) == 'i' && dataStr.charAt(2) == 's' && dataStr.charAt(3) == 's'){ //you miss
					int row = Character.getNumericValue(dataStr.charAt(4));
					int col = Character.getNumericValue(dataStr.charAt(5));
					cellButton temp = (cellButton) attackBoard.getChildren().get( (row * 10) + col);
					temp.setDisable(true);
					temp.setStyle("-fx-background-image: url('misscell.png'); -fx-background-position: center; -fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 1px;");
				} else if (dataStr.charAt(0) == 'h' && dataStr.charAt(1) == 'i' && dataStr.charAt(2) == 't'){ //you hit
					int row = Character.getNumericValue(dataStr.charAt(3));
					int col = Character.getNumericValue(dataStr.charAt(4));
					cellButton temp = (cellButton) attackBoard.getChildren().get( (row * 10) + col);
					temp.setDisable(true);
					temp.setStyle("-fx-background-image: url('deadcell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: white;  -fx-border-color: red; -fx-border-width: 2px;");
					hits.getAndIncrement();
					if (hits.get() == 17){
						clientConnection.send("loser");
						Platform.runLater(() -> {
							showGameOverImage(true);
						});
					}
				} else if (data.equals("ready")){ //other player is ready
					opReady = "yes";
				} else if (data.equals("loser")){
					Platform.runLater(() -> {
						showGameOverImage(false);
					});
				}

				if (opReady.equals("yes")){
					Platform.runLater(() -> {
						boards.getChildren().add(attackBoard);
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
		backButton.setStyle(
				"-fx-font-size: 15px; " +
						"-fx-font-weight: bold; " +
						"-fx-background-color: #333232; " +
						"-fx-text-fill: #f1f1f1; " +
						"-fx-padding: 10 20 10 20; " +
						"-fx-background-radius: 15; " +
						"-fx-border-color: #000000; " +
						"-fx-border-width: 4; " +
						"-fx-border-radius: 5; " +
						"-fx-font-family: 'Super Foods'; " +
						"-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);"
		);
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

	private void handleOnlineButtonClick(int row, int col, GridPane grid) {
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
				clientConnection.send("self");
				clientConnection.send("ready");
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
				clientConnection.send("self");
				clientConnection.send("ready");
			}
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
				int row00 = ships[0].getBack() / 10;
				int col00 = ships[0].getBack() % 10;
				int row01 = ships[0].getBack2() / 10;
				int col01 = ships[0].getBack2() % 10;
				int row02 = ships[0].getMid() / 10;
				int col02 = ships[0].getMid() % 10;
				int row03 = ships[0].getFront2() / 10;
				int col03 = ships[0].getFront2() % 10;
				int row04 = ships[0].getFront() / 10;
				int col04 = ships[0].getFront() % 10;
				int row10 = ships[1].getBack() / 10;
				int col10 = ships[1].getBack() % 10;
				int row11 = ships[1].getBack2() / 10;
				int col11 = ships[1].getBack2() % 10;
				int row12 = ships[1].getMid() / 10;
				int col12 = ships[1].getMid() % 10;
				int row13 = ships[1].getFront() / 10;
				int col13 = ships[1].getFront() % 10;
				int row20 = ships[2].getBack() / 10;
				int col20 = ships[2].getBack() % 10;
				int row21 = ships[2].getMid() / 10;
				int col21 = ships[2].getMid() % 10;
				int row22 = ships[2].getFront() / 10;
				int col22 = ships[2].getFront() % 10;
				int row30 = ships[3].getBack() / 10;
				int col30 = ships[3].getBack() % 10;
				int row31 = ships[3].getMid() / 10;
				int col31 = ships[3].getMid() % 10;
				int row32 = ships[3].getFront() / 10;
				int col32 = ships[3].getFront() % 10;
				int row40 = ships[4].getBack() / 10;
				int col40 = ships[4].getBack() % 10;
				int row41 = ships[4].getFront() / 10;
				int col41 = ships[4].getFront() % 10;

				playerGrid[row00][col00] = true;
				playerGrid[row01][col01] = true;
				playerGrid[row02][col02] = true;
				playerGrid[row03][col03] = true;
				playerGrid[row04][col04] = true;
				playerGrid[row10][col10] = true;
				playerGrid[row11][col11] = true;
				playerGrid[row12][col12] = true;
				playerGrid[row13][col13] = true;
				playerGrid[row20][col20] = true;
				playerGrid[row21][col21] = true;
				playerGrid[row22][col22] = true;
				playerGrid[row30][col30] = true;
				playerGrid[row31][col31] = true;
				playerGrid[row32][col32] = true;
				playerGrid[row40][col40] = true;
				playerGrid[row41][col41] = true;
		}


	}

	private Scene createGameScene() {
		BorderPane gamePane = new BorderPane();
		setBackground(gamePane, "battlebackground.png");


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
		initializeEnemyGrid(rightBoard, leftBoard);

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
		backButton.setStyle(
				"-fx-font-size: 15px; " +
						"-fx-font-weight: bold; " +
						"-fx-background-color: #333232; " +
						"-fx-text-fill: #f1f1f1; " +
						"-fx-padding: 10 20 10 20; " +
						"-fx-background-radius: 15; " +
						"-fx-border-color: #000000; " +
						"-fx-border-width: 4; " +
						"-fx-border-radius: 5; " +
						"-fx-font-family: 'Super Foods'; " +
						"-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);"
		);
		backButton.setOnAction(e -> {
			resetShips();
			numPlayerCellsDestroyed = 0;
			//primaryStage.setScene(openScene());
			showGameOverImage(false);

			resetGrid(rightBoard); // reset enemy cpu grid
		});
		gamePane.setBottom(backButton);
		BorderPane.setAlignment(backButton, Pos.BOTTOM_LEFT);
		boardsContainer.getChildren().addAll(leftBoard, rightBoard);
		gamePane.setTop(boardsContainer);

		return new Scene(gamePane, 1350, 650);
	}
	public void resetShips() {
        Arrays.fill(ships, null);
	}


	private void initializeEnemyGrid(GridPane grid, GridPane playergrid) {
		placeEnemyShips(); // Ensure ships are placed before initializing UI

		for (int row = 0; row < 10; row++) {
			for (int col = 0; col < 10; col++) {
				Button enemyCell = new Button();
				enemyCell.setPrefSize(40, 40);
				enemyCell.setStyle("-fx-background-color: transparent; -fx-border-color: black; -fx-border-width: 2px;");
				if (enemyCpuGrid[row][col]) {
					//enemyCell.setStyle("-fx-background-color: darkgray;"); // Visual cue for debugging
				}
				int finalRow = row;
				int finalCol = col;
				enemyCell.setOnAction(e -> handleEnemyCellAction(finalRow, finalCol, enemyCell, grid, playergrid));
				grid.add(enemyCell, col, row);
			}
		}
	}


	private void handleEnemyCellAction(int row, int col, Button cell, GridPane grid, GridPane playergrid) {
		boolean hit = checkHit(row, col, grid); // Directly check the grid

		if (hit) {
			if (!boatDestroyed){
				cell.setStyle("-fx-background-image: url('deadcell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: white; -fx-border-color: blue; -fx-border-width: 2px;");
				System.out.println("Player hit at " + row + ", " + col);
			} else {
				boatDestroyed = false;
			}
		} else {
			cell.setStyle("-fx-background-image: url('misscell.png'); -fx-background-position: center; -fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 1px;");
			System.out.println("Player miss at " + row + ", " + col);
		}
		cell.setDisable(true); // Disable the button after it's been clicked
		Random random = new Random();

		int index = random.nextInt(100);
		boolean valid = availableCells[index];
		while (!valid) {
			index = random.nextInt(100);
			valid = availableCells[index];
		}
		int prow = index / 10;
		int pcol = index % 10;

		handleCpuChoice(index, prow, pcol, playergrid);
		availableCells[index] = false;


		if (areAllShipsDestroyed(shipsStatus)) {
			resetGrid(grid); // reset enemy cpu and player grids
			resetShips();
			numPlayerCellsDestroyed = 0;
			showGameOverImage(true);  // Player win
		} else if (numPlayerCellsDestroyed == 17) {
			resetGrid(grid); // reset enemy cpu and player grids
			resetShips();
			numPlayerCellsDestroyed = 0;
			showGameOverImage(false);  // Player lost
		}

	}



	private void handleCpuChoice (int index, int row, int col, GridPane grid) {

		cellButton currButton = (cellButton) grid.getChildren().get(index);
		boolean hit = checkCPUHit(row, col, grid);

		if (hit) {
			if (!boatDestroyed){
				currButton.setStyle("-fx-background-image: url('deadcell.png'); -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-color: white; -fx-border-color: red; -fx-border-width: 2px;");
				System.out.println("CPU hit at " + row + ", " + col);
			} else {
				boatDestroyed = false;
			}
		} else {
			currButton.setStyle("-fx-background-image: url('misscell.png'); -fx-background-position: center; -fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 1px;");
			System.out.println("CPU miss at " + row + ", " + col);
		}

	}

	// This grid keeps track of whether a cell has part of a ship
	private boolean[][] enemyCpuGrid = new boolean[10][10];
	private int[][] shipAtPosition = new int[10][10]; // Default value of 0 means no ship

	int numPlayerCellsDestroyed = 0;
	Boolean boatDestroyed = false;
	private void placeEnemyShips() {
		int[] shipSizes = {5, 4, 3, 3, 2}; // Sizes of ships
		for (int row = 0; row < 10; row++) {
			for (int col = 0; col < 10; col++) {
				enemyCpuGrid[row][col] = false;
			}
		}
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
	private boolean checkCPUHit(int row, int col, GridPane grid) {
		if (playerGrid[row][col]) {
			numPlayerCellsDestroyed++;
			return true;
		}
		return false;
	}

	private void resetGrid(GridPane grid) {
		// Clear the state of the enemy cpu and player grid's ship placements
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				enemyCpuGrid[i][j] = false;
				playerGrid[i][j] = false;
				shipAtPosition[i][j] = 0; // Reset ship tracking
			}
		}

		for (int i = 0; i < 100; i++) {
			availableCells[i] = true;
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
			System.out.println("Player hit cpu ship");

			// prints whenever a ship is destroyed along with its number (index + 1)
			if (shipsStatus[shipIndex].isDestroyed()) {
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
	private boolean areAllShipsDestroyed(ShipStatus[] shipStatuses) {
		for (ShipStatus status : shipStatuses) {
			if (!status.isDestroyed()) {
				return false;
			}
		}
		return true;
	}
	private void showGameOverImage(boolean playerWon) {
		numPlayerCellsDestroyed = 0;
		String imagePath = playerWon ? "win.png" : "lose.png";
		Image gameOverImage = new Image(imagePath);
		ImageView imageView = new ImageView(gameOverImage);
		imageView.setPreserveRatio(true);
		imageView.setFitHeight(300);
		imageView.setFitWidth(500);


		StackPane gameOverPane = new StackPane(imageView);
		gameOverPane.setAlignment(Pos.CENTER);
		gameOverPane.setStyle("-fx-background-color: transparent;");


		BorderPane root = new BorderPane();
		setBackground(root, "battlebackground.png");
		root.setCenter(gameOverPane);


		Scene gameOverScene = new Scene(root, 1350, 650);


		Platform.runLater(() -> primaryStage.setScene(gameOverScene));


		PauseTransition delay = new PauseTransition(Duration.seconds(3));
		delay.setOnFinished(e -> {
			Platform.runLater(() -> {
				primaryStage.setScene(openScene());
			});
		});
		delay.play();
	}


}
