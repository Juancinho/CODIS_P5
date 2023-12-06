package com.iagofernandezjuanotero;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import java.net.URL;
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

    @FXML
    public void onLoginButtonClick () {


    }

    @FXML
    public void onRegisterButtonClick () {
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
