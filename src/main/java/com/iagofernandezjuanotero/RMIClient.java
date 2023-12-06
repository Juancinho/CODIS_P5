package com.iagofernandezjuanotero;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.Naming;

public class RMIClient {

    public static void main(String args[]) {

        try {

            InputStreamReader is = new InputStreamReader(System.in);
            BufferedReader br = new BufferedReader(is);
            String hostName;
            int RMIPort;

            // TODO
            // This must be UI integrated (also requesting for password?)
            System.out.println("Introduce el nombre de host para el registro RMI: ");
            hostName = br.readLine();
            System.out.println("Introduce el puerto para el registro RMI: ");
            String portNum = br.readLine();
            RMIPort = Integer.parseInt(portNum);

            String registryURL = "rmi://" + hostName + ":" + portNum + "/messagingApp";
            RMIServerInterface rmiServerInterface = (RMIServerInterface) Naming.lookup(registryURL);

            System.out.print("Introduce tu nombre: ");
            String name = br.readLine();

            RMIClientImpl rmiClientImpl = new RMIClientImpl(name, rmiServerInterface);

            System.out.println("El cliente " + name + " está listo. Puedes empezar a chatear.");

            while (true) {
                System.out.print("Destinatario: ");
                String receiver = br.readLine();
                System.out.print("Mensaje: ");
                String message = br.readLine();

                rmiClientImpl.sendMessage(receiver, message);
            }
        } catch (Exception e) {
            System.out.println("Excepción de envío de mensajes: " + e.getMessage());
        }
    }
}
