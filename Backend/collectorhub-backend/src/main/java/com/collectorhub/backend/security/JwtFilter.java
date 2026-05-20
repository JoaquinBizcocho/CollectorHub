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

    // Se ejecuta una vez por cada peticion antes de llegar al controller
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Quitamos el prefijo "Bearer" para quedanos solo con el token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtil.validarToken(token)) {
                // Extraemos los datos del usuario que viajan dentro del token
                Claims claims = jwtUtil.extraerClaims(token);
                String alias = claims.getSubject();
                String rol = claims.get("rol", String.class);
                Integer usuarioId = claims.get("id", Integer.class);

                // Spring Security necesita el rol con el prefijo ROLE_ para los permisos
                String roleGranted = (rol != null && rol.contains("admin")) ? "ROLE_ADMIN" : "ROLE_USER";

                // construimos el principal con los datos del usuario y lo metemos en el contexto
                AuthenticatedUser principal = new AuthenticatedUser(usuarioId, alias, rol);

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        principal, null, Collections.singletonList(new SimpleGrantedAuthority(roleGranted)));

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(request, response);
    }
}