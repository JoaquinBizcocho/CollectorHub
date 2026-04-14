package com.collectorhub.backend.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Quitamos la palabra "Bearer "

            if (jwtUtil.validarToken(token)) {
                Claims claims = jwtUtil.extraerClaims(token);
                String alias = claims.getSubject();
                String rol = claims.get("rol", String.class);

                // Convertimos tu rol de BD en el formato que Spring Security entiende
                String roleGranted = (rol != null && rol.contains("admin")) ? "ROLE_ADMIN" : "ROLE_USER";

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        alias, null, Collections.singletonList(new SimpleGrantedAuthority(roleGranted)));

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(request, response);
    }
}