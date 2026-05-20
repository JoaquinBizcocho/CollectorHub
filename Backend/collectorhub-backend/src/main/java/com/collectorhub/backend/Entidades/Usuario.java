package com.collectorhub.backend.Entidades;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Data
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String alias;

    // Guarda la contraseña encriptada con BCrypt
    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String correoElectronico;

    // User o Admin
    private String rol;

    // La cuenta no esta activa hasta que el usuario no verifique su correo con el PIN
    @Column(nullable = false)
    private boolean cuentaActiva = false;

    // Aqui esta el PIN temporal de 6 digitos enviado por correo para activar la cuenta
    @Column(length = 6)
    private String codigoVerificacion;


    @Column
    private LocalDateTime fechaRegistro;

    // Contador de logins fallidos que se usa para bloquear la cuenta si supera el limite
    @Column(nullable = false)
    private int intentosFallidos = 0;

}