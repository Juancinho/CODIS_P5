package com.iagofernandezjuanotero;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = "jdbc:postgresql://localhost:5432/messagingappdb";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    public static Connection getConnection() {

        try {
            // Cargar el controlador JDBC
            Class.forName("org.postgresql.Driver");

            // Establecer la conexión con la base de datos
            return DriverManager.getConnection(URL, USER, PASSWORD);

        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Error de conexión sql: " + e.getMessage());
            throw new RuntimeException("Error al obtener la conexión a la base de datos", e);
        }
    }
}
