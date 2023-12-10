package com.iagofernandezjuanotero;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.Serializable;
import java.net.URL;
import java.rmi.RemoteException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

// Must be serializable because of transitivity to MainControllerData (which is indeed serializable for network transfers)
public class MainController implements Initializable, Serializable {

    // There are serialization problems with JavaFX components (they are more complex than average Java classes)
    // It seems compulsory to work with transient attributes and taking care of method calls
    @FXML
    private transient AnchorPane rootPane;

    @FXML
    private transient ScrollPane scrollPane;

    @FXML
    private transient TextField messageTextField;

    @FXML
    private ChoiceBox<String> pendingRequestsChoiceBox;

    @FXML
    private transient TextField friendRequestText;

    @FXML
    private transient ComboBox<String> receiverComboBox;

    @FXML
    private transient TextFlow textFlow;

    private RMIServerInterface rmiServerInterface;
    private RMIClientInterface rmiClientInterface;

    private static final int MAX_MESSAGES = 100;

    public void setRmiServerInterface(RMIServerInterface rmiServerInterface) {

        this.rmiServerInterface = rmiServerInterface;
    }

    public RMIServerInterface getRmiServerInterface() {

        return rmiServerInterface;
    }

    public void setRmiClientInterface(RMIClientInterface rmiClientInterface) {

        this.rmiClientInterface = rmiClientInterface;
    }

    public RMIClientInterface getRmiClientInterface() {

        return rmiClientInterface;
    }

    @FXML
    void onAcceptButtonClick(ActionEvent event) throws RemoteException {

        String requesterClient = pendingRequestsChoiceBox.getValue();

        if (isValidUsername(requesterClient)) {

            rmiServerInterface.acceptClientRequest(rmiClientInterface.getUsername(), requesterClient);
        }
    }

    @FXML
    void onRejectButtonClick(ActionEvent event) throws RemoteException {

        String requesterClient = pendingRequestsChoiceBox.getValue();

        if (isValidUsername(requesterClient)) {

            rmiServerInterface.rejectClientRequest(rmiClientInterface.getUsername(), requesterClient);
        }
    }

    @FXML
    void onFriendRequestButtonClick(ActionEvent event) throws RemoteException {

        String requestedClient = friendRequestText.getText();

        if (isValidUsername(requestedClient)) {

            if (rmiServerInterface.isUsernameTaken(requestedClient)) {

                // If client has already an incoming friendship request, then simply adds the user
                if (rmiServerInterface.getClientData(rmiClientInterface.getUsername()).getPendingSentFriendshipRequests().contains(requestedClient)) {
                    rmiServerInterface.acceptClientRequest(requestedClient, rmiClientInterface.getUsername());
                } else {
                    rmiServerInterface.createClientRequest(requestedClient, rmiClientInterface.getUsername());
                }
            } else {
                rmiClientInterface.printError("El usuario '" + requestedClient +"' no existe");
            }
        }
    }

    @FXML
    void onSendButtonClick(ActionEvent event) throws RemoteException {

        String receiver = receiverComboBox.getValue();
        String message = messageTextField.getText();

        if (isValidText(message) && isValidUsername(receiver)) {

            if (rmiServerInterface.isUserOnline(receiver)) {
                rmiServerInterface.getClientToMessage(receiver).receiveMessage(rmiClientInterface.getUsername(), message);
                rmiClientInterface.sendMessage(receiver, message);
            } else {
                rmiClientInterface.printError("El usuario '" + receiver +"' no está conectado");
            }
        }
    }

    private boolean isValidText(String message) {

        return message != null && !message.trim().isEmpty();
    }

    private boolean isValidUsername(String clientName) {

        return clientName != null && !clientName.trim().isEmpty();
    }

    @FXML
    public void loseFocus() {

        // Cannot be set on initialize as drivers are not ready yet
        rootPane.requestFocus();
    }

    @FXML
    public void updatePendingRequestsChoiceBox() throws RemoteException {

        pendingRequestsChoiceBox.setItems(FXCollections.observableArrayList(rmiServerInterface.getClientData(rmiClientInterface.getUsername()).getPendingReceivedFriendshipRequests()));
    }

    @FXML
    public void updateReceiverComboBox() throws RemoteException {

        receiverComboBox.setItems(FXCollections.observableArrayList(rmiServerInterface.getClientData(rmiClientInterface.getUsername()).getAddedFriends()));
    }

    // Method that prints data in the console (both messages from/to other users or related to the program itself)
    @FXML
    public void printToConsole (String message) {

        // Gets current time, to print formatted text
        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedTime = currentTime.format(timeFormatter);

        // Formats the text itself
        Text text = new Text ("[" + formattedTime + "] " + message + "\n");

        // Adds a text object to the textFlow (simulating kind of a terminal). If the max size has been reached,
        // it simply removes the older text
        textFlow.getChildren().add(0, text);
        if (textFlow.getChildren().size() > MAX_MESSAGES) {
            textFlow.getChildren().remove(MAX_MESSAGES);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Prints to console that the window is ready
        printToConsole("SISTEMA: Se ha inicializado el programa correctamente");
    }
}

