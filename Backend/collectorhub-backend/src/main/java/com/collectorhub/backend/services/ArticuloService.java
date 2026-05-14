package com.collectorhub.backend.services;

import com.collectorhub.backend.DTO.ArticuloDTO;
import com.collectorhub.backend.Entidades.Articulo;
import com.collectorhub.backend.Repository.ArticuloRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

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
        articulo.setEstado(dto.getEstado() != null ? dto.getEstado() : "COLECCION");
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

        articulo.setEstado(dto.getEstado() != null ? dto.getEstado() : articulo.getEstado());
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
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", art.getId());
                item.put("estado", art.getEstado());
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
        if (articulos.isEmpty()) return "id,estado,datos\n";

        StringBuilder sb = new StringBuilder();
        Map<String, Object> primerDato = articulos.get(0).getDatos();
        sb.append("id,estado");
        if (primerDato != null) {
            for (String clave : primerDato.keySet()) {
                sb.append(",").append(clave);
            }
        }
        sb.append("\n");

        for (Articulo art : articulos) {
            sb.append(art.getId()).append(",").append(art.getEstado());
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

    public Map<String, Object> importarDesdeJson(Integer categoriaId, Integer usuarioId,
                                                 String jsonContent, boolean sobreescribir) {
        try {
            List<Map<String, Object>> items = objectMapper.readValue(jsonContent, List.class);

            if (items == null || items.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El fichero JSON está vacío o no tiene el formato correcto.");
            }

            Map<String, Object> primero = items.get(0);
            if (!primero.containsKey("datos")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El fichero JSON no tiene el formato correcto. Asegúrate de que fue exportado desde CollectorHub.");
            }

            List<Articulo> existentes = articuloRepository.findByCategoriaIdAndUsuarioId(categoriaId, usuarioId);
            Set<Integer> idsExistentes = new HashSet<>();
            for (Articulo a : existentes) idsExistentes.add(a.getId());

            List<Integer> conflictos = new ArrayList<>();
            for (Map<String, Object> item : items) {
                Object idObj = item.get("id");
                if (idObj != null) {
                    Integer id = ((Number) idObj).intValue();
                    if (idsExistentes.contains(id)) conflictos.add(id);
                }
            }

            if (!conflictos.isEmpty() && !sobreescribir) {
                Map<String, Object> respuesta = new LinkedHashMap<>();
                respuesta.put("conflictos", conflictos.size());
                respuesta.put("requiereConfirmacion", true);
                return respuesta;
            }

            int creados = 0, actualizados = 0;
            for (Map<String, Object> item : items) {
                Object idObj = item.get("id");
                Integer id = idObj != null ? ((Number) idObj).intValue() : null;
                String estado = item.get("estado") != null ? item.get("estado").toString() : "COLECCION";
                Map<String, Object> datos = (Map<String, Object>) item.get("datos");

                if (id != null && idsExistentes.contains(id) && sobreescribir) {
                    Articulo art = articuloRepository.findById(id).orElse(null);
                    if (art != null && art.getUsuarioId().equals(usuarioId)) {
                        art.setEstado(estado);
                        art.setDatos(datos);
                        articuloRepository.save(art);
                        actualizados++;
                    }
                } else if (id == null || !idsExistentes.contains(id)) {
                    Articulo nuevo = new Articulo();
                    nuevo.setCategoriaId(categoriaId);
                    nuevo.setUsuarioId(usuarioId);
                    nuevo.setEstado(estado);
                    nuevo.setDatos(datos);
                    articuloRepository.save(nuevo);
                    creados++;
                }
            }

            Map<String, Object> respuesta = new LinkedHashMap<>();
            respuesta.put("creados", creados);
            respuesta.put("actualizados", actualizados);
            respuesta.put("requiereConfirmacion", false);
            return respuesta;

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El fichero no tiene un formato JSON válido.");
        }
    }

    public Map<String, Object> importarDesdeCsv(Integer categoriaId, Integer usuarioId,
                                                String csvContent, boolean sobreescribir) {
        try {
            String[] lineas = csvContent.split("\n");
            if (lineas.length < 2) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El fichero CSV está vacío o no tiene datos.");
            }

            String[] cabeceras = lineas[0].trim().split(",");

            boolean tieneId = Arrays.stream(cabeceras).anyMatch(c -> c.trim().equals("id"));
            boolean tieneEstado = Arrays.stream(cabeceras).anyMatch(c -> c.trim().equals("estado"));

            if (!tieneId || !tieneEstado) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El fichero CSV no tiene el formato correcto. Asegúrate de que fue exportado desde CollectorHub.");
            }

            List<Articulo> existentes = articuloRepository.findByCategoriaIdAndUsuarioId(categoriaId, usuarioId);
            Set<Integer> idsExistentes = new HashSet<>();
            for (Articulo a : existentes) idsExistentes.add(a.getId());

            List<Integer> conflictos = new ArrayList<>();
            List<String[]> filas = new ArrayList<>();

            for (int i = 1; i < lineas.length; i++) {
                if (lineas[i].trim().isEmpty()) continue;
                String[] cols = lineas[i].trim().split(",", -1);
                filas.add(cols);
                if (cols.length > 0 && !cols[0].isEmpty()) {
                    try {
                        Integer id = Integer.parseInt(cols[0].trim());
                        if (idsExistentes.contains(id)) conflictos.add(id);
                    } catch (NumberFormatException ignored) {}
                }
            }

            if (!conflictos.isEmpty() && !sobreescribir) {
                Map<String, Object> respuesta = new LinkedHashMap<>();
                respuesta.put("conflictos", conflictos.size());
                respuesta.put("requiereConfirmacion", true);
                return respuesta;
            }

            int creados = 0, actualizados = 0;
            for (String[] cols : filas) {
                Integer id = null;
                try { id = Integer.parseInt(cols[0].trim()); } catch (NumberFormatException ignored) {}
                String estado = cols.length > 1 ? cols[1].trim() : "COLECCION";

                Map<String, Object> datos = new LinkedHashMap<>();
                for (int c = 2; c < cabeceras.length && c < cols.length; c++) {
                    datos.put(cabeceras[c].trim(), cols[c].trim());
                }

                if (id != null && idsExistentes.contains(id) && sobreescribir) {
                    Articulo art = articuloRepository.findById(id).orElse(null);
                    if (art != null && art.getUsuarioId().equals(usuarioId)) {
                        art.setEstado(estado);
                        art.setDatos(datos);
                        articuloRepository.save(art);
                        actualizados++;
                    }
                } else {
                    Articulo nuevo = new Articulo();
                    nuevo.setCategoriaId(categoriaId);
                    nuevo.setUsuarioId(usuarioId);
                    nuevo.setEstado(estado);
                    nuevo.setDatos(datos);
                    articuloRepository.save(nuevo);
                    creados++;
                }
            }

            Map<String, Object> respuesta = new LinkedHashMap<>();
            respuesta.put("creados", creados);
            respuesta.put("actualizados", actualizados);
            respuesta.put("requiereConfirmacion", false);
            return respuesta;

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El fichero no tiene un formato CSV válido.");
        }
    }
}