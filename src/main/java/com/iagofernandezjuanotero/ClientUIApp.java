package com.iagofernandezjuanotero;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientUIApp extends Application {

    // Required attributes
    RMIClient rmiClient;

    // Connection instances
    RMIServerInterface rmiServerInterface;
    RMIClientInterface rmiClientInterface;

    // Controllers
    ConnectionSetupController connectionSetupController;
    LoginController loginController;
    MainController mainController;

    @Override
    public void start (Stage mainStage) throws Exception {

        rmiClient = new RMIClient();
        //rmiServerImpl = new RMIServerImpl();ç
        // All implementation instances are initialized within controllers (or methods called by them)

        FXMLLoader connectionSetupLoader = new FXMLLoader(RMIClient.class.getResource("ConnectionSetupView.fxml"));
        Scene connectionSetupScene = new Scene(connectionSetupLoader.load(), 420, 218);
        Stage connectionSetupStage = new Stage();
        connectionSetupStage.setTitle("Conectarse al servidor");
        connectionSetupStage.setScene(connectionSetupScene);
        connectionSetupStage.setResizable(false);
        /*connectionSetupStage.setOnCloseRequest(event -> {
            // TODO when exit button clicked
            System.exit(0);
        });*/
        connectionSetupController = connectionSetupLoader.getController();
        connectionSetupController.loseFocus();
        //connectionSetupController.setRmiServerInterface(rmiServerImpl);
        connectionSetupStage.showAndWait();
        rmiServerInterface = connectionSetupController.getRmiServerInterface();

        FXMLLoader loginFxmlLoader = new FXMLLoader(RMIClient.class.getResource("LoginView.fxml"));
        Scene loginScene = new Scene(loginFxmlLoader.load(), 441, 227);
        Stage loginStage = new Stage();
        loginStage.setTitle("Iniciar sesión o registrarse");
        loginStage.setScene(loginScene);
        loginStage.setResizable(false);
        /*loginStage.setOnCloseRequest(event -> {
            // TODO when exit button clicked
            System.exit(0);
        });*/
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
        mainStage.setOnCloseRequest(event -> {
            // TODO when exit button clicked
            System.exit(0);
        });
        // TODO main functionality
        mainController = mainFxmlLoader.getController();
        mainController.loseFocus();
        mainController.setRmiServerInterface(rmiServerInterface);
        mainController.setRmiClientInterface(rmiClientInterface);
        rmiClientInterface.setMainController(mainController);
        rmiServerInterface.registerClient(rmiClientInterface.getUsername(), rmiClientInterface.getPasswordHash(), rmiClientInterface);
        mainController.updatePendingRequestsChoiceBox();
        mainController.updateReceiverComboBox();
        mainStage.show();

        // TODO Keep updating controllers/other instances without reference losses
        rmiClient.setReferences(loginController, mainController, rmiServerInterface, rmiClientInterface);
        rmiClient.run();

        // Leaves the JavaFX thread to keep UI up
    }
}
