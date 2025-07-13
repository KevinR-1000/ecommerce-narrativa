package com.ed.ecommerce.mvcDemo.Pattern;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

@Component // Para que Spring la detecte y la ejecute.
public class DatabaseConnectionChecker implements CommandLineRunner {

    // Inyectamos el DataSource, que es el gestor de la conexión a la base de datos.
    private final DataSource dataSource;

    public DatabaseConnectionChecker(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=================================================");
        System.out.println("VERIFICANDO CONEXIÓN CON LA BASE DE DATOS...");

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();

            System.out.println("✅ ¡CONEXIÓN EXITOSA!");
            System.out.println("   - URL de la Base de Datos: " + metaData.getURL());
            System.out.println("   - Usuario: " + metaData.getUserName());
            System.out.println("   - Nombre del Driver: " + metaData.getDriverName());
            System.out.println("   - Versión del Driver: " + metaData.getDriverVersion());

        } catch (Exception e) {
            System.err.println("❌ ¡ERROR AL CONECTAR CON LA BASE DE DATOS!");
            System.err.println("   - Mensaje de Error: " + e.getMessage());
            System.err.println("   - Revisa tu archivo 'application.properties' y asegúrate de que la base de datos esté activa.");
        }
        System.out.println("=================================================");
    }
}