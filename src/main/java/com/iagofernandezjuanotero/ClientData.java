package com.iagofernandezjuanotero;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;

// Serializable so it works properly with JavaRMI
// Acts mostly like a placeholder of all attributes for a given client (when offline)
// Requires Serializable interface to work with networking data transfers
public class ClientData implements Serializable {

    private String username;
    private String passwordHash;
    private ArrayList<String> pendingSentFriendshipRequests;
    private ArrayList<String> pendingReceivedFriendshipRequests;
    private ArrayList<String> addedFriends;
    private ArrayList<String> whileOfflineMessageStack;

    public ClientData(String username, String passwordHash) {

        this.username = username;
        this.passwordHash = passwordHash;
        pendingSentFriendshipRequests = new ArrayList<>();
        pendingReceivedFriendshipRequests = new ArrayList<>();
        addedFriends = new ArrayList<>();
        whileOfflineMessageStack = new ArrayList<>();
    }

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {

        this.username = username;
    }

    public String getPasswordHash() {

        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {

        this.passwordHash = passwordHash;
    }

    public ArrayList<String> getPendingSentFriendshipRequests() {

        return pendingSentFriendshipRequests;
    }

    public void setPendingSentFriendshipRequests(ArrayList<String> pendingSentFriendshipRequests) {

        this.pendingSentFriendshipRequests = pendingSentFriendshipRequests;
    }

    public ArrayList<String> getPendingReceivedFriendshipRequests() {

        return pendingReceivedFriendshipRequests;
    }

    public void setPendingReceivedFriendshipRequests(ArrayList<String> pendingReceivedFriendshipRequests) {

        this.pendingReceivedFriendshipRequests = pendingReceivedFriendshipRequests;
    }

    public ArrayList<String> getAddedFriends() {
        return addedFriends;
    }

    public void setAddedFriends(ArrayList<String> addedFriends) {
        this.addedFriends = addedFriends;
    }

    public ArrayList<String> getWhileOfflineMessageStack() {

        return whileOfflineMessageStack;
    }

    public void setWhileOfflineMessageStack(ArrayList<String> whileOfflineMessageStack) {

        this.whileOfflineMessageStack = whileOfflineMessageStack;
    }

    public void addFriend(String name) {

        addedFriends.add(name);
    }

    public void removeFriend(String name) {

        addedFriends.remove(name);
    }

    public void addPendingRequest(String friendRequester) {

        pendingReceivedFriendshipRequests.add(friendRequester);
    }

    public void removePendingRequest(String friendRequester) {

        pendingReceivedFriendshipRequests.remove(friendRequester);
    }

    public void addSentPendingRequest(String friendRequested) {

        pendingSentFriendshipRequests.add(friendRequested);
    }

    public void removeSentPendingRequest(String friendRequested) {

        pendingSentFriendshipRequests.remove(friendRequested);
    }

    public void addWhileOfflineMessage(String message) {

        whileOfflineMessageStack.add(message);
    }

    public void removeWhileOfflineMessage(String message) {

        whileOfflineMessageStack.remove(message);
    }
}
