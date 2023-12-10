package com.iagofernandezjuanotero;

import javafx.application.Platform;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class RMIClientImpl extends UnicastRemoteObject implements RMIClientInterface {

    private String username;
    private String passwordHash;
    private RMIServerInterface rmiServerInterface;
    private MainController mainController;

    public RMIClientImpl(String username, String passwordHash, RMIServerInterface rmiServerInterface) throws RemoteException {

        super();

        this.username = username;
        this.passwordHash = passwordHash;
        this.rmiServerInterface = rmiServerInterface;

        //server.registerClient(username, passwordHash, this);
        // This previous line may lead to infinite loop exceptions (work carefully)
    }

    @Override
    public String getUsername() throws RemoteException {

        return username;
    }

    @Override
    public String getPasswordHash() throws RemoteException {

        return passwordHash;
    }

    public MainController getMainController() {

        return mainController;
    }

    @Override
    public void setMainController(MainController mainController) throws RemoteException {

        this.mainController = mainController;
    }

    @Override
    public void receiveMessage(String sender, String message) throws RemoteException {

        Platform.runLater(() -> {
            mainController.printToConsole("ENTRADA ('" + username + "' ← '" + sender + "'): " + message);
        });
    }

    @Override
    public void sendMessage(String receiver, String message) throws RemoteException {

        Platform.runLater(() -> {
            mainController.printToConsole("SALIDA ('" + username + "' → '" + receiver + "'): " + message);
        });
    }

    @Override
    public void notifyConnection(String newClient, boolean isNewClient) throws RemoteException {

        Platform.runLater(() -> {
            try {
                mainController.updateReceiverComboBox();
            } catch (RemoteException e) {
                System.out.println("Excepción de acceso remoto: " + e.getMessage());
            }

            mainController.printToConsole("SISTEMA: '" + newClient + "' se ha conectado");
        });
    }

    @Override
    public void notifyDisconnection(String disconnectedClient) throws RemoteException {

        Platform.runLater(() -> {
            try {
                mainController.updateReceiverComboBox();
            } catch (RemoteException e) {
                System.out.println("Excepción de acceso remoto: " + e.getMessage());
            }

            mainController.printToConsole("SISTEMA: '" + disconnectedClient + "' se ha desconectado");
        });
    }

    @Override
    public void notifySentFriendRequest(String requestedClient) throws RemoteException {

        Platform.runLater(() -> {
            mainController.printToConsole("AMISTAD: Solicitud de amistad enviada correctamente a '" + requestedClient + "'");
        });
    }

    @Override
    public void notifyReceivedFriendRequest(String requesterClient) throws RemoteException {

        Platform.runLater(() -> {
            try {
                mainController.updatePendingRequestsChoiceBox();
            } catch (RemoteException e) {
                System.out.println("Excepción de acceso remoto: " + e.getMessage());
            }

            mainController.printToConsole("AMISTAD: '" + requesterClient + "' ha solicitado ser tu amigo");
        });
    }

    @Override
    public void notifyAcceptedSentFriendRequest(String requestedClient) throws RemoteException {

        Platform.runLater(() -> {
            try {
                mainController.updateReceiverComboBox();
            } catch (RemoteException e) {
                System.out.println("Excepción de acceso remoto: " + e.getMessage());
            }

            mainController.printToConsole("AMISTAD: '" + requestedClient + "' ha aceptado tu solicitud de amistad. Ahora sois amigos");
        });
    }

    @Override
    public void notifyAcceptedReceivedFriendRequest(String requesterClient) throws RemoteException {

        Platform.runLater(() -> {
            try {
                mainController.updateReceiverComboBox();
                mainController.updatePendingRequestsChoiceBox();
            } catch (RemoteException e) {
                System.out.println("Excepción de acceso remoto: " + e.getMessage());
            }

            mainController.printToConsole("AMISTAD: Has aceptado la solicitud de amistad de '" + requesterClient + "'. Ahora sois amigos");
        });
    }

    @Override
    public void notifyRejectedSentFriendRequest(String requestedClient) throws RemoteException {

        Platform.runLater(() -> {
            mainController.printToConsole("AMISTAD: '" + requestedClient + "' ha rechazado tu solicitud de amistad");
        });
    }

    @Override
    public void notifyRejectedReceivedFriendRequest(String requesterClient) throws RemoteException {

        Platform.runLater(() -> {
            try {
                mainController.updatePendingRequestsChoiceBox();
            } catch (RemoteException e) {
                System.out.println("Excepción de acceso remoto: " + e.getMessage());
            }

            mainController.printToConsole("AMISTAD: Has rechazado la solicitud de amistad de '" + requesterClient + "'");
        });
    }

    @Override
    public void printInfo(String message) throws RemoteException {

        Platform.runLater(() -> {
            mainController.printToConsole("SISTEMA: " + message);
        });
    }

    @Override
    public void printError(String message) throws RemoteException {

        Platform.runLater(() -> {
            mainController.printToConsole("ERROR: " + message);
        });
    }
}
