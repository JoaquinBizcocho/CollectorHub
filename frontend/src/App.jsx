import { useState } from 'react';
import Login from './components/login/Login'; 
import Register from './components/register/Register';
import CategoriasDashboard from './components/categorias/CategoriaDashboard';
import ArticulosView from './components/articulos/ArticulosView';
import AdminDashboard from './components/admin/AdminDashboard'; 
import "./App.css"

function App() {
  const [vistaActual, setVistaActual] = useState(() => {
    const usuarioGuardado = localStorage.getItem('collectorhub-usuario-id');
    return usuarioGuardado ? 'dashboard' : 'login';
  });

  const [categoriaSeleccionada, setCategoriaSeleccionada] = useState(null);

  const cerrarSesion = () => {
    localStorage.removeItem('collectorhub-usuario-id');
    localStorage.removeItem('collectorhub-usuario-alias');
    localStorage.removeItem('collectorhub-rol'); // Limpiamos el rol tambien
    setCategoriaSeleccionada(null);
    setVistaActual('login');
  };

  return (
    <main className="main-layout">
      {vistaActual === 'login' && (
        <Login 
          alIrARegistro={() => setVistaActual('register')} 
          alLoginExitoso={() => setVistaActual('dashboard')} 
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
          alIrAdmin={() => setVistaActual('admin')} // Le pasamos la funcion para ir al panel
        />
      )}
      {vistaActual === 'articulos' && categoriaSeleccionada && (
        <ArticulosView 
          categoria={categoriaSeleccionada}
          alVolver={() => setVistaActual('dashboard')}
          alCerrarSesion={cerrarSesion}
        />
      )}
      {/* NUEVA VISTA DE ADMIN */}
      {vistaActual === 'admin' && (
        <AdminDashboard 
          alVolver={() => setVistaActual('dashboard')}
          alCerrarSesion={cerrarSesion}
        />
      )}
    </main>
  );
}

export default App;