/*
 * Actividad: Aplicaciones P2P. Clase controlador de la vista principal
 * Fecha: Miércoles, 29 de noviembre de 2023
 * Autores: Iago Fernández Perlo y Juan Otero Rivas
 */

package com.iagofernandezjuanotero;

import javafx.collections.FXCollections;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ResourceBundle;

// Must be serializable, to prevent remote connection problems with JavaRMI
// This leads to compatibility problems between JavaFX and JavaRMI (there is no API between them)
// The solution taken was to declare JavaFX as transient (so they are not sent, preventing serialization
// exceptions), but the references must be updated with extreme caution to prevent null pointers
public class MainController implements Initializable, Serializable {

    // Must declare as transient all JavaFX components (those with the tag FXML), as they are more complex
    // than average Java classes, and can not be serialized by JavaRMI
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

    // References to both interfaces
    private RMIServerInterface rmiServerInterface;
    private RMIClientInterface rmiClientInterface;

    // Constant for the maximum number of messages that can be shown in the main UI panel
    private static final int MAX_MESSAGES = 100;

    public void setRmiServerInterface(RMIServerInterface rmiServerInterface) {

        this.rmiServerInterface = rmiServerInterface;
    }

    public void setRmiClientInterface(RMIClientInterface rmiClientInterface) {

        this.rmiClientInterface = rmiClientInterface;
    }

    // When accept button is clicked, it gets the value from the choice box next to him, and if the username is valid,
    // calls the server method where the responsibility of the request acceptation relies on
    @FXML
    void onAcceptButtonClick() throws RemoteException {

        String requesterClient = pendingRequestsChoiceBox.getValue();

        if (isValidUsername(requesterClient)) {

            rmiServerInterface.acceptClientRequest(rmiClientInterface.getUsername(), requesterClient);
        }
    }

    // Analogue to above, in this case works with the reject button but the same choice box
    @FXML
    void onRejectButtonClick() throws RemoteException {

        String requesterClient = pendingRequestsChoiceBox.getValue();

        if (isValidUsername(requesterClient)) {

            rmiServerInterface.rejectClientRequest(rmiClientInterface.getUsername(), requesterClient);
        }
    }

    // Method called when the user clicks the friend request send button
    @FXML
    void onFriendRequestButtonClick() throws RemoteException {

        String requestedClient = friendRequestText.getText();

        // Does all required checks, and applies the method printError to print messages to the console (errors in this case)
        if (isValidUsername(requestedClient)) {

            if (rmiServerInterface.isUsernameTaken(requestedClient)) {

                if (!rmiClientInterface.getUsername().equals(requestedClient)) {

                    // If client has already an incoming friendship request, then simply adds the user
                    if (rmiServerInterface.getClientData(rmiClientInterface.getUsername()).getPendingReceivedFriendshipRequests().contains(requestedClient)) {

                        rmiServerInterface.acceptClientRequest(requestedClient, rmiClientInterface.getUsername());

                    } else {

                        // If the client username is valid, call the server method that creates the links and stores all the required data
                        rmiServerInterface.createClientRequest(requestedClient, rmiClientInterface.getUsername());
                    }
                } else {
                    rmiClientInterface.printError("No puedes enviarte una solicitud de amistad a ti mismo");
                }
            } else {
                rmiClientInterface.printError("El usuario '" + requestedClient +"' no existe");
            }
        }
    }

    // Method called when button click is pressed (note that these last four methods depend on the value from text fields or choice boxes)
    @FXML
    void onSendButtonClick() throws RemoteException {

        String receiver = receiverComboBox.getValue();
        String message = messageTextField.getText();

        // Once again, extracts data from the fields, checks and calls the server method
        if (isValidText(message) && isValidUsername(receiver)) {

            if(rmiServerInterface.isUsernameTaken(receiver)) {

                if (rmiServerInterface.isUserOnline(receiver)) {

                    if (!rmiClientInterface.getUsername().equals(receiver)) {

                        // In this case, as the message must be sent between user and receiver (clients, p2p) without server participation,
                        // this client gets the reference to the other client, and calls the receiveMessage() on it. Also calls sendMessage() on itself
                        rmiServerInterface.getClientToMessage(receiver).receiveMessage(rmiClientInterface.getUsername(), message);
                        rmiClientInterface.sendMessage(receiver, message);

                    } else {
                        rmiClientInterface.printError("No puedes enviarte un mensaje a ti mismo");
                    }
                } else {
                    rmiClientInterface.printError("El usuario '" + receiver +"' no está conectado");
                }
            } else {
                rmiClientInterface.printError("El usuario '" + receiver +"' no existe");
            }
        }
    }

    // Classic checkers of non-null and non-empty string values
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

    // Methods that update the choice and combo boxes in the UI. Note that the ComboBox must show all the connected friends (as
    // the method itself is intended to work with sendMessage(), so it must return valid (online and friend) clients. However,
    // the ChoiceBox shows all the clients that requested to be friends with the user, but they can be offline (this is a feature of the program)
    @FXML
    public void updatePendingRequestsChoiceBox() throws RemoteException {

        pendingRequestsChoiceBox.setItems(FXCollections.observableArrayList(rmiServerInterface.getClientData(rmiClientInterface.getUsername()).getPendingReceivedFriendshipRequests()));
    }

    @FXML
    public void updateReceiverComboBox() throws RemoteException {

        ArrayList<String> onlineAddedFriends = rmiServerInterface.getClientData(rmiClientInterface.getUsername()).getAddedFriends();

        // Must use an iterator. With a loop, it falls into concurrent modification exceptions
        Iterator<String> iterator = onlineAddedFriends.iterator();
        while (iterator.hasNext()) {
            String friend = iterator.next();

            if (!rmiServerInterface.isUserOnline(friend)) {
                iterator.remove();
            }
        }

        receiverComboBox.setItems(FXCollections.observableArrayList(onlineAddedFriends));
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

    // Similar to above, but gets the data from the ArrayList of Strings stored in the ClientData for a client (class on which
    // the whole database is based). It does not print time trace, and prints "OFFLINE" instead.
    @FXML
    public void printWhileOfflineMessages() throws RemoteException {

        // Simple function that gets the messages received while offline and prints them locally
        for (String message: rmiServerInterface.getClientData(rmiClientInterface.getUsername()).getWhileOfflineMessageStack()) {

            // Works similarly to printToConsole(), but without time stamp
            Text text = new Text ("[OFFLINE] " + message + "\n");

            textFlow.getChildren().add(0, text);
            if (textFlow.getChildren().size() > MAX_MESSAGES) {
                textFlow.getChildren().remove(MAX_MESSAGES);
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Prints to console that the window is ready
        printToConsole("SISTEMA: Se ha inicializado el programa correctamente");
    }
}

