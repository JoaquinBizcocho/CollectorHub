package com.collectorhub.backend.DTO;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String alias;
    private String password;
}