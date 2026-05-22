import { useState, useEffect } from 'react';
import Login from './components/login/Login'; 
import Register from './components/register/Register';
import CategoriasDashboard from './components/categorias/CategoriaDashboard';
import ArticulosView from './components/articulos/ArticulosView';
import AdminDashboard from './components/admin/AdminDashboard'; 
import { setSesionExpiradaCallback } from './services/api';
import "./App.css"

// Decodifica el JWT para sacar el rol sin fiarse del localStorage, que podría estar manipulado
const getRolDesdeToken = () => {
  try {
    const token = localStorage.getItem('token');
    if (!token) return null;
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.rol || null;
  } catch (e) {
    return null;
  }
};

function App() {

    // Si ya hay sesión guardada arrancamos directamente en el dashboard en vez del login
  const [vistaActual, setVistaActual] = useState(() => {
    const usuarioGuardado = localStorage.getItem('usuarioId');
    return usuarioGuardado ? 'dashboard' : 'login';
  });

  const [categoriaSeleccionada, setCategoriaSeleccionada] = useState(null);

    // Limpia el localStorage y vuelve al login
  const cerrarSesion = () => {
    localStorage.removeItem('usuarioId');
    localStorage.removeItem('alias');
    localStorage.removeItem('token');
    setCategoriaSeleccionada(null);
    setVistaActual('login');
  };

    // Registramos el callback de sesión expirada para que api.js pueda redirigir al login cuando el token caduca
  useEffect(() => {
    setSesionExpiradaCallback(() => {
      setCategoriaSeleccionada(null);
      setVistaActual('login');
      alert('Tu sesión ha expirado. Por favor, inicia sesión de nuevo.');
    });
  }, []);

    // Verifica el rol desde el token antes de dejar entrar al panel de admin
  const irAAdmin = () => {
  const rol = getRolDesdeToken();
  if (rol === 'admin') {
    setVistaActual('admin');
  } else {
    alert('No tienes permisos para acceder al panel de administración.');
  }
};

  return (
        // El main-layout solo aplica en login y registro para centrar la tarjeta en pantalla
    <main className={vistaActual === 'login' || vistaActual === 'register' ? "main-layout" : ""}>
      {vistaActual === 'login' && (
        <Login 
          alIrARegistro={() => setVistaActual('register')} 
          alEntrar={() => setVistaActual('dashboard')} 
        />
      )}
      {vistaActual === 'register' && (
        <Register alIrALogin={() => setVistaActual('login')} />
      )}
      {vistaActual === 'dashboard' && (
        <CategoriasDashboard 
          alCerrarSesion={cerrarSesion}
          alAbrirCategoria={(categoria) => {
            setCategoriaSeleccionada(categoria);
            setVistaActual('articulos');
          }}
          alIrAdmin={irAAdmin}
        />
      )}
      {vistaActual === 'articulos' && categoriaSeleccionada && (
        <ArticulosView 
          categoria={categoriaSeleccionada}
          alVolver={() => setVistaActual('dashboard')}
          alCerrarSesion={cerrarSesion}
        />
      )}
            {/* Doble comprobación del rol para que nadie pueda llegar a la vista admin sin serlo */}
      {vistaActual === 'admin' && getRolDesdeToken() === 'admin' && (
        <AdminDashboard 
          alVolver={() => setVistaActual('dashboard')}
          alCerrarSesion={cerrarSesion}
        />
      )}
    </main>
  );
}

export default App;