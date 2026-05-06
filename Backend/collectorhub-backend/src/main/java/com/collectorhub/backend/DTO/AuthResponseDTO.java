package com.collectorhub.backend.DTO;

import lombok.Data;

@Data
public class AuthResponseDTO {
    private String mensaje;
    private Long usuarioId;
    private String rol;
    private String token;
}