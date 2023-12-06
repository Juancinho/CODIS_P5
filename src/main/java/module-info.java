module com.iagofernandezjuanotero {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.rmi;


    opens com.iagofernandezjuanotero to javafx.fxml;
    exports com.iagofernandezjuanotero;
}