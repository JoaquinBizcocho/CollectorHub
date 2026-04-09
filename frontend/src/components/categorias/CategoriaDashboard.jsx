import { useState, useEffect } from 'react';
import './Categorias.css';

const CategoriasDashboard = ({ alCerrarSesion }) => { 
  const [categorias, setCategorias] = useState([]);
  const [mostrarModal, setMostrarModal] = useState(false);
  
  // Nuevo estado para saber si estamos editando o creando
  const [idEditando, setIdEditando] = useState(null);

  const [nombre, setNombre] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [esquema, setEsquema] = useState([{ nombre: '', tipo: 'text' }]);
  
  const nombreUsuario = localStorage.getItem('collectorhub-usuario-alias') || 'Coleccionista';

  useEffect(() => {
    cargarCategorias();
  }, []);

  const cargarCategorias = async () => {
    const usuarioId = localStorage.getItem('collectorhub-usuario-id');
    try {
      const response = await fetch(`http://localhost:8080/api/categorias/usuario/${usuarioId}`);
      if (response.ok) {
        const data = await response.json();
        setCategorias(data);
      }
    } catch (error) {
      console.error("Error al cargar categorías", error);
    }
  };

  // --- LÓGICA DE EDICIÓN Y BORRADO ---
  
  const abrirNuevaCategoria = () => {
    setIdEditando(null); // Nos aseguramos de que no estamos editando
    setNombre('');
    setDescripcion('');
    setEsquema([{ nombre: '', tipo: 'text' }]);
    setMostrarModal(true);
  };

  const abrirEdicion = (cat) => {
    setIdEditando(cat.id);
    setNombre(cat.nombre);
    setDescripcion(cat.descripcion);
    // Si la categoría no tiene esquema, ponemos uno vacío por defecto
    setEsquema(cat.esquema || []); 
    setMostrarModal(true);
  };

  const borrarCategoria = async (id) => {
    // Confirmación nativa del navegador para evitar borrados accidentales
    if (window.confirm("¿Estás seguro? Se borrarán todos los objetos de esta categoría para siempre.")) {
      try {
        const response = await fetch(`http://localhost:8080/api/categorias/${id}`, { 
          method: 'DELETE' 
        });
        if (response.ok) {
          cargarCategorias(); // Recargamos la lista al borrar
        }
      } catch (error) {
        console.error("Error al borrar la categoría", error);
      }
    }
  };

  // --- LÓGICA DE ESQUEMAS DINÁMICOS ---

  const agregarCampoEsquema = () => setEsquema([...esquema, { nombre: '', tipo: 'text' }]);
  
  const actualizarCampoEsquema = (index, campo, valor) => {
    const nuevoEsquema = [...esquema];
    nuevoEsquema[index][campo] = valor;
    setEsquema(nuevoEsquema);
  };

  const eliminarCampoEsquema = (index) => {
    const nuevoEsquema = esquema.filter((_, i) => i !== index);
    setEsquema(nuevoEsquema);
  };

  // --- GUARDAR (CREAR O EDITAR) ---

  const guardarCategoria = async (e) => {
    e.preventDefault();
    const esquemaLimpio = esquema.filter(campo => campo.nombre.trim() !== '');
    const usuarioId = localStorage.getItem('collectorhub-usuario-id');

    const categoriaGuardar = {
      nombre,
      descripcion,
      esquema: esquemaLimpio,
      usuarioId: parseInt(usuarioId)
    };

    // Si tenemos un idEditando, hacemos PUT (actualizar). Si no, POST (crear nueva).
    const metodo = idEditando ? 'PUT' : 'POST';
    const url = idEditando 
      ? `http://localhost:8080/api/categorias/${idEditando}` 
      : 'http://localhost:8080/api/categorias';

    try {
      const response = await fetch(url, {
        method: metodo,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(categoriaGuardar)
      });

      if (response.ok) {
        cargarCategorias();
        setMostrarModal(false);
      }
    } catch (error) {
      console.error("Error al guardar la categoría", error);
    }
  };

  return (
    <div className="dashboard-wrapper">
      <header className="dashboard-topbar">
        <h1 className="logo-text">COLLECTOR HUB</h1>
        <button className="btn-cerrar-sesion" onClick={alCerrarSesion}>
          Cerrar Sesión
        </button>
      </header>

      <div className="dashboard-container">
        <div className="dashboard-header">
          <div className="header-titles">
            <h2 className="welcome-text">Bienvenido, {nombreUsuario}</h2>
            <h3 className="section-title">Mis Colecciones</h3>
          </div>
          <button className="btn-nueva-categoria" onClick={abrirNuevaCategoria}>
            + Nueva Categoría
          </button>
        </div>

        <div className="categorias-list">
          {categorias.length === 0 ? (
            <p className="empty-state">Aún no tienes categorías. ¡Crea la primera!</p>
          ) : (
            categorias.map((cat) => (
              <div key={cat.id} className="categoria-card-horizontal">
                <div className="categoria-info">
                  <h4>{cat.nombre}</h4>
                  <p>{cat.descripcion}</p>
                  <small>{cat.esquema ? cat.esquema.length : 0} campos personalizados</small>
                </div>
                
                {/* --- NUEVA ZONA DE BOTONES --- */}
                <div className="categoria-acciones">
                  <button className="btn-icon" onClick={() => abrirEdicion(cat)} title="Editar categoría">✏️</button>
                  <button className="btn-icon btn-peligro" onClick={() => borrarCategoria(cat.id)} title="Borrar categoría">🗑️</button>
                  <button className="btn-entrar">Ver Inventario</button>
                </div>
              </div>
            ))
          )}
        </div>

        {mostrarModal && (
          <div className="modal-overlay">
            <div className="modal-content">
              {/* El título cambia según si creamos o editamos */}
              <h3>{idEditando ? 'Editar Categoría' : 'Crear Nueva Categoría'}</h3>
              <form onSubmit={guardarCategoria}>
                <input 
                  type="text" 
                  placeholder="Nombre (ej: Cartas Pokémon)" 
                  value={nombre} 
                  onChange={(e) => setNombre(e.target.value)} 
                  required 
                  className="input-base"
                />
                <textarea 
                  placeholder="Descripción breve..." 
                  value={descripcion} 
                  onChange={(e) => setDescripcion(e.target.value)} 
                  className="input-base"
                />

                <div className="esquema-builder">
                  <h4>Campos Personalizados (Opcional)</h4>
                  <p className="hint">Define qué datos quieres guardar para estos objetos.</p>
                  
                  {esquema.map((campo, index) => (
                    <div key={index} className="esquema-row">
                      <input 
                        type="text" 
                        placeholder="Ej: Valoración PSA" 
                        value={campo.nombre} 
                        onChange={(e) => actualizarCampoEsquema(index, 'nombre', e.target.value)}
                      />
                      <select 
                        value={campo.tipo} 
                        onChange={(e) => actualizarCampoEsquema(index, 'tipo', e.target.value)}
                      >
                        <option value="text">Texto Corto</option>
                        <option value="number">Número</option>
                        <option value="boolean">Sí / No</option>
                        <option value="date">Fecha</option>
                      </select>
                      <button type="button" onClick={() => eliminarCampoEsquema(index)}>X</button>
                    </div>
                  ))}
                  
                  <button type="button" className="btn-add-campo" onClick={agregarCampoEsquema}>
                    + Añadir otro campo
                  </button>
                </div>

                <div className="modal-actions">
                  <button type="button" className="btn-cancelar" onClick={() => setMostrarModal(false)}>Cancelar</button>
                  {/* El texto del botón también cambia */}
                  <button type="submit" className="btn-guardar">{idEditando ? 'Actualizar' : 'Guardar'}</button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default CategoriasDashboard;