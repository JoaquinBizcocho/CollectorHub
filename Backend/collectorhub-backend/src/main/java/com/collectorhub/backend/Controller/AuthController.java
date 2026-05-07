package com.collectorhub.backend.Controller;

import com.collectorhub.backend.DTO.*;
import com.collectorhub.backend.Entidades.Usuario;
import com.collectorhub.backend.Repository.UsuarioRepository;
import com.collectorhub.backend.security.JwtUtil;
import com.collectorhub.backend.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "https://collector-hub-frontend.vercel.app")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO credenciales) {
        Optional<Usuario> optionalUser = usuarioRepository.findByAlias(credenciales.getAlias());

        if (optionalUser.isPresent()) {
            Usuario user = optionalUser.get();
            if (passwordEncoder.matches(credenciales.getPassword(), user.getPassword())) {

                if (!user.isCuentaActiva()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cuenta inactiva. Verifica tu correo electronico primero.");
                }

                AuthResponseDTO respuesta = new AuthResponseDTO();
                respuesta.setMensaje("Acceso concedido");
                respuesta.setUsuarioId(user.getId());
                respuesta.setRol(user.getRol());
                respuesta.setToken(jwtUtil.generarToken(user.getId().intValue(), user.getAlias(), user.getRol()));

                return ResponseEntity.ok(respuesta);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
    }

    @PostMapping("/register")
    public ResponseEntity<String> registrarUsuario(@RequestBody RegistroRequestDTO registroDTO) {
        // 1. Validación de Formatos
        String regexCorreo = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        String regexPassword = "^(?=.*[0-9])(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$";

        if (registroDTO.getCorreoElectronico() == null || !registroDTO.getCorreoElectronico().matches(regexCorreo)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Correo inválido.");
        }
        if (registroDTO.getPassword() == null || !registroDTO.getPassword().matches(regexPassword)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Contraseña poco segura (mínimo 8 caracteres, número y símbolo).");
        }

        // 2. Comprobación de duplicados
        if (usuarioRepository.findByAlias(registroDTO.getAlias()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: El alias ya está en uso.");
        }

        // 3. Crear y Guardar usuario temporal
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setAlias(registroDTO.getAlias());
        nuevoUsuario.setCorreoElectronico(registroDTO.getCorreoElectronico());
        nuevoUsuario.setPassword(passwordEncoder.encode(registroDTO.getPassword()));
        nuevoUsuario.setRol("user");
        nuevoUsuario.setCuentaActiva(false);

        String pin = String.format("%06d", new java.util.Random().nextInt(1000000));
        nuevoUsuario.setCodigoVerificacion(pin);

        usuarioRepository.save(nuevoUsuario);

        // 4. Intentar envío
        try {
            emailService.enviarCorreoAPI(nuevoUsuario.getCorreoElectronico(), nuevoUsuario.getAlias(), pin);
        } catch (Exception e) {
            // SI FALLA EL ENVÍO, BORRAMOS EL USUARIO FANTASMA
            usuarioRepository.delete(nuevoUsuario);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al enviar el correo. Inténtalo de nuevo en unos segundos.");
        }

        return ResponseEntity.ok("Registro guardado. Revisa tu email.");
    }

    @PostMapping("/verify-pin")
    public ResponseEntity<String> verificarPin(@RequestBody PinRequestDTO datos) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByAlias(datos.getAlias());

        if (usuarioOpt.isPresent()) {
            Usuario u = usuarioOpt.get();
            if (u.getCodigoVerificacion() != null && u.getCodigoVerificacion().equals(datos.getPin())) {
                u.setCuentaActiva(true);
                u.setCodigoVerificacion(null);
                usuarioRepository.save(u);
                return ResponseEntity.ok("Cuenta verificada correctamente.");
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El código PIN es incorrecto.");
    }
}

//@CrossOrigin(origins = "http://localhost:5173")
//@CrossOrigin(origins = "*")