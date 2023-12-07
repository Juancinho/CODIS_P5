package com.iagofernandezjuanotero;

import java.io.Serializable;
import java.util.ArrayList;

public class MainControllerData implements Serializable {

    private String message;
    private ArrayList<String> storedClients;
    private ArrayList<String> onlineClients;

    private MainController mainController;
    private RMIServerInterface rmiServerInterface;
    private RMIClientInterface rmiClientInterface;

    private static final int MAX_MESSAGES = 100;

    public MainControllerData(MainController mainController, RMIServerInterface rmiServerInterface, RMIClientInterface rmiClientInterface) {

        this.mainController = mainController;
        this.rmiServerInterface = rmiServerInterface;
        this.rmiClientInterface = rmiClientInterface;
    }

    public String getMessage() {

        return message;
    }

    public void setMessage(String message) {

        this.message = message;
    }

    public ArrayList<String> getStoredClients() {

        return storedClients;
    }

    public void setStoredClients(ArrayList<String> storedClients) {

        this.storedClients = storedClients;
    }

    public ArrayList<String> getOnlineClients() {

        return onlineClients;
    }

    public void setOnlineClients(ArrayList<String> onlineClients) {

        this.onlineClients = onlineClients;
    }

    public MainController getMainController() {

        return mainController;
    }

    public void setMainController(MainController mainController) {

        this.mainController = mainController;
    }

    public RMIServerInterface getRmiServerInterface() {

        return rmiServerInterface;
    }

    public void setRmiServerInterface(RMIServerInterface rmiServerInterface) {

        this.rmiServerInterface = rmiServerInterface;
    }

    public RMIClientInterface getRmiClientInterface() {

        return rmiClientInterface;
    }

    public void setRmiClientInterface(RMIClientInterface rmiClientInterface) {

        this.rmiClientInterface = rmiClientInterface;
    }
}
