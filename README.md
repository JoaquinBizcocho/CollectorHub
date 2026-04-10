Documentación Operativa: Collector Hub (Versión Inicial)
1. Descripción del Sistema
Collector Hub es una aplicación web full-stack diseñada para la gestión personalizada de colecciones. El sistema permite a los usuarios definir sus propias estructuras de datos (categorías dinámicas) y gestionar inventarios con soporte multimedia.

2. Requisitos de Entorno
Para poner en marcha la aplicación, se requiere el siguiente entorno:

Backend: Java JDK 17 o superior y Maven.

Frontend: Node.js (v18+) y gestor de paquetes npm.

Base de Datos: MySQL (compatible con Railway para despliegue).

3. Arquitectura Técnica
La aplicación sigue una arquitectura desacoplada:

Backend (Spring Boot): Expone una API REST que gestiona la lógica de negocio, persistencia en base de datos y seguridad. Utiliza BCrypt para el cifrado de contraseñas.

Frontend (React + Vite): Interfaz de usuario SPA (Single Page Application) que consume los servicios del backend mediante fetch. Utiliza LocalStorage para la gestión de estados de sesión y roles.

4. Guía de Puesta en Marcha
4.1. Preparación del Backend
Configurar la base de datos en src/main/resources/application.properties.

Ejecutar el proyecto mediante IntelliJ IDEA o usando el comando ./mvnw spring-boot:run.

El servidor se levantará por defecto en el puerto 8080.

4.2. Preparación del Frontend
Navegar a la carpeta del frontend.

Instalar dependencias: npm install.

Lanzar en modo desarrollo: npm run dev.

Acceder mediante http://localhost:5173.

5. Gestión de Roles y Seguridad
Actualmente, el sistema implementa dos niveles de acceso:

user: Acceso a la gestión de sus propias colecciones e inventarios.

admin: Acceso al panel de estadísticas globales y capacidad de crear "Plantillas Oficiales".

Seguridad: Se ha implementado un filtrado de rutas en el Frontend para que los usuarios no visualicen herramientas de administración si no poseen el rol correspondiente.

6. Manejo de Datos Especiales
Esquemas Dinámicos: Se utiliza el tipo de dato JSON en MySQL para almacenar la estructura personalizada de cada categoría, permitiendo flexibilidad total sin cambiar la base de datos.

Multimedia: Los artículos soportan hasta dos imágenes. Estas se procesan en el Frontend, se convierten a Base64 y se almacenan como LONGTEXT para garantizar la persistencia sin servidores de archivos externos.

7. Próximos Pasos (Fase Final)
Esta documentación se ampliará en la siguiente entrega incluyendo:

Batería de pruebas unitarias para los controladores.

Refactorización de servicios de seguridad.

Manual de usuario final.
