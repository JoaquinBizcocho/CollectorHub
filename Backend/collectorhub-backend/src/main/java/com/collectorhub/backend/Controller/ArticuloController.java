package com.collectorhub.backend.Controller;

import com.collectorhub.backend.DTO.ArticuloDTO;
import com.collectorhub.backend.Entidades.Articulo;
import com.collectorhub.backend.Repository.ArticuloRepository;
import com.collectorhub.backend.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articulos")
@CrossOrigin(origins = "https://collector-hub-frontend.vercel.app")
public class ArticuloController {

    @Autowired
    private ArticuloRepository articuloRepository;

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<Articulo>> obtenerPorCategoria(
            @PathVariable Integer categoriaId,
            Authentication authentication) {

        AuthenticatedUser usuario = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(
                articuloRepository.findByCategoriaIdAndUsuarioId(categoriaId, usuario.getId())
        );
    }

    @PostMapping
    public ResponseEntity<Articulo> crearArticulo(@Valid @RequestBody ArticuloDTO dto, Authentication authentication) {
        AuthenticatedUser usuario = (AuthenticatedUser) authentication.getPrincipal();

        Articulo articulo = new Articulo();
        articulo.setCategoriaId(dto.getCategoriaId());
        articulo.setUsuarioId(usuario.getId());
        articulo.setDatos(dto.getDatos());
        articulo.setImagen1(dto.getImagen1());
        articulo.setImagen2(dto.getImagen2());

        return ResponseEntity.ok(articuloRepository.save(articulo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarArticulo(
            @PathVariable Integer id,
            @Valid @RequestBody ArticuloDTO dto,
            Authentication authentication) {

        AuthenticatedUser usuario = (AuthenticatedUser) authentication.getPrincipal();

        return articuloRepository.findById(id)
                .map(art -> {
                    // Verificamos que el artículo pertenece al usuario autenticado
                    if (!art.getUsuarioId().equals(usuario.getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<Articulo>build();
                    }
                    art.setDatos(dto.getDatos());
                    art.setImagen1(dto.getImagen1());
                    art.setImagen2(dto.getImagen2());
                    return ResponseEntity.ok(articuloRepository.save(art));
                }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarArticulo(@PathVariable Integer id, Authentication authentication) {
        AuthenticatedUser usuario = (AuthenticatedUser) authentication.getPrincipal();

        var articuloOpt = articuloRepository.findById(id);

        if (articuloOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Articulo art = articuloOpt.get();

        if (!art.getUsuarioId().equals(usuario.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        articuloRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}