/*
 * Actividad: Aplicaciones P2P. Clase controlador de la vista de inicio de sesión
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

import java.net.URL;
import java.rmi.RemoteException;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private Text errorMessageText;

    @FXML
    private TextField usernameTextField;

    @FXML
    private PasswordField passwordField;

    private RMIServerInterface rmiServerInterface;
    private RMIClientInterface rmiClientInterface;

    public void setRmiServerInterface(RMIServerInterface rmiServerInterface) {

        this.rmiServerInterface = rmiServerInterface;
    }

    public RMIServerInterface getRmiServerInterface() {

        return rmiServerInterface;
    }

    public RMIClientInterface getRmiClientInterface() {

        return rmiClientInterface;
    }

    // Method called when user clicks de LogIn button
    @FXML
    public void onLoginButtonClick (ActionEvent event) {

        // Extracts the parameters from the text fields
        String username = usernameTextField.getText();
        String password = passwordField.getText();

        // Does all necessary checks, calling printErrorMessage to show any possible error to user
        // In any case, it returns, not going further than this conditional clause
        if (isUsernameInvalid()) {
            printErrorMessage("El nombre de usuario no es válido");
            return;
        } else if (isPasswordInvalid()) {
            printErrorMessage("La contraseña no es válida");
            return;
        } else {
            try {
                if (!rmiServerInterface.isUsernameTaken(username)) {
                    printErrorMessage("No existe ningún usuario con ese nombre");
                    return;
                } else if (!rmiServerInterface.verifyPassword(username, password)) {
                    printErrorMessage("La contraseña introducida no es correcta");
                    return;
                } else if (rmiServerInterface.isUserOnline(username)) {
                    printErrorMessage("El usuario ya está conectado");
                    return;
                }
            } catch (RemoteException e) {
                System.out.println("Excepción de invocación remota: " + e.getMessage());
                printErrorMessage("Invocación remota fallida");
            }
        }

        // Tries to create the client interface. On success, simply exits
        try {
            rmiClientInterface = new RMIClientImpl(username, rmiServerInterface.calcHashForGivenPassword(password));
            //rmiClientInterface = rmiServerInterface.createNewClient(username, password);

            // User successfully logged in to the app
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.close();
        } catch (RemoteException e) {
            System.out.println("Excepción de invocación remota: " + e.getMessage());
            printErrorMessage("Invocación remota fallida");
        }
    }

    // Completely analogue to above method, but registering a new user to the database (instead of logging-in an existing one
    @FXML
    public void onRegisterButtonClick (ActionEvent event) {

        String username = usernameTextField.getText();
        String password = passwordField.getText();

        // Again, checks every possible mistyping in data
        if (isUsernameInvalid()) {
            printErrorMessage("El nombre de usuario no es válido");
            return;
        } else if (isPasswordInvalid()) {
            printErrorMessage("La contraseña no es válida");
            return;
        } else {
            try {
                if (rmiServerInterface.isUsernameTaken(username)) {
                    printErrorMessage("Ya existe un usuario con ese nombre");
                    return;
                }
            } catch (RemoteException e) {
                System.out.println("Excepción de invocación remota: " + e.getMessage());
                printErrorMessage("Invocación remota fallida");
            }
        }

        // And again, creates the client interface (whose reference will be later get from the main JavaFX thread), and exits on success
        try {
            rmiClientInterface = new RMIClientImpl(username, rmiServerInterface.calcHashForGivenPassword(password));
            //rmiClientInterface = rmiServerInterface.createNewClient(username, password);

            // User successfully registered in the app
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.close();
        } catch (RemoteException e) {
            System.out.println("Excepción de invocación remota: " + e.getMessage());
            printErrorMessage("Invocación remota fallida");
        }
    }

    // Same function that in ConnectionSetupController, prints errors to UI by changing the value of a text component
    @FXML
    private void printErrorMessage(String message) {

        errorMessageText.setText(message);

        if(!errorMessageText.isVisible()) {
            errorMessageText.setVisible(true);
        }
    }

    // Again, sets the scene focus to the background pane
    @FXML
    public void loseFocus() {

        // Cannot be set on initialize as drivers are not ready yet
        rootPane.requestFocus();
    }

    // Auxiliary functions that check if the username and password written by the user are valid
    // This is, they must be non-null and non-empty
    private boolean isUsernameInvalid() {

        String username = usernameTextField.getText();

        return username.isEmpty();
    }

    private boolean isPasswordInvalid() {

        String password = passwordField.getText();

        return password.isEmpty();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        errorMessageText.setVisible(false);
    }
}
