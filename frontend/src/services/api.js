const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const getToken = () => localStorage.getItem('token');

let onSesionExpirada = null;

export const setSesionExpiradaCallback = (callback) => {
  onSesionExpirada = callback;
};

const cerrarSesionAutomatico = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('usuarioId');
  localStorage.removeItem('alias');
  localStorage.removeItem('rol');
  if (onSesionExpirada) onSesionExpirada();
};

const fetchConAuth = async (url, opciones = {}) => {
  const response = await fetch(url, opciones);
  if (response.status === 401) {
    cerrarSesionAutomatico();
  }
  return response;
};

const headers = (conBody = false) => ({
  ...(conBody && { 'Content-Type': 'application/json' }),
  'Authorization': 'Bearer ' + getToken()
});

export const categoriasApi = {
  getByUsuario: () =>
    fetchConAuth(`${API_BASE}/api/categorias/usuario`, { headers: headers() }),

  getOficiales: () =>
    fetchConAuth(`${API_BASE}/api/categorias/oficiales`, { headers: headers() }),

  crear: (data) =>
    fetchConAuth(`${API_BASE}/api/categorias`, {
      method: 'POST',
      headers: headers(true),
      body: JSON.stringify(data)
    }),

  actualizar: (id, data) =>
    fetchConAuth(`${API_BASE}/api/categorias/${id}`, {
      method: 'PUT',
      headers: headers(true),
      body: JSON.stringify(data)
    }),

  eliminar: (id) =>
    fetchConAuth(`${API_BASE}/api/categorias/${id}`, {
      method: 'DELETE',
      headers: headers()
    })
};

export const articulosApi = {
  getByCategoria: (categoriaId) =>
    fetchConAuth(`${API_BASE}/api/articulos/categoria/${categoriaId}`, { headers: headers() }),

  exportarJson: (categoriaId) =>
    fetchConAuth(`${API_BASE}/api/articulos/categoria/${categoriaId}/exportar/json`, { headers: headers() }),

  exportarCsv: (categoriaId) =>
    fetchConAuth(`${API_BASE}/api/articulos/categoria/${categoriaId}/exportar/csv`, { headers: headers() }),

  crear: (data) =>
    fetchConAuth(`${API_BASE}/api/articulos`, {
      method: 'POST',
      headers: headers(true),
      body: JSON.stringify(data)
    }),

  actualizar: (id, data) =>
    fetchConAuth(`${API_BASE}/api/articulos/${id}`, {
      method: 'PUT',
      headers: headers(true),
      body: JSON.stringify(data)
    }),

  eliminar: (id) =>
    fetchConAuth(`${API_BASE}/api/articulos/${id}`, {
      method: 'DELETE',
      headers: headers()
    })
};

export const adminApi = {
  getEstadisticas: () =>
    fetchConAuth(`${API_BASE}/api/admin/estadisticas`, { headers: headers() })
};

export const usuarioApi = {
  eliminarCuenta: () =>
    fetchConAuth(`${API_BASE}/api/usuario/cuenta`, {
      method: 'DELETE',
      headers: headers()
    })
};