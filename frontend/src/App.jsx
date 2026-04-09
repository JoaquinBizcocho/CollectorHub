// src/App.jsx
import { useState } from 'react';
import Login from './components/login/Login';
import Register from './components/register/Register';
import CategoriasDashboard from './components/categorias/CategoriaDashboard'; // Importamos el Dashboard
import './App.css';

function App() {
  // Ahora el estado puede ser: 'login', 'register' o 'dashboard'
  const [vistaActual, setVistaActual] = useState('login');

  return (
    <main className="main-layout">
      {/* Mostramos Login si el estado es 'login' */}
      {vistaActual === 'login' && (
        <Login 
          alIrARegistro={() => setVistaActual('register')} 
          alLoginExitoso={() => setVistaActual('dashboard')} // NUEVA LLAVE
        />
      )}

      {/* Mostramos Register si el estado es 'register' */}
      {vistaActual === 'register' && (
        <Register alIrALogin={() => setVistaActual('login')} />
      )}

      {/* Mostramos el Dashboard si el estado es 'dashboard' */}
      {vistaActual === 'dashboard' && (
        <CategoriasDashboard 
          alCerrarSesion={() => {
            localStorage.removeItem('collectorhub-usuario-id');
            localStorage.removeItem('collectorhub-usuario-alias');
            setVistaActual('login');
          }}
        />
      )}
    </main>
  );
}

export default App;