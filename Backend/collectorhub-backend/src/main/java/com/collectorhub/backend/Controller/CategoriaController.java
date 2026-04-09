package com.collectorhub.backend.Controller; // Ajusta el paquete

import com.collectorhub.backend.Entidades.Categoria;
import com.collectorhub.backend.Repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
// ¡OJO! Ajusta el puerto del localhost si tu React corre en el 5174 u otro.
@CrossOrigin(origins = "http://localhost:5173")
public class CategoriaController {

    @Autowired
    private CategoriaRepository categoriaRepository;

    // Obtener TODAS las categorías (Para el Dashboard)
    @GetMapping
    public List<Categoria> obtenerTodas() {
        return categoriaRepository.findAll();
    }

    // CREAR una nueva categoría
    @PostMapping
    public ResponseEntity<Categoria> crearCategoria(@RequestBody Categoria categoria) {
        Categoria nuevaCategoria = categoriaRepository.save(categoria);
        return ResponseEntity.ok(nuevaCategoria);
    }

    // Obtener SOLO las categorías del usuario logueado
    @GetMapping("/usuario/{usuarioId}")
    public List<Categoria> obtenerDelUsuario(@PathVariable Integer usuarioId) {
        return categoriaRepository.findByUsuarioId(usuarioId);
    }

    // BORRAR una categoría (y sus objetos en cascada)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarCategoria(@PathVariable Integer id) {
        if (categoriaRepository.existsById(id)) {
            categoriaRepository.deleteById(id);
            return ResponseEntity.ok("Categoría eliminada con éxito");
        }
        return ResponseEntity.badRequest().body("La categoría no existe");
    }

    // Modificar una categoría existente
    @PutMapping("/{id}")
    public ResponseEntity<Categoria> actualizarCategoria(@PathVariable Integer id, @RequestBody Categoria categoriaActualizada) {
        return categoriaRepository.findById(id)
                .map(cat -> {
                    // Actualizamos los datos con lo que nos llega desde React
                    cat.setNombre(categoriaActualizada.getNombre());
                    cat.setDescripcion(categoriaActualizada.getDescripcion());
                    cat.setEsquema(categoriaActualizada.getEsquema());

                    // Guardamos los cambios en la base de datos
                    Categoria categoriaGuardada = categoriaRepository.save(cat);
                    return ResponseEntity.ok(categoriaGuardada);
                })
                .orElse(ResponseEntity.notFound().build()); // Da error 404 si la categoría no existe
    }

}