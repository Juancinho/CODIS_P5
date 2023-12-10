/*
 * Actividad: Aplicaciones P2P. Clase implementación del cliente RMI
 * Fecha: Miércoles, 29 de noviembre de 2023
 * Autores: Iago Fernández Perlo y Juan Otero Rivas
 */

package com.iagofernandezjuanotero;

import javafx.application.Platform;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RMIClientImpl extends UnicastRemoteObject implements RMIClientInterface {

    // Declares the username and passwordHash (encrypted in the server), and the mainController reference
    private final String username;
    private final String passwordHash;
    private MainController mainController;

    public RMIClientImpl(String username, String passwordHash) throws RemoteException {

        super();

        this.username = username;
        this.passwordHash = passwordHash;
    }

    @Override
    public String getUsername() throws RemoteException {

        return username;
    }

    @Override
    public String getPasswordHash() throws RemoteException {

        return passwordHash;
    }

    @Override
    public void setMainController(MainController mainController) throws RemoteException {

        this.mainController = mainController;
    }

    // All the functions underneath just print custom messages for certain situations
    // They could have been only one function, but this encapsulates the contents and makes it
    // clearer to understand what the server calls are intended to do in the client side
    @Override
    public void receiveMessage(String sender, String message) throws RemoteException {

        Platform.runLater(() -> mainController.printToConsole("ENTRADA ('" + username + "' ← '" + sender + "'): " + message));
    }

    @Override
    public void sendMessage(String receiver, String message) throws RemoteException {

        Platform.runLater(() -> mainController.printToConsole("SALIDA ('" + username + "' → '" + receiver + "'): " + message));
    }

    @Override
    public void notifyConnection(String newClient, boolean isNewClient) throws RemoteException {

        Platform.runLater(() -> {
            try {
                // This, and some other functions, modify data that is printed in the UI boxes, so must update them
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

        Platform.runLater(() -> mainController.printToConsole("AMISTAD: Solicitud de amistad enviada correctamente a '" + requestedClient + "'"));
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

        Platform.runLater(() -> mainController.printToConsole("AMISTAD: '" + requestedClient + "' ha rechazado tu solicitud de amistad"));
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

        Platform.runLater(() -> mainController.printToConsole("SISTEMA: " + message));
    }

    @Override
    public void printError(String message) throws RemoteException {

        Platform.runLater(() -> mainController.printToConsole("ERROR: " + message));
    }
}
