package com.iagofernandezjuanotero;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIServerInterface extends Remote {

    void registerClient(String name, String password, RMIClientInterface client) throws RemoteException;
    void getClientToMessage(String sender, String receiver, String message) throws RemoteException;
    void unregisterClient(String name) throws RemoteException;
    boolean isUsernameTaken(String name) throws RemoteException;
    RMIClientImpl createNewClient(String username, String password) throws RemoteException;
    boolean verifyPassword(String username, String password) throws RemoteException;
}
