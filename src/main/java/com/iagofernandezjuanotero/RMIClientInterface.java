package com.iagofernandezjuanotero;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIClientInterface extends Remote {

    String getUsername() throws RemoteException;
    String getPasswordHash() throws RemoteException;
    public void setMainController(MainController mainController) throws RemoteException;

    void receiveMessage(String sender, String message) throws RemoteException;
    void sendMessage(String receiver, String message) throws RemoteException;
    void notifyConnection(String newClient, boolean isNewClient) throws RemoteException;
    void notifyDisconnection(String disconnectedClient) throws RemoteException;
    public void notifySentFriendRequest(String requestedClient) throws RemoteException;
    public void notifyReceivedFriendRequest(String requesterClient) throws RemoteException;
    public void notifyAcceptedSentFriendRequest(String requestedClient) throws RemoteException;
    public void notifyAcceptedReceivedFriendRequest(String requesterClient) throws RemoteException;
    public void notifyRejectedSentFriendRequest(String requestedClient) throws RemoteException;
    public void notifyRejectedReceivedFriendRequest(String requesterClient) throws RemoteException;
    public void printInfo(String message) throws RemoteException;
    public void printError(String message) throws RemoteException;
}
