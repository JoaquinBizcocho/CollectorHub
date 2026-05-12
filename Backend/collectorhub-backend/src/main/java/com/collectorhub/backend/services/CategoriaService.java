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

    public List<Categoria> obtenerPorUsuario(Integer usuarioId) {
        return categoriaRepository.findByUsuarioId(usuarioId);
    }

    public List<Categoria> obtenerOficiales() {
        return categoriaRepository.findByEsOficialTrue();
    }

    public Categoria crearCategoria(CategoriaDTO dto, Integer usuarioId) {
        Categoria categoria = new Categoria();
        categoria.setNombre(dto.getNombre());
        categoria.setDescripcion(dto.getDescripcion());
        categoria.setUsuarioId(usuarioId); // Siempre del token, nunca del body
        categoria.setEsquema(dto.getEsquema());
        categoria.setEsOficial(dto.getEsOficial() != null ? dto.getEsOficial() : false);
        return categoriaRepository.save(categoria);
    }

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

    public void eliminarCategoria(Integer id, Integer usuarioId) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria no encontrada"));

        if (!categoria.getUsuarioId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para eliminar esta categoria");
        }

        categoriaRepository.deleteById(id);
    }
}