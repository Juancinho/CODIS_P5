package com.iagofernandezjuanotero;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.Naming;

public class RMIClient {

    LoginController loginController;
    MainController mainController;
    RMIClientImpl rmiClientImpl;

    String hostName;

    public void setControllers(LoginController loginController, MainController mainController) {

        this.loginController = loginController;
        this.mainController = mainController;
    }

    public RMIClient() {

        // Nothing to do here, client is initialized before any data can be fetched
    }

    public void run() {



        /*try {

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

            RMIClientImpl rmiClientImpl = new RMIClientImpl(hostName, "password", rmiServerInterface);

            System.out.println("El cliente " + hostName + " está listo. Puedes empezar a chatear.");

            while (true) {
                System.out.print("Destinatario: ");
                String receiver = br.readLine();
                System.out.print("Mensaje: ");
                String message = br.readLine();

                rmiClientImpl.sendMessage(receiver, message);
            }
        } catch (Exception e) {
            System.out.println("Excepción de envío de mensajes: " + e.getMessage());
        }*/
    }
}
