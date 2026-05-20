package com.collectorhub.backend.services;

import com.collectorhub.backend.DTO.CategoriaDTO;
import com.collectorhub.backend.Entidades.Categoria;
import com.collectorhub.backend.Repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    // Devuelve todas las categorias que pertenecen al usuario
    public List<Categoria> obtenerPorUsuario(Integer usuarioId) {
        return categoriaRepository.findByUsuarioId(usuarioId);
    }

    // Devuelve las plantillas oficiales creadas por un admin, visibles para todos los usuarios
    public List<Categoria> obtenerOficiales() {
        return categoriaRepository.findByEsOficialTrue();
    }

    // Crea una categoria nueva vinculada al usuario, si no viene esOficial en el DTO se pone false por defecto
    public Categoria crearCategoria(CategoriaDTO dto, Integer usuarioId) {
        Categoria categoria = new Categoria();
        categoria.setNombre(dto.getNombre());
        categoria.setDescripcion(dto.getDescripcion());
        categoria.setUsuarioId(usuarioId);
        categoria.setEsquema(dto.getEsquema());
        categoria.setEsOficial(dto.getEsOficial() != null ? dto.getEsOficial() : false);
        return categoriaRepository.save(categoria);
    }

    // Actualiza una categoria existente comprobando que pertenezca al usuario antes de modificarla
    public Categoria actualizarCategoria(Integer id, CategoriaDTO dto, Integer usuarioId) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria no encontrada"));

        if (!categoria.getUsuarioId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para editar esta categoria");
        }

        categoria.setNombre(dto.getNombre());
        categoria.setDescripcion(dto.getDescripcion());
        categoria.setEsquema(dto.getEsquema());
        return categoriaRepository.save(categoria);
    }

    // Elimina una categoria comprobando que pertenezca al usuario antes de borrarla
    public void eliminarCategoria(Integer id, Integer usuarioId) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria no encontrada"));

        if (!categoria.getUsuarioId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para eliminar esta categoria");
        }

        categoriaRepository.deleteById(id);
    }
}