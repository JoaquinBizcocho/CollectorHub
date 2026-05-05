package com.collectorhub.backend.Controller;

import com.collectorhub.backend.Repository.ArticuloRepository;
import com.collectorhub.backend.Repository.CategoriaRepository;
import com.collectorhub.backend.Repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.collectorhub.backend.security.JwtUtil;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false) // Desactiva seguridad solo para el test
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc; // Esto simula ser Postman o el navegador

    // MockBean crea repositorios "de mentira" (dobles de acción)
    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CategoriaRepository categoriaRepository;

    @MockitoBean
    private ArticuloRepository articuloRepository;

    @Test
    public void testObtenerEstadisticas_DebeDevolverValoresCorrectos() throws Exception {

        Mockito.when(usuarioRepository.count()).thenReturn(15L);
        Mockito.when(categoriaRepository.count()).thenReturn(5L);
        Mockito.when(articuloRepository.count()).thenReturn(50L);

        mockMvc.perform(get("/api/admin/estadisticas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsuarios").value(15))
                .andExpect(jsonPath("$.totalCategorias").value(5))
                .andExpect(jsonPath("$.totalArticulos").value(50));
    }
}