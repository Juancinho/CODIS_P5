package com.iagofernandezjuanotero;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
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
import java.util.ArrayList;
import java.util.ResourceBundle;

// Must be serializable because of transitivity to MainControllerData (which is indeed serializable for network transfers)
public class MainController implements Initializable, Serializable {

    // There are serialization problems with JavaFX components (they are more complex than average Java classes)
    // It seems compulsory to work with transient attributes and taking care of method calls
    @FXML
    private AnchorPane rootPane;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private TextField messageTextField;

    @FXML
    private ChoiceBox<String> friendRequestChoiceBox;

    @FXML
    private ChoiceBox<String> receiverChoiceBox;

    @FXML
    private TextFlow textFlow;

    private RMIServerInterface rmiServerInterface;
    private RMIClientInterface rmiClientInterface;

    private MainControllerData mainControllerData;
    private static final int MAX_MESSAGES = 100;

    public void setRmiServerInterface(RMIServerInterface rmiServerInterface) {

        this.rmiServerInterface = rmiServerInterface;
        mainControllerData.setRmiServerInterface(rmiServerInterface);
    }

    public RMIServerInterface getRmiServerInterface() {

        return rmiServerInterface;
    }

    public void setRmiClientInterface(RMIClientInterface rmiClientInterface) {

        this.rmiClientInterface = rmiClientInterface;
        mainControllerData.setRmiClientInterface(rmiClientInterface);
    }

    public RMIClientInterface getRmiClientInterface() {

        return rmiClientInterface;
    }

    MainControllerData getMainControllerData() {

        return mainControllerData;
    }

    @FXML
    void onFriendRequestButtonClick(ActionEvent event) {

    }

    @FXML
    void onSendButtonClick(ActionEvent event) {

    }

    @FXML
    public void loseFocus() {

        // Cannot be set on initialize as drivers are not ready yet
        rootPane.requestFocus();
    }

    @FXML
    public void updateFriendRequestChoiceBox() throws RemoteException {

        ArrayList<String> storedClients = rmiServerInterface.getStoredClientsNames();
        ObservableList<String> observableStoredClients = FXCollections.observableArrayList(storedClients);
        observableStoredClients.remove(rmiClientInterface.getUsername());

        friendRequestChoiceBox.setItems(observableStoredClients);

        mainControllerData.setStoredClients(storedClients);
    }

    @FXML
    public void updateReceiverChoiceBox() throws RemoteException {

        ArrayList<String> onlineClients = rmiServerInterface.getOnlineClientsNames();
        ObservableList<String> observableOnlineClients = FXCollections.observableArrayList(onlineClients);
        observableOnlineClients.remove(rmiClientInterface.getUsername());

        receiverChoiceBox.setItems(observableOnlineClients);

        mainControllerData.setOnlineClients(onlineClients);
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

        mainControllerData = new MainControllerData(this, rmiServerInterface, rmiClientInterface);

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Prints to console that window is ready
        printToConsole("SISTEMA: Se ha inicializado el programa correctamente");
    }
}

