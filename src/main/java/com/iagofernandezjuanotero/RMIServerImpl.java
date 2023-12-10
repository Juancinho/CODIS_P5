/*
 * Actividad: Aplicaciones P2P. Clase implementación del servidor RMI
 * Fecha: Miércoles, 29 de noviembre de 2023
 * Autores: Iago Fernández Perlo y Juan Otero Rivas
 */

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

    // Uses two HashMaps, one to store the clients even if they are offline (with help of the placeholder ClientData)
    // and another one that dynamically stores the connected clients (stores the references to the remote objects, so it
    // can call remote method on the clients). In both cases, names (strings) are used as keys for the map entries
    private final Map<String, RMIClientInterface> connectedClients;
    private final Map<String, ClientData> userDatabase;
    private final byte[] salt;      // Random byte sequence used for password encryption

    public RMIServerImpl() throws RemoteException {

        super();

        connectedClients = new HashMap<>();
        userDatabase = new HashMap<>();
        salt = generateSalt();
    }

    // Returns the ClientData associated to a username
    @Override
    public ClientData getClientData(String username) throws RemoteException {

        return userDatabase.get(username);
    }

    // Note that this 'register' stands for simply accessing the app (not only creating new accounts)
    // Anyway, this method stores the client if it does not exist yet (so it does actually register new users to the server)
    @Override
    public void registerClient(String name, String passwordHash, RMIClientInterface client) throws RemoteException {

        // As this method is called every time a client connects (registers) to the server, and it takes
        // a while, a new thread is created in the server side to handle the registration of the new client
        ClientHandlerThread clientHandlerThread = new ClientHandlerThread(name, passwordHash, client);
        clientHandlerThread.start();
    }

    // Method that removes a client from the server
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

    // Method that returns a RMIClientInterface (this is, the remote reference to the client)
    // Its compulsory to allow the communication between clients without the server intervention
    @Override
    public RMIClientInterface getClientToMessage(String receiver) throws RemoteException {

        return connectedClients.get(receiver);
    }

    // Method that creates a friend request
    // In this function, requestedClient will stand for that client that receives the request, while requester is the one
    // that sends the request. Seems easy at first, but may lead to misunderstandings
    @Override
    public void createClientRequest(String requestedClient, String requesterClient) throws RemoteException {

        // Checks that the user does not collide with another already added
        if (userDatabase.get(requesterClient).getAddedFriends().contains(requestedClient)) {
            getClientToMessage(requesterClient).printError("Ese usuario ya es tu amigo");
            return;
        }

        boolean activePendingRequest = userDatabase.get(requesterClient).getPendingSentFriendshipRequests().contains(requestedClient);

        // The clients store incoming requests and outgoing requests, that must be always worked on in pairs
        // (this is, if a client has one new incoming request, there must be another client with a new outgoing request)
        userDatabase.get(requestedClient).addPendingRequest(requesterClient);
        userDatabase.get(requesterClient).addSentPendingRequest(requestedClient);

        // Checks whether the user is online or not, and if there is an active request already
        if (isUserOnline(requestedClient)) {

            if (activePendingRequest) {
                getClientToMessage(requesterClient).printError("Ya hay una solicitud de amistad activa a ese usuario");
            } else {
                // It must notify both clients (one that sent the request, and another one that received it)
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

    // Same as above. The only major change is that in this case the requester (that one client who sent the request)
    // may be offline at the moment the requested (the request receiver) rejects it (accepts it, for the next method)
    // so the message must be stored in the database object for that client, as the client is not available in onlineClients
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
        // (We could even make this method synchronized to completely make sure this, but seems excessive)
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

    // Method that checks if a username is already taken by a user (in the database, no matter if the client is offline)
    @Override
    public boolean isUsernameTaken(String name) throws RemoteException{

        return userDatabase.containsKey(name);
    }

    // Method that checks if a username given refers to a user which is online
    @Override
    public boolean isUserOnline(String name) throws RemoteException {

        return connectedClients.containsKey(name);
    }

    // One-line method to check if both hashes (using SHA256 encryption) match
    @Override
    public boolean verifyPassword(String username, String password) throws RemoteException{

        return userDatabase.get(username).getPasswordHash().equals(calcHashForGivenPassword(password));
    }

    // Methods that return only the keySets for the onlineClients and the database, respectively
    @Override
    public ArrayList<String> getOnlineClientsNames() throws RemoteException {

        return new ArrayList<>(connectedClients.keySet());
    }

    // Method that checks if two clients, given their usernames, are friends
    @Override
    public boolean isFriend(String client1, String client2) throws RemoteException {

        return userDatabase.get(client1).getAddedFriends().contains(client2);
    }

    // Method that receives a password (in plain text), and encrypts it using SHA-256
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

    // For password encryption purposes, creates the salt seed on server initialization
    private byte[] generateSalt() {

        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        return salt;
    }

    // Custom thread used for the registration of a new client
    private class ClientHandlerThread extends Thread {

        private final String name;
        private final String passwordHash;
        private final RMIClientInterface client;

        // It will handle the registration client (1 thread per client connection)
        public ClientHandlerThread(String name, String passwordHash, RMIClientInterface client) {

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
                ClientData clientData = new ClientData(passwordHash);
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
                StringBuilder message = new StringBuilder("Tus amigos conectados son:");
                for (int i = 0; i < getOnlineClientsNames().size(); ++i) {
                    if (!name.equals(getOnlineClientsNames().get(i)) && isFriend(name, getOnlineClientsNames().get(i))) {
                        message.append(" '").append(getOnlineClientsNames().get(i)).append("'");
                        someoneOnline = true;
                    }
                }

                if (someoneOnline) {
                    client.printInfo(message.toString());
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

            // The thread prints some data to the server (so it can follow the flow of the connection and disconnection of users)
            // but cannot access to the messages, as the program is intended to be peer to peer
            System.out.println("-> '" + name + "' se ha conectado");
        }
    }
}