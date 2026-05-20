import { useState } from 'react';
import './Login.css';

// URL del backend, en produccion coge la variable de entorno
const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080';

// alIraRegistro y alEntrar son callbacks del componente padre para cambiar de vista
const Login = ({ alIrARegistro, alEntrar }) => {
  const [alias, setAlias] = useState('');
  const [password, setPassword] = useState('');
  const [mostrarPass, setMostrarPass] = useState(false);
  const [mensaje, setMensaje] = useState('');
  

  // Gestiona el submit del formulario, llama al endpoint de login y guarda el token
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

        // Validamos que el token sea un string valido antes de guardarlo
        if (typeof data.token !== 'string' || !data.token) {
          setMensaje("Error: respuesta del servidor inválida.");
          return;
        }

        // Guardamos el token y datos del usuario en localStorage para usarlos en el resto de la app
        localStorage.setItem('token', data.token);
        localStorage.setItem('usuarioId', data.usuarioId);
        localStorage.setItem('alias', alias);

        setMensaje("¡Acceso correcto! Entrando...");
        setTimeout(() => { alEntrar(); }, 1000);

      } else {
        const texto = await respuesta.text();
        // Mensaje distintivo degun el codigo de error que devuelva el backend
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
          {/* Boton par alternar la visibilidad de la contraseña */}
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