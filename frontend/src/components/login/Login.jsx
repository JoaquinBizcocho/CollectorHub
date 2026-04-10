import { useState, useEffect } from 'react';
import './Login.css';

const Login = ({ alIrARegistro, alLoginExitoso }) => {
  const [alias, setAlias] = useState('');
  const [password, setPassword] = useState('');
  const [mostrarPass, setMostrarPass] = useState(false);
  const [mensaje, setMensaje] = useState('');
  
  const [isDarkMode, setIsDarkMode] = useState(() => {
    return localStorage.getItem('collectorhub-tema') === 'dark';
  });

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', isDarkMode ? 'dark' : 'light');
    localStorage.setItem('collectorhub-tema', isDarkMode ? 'dark' : 'light');
  }, [isDarkMode]);

  const manejarLogin = async (e) => {
  e.preventDefault();
  try {
    const respuesta = await fetch('http://localhost:8080/api/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ alias, password })
    });

    const data = await respuesta.json();
    setMensaje(data.mensaje);

    // Comprobamos que la respuesta es correcta y que el servidor nos ha enviado un ID de usuario
    if (respuesta.ok && data.usuarioId) {
      localStorage.setItem('collectorhub-usuario-id', data.usuarioId);
      localStorage.setItem('collectorhub-usuario-alias', alias);
      localStorage.setItem('collectorhub-rol', data.rol);

      // Esperamos un segundo para que el usuario pueda leer el mensaje de exito
      setTimeout(() => {
        alLoginExitoso();
      }, 1000);
    }
  } catch (error) {
    setMensaje("Error critico de conexion con el servidor");
  }
};

  return (
    <div className="login-card">
      {/* <button 
        className="login-theme-btn"
        onClick={() => setIsDarkMode(!isDarkMode)}
        title="Cambiar tema"
      >
        {isDarkMode ? '☀️' : '🌙'}
      </button> */}

      <h1 className="login-title">CollectorHub</h1>
      
      <form onSubmit={manejarLogin} className="login-form">
        <div className="login-input-group">
          <input 
            type="text" 
            className="login-input"
            placeholder="Alias de Coleccionista" 
            value={alias} 
            onChange={(e) => setAlias(e.target.value)} 
            required
          />
        </div>

        <div className="login-input-group">
          <input 
            type={mostrarPass ? "text" : "password"} 
            className="login-input"
            placeholder="Contraseña" 
            value={password} 
            onChange={(e) => setPassword(e.target.value)} 
            required
          />
          <button 
            type="button" 
            className="login-toggle-pass"
            onClick={() => setMostrarPass(!mostrarPass)}
          >
            {mostrarPass ? "🙈" : "👁️"}
          </button>
        </div>

        <button type="submit" className="login-submit-btn">
          Acceder al Inventario
        </button>
      </form>

      <div className="login-footer">
        <p>¿No tienes una cuenta de coleccionista?</p>
        <button type="button" className="login-link-btn" onClick={alIrARegistro}>
          Regístrate aquí
        </button>
      </div>

      {mensaje && <div className="login-alert">{mensaje}</div>}
    </div>
  );
};

export default Login;