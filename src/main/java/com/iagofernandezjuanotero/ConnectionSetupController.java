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

    public void setRmiServerInterface(RMIServerInterface rmiServerInterface) {

        this.rmiServerInterface = rmiServerInterface;
    }

    @FXML
    public void onConnectButtonClick (ActionEvent event) {

        String hostname = hostnameTextField.getText();
        int port = portSpinner.getValue();

        if (!isHostnameValid()) {
            printErrorMessage("El nombre de host no es válido");
            return;
        }

        String registryURL = "rmi://" + hostname + ":" + port + "/messagingApp";
        try {
            rmiServerInterface = (RMIServerInterface) Naming.lookup(registryURL);

            // The connection was set up correctly, simply exits
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.close();
        } catch (NotBoundException e) {
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

    @FXML
    private void printErrorMessage(String message) {

        errorMessageText.setText(message);

        if(!errorMessageText.isVisible()) {
            errorMessageText.setVisible(true);
        }
    }

    @FXML
    public void loseFocus() {

        // Cannot be set on initialize as drivers are not ready yet
        rootPane.requestFocus();
    }

    private boolean isHostnameValid() {

        String hostname = hostnameTextField.getText();

        return !hostname.isEmpty();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        errorMessageText.setVisible(false);

        // Dodges IANA reserved ports (0-1023), and private (dynamic) ports (49152 and above)
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1024, 49151, 1099);
        portSpinner.setValueFactory(valueFactory);
    }
}
