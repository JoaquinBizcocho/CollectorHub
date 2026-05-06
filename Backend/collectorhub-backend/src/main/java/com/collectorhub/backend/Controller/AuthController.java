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
@CrossOrigin(origins = "http://localhost:5173")
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

        String regexCorreo = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        if (registroDTO.getCorreoElectronico() == null || !registroDTO.getCorreoElectronico().matches(regexCorreo)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: El formato del correo no es valido.");
        }

        if (usuarioRepository.findByAlias(registroDTO.getAlias()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: El alias ya esta en uso.");
        }
        if (usuarioRepository.comprobarSiExisteCorreo(registroDTO.getCorreoElectronico())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Ya existe una cuenta con este correo.");
        }

        // Pasamos los datos seguros del DTO a la Entidad real
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setAlias(registroDTO.getAlias());
        nuevoUsuario.setCorreoElectronico(registroDTO.getCorreoElectronico());
        nuevoUsuario.setPassword(passwordEncoder.encode(registroDTO.getPassword()));
        nuevoUsuario.setRol("user");
        nuevoUsuario.setCuentaActiva(false);

        String pinSeguridad = String.format("%06d", new Random().nextInt(1000000));
        nuevoUsuario.setCodigoVerificacion(pinSeguridad);

        usuarioRepository.save(nuevoUsuario);

        try {
            emailService.enviarCorreoPin(nuevoUsuario.getCorreoElectronico(), nuevoUsuario.getAlias(), pinSeguridad);
        } catch (Exception e) {
            usuarioRepository.delete(nuevoUsuario);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: No pudimos enviar el correo.");
        }

        return ResponseEntity.ok("Registro guardado. Esperando PIN de verificacion.");
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