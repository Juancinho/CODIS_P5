package com.iagofernandezjuanotero;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ClientUIApp extends Application {

    // Controllers and/or clients  (or server)

    RMIClient rmiClient;
    RMIClientImpl rmiClientImpl;
    LoginController loginController;
    MainController mainController;

    @Override
    public void start (Stage mainStage) throws Exception {

        rmiClient = new RMIClient();

        FXMLLoader mainFxmlLoader = new FXMLLoader(RMIClient.class.getResource("MainView.fxml"));
        Scene mainScene = new Scene(mainFxmlLoader.load(), 800, 400);
        mainStage.setTitle("Practica 6 | Aplicaciones P2P");
        mainStage.setScene(mainScene);
        mainStage.setResizable(false);
        mainStage.setOnCloseRequest(event -> {
            // TODO when exit button clicked
            System.exit(0);
        });
        mainController = mainFxmlLoader.getController();
        mainStage.show();

        FXMLLoader loginFxmlLoader = new FXMLLoader(RMIClient.class.getResource("LoginView.fxml"));
        Scene loginScene = new Scene(loginFxmlLoader.load(), 399, 205);
        Stage loginStage = new Stage();
        loginStage.setTitle("Iniciar sesión o registrarse");
        loginStage.setScene(loginScene);
        loginStage.setResizable(false);
        loginStage.setOnCloseRequest(event -> {
            // TODO when exit button clicked
            System.exit(0);
        });
        loginController = loginFxmlLoader.getController();
        loginController.loseFocus();
        loginStage.show();

        // TODO Keep updating controllers/other instances without reference lost
        rmiClient.setControllers(loginController, mainController);
        rmiClient.run();
    }
}
