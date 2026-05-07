package com.collectorhub.backend.Controller;

import com.collectorhub.backend.DTO.CategoriaDTO;
import com.collectorhub.backend.Entidades.Categoria;
import com.collectorhub.backend.Repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "https://collector-hub-frontend.vercel.app/")
public class CategoriaController {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @GetMapping("/usuario/{usuarioId}")
    public List<Categoria> obtenerPorUsuario(@PathVariable Integer usuarioId) {
        return categoriaRepository.findByUsuarioId(usuarioId);
    }

    @GetMapping("/oficiales")
    public List<Categoria> obtenerPlantillasOficiales() {
        return categoriaRepository.findByEsOficialTrue();
    }

    @PostMapping
    public ResponseEntity<Categoria> crearCategoria(@RequestBody CategoriaDTO dto) {
        Categoria categoria = new Categoria();
        categoria.setNombre(dto.getNombre());
        categoria.setDescripcion(dto.getDescripcion());
        categoria.setUsuarioId(dto.getUsuarioId());
        categoria.setEsquema(dto.getEsquema());
        categoria.setEsOficial(dto.getEsOficial() != null ? dto.getEsOficial() : false);

        return ResponseEntity.ok(categoriaRepository.save(categoria));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Categoria> actualizarCategoria(@PathVariable Integer id, @RequestBody CategoriaDTO dto) {
        return categoriaRepository.findById(id)
                .map(cat -> {
                    // Protegemos el ID y el Creador (usuarioId) para que no puedan ser modificados en un PUT
                    cat.setNombre(dto.getNombre());
                    cat.setDescripcion(dto.getDescripcion());
                    cat.setEsquema(dto.getEsquema());
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
