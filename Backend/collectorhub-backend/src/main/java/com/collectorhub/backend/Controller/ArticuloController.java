package com.collectorhub.backend.Controller;

import com.collectorhub.backend.Entidades.Articulo;
import com.collectorhub.backend.Repository.ArticuloRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/articulos")
@CrossOrigin(origins = "http://localhost:5173")
public class ArticuloController {

    @Autowired
    private ArticuloRepository articuloRepository;

    @GetMapping("/categoria/{categoriaId}/usuario/{usuarioId}")
    public List<Articulo> obtenerPorCategoria(@PathVariable Integer categoriaId, @PathVariable Integer usuarioId) {
        return articuloRepository.findByCategoriaIdAndUsuarioId(categoriaId, usuarioId);
    }

    @PostMapping
    public ResponseEntity<Articulo> crearArticulo(@RequestBody Articulo articulo) {
        return ResponseEntity.ok(articuloRepository.save(articulo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Articulo> actualizarArticulo(@PathVariable Integer id, @RequestBody Articulo datosNuevos) {
        return articuloRepository.findById(id)
                .map(art -> {
                    art.setDatos(datosNuevos.getDatos());
                    art.setImagen1(datosNuevos.getImagen1());
                    art.setImagen2(datosNuevos.getImagen2());
                    return ResponseEntity.ok(articuloRepository.save(art));
                }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarArticulo(@PathVariable Integer id) {
        if (articuloRepository.existsById(id)) {
            articuloRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}