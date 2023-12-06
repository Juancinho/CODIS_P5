package com.iagofernandezjuanotero;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import java.net.URL;
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


    private RMIServerImpl rmiServerImpl;

    private int port;

    @FXML
    public void onConnectButtonClick () {

        port = portSpinner.getValue();
    }

    @FXML
    public void loseFocus() {

        // Cannot be set on initialize as drivers are not ready yet
        rootPane.requestFocus();
    }

    private boolean isUsernameValid() {

        String username = hostnameTextField.getText();

        return !username.isEmpty();
    }

    private boolean isPasswordValid() {

        String password = hostnameTextField.getText();

        return !password.isEmpty();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        errorMessageText.setVisible(false);

        // Dodges IANA reserved ports (0-1023), and private (dynamic) ports (49152 and above)
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1024, 49151, 1099);
        portSpinner.setValueFactory(valueFactory);
    }
}
