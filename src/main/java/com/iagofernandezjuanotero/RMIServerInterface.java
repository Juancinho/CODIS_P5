package com.iagofernandezjuanotero;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIServerInterface extends Remote {

    void registerClient(String name, RMIClientInterface client) throws RemoteException;
    void sendMessage(String sender, String receiver, String message) throws RemoteException;
    void unregisterClient(String name) throws RemoteException;
}