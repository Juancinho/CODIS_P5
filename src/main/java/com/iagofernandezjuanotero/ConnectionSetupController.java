/*
 * Actividad: Aplicaciones P2P. Clase controlador de la vista de configuración de la conexión
 * Fecha: Miércoles, 29 de noviembre de 2023
 * Autores: Iago Fernández Perlo y Juan Otero Rivas
 */

package com.iagofernandezjuanotero;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ResourceBundle;

public class ConnectionSetupController implements Initializable {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private Text errorMessageText;

    @FXML
    private TextField hostnameTextField;

    @FXML
    private Spinner<Integer> portSpinner;

    private RMIServerInterface rmiServerInterface;

    public RMIServerInterface getRmiServerInterface() {

        return rmiServerInterface;
    }

    // Method run when the user clicks the "connect" button
    @FXML
    public void onConnectButtonClick (ActionEvent event) {

        // Uses "localhost" and 1099 as default parameters (those are the ones shown in the UI)
        String hostname;
        if (hostnameTextField.getText().isEmpty()) {
            hostname = "localhost";
        } else {
            hostname = hostnameTextField.getText();
        }
        int port = portSpinner.getValue();

        // As in other JavaRMI programs already made, creates the registryURL and calls Lookup to get the linked remote object
        String registryURL = "rmi://" + hostname + ":" + port + "/messagingApp";
        try {
            // Gets the reference
            rmiServerInterface = (RMIServerInterface) Naming.lookup(registryURL);

            // The connection was set up correctly, simply exits
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.close();
        } catch (NotBoundException e) {     // Exception management, printing errors both to UI and prompt
            System.out.println("Excepción de objeto remoto no encontrado: " + e.getMessage());
            printErrorMessage("No se ha encontrado el objeto remoto");
        } catch (MalformedURLException e) {
            System.out.println("Excepción de URL con mal formato: " + e.getMessage());
            printErrorMessage("El formato de la URL es inválido");
        } catch (RemoteException e) {
            System.out.println("Excepción de invocación remota: " + e.getMessage());
            printErrorMessage("Invocación remota fallida");
        }
    }

    // Method that changes the text label (in red colour) showing an error in the UI
    @FXML
    private void printErrorMessage(String message) {

        errorMessageText.setText(message);

        if(!errorMessageText.isVisible()) {
            errorMessageText.setVisible(true);
        }
    }

    // Auxiliary function that removes the focus from the buttons (simply aesthetic purposed)
    @FXML
    public void loseFocus() {

        // Cannot be set on initialize as drivers are not ready yet
        rootPane.requestFocus();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        errorMessageText.setVisible(false);

        // Dodges IANA reserved ports (0-1023), and private (dynamic) ports (49152 and above)
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1024, 49151, 1099);
        portSpinner.setValueFactory(valueFactory);
    }
}
