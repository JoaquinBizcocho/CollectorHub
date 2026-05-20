package com.collectorhub.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // Clave secreta definida en application.properties, se usa para firmar y verificar los tokens
    @Value("${jwt.secret}")
    private String secretKey;

    // Convierte la clave secreta en un objeto KEY que entiende la libreria JWT
    private Key getKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // Genera un token JWT con los datos del usuario que expira en 24h
    public String generarToken(Integer usuarioId, String alias, String rol) {
        return Jwts.builder()
                .setSubject(alias)
                .claim("id", usuarioId)
                .claim("rol", rol)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // esto son 24 horas en ms
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Extrae todos los datos que viajan dentro del token
    public Claims extraerClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token).getBody();
    }

    // Valida el token intentando extraer sus claims, si lanza una excepcion no es valido
    public boolean validarToken(String token) {
        try {
            extraerClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}