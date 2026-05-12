const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const getToken = () => localStorage.getItem('token');

const headers = (conBody = false) => ({
  ...(conBody && { 'Content-Type': 'application/json' }),
  'Authorization': 'Bearer ' + getToken()
});

export const categoriasApi = {
  getByUsuario: () =>
    fetch(`${API_BASE}/api/categorias/usuario`, { headers: headers() }),

  getOficiales: () =>
    fetch(`${API_BASE}/api/categorias/oficiales`, { headers: headers() }),

  crear: (data) =>
    fetch(`${API_BASE}/api/categorias`, {
      method: 'POST',
      headers: headers(true),
      body: JSON.stringify(data)
    }),

  actualizar: (id, data) =>
    fetch(`${API_BASE}/api/categorias/${id}`, {
      method: 'PUT',
      headers: headers(true),
      body: JSON.stringify(data)
    }),

  eliminar: (id) =>
    fetch(`${API_BASE}/api/categorias/${id}`, {
      method: 'DELETE',
      headers: headers()
    })
};

export const articulosApi = {
  getByCategoria: (categoriaId) =>
    fetch(`${API_BASE}/api/articulos/categoria/${categoriaId}`, { headers: headers() }),

  crear: (data) =>
    fetch(`${API_BASE}/api/articulos`, {
      method: 'POST',
      headers: headers(true),
      body: JSON.stringify(data)
    }),

  actualizar: (id, data) =>
    fetch(`${API_BASE}/api/articulos/${id}`, {
      method: 'PUT',
      headers: headers(true),
      body: JSON.stringify(data)
    }),

  eliminar: (id) =>
    fetch(`${API_BASE}/api/articulos/${id}`, {
      method: 'DELETE',
      headers: headers()
    })
};

export const adminApi = {
  getEstadisticas: () =>
    fetch(`${API_BASE}/api/admin/estadisticas`, { headers: headers() })
};