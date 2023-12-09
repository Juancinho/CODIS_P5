package com.iagofernandezjuanotero;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RMIServerImpl extends UnicastRemoteObject implements RMIServerInterface {

    private final Map<String, RMIClientInterface> connectedClients;
    private final Map<String, ClientData> userDatabase;
    private final byte[] salt;

    public RMIServerImpl() throws RemoteException {

        super();

        connectedClients = new HashMap<>();
        userDatabase = new HashMap<>();
        salt = generateSalt();
    }

    // Note that this 'register' stands for simply accessing the app (not only creating new accounts)
    // Anyway, this method stores the client if it does not exist yet (so it does actually register new users to the server)
    @Override
    public void registerClient(String name, String passwordHash, RMIClientInterface client) throws RemoteException {

        ClientHandlerThread clientHandlerThread = new ClientHandlerThread(name, passwordHash, client);
        clientHandlerThread.start();
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
    public boolean isUsernameTaken(String name) throws RemoteException{

        return userDatabase.containsKey(name);
    }

    // One-line method to check if both hashes (using SHA256 encryption) match
    @Override
    public boolean verifyPassword(String username, String password) throws RemoteException{

        return userDatabase.get(username).getPasswordHash().equals(calcHashForGivenPassword(password));
    }

    @Override
    public ArrayList<String> getOnlineClientsNames() throws RemoteException {

        return new ArrayList<>(connectedClients.keySet());
    }

    @Override
    public ArrayList<String> getStoredClientsNames() throws RemoteException {

        return new ArrayList<>(userDatabase.keySet());
    }

    @Override
    public String calcHashForGivenPassword(String password) throws RemoteException {

        StringBuilder sb = new StringBuilder();

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] bytes = md.digest(password.getBytes());
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Excepción en la conversión de hash de contraseñas: " + e.getMessage());
        }

        return sb.toString();
    }

    // For password encryption purposes
    private byte[] generateSalt() {

        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        return salt;
    }

    private class ClientHandlerThread extends Thread {

        private String name;
        private String passwordHash;
        private RMIClientInterface client;

        // Custom thread that will handle a client (1 thread per client connection)
        public ClientHandlerThread(String name, String passwordHash, RMIClientInterface client) throws RemoteException {

            this.name = name;
            this.passwordHash = passwordHash;
            this.client = client;
        }

        @Override
        public void run() {

            // Register the new client as online
            connectedClients.put(name, client);

            // Adds the newly created client to the database (if it is not there yet)
            if (!userDatabase.containsKey(name)) {
                ClientData clientData = new ClientData(name, passwordHash);
                userDatabase.put(name, clientData);
                System.out.println("Se ha registrado el cliente '" + name + "' en la base de datos");
            }

            // Notifies the online users about the newly connected one
            for (RMIClientInterface c : connectedClients.values()) {
                if (!c.equals(client)) {
                    try {
                        c.notifyConnection(name);
                    } catch (RemoteException e) {
                        System.out.println("Excepción de acceso remoto: " + e.getMessage());
                    }
                }
            }

            // Informs the client on which clients are currently online
            try {
                String message = "SISTEMA: Los usuarios conectados son:";
                for (int i = 0; i < getOnlineClientsNames().size(); ++i) {
                    message += " '" + getOnlineClientsNames().get(i) + "'";
                }
                client.receiveMessage("threadcreador", "mensaje de prueba");
                //client.getMainControllerData().getMainController().printToConsole(message);
            } catch (RemoteException e) {
                System.out.println("Excepción de acceso remoto: " + e.getMessage());
                try {
                    client.receiveMessage("threadcreador", "mensaje de error remoto");
                    //client.getMainControllerData().getMainController().printToConsole("ERROR: No se ha podido obtener la lista de usuarios conectados");
                } catch (RemoteException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
}