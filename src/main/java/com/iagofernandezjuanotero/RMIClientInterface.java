/*
 * Actividad: Aplicaciones P2P. Clase interfaz del cliente RMI
 * Fecha: Miércoles, 29 de noviembre de 2023
 * Autores: Iago Fernández Perlo y Juan Otero Rivas
 */

package com.iagofernandezjuanotero;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIClientInterface extends Remote {

    // General purpose getters and setters
    String getUsername() throws RemoteException;
    String getPasswordHash() throws RemoteException;
    void setMainController(MainController mainController) throws RemoteException;

    // Message printing on client side methods
    void receiveMessage(String sender, String message) throws RemoteException;
    void sendMessage(String receiver, String message) throws RemoteException;
    void notifyConnection(String newClient, boolean isNewClient) throws RemoteException;
    void notifyDisconnection(String disconnectedClient) throws RemoteException;
    void notifySentFriendRequest(String requestedClient) throws RemoteException;
    void notifyReceivedFriendRequest(String requesterClient) throws RemoteException;
    void notifyAcceptedSentFriendRequest(String requestedClient) throws RemoteException;
    void notifyAcceptedReceivedFriendRequest(String requesterClient) throws RemoteException;
    void notifyRejectedSentFriendRequest(String requestedClient) throws RemoteException;
    void notifyRejectedReceivedFriendRequest(String requesterClient) throws RemoteException;
    void printInfo(String message) throws RemoteException;
    void printError(String message) throws RemoteException;
}
