CREATE DATABASE IF NOT EXISTS collectorhub;
USE collectorhub;

CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    alias VARCHAR(50) NOT NULL UNIQUE,
    correo_electronico VARCHAR(100) UNIQUE NOT NULL,
    rol ENUM('admin', 'user') DEFAULT 'user',
    password VARCHAR(255) NOT NULL
);

CREATE TABLE categorias (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    esquema JSON
);

CREATE TABLE articulos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    categoria_id INT,
    precio DECIMAL(10, 2) DEFAULT 0.00,
    datos_especificos JSON,
    fotos JSON,
    usuario_id INT,
    FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- Usuarios de prueba para el profesor
INSERT INTO usuarios (alias, correo_electronico, rol, password) VALUES 
('profe_test', 'profe@ejemplo.com', 'admin', 'profe123'),
('alumno_test', 'alumno@ejemplo.com', 'user', 'alumno123');