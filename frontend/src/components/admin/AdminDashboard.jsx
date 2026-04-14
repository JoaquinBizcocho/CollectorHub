import { useState, useEffect } from 'react';
import '../categorias/Categorias.css'; 

const AdminDashboard = ({ alVolver, alCerrarSesion }) => {
  const [estadisticas, setEstadisticas] = useState({ totalUsuarios: 0, totalCategorias: 0, totalArticulos: 0 });
  const [plantillas, setPlantillas] = useState([]);
  const [mostrarModal, setMostrarModal] = useState(false);
  const [nombre, setNombre] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [esquema, setEsquema] = useState([{ nombre: '', tipo: 'text' }]);

  const usuarioId = localStorage.getItem('collectorhub-usuario-id');

  useEffect(() => {
    cargarEstadisticas();
    cargarPlantillas();
  }, []);

  const cargarEstadisticas = async () => {
    try {
      // 1. Sacamos la "pulsera VIP" del almacenamiento
      const token = localStorage.getItem('collectorhub-token');

      // 2. Hacemos el fetch añadiendo las cabeceras (headers)
      const response = await fetch('http://localhost:8080/api/admin/estadisticas', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + token // ¡Aquí enseñamos el pase!
        }
      });

      if (response.ok) {
        const data = await response.json();
        setEstadisticas(data);
      } else {
        console.error("Acceso denegado. El token podría estar caducado o no eres admin.");
      }
    } catch (error) {
      console.error("Error al cargar estadisticas", error);
    }
  }

  const cargarPlantillas = async () => {
    try {
      const token = localStorage.getItem('collectorhub-token');

      const response = await fetch('http://localhost:8080/api/categorias/oficiales', {
        method: 'GET',
        headers: {
          'Authorization': 'Bearer ' + token
        }
      });
      if (response.ok) {
        const data = await response.json();
        setPlantillas(data);
      }
    } catch (error) {
      console.error("Error al cargar plantillas", error);
    }
  };

  const agregarCampoEsquema = () => setEsquema([...esquema, { nombre: '', tipo: 'text' }]);
  
  const actualizarCampoEsquema = (index, campo, valor) => {
    const nuevoEsquema = [...esquema];
    nuevoEsquema[index][campo] = valor;
    setEsquema(nuevoEsquema);
  };

  const eliminarCampoEsquema = (index) => {
    setEsquema(esquema.filter((_, i) => i !== index));
  };

  const borrarPlantilla = async (id) => {
    if (window.confirm("¿Borrar esta plantilla oficial?")) {
      const token = localStorage.getItem('collectorhub-token');

      await fetch(`http://localhost:8080/api/categorias/${id}`, { 
        method: 'DELETE',
        headers: {
          'Authorization': 'Bearer ' + token
        }
      });
      cargarPlantillas();
    }
  };

  const guardarPlantilla = async (e) => {
    e.preventDefault();
    const esquemaLimpio = esquema.filter(campo => campo.nombre.trim() !== '');
    
    const nuevaPlantilla = {
      nombre,
      descripcion,
      esquema: esquemaLimpio,
      usuarioId: parseInt(usuarioId),
      esOficial: true 
    };

    try {
      const token = localStorage.getItem('collectorhub-token');

      const response = await fetch('http://localhost:8080/api/categorias', {
        method: 'POST',
        headers: { 
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + token
        },
        body: JSON.stringify(nuevaPlantilla)
      });

      if (response.ok) {
        cargarPlantillas();
        cargarEstadisticas();
        setMostrarModal(false);
        setNombre('');
        setDescripcion('');
        setEsquema([{ nombre: '', tipo: 'text' }]);
      }
    } catch (error) {
      console.error("Error al guardar la plantilla", error);
    }
  };

  return (
    <div className="dashboard-wrapper">
      <header className="dashboard-topbar" style={{backgroundColor: '#333'}}>
        <h1 className="logo-text" style={{color: '#ffb300'}}>ADMIN </h1>
        <div className="topbar-actions">
          <button className="btn-volver" onClick={alVolver}>Volver a Mis categorias</button>
          <button className="btn-cerrar-sesion" onClick={alCerrarSesion}>Cerrar Sesión</button>
        </div>
      </header>

      <div className="dashboard-container">
        
        <h3 className="section-title" style={{ marginTop: '0', marginBottom: '20px' }}>Métricas Globales</h3>
        <div className="estadisticas-grid">
          <div className="estadistica-card">
            <h3>Usuarios Registrados</h3>
            <p className="estadistica-numero">{estadisticas.totalUsuarios}</p>
          </div>
          <div className="estadistica-card">
            <h3>Categorías Creadas</h3>
            <p className="estadistica-numero">{estadisticas.totalCategorias}</p>
          </div>
          <div className="estadistica-card">
            <h3>Artículos Totales</h3>
            <p className="estadistica-numero">{estadisticas.totalArticulos}</p>
          </div>
        </div>

        <div className="dashboard-header" style={{ marginTop: '40px' }}>
          <div className="header-titles">
            <h3 className="section-title">Plantillas Oficiales del Sistema</h3>
          </div>
          <button className="btn-nueva-categoria" style={{backgroundColor: '#ffb300', color: '#000'}} onClick={() => setMostrarModal(true)}>
            + Crear Plantilla Oficial
          </button>
        </div>

        <div className="categorias-list">
          {plantillas.length === 0 ? (
            <p className="empty-state">No hay plantillas oficiales todavía.</p>
          ) : (
            plantillas.map((plan) => (
              <div key={plan.id} className="categoria-card-horizontal" style={{borderLeft: '5px solid #ffb300'}}>
                <div className="categoria-info">
                  <h4 style={{ color: '#ffb300' }}>[OFICIAL] {plan.nombre}</h4>
                  <p>{plan.descripcion}</p>
                  <small>{plan.esquema ? plan.esquema.length : 0} campos definidos</small>
                </div>
                <button className="btn-icon-text peligro" onClick={() => borrarPlantilla(plan.id)}>Eliminar</button>
              </div>
            ))
          )}
        </div>

        {mostrarModal && (
          <div className="modal-overlay">
            <div className="modal-content">
              <h3>Crear Plantilla Oficial</h3>
              <form onSubmit={guardarPlantilla}>
                <input type="text" placeholder="Nombre (ej: Cartas Pokémon Oficial)" value={nombre} onChange={(e) => setNombre(e.target.value)} required className="input-base" />
                <textarea placeholder="Descripción breve..." value={descripcion} onChange={(e) => setDescripcion(e.target.value)} className="input-base" />

                <div className="esquema-builder">
                  <h4>Estructura de Datos Estándar</h4>
                  {esquema.map((campo, index) => (
                    <div key={index} className="esquema-row">
                      <input type="text" placeholder="Ej: Condición PSA" value={campo.nombre} onChange={(e) => actualizarCampoEsquema(index, 'nombre', e.target.value)} />
                      <select value={campo.tipo} onChange={(e) => actualizarCampoEsquema(index, 'tipo', e.target.value)}>
                        <option value="text">Texto Corto</option>
                        <option value="number">Número</option>
                        <option value="boolean">Sí / No</option>
                        <option value="date">Fecha</option>
                      </select>
                      <button type="button" onClick={() => eliminarCampoEsquema(index)}>X</button>
                    </div>
                  ))}
                  <button type="button" className="btn-add-campo" onClick={agregarCampoEsquema}>+ Añadir otro campo</button>
                </div>

                <div className="modal-actions">
                  <button type="button" className="btn-cancelar" onClick={() => setMostrarModal(false)}>Cancelar</button>
                  <button type="submit" className="btn-guardar" style={{backgroundColor: '#ffb300', color: '#000'}}>Guardar Plantilla</button>
                </div>
              </form>
            </div>
          </div>
        )}

      </div>
    </div>
  );
};

export default AdminDashboard;