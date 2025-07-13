package com.ed.ecommerce.mvcDemo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean; // Necesitas esta importación

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException; // Importa SQLException
import java.sql.Statement; // Importa Statement

@SpringBootApplication
public class MvcDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(MvcDemoApplication.class, args);
	}
}
