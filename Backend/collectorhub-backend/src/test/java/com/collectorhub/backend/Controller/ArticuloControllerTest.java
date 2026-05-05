package com.collectorhub.backend.Controller;

import com.collectorhub.backend.Repository.ArticuloRepository;
import com.collectorhub.backend.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ArticuloController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ArticuloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private ArticuloRepository articuloRepository;

    @Test
    public void testObtenerPorCategoria_DebeDevolverLista() throws Exception {

        Mockito.when(articuloRepository.findByCategoriaIdAndUsuarioId(1, 1))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/articulos/categoria/1/usuario/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}