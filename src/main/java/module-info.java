module com.iagofernandezjuanotero {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.rmi;
    requires java.sql;
    requires org.postgresql.jdbc;


    opens com.iagofernandezjuanotero to javafx.fxml;
    exports com.iagofernandezjuanotero;
}