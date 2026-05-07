import { useState, useEffect } from 'react';
import '../categorias/Categorias.css';

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

  if (imagenesValidas.length === 0) {
    return <div className="articulo-sin-imagen">Sin imagen</div>;
  }

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

const ArticulosView = ({ categoria, alVolver, alCerrarSesion }) => {
  const [articulos, setArticulos] = useState([]);
  const [mostrarModal, setMostrarModal] = useState(false);
  const [idEditando, setIdEditando] = useState(null);
  const [datosFormulario, setDatosFormulario] = useState({});
  const [imagenes, setImagenes] = useState([]); 

  const [lightbox, setLightbox] = useState({ 
    activo: false, 
    listaImagenes: [], 
    indiceActual: 0 
  });

const usuarioId = localStorage.getItem('usuarioId');

  useEffect(() => {
    cargarArticulos();
  }, [categoria]);

  const cargarArticulos = async () => {
    try {
      const token = localStorage.getItem('token'); 
      const response = await fetch(`https://collectorhub-z5z2.onrender.com/api/articulos/categoria/${categoria.id}/usuario/${usuarioId}`, {
        method: 'GET',
        headers: {
          'Authorization': 'Bearer ' + token 
        }
      });
      if (response.ok) {
        const data = await response.json();
        setArticulos(data);
      }
    } catch (error) {
      console.error("Error al cargar articulos", error);
    }
  };

  const abrirLightbox = (imagenesLista, indexClicado) => {
    setLightbox({
      activo: true,
      listaImagenes: imagenesLista,
      indiceActual: indexClicado
    });
  };

  const cerrarLightbox = () => {
    setLightbox({ ...lightbox, activo: false });
  };

  const fotoSiguiente = (e) => {
    e.stopPropagation(); 
    setLightbox(prev => ({
      ...prev,
      indiceActual: (prev.indiceActual + 1) % prev.listaImagenes.length
    }));
  };

  const fotoAnterior = (e) => {
    e.stopPropagation();
    setLightbox(prev => ({
      ...prev,
      indiceActual: (prev.indiceActual - 1 + prev.listaImagenes.length) % prev.listaImagenes.length
    }));
  };

  const manejarCambioInput = (nombreCampo, valor) => {
    setDatosFormulario({ ...datosFormulario, [nombreCampo]: valor });
  };

  const manejarSubidaImagen = (e) => {
    const archivos = Array.from(e.target.files);
    if (imagenes.length + archivos.length > 2) {
      alert("Solo puedes subir un maximo de 2 imagenes por articulo.");
      return;
    }
    archivos.forEach(archivo => {
      const reader = new FileReader();
      reader.onloadend = () => {
        setImagenes(prev => [...prev, reader.result]);
      };
      reader.readAsDataURL(archivo);
    });
  };

  const quitarImagen = (index) => {
    setImagenes(imagenes.filter((_, i) => i !== index));
  };

  const abrirNuevoArticulo = () => {
    setIdEditando(null);
    setDatosFormulario({});
    setImagenes([]);
    setMostrarModal(true);
  };

  const abrirEdicion = (art) => {
    setIdEditando(art.id);
    setDatosFormulario(art.datos || {});
    const imgs = [];
    if (art.imagen1) imgs.push(art.imagen1);
    if (art.imagen2) imgs.push(art.imagen2);
    setImagenes(imgs);
    setMostrarModal(true);
  };

 const borrarArticulo = async (id) => {
    if (window.confirm("¿Estás seguro de que quieres borrar este artículo?")) {
      
      const token = localStorage.getItem('token'); 

      try {
        const response = await fetch(`https://collectorhub-z5z2.onrender.com/api/articulos/${id}`, { 
          method: 'DELETE',
          headers: {
            'Authorization': 'Bearer ' + token 
          }
        });

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
    
    let hayCamposVacios = false;
    const camposFaltantes = [];

    if (categoria.esquema) {
      categoria.esquema.forEach((campo) => {
        const valor = datosFormulario[campo.nombre];
        
        if (valor === undefined || valor === null || String(valor).trim() === "") {
          hayCamposVacios = true;
          camposFaltantes.push(campo.nombre);
        }
      });
    }

    if (hayCamposVacios) {
      alert(`Por favor, rellena todos los campos obligatorios. Te falta: ${camposFaltantes.join(', ')}`);
      return; 
    }
    // -----------------------------------------
    
    const articuloGuardar = {
      id: idEditando, 
      categoriaId: categoria.id,
      usuarioId: parseInt(usuarioId),
      datos: datosFormulario,
      imagen1: imagenes[0] || null,
      imagen2: imagenes[1] || null
    };
    
    const metodo = idEditando ? 'PUT' : 'POST';
    const url = idEditando ? `https://collectorhub-z5z2.onrender.com/api/articulos/${idEditando}` : 'https://collectorhub-z5z2.onrender.com/api/articulos';
    
    try {
      const token = localStorage.getItem('token');

      const response = await fetch(url, {
        method: metodo, 
        headers: { 
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + token 
        },
        body: JSON.stringify(articuloGuardar) 
      });

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
  }

  return (
    <div className="dashboard-wrapper">
      <header className="dashboard-topbar">
        <h1 className="logo-text">COLLECTOR HUB</h1>
        <div className="topbar-actions">
          <button className="btn-volver" onClick={alVolver}>Volver a Categorias</button>
          <button className="btn-cerrar-sesion" onClick={alCerrarSesion}>Cerrar Sesion</button>
        </div>
      </header>

      <div className="dashboard-container">
        <div className="dashboard-header">
          <div className="header-titles">
            <h2 className="welcome-text">{categoria.nombre}</h2>
            <h3 className="section-title">Inventario</h3>
          </div>
          <button className="btn-nueva-categoria" onClick={abrirNuevoArticulo}>
            + Añadir Articulo
          </button>
        </div>

        <div className="categorias-list">
          {articulos.length === 0 ? (
            <p className="empty-state">No hay objetos en esta coleccion aun.</p>
          ) : (
            articulos.map((art) => (
              <div key={art.id} className="articulo-card">
                <div className="articulo-imagen-container">
                  <CarruselMiniatura 
                    imagen1={art.imagen1} 
                    imagen2={art.imagen2} 
                    alHacerClic={abrirLightbox} 
                  />
                </div>

                <div className="articulo-info">
                  {categoria.esquema && categoria.esquema.map((campo, idx) => (
                    <div key={idx} className="articulo-dato">
                      <span className="dato-etiqueta">{campo.nombre}: </span>
                      <span className="dato-valor">
                        {art.datos && art.datos[campo.nombre] !== undefined && art.datos[campo.nombre] !== null && art.datos[campo.nombre] !== ''
                          ? (campo.tipo === 'boolean' ? (art.datos[campo.nombre] ? 'Si' : 'No') : art.datos[campo.nombre]) 
                          : '-'}
                      </span>
                    </div>
                  ))}
                </div>
                
                <div className="articulo-acciones-vertical">
                  <button className="btn-icon-text" onClick={() => abrirEdicion(art)}>Editar</button>
                  <button className="btn-icon-text peligro" onClick={() => borrarArticulo(art.id)}>Eliminar</button>
                </div>
              </div>
            ))
          )}
        </div>

        {mostrarModal && (
          <div className="modal-overlay">
            <div className="modal-content modal-largo">
              <h3>{idEditando ? 'Editar Articulo' : 'Añadir Articulo'}</h3>
              <form onSubmit={guardarArticulo}>
                <div className="campos-dinamicos-grid">
                  {categoria.esquema && categoria.esquema.map((campo, index) => (
                    <div key={index} className="campo-dinamico">
                      <label>{campo.nombre}</label>
                      {campo.tipo === 'boolean' ? (
                        <select className="input-base" value={datosFormulario[campo.nombre] !== undefined ? datosFormulario[campo.nombre] : ''} onChange={(e) => manejarCambioInput(campo.nombre, e.target.value === 'true' ? true : e.target.value === 'false' ? false : '')}>
                          <option value="">Selecciona...</option>
                          <option value="true">Si</option>
                          <option value="false">No</option>
                        </select>
                      ) : (
                        <input 
                          type={campo.tipo === 'number' ? 'number' : campo.tipo === 'date' ? 'date' : 'text'} 
                          className="input-base" 
                          placeholder={`Introduce ${campo.nombre}`} 
                          value={datosFormulario[campo.nombre] || ''} 
                          onChange={(e) => manejarCambioInput(campo.nombre, e.target.value)} 
                          maxLength={campo.tipo === 'text' ? 50 : undefined} 
                        />
                      )}
                    </div>
                  ))}
                </div>
                <div className="zona-imagenes">
                  <h4>Imagenes (Max. 2)</h4>
                  <input type="file" accept="image/*" multiple onChange={manejarSubidaImagen} disabled={imagenes.length >= 2} className="input-file" />
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

        {/* --- FLECHAS DE NAVEGACION --- */}
        {lightbox.activo && (
          <div className="lightbox-overlay" onClick={cerrarLightbox}>
            <div className="lightbox-content" onClick={(e) => e.stopPropagation()}>
              <button className="btn-close-lightbox" onClick={cerrarLightbox}>X</button>
              
              {/* Boton Izquierda */}
              {lightbox.listaImagenes.length > 1 && (
                <button className="btn-flecha-lightbox izq" onClick={fotoAnterior}>
                  &#10094;
                </button>
              )}

              <img 
                key={lightbox.indiceActual}
                src={lightbox.listaImagenes[lightbox.indiceActual]} 
                alt="Imagen agrandada" 
                className="lightbox-img animacion-fade" 
              />

              {/* Boton Derecha */}
              {lightbox.listaImagenes.length > 1 && (
                <button className="btn-flecha-lightbox der" onClick={fotoSiguiente}>
                  &#10095;
                </button>
              )}
            </div>
          </div>
        )}

      </div>
    </div>
  );
};

export default ArticulosView;