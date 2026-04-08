// src/components/Login/Login.jsx
import { useState, useEffect } from 'react';
import './Login.css';

/**
 * Componente principal de autenticación (Login).
 * Gestiona la interfaz de acceso del usuario, el cambio de tema (claro/oscuro) 
 * y la comunicación con la API del Backend para validar credenciales.
 * * @returns {JSX.Element} La interfaz de usuario de la tarjeta de login.
 */
const Login = () => {
  // --- ESTADOS DEL COMPONENTE ---
  
  /** @type {[string, Function]} Estado que almacena el nombre de usuario o alias introducido. */
  const [alias, setAlias] = useState('');
  
  /** @type {[string, Function]} Estado que almacena la contraseña introducida. */
  const [password, setPassword] = useState('');
  
  /** @type {[boolean, Function]} Estado que controla si la contraseña es visible (texto plano) o está oculta. */
  const [mostrarPass, setMostrarPass] = useState(false);
  
  /** @type {[string, Function]} Estado para mostrar mensajes de retroalimentación al usuario (éxito o error). */
  const [mensaje, setMensaje] = useState('');
  
  /** * @type {[boolean, Function]} Estado que determina si el modo oscuro está activado en esta vista. 
   * Comprueba la memoria del navegador (localStorage) al iniciar para mantener las preferencias del usuario.
   */
  const [isDarkMode, setIsDarkMode] = useState(() => {
    const temaGuardado = localStorage.getItem('collectorhub-tema');
    return temaGuardado === 'dark';
  });

  // --- EFECTOS SECUNDARIOS (HOOKS) ---

  /**
   * Efecto que se ejecuta cada vez que el estado `isDarkMode` cambia.
   * Inyecta dinámicamente el atributo 'data-theme' en la raíz del documento HTML
   * para aplicar las variables CSS del modo claro u oscuro y lo guarda en localStorage.
   */
  useEffect(() => {
    if (isDarkMode) {
      document.documentElement.setAttribute('data-theme', 'dark');
      localStorage.setItem('collectorhub-tema', 'dark');
    } else {
      document.documentElement.setAttribute('data-theme', 'light');
      localStorage.setItem('collectorhub-tema', 'light');
    }
  }, [isDarkMode]);

  // --- FUNCIONES MANEJADORAS ---

  /**
   * Intercepta el envío del formulario, previene la recarga de la página 
   * y realiza una petición asíncrona al Backend para validar las credenciales.
   * * @async
   * @param {React.FormEvent<HTMLFormElement>} e - El evento de envío del formulario.
   */
  const manejarLogin = async (e) => {
    e.preventDefault();
    try {
      const respuesta = await fetch('http://localhost:8080/api/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ alias, password })
      });
      const texto = await respuesta.text();
      setMensaje(texto);
    } catch (error) {
      setMensaje("Error en la conexión con el Backend");
    }
  };

  // --- RENDERIZADO DE LA INTERFAZ ---

  return (
    <div className="login-card">
      {/* Botón sutil en la esquina de la tarjeta para alternar el tema visual */}
      <button 
        className="btn-theme-mini"
        onClick={() => setIsDarkMode(!isDarkMode)}
        title="Cambiar tema"
      >
        {isDarkMode ? '☀️' : '🌙'}
      </button>

      <h1>CollectorHub</h1>
      
      <form onSubmit={manejarLogin}>
        <div className="input-group">
          <input 
            type="text" 
            placeholder="Alias de Coleccionista" 
            value={alias} 
            onChange={(e) => setAlias(e.target.value)} 
            required
          />
        </div>

        <div className="input-group">
          <input 
            type={mostrarPass ? "text" : "password"} 
            placeholder="Contraseña" 
            value={password} 
            onChange={(e) => setPassword(e.target.value)} 
            required
          />
          {/* Botón para cambiar el tipo de input entre 'password' y 'text' */}
          <button 
            type="button" 
            className="btn-toggle"
            onClick={() => setMostrarPass(!mostrarPass)}
            aria-label={mostrarPass ? "Ocultar contraseña" : "Mostrar contraseña"}
          >
            {mostrarPass ? "🙈" : "👁️"}
          </button>
        </div>

        <button type="submit" className="login-button">
          Acceder al Inventario
        </button>
      </form>

      {/* Renderizado condicional: Solo dibuja este div si la variable 'mensaje' tiene algún texto */}
      {mensaje && <div className="mensaje-alerta">{mensaje}</div>}
    </div>
  );
};

export default Login;