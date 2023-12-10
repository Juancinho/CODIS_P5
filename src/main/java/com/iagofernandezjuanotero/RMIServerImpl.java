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

    @Override
    public ClientData getClientData(String username) throws RemoteException {

        return userDatabase.get(username);
    }

    // Note that this 'register' stands for simply accessing the app (not only creating new accounts)
    // Anyway, this method stores the client if it does not exist yet (so it does actually register new users to the server)
    @Override
    public void registerClient(String name, String passwordHash, RMIClientInterface client) throws RemoteException {

        ClientHandlerThread clientHandlerThread = new ClientHandlerThread(name, passwordHash, client);
        clientHandlerThread.start();
    }

    @Override
    public void unregisterClient(String name) throws RemoteException {

        connectedClients.remove(name);

        // Notifies the client friends about the disconnection
        for (String client: connectedClients.keySet()) {
            if (isFriend(client, name)) {
                getClientToMessage(client).notifyDisconnection(name);
            }
        }

        System.out.println("-> '" + name + "' se ha desconectado");
    }

    @Override
    public RMIClientInterface getClientToMessage(String receiver) throws RemoteException {

        return connectedClients.get(receiver);
    }

    @Override
    public void createClientRequest(String requestedClient, String requesterClient) throws RemoteException {

        if (userDatabase.get(requesterClient).getAddedFriends().contains(requestedClient)) {
            getClientToMessage(requesterClient).printError("Ese usuario ya es tu amigo");
            return;
        }

        boolean activePendingRequest = userDatabase.get(requesterClient).getPendingSentFriendshipRequests().contains(requestedClient);

        // Notation is kind of confusing for this function (but logical after all)
        userDatabase.get(requestedClient).addPendingRequest(requesterClient);
        userDatabase.get(requesterClient).addSentPendingRequest(requestedClient);

        if (isUserOnline(requestedClient)) {

            if (activePendingRequest) {
                getClientToMessage(requesterClient).printError("Ya hay una solicitud de amistad activa a ese usuario");
            } else {
                connectedClients.get(requesterClient).notifySentFriendRequest(requestedClient);
                connectedClients.get(requestedClient).notifyReceivedFriendRequest(requesterClient);
            }

        } else {

            if (activePendingRequest) {
                getClientToMessage(requesterClient).printError("Ya hay una solicitud de amistad activa a ese usuario");
            } else {
                connectedClients.get(requesterClient).notifySentFriendRequest(requestedClient);
                getClientData(requestedClient).addWhileOfflineMessage("AMISTAD: '" + requesterClient + "' ha solicitado ser tu amigo");
            }
        }
    }

    @Override
    public void rejectClientRequest(String requestedClient, String requesterClient) throws RemoteException {

        userDatabase.get(requestedClient).removePendingRequest(requesterClient);
        userDatabase.get(requesterClient).removeSentPendingRequest(requestedClient);

        if (isUserOnline(requesterClient)) {

            connectedClients.get(requesterClient).notifyRejectedSentFriendRequest(requestedClient);
            connectedClients.get(requestedClient).notifyRejectedReceivedFriendRequest(requesterClient);

        } else {

            connectedClients.get(requestedClient).notifyRejectedReceivedFriendRequest(requesterClient);
            getClientData(requesterClient).addWhileOfflineMessage("AMISTAD: '" + requestedClient + "' ha rechazado tu solicitud de amistad");
        }
    }

    @Override
    public void acceptClientRequest(String requestedClient, String requesterClient) throws RemoteException {

        userDatabase.get(requestedClient).removePendingRequest(requesterClient);
        userDatabase.get(requesterClient).removeSentPendingRequest(requestedClient);

        // Friendship is biyective, so both vectors must be linked (a -> b, b -> a)
        userDatabase.get(requestedClient).addFriend(requesterClient);
        userDatabase.get(requesterClient).addFriend(requestedClient);

        if (isUserOnline(requesterClient)) {

            connectedClients.get(requesterClient).notifyAcceptedSentFriendRequest(requestedClient);
            connectedClients.get(requestedClient).notifyAcceptedReceivedFriendRequest(requesterClient);

        } else {

            connectedClients.get(requestedClient).notifyAcceptedReceivedFriendRequest(requesterClient);
            getClientData(requesterClient).addWhileOfflineMessage("AMISTAD: '" + requestedClient + "' ha aceptado tu solicitud de amistad. Ahora sois amigos");
        }
    }

    @Override
    public boolean isUsernameTaken(String name) throws RemoteException{

        return userDatabase.containsKey(name);
    }

    @Override
    public boolean isUserOnline(String name) throws RemoteException {

        return connectedClients.containsKey(name);
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
    public boolean isFriend(String client1, String client2) throws RemoteException {

        return userDatabase.get(client1).getAddedFriends().contains(client2);
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
            boolean isNewClient = false;
            if (!userDatabase.containsKey(name)) {
                ClientData clientData = new ClientData(name, passwordHash);
                userDatabase.put(name, clientData);
                System.out.println("[NUEVO] Se ha registrado el cliente '" + name + "' en la base de datos");
                isNewClient = true;
            }

            // Notifies the online friends about the newly connected one
            for (String username: connectedClients.keySet()) {
                try {
                    if (!username.equals(name)) {
                        if (isFriend(username, name)) {
                            getClientToMessage(username).notifyConnection(name, isNewClient);
                        }
                    }
                } catch (RemoteException e) {
                    System.out.println("Excepción de acceso remoto: " + e.getMessage());
                }
            }

            // Informs the client on which friends are currently online
            try {
                boolean someoneOnline = false;
                String message = "Tus amigos conectados son:";
                for (int i = 0; i < getOnlineClientsNames().size(); ++i) {
                    if (!name.equals(getOnlineClientsNames().get(i)) && isFriend(name, getOnlineClientsNames().get(i))) {
                        message += " '" + getOnlineClientsNames().get(i) + "'";
                        someoneOnline = true;
                    }
                }

                if (someoneOnline) {
                    client.printInfo(message);
                } else {
                    client.printInfo("No hay ningún amigo conectado en este momento");
                }

            } catch (RemoteException e) {

                System.out.println("Excepción de acceso remoto: " + e.getMessage());
                try {
                    client.printError("No se ha podido obtener la lista de usuarios conectados");
                } catch (RemoteException ex) {
                    System.out.println("Error de acceso remoto: " + ex.getMessage());
                }
            }

            System.out.println("-> '" + name + "' se ha conectado");
        }
    }
}