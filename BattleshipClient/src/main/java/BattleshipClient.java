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

	TextField c1, nameEnter;
	BorderPane listitemPane;
	Button b1, b2, b3, b4, b5, b6, b7;
	Label l1, l2, l3, l4, l5;
	HashMap<String, Scene> sceneMap;
	VBox clientBox, usernameBox;
	HBox buttonBox;
	Client clientConnection;
	ListView<String> listItems2;
	ContextMenu usernameMenu, groupUsernameMenu, groupsMenu;

	// for label showing username and who message is being sent to
	String userName;
	Boolean sendAll = false;
	Boolean whisper = false;
	Boolean toGroup = false;

	// used to log the username the message is being sent to
	String usernameToSendTo;
	String groupToSendTo;

	ArrayList<String> allUsers = new ArrayList<>();
	ArrayList<String> groupMembers = new ArrayList<>();
	HashMap<String, ArrayList<String>> groupsAhh = new HashMap<>(); // I ran out of good name choices

	// these are used to update the l3 label
	StringProperty s1 = new SimpleStringProperty("NULL");
	StringProperty s2 = new SimpleStringProperty("Choose Destination");
	StringProperty s3 = new SimpleStringProperty("");

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// create a new message class for this user
		allUsers.add("null");
		clientConnection = new Client(data -> {
			Platform.runLater(() -> {
				if ("USERNAME TAKEN".equals(data.toString())) {
					showAlert("Username is already taken. Please choose a different one.");
				}
				else if ("USERNAME GOOD".equals(data.toString())) {
					primaryStage.setScene(sceneMap.get("client"));
					primaryStage.centerOnScreen();
				}
				else {
					// Assuming data is a Message object for normal flow
					Message incomingMessage = (Message) data;

					// if it's from the server saying a new group was created
					if (incomingMessage.isServerGroupMes) {
						groupsAhh = new HashMap<>();
						for (HashMap.Entry<String, ArrayList<String>> entry : incomingMessage.actualGroups.entrySet()) {
							groupsAhh.put(entry.getKey(), new ArrayList<>(entry.getValue()));
						}
						updateGroupShow();
					}
					// if its to a group
					else if (incomingMessage.isToGroup) {
						String displayText = "[GROUP: " + incomingMessage.getGroupName() + "] " +
								incomingMessage.getUserName() + ": " + incomingMessage.getMessage();
						listItems2.getItems().add(displayText);
						listItems2.refresh();
					}
					// If it's for the whole server from a user
					else if (incomingMessage.getIsSendAll()) {
						String displayText = "[GLOBAL] " + incomingMessage.getUserName() + ": " + incomingMessage.getMessage();
						listItems2.getItems().add(displayText);
						listItems2.refresh();
					}
					// if it's a whisper
					else if (incomingMessage.getIsWhisper()) {
						String displayText = "[WHISPER] " + incomingMessage.getUserName() + ": " + incomingMessage.getMessage();
						listItems2.getItems().add(displayText);
						listItems2.refresh();
					}
					// if it's a server message
					else if (incomingMessage.getIsServer()) {
						String displayText = incomingMessage.getUserName() + ": " + incomingMessage.getMessage();
						listItems2.getItems().add(displayText);
						listItems2.refresh();

						allUsers.clear();
						allUsers.addAll(incomingMessage.getUsers());
						groupsAhh = new HashMap<>();
						for (HashMap.Entry<String, ArrayList<String>> entry : incomingMessage.actualGroups.entrySet()) {
							groupsAhh.put(entry.getKey(), new ArrayList<>(entry.getValue()));
						}
						updateGroupShow();
						updateUserMenu();
						updateGroupUserMenu();
						updateGroupShow();

						// for making text red and bold for server messages
						listItems2.setCellFactory(lv -> new ListCell<String>() {
							@Override
							protected void updateItem(String item, boolean empty) {
								super.updateItem(item, empty);
								if (empty || item == null) {
									setText(null);
									setGraphic(null);
								} else {
									setText(item);
									if (item.startsWith("[SERVER]")) {
										// Red and bold for SERVER messages
										setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-family: 'Constantia';");
									} else if (item.startsWith("[GLOBAL]")) {
										// Yellow for GLOBAL messages
										setStyle("-fx-text-fill: black; -fx-font-weight: bold; -fx-font-family: 'Constantia';");
									} else if (item.startsWith("[WHISPER]")) {
										// Light blue for WHISPER messages
										setStyle("-fx-text-fill: blue; -fx-font-weight: bold; -fx-font-family: 'Constantia';");
									} else if (item.startsWith("[GROUP")){
										setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-family: 'Constantia';");
									} else {
										// Default style for any other type of message
										setStyle("-fx-text-fill: black; fx-font-weight: bold; -fx-font-family: 'Constantia';");
									}
								}
							}
						});
					}
				}
			});
		});

		clientConnection.start();

		listItems2 = new ListView<>();

		listitemPane = new BorderPane();
		listitemPane.setPadding(new Insets(10, 10, 0, 10));

		listitemPane.setCenter(listItems2);


		c1 = new TextField();

		b1 = new Button("Send");

		b3 = new Button("Send All");
		b4 = new Button("Create Group");
		b5 = new Button("Members");
		b6 = new Button("Groups");

		// used for hover text
		Tooltip t1 = new Tooltip("Red Means You Are Sending To All");
		Tooltip t2 = new Tooltip("Create A New Group");
		Tooltip t3 = new Tooltip("View/Choose Members");
		Tooltip t4 = new Tooltip("View/Choose Groups");
		t1.setShowDelay(Duration.seconds(0.002));
		t2.setShowDelay(Duration.seconds(0.002));
		t3.setShowDelay(Duration.seconds(0.002));
		t4.setShowDelay(Duration.seconds(0.002));
		Tooltip.install(b3, t1);
		Tooltip.install(b4, t2);
		Tooltip.install(b5, t3);
		Tooltip.install(b6, t4);

		l2 = new Label("Funky Wunky Text Server");

		// s1: username s2: is it a group, user, or whole server s3: group, or users name
		l3 = new Label();
		l3.textProperty().bind(
				// javaFX has no way to auto size text size, which is awesome, so short usernames please
				Bindings.concat("Username: ", s1, " Sending To: ", s2, " ", s3)
		);

		sceneMap = new HashMap<>();

		// the server will hold all usernames, we need a way to contact the server to validate if a username is allowed
		l1 = new Label("Enter unique username");
		nameEnter = new TextField();
		b2 = new Button("Enter");

		sceneMap.put("username", createNameGui());
		sceneMap.put("client",  createClientGui());

		// this is the only button that sends messages
		b1.setOnAction(e->{
			if (!sendAll && !whisper && !toGroup) {
				showAlert("Must Choose Destination");
			}
			else {
				Message messageToSend = new Message();
				messageToSend.setMessage(c1.getText());
				messageToSend.setUserName(userName);

				// flag setting
				messageToSend.setIsNewUser(false); // Since it's not a new user registration message
				messageToSend.setIsNewGroup(false); // not creating a group
				messageToSend.setIsServer(false); // not a server message
				messageToSend.setIsDeletedUser(false); // user still exists if its sending messages
				messageToSend.isToGroup = toGroup;
				messageToSend.setGroupName(groupToSendTo);
				messageToSend.setIsWhisper(whisper); // decided through choosing a user
				messageToSend.setUserNameToSendTo(usernameToSendTo); // if not a whisper it is null
				messageToSend.setIsSendAll(sendAll); // decided through send all button

				// Debugging
				System.out.println("Whisper: " + messageToSend.getIsWhisper());
				System.out.println(messageToSend.getUserNameToSendTo());
				System.out.println("SendAll: " + messageToSend.getIsSendAll());

				clientConnection.send(messageToSend);
				c1.clear();
			}
		});

		/// sets username but checks with server that username is not taken
		b2.setOnAction(e ->{
			newUserEnter();
			s1.setValue(userName);
		});

		// all the "Send All" button does is change the target sender to the whole server
		// user still has to click send button
		b3.setOnAction(e->{
			b5.setStyle("-fx-cursor: hand; -fx-background-color: black; -fx-text-fill: white;");
			String getStyle = b3.getStyle();
			if (getStyle.contains("black")) {
				b3.setStyle("-fx-cursor: hand; -fx-background-color: red; -fx-text-fill: white;");
				b5.setStyle("-fx-cursor: hand; -fx-background-color: black; -fx-text-fill: white;");
				s2.setValue("Whole Server");
				s3.setValue("");
				usernameToSendTo = "";
				whisper = false;
				sendAll = true;
				toGroup = false;
			} else {
				b3.setStyle("-fx-cursor: hand; -fx-background-color: black; -fx-text-fill: white;");
				s2.setValue("Choose Destination");
				s3.setValue("");
				usernameToSendTo = "";
				whisper = false;
				sendAll = false;
				toGroup = false;
			}
		});

		// for listing users, userMenu is updated everytime a [SERVER] message is sent over
		usernameMenu = new ContextMenu();
		groupUsernameMenu = new ContextMenu();
		groupsMenu = new ContextMenu();
		updateUserMenu();
		updateGroupUserMenu();
		updateGroupShow();

		b4.setOnAction(event -> {
			groupUsernameMenu.show(b4, 0, 0);
			Platform.runLater(() -> {
				double menuHeight = groupUsernameMenu.getHeight();
				double posX = b4.localToScreen(b4.getBoundsInLocal()).getMinX();
				double posY = b4.localToScreen(b4.getBoundsInLocal()).getMinY() - menuHeight;
				groupUsernameMenu.hide();
				groupUsernameMenu.show(b4, posX, posY);
			});
			// make text label color back to black
			groupMembers.clear();
			for (MenuItem men : groupUsernameMenu.getItems()) {
				if (men instanceof CustomMenuItem) {
					Node con = ((CustomMenuItem) men).getContent();
					if (con instanceof Label) {
						((Label) con).setStyle("--fx-cursor: hand; fx-text-fill: black;");
					}
				}
			}
		});

		// work around to get the usernameMenu to open upwards, very efficient! (joke)
		b5.setOnAction(event -> {
			usernameMenu.show(b5, 0, 0);
			Platform.runLater(() -> {
				double menuHeight = usernameMenu.getHeight();
				double posX = b5.localToScreen(b5.getBoundsInLocal()).getMinX();
				double posY = b5.localToScreen(b5.getBoundsInLocal()).getMinY() - menuHeight;
				usernameMenu.hide();
				usernameMenu.show(b5, posX, posY);
			});
		});

		b6.setOnAction(event -> {
			System.out.println(groupsAhh);
			groupsMenu.show(b6, 0, 0);
			Platform.runLater(() -> {
				double menuHeight = groupsMenu.getHeight();
				double posX = b6.localToScreen(b6.getBoundsInLocal()).getMinX();
				double posY = b6.localToScreen(b6.getBoundsInLocal()).getMinY() - menuHeight;
				groupsMenu.hide();
				groupsMenu.show(b6, posX, posY);
			});
		});

		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });

		primaryStage.setScene(sceneMap.get("username"));
		primaryStage.setTitle("Client");
		primaryStage.show();
	}

	private void updateUserMenu() {
		usernameMenu.getItems().clear();

		for (String username : allUsers) {
			// do not display the own user
			if (!username.equals(userName)){
				Label label = new Label(username);
				label.setStyle("-fx-cursor: hand;");
				Tooltip tooltip = new Tooltip("Whisper to " + username);
				tooltip.setShowDelay(Duration.seconds(0.002));
				Tooltip.install(label, tooltip);

				CustomMenuItem menuItem = new CustomMenuItem(label, false);
				menuItem.setOnAction(e -> {
					b6.setStyle("-fx-cursor: hand; -fx-background-color: black; -fx-text-fill: white;");
					s2.setValue("User");
					s3.setValue(username);
					usernameToSendTo = username;
					usernameMenu.hide();
					b3.setStyle("-fx-cursor: hand; -fx-background-color: black; -fx-text-fill: white;");
					b5.setStyle("-fx-cursor: hand; -fx-background-color: red; -fx-text-fill: white;");
					sendAll = false;
					whisper = true;
					toGroup = false;
				});

				usernameMenu.getItems().add(menuItem);
			}
		}
	}

	private void updateGroupUserMenu() {
		groupUsernameMenu.getItems().clear();

		for (String username : allUsers) {
			// do not display the own user
			if (!username.equals(userName)){
				Label label = new Label(username);
				label.setStyle("-fx-cursor: hand;-fx-text-fill: black;");
				Tooltip tooltip = new Tooltip("Add " + username + " to group");
				tooltip.setShowDelay(Duration.seconds(0.002));
				Tooltip.install(label, tooltip);
				CustomMenuItem menuItem = new CustomMenuItem(label, false);
				menuItem.setOnAction(e -> {
					if (label.getStyle().contains("black")) {
						label.setStyle("-fx-cursor: hand;-fx-text-fill: red;");
						groupMembers.add(username);
						groupMembers.add(userName); // you will be in all groups you create
						System.out.println(groupMembers);
					}
					else if (label.getStyle().contains("red")) {
						label.setStyle("-fx-cursor: hand;-fx-text-fill: black;");
						groupMembers.remove(username);
						System.out.println(groupMembers);
					}
				});

				groupUsernameMenu.getItems().add(menuItem);
			}
		}

		if (allUsers.size() > 1) {
			Button accept = new Button("Confirm");
			accept.setStyle("-fx-cursor: hand; -fx-background-color: black; -fx-text-fill: white;");
			CustomMenuItem createGroupItem = new CustomMenuItem(accept, false);
			createGroupItem.setOnAction(e -> {
				if (!groupMembers.isEmpty()) {
					Button confirmButton = new Button("Confirm");
					confirmButton.setStyle("-fx-cursor: hand; -fx-background-color: black; -fx-text-fill: white;");

					Button cancelButton = new Button("Cancel");
					cancelButton.setStyle("-fx-cursor: hand; -fx-background-color: black; -fx-text-fill: white;");

					Dialog<String> dialog = new Dialog<>();
					dialog.setTitle("Create Group");

					ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
					ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
					dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, cancelButtonType);

					dialog.getDialogPane().lookupButton(confirmButtonType).setStyle("-fx-cursor: hand; -fx-background-color: black; -fx-text-fill: white;");
					dialog.getDialogPane().lookupButton(cancelButtonType).setStyle("-fx-cursor: hand; -fx-background-color: black; -fx-text-fill: white;");

					GridPane grid = new GridPane();
					grid.setHgap(10);
					grid.setVgap(10);
					grid.setPadding(new Insets(20, 150, 10, 10));

					TextField groupNameField = new TextField();
					groupNameField.setPromptText("Group Name");
					Label label = new Label("Choose a group name:");
					grid.add(label, 0, 0);
					grid.add(groupNameField, 1, 0);

					dialog.getDialogPane().setContent(grid);

					Platform.runLater(groupNameField::requestFocus);

					dialog.setResultConverter(dialogButton -> {
						if (dialogButton == confirmButtonType) {
							return groupNameField.getText();
						}
						return null;
					});

					Optional<String> result = dialog.showAndWait();

					result.ifPresent(groupName -> {
						// TODO: Delete testing statement
						System.out.println("Group name: " + groupName);
						if (groupsAhh.containsKey(groupName)) {
							// TODO: This is a bit redundant maybe gray button out and add tooltip message
							showAlert("Group name is taken try again");
						} else {
							Message sendGroup = new Message();
							sendGroup.setIsNewGroup(true);
							sendGroup.groupNames = groupMembers;
							sendGroup.setGroupName(groupName);

							// send the message to the server saying a group was created
							clientConnection.send(sendGroup);

							// clear the list of group members
							groupMembers.clear();

							groupUsernameMenu.hide();
						}
					});
					groupMembers.clear();
				}
				else {
					showAlert("Must Choose At Least 1 User");
				}
			});
			groupUsernameMenu.setOnCloseRequest(e -> {
				System.out.println("TESTING");
			});
			groupUsernameMenu.getItems().add(createGroupItem);
		}
	}

	private void updateGroupShow() {
		groupsMenu.getItems().clear();
		for (String groupName : groupsAhh.keySet()) {
			if (groupsAhh.get(groupName).contains(userName)) {
				System.out.println(groupName);
				Label label = new Label(groupName);
				label.setStyle("-fx-cursor: hand;");
				Tooltip tooltip = new Tooltip("Whisper in " + groupName);
				tooltip.setShowDelay(Duration.seconds(0.002));
				Tooltip.install(label, tooltip);

				CustomMenuItem menuItem = new CustomMenuItem(label, false);
				menuItem.setOnAction(e -> {
					b5.setStyle("-fx-cursor: hand; -fx-background-color: black; -fx-text-fill: white;");
					b6.setStyle("-fx-cursor: hand; -fx-background-color: red; -fx-text-fill: white;");
					s2.setValue("Group");
					s3.setValue(label.getText());
					groupToSendTo = label.getText();
					toGroup = true;
					whisper = false;
					sendAll = false;
					if (b3.getStyle().contains("red")){
						b3.setStyle("-fx-cursor: hand; -fx-background-color: black; -fx-text-fill: white;");
					}
					groupsMenu.hide();
				});

				groupsMenu.getItems().add(menuItem);
			}
			else {
				System.out.println(groupName);
				Label label = new Label(groupName);
				label.setStyle("-fx-text-fill: gray; -fx-cursor: not-allowed");
				Tooltip tooltip = new Tooltip("Your not in the group " + groupName);
				tooltip.setShowDelay(Duration.seconds(0.002));
				Tooltip.install(label, tooltip);

				CustomMenuItem menuItem = new CustomMenuItem(label, false);
				menuItem.setOnAction(e -> {

				});

				groupsMenu.getItems().add(menuItem);
			}
        }
	}

	// simple error box
	private void showAlert(String msg) {
		// shows an alert box on the screen
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("");
		alert.setHeaderText(null);
		alert.setContentText(msg);
		alert.showAndWait();
	}

	private void newUserEnter() {
		// try catch block to validate username
		try {
			if (nameEnter.getText().isEmpty()) {
				showAlert("Must Enter A Username");
			} else {
				Message user = new Message();
				user.setUserName(nameEnter.getText());
				userName = nameEnter.getText();
				user.setIsNewUser(true);
				clientConnection.send(user);
			}
		} catch (NumberFormatException f) {
			showAlert("Must Enter A Valid Username");
		}
	}

	public Scene createNameGui() {
		usernameBox = new VBox(10);
		usernameBox.setAlignment(Pos.CENTER);

		l1.setStyle("-fx-cursor: not-allowed; -fx-font-family: 'Constantia'; -fx-text-fill: black; " +
				"-fx-font-size: 15px; -fx-font-weight: bold;");

		nameEnter.setStyle("-fx-border-color: black; -fx-border-radius: 10; -fx-background-radius: 10;");
		nameEnter.setMaxWidth(200);

		b2.setStyle("-fx-cursor: hand; -fx-background-color: black; -fx-text-fill: white;");

		usernameBox.getChildren().addAll(l1, nameEnter, b2);

        return new Scene(usernameBox, 400, 300);
	}

	public Scene createClientGui() {

		buttonBox = new HBox(10, b1, b3, b4, b5, b6);
		clientBox = new VBox(10, l2, l3, listitemPane, c1, buttonBox);

		clientBox.setAlignment(Pos.CENTER);
		buttonBox.setAlignment(Pos.CENTER);

		l2.setStyle("-fx-cursor: not-allowed; -fx-font-family: 'Constantia'; " +
				"-fx-font-size: 23px; -fx-font-weight: bold;" +
				"-fx-text-fill: linear-gradient(to left, black);");

		// this will change
		l3.setStyle("-fx-cursor: not-allowed; -fx-font-family: 'Constantia'; " +
				"-fx-font-size: 15px; -fx-font-weight: bold;" + "-fx-text-fill: red;");

		listItems2.setStyle("-fx-border-color: black;");

		c1.setStyle("-fx-border-color: black; -fx-border-radius: 10; -fx-background-radius: 10;");
		c1.setMaxWidth(600);

		b1.setStyle("-fx-cursor: hand; -fx-background-color: black; -fx-text-fill: white;");
		b3.setStyle("-fx-cursor: hand; -fx-background-color: black; -fx-text-fill: white;");
		b4.setStyle("-fx-cursor: hand; -fx-background-color: black; -fx-text-fill: white;");
		b5.setStyle("-fx-cursor: hand; -fx-background-color: black; -fx-text-fill: white;");
		b6.setStyle("-fx-cursor: hand; -fx-background-color: black; -fx-text-fill: white;");

		return new Scene(clientBox, 800, 600);

	}

}
