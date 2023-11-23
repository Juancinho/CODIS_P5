import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class ClienteImpl extends UnicastRemoteObject implements ClienteInterface {
    private String nombre;
    private ServidorInterface servidor;

    public ClienteImpl(String nombre, ServidorInterface servidor) throws RemoteException {
        super();
        this.nombre = nombre;
        this.servidor = servidor;
        servidor.registrarCliente(nombre, this);
    }

    @Override
    public void recibirMensaje(String remitente, String mensaje) throws RemoteException {
        System.out.println("Mensaje de " + remitente + ": " + mensaje);
    }

    @Override
    public void notificarConexion(String nuevoCliente) throws RemoteException {
        System.out.println("Se ha conectado " + nuevoCliente);
    }

    @Override
    public void notificarDesconexion(String clienteDesconectado) throws RemoteException {
        System.out.println(clienteDesconectado + " se ha desconectado");
    }

    public void enviarMensaje(String destinatario, String mensaje) throws RemoteException {
        servidor.enviarMensaje(nombre, destinatario, mensaje);
    }

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);

            System.out.print("Ingresa tu nombre: ");
            String nombre = scanner.nextLine();

            ServidorInterface servidor = (ServidorInterface) Naming.lookup("//localhost/Servidor");
            ClienteImpl cliente = new ClienteImpl(nombre, servidor);

            System.out.println("Cliente " + nombre + " listo. Puedes comenzar a chatear.");

            while (true) {
                System.out.print("Destinatario: ");
                String destinatario = scanner.nextLine();
                System.out.print("Mensaje: ");
                String mensaje = scanner.nextLine();

                cliente.enviarMensaje(destinatario, mensaje);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
