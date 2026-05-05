package com.collectorhub.backend.Controller;

import com.collectorhub.backend.Entidades.Categoria;
import com.collectorhub.backend.Repository.CategoriaRepository;
import com.collectorhub.backend.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoriaController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CategoriaRepository categoriaRepository;

    @Test
    public void testCrearCategoria_AsignaNoOficialPorDefecto() throws Exception {

        Categoria categoriaGuardada = new Categoria();
        categoriaGuardada.setId(1);
        categoriaGuardada.setNombre("Monedas");
        categoriaGuardada.setEsOficial(false);

        Mockito.when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoriaGuardada);

        String jsonPeticion = "{\"nombre\": \"Monedas\", \"descripcion\": \"Coleccion antigua\"}";

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPeticion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Monedas"))
                .andExpect(jsonPath("$.esOficial").value(false));
    }
}