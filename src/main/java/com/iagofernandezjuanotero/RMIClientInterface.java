package com.iagofernandezjuanotero;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIClientInterface extends Remote {

    void receiveMessage(String sender, String message) throws RemoteException;
    void notifyConnection(String newClient) throws RemoteException;
    void notifyDisconnection(String disconnectedClient) throws RemoteException;
}
