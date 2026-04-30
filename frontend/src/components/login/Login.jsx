import { useState, useEffect } from 'react';
import './Login.css';

const Login = ({ alIrARegistro, alEntrar }) => {
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
      // 1. Asegúrate de que la ruta tiene /auth/
      const respuesta = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        // 2. Asegúrate de enviar la variable 'alias', no 'correo'
        body: JSON.stringify({ alias: alias, password: password })
      });

      // Nuestro backend devuelve un JSON (Map) tanto si hay éxito como si hay error
      const data = await respuesta.json();

     if (respuesta.ok) {
        // Guardamos todo tu "carnet de identidad" en el navegador
        localStorage.setItem('token', data.token);
        localStorage.setItem('usuarioId', data.usuarioId);
        localStorage.setItem('alias', alias); // Guardamos el nombre que escribiste
        localStorage.setItem('rol', data.rol);
        
        setMensaje("¡Acceso correcto! Entrando...");
        
        setTimeout(() => {
          alEntrar(); 
        }, 1000);
      } else {
        // Si la contraseña está mal o la cuenta no está verificada, mostramos el mensaje del backend
        setMensaje("Error: " + data.mensaje);
      }
    } catch (error) {
      setMensaje("Error crítico de conexión con el servidor. Revisa que el backend esté encendido.");
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