package com.collectorhub.backend.Controller;

import com.collectorhub.backend.DTO.ArticuloDTO;
import com.collectorhub.backend.Entidades.Articulo;
import com.collectorhub.backend.Repository.ArticuloRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/articulos")
@CrossOrigin(origins = "https://collector-hub-frontend.vercel.app")
public class ArticuloController {

    @Autowired
    private ArticuloRepository articuloRepository;

    @GetMapping("/categoria/{categoriaId}/usuario/{usuarioId}")
    public List<Articulo> obtenerPorCategoria(@PathVariable Integer categoriaId, @PathVariable Integer usuarioId) {
        // En un Get devolver la Entidad directamente es aceptable si la Entidad no tiene campos sensibles
        // (En el caso de Articulo, no hay contraseñas).
        return articuloRepository.findByCategoriaIdAndUsuarioId(categoriaId, usuarioId);
    }

    @PostMapping
    public ResponseEntity<Articulo> crearArticulo(@RequestBody ArticuloDTO dto) {
        Articulo articulo = new Articulo();
        articulo.setCategoriaId(dto.getCategoriaId());
        articulo.setUsuarioId(dto.getUsuarioId());
        articulo.setDatos(dto.getDatos());
        articulo.setImagen1(dto.getImagen1());
        articulo.setImagen2(dto.getImagen2());

        return ResponseEntity.ok(articuloRepository.save(articulo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Articulo> actualizarArticulo(@PathVariable Integer id, @RequestBody ArticuloDTO dto) {
        return articuloRepository.findById(id)
                .map(art -> {
                    // Solo permitimos actualizar los datos y las imagenes, evitamos que cambien el dueño del articulo
                    art.setDatos(dto.getDatos());
                    art.setImagen1(dto.getImagen1());
                    art.setImagen2(dto.getImagen2());
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


//@CrossOrigin(origins = "https://collector-hub-frontend.vercel.app/")
//@CrossOrigin(origins = "*")