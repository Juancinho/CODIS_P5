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

    private RMIServerImpl rmiServerImpl;
    private RMIClientImpl rmiClientImpl;

    public void setRmiServerImpl(RMIServerImpl rmiServerImpl) {

        this.rmiServerImpl = rmiServerImpl;
    }

    public void setRmiClientImpl(RMIClientImpl rmiClientImpl) {
        this.rmiClientImpl = rmiClientImpl;
    }

    @FXML
    public void onLoginButtonClick (ActionEvent event) {

        String username = usernameTextField.getText();
        String password = passwordField.getText();

        if (!isUsernameValid()) {
            printErrorMessage("El nombre de usuario no es válido");
            return;
        } else if (!isPasswordValid()) {
            printErrorMessage("La contraseña no es válida");
            return;
        } else {
            try {
                if (!rmiServerImpl.isUsernameTaken(username)) {
                    printErrorMessage("No existe ningún usuario con ese nombre");
                    return;
                } else if (!rmiServerImpl.verifyPassword(username, password)) {
                    printErrorMessage("La contraseña introducida no es correcta");
                    return;
                }
            } catch (RemoteException e) {
                System.out.println("Excepción de invocación remota: " + e.getMessage());
                printErrorMessage("Invocación remota fallida");
            }
        }

        try {
            rmiClientImpl = rmiServerImpl.createNewClient(username, password);
            rmiServerImpl.registerClient(username, password, rmiClientImpl);

            // User successfully logged in to the app
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.close();
        } catch (RemoteException e) {
            System.out.println("Excepción de invocación remota: " + e.getMessage());
            printErrorMessage("Invocación remota fallida");
        }
    }

    @FXML
    public void onRegisterButtonClick (ActionEvent event) {

        String username = usernameTextField.getText();
        String password = passwordField.getText();

        if (!isUsernameValid()) {
            printErrorMessage("El nombre de usuario no es válido");
            return;
        } else if (!isPasswordValid()) {
            printErrorMessage("La contraseña no es válida");
            return;
        } else {
            try {
                if (rmiServerImpl.isUsernameTaken(username)) {
                    printErrorMessage("Ya existe un usuario con ese nombre");
                    return;
                }
            } catch (RemoteException e) {
                System.out.println("Excepción de invocación remota: " + e.getMessage());
                printErrorMessage("Invocación remota fallida");
            }
        }

        try {
            rmiClientImpl = rmiServerImpl.createNewClient(username, password);
            System.out.println("DATOS: " + rmiClientImpl.getUsername() + " | " + rmiClientImpl.getPasswordHash());
            rmiServerImpl.registerClient(username, password, rmiClientImpl);

            // User successfully registered in the app
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.close();
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

    private boolean isUsernameValid() {

        String username = usernameTextField.getText();

        return !username.isEmpty();
    }

    private boolean isPasswordValid() {

        String password = passwordField.getText();

        return !password.isEmpty();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        errorMessageText.setVisible(false);
    }
}
