package com.iagofernandezjuanotero;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface RMIClientInterface extends Remote {

    void setMainControllerData(MainControllerData mainControllerData) throws RemoteException;
    String getUsername() throws RemoteException;
    ArrayList<String> getPendingFriendshipRequests() throws RemoteException;
    String getPasswordHash() throws RemoteException;
    MainControllerData getMainControllerData() throws RemoteException;

    void receiveMessage(String sender, String message) throws RemoteException;
    void notifyConnection(String newClient) throws RemoteException;
    void notifyDisconnection(String disconnectedClient) throws RemoteException;
    void sendMessage(String receiver, String message) throws RemoteException;
}
