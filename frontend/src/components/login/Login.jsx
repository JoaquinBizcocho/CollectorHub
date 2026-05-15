import { useState, useEffect } from 'react';
import './Login.css';

const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080';

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
      const respuesta = await fetch(`${API_BASE}/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ alias: alias, password: password })
      });

      if (respuesta.ok) {
        const data = await respuesta.json();

        if (typeof data.token !== 'string' || !data.token) {
          setMensaje("Error: respuesta del servidor inválida.");
          return;
        }

        localStorage.setItem('token', data.token);
        localStorage.setItem('usuarioId', data.usuarioId);
        localStorage.setItem('alias', alias);

        setMensaje("¡Acceso correcto! Entrando...");
        setTimeout(() => { alEntrar(); }, 1000);

      } else {
        const texto = await respuesta.text();
        if (respuesta.status === 401) {
          setMensaje("Alias o contraseña incorrectos.");
        } else if (respuesta.status === 403) {
          setMensaje("Cuenta no verificada. Revisa tu correo electrónico.");
        } else {
          setMensaje(texto || "Error al iniciar sesión. Inténtalo de nuevo.");
        }
      }

    } catch (error) {
      setMensaje("No se puede conectar con el servidor. Espera unos segundos e inténtalo de nuevo.");
    }
  };

  return (
    <div className="login-card">

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