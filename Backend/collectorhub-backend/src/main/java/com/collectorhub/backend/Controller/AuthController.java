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


    //Comprueba las credenciales y devuelve un token JWT si son correctas
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO credenciales) {
        //Buscamos el usuario por el alias en la base de datos
        Optional<Usuario> optionalUser = usuarioRepository.findByAlias(credenciales.getAlias());

        if (optionalUser.isPresent()) {
            Usuario user = optionalUser.get();
            //Comparamos la contraseña recibida con el hash almacenado
            if (passwordEncoder.matches(credenciales.getPassword(), user.getPassword())) {
                //Si el usuario no se ha verificado todavia el email no puede entrar
                if (!user.isCuentaActiva()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("Cuenta inactiva. Verifica tu correo electrónico primero.");
                }

                //Montamos la respuesta con el Token JWT y los datos basicos del usuario
                AuthResponseDTO respuesta = new AuthResponseDTO();
                respuesta.setMensaje("Acceso concedido");
                respuesta.setUsuarioId(user.getId());
                respuesta.setRol(user.getRol());
                respuesta.setToken(jwtUtil.generarToken(user.getId().intValue(), user.getAlias(), user.getRol()));

                return ResponseEntity.ok(respuesta);
            }
        }
        //Si el alias o la contraseña no existe no coinciden o no existen devolvemos un mensaje generico
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas.");
    }


    //Registra un nuevo usuario validando el formato del correo y contraseña
    //Genera un PIN de verificacion y lo manda por email
    @PostMapping("/register")
    public ResponseEntity<String> registrarUsuario(@RequestBody RegistroRequestDTO registroDTO) {

        // Validamos formato de correo y contraseña con regex antes de hacer nada
        String regexCorreo = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        String regexPassword = "^(?=.*[0-9])(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$";

        if (registroDTO.getCorreoElectronico() == null || !registroDTO.getCorreoElectronico().matches(regexCorreo)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Correo inválido.");
        }

        if (registroDTO.getPassword() == null || !registroDTO.getPassword().matches(regexPassword)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: Contraseña poco segura. Debe tener mínimo 8 caracteres, al menos un número y al menos un símbolo (!@#$%^&*...).");
        }

        // Comprobamos que el alias y el correo no esten en uso ya
        if (usuarioRepository.findByAlias(registroDTO.getAlias()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: El alias '" + registroDTO.getAlias() + "' ya está en uso.");
        }

        if (usuarioRepository.findByCorreoElectronico(registroDTO.getCorreoElectronico()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: Ya existe una cuenta registrada con ese correo electrónico.");
        }

        // Creamos el usuario con la cuenta inactiva hasta que verifique el PIN
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setAlias(registroDTO.getAlias());
        nuevoUsuario.setCorreoElectronico(registroDTO.getCorreoElectronico());
        nuevoUsuario.setPassword(passwordEncoder.encode(registroDTO.getPassword()));
        nuevoUsuario.setRol("user");
        nuevoUsuario.setCuentaActiva(false);
        nuevoUsuario.setFechaRegistro(LocalDateTime.now());
        nuevoUsuario.setIntentosFallidos(0);

        //Generamos un PIN de 6 digitos aleatorio y lo guardamos en el usuario
        String pin = String.format("%06d", new SecureRandom().nextInt(1000000));
        nuevoUsuario.setCodigoVerificacion(pin);

        usuarioRepository.save(nuevoUsuario);

        // Intentamos mandar el PIN por correo, si falla borramos el usuario
        try {
            emailService.enviarCorreoAPI(nuevoUsuario.getCorreoElectronico(), nuevoUsuario.getAlias(), pin);
        } catch (Exception e) {
            usuarioRepository.delete(nuevoUsuario);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al enviar el correo. Inténtalo de nuevo en unos segundos.");
        }

        return ResponseEntity.ok("Registro guardado. Revisa tu email para obtener el PIN (caduca en 5 minutos).");
    }


    // Recibe el PIN que el usuario introduce y activa la cuenta si es correcto

    @PostMapping("/verify-pin")
    public ResponseEntity<String> verificarPin(@RequestBody PinRequestDTO datos) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByAlias(datos.getAlias());

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuario no encontrado.");
        }

        Usuario u = usuarioOpt.get();

        // Si han pasado mas de 5 min desde el registro el PIN caduca y borramos el usuario
        if (u.getFechaRegistro() != null &&
                u.getFechaRegistro().plusMinutes(5).isBefore(LocalDateTime.now())) {
            usuarioRepository.delete(u);
            return ResponseEntity.status(HttpStatus.GONE)
                    .body("El PIN ha caducado. Tu registro ha sido eliminado. Vuelve a registrarte.");
        }

        // Si ya agotó los 3 intentos borramos el usuario por seguridad
        if (u.getIntentosFallidos() >= MAX_INTENTOS_PIN) {
            usuarioRepository.delete(u);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Demasiados intentos fallidos. Tu registro ha sido eliminado por seguridad. Vuelve a registrarte.");
        }

        //Pin correcto, activamos la cuenta y limpiamos los campos temporales
        if (u.getCodigoVerificacion() != null && u.getCodigoVerificacion().equals(datos.getPin())) {
            u.setCuentaActiva(true);
            u.setCodigoVerificacion(null);
            u.setIntentosFallidos(0);
            usuarioRepository.save(u);
            return ResponseEntity.ok("Cuenta verificada correctamente.");
        }

        // Si el PIN es incorrecto sumamos un intento y avisamos cuantos quedan
        u.setIntentosFallidos(u.getIntentosFallidos() + 1);
        int intentosRestantes = MAX_INTENTOS_PIN - u.getIntentosFallidos();
        usuarioRepository.save(u);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("PIN incorrecto. Te quedan " + intentosRestantes + " intento(s).");
    }


    // Se ejecuta cada minuto buscando usuarios sin verificar cuyo registro tenga mas de 5 min y los elimina de la base de datos.
    @Scheduled(fixedRate = 60000)
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