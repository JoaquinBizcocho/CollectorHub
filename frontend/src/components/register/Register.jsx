import { useState, useEffect } from 'react';
import './Register.css'; // Usamos su propio CSS

const Register = ({ alIrALogin }) => {
  const [alias, setAlias] = useState('');
  const [correo, setCorreo] = useState('');
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

  const manejarRegistro = async (e) => {
    e.preventDefault();
    try {
      const respuesta = await fetch('http://localhost:8080/api/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ alias: alias, correo_electronico: correo, password: password })
      });
      
      const texto = await respuesta.text();
      setMensaje(texto);
      
      if (texto.includes('✅')) {
        setAlias('');
        setCorreo('');
        setPassword('');
      }
    } catch (error) {
      setMensaje("Error crítico de conexión con el Backend");
    }
  };

  return (
    <div className="register-card">
      {/* <button 
        className="register-theme-btn"
        onClick={() => setIsDarkMode(!isDarkMode)}
        title="Cambiar tema"
      >
        {isDarkMode ? '☀️' : '🌙'}
      </button> */}

      <h1 className="register-title">Crear Cuenta</h1>
      <p className="register-subtitle">Únete a CollectorHub</p>
      
      <form onSubmit={manejarRegistro} className="register-form">
        <div className="register-input-group">
          <input 
            type="text" 
            className="register-input"
            placeholder="Alias de Coleccionista" 
            value={alias} 
            onChange={(e) => setAlias(e.target.value)} 
            required
          />
        </div>

        <div className="register-input-group">
          <input 
            type="email" 
            className="register-input"
            placeholder="Correo Electrónico" 
            value={correo} 
            onChange={(e) => setCorreo(e.target.value)} 
            required
          />
        </div>

        <div className="register-input-group">
          <input 
            type={mostrarPass ? "text" : "password"} 
            className="register-input"
            placeholder="Contraseña segura" 
            value={password} 
            onChange={(e) => setPassword(e.target.value)} 
            required
          />
          <button 
            type="button" 
            className="register-toggle-pass"
            onClick={() => setMostrarPass(!mostrarPass)}
          >
            {mostrarPass ? "🙈" : "👁️"}
          </button>
        </div>

        <button type="submit" className="register-submit-btn">
          Registrarse
        </button>
      </form>

      <div className="register-footer">
        <p>¿Ya tienes una cuenta?</p>
        <button type="button" className="register-link-btn" onClick={alIrALogin}>
          Inicia sesión
        </button>
      </div>

      {mensaje && <div className="register-alert">{mensaje}</div>}
    </div>
  );
};

export default Register;