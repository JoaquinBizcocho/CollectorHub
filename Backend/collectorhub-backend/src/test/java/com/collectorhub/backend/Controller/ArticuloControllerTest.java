package com.collectorhub.backend.Controller;

import com.collectorhub.backend.security.AuthenticatedUser;
import com.collectorhub.backend.security.JwtUtil;
import com.collectorhub.backend.services.ArticuloService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ArticuloController.class)
public class ArticuloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private ArticuloService articuloService;

    @Test
    public void testObtenerPorCategoria_DebeDevolverLista() throws Exception {

        AuthenticatedUser principal = new AuthenticatedUser(1, "Joaquin", "user");
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        Mockito.when(articuloService.obtenerPorCategoriaYUsuario(1, 1))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/articulos/categoria/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}