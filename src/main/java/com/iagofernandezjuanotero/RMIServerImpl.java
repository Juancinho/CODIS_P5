package com.iagofernandezjuanotero;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class RMIServerImpl extends UnicastRemoteObject implements RMIServerInterface {

    private final Map<String, RMIClientInterface> connectedClients;
    private final Map<String, RMIClientInterface> savedClients;

    public RMIServerImpl() throws RemoteException {

        super();
        connectedClients = new HashMap<>();
        savedClients = new HashMap<>();
    }

    @Override
    public void registerClient(String name, RMIClientInterface client) throws RemoteException {

        connectedClients.put(name, client);

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
}
