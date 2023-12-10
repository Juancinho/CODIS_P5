package com.iagofernandezjuanotero;

public class RMIClient {

    LoginController loginController;
    MainController mainController;
    RMIServerInterface rmiServerInterface;
    RMIClientInterface rmiClientInterface;

    public void setReferences(LoginController loginController, MainController mainController, RMIServerInterface rmiServerInterface, RMIClientInterface rmiClientInterface) {

        this.loginController = loginController;
        this.mainController = mainController;
        this.rmiServerInterface = rmiServerInterface;
        this.rmiClientInterface = rmiClientInterface;
    }

    public RMIClient() {

        // Nothing to do here, client is initialized before any data can be fetched
    }

    public void run() {
    }
}
