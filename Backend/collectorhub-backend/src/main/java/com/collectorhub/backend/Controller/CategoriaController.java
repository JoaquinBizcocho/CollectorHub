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

    // Devuelve todas las categorías que pertenecen al usuario autenticado
    @GetMapping("/usuario")
    public ResponseEntity<List<Categoria>> obtenerPorUsuario(Authentication authentication) {
        // Extraemos el id del usuario del token para pasárselo al service
        AuthenticatedUser usuario = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(categoriaService.obtenerPorUsuario(usuario.getId()));
    }

    // Devuelve las plantillas oficiales creadas por el admin, cualquier usuario autenticado puede verlas
    @GetMapping("/oficiales")
    public List<Categoria> obtenerPlantillasOficiales() {
        return categoriaService.obtenerOficiales();
    }

    // Crea una categoría nueva vinculada al usuario autenticado
    @PostMapping
    public ResponseEntity<Categoria> crearCategoria(@Valid @RequestBody CategoriaDTO dto, Authentication authentication) {
        // Le pasamos el id del usuario al service para que la categoría quede asociada a él
        AuthenticatedUser usuario = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(categoriaService.crearCategoria(dto, usuario.getId()));
    }

    // Actualiza los datos de una categoría existente
    @PutMapping("/{id}")
    public ResponseEntity<Categoria> actualizarCategoria(
            @PathVariable Integer id,
            @Valid @RequestBody CategoriaDTO dto,
            Authentication authentication) {
        // El service comprueba que la categoría le pertenezca al usuario antes de modificarla
        AuthenticatedUser usuario = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(categoriaService.actualizarCategoria(id, dto, usuario.getId()));
    }

    // Elimina una categoría por id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Integer id, Authentication authentication) {
        // El service verifica que sea del usuario antes de borrarla
        AuthenticatedUser usuario = (AuthenticatedUser) authentication.getPrincipal();
        categoriaService.eliminarCategoria(id, usuario.getId());
        return ResponseEntity.noContent().build();
    }
}