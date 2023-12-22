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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        userDatabase.get(requestedClient).addReceivedPendingRequest(requesterClient);
        userDatabase.get(requesterClient).addSentPendingRequest(requestedClient);
        createClientRequestOnDatabase(requestedClient, requesterClient);

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

    private void createClientRequestOnDatabase(String requestedClient, String requesterClient) {

        try (Connection connection = DBConnection.getConnection()) {

            // Works on the client who receives the request

            // SQL query to add the pending request to the user
            String requestedQuery = "UPDATE client SET pending_received_friendship_requests = pending_received_friendship_requests || ? WHERE username = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(requestedQuery)) {

                preparedStatement.setString(1, "," + requesterClient);  // Adds comma if there are no existing elements
                preparedStatement.setString(2, requestedClient);

                // Executes the query
                preparedStatement.executeUpdate();
            }

            // Works on the client who sends the request
            // Completely analogue to the code above

            String requesterQuery = "UPDATE client SET pending_sent_friendship_requests = pending_sent_friendship_requests || ? WHERE username = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(requesterQuery)) {

                preparedStatement.setString(1, "," + requestedClient);  // Adds comma if there are no existing elements
                preparedStatement.setString(2, requesterClient);

                preparedStatement.executeUpdate();
            }

        } catch (SQLException e) {
            System.out.println("Excepción de sql: " + e.getMessage());
        }
    }

    // Same as above. The only major change is that in this case the requester (that one client who sent the request)
    // may be offline at the moment the requested (the request receiver) rejects it (accepts it, for the next method)
    // so the message must be stored in the database object for that client, as the client is not available in onlineClients
    @Override
    public void rejectClientRequest(String requestedClient, String requesterClient) throws RemoteException {

        userDatabase.get(requestedClient).removeReceivedPendingRequest(requesterClient);
        userDatabase.get(requesterClient).removeSentPendingRequest(requestedClient);
        removeClientRequestOnDatabase(requestedClient, requesterClient);

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

        userDatabase.get(requestedClient).removeReceivedPendingRequest(requesterClient);
        userDatabase.get(requesterClient).removeSentPendingRequest(requestedClient);
        removeClientRequestOnDatabase(requestedClient, requesterClient);

        // Friendship is biyective, so both vectors must be linked (a -> b, b -> a)
        // (We could even make this method synchronized to completely make sure this, but seems excessive)
        userDatabase.get(requestedClient).addFriend(requesterClient);
        userDatabase.get(requesterClient).addFriend(requestedClient);
        addFriendOnDatabase(requestedClient, requesterClient);

        if (isUserOnline(requesterClient)) {

            connectedClients.get(requesterClient).notifyAcceptedSentFriendRequest(requestedClient);
            connectedClients.get(requestedClient).notifyAcceptedReceivedFriendRequest(requesterClient);

        } else {

            connectedClients.get(requestedClient).notifyAcceptedReceivedFriendRequest(requesterClient);
            getClientData(requesterClient).addWhileOfflineMessage("AMISTAD: '" + requestedClient + "' ha aceptado tu solicitud de amistad. Ahora sois amigos");
        }
    }

    private void removeClientRequestOnDatabase(String requestedClient, String requesterClient) {

        try (Connection connection = DBConnection.getConnection()) {

            // Works on the client who receives the request

            // SQL query to remove the specified request from the user's pending requests
            String requestedQuery = "UPDATE client SET pending_received_friendship_requests = array_remove(pending_received_friendship_requests, ?) WHERE username = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(requestedQuery)) {

                // Adjusts parameters in the correct order
                preparedStatement.setString(1, requesterClient);
                preparedStatement.setString(2, requestedClient);

                // Executes the query
                preparedStatement.executeUpdate();
            }

            // Works on the client who sends the request
            // Completely analogue to the code above

            String requesterQuery = "UPDATE client SET pending_sent_friendship_requests = array_remove(pending_sent_friendship_requests, ?) WHERE username = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(requesterQuery)) {

                preparedStatement.setString(1, requestedClient);
                preparedStatement.setString(2, requesterClient);

                preparedStatement.executeUpdate();
            }

        } catch (SQLException e) {
            System.out.println("Excepción de sql: " + e.getMessage());
        }
    }

    private void addFriendOnDatabase(String requestedClient, String requesterClient) {

        try (Connection connection = DBConnection.getConnection()) {

            // Works on the client who receives the request

            String requestedQuery = "UPDATE client SET added_friends = added_friends || ? WHERE username = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(requestedQuery)) {

                preparedStatement.setString(1, "," + requesterClient);
                preparedStatement.setString(2, requestedClient);

                preparedStatement.executeUpdate();
            }

            // Works on the client who sends the request

            String requesterQuery = "UPDATE client SET added_friends = added_friends || ? WHERE username = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(requesterQuery)) {

                preparedStatement.setString(1, "," + requestedClient);
                preparedStatement.setString(2, requesterClient);

                preparedStatement.executeUpdate();
            }

        } catch (SQLException e) {
            System.out.println("Excepción de sql: " + e.getMessage());
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

        byte[] salt = null;

        try (Connection connection = DBConnection.getConnection()) {

            // SQL query to retrieve the value from the "salt" column of the "salt" table (unique for encryption purposes)
            String query = "SELECT salt FROM salt";

            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                // Check if there is any result
                if (resultSet.next()) {

                    // Return the value obtained
                    salt = resultSet.getBytes("salt");
                }
            } catch (SQLException e) {
                System.out.println("Excepción de sql: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Excepción de sql: " + e.getMessage());
        }

        // If the salt was not restored from the database, it generates a new one (this will make the previously stored passwords unreachable)
        if (salt == null) {

            System.out.println("No se ha conseguido el valor de 'salt', se genera uno nuevo");

            // No results found. Generates a new salt
            SecureRandom random = new SecureRandom();
            salt = new byte[16];
            random.nextBytes(salt);

            // Finally, if there was no salt stored in the database, it stores the newly created one
            try (Connection connection = DBConnection.getConnection()) {

                // Erases the previous possible values in the table
                String deleteQuery = "DELETE FROM salt";
                try (PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery)) {
                    deleteStatement.executeUpdate();
                }

                // Inserts the newly calculated salt to the table
                String query = "INSERT INTO salt (salt) VALUES (?);\n";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                    preparedStatement.setBytes(1, salt);
                    preparedStatement.executeUpdate();

                } catch (SQLException e) {
                    System.out.println("Excepción de sql: " + e.getMessage());
                }

            } catch (SQLException e) {
                System.out.println("Excepción de sql: " + e.getMessage());
            }
        }

        return salt;
    }

    public void readDatabase() {

        System.out.println("Leyendo la base de datos...");

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = DBConnection.getConnection();

            String databaseQuery = "SELECT * FROM client";
            preparedStatement = connection.prepareStatement(databaseQuery);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                String username = resultSet.getString("username");
                String passwordHash = resultSet.getString("password_hash");
                ArrayList<String> pendingSentFriendshipRequests = getListFromDB(resultSet, "pending_sent_friendship_requests");
                ArrayList<String> pendingReceivedFriendshipRequests = getListFromDB(resultSet, "pending_received_friendship_requests");
                ArrayList<String> addedFriends = getListFromDB(resultSet, "added_friends");
                ArrayList<String> whileOfflineMessageStack = getListFromDB(resultSet, "while_offline_message_stack");

                // Creates the ClientData object and adds it to the dynamic database managed by this class
                ClientData clientData = new ClientData(passwordHash, pendingSentFriendshipRequests,
                        pendingReceivedFriendshipRequests, addedFriends, whileOfflineMessageStack);
                userDatabase.put(username, clientData);
            }
        } catch (SQLException e) {
            System.out.println("Excepción de sql: " + e.getMessage());
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    System.out.println("Excepción de sql: " + e.getMessage());
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    System.out.println("Excepción de sql: " + e.getMessage());
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    System.out.println("Excepción de sql: " + e.getMessage());
                }
            }
        }

        if (userDatabase.isEmpty()) {
            System.out.println("No se ha leído ningún dato de la base de datos");
        }
    }

    private ArrayList<String> getListFromDB(ResultSet resultSet, String columnName) throws SQLException {

        ArrayList<String> list = new ArrayList<>();

        // Retrieve the comma-separated string from the database
        String dbList = resultSet.getString(columnName);

        if (dbList != null) {

            // Split the database string into a list of strings
            String[] elements = dbList.split(",");

            // Trim each element and add it to the list
            for (String element : elements) {
                list.add(element.trim());
            }
        }

        return list;
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
                addClientToDatabase();
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

        private void addClientToDatabase() {

            try (Connection connection = DBConnection.getConnection()) {

                // SQL query to insert a new client into the table (client, the only one worked on within this program)
                String query = "INSERT INTO client (username, password_hash) VALUES (?, ?)";

                try (PreparedStatement statement = connection.prepareStatement(query)) {

                    statement.setString(1, name);
                    statement.setString(2, passwordHash);

                    statement.executeUpdate();
                }

            } catch (SQLException e) {
                System.out.println("Excepción de sql: " + e.getMessage());
            }
        }
    }
}