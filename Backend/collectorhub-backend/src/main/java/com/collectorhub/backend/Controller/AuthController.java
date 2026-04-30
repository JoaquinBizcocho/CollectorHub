package com.collectorhub.backend.Controller;

import com.collectorhub.backend.Entidades.Usuario;
import com.collectorhub.backend.Repository.UsuarioRepository;
import com.collectorhub.backend.security.JwtUtil;
import com.collectorhub.backend.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // --- 1. LOGIN ---
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credenciales) {
        String alias = credenciales.get("alias");
        String pass = credenciales.get("password");

        Optional<Usuario> optionalUser = usuarioRepository.findByAlias(alias);

        if (optionalUser.isPresent()) {
            Usuario user = optionalUser.get();
            if (passwordEncoder.matches(pass, user.getPassword())) {

                // COMPROBACIÓN DE SEGURIDAD: ¿Tiene la cuenta verificada con el PIN?
                if (!user.isCuentaActiva()) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("mensaje", "Cuenta inactiva. Verifica tu correo electronico primero.");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                }

                Map<String, Object> respuesta = new HashMap<>();
                respuesta.put("mensaje", "Acceso concedido");
                respuesta.put("usuarioId", user.getId());
                respuesta.put("rol", user.getRol());
                respuesta.put("token", jwtUtil.generarToken(user.getId().intValue(), user.getAlias(), user.getRol()));

                return ResponseEntity.ok(respuesta);
            }
        }

        Map<String, Object> error = new HashMap<>();
        error.put("mensaje", "Credenciales incorrectas");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // --- 2. REGISTRO ---
    @PostMapping("/register")
    public ResponseEntity<String> registrarUsuario(@RequestBody Usuario nuevoUsuario) {

        // Validamos formato del correo
        String regexCorreo = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        if (nuevoUsuario.getCorreoElectronico() == null || !nuevoUsuario.getCorreoElectronico().matches(regexCorreo)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: El formato del correo no es valido.");
        }

        // Comprobamos alias y correo en BD
        if (usuarioRepository.findByAlias(nuevoUsuario.getAlias()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: El alias ya esta en uso.");
        }
        if (usuarioRepository.comprobarSiExisteCorreo(nuevoUsuario.getCorreoElectronico())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Ya existe una cuenta con este correo.");
        }

        // Preparamos al usuario
        nuevoUsuario.setPassword(passwordEncoder.encode(nuevoUsuario.getPassword()));
        if (nuevoUsuario.getRol() == null || nuevoUsuario.getRol().isEmpty()) { nuevoUsuario.setRol("user"); }

        // --- LA MAGIA DEL PIN ---
        nuevoUsuario.setCuentaActiva(false); // Nace inactivo
        String pinSeguridad = String.format("%06d", new Random().nextInt(1000000)); // Genera 6 numeros
        nuevoUsuario.setCodigoVerificacion(pinSeguridad);

        // Guardamos en BD temporalmente
        usuarioRepository.save(nuevoUsuario);

        // Intentamos enviar el correo
        try {
            emailService.enviarCorreoPin(nuevoUsuario.getCorreoElectronico(), nuevoUsuario.getAlias(), pinSeguridad);
        } catch (Exception e) {
            // SI FALLA EL CORREO: Borramos al usuario de la BD para que no se quede atascado
            usuarioRepository.delete(nuevoUsuario);
            e.printStackTrace();
            // Le decimos a React que falló, así no pasa a la pantalla del PIN
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error crítico: No pudimos enviar el correo. Revisa la configuración del servidor.");
        }

        return ResponseEntity.ok("Registro guardado. Esperando PIN de verificacion.");
    }

    // --- 3. VERIFICAR PIN ---
    @PostMapping("/verify-pin")
    public ResponseEntity<String> verificarPin(@RequestBody Map<String, String> datos) {
        String alias = datos.get("alias");
        String pinIngresado = datos.get("pin");

        Optional<Usuario> usuarioOpt = usuarioRepository.findByAlias(alias);

        if (usuarioOpt.isPresent()) {
            Usuario u = usuarioOpt.get();
            // Comprobamos si el PIN coincide
            if (u.getCodigoVerificacion() != null && u.getCodigoVerificacion().equals(pinIngresado)) {
                u.setCuentaActiva(true); // ¡Cuenta activada!
                u.setCodigoVerificacion(null); // Borramos el PIN por seguridad
                usuarioRepository.save(u);
                return ResponseEntity.ok("Cuenta verificada correctamente.");
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El código PIN es incorrecto.");
    }
}