package com.iagofernandezjuanotero;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RMIClientImpl extends UnicastRemoteObject implements RMIClientInterface, Serializable {

    private final String name;
    private final ArrayList<String> pendingFriendshipRequests;
    private final RMIServerInterface server;
    private final String passwordHash;
    private final byte[] salt;

    public RMIClientImpl(String name, String password, RMIServerInterface server) throws RemoteException {

        super();
        this.name = name;
        salt = generateSalt();
        passwordHash = calcHashForGivenPassword(password, salt);
        pendingFriendshipRequests = new ArrayList<>();
        this.server = server;

        server.registerClient(name, this);
    }

    public ArrayList<String> getPendingFriendshipRequests () {

        return pendingFriendshipRequests;
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

    public void sendMessage(String receiver, String message) throws RemoteException {

        server.getClientToMessage(name, receiver, message);
    }

    public boolean verifyPassword(String password) {

        String hashForGivenPassword = calcHashForGivenPassword(password, salt);

        return hashForGivenPassword.equals(passwordHash);
    }

    private byte[] generateSalt() {

        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        return salt;
    }

    private String calcHashForGivenPassword(String contrasena, byte[] salt) {

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] bytes = md.digest(contrasena.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al calcular el hash de la contraseña", e);
        }
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
