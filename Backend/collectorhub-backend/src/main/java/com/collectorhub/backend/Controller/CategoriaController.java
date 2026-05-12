package com.collectorhub.backend.Controller;

import com.collectorhub.backend.DTO.CategoriaDTO;
import com.collectorhub.backend.Entidades.Categoria;
import com.collectorhub.backend.security.AuthenticatedUser;
import com.collectorhub.backend.services.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "https://collector-hub-frontend.vercel.app")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping("/usuario")
    public ResponseEntity<List<Categoria>> obtenerPorUsuario(Authentication authentication) {
        AuthenticatedUser usuario = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(categoriaService.obtenerPorUsuario(usuario.getId()));
    }

    @GetMapping("/oficiales")
    public List<Categoria> obtenerPlantillasOficiales() {
        return categoriaService.obtenerOficiales();
    }

    @PostMapping
    public ResponseEntity<Categoria> crearCategoria(@Valid @RequestBody CategoriaDTO dto, Authentication authentication) {
        AuthenticatedUser usuario = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(categoriaService.crearCategoria(dto, usuario.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Categoria> actualizarCategoria(
            @PathVariable Integer id,
            @Valid @RequestBody CategoriaDTO dto,
            Authentication authentication) {
        AuthenticatedUser usuario = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(categoriaService.actualizarCategoria(id, dto, usuario.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Integer id, Authentication authentication) {
        AuthenticatedUser usuario = (AuthenticatedUser) authentication.getPrincipal();
        categoriaService.eliminarCategoria(id, usuario.getId());
        return ResponseEntity.noContent().build();
    }
}