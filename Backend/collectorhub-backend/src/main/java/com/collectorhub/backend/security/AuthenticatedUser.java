package com.collectorhub.backend.security;

public class AuthenticatedUser {
    private final Integer id;
    private final String alias;
    private final String rol;

    public AuthenticatedUser(Integer id, String alias, String rol) {
        this.id = id;
        this.alias = alias;
        this.rol = rol;
    }

    public Integer getId() { return id; }
    public String getAlias() { return alias; }
    public String getRol() { return rol; }
}