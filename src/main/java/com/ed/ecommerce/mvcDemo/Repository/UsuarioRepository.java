package com.ed.ecommerce.mvcDemo.Repository;

import com.ed.ecommerce.mvcDemo.Model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
// Spring Boot crea todos los métodos (save, findById, findAll, etc.).
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // Spring Data JPA entenderá este nombre y creará automáticamente la consulta "SELECT * FROM Usuario WHERE correo = ?"
    Optional<Usuario> findByCorreo(String correo);

}