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

    // Libreria de Jackson para serializar o deserializar JSON
    @Autowired
    private ObjectMapper objectMapper;

    //Devuelve todos los articulos de una categoria concreta que pertenecen al usuario
    public List<Articulo> obtenerPorCategoriaYUsuario(Integer categoriaId, Integer usuarioId) {
        return articuloRepository.findByCategoriaIdAndUsuarioId(categoriaId, usuarioId);
    }

    // Crea un articulo nuevo vinculado al usuario autenticado
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

    // Actualiza un articulo existente comprobando que pertenezca al usuario antes de modificarlo
    public Articulo actualizarArticulo(Integer id, ArticuloDTO dto, Integer usuarioId) {
        Articulo articulo = articuloRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Articulo no encontrado"));

        if (!articulo.getUsuarioId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para editar este articulo");
        }

        // Si el DTO no trae estado, mantenemos el que ya tenia
        articulo.setEstado(dto.getEstado() != null ? dto.getEstado() : articulo.getEstado());
        articulo.setDatos(dto.getDatos());
        articulo.setImagen1(dto.getImagen1());
        articulo.setImagen2(dto.getImagen2());
        return articuloRepository.save(articulo);
    }

    // Elimina un articulo comprobando que pertenezca al usuario antes de borrarlo
    public void eliminarArticulo(Integer id, Integer usuarioId) {
        Articulo articulo = articuloRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Articulo no encontrado"));

        if (!articulo.getUsuarioId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para eliminar este articulo");
        }

        articuloRepository.deleteById(id);
    }

    // Exporta todos los articulos de una categoria como JSON formateado
    public String exportarComoJson(Integer categoriaId, Integer usuarioId) {
        List<Articulo> articulos = articuloRepository.findByCategoriaIdAndUsuarioId(categoriaId, usuarioId);
        try {
            // Construimos una lista con solo los campos que queremos exportar
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

    // Exporta todos los articulos de una categoria como CSV
    public String exportarComoCsv(Integer categoriaId, Integer usuarioId) {
        List<Articulo> articulos = articuloRepository.findByCategoriaIdAndUsuarioId(categoriaId, usuarioId);
        if (articulos.isEmpty()) return "id,estado,datos\n";

        StringBuilder sb = new StringBuilder();

        // Sacamos las columnas del primer articulo para construir la cabecera del CSV
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
                    // Las comas dentro de los valores se reemplazan por ; para no romper el CSV
                    String v = valor == null ? "" : valor.toString().replace(",", ";");
                    sb.append(",").append(v);
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    // Importa articulos desde un JSON exportado previamente desde La aplicacion
    // Si hay conflictos de id y sobreescribir es false, devuelve cuantos hay para que el frontend pregunte al usuario
    public Map<String, Object> importarDesdeJson(Integer categoriaId, Integer usuarioId,
                                                 String jsonContent, boolean sobreescribir) {
        try {
            List<Map<String, Object>> items = objectMapper.readValue(jsonContent, List.class);

            if (items == null || items.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El fichero JSON está vacío o no tiene el formato correcto.");
            }

            // Validamos que el JSON venga de la aplicacion comprobando que tenga el campo "datos"
            Map<String, Object> primero = items.get(0);
            if (!primero.containsKey("datos")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El fichero JSON no tiene el formato correcto. Asegúrate de que fue exportado desde CollectorHub.");
            }

            // Cargamos los ids existentes del usuario para detectar conflictos
            List<Articulo> existentes = articuloRepository.findByCategoriaIdAndUsuarioId(categoriaId, usuarioId);
            Set<Integer> idsExistentes = new HashSet<>();
            for (Articulo a : existentes) idsExistentes.add(a.getId());

            // Buscamos que ids del fichero ya existen en la categoria
            List<Integer> conflictos = new ArrayList<>();
            for (Map<String, Object> item : items) {
                Object idObj = item.get("id");
                if (idObj != null) {
                    Integer id = ((Number) idObj).intValue();
                    if (idsExistentes.contains(id)) conflictos.add(id);
                }
            }

            // Si hay conflictos y no se ha confirmado sobreescribir, avisamos el frontend
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
                    // El articulo ya existe y el usuario confirmo sobreescribir
                    Articulo art = articuloRepository.findById(id).orElse(null);
                    if (art != null && art.getUsuarioId().equals(usuarioId)) {
                        art.setEstado(estado);
                        art.setDatos(datos);
                        articuloRepository.save(art);
                        actualizados++;
                    }
                } else if (id == null || !idsExistentes.contains(id)) {
                    // Si el articulo no existe lo creamos de nuevo
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

    // Igual que importarDesdeJson pero procesando el contenido en formato CSV
    // Misma logica de conflictos, si sobreescribir es false y hay ids duplicados, avisa al frontend
    public Map<String, Object> importarDesdeCsv(Integer categoriaId, Integer usuarioId,
                                                String csvContent, boolean sobreescribir) {
        try {
            String[] lineas = csvContent.split("\n");
            if (lineas.length < 2) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El fichero CSV está vacío o no tiene datos.");
            }

            String[] cabeceras = lineas[0].trim().split(",");

            // Validamos que el CSV venga de la aplicacion comprobando que tenga las columnas id y estado
            boolean tieneId = Arrays.stream(cabeceras).anyMatch(c -> c.trim().equals("id"));
            boolean tieneEstado = Arrays.stream(cabeceras).anyMatch(c -> c.trim().equals("estado"));

            if (!tieneId || !tieneEstado) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El fichero CSV no tiene el formato correcto. Asegúrate de que fue exportado desde CollectorHub.");
            }

            List<Articulo> existentes = articuloRepository.findByCategoriaIdAndUsuarioId(categoriaId, usuarioId);
            Set<Integer> idsExistentes = new HashSet<>();
            for (Articulo a : existentes) idsExistentes.add(a.getId());

            // Recorremos las filas para detectar conflictos y guardarlas para procesarlas despues
            List<Integer> conflictos = new ArrayList<>();
            List<String[]> filas = new ArrayList<>();

            for (int i = 1; i < lineas.length; i++) {
                if (lineas[i].trim().isEmpty()) continue;
                // split con -1 para no perder columnas vacias al final de la linea
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

                // Las columnas a partir de la 3ª son los campos dinamicos del articulo
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