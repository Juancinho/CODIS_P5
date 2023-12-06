package com.iagofernandezjuanotero;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClienteInterface extends Remote {
    void recibirMensaje(String remitente, String mensaje) throws RemoteException;
    void notificarConexion(String nuevoCliente) throws RemoteException;
    void notificarDesconexion(String clienteDesconectado) throws RemoteException;
}
