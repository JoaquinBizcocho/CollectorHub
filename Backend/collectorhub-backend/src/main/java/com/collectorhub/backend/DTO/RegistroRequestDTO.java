package com.collectorhub.backend.DTO;

import lombok.Data;

@Data
public class RegistroRequestDTO {
    private String alias;
    private String password;
    private String correoElectronico;
}