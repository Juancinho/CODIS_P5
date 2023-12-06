package com.iagofernandezjuanotero;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ClientUIApp extends Application {

    // Controllers and/or clients  (or server)
    RMIClient rmiClient;
    LoginController loginController;

    @Override
    public void start (Stage mainStage) throws  Exception {

        //rmiClient = new RMIClient();

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
        loginStage.showAndWait();

        // TODO Create the main view, then rely on RMIClient to maintain the program flow
    }
}
