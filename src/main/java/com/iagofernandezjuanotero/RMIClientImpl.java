package com.iagofernandezjuanotero;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RMIClientImpl extends UnicastRemoteObject implements RMIClientInterface {

    private final String username;
    private final ArrayList<String> pendingFriendshipRequests;
    private final RMIServerInterface server;
    private final String passwordHash;

    public RMIClientImpl(String username, String passwordHash, RMIServerInterface server) throws RemoteException {

        super();

        this.username = username;
        this.passwordHash = passwordHash;
        pendingFriendshipRequests = new ArrayList<>();
        this.server = server;

        server.registerClient(username, passwordHash, this);
    }

    @Override
    public String getUsername() {

        return username;
    }

    @Override
    public ArrayList<String> getPendingFriendshipRequests() {

        return pendingFriendshipRequests;
    }

    public String getPasswordHash() {

        return passwordHash;
    }

    @Override
    public void receiveMessage(String sender, String message) throws RemoteException {

        System.out.println("Mensaje de " + sender + ": " + message);
    }

    @Override
    public void notifyConnection(String newClient) throws RemoteException {

        System.out.println("Se ha conectado " + newClient);
    }

    @Override
    public void notifyDisconnection(String disconnectedClient) throws RemoteException {

        System.out.println(disconnectedClient + " se ha desconectado");
    }

    @Override
    public void sendMessage(String receiver, String message) throws RemoteException {

        server.getClientToMessage(username, receiver, message);
    }

    /*
    public static void main(String[] args) {

        try {

            Scanner scanner = new Scanner(System.in);

            System.out.print("Ingresa tu nombre: ");
            String name = scanner.nextLine();

            RMIServerInterface server = (RMIServerInterface) Naming.lookup("//localhost/MessagingApp");
            RMIClientImpl client = new RMIClientImpl(name, server);

            System.out.println("RMIClient " + name + " listo. Puedes comenzar a chatear.");

            while (true) {

                System.out.print("Destinatario: ");
                String destinatario = scanner.nextLine();
                System.out.print("Mensaje: ");
                String mensaje = scanner.nextLine();

                client.sendMessage(destinatario, mensaje);
            }

        } catch (Exception e) {
            System.out.println("Excepción de envío de mensaje: " + e.getMessage());
        }
    }
    */
}
