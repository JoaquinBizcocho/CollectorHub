package com.collectorhub.backend.Controller;

import com.collectorhub.backend.DTO.ArticuloDTO;
import com.collectorhub.backend.Entidades.Articulo;
import com.collectorhub.backend.security.AuthenticatedUser;
import com.collectorhub.backend.services.ArticuloService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articulos")
@CrossOrigin(origins = "https://collector-hub-frontend.vercel.app")
public class ArticuloController {

    @Autowired
    private ArticuloService articuloService;

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<Articulo>> obtenerPorCategoria(
            @PathVariable Integer categoriaId,
            Authentication authentication) {
        AuthenticatedUser usuario = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(articuloService.obtenerPorCategoriaYUsuario(categoriaId, usuario.getId()));
    }

    @PostMapping
    public ResponseEntity<Articulo> crearArticulo(@Valid @RequestBody ArticuloDTO dto, Authentication authentication) {
        AuthenticatedUser usuario = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(articuloService.crearArticulo(dto, usuario.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Articulo> actualizarArticulo(
            @PathVariable Integer id,
            @Valid @RequestBody ArticuloDTO dto,
            Authentication authentication) {
        AuthenticatedUser usuario = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(articuloService.actualizarArticulo(id, dto, usuario.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarArticulo(@PathVariable Integer id, Authentication authentication) {
        AuthenticatedUser usuario = (AuthenticatedUser) authentication.getPrincipal();
        articuloService.eliminarArticulo(id, usuario.getId());
        return ResponseEntity.noContent().build();
    }
}