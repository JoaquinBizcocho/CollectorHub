package com.collectorhub.backend.security;

import lombok.Getter;

public class AuthenticatedUser {
    @Getter
    private final Integer id;
    private final String alias;
    private final String rol;

    // se construye al validar el token JWT con los datos extraidos de el
    public AuthenticatedUser(Integer id, String alias, String rol) {
        this.id = id;
        this.alias = alias;
        this.rol = rol;
    }

}