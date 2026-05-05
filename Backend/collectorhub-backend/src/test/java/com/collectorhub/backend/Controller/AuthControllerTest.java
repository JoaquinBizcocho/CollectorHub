package com.collectorhub.backend.Controller;

import com.collectorhub.backend.Entidades.Usuario;
import com.collectorhub.backend.Repository.UsuarioRepository;
import com.collectorhub.backend.security.JwtUtil;
import com.collectorhub.backend.services.EmailService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    public void testLogin_CuentaInactiva_DebeDevolver403() throws Exception {
        Usuario usuarioFalso = new Usuario();
        usuarioFalso.setAlias("Joaquin");
        usuarioFalso.setPassword("claveEncriptada");
        usuarioFalso.setCuentaActiva(false);

        Mockito.when(usuarioRepository.findByAlias("Joaquin"))
                .thenReturn(Optional.of(usuarioFalso));
        Mockito.when(passwordEncoder.matches("1234", "claveEncriptada"))
                .thenReturn(true);

        String bodyJson = "{\"alias\":\"Joaquin\", \"password\":\"1234\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("Cuenta inactiva. Verifica tu correo electronico primero."));
    }
}