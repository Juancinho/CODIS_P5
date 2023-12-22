/*
 * Actividad: Aplicaciones P2P. Clase principal del servidor RMI
 * Fecha: Miércoles, 29 de noviembre de 2023
 * Autores: Iago Fernández Perlo y Juan Otero Rivas
 */

package com.iagofernandezjuanotero;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

// Simple class that works on console, and sets up the remote object and waits for calls
public class RMIServer {

    public static void main(String[] args) {

        // This code is based on given example by Mr.Lui
        InputStreamReader is = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(is);
        String portNum, registryURL;

        try {

            System.out.println("\n  *** SERVIDOR RMI ***  \n\n");

            // Tries to connect to the database to get stored data
            RMIServerImpl rmiServerImpl = new RMIServerImpl();
            rmiServerImpl.readDatabase();

            // Asks for the port (the server works on localhost)
            System.out.println("\nIntroduce el puerto para el registro RMI: ");
            portNum = (br.readLine()).trim();
            int RMIPortNum = Integer.parseInt(portNum);
            startRegistry(RMIPortNum);

            // Sets up the remote object
            registryURL = "rmi://localhost:" + portNum + "/messagingApp";
            Naming.rebind(registryURL, rmiServerImpl);

            System.out.println("\nEl servidor está activo. Esperando clientes...\n");

            // Actively waits for clients

        } catch (Exception e) {
            System.out.println("Excepción en el servidor: " + e.getMessage());
        }
    }

    // Function that starts the registry if it is not already running
    private static void startRegistry (int RMIPortNum) throws RemoteException {

        try {
            Registry registry = LocateRegistry.getRegistry(RMIPortNum);
            registry.list();
        } catch (RemoteException e) {
            LocateRegistry.createRegistry(RMIPortNum);
        }
    }
}

