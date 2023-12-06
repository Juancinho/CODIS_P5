package com.iagofernandezjuanotero;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class RMIServerImpl extends UnicastRemoteObject implements RMIServerInterface {

    private final Map<String, RMIClientInterface> connectedClients;
    private final Map<String, RMIClientImpl> savedClients;
    private final byte[] salt;

    public RMIServerImpl() throws RemoteException {

        super();

        connectedClients = new HashMap<>();
        savedClients = new HashMap<>();
        salt = generateSalt();
    }

    // Note that this 'register' stands for simply accessing the app (not only creating new accounts)
    // Anyway, this method stores the client if it does not exist yet
    @Override
    public void registerClient(String name, String password, RMIClientInterface client) throws RemoteException {

        connectedClients.put(name, client);

        // Adds the newly created client to the database (if it is not yet there)
        if (!savedClients.containsKey(name)) {
            RMIClientImpl rmiClientImpl = new RMIClientImpl(name, password, salt, this);
            savedClients.put(name, rmiClientImpl);
        }

        // Notificar a los clientes existentes sobre la nueva conexión
        for (RMIClientInterface c : connectedClients.values()) {
            if (!c.equals(client)) {
                c.notifyConnection(name);
            }
        }
    }

    @Override
    public void getClientToMessage(String sender, String receiver, String message) throws RemoteException {

        RMIClientInterface receiverClient = connectedClients.get(receiver);
        if (receiverClient != null) {
            receiverClient.receiveMessage(sender, message);
        }
    }

    @Override
    public void unregisterClient(String name) throws RemoteException {

        connectedClients.remove(name);

        // Notificar a los clientes existentes sobre la desconexión
        for (RMIClientInterface c : connectedClients.values()) {
            c.notifyDisconnection(name);
        }
    }

    @Override
    public boolean isUsernameTaken (String name) {

        for (RMIClientImpl c: savedClients.values()) {
            if (c.getUsername().equals(name)) {
                return true;
            }
        }

        return false;
    }

    // For password encryption purposes
    private byte[] generateSalt() {

        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        return salt;
    }
}
