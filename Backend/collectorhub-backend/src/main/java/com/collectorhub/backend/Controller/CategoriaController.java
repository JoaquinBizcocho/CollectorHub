package com.collectorhub.backend.Controller;

import com.collectorhub.backend.DTO.CategoriaDTO;
import com.collectorhub.backend.Entidades.Categoria;
import com.collectorhub.backend.Repository.CategoriaRepository;
import com.collectorhub.backend.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "https://collector-hub-frontend.vercel.app")
public class CategoriaController {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @GetMapping("/usuario")
    public ResponseEntity<List<Categoria>> obtenerPorUsuario(Authentication authentication) {
        AuthenticatedUser usuario = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(categoriaRepository.findByUsuarioId(usuario.getId()));
    }

    @GetMapping("/oficiales")
    public List<Categoria> obtenerPlantillasOficiales() {
        return categoriaRepository.findByEsOficialTrue();
    }

    @PostMapping
    public ResponseEntity<Categoria> crearCategoria(@Valid @RequestBody CategoriaDTO dto, Authentication authentication) {
        AuthenticatedUser usuario = (AuthenticatedUser) authentication.getPrincipal();

        Categoria categoria = new Categoria();
        categoria.setNombre(dto.getNombre());
        categoria.setDescripcion(dto.getDescripcion());
        categoria.setUsuarioId(usuario.getId()); // Ignoramos el usuarioId del body
        categoria.setEsquema(dto.getEsquema());
        categoria.setEsOficial(dto.getEsOficial() != null ? dto.getEsOficial() : false);

        return ResponseEntity.ok(categoriaRepository.save(categoria));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarCategoria(
            @PathVariable Integer id,
            @Valid @RequestBody CategoriaDTO dto,
            Authentication authentication) {

        AuthenticatedUser usuario = (AuthenticatedUser) authentication.getPrincipal();

        return categoriaRepository.findById(id)
                .map(cat -> {
                    if (!cat.getUsuarioId().equals(usuario.getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<Categoria>build();
                    }
                    cat.setNombre(dto.getNombre());
                    cat.setDescripcion(dto.getDescripcion());
                    cat.setEsquema(dto.getEsquema());
                    return ResponseEntity.ok(categoriaRepository.save(cat));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Integer id, Authentication authentication) {
        AuthenticatedUser usuario = (AuthenticatedUser) authentication.getPrincipal();

        var categoriaOpt = categoriaRepository.findById(id);

        if (categoriaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Categoria cat = categoriaOpt.get();

        if (!cat.getUsuarioId().equals(usuario.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        categoriaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}