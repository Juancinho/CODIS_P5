package com.iagofernandezjuanotero;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.Naming;

public class Cliente {

    public static void main(String args[]) {
        try {
            String hostName;
            int RMIPort;
            InputStreamReader is = new InputStreamReader(System.in);
            BufferedReader br = new BufferedReader(is);

            System.out.println("Enter the RMIRegistry host name:");
            hostName = br.readLine();
            System.out.println("Enter the RMIRegistry port number:");
            String portNum = br.readLine();
            RMIPort = Integer.parseInt(portNum);

            String registryURL = "rmi://" + hostName + ":" + portNum + "/servidor";
            ServidorInterface servidor = (ServidorInterface) Naming.lookup(registryURL);

            System.out.print("Enter your name: ");
            String nombre = br.readLine();

            ClienteImpl cliente = new ClienteImpl(nombre, servidor);

            System.out.println("Cliente " + nombre + " ready. You can start chatting.");

            while (true) {
                System.out.print("Recipient: ");
                String destinatario = br.readLine();
                System.out.print("Message: ");
                String mensaje = br.readLine();

                cliente.enviarMensaje(destinatario, mensaje);
            }
        } catch (Exception e) {
            System.out.println("Exception in MensajeriaCliente: " + e);
        }
    }
}
