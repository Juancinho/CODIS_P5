package com.iagofernandezjuanotero;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class RMIClientImpl extends UnicastRemoteObject implements RMIClientInterface {

    private String username;
    private String passwordHash;
    private ArrayList<String> pendingFriendshipRequests;
    private RMIServerInterface rmiServerInterface;
    private MainController mainController;

    public RMIClientImpl(String username, String passwordHash, RMIServerInterface rmiServerInterface) throws RemoteException {

        super();

        this.username = username;
        this.passwordHash = passwordHash;
        pendingFriendshipRequests = new ArrayList<>();
        this.rmiServerInterface = rmiServerInterface;

        //server.registerClient(username, passwordHash, this);
        // This previous line may lead to infinite loop exceptions (work carefully)
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

    public MainController getMainController() {

        return mainController;
    }

    @Override
    public void setMainController(MainController mainController) throws RemoteException {

        this.mainController = mainController;
    }

    @Override
    public void receiveMessage(String sender, String message) throws RemoteException {

        System.out.println("Mensaje de '" + sender + "': " + message);
        mainController.printToConsole("Mensaje de '" + sender + "': " + message);

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

        rmiServerInterface.getClientToMessage(username, receiver, message);
    }
}
