import { useState, useEffect } from 'react';
import '../categorias/Categorias.css';
import './Articulos.css';
import '../responsive/Responsive.css'
import { articulosApi } from '../../services/api';

const CarruselMiniatura = ({ imagen1, imagen2, alHacerClic }) => {
  const [indice, setIndice] = useState(0);
  
  const imagenesValidas = [imagen1, imagen2].filter(img => img !== null && img !== "");

  useEffect(() => {
    if (imagenesValidas.length > 1) {
      const temporizador = setInterval(() => {
        setIndice(prev => (prev + 1) % imagenesValidas.length);
      }, 5000);
      return () => clearInterval(temporizador);
    }
  }, [imagenesValidas.length]);

  if (imagenesValidas.length === 0) return null;

  return (
    <img 
      src={imagenesValidas[indice]} 
      alt="Articulo" 
      className="articulo-imagen-preview clickable animacion-fade" 
      onClick={() => alHacerClic(imagenesValidas, indice)}
      title="Haz clic para agrandar"
    />
  );
};

const formatearValor = (campo, valor) => {
  if (campo.tipo === 'boolean') return valor === true || valor === 'true' ? 'Si' : 'No';
  if (campo.tipo === 'date') return new Date(valor + 'T00:00:00').toLocaleDateString('es-ES');
  return valor;
};

const claveIndice = (index) => `campo_${index}`;

const datosAFormulario = (esquema, datos) => {
  const formulario = {};
  esquema.forEach((_, index) => {
    const clave = claveIndice(index);
    formulario[clave] = datos?.[clave] ?? '';
  });
  return formulario;
};

const formularioADatos = (esquema, formulario) => {
  const datos = {};
  esquema.forEach((_, index) => {
    const clave = claveIndice(index);
    datos[clave] = formulario[clave] ?? '';
  });
  return datos;
};

const ordenarArticulos = (articulos, esquema, campoIndex, direccion) => {
  if (campoIndex === null || !esquema?.[campoIndex]) return articulos;
  const campo = esquema[campoIndex];
  const clave = claveIndice(campoIndex);
  return [...articulos].sort((a, b) => {
    const valA = a.datos?.[clave] ?? '';
    const valB = b.datos?.[clave] ?? '';
    if (valA === '' && valB !== '') return 1;
    if (valB === '' && valA !== '') return -1;
    if (valA === '' && valB === '') return 0;
    let resultado = 0;
    if (campo.tipo === 'number') {
      resultado = parseFloat(valA) - parseFloat(valB);
    } else if (campo.tipo === 'boolean') {
      const numA = (valA === true || valA === 'true') ? 1 : 0;
      const numB = (valB === true || valB === 'true') ? 1 : 0;
      resultado = numB - numA;
    } else if (campo.tipo === 'date') {
      resultado = new Date(valA) - new Date(valB);
    } else {
      resultado = String(valA).localeCompare(String(valB), 'es', { sensitivity: 'base' });
    }
    return direccion === 'asc' ? resultado : -resultado;
  });
};

const etiquetaDireccion = (tipo, direccion) => {
  if (tipo === 'number') return direccion === 'asc' ? '↑ Menor' : '↓ Mayor';
  if (tipo === 'boolean') return direccion === 'asc' ? '↑ Sí' : '↓ No';
  if (tipo === 'date') return direccion === 'asc' ? '↑ Antiguo' : '↓ Reciente';
  return direccion === 'asc' ? '↑ A→Z' : '↓ Z→A';
};

// ===== PANEL DE ESTADÍSTICAS =====
const PanelEstadisticas = ({ articulos, esquema }) => {
  const [modoNumerico, setModoNumerico] = useState({});

  const coleccion = articulos.filter(a => a.estado === 'COLECCION');
  const wishlist = articulos.filter(a => a.estado === 'WISHLIST');

  const camposNumericos = (esquema || [])
    .map((campo, idx) => ({ ...campo, idx }))
    .filter(c => c.tipo === 'number');

  const camposBooleanos = (esquema || [])
    .map((campo, idx) => ({ ...campo, idx }))
    .filter(c => c.tipo === 'boolean');

  const calcularNumerico = (campo) => {
    const clave = claveIndice(campo.idx);
    const valores = coleccion
      .map(a => parseFloat(a.datos?.[clave]))
      .filter(v => !isNaN(v));
    if (valores.length === 0) return '—';
    const modo = modoNumerico[campo.idx] || 'suma';
    if (modo === 'suma') {
      return valores.reduce((a, b) => a + b, 0).toLocaleString('es-ES', { maximumFractionDigits: 2 });
    }
    return (valores.reduce((a, b) => a + b, 0) / valores.length).toLocaleString('es-ES', { maximumFractionDigits: 2 });
  };

  const toggleModo = (idx) => {
    setModoNumerico(prev => ({
      ...prev,
      [idx]: (prev[idx] || 'suma') === 'suma' ? 'media' : 'suma'
    }));
  };

  return (
    <div className="stats-panel">
      <h3 className="stats-titulo">Estadísticas</h3>

      <div className="stats-seccion">
        <div className="stats-fila">
          <span className="stats-label">En colección</span>
          <span className="stats-valor">{coleccion.length}</span>
        </div>
        <div className="stats-fila">
          <span className="stats-label">Deseados</span>
          <span className="stats-valor stats-valor-wishlist">{wishlist.length}</span>
        </div>
      </div>

      {camposNumericos.length > 0 && (
        <div className="stats-seccion">
          <p className="stats-subtitulo">Campos numéricos (Mi Collecion)</p>
          {camposNumericos.map(campo => (
            <div key={campo.idx} className="stats-numerico">
              <div className="stats-numerico-header">
                <span className="stats-label">{campo.nombre}</span>
                <button
                  className="btn-toggle-modo"
                  onClick={() => toggleModo(campo.idx)}
                  title="Cambiar entre Suma y Media"
                >
                  {(modoNumerico[campo.idx] || 'suma') === 'suma' ? 'Suma' : 'Media'}
                </button>
              </div>
              <span className="stats-valor-grande">{calcularNumerico(campo)}</span>
            </div>
          ))}
        </div> 
      )}

      {camposBooleanos.length > 0 && (
        <div className="stats-seccion">
          <p className="stats-subtitulo">Campos Sí / No (Mi Collecion)</p>
          {camposBooleanos.map(campo => {
            const clave = claveIndice(campo.idx);
            const si = coleccion.filter(a => a.datos?.[clave] === true || a.datos?.[clave] === 'true').length;
            const no = coleccion.filter(a => a.datos?.[clave] === false || a.datos?.[clave] === 'false').length;
            return (
              <div key={campo.idx} className="stats-boolean">
                <span className="stats-label">{campo.nombre}</span>
                <div className="stats-boolean-valores">
                  <span className="stats-si">✓ {si}</span>
                  <span className="stats-no">✗ {no}</span>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {camposNumericos.length === 0 && camposBooleanos.length === 0 && (
        <p className="stats-vacio">Esta categoría no tiene campos numéricos ni Sí/No.</p>
      )}
    </div>
  );
};

const ArticulosView = ({ categoria, alVolver, alCerrarSesion }) => {
  const [articulos, setArticulos] = useState([]);
  const [mostrarModal, setMostrarModal] = useState(false);
  const [idEditando, setIdEditando] = useState(null);
  const [datosFormulario, setDatosFormulario] = useState({});
  const [imagenes, setImagenes] = useState([]);
  const [arrastrando, setArrastrando] = useState(false);
  const [pestanaActiva, setPestanaActiva] = useState('COLECCION');

  const [ordenCampoIndex, setOrdenCampoIndex] = useState(null);
  const [ordenDireccion, setOrdenDireccion] = useState('asc');

  const [lightbox, setLightbox] = useState({ 
    activo: false, 
    listaImagenes: [], 
    indiceActual: 0 
  });

  useEffect(() => { cargarArticulos(); }, [categoria]);
  useEffect(() => {
    setOrdenCampoIndex(null);
    setOrdenDireccion('asc');
  }, [categoria]);

  const cargarArticulos = async () => {
    try {
      const response = await articulosApi.getByCategoria(categoria.id);
      if (response.ok) setArticulos(await response.json());
    } catch (error) {
      console.error("Error al cargar articulos", error);
    }
  };

  const abrirLightbox = (imagenesLista, indexClicado) => {
    setLightbox({ activo: true, listaImagenes: imagenesLista, indiceActual: indexClicado });
  };
  const cerrarLightbox = () => setLightbox({ ...lightbox, activo: false });
  const fotoSiguiente = (e) => {
    e.stopPropagation();
    setLightbox(prev => ({ ...prev, indiceActual: (prev.indiceActual + 1) % prev.listaImagenes.length }));
  };
  const fotoAnterior = (e) => {
    e.stopPropagation();
    setLightbox(prev => ({ ...prev, indiceActual: (prev.indiceActual - 1 + prev.listaImagenes.length) % prev.listaImagenes.length }));
  };

  const manejarCambioInput = (clave, valor) => {
    setDatosFormulario(prev => ({ ...prev, [clave]: valor }));
  };

  const procesarArchivos = (archivos) => {
    if (imagenes.length + archivos.length > 2) {
      alert("Solo puedes subir un maximo de 2 imagenes por articulo.");
      return;
    }
    archivos.forEach(archivo => {
      const reader = new FileReader();
      reader.onloadend = () => setImagenes(prev => [...prev, reader.result]);
      reader.readAsDataURL(archivo);
    });
  };

  const manejarSubidaImagen = (e) => procesarArchivos(Array.from(e.target.files));
  const manejarDrop = (e) => {
    e.preventDefault(); e.stopPropagation(); setArrastrando(false);
    const archivos = Array.from(e.dataTransfer.files).filter(f => f.type.startsWith('image/'));
    procesarArchivos(archivos);
  };
  const manejarPegado = (e) => {
    const items = e.clipboardData?.items;
    if (!items) return;
    const archivos = [];
    for (const item of items) { if (item.type.startsWith('image/')) archivos.push(item.getAsFile()); }
    procesarArchivos(archivos);
  };
  const quitarImagen = (index) => setImagenes(imagenes.filter((_, i) => i !== index));

  const abrirNuevoArticulo = () => {
    setIdEditando(null);
    setDatosFormulario(datosAFormulario(categoria.esquema || [], {}));
    setImagenes([]);
    setMostrarModal(true);
  };

  const abrirEdicion = (art) => {
    setIdEditando(art.id);
    setDatosFormulario(datosAFormulario(categoria.esquema || [], art.datos || {}));
    const imgs = [];
    if (art.imagen1) imgs.push(art.imagen1);
    if (art.imagen2) imgs.push(art.imagen2);
    setImagenes(imgs);
    setMostrarModal(true);
  };

  const borrarArticulo = async (id) => {
    if (window.confirm("¿Estás seguro de que quieres borrar este artículo?")) {
      try {
        const response = await articulosApi.eliminar(id);
        if (response.ok) {
          cargarArticulos();
        } else {
          const errorTexto = await response.text();
          alert("Error al eliminar: El servidor rechazó la operación. " + errorTexto);
        }
      } catch (error) {
        console.error("Error de conexión al borrar", error);
        alert("Error crítico de conexión al intentar borrar.");
      }
    }
  };

  const guardarArticulo = async (e) => {
    e.preventDefault();
    const camposFaltantes = [];
    if (categoria.esquema) {
      categoria.esquema.forEach((campo, index) => {
        const clave = claveIndice(index);
        const valor = datosFormulario[clave];
        if (valor === undefined || valor === null || String(valor).trim() === "") {
          camposFaltantes.push(campo.nombre);
        }
      });
    }
    if (camposFaltantes.length > 0) {
      alert(`Por favor, rellena todos los campos obligatorios. Te falta: ${camposFaltantes.join(', ')}`);
      return;
    }
    const articuloGuardar = {
      categoriaId: categoria.id,
      estado: idEditando ? (articulos.find(a => a.id === idEditando)?.estado || pestanaActiva) : pestanaActiva,
      datos: formularioADatos(categoria.esquema || [], datosFormulario),
      imagen1: imagenes[0] || null,
      imagen2: imagenes[1] || null
    };
    try {
      const response = idEditando
        ? await articulosApi.actualizar(idEditando, articuloGuardar)
        : await articulosApi.crear(articuloGuardar);
      if (response.ok) {
        cargarArticulos();
        setMostrarModal(false);
        setIdEditando(null);
      } else {
        alert("Error de permisos: El servidor rechazó la operación.");
      }
    } catch (error) {
      console.error("Error al guardar", error);
    }
  };

  const descargarArchivo = async (tipo) => {
    const response = tipo === 'csv'
      ? await articulosApi.exportarCsv(categoria.id)
      : await articulosApi.exportarJson(categoria.id);
    const blob = await response.blob();
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${categoria.nombre}_coleccion.${tipo}`;
    a.click();
    URL.revokeObjectURL(url);
  };

  const articulosOrdenados = ordenarArticulos(articulos, categoria.esquema, ordenCampoIndex, ordenDireccion);
  const articulosFiltrados = articulosOrdenados.filter(a => a.estado === pestanaActiva);
  const campoOrdenActual = ordenCampoIndex !== null ? categoria.esquema?.[ordenCampoIndex] : null;

  return (
    <div className="dashboard-wrapper">
      <header className="dashboard-topbar">
        <h1 className="logo-text">COLLECTOR HUB</h1>
        <div className="topbar-actions">
          <button className="btn-volver" onClick={alVolver}>Volver a Categorias</button>
          <button className="btn-cerrar-sesion" onClick={alCerrarSesion}>Cerrar Sesion</button>
        </div>
      </header>

      <div className="articulos-layout">

        <div className="articulos-main">
          <div className="dashboard-header">
            <div className="header-titles">
              <h2 className="welcome-text">{categoria.nombre}</h2>
              <div className="pestanas-wrapper">
                <button
                  className={`btn-pestana ${pestanaActiva === 'COLECCION' ? 'activa' : ''}`}
                  onClick={() => setPestanaActiva('COLECCION')}
                > Mi Colección </button>
                <button
                  className={`btn-pestana wishlist ${pestanaActiva === 'WISHLIST' ? 'activa wishlist-activa' : ''}`}
                  onClick={() => setPestanaActiva('WISHLIST')}
                > Deseados </button>
              </div>
            </div>
            <div className="topbar-actions">
              <button className="btn-nueva-categoria" onClick={() => descargarArchivo('csv')}>Exportar CSV</button>
              <button className="btn-nueva-categoria" onClick={() => descargarArchivo('json')}>Exportar JSON</button>
              <button className="btn-nueva-categoria" onClick={abrirNuevoArticulo}>+ Añadir Articulo</button>
            </div>
          </div>

          {articulosFiltrados.length > 0 && categoria.esquema?.length > 0 && (
            <div className="orden-barra">
              <span className="orden-label">Ordenar por:</span>
              <div className="orden-control">
                <select
                  className="orden-select"
                  value={ordenCampoIndex ?? ''}
                  onChange={(e) => {
                    const val = e.target.value;
                    setOrdenCampoIndex(val === '' ? null : parseInt(val));
                    setOrdenDireccion('asc');
                  }}
                >
                  <option value="">— Sin ordenar —</option>
                  {categoria.esquema.map((campo, idx) => (
                    <option key={idx} value={idx}>{campo.nombre}</option>
                  ))}
                </select>
                {ordenCampoIndex !== null && (
                  <>
                    <div className="orden-divisor" />
                    <button
                      type="button"
                      className="btn-orden-direccion"
                      onClick={() => setOrdenDireccion(prev => prev === 'asc' ? 'desc' : 'asc')}
                    >
                      {etiquetaDireccion(campoOrdenActual?.tipo, ordenDireccion)}
                    </button>
                  </>
                )}
              </div>
            </div>
          )}

          <div className="categorias-list">
            {articulosFiltrados.length === 0 ? (
              <p className="empty-state">
                {pestanaActiva === 'WISHLIST'
                  ? 'No tienes artículos en tu Wishlist todavía.'
                  : 'No hay objetos en esta colección aún.'}
              </p>
            ) : (
              articulosFiltrados.map((art) => {
                const tieneImagenes = (art.imagen1 && art.imagen1 !== "") || (art.imagen2 && art.imagen2 !== "");
                return (
                  <div key={art.id} className="articulo-card">
                    {tieneImagenes && (
                      <div className="articulo-imagen-container">
                        <CarruselMiniatura 
                          imagen1={art.imagen1} 
                          imagen2={art.imagen2} 
                          alHacerClic={abrirLightbox} 
                        />
                      </div>
                    )}
                    <div className="articulo-info">
                      {categoria.esquema && categoria.esquema.map((campo, idx) => {
                        const clave = claveIndice(idx);
                        const valor = art.datos?.[clave];
                        return (
                          <div key={idx} className="articulo-dato">
                            <span className="dato-etiqueta">{campo.nombre}: </span>
                            <span className="dato-valor">
                              {valor !== undefined && valor !== null && valor !== ''
                                ? formatearValor(campo, valor)
                                : '-'}
                            </span>
                          </div>
                        );
                      })}
                    </div>
                    <div className="articulo-acciones-vertical">
                      <button className="btn-icon-text" onClick={() => abrirEdicion(art)}>Editar</button>
                      <button className="btn-icon-text peligro" onClick={() => borrarArticulo(art.id)}>Eliminar</button>
                    </div>
                  </div>
                );
              })
            )}
          </div>
        </div>

        <aside className="articulos-sidebar">
          <PanelEstadisticas articulos={articulos} esquema={categoria.esquema} />
        </aside>

      </div>

      {mostrarModal && (
        <div className="modal-overlay">
          <div className="modal-content modal-largo">
            <h3>{idEditando ? 'Editar Articulo' : 'Añadir Articulo'}</h3>
            <form onSubmit={guardarArticulo}>
              <div className="campos-dinamicos-grid">
                {categoria.esquema && categoria.esquema.map((campo, index) => {
                  const clave = claveIndice(index);
                  return (
                    <div key={index} className="campo-dinamico">
                      <label>{campo.nombre}</label>
                      {campo.tipo === 'boolean' ? (
                        <select
                          className="input-base"
                          value={datosFormulario[clave] !== undefined ? datosFormulario[clave] : ''}
                          onChange={(e) => manejarCambioInput(clave, e.target.value === 'true' ? true : e.target.value === 'false' ? false : '')}
                        >
                          <option value="">Selecciona...</option>
                          <option value="true">Si</option>
                          <option value="false">No</option>
                        </select>
                      ) : (
                        <input
                          type={campo.tipo === 'number' ? 'number' : campo.tipo === 'date' ? 'date' : 'text'}
                          className="input-base"
                          placeholder={`Introduce ${campo.nombre}`}
                          value={datosFormulario[clave] || ''}
                          onChange={(e) => manejarCambioInput(clave, e.target.value)}
                          maxLength={campo.tipo === 'text' ? 50 : undefined}
                        />
                      )}
                    </div>
                  );
                })}
              </div>

              <div
                className={`zona-imagenes ${arrastrando ? 'arrastrando' : ''}`}
                onDrop={manejarDrop}
                onDragOver={(e) => { e.preventDefault(); setArrastrando(true); }}
                onDragLeave={() => setArrastrando(false)}
                onPaste={manejarPegado}
                tabIndex={0}
              >
                <h4>Imágenes (Max. 2)</h4>
                <label className={`btn-subir-imagen ${imagenes.length >= 2 ? 'disabled' : ''}`}>
                  Seleccionar archivo
                  <input type="file" accept="image/*" multiple onChange={manejarSubidaImagen} disabled={imagenes.length >= 2} style={{ display: 'none' }} />
                </label>
                <div className="imagenes-preview-container">
                  {imagenes.map((img, idx) => (
                    <div key={idx} className="miniatura-wrapper">
                      <img src={img} alt={`Preview ${idx}`} className="miniatura-img" />
                      <button type="button" className="btn-quitar-img" onClick={() => quitarImagen(idx)}>X</button>
                    </div>
                  ))}
                </div>
              </div>

              <div className="modal-actions">
                <button type="button" className="btn-cancelar" onClick={() => setMostrarModal(false)}>Cancelar</button>
                <button type="submit" className="btn-guardar">{idEditando ? 'Actualizar' : 'Guardar'}</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {lightbox.activo && (
        <div className="lightbox-overlay" onClick={cerrarLightbox}>
          <div className="lightbox-content" onClick={(e) => e.stopPropagation()}>
            <button className="btn-close-lightbox" onClick={cerrarLightbox}>X</button>
            {lightbox.listaImagenes.length > 1 && (
              <button className="btn-flecha-lightbox izq" onClick={fotoAnterior}>&#10094;</button>
            )}
            <img
              key={lightbox.indiceActual}
              src={lightbox.listaImagenes[lightbox.indiceActual]}
              alt="Imagen agrandada"
              className="lightbox-img animacion-fade"
            />
            {lightbox.listaImagenes.length > 1 && (
              <button className="btn-flecha-lightbox der" onClick={fotoSiguiente}>&#10095;</button>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default ArticulosView;