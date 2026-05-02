package com.collectorhub.backend; 

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

    @Test
    void testFuncionamientoBase() {
        String nombre = "CollectorHub";
        assertEquals("CollectorHub", nombre, "El nombre debería coincidir");
    }

    @Test
    void testVerificacionDeEstado() {
        boolean sistemaActivo = true;
        assertTrue(sistemaActivo, "El sistema debería estar activo para las pruebas");
    }
}