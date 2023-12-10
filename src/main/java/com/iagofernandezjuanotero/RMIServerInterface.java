package com.iagofernandezjuanotero;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface RMIServerInterface extends Remote {

    public ClientData getClientData(String username) throws RemoteException;
    void registerClient(String name, String passwordHash, RMIClientInterface client) throws RemoteException;
    void unregisterClient(String name) throws RemoteException;
    public RMIClientInterface getClientToMessage(String receiver) throws RemoteException;
    public void createClientRequest(String requestedClient, String requesterClient) throws RemoteException;
    public void rejectClientRequest(String requestedClient, String requesterClient) throws RemoteException;
    public void acceptClientRequest(String requestedClient, String requesterClient) throws RemoteException;
    boolean isUsernameTaken(String name) throws RemoteException;
    boolean isUserOnline(String name) throws RemoteException;
    boolean verifyPassword(String username, String password) throws RemoteException;
    ArrayList<String> getOnlineClientsNames() throws RemoteException;
    ArrayList<String> getStoredClientsNames() throws RemoteException;
    boolean isFriend(String client1, String client2) throws RemoteException;
    String calcHashForGivenPassword(String password) throws RemoteException;
}
