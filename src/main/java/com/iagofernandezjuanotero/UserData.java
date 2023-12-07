package com.iagofernandezjuanotero;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

// Serializable so it works properly with JavaRMI
// Acts mostly like a placeholder of all attributes for a given client (when offline)
public class UserData implements Serializable {

    private String username;
    private ArrayList<String> pendingFriendshipRequests;
    private String passwordHash;

    public UserData(String username, String passwordHash) {

        this.username = username;
        this.passwordHash = passwordHash;
        pendingFriendshipRequests = new ArrayList<>();
    }

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {

        this.username = username;
    }

    public ArrayList<String> getPendingFriendshipRequests() {

        return pendingFriendshipRequests;
    }

    public void setPendingFriendshipRequests(ArrayList<String> pendingFriendshipRequests) {

        this.pendingFriendshipRequests = pendingFriendshipRequests;
    }

    public String getPasswordHash() {

        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {

        this.passwordHash = passwordHash;
    }
}
