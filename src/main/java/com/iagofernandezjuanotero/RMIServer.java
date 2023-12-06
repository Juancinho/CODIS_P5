package com.iagofernandezjuanotero;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIServer {

    public static void main(String[] args) {

        InputStreamReader is = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(is);
        String portNum, registryURL;

        try {

            System.out.println("Introduce el puerto para el registro RMI: ");
            portNum = (br.readLine()).trim();
            int RMIPortNum = Integer.parseInt(portNum);
            startRegistry(RMIPortNum);

            RMIServerImpl rmiServerImpl = new RMIServerImpl();
            registryURL = "rmi://localhost:" + portNum + "/messagingApp";
            Naming.rebind(registryURL, rmiServerImpl);

            //listRegistry(registryURL);
            System.out.println("El servidor está activo. Esperando clientes...");

            // Actively waits for clients

        } catch (Exception e) {
            //System.out.println("Excepción en el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Function that starts the registry if it is not already running
    private static void startRegistry (int RMIPortNum) throws RemoteException {

        try {
            Registry registry = LocateRegistry.getRegistry(RMIPortNum);
            registry.list();
        } catch (RemoteException e) {
            Registry registry = LocateRegistry.createRegistry(RMIPortNum);
        }
    }

    /*private static void listRegistry(String registryURL) throws RemoteException, MalformedURLException {

        System.out.println("El registro '" + registryURL + "' contiene: ");

        String[] names = Naming.list(registryURL);
        for (String name : names) {
            System.out.println(name);
        }
    }*/
}

