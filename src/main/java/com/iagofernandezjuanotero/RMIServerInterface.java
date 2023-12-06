package com.iagofernandezjuanotero;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIServerInterface extends Remote {

    void registerClient(String name, String password, RMIClientInterface client) throws RemoteException;
    void getClientToMessage(String sender, String receiver, String message) throws RemoteException;
    void unregisterClient(String name) throws RemoteException;
    public boolean isUsernameTaken(String name);
    public RMIClientImpl createNewClient(String username, String password) throws RemoteException;
    public boolean verifyPassword(String username, String password);
}
