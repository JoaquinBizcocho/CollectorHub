DROP DATABASE IF EXISTS collectorhub;
CREATE DATABASE collectorhub;
USE collectorhub;

CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alias VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    correo_electronico VARCHAR(255) NOT NULL UNIQUE,
    rol VARCHAR(50),
    cuenta_activa TINYINT(1) NOT NULL DEFAULT 0,
    codigo_verificacion VARCHAR(6)
);

CREATE TABLE categorias (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    descripcion VARCHAR(255),
    usuario_id BIGINT NOT NULL,
    es_oficial TINYINT(1) DEFAULT 0,
    esquema JSON,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE TABLE articulos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    categoria_id INT NOT NULL,
    usuario_id BIGINT NOT NULL,
    datos JSON,
    imagen1 LONGTEXT,
    imagen2 LONGTEXT,
    FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);
