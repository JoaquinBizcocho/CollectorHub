import { useState, useEffect } from 'react';
import './Register.css';

// URL del backen, en produccion la coge de la variable de entorno
const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080';

// alIrALogin es el callback del padre para volver a la vista de login
const Register = ({ alIrALogin }) => {
  // paso 1: formulario registro, paso 2: verificacion PIN
  const [paso, setPaso] = useState(1);
  const [alias, setAlias] = useState('');
  const [correo, setCorreo] = useState('');
  const [password, setPassword] = useState('');
  const [pin, setPin] = useState('');
  const [mostrarPass, setMostrarPass] = useState(false);
  const [mensaje, setMensaje] = useState('');
  const [cargando, setCargando] = useState(false);


  // Valida los campos, llama al endpoint de registro y avanza al paso 2 si va bien
  const manejarRegistro = async (e) => {
    e.preventDefault();

    // Validamos la contraseña en el frontend antes de llamar al backend
    const regexPassword = /^(?=.*[0-9])(?=.*[!@#$%^&*(),.?":{}|<>]).{8,}$/;
    if (!regexPassword.test(password)) {
      setMensaje("Error: La contraseña debe tener al menos 8 caracteres, un número y un símbolo.");
      return;
    }

    const regexCorreo = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!regexCorreo.test(correo)) {
      setMensaje("Error: Formato de correo inválido.");
      return;
    }

    setCargando(true);
    setMensaje('');

    try {
      const respuesta = await fetch(`${API_BASE}/api/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ alias, correoElectronico: correo, password })
      });

      const texto = await respuesta.text();

      if (respuesta.ok) {
        setMensaje("Te hemos enviado un código de 6 dígitos a tu correo.");
        // Avanzamos al formulario de verificacion del PIN
        setPaso(2);
      } else {
        setMensaje(texto);
      }
    } catch (error) {
      setMensaje("Error de conexión con el servidor. El servidor puede estar arrancando, espera unos segundos e inténtalo de nuevo.");
    } finally {
      setCargando(false);
    }
  };

  // Envia el PIN al backend para verificar la cuenta, si va bien redirige al login
  const manejarVerificacion = async (e) => {
    e.preventDefault();

    setCargando(true);
    setMensaje('');

    try {
      const respuesta = await fetch(`${API_BASE}/api/auth/verify-pin`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ alias, pin })
      });

      const texto = await respuesta.text();

      if (respuesta.ok) {
        setMensaje("¡Cuenta verificada con éxito! Redirigiendo al login...");
        // Esperamos 3 segundos para que el usuario lea el mensaje antes de redirigir
        setTimeout(() => {
          alIrALogin();
        }, 3000);
      } else {
        setMensaje(texto);
      }
    } catch (error) {
      setMensaje("Error de conexión al verificar.");
    } finally {
      setCargando(false);
    }
  };

  return (
    <div className="register-card">
      {/* El titulo y subtitulo cambian segun el paso en el que este el usuario */}
      <h1 className="register-title">{paso === 1 ? "Crear Cuenta" : "Verifica tu correo"}</h1>
      <p className="register-subtitle">{paso === 1 ? "Únete a CollectorHub" : "Introduce el PIN que hemos enviado a tu email"}</p>

      {paso === 1 && (
        <form onSubmit={manejarRegistro} className="register-form">
          <div className="register-input-group">
            <input type="text" className="register-input" placeholder="Alias de Coleccionista" value={alias} onChange={(e) => setAlias(e.target.value)} required disabled={cargando} />
          </div>
          <div className="register-input-group">
            <input type="email" className="register-input" placeholder="Correo Electrónico" value={correo} onChange={(e) => setCorreo(e.target.value)} required disabled={cargando} />
          </div>
          <div className="register-input-group">
            <input type={mostrarPass ? "text" : "password"} className="register-input" placeholder="Contraseña segura" value={password} onChange={(e) => setPassword(e.target.value)} required disabled={cargando} />
            <button type="button" className="register-toggle-pass" onClick={() => setMostrarPass(!mostrarPass)}>
              {mostrarPass ? "🙈" : "👁️"}
            </button>
          </div>
          <button type="submit" className="register-submit-btn" disabled={cargando}>
            {cargando ? 'Registrando...' : 'Registrarse'}
          </button>
        </form>
      )}

      {paso === 2 && (
        <form onSubmit={manejarVerificacion} className="register-form">
          <div className="register-input-group">
            {/* Input del PIN con estilo especial para que los digitos se vean separados */}
            <input
              type="text"
              className="register-input"
              placeholder="Ej: 123456"
              maxLength="6"
              value={pin}
              onChange={(e) => setPin(e.target.value)}
              required
              disabled={cargando}
              style={{ letterSpacing: '4px', textAlign: 'center', fontSize: '1.2rem', fontWeight: 'bold' }}
            />
          </div>
          <button type="submit" className="register-submit-btn" disabled={cargando}>
            {cargando ? 'Verificando...' : 'Verificar PIN'}
          </button>
        </form>
      )}

      <div className="register-footer">
        <p>¿Ya tienes una cuenta?</p>
        <button type="button" className="register-link-btn" onClick={alIrALogin}>Inicia sesión</button>
      </div>

      {mensaje && <div className="register-alert">{mensaje}</div>}
    </div>
  );
};

export default Register;