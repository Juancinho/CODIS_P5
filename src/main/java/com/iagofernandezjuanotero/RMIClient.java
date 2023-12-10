package com.iagofernandezjuanotero;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.rmi.RemoteException;

public class RMIClient extends Application {

    // Connection instances
    RMIServerInterface rmiServerInterface;
    RMIClientInterface rmiClientInterface;

    // Controllers
    ConnectionSetupController connectionSetupController;
    LoginController loginController;
    MainController mainController;

    @Override
    public void start (Stage mainStage) throws Exception {

        FXMLLoader connectionSetupLoader = new FXMLLoader(RMIClient.class.getResource("ConnectionSetupView.fxml"));
        Scene connectionSetupScene = new Scene(connectionSetupLoader.load(), 420, 218);
        Stage connectionSetupStage = new Stage();
        connectionSetupStage.setTitle("Conectarse al servidor");
        connectionSetupStage.setScene(connectionSetupScene);
        connectionSetupStage.setResizable(false);
        connectionSetupStage.setOnCloseRequest(event -> {
            // TODO when exit button clicked
            System.out.println("Se ha cerrado la ventana de establecimiento de conexión");
            System.exit(0);
        });
        connectionSetupController = connectionSetupLoader.getController();
        connectionSetupController.loseFocus();
        connectionSetupStage.showAndWait();
        rmiServerInterface = connectionSetupController.getRmiServerInterface();

        FXMLLoader loginFxmlLoader = new FXMLLoader(RMIClient.class.getResource("LoginView.fxml"));
        Scene loginScene = new Scene(loginFxmlLoader.load(), 441, 227);
        Stage loginStage = new Stage();
        loginStage.setTitle("Iniciar sesión o registrarse");
        loginStage.setScene(loginScene);
        loginStage.setResizable(false);
        loginStage.setOnCloseRequest(event -> {
            System.out.println("Se ha cerrado la ventana de inicio de sesión");
            System.exit(0);
        });
        loginController = loginFxmlLoader.getController();
        loginController.loseFocus();
        loginController.setRmiServerInterface(rmiServerInterface);
        loginStage.showAndWait();

        rmiServerInterface = loginController.getRmiServerInterface();
        rmiClientInterface = loginController.getRmiClientInterface();

        FXMLLoader mainFxmlLoader = new FXMLLoader(RMIClient.class.getResource("MainView.fxml"));
        Scene mainScene = new Scene(mainFxmlLoader.load(), 796, 529);
        mainStage.setTitle("Aplicación P2P | " + rmiClientInterface.getUsername());
        mainStage.setScene(mainScene);
        mainStage.setResizable(false);
        mainController = mainFxmlLoader.getController();
        mainController.loseFocus();
        mainController.setRmiServerInterface(rmiServerInterface);
        mainController.setRmiClientInterface(rmiClientInterface);
        rmiClientInterface.setMainController(mainController);
        rmiServerInterface.registerClient(rmiClientInterface.getUsername(), rmiClientInterface.getPasswordHash(), rmiClientInterface);
        mainController.updatePendingRequestsChoiceBox();
        mainController.updateReceiverComboBox();

        mainController.printWhileOfflineMessages();
        mainStage.setOnCloseRequest(event -> {
            try {
                rmiServerInterface.unregisterClient(rmiClientInterface.getUsername());
            } catch (RemoteException e) {
                System.out.println("Excepción de acceso remoto: " + e.getMessage());
            }
            System.out.println("Finalizando el programa...");
            System.exit(0);
        });
        mainStage.show();

        // Leaves the JavaFX thread to keep UI up
        // The window closing listener is set up above, to unregister the client and exit
    }
}
