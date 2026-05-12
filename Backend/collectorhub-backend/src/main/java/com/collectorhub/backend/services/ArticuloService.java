package com.collectorhub.backend.services;

import com.collectorhub.backend.DTO.ArticuloDTO;
import com.collectorhub.backend.Entidades.Articulo;
import com.collectorhub.backend.Repository.ArticuloRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ArticuloService {

    @Autowired
    private ArticuloRepository articuloRepository;

    public List<Articulo> obtenerPorCategoriaYUsuario(Integer categoriaId, Integer usuarioId) {
        return articuloRepository.findByCategoriaIdAndUsuarioId(categoriaId, usuarioId);
    }

    public Articulo crearArticulo(ArticuloDTO dto, Integer usuarioId) {
        Articulo articulo = new Articulo();
        articulo.setCategoriaId(dto.getCategoriaId());
        articulo.setUsuarioId(usuarioId); // Siempre del token, nunca del body
        articulo.setDatos(dto.getDatos());
        articulo.setImagen1(dto.getImagen1());
        articulo.setImagen2(dto.getImagen2());
        return articuloRepository.save(articulo);
    }

    public Articulo actualizarArticulo(Integer id, ArticuloDTO dto, Integer usuarioId) {
        Articulo articulo = articuloRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Articulo no encontrado"));

        if (!articulo.getUsuarioId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para editar este articulo");
        }

        articulo.setDatos(dto.getDatos());
        articulo.setImagen1(dto.getImagen1());
        articulo.setImagen2(dto.getImagen2());
        return articuloRepository.save(articulo);
    }

    public void eliminarArticulo(Integer id, Integer usuarioId) {
        Articulo articulo = articuloRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Articulo no encontrado"));

        if (!articulo.getUsuarioId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para eliminar este articulo");
        }

        articuloRepository.deleteById(id);
    }
}