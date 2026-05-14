package com.collectorhub.backend.Controller;

import com.collectorhub.backend.DTO.ArticuloDTO;
import com.collectorhub.backend.Entidades.Articulo;
import com.collectorhub.backend.security.AuthenticatedUser;
import com.collectorhub.backend.services.ArticuloService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/categoria/{categoriaId}/exportar/json")
    public ResponseEntity<String> exportarJson(
            @PathVariable Integer categoriaId,
            Authentication authentication) {
        AuthenticatedUser usuario = (AuthenticatedUser) authentication.getPrincipal();
        String json = articuloService.exportarComoJson(categoriaId, usuario.getId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"coleccion_" + categoriaId + ".json\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);
    }

    @GetMapping("/categoria/{categoriaId}/exportar/csv")
    public ResponseEntity<String> exportarCsv(
            @PathVariable Integer categoriaId,
            Authentication authentication) {
        AuthenticatedUser usuario = (AuthenticatedUser) authentication.getPrincipal();
        String csv = articuloService.exportarComoCsv(categoriaId, usuario.getId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"coleccion_" + categoriaId + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @PostMapping("/categoria/{categoriaId}/importar/json")
    public ResponseEntity<Map<String, Object>> importarJson(
            @PathVariable Integer categoriaId,
            @RequestParam(defaultValue = "false") boolean sobreescribir,
            @RequestBody String jsonContent,
            Authentication authentication) {
        AuthenticatedUser usuario = (AuthenticatedUser) authentication.getPrincipal();
        Map<String, Object> resultado = articuloService.importarDesdeJson(
                categoriaId, usuario.getId(), jsonContent, sobreescribir);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/categoria/{categoriaId}/importar/csv")
    public ResponseEntity<Map<String, Object>> importarCsv(
            @PathVariable Integer categoriaId,
            @RequestParam(defaultValue = "false") boolean sobreescribir,
            @RequestBody String csvContent,
            Authentication authentication) {
        AuthenticatedUser usuario = (AuthenticatedUser) authentication.getPrincipal();
        Map<String, Object> resultado = articuloService.importarDesdeCsv(
                categoriaId, usuario.getId(), csvContent, sobreescribir);
        return ResponseEntity.ok(resultado);
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