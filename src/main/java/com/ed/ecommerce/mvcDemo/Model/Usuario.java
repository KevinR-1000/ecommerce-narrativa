package com.ed.ecommerce.mvcDemo.Model;

import jakarta.persistence.*;

@Entity
// CAMBIO: Nombre de la tabla a minúsculas para coincidir con PostgreSQL.
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // CAMBIO: Nombre de la columna a snake_case.
    @Column(name = "id_usuario")
    private Integer idUsuario;

    // CAMBIO: Nombre de la columna a snake_case.
    @Column(name = "primer_nombre")
    private String primerNombre;

    // CAMBIO: Nombre de la columna a snake_case.
    @Column(name = "segundo_nombre")
    private String segundoNombre;

    // CAMBIO: Nombre de la columna a snake_case.
    @Column(name = "primer_apellido")
    private String primerApellido;

    // CAMBIO: Nombre de la columna a snake_case.
    @Column(name = "segundo_apellido")
    private String segundoApellido;

    @Column(name = "correo", unique = true)
    private String correo;

    @Column(name = "contrasena")
    private String contrasena;

    @Column(name = "direccion")
    private String direccion;

    @Column(name = "telefono")
    private String telefono;

    // --- Getters y Setters (Estos no cambian) ---

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getPrimerNombre() {
        return primerNombre;
    }

    public void setPrimerNombre(String primerNombre) {
        this.primerNombre = primerNombre;
    }

    public String getSegundoNombre() {
        return segundoNombre;
    }

    public void setSegundoNombre(String segundoNombre) {
        this.segundoNombre = segundoNombre;
    }

    public String getPrimerApellido() {
        return primerApellido;
    }

    public void setPrimerApellido(String primerApellido) {
        this.primerApellido = primerApellido;
    }

    public String getSegundoApellido() {
        return segundoApellido;
    }

    public void setSegundoApellido(String segundoApellido) {
        this.segundoApellido = segundoApellido;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}