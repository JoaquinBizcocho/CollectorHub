package com.collectorhub.backend.Controller;

import com.collectorhub.backend.DTO.*;
import com.collectorhub.backend.Entidades.Usuario;
import com.collectorhub.backend.Repository.UsuarioRepository;
import com.collectorhub.backend.security.JwtUtil;
import com.collectorhub.backend.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.security.SecureRandom;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "https://collector-hub-frontend.vercel.app")
public class AuthController {

    private static final int MAX_INTENTOS_PIN = 3;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // LOGIN

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO credenciales) {
        Optional<Usuario> optionalUser = usuarioRepository.findByAlias(credenciales.getAlias());

        if (optionalUser.isPresent()) {
            Usuario user = optionalUser.get();
            if (passwordEncoder.matches(credenciales.getPassword(), user.getPassword())) {

                if (!user.isCuentaActiva()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("Cuenta inactiva. Verifica tu correo electrónico primero.");
                }

                AuthResponseDTO respuesta = new AuthResponseDTO();
                respuesta.setMensaje("Acceso concedido");
                respuesta.setUsuarioId(user.getId());
                respuesta.setRol(user.getRol());
                respuesta.setToken(jwtUtil.generarToken(user.getId().intValue(), user.getAlias(), user.getRol()));

                return ResponseEntity.ok(respuesta);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas.");
    }


    // REGISTRO

    @PostMapping("/register")
    public ResponseEntity<String> registrarUsuario(@RequestBody RegistroRequestDTO registroDTO) {

        // 1. Validación de formatos
        String regexCorreo = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        String regexPassword = "^(?=.*[0-9])(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$";

        if (registroDTO.getCorreoElectronico() == null || !registroDTO.getCorreoElectronico().matches(regexCorreo)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Correo inválido.");
        }

        if (registroDTO.getPassword() == null || !registroDTO.getPassword().matches(regexPassword)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: Contraseña poco segura. Debe tener mínimo 8 caracteres, al menos un número y al menos un símbolo (!@#$%^&*...).");
        }

        // 2. Comprobación de duplicados (alias Y correo)
        if (usuarioRepository.findByAlias(registroDTO.getAlias()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: El alias '" + registroDTO.getAlias() + "' ya está en uso.");
        }

        if (usuarioRepository.findByCorreoElectronico(registroDTO.getCorreoElectronico()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: Ya existe una cuenta registrada con ese correo electrónico.");
        }

        // 3. Crear y guardar usuario temporal
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setAlias(registroDTO.getAlias());
        nuevoUsuario.setCorreoElectronico(registroDTO.getCorreoElectronico());
        nuevoUsuario.setPassword(passwordEncoder.encode(registroDTO.getPassword()));
        nuevoUsuario.setRol("user");
        nuevoUsuario.setCuentaActiva(false);
        nuevoUsuario.setFechaRegistro(LocalDateTime.now());
        nuevoUsuario.setIntentosFallidos(0);

        String pin = String.format("%06d", new SecureRandom().nextInt(1000000));
        nuevoUsuario.setCodigoVerificacion(pin);

        usuarioRepository.save(nuevoUsuario);

        // 4. Intentar envío de correo
        try {
            emailService.enviarCorreoAPI(nuevoUsuario.getCorreoElectronico(), nuevoUsuario.getAlias(), pin);
        } catch (Exception e) {
            usuarioRepository.delete(nuevoUsuario);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al enviar el correo. Inténtalo de nuevo en unos segundos.");
        }

        return ResponseEntity.ok("Registro guardado. Revisa tu email para obtener el PIN (caduca en 5 minutos).");
    }


    // VERIFICACIÓN DE PIN

    @PostMapping("/verify-pin")
    public ResponseEntity<String> verificarPin(@RequestBody PinRequestDTO datos) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByAlias(datos.getAlias());

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuario no encontrado.");
        }

        Usuario u = usuarioOpt.get();

        // Comprobar si el PIN ya expiró (cuenta eliminada por scheduler o fecha pasada)
        if (u.getFechaRegistro() != null &&
                u.getFechaRegistro().plusMinutes(5).isBefore(LocalDateTime.now())) {
            usuarioRepository.delete(u);
            return ResponseEntity.status(HttpStatus.GONE)
                    .body("El PIN ha caducado. Tu registro ha sido eliminado. Vuelve a registrarte.");
        }

        // comprobar si superó los intentos máximos
        if (u.getIntentosFallidos() >= MAX_INTENTOS_PIN) {
            usuarioRepository.delete(u);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Demasiados intentos fallidos. Tu registro ha sido eliminado por seguridad. Vuelve a registrarte.");
        }

        if (u.getCodigoVerificacion() != null && u.getCodigoVerificacion().equals(datos.getPin())) {
            u.setCuentaActiva(true);
            u.setCodigoVerificacion(null);
            u.setIntentosFallidos(0);
            usuarioRepository.save(u);
            return ResponseEntity.ok("Cuenta verificada correctamente.");
        }

        // PIN incorrecto: sumar intento fallido
        u.setIntentosFallidos(u.getIntentosFallidos() + 1);
        int intentosRestantes = MAX_INTENTOS_PIN - u.getIntentosFallidos();
        usuarioRepository.save(u);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("PIN incorrecto. Te quedan " + intentosRestantes + " intento(s).");
    }


    // SCHEDULER: limpieza automática cada minuto
    @Scheduled(fixedRate = 60000) // se ejecuta cada 60 segundos
    public void limpiarCuentasNoVerificadas() {
        LocalDateTime hace5Minutos = LocalDateTime.now().minusMinutes(5);
        List<Usuario> expirados = usuarioRepository
                .buscarCuentasExpiradas(hace5Minutos);

        if (!expirados.isEmpty()) {
            usuarioRepository.deleteAll(expirados);
            System.out.println("Limpieza: eliminados " + expirados.size() + " usuario(s) sin verificar.");
        }
    }
}