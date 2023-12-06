package com.iagofernandezjuanotero;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServidorInterface extends Remote {
    void registrarCliente(String nombre, ClienteInterface cliente) throws RemoteException;
    void enviarMensaje(String remitente, String destinatario, String mensaje) throws RemoteException;
    void desconectarCliente(String nombre) throws RemoteException;
}
