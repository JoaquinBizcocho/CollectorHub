package com.collectorhub.backend.DTO;

import lombok.Data;

@Data
public class AuthResponseDTO {
    private String mensaje;
    private Long usuarioId;
    private String rol;
    // JWT que el frontend adjunta en cada peticion autenticada
    private String token;
}