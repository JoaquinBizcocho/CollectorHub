const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const getToken = () => localStorage.getItem('token');

// Callback que se ejecuta cuando la sesión expira, se registra desde App.jsx
let onSesionExpirada = null;

export const setSesionExpiradaCallback = (callback) => {
  onSesionExpirada = callback;
};

// Limpia el localStorage y llama al callback para volver al login cuando el token expira
const cerrarSesionAutomatico = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('usuarioId');
  localStorage.removeItem('alias');
  localStorage.removeItem('rol');
  if (onSesionExpirada) onSesionExpirada();
};

// Wrapper de fetch que cierra la sesión automáticamente si el backend devuelve 401
const fetchConAuth = async (url, opciones = {}) => {
  const response = await fetch(url, opciones);
  if (response.status === 401) {
    cerrarSesionAutomatico();
  }
  return response;
};

// Genera las cabeceras con el token JWT, añade Content-Type si la petición tiene body
const headers = (conBody = false) => ({
  ...(conBody && { 'Content-Type': 'application/json' }),
  'Authorization': 'Bearer ' + getToken()
});

// ===== ENDPOINTS DE CATEGORÍAS =====
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

// ===== ENDPOINTS DE ARTÍCULOS =====
export const articulosApi = {
  getByCategoria: (categoriaId) =>
    fetchConAuth(`${API_BASE}/api/articulos/categoria/${categoriaId}`, { headers: headers() }),

  exportarJson: (categoriaId) =>
    fetchConAuth(`${API_BASE}/api/articulos/categoria/${categoriaId}/exportar/json`, { headers: headers() }),

  exportarCsv: (categoriaId) =>
    fetchConAuth(`${API_BASE}/api/articulos/categoria/${categoriaId}/exportar/csv`, { headers: headers() }),

    // sobreescribir=false en la primera llamada para detectar conflictos, true si el usuario confirma
  importarJson: (categoriaId, jsonContent, sobreescribir = false) =>
  fetchConAuth(`${API_BASE}/api/articulos/categoria/${categoriaId}/importar/json?sobreescribir=${sobreescribir}`, {
    method: 'POST',
    headers: { ...headers(), 'Content-Type': 'application/json' },
    body: jsonContent
  }),

    // Igual que importarJson pero con Content-Type text/plain para el CSV
  importarCsv: (categoriaId, csvContent, sobreescribir = false) =>
    fetchConAuth(`${API_BASE}/api/articulos/categoria/${categoriaId}/importar/csv?sobreescribir=${sobreescribir}`, {
      method: 'POST',
      headers: { ...headers(), 'Content-Type': 'text/plain' },
      body: csvContent
    }),

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

// ===== ENDPOINTS DE ADMIN =====
export const adminApi = {
  getEstadisticas: () =>
    fetchConAuth(`${API_BASE}/api/admin/estadisticas`, { headers: headers() })
};

// ===== ENDPOINTS DE USUARIO =====
export const usuarioApi = {
  eliminarCuenta: () =>
    fetchConAuth(`${API_BASE}/api/usuario/cuenta`, {
      method: 'DELETE',
      headers: headers()
    })
};