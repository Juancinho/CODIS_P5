/*
 * Actividad: Aplicaciones P2P. Clase datos del cliente
 * Fecha: Miércoles, 29 de noviembre de 2023
 * Autores: Iago Fernández Perlo y Juan Otero Rivas
 */

package com.iagofernandezjuanotero;

import java.io.Serializable;
import java.util.ArrayList;

// Acts mostly like a placeholder of all attributes for a given client (when offline)
// Requires Serializable interface to work with networking data transfers
public class ClientData implements Serializable {

    // Does not require username as it is stored as key in the hashmap used by the server remote interface
    private final String passwordHash;
    private final ArrayList<String> pendingSentFriendshipRequests;
    private final ArrayList<String> pendingReceivedFriendshipRequests;
    private final ArrayList<String> addedFriends;
    private final ArrayList<String> whileOfflineMessageStack;

    public ClientData(String passwordHash, ArrayList<String> pendingSentFriendshipRequests, ArrayList<String> pendingReceivedFriendshipRequests, ArrayList<String> addedFriends, ArrayList<String> whileOfflineMessageStack) {

        this.passwordHash = passwordHash;
        this.pendingSentFriendshipRequests = pendingSentFriendshipRequests;
        this.pendingReceivedFriendshipRequests = pendingReceivedFriendshipRequests;
        this.addedFriends = addedFriends;
        this.whileOfflineMessageStack = whileOfflineMessageStack;
    }

    public ClientData(String passwordHash) {

        this.passwordHash = passwordHash;
        pendingSentFriendshipRequests = new ArrayList<>();
        pendingReceivedFriendshipRequests = new ArrayList<>();
        addedFriends = new ArrayList<>();
        whileOfflineMessageStack = new ArrayList<>();
    }

    public String getPasswordHash() {

        return passwordHash;
    }

    public ArrayList<String> getPendingSentFriendshipRequests() {

        return pendingSentFriendshipRequests;
    }

    public ArrayList<String> getPendingReceivedFriendshipRequests() {

        return pendingReceivedFriendshipRequests;
    }

    public ArrayList<String> getAddedFriends() {

        return addedFriends;
    }

    public ArrayList<String> getWhileOfflineMessageStack() {

        return whileOfflineMessageStack;
    }

    // Methods that act like setters and getters but adding or removing elements from Arraylists one by one

    public void addFriend(String name) {

        addedFriends.add(name);
    }

    public void addReceivedPendingRequest(String friendRequester) {

        pendingReceivedFriendshipRequests.add(friendRequester);
    }

    public void removeReceivedPendingRequest(String friendRequester) {

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

    // Related to file management methods


}
