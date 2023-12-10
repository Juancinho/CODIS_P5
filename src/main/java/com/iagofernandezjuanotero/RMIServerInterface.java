/*
 * Actividad: Aplicaciones P2P. Clase interfaz del servidor RMI
 * Fecha: Miércoles, 29 de noviembre de 2023
 * Autores: Iago Fernández Perlo y Juan Otero Rivas
 */

package com.iagofernandezjuanotero;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface RMIServerInterface extends Remote {

    ClientData getClientData(String username) throws RemoteException;
    void registerClient(String name, String passwordHash, RMIClientInterface client) throws RemoteException;
    void unregisterClient(String name) throws RemoteException;
    RMIClientInterface getClientToMessage(String receiver) throws RemoteException;
    void createClientRequest(String requestedClient, String requesterClient) throws RemoteException;
    void rejectClientRequest(String requestedClient, String requesterClient) throws RemoteException;
    void acceptClientRequest(String requestedClient, String requesterClient) throws RemoteException;
    boolean isUsernameTaken(String name) throws RemoteException;
    boolean isUserOnline(String name) throws RemoteException;
    boolean verifyPassword(String username, String password) throws RemoteException;
    ArrayList<String> getOnlineClientsNames() throws RemoteException;
    boolean isFriend(String client1, String client2) throws RemoteException;
    String calcHashForGivenPassword(String password) throws RemoteException;
}
