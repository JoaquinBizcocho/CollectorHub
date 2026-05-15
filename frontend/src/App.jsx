import { useState, useEffect } from 'react';
import Login from './components/login/Login'; 
import Register from './components/register/Register';
import CategoriasDashboard from './components/categorias/CategoriaDashboard';
import ArticulosView from './components/articulos/ArticulosView';
import AdminDashboard from './components/admin/AdminDashboard'; 
import { setSesionExpiradaCallback } from './services/api';
import "./App.css"

// Lee el rol directamente del JWT sin fiarse del localStorage
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

  const [vistaActual, setVistaActual] = useState(() => {
    const usuarioGuardado = localStorage.getItem('usuarioId');
    return usuarioGuardado ? 'dashboard' : 'login';
  });

  const [categoriaSeleccionada, setCategoriaSeleccionada] = useState(null);

  const cerrarSesion = () => {
    localStorage.removeItem('usuarioId');
    localStorage.removeItem('alias');
    localStorage.removeItem('token');
    setCategoriaSeleccionada(null);
    setVistaActual('login');
  };

  useEffect(() => {
    setSesionExpiradaCallback(() => {
      setCategoriaSeleccionada(null);
      setVistaActual('login');
      alert('Tu sesión ha expirado. Por favor, inicia sesión de nuevo.');
    });
  }, []);

  const irAAdmin = () => {
  const rol = getRolDesdeToken();
  if (rol === 'admin') {
    setVistaActual('admin');
  } else {
    alert('No tienes permisos para acceder al panel de administración.');
  }
};

  return (
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