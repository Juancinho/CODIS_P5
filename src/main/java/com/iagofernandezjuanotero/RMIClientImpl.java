package com.iagofernandezjuanotero;

import javafx.application.Platform;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RMIClientImpl extends UnicastRemoteObject implements RMIClientInterface {

    private String username;
    private ArrayList<String> pendingFriendshipRequests;
    private RMIServerInterface server;
    private String passwordHash;

    public RMIClientImpl(String username, String passwordHash, RMIServerInterface server) throws RemoteException {

        super();

        this.username = username;
        this.passwordHash = passwordHash;
        pendingFriendshipRequests = new ArrayList<>();
        this.server = server;

        server.registerClient(username, passwordHash, this);
        // This previous line may lead to infinite loop exceptions (work carefully)
    }

    // Useful when casting from client interface to implementation (initializes all attributes)
    public void setParameters (String username, String passwordHash, RMIServerInterface server) {

        this.username = username;
        this.passwordHash = passwordHash;
        pendingFriendshipRequests = new ArrayList<>();
        this.server = server;
    }

    @Override
    public String getUsername() throws RemoteException {

        return username;
    }

    @Override
    public ArrayList<String> getPendingFriendshipRequests() throws RemoteException {

        return pendingFriendshipRequests;
    }

    @Override
    public String getPasswordHash() throws RemoteException {

        return passwordHash;
    }

    @Override
    public void receiveMessage(String sender, String message) throws RemoteException {

        System.out.println("Mensaje de '" + sender + "': " + message);
    }

    @Override
    public void notifyConnection(String newClient) throws RemoteException {

        System.out.println("'" + newClient + "' se ha conectado");
    }

    @Override
    public void notifyDisconnection(String disconnectedClient) throws RemoteException {

        System.out.println("'" + disconnectedClient + "' se ha desconectado");
    }

    @Override
    public void sendMessage(String receiver, String message) throws RemoteException {

        server.getClientToMessage(username, receiver, message);
    }
}
