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
    public ResponseEntity<?> registrarUsuario(@RequestBody RegistroRequestDTO registroDTO) {
        // 1. Validaciones de siempre
        if (usuarioRepository.findByAlias(registroDTO.getAlias()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: El alias ya está en uso.");
        }
        if (usuarioRepository.comprobarSiExisteCorreo(registroDTO.getCorreoElectronico())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: El correo ya está en uso.");
        }

        // 2. Crear usuario
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setAlias(registroDTO.getAlias());
        nuevoUsuario.setCorreoElectronico(registroDTO.getCorreoElectronico());
        nuevoUsuario.setPassword(passwordEncoder.encode(registroDTO.getPassword()));
        nuevoUsuario.setRol("user");
        nuevoUsuario.setCuentaActiva(false);

        String pinSeguridad = String.format("%06d", new java.util.Random().nextInt(1000000));
        nuevoUsuario.setCodigoVerificacion(pinSeguridad);

        // 3. Guardar en la DB
        usuarioRepository.save(nuevoUsuario);

        // 4. Lanzar el mail (al ser @Async, esto no frena la respuesta)
        emailService.enviarCorreoPin(nuevoUsuario.getCorreoElectronico(), nuevoUsuario.getAlias(), pinSeguridad);

        // 5. Responder YA a React
        return ResponseEntity.ok("Te hemos enviado un código de 6 dígitos a tu correo.");
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