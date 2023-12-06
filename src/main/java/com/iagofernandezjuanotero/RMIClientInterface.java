package com.iagofernandezjuanotero;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface RMIClientInterface extends Remote {

    public String getUsername();
    public ArrayList<String> getPendingFriendshipRequests();

    void receiveMessage(String sender, String message) throws RemoteException;
    void notifyConnection(String newClient) throws RemoteException;
    void notifyDisconnection(String disconnectedClient) throws RemoteException;
    void sendMessage(String receiver, String message) throws RemoteException;
}
