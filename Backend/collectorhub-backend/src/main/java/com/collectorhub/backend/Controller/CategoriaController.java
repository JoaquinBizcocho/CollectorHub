package com.collectorhub.backend.Controller;

import com.collectorhub.backend.Entidades.Categoria;
import com.collectorhub.backend.Repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "*")
public class CategoriaController {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @GetMapping("/usuario/{usuarioId}")
    public List<Categoria> obtenerPorUsuario(@PathVariable Integer usuarioId) {
        return categoriaRepository.findByUsuarioId(usuarioId);
    }

    // Obtener solo las plantillas oficiales
    @GetMapping("/oficiales")
    public List<Categoria> obtenerPlantillasOficiales() {
        return categoriaRepository.findByEsOficialTrue();
    }

    @PostMapping
    public ResponseEntity<Categoria> crearCategoria(@RequestBody Categoria categoria) {
        // Si no nos dicen si es oficial, por defecto es false
        if (categoria.getEsOficial() == null) {
            categoria.setEsOficial(false);
        }
        return ResponseEntity.ok(categoriaRepository.save(categoria));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Categoria> actualizarCategoria(@PathVariable Integer id, @RequestBody Categoria categoriaActualizada) {
        return categoriaRepository.findById(id)
                .map(cat -> {
                    cat.setNombre(categoriaActualizada.getNombre());
                    cat.setDescripcion(categoriaActualizada.getDescripcion());
                    cat.setEsquema(categoriaActualizada.getEsquema());
                    return ResponseEntity.ok(categoriaRepository.save(cat));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Integer id) {
        if (categoriaRepository.existsById(id)) {
            categoriaRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}