package com.iagofernandezjuanotero;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class RMIClientImpl extends UnicastRemoteObject implements RMIClientInterface {

    private final String name;
    private final RMIServerInterface server;

    public RMIClientImpl(String name, RMIServerInterface server) throws RemoteException {

        super();
        this.name = name;
        this.server = server;
        server.registerClient(name, this);
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

        server.sendMessage(name, receiver, message);
    }

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
}
