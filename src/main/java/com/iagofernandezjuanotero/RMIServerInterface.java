package com.iagofernandezjuanotero;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;

public interface RMIServerInterface extends Remote {

    void registerClient(String name, String password, RMIClientInterface client) throws RemoteException;
    void getClientToMessage(String sender, String receiver, String message) throws RemoteException;
    void unregisterClient(String name) throws RemoteException;
    boolean isUsernameTaken(String name) throws RemoteException;
    RMIClientInterface createNewClient(String username, String password) throws RemoteException;
    boolean verifyPassword(String username, String password) throws RemoteException;
    public ArrayList<String> getOnlineClientsNames() throws RemoteException;
    public ArrayList<String> getStoredClientsNames() throws RemoteException;
}
