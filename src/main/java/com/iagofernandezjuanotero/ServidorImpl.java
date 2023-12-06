package com.iagofernandezjuanotero;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class ServidorImpl extends UnicastRemoteObject implements ServidorInterface {
    private Map<String, ClienteInterface> clientesConectados;

    public ServidorImpl() throws RemoteException {
        super();
        this.clientesConectados = new HashMap<>();
    }

    @Override
    public void registrarCliente(String nombre, ClienteInterface cliente) throws RemoteException {
        clientesConectados.put(nombre, cliente);

        // Notificar a los clientes existentes sobre la nueva conexión
        for (ClienteInterface c : clientesConectados.values()) {
            if (!c.equals(cliente)) {
                c.notificarConexion(nombre);
            }
        }
    }

    @Override
    public void enviarMensaje(String remitente, String destinatario, String mensaje) throws RemoteException {
        ClienteInterface clienteDestino = clientesConectados.get(destinatario);
        if (clienteDestino != null) {
            clienteDestino.recibirMensaje(remitente, mensaje);
        }
    }

    @Override
    public void desconectarCliente(String nombre) throws RemoteException {
        clientesConectados.remove(nombre);

        // Notificar a los clientes existentes sobre la desconexión
        for (ClienteInterface c : clientesConectados.values()) {
            c.notificarDesconexion(nombre);
        }
    }
}
