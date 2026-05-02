import { useState } from 'react';
import Login from './components/login/Login'; 
import Register from './components/register/Register';
import CategoriasDashboard from './components/categorias/CategoriaDashboard';
import ArticulosView from './components/articulos/ArticulosView';
import AdminDashboard from './components/admin/AdminDashboard'; 
import "./App.css"

function App() {

  const [vistaActual, setVistaActual] = useState(() => {
    const usuarioGuardado = localStorage.getItem('usuarioId');
    return usuarioGuardado ? 'dashboard' : 'login';
  });

  const [categoriaSeleccionada, setCategoriaSeleccionada] = useState(null);

  const cerrarSesion = () => {
    localStorage.removeItem('usuarioId');
    localStorage.removeItem('alias');
    localStorage.removeItem('rol');
    localStorage.removeItem('token');
    setCategoriaSeleccionada(null);
    setVistaActual('login');
  };

  return (
    <main className="main-layout">
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
          alIrAdmin={() => setVistaActual('admin')}
        />
      )}
      {vistaActual === 'articulos' && categoriaSeleccionada && (
        <ArticulosView 
          categoria={categoriaSeleccionada}
          alVolver={() => setVistaActual('dashboard')}
          alCerrarSesion={cerrarSesion}
        />
      )}
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