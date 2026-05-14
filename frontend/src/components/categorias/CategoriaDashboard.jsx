import { useState, useEffect } from 'react';
import './Categorias.css';
import '../responsive/Responsive.css';
import { categoriasApi, usuarioApi } from '../../services/api';

const CategoriasDashboard = ({ alCerrarSesion, alAbrirCategoria, alIrAdmin }) => {
  const [categorias, setCategorias] = useState([]);
  const [plantillas, setPlantillas] = useState([]);
  
  const [pasoModal, setPasoModal] = useState(0);
  const [idEditando, setIdEditando] = useState(null);

  const [nombre, setNombre] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [esquema, setEsquema] = useState([{ nombre: '', tipo: 'text' }]);
  
  const nombreUsuario = localStorage.getItem('alias') || 'Coleccionista';
  const rolUsuario = localStorage.getItem('rol') || 'user';

  useEffect(() => {
    cargarCategorias();
    cargarPlantillas();
  }, []);

  const cargarCategorias = async () => {
    const token = localStorage.getItem('token');
    if (!token) { console.error("No hay sesión activa"); return; }
    try {
      const response = await categoriasApi.getByUsuario();
      if (response.ok) setCategorias(await response.json());
    } catch (error) {
      console.error("Error al cargar categorias", error);
    }
  };

  const cargarPlantillas = async () => {
    try {
      const response = await categoriasApi.getOficiales();
      if (response.ok) setPlantillas(await response.json());
    } catch (error) {
      console.error("Error al cargar plantillas", error);
    }
  };

  const abrirSelectorNuevaCategoria = () => {
    setIdEditando(null);
    setNombre('');
    setDescripcion('');
    setEsquema([{ nombre: '', tipo: 'text' }]);
    setPasoModal(1);
  };

  const abrirEdicion = (cat) => {
    setIdEditando(cat.id);
    setNombre(cat.nombre);
    setDescripcion(cat.descripcion);
    setEsquema(cat.esquema || []);
    setPasoModal(2);
  };

  const cerrarModal = () => {
    setPasoModal(0);
  };

  const borrarCategoria = async (id) => {
    if (window.confirm("¿Estas seguro? Se borraran todos los objetos de esta categoria para siempre.")) {
      try {
        const response = await categoriasApi.eliminar(id);
        if (response.ok) cargarCategorias();
      } catch (error) {
        console.error("Error al borrar la categoria", error);
      }
    }
  };

  const eliminarCuenta = async () => {
    const primerConfirm = window.confirm(
      "⚠️ ¿Estás seguro de que quieres eliminar tu cuenta?\n\nSe borrarán TODOS tus datos: categorías, artículos e imágenes. Esta acción es irreversible."
    );
    if (!primerConfirm) return;

    const segundoConfirm = window.confirm(
      "Esta es tu última oportunidad. ¿Confirmas que quieres borrar tu cuenta y todos tus datos permanentemente?"
    );
    if (!segundoConfirm) return;

    try {
      const response = await usuarioApi.eliminarCuenta();
      if (response.ok) {
        localStorage.removeItem('token');
        localStorage.removeItem('usuarioId');
        localStorage.removeItem('alias');
        localStorage.removeItem('rol');
        window.location.reload();
      } else {
        alert("Error al eliminar la cuenta. Inténtalo de nuevo.");
      }
    } catch (error) {
      console.error("Error al eliminar cuenta", error);
      alert("Error de conexión al intentar eliminar la cuenta.");
    }
  };

  const crearDesdePlantilla = async (plantilla) => {
    const nuevaCategoria = {
      nombre: plantilla.nombre,
      descripcion: plantilla.descripcion,
      esquema: plantilla.esquema || [],
      esOficial: false
    };
    try {
      const response = await categoriasApi.crear(nuevaCategoria);
      if (response.ok) { cargarCategorias(); cerrarModal(); }
      else console.error("No se pudo crear la categoría. Revisa tus permisos.");
    } catch (error) {
      console.error("Error al crear desde plantilla", error);
    }
  };

  const guardarCategoria = async (e) => {
    e.preventDefault();
    const esquemaLimpio = esquema.filter(campo => campo.nombre.trim() !== '');

    if (esquemaLimpio.length === 0) {
      alert("Error: No puedes crear una categoría vacía. Añade al menos un campo personalizado y ponle un nombre.");
      return;
    }

    const categoriaGuardar = {
      nombre,
      descripcion,
      esquema: esquemaLimpio,
      esOficial: false
    };

    try {
      const response = idEditando
        ? await categoriasApi.actualizar(idEditando, categoriaGuardar)
        : await categoriasApi.crear(categoriaGuardar);
      if (response.ok) { cargarCategorias(); cerrarModal(); }
    } catch (error) {
      console.error("Error al guardar la categoria", error);
    }
  };

  const agregarCampoEsquema = () => setEsquema([...esquema, { nombre: '', tipo: 'text' }]);
  const actualizarCampoEsquema = (index, campo, valor) => {
    const nuevoEsquema = [...esquema];
    nuevoEsquema[index][campo] = valor;
    setEsquema(nuevoEsquema);
  };
  const eliminarCampoEsquema = (index) => setEsquema(esquema.filter((_, i) => i !== index));

  return (
    <div className="dashboard-wrapper">
      <header className="dashboard-topbar">
        <h1 className="logo-text">COLLECTOR HUB</h1>
        <div className="topbar-actions">
          <button className="btn-eliminar-cuenta" onClick={eliminarCuenta}>
            Eliminar cuenta
          </button>
          <button className="btn-cerrar-sesion" onClick={alCerrarSesion}>
            Cerrar Sesion
          </button>
        </div>
      </header>

      <div className="dashboard-container">
        <div className="dashboard-header">
          <div className="header-titles">
            <h2 className="welcome-text">Bienvenido, {nombreUsuario}</h2>
            <h3 className="section-title">Mis Colecciones</h3>
          </div>
          
          <div className="topbar-actions">
            {rolUsuario === 'admin' && (
              <button 
                className="btn-nueva-categoria" 
                style={{backgroundColor: '#ffb300', color: 'black', marginRight: '10px'}} 
                onClick={alIrAdmin}
              >
                Panel Admin
              </button>
            )}
            <button className="btn-nueva-categoria" onClick={abrirSelectorNuevaCategoria}>
              + Nueva Categoria
            </button>
          </div>
        </div>

        <div className="categorias-list">
          {categorias.filter(cat => !cat.esOficial).length === 0 ? (
            <p className="empty-state">Aun no tienes categorias personales. ¡Crea la primera!</p>
          ) : (
            categorias.filter(cat => !cat.esOficial).map((cat) => (
              <div key={cat.id} className="categoria-card-horizontal">
                <div className="categoria-info">
                  <h4>{cat.nombre}</h4>
                  <p>{cat.descripcion}</p>
                  <small>{cat.esquema ? cat.esquema.length : 0} campos personalizados</small>
                </div>
                <div className="categoria-acciones">
                  <button className="btn-icon" onClick={() => abrirEdicion(cat)}>Editar</button>
                  <button className="btn-icon btn-peligro" onClick={() => borrarCategoria(cat.id)}>Borrar</button>
                  <button className="btn-entrar" onClick={() => alAbrirCategoria(cat)}>Ver Inventario</button>
                </div>
              </div>
            ))
          )}
        </div>

        {pasoModal > 0 && (
          <div className="modal-overlay">
            {pasoModal === 1 && (
              <div className="modal-content" style={{textAlign: 'center'}}>
                <h3 style={{marginBottom: '30px'}}>¿Como quieres empezar?</h3>
                <div className="opciones-ruta-grid">
                  <button className="btn-ruta oficial" onClick={() => setPasoModal(3)}>
                    <h4>Categorias ya definidas</h4>
                    <p>Usa una plantilla oficial con los campos ya configurados</p>
                  </button>
                  <button className="btn-ruta libre" onClick={() => setPasoModal(2)}>
                    <h4>Nueva categoria</h4>
                    <p>Crea tu coleccion desde cero con tus propios campos</p>
                  </button>
                </div>
                <div className="modal-actions" style={{justifyContent: 'center', marginTop: '30px'}}>
                  <button type="button" className="btn-cancelar" style={{flex: 'none', padding: '15px 40px'}} onClick={cerrarModal}>Cancelar</button>
                </div>
              </div>
            )}

            {pasoModal === 2 && (
              <div className="modal-content">
                <h3>{idEditando ? 'Editar Categoria' : 'Crear Nueva Categoria'}</h3>
                <form onSubmit={guardarCategoria}>
                  <input type="text" placeholder="Nombre (ej: Cartas Pokemon)" value={nombre} onChange={(e) => setNombre(e.target.value)} required className="input-base" maxLength={50} />
                  <textarea placeholder="Descripcion breve..." value={descripcion} onChange={(e) => setDescripcion(e.target.value)} className="input-base" />

                  <div className="esquema-builder">
                    <h4>Campos Personalizados</h4>
                    <p className="hint">Define que datos quieres guardar para estos objetos.</p>
                    {esquema.map((campo, index) => (
                      <div key={index} className="esquema-row">
                        <input
                          type="text"
                          placeholder="Ej: Valoracion"
                          value={campo.nombre}
                          onChange={(e) => actualizarCampoEsquema(index, 'nombre', e.target.value)}
                          maxLength={50}
                        />
                        <select value={campo.tipo} onChange={(e) => actualizarCampoEsquema(index, 'tipo', e.target.value)}>
                          <option value="text">Texto Corto</option>
                          <option value="number">Numero</option>
                          <option value="boolean">Si / No</option>
                          <option value="date">Fecha</option>
                        </select>
                        <button type="button" onClick={() => eliminarCampoEsquema(index)}>X</button>
                      </div>
                    ))}
                    <button type="button" className="btn-add-campo" onClick={agregarCampoEsquema}>+ Añadir otro campo</button>
                  </div>

                  <div className="modal-actions">
                    <button type="button" className="btn-cancelar" onClick={idEditando ? cerrarModal : () => setPasoModal(1)}>
                      {idEditando ? 'Cancelar' : 'Volver atras'}
                    </button>
                    <button type="submit" className="btn-guardar">{idEditando ? 'Actualizar' : 'Guardar'}</button>
                  </div>
                </form>
              </div>
            )}

            {pasoModal === 3 && (
              <div className="modal-content modal-largo">
                <h3>Seleccionar plantilla oficial</h3>
                <div className="plantillas-grid" style={{marginTop: '20px', marginBottom: '20px'}}>
                  {plantillas.length === 0 ? (
                    <p className="empty-state">No hay plantillas oficiales disponibles en este momento.</p>
                  ) : (
                    plantillas.map((plan) => (
                      <div key={plan.id} className="categoria-card-horizontal" style={{borderLeft: '5px solid #ffb300', marginBottom: '15px'}}>
                        <div className="categoria-info">
                          <h4 style={{color: '#ffb300', margin: '0 0 5px 0'}}>{plan.nombre}</h4>
                          <p style={{margin: '0 0 10px 0', opacity: 0.8}}>{plan.descripcion}</p>
                          <small style={{color: '#9e9e9e'}}>
                            Campos incluidos: {plan.esquema ? plan.esquema.map(e => e.nombre).join(', ') : 'Ninguno'}
                          </small>
                        </div>
                        <button className="btn-guardar" style={{minWidth: '150px'}} onClick={() => crearDesdePlantilla(plan)}>
                          Usar plantilla
                        </button>
                      </div>
                    ))
                  )}
                </div>
                <div className="modal-actions">
                  <button type="button" className="btn-cancelar" onClick={() => setPasoModal(1)}>Volver atras</button>
                  <button type="button" className="btn-cancelar" onClick={cerrarModal}>Cerrar</button>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default CategoriasDashboard;