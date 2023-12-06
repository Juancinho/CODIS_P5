package com.iagofernandezjuanotero;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
            RMIClientImpl rmiClientImpl = new RMIClientImpl(name, calcHashForGivenPassword(password), this);
            savedClients.put(name, rmiClientImpl);
        }

        // Notifies the online users about the newly connected one
        for (RMIClientInterface c : connectedClients.values()) {
            if (!c.equals(client)) {
                c.notifyConnection(name);
            }
        }

        // Finally, prints data to server prompt
        System.out.println("Se ha registrado el cliente '" + name + "'");
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
    public boolean isUsernameTaken (String name)  throws RemoteException{

        return savedClients.containsKey(name);
    }

    // Not compulsory but highly recommended method to preserve encapsulation and security in encrypted passwords
    @Override
    public RMIClientImpl createNewClient(String username, String password) throws RemoteException {

        return new RMIClientImpl(username, calcHashForGivenPassword(password), this);
    }

    // One-line method to check if both hashes (using SHA256 encryption) match
    @Override
    public boolean verifyPassword(String username, String password)  throws RemoteException{

        return savedClients.get(username).getPasswordHash().equals(calcHashForGivenPassword(password));
    }

    private String calcHashForGivenPassword(String password) {

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
}
