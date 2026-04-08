// src/App.jsx
import { useState } from 'react';
import Login from './components/login/Login';
import Register from './components/register/Register';
import './App.css';

function App() {
  // Estado que controla qué pantalla se ve. Empieza siempre en 'login'
  const [vistaActual, setVistaActual] = useState('login');

  return (
    <main className="main-layout">
      {/* Renderizado condicional: 
        Si vistaActual es igual a 'login', dibuja la tarjeta de Login.
        Si no, dibuja la tarjeta de Register.
      */}
      {vistaActual === 'login' ? (
        <Login alIrARegistro={() => setVistaActual('register')} />
      ) : (
        <Register alIrALogin={() => setVistaActual('login')} />
      )}
    </main>
  );
}

export default App;