package com.collectorhub.backend.Controller;

import com.collectorhub.backend.Repository.UsuarioRepository;
import com.collectorhub.backend.Entidades.Usuario; // Asegúrate de que esta ruta coincida con tu clase Usuario
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST encargado de gestionar la autenticación y registro de los usuarios.
 * Expone los endpoints de la API relacionados con el acceso (Login) y creación de cuentas (Register),
 * permitiendo peticiones de orígenes cruzados (CORS) para comunicarse con el Frontend (React).
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class LoginController {

    /**
     * Inyección de dependencias del repositorio de usuarios.
     * Permite realizar consultas a la base de datos MySQL (JPA).
     */
    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Inyección de la herramienta de cifrado de contraseñas (BCrypt).
     * Se configura en SecurityConfig.java.
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Procesa las peticiones HTTP POST enviadas a la ruta "/api/login".
     * Extrae las credenciales del cuerpo de la petición (JSON) y las verifica
     * de forma dinámica contra los registros de la base de datos.
     */
    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> credenciales) {
        String alias = credenciales.get("alias");
        String pass = credenciales.get("password");

        // BÚSQUEDA REAL EN LA BASE DE DATOS
        return usuarioRepository.findByAlias(alias)
                .map(user -> {
                    // OJO: Como ahora vamos a cifrar las contraseñas, en el futuro aquí usaremos passwordEncoder.matches()
                    // Pero por ahora lo dejamos así para no romper los usuarios de prueba del init.sql
                    if (user.getPassword().equals(pass)) {
                        return "¡Acceso concedido! Bienvenido " + user.getAlias();
                    }
                    return "Contraseña incorrecta.";
                })
                .orElse("El usuario no existe en la base de datos.");
    }

    /**
     * Procesa las peticiones HTTP POST enviadas a la ruta "/api/register".
     * Crea un nuevo usuario validando que el alias y el correo no existan previamente,
     * y cifra la contraseña antes de guardarla en la base de datos.
     */
    @PostMapping("/register")
    public String registrarUsuario(@RequestBody Map<String, String> datosUsuario) {
        String alias = datosUsuario.get("alias");
        String correo = datosUsuario.get("correo_electronico");
        String passPlana = datosUsuario.get("password");

        // 1. Comprobar que no faltan datos
        if (alias == null || correo == null || passPlana == null) {
            return "Error: Faltan datos obligatorios.";
        }

        // 2. Comprobar que el alias no existe (usando el radar que pusimos en el Repository)
        if (usuarioRepository.existsByAlias(alias)) {
            return "Error: El alias ya está en uso. Elige otro.";
        }

        // 3. Comprobar que el correo no existe
        if (usuarioRepository.comprobarSiExisteCorreo(correo)) {
            return "Error: Ya existe una cuenta con este correo electrónico.";
        }

        // 4. Encriptar la contraseña
        String passEncriptada = passwordEncoder.encode(passPlana);

        // 5. Crear el nuevo usuario y guardarlo
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setAlias(alias);
        nuevoUsuario.setCorreo_electronico(correo);
        nuevoUsuario.setPassword(passEncriptada);
        nuevoUsuario.setRol("user"); // Por defecto, todo el que se registra es un usuario normal

        usuarioRepository.save(nuevoUsuario);

        return "¡Usuario registrado con éxito! Ya puedes iniciar sesión.";
    }
}