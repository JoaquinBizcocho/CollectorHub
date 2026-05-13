package com.collectorhub.backend.services;

import com.collectorhub.backend.DTO.ArticuloDTO;
import com.collectorhub.backend.Entidades.Articulo;
import com.collectorhub.backend.Repository.ArticuloRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
public class ArticuloService {

    @Autowired
    private ArticuloRepository articuloRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public List<Articulo> obtenerPorCategoriaYUsuario(Integer categoriaId, Integer usuarioId) {
        return articuloRepository.findByCategoriaIdAndUsuarioId(categoriaId, usuarioId);
    }

    public Articulo crearArticulo(ArticuloDTO dto, Integer usuarioId) {
        Articulo articulo = new Articulo();
        articulo.setCategoriaId(dto.getCategoriaId());
        articulo.setUsuarioId(usuarioId);
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

    public String exportarComoJson(Integer categoriaId, Integer usuarioId) {
        List<Articulo> articulos = articuloRepository.findByCategoriaIdAndUsuarioId(categoriaId, usuarioId);
        try {
            List<Map<String, Object>> exportar = articulos.stream().map(art -> {
                Map<String, Object> item = new java.util.LinkedHashMap<>();
                item.put("id", art.getId());
                item.put("datos", art.getDatos());
                return item;
            }).toList();
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(exportar);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al generar JSON");
        }
    }

    public String exportarComoCsv(Integer categoriaId, Integer usuarioId) {
        List<Articulo> articulos = articuloRepository.findByCategoriaIdAndUsuarioId(categoriaId, usuarioId);
        if (articulos.isEmpty()) return "id,datos\n";

        StringBuilder sb = new StringBuilder();

        // Cabecera: sacamos las claves del primer artículo
        Map<String, Object> primerDato = articulos.get(0).getDatos();
        sb.append("id");
        if (primerDato != null) {
            for (String clave : primerDato.keySet()) {
                sb.append(",").append(clave);
            }
        }
        sb.append("\n");

        // Filas
        for (Articulo art : articulos) {
            sb.append(art.getId());
            if (art.getDatos() != null) {
                for (Object valor : art.getDatos().values()) {
                    String v = valor == null ? "" : valor.toString().replace(",", ";");
                    sb.append(",").append(v);
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}