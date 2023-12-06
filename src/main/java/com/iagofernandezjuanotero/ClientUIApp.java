package com.iagofernandezjuanotero;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ClientUIApp extends Application {

    // Required attributes
    RMIClient rmiClient;

    // Connection instances
    RMIServerImpl rmiServerImpl;
    RMIClientImpl rmiClientImpl;

    // Controllers
    ConnectionSetupController connectionSetupController;
    LoginController loginController;
    MainController mainController;

    @Override
    public void start (Stage mainStage) throws Exception {

        rmiClient = new RMIClient();
        rmiServerImpl = new RMIServerImpl();
        //rmiClientImpl = new RMIClientImpl();

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
        connectionSetupController.setRmiServerInterface(rmiServerImpl);
        connectionSetupStage.showAndWait();

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
        loginController.setRmiServerImpl(rmiServerImpl);
        loginController.setRmiClientImpl(rmiClientImpl);
        loginStage.showAndWait();

        FXMLLoader mainFxmlLoader = new FXMLLoader(RMIClient.class.getResource("MainView.fxml"));
        Scene mainScene = new Scene(mainFxmlLoader.load(), 800, 400);
        mainStage.setTitle("Aplicaciones P2P");
        mainStage.setScene(mainScene);
        mainStage.setResizable(false);
        mainStage.setOnCloseRequest(event -> {
            // TODO when exit button clicked
            System.exit(0);
        });
        mainController = mainFxmlLoader.getController();
        mainStage.show();

        // TODO Keep updating controllers/other instances without reference losses
        rmiClient.setControllers(loginController, mainController);
        rmiClient.run();
    }
}
