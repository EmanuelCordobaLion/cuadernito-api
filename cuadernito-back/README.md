# Cuadernito API - Backend

API REST desarrollada con Spring Boot para que comerciantes barriales registren ventas, gastos y deudas de forma digital.

## Tecnologías

- **Java 21**
- **Spring Boot 3.5.10**
- **Spring Security + JWT** (Stateless, autenticación basada en tokens)
- **Spring Data JPA con MySQL** (Persistencia de datos)
- **Lombok** (Reducción de código boilerplate)
- **MapStruct** (Mapeo automático de DTOs)
- **Spring Boot Validation (JSR-303)** (Validación de datos)
- **SpringDoc OpenAPI 2.7.0 (Swagger)** (Documentación interactiva de API)

## Estructura del Proyecto

```
src/main/java/com/cuadernito/cuadernito_back/
├── config/              # Configuraciones (Security, etc.)
├── controller/          # Controladores REST
├── dto/                 # Data Transfer Objects
│   └── auth/           # DTOs de autenticación
├── entity/             # Entidades JPA
├── exception/          # Manejo de excepciones
├── mapper/             # Mappers MapStruct
├── repository/         # Repositorios JPA
├── security/           # Configuración de seguridad y JWT
└── service/            # Lógica de negocio
    └── impl/          # Implementaciones de servicios
```

## Configuración Inicial

### 1. Base de Datos con Docker

Levanta MySQL usando Docker Compose:

```bash
docker-compose up -d
```

Esto creará un contenedor MySQL con:
- Base de datos: `cuadernito_db`
- Usuario: `root`
- Contraseña: `root`
- Puerto: `3306`

### 2. Usuario Administrador Inicial

El usuario ADMIN se crea automáticamente al iniciar la aplicación por primera vez mediante `DataInitializer`.

**Credenciales por defecto (CAMBIAR después del primer login):**
- Email: `admin@cuadernito.com`
- Contraseña: `Admin123`

**IMPORTANTE**: Cambia estas credenciales inmediatamente después del primer login por seguridad.

### 3. Configuración de JWT

El JWT está configurado en `application.properties`:

```properties
jwt.secret=cuadernito-dev-secret-key-2026-minimum-32-characters-for-development-only
jwt.expiration=86400000  # 24 horas en milisegundos
```

**IMPORTANTE**: 
- Para desarrollo: La clave actual es suficiente
- Para producción: Genera una clave aleatoria segura con `openssl rand -base64 64` y reemplázala

## Explicación de Spring Security y JWT

### Arquitectura de Seguridad

La seguridad está implementada de forma **stateless** usando JWT (JSON Web Tokens). Esto significa que el servidor no mantiene sesiones, sino que cada request incluye un token que valida la identidad del usuario.

### Componentes Principales

#### 1. **SecurityConfig** (`config/SecurityConfig.java`)
- Configura la cadena de filtros de seguridad
- Define qué endpoints son públicos (`/api/v1/auth/**`)
- Configura el resto de endpoints para requerir autenticación
- Establece política de sesión STATELESS (sin sesiones)
- Registra el `JwtAuthenticationFilter` antes del filtro de autenticación por defecto

#### 2. **JwtTokenProvider** (`security/JwtTokenProvider.java`)
- **Genera tokens JWT**: Crea tokens firmados con el secreto configurado
- **Valida tokens**: Verifica que el token sea válido y no esté expirado
- **Extrae información**: Obtiene el email del usuario desde el token
- Usa la librería `jjwt` para manejar los tokens

#### 3. **JwtAuthenticationFilter** (`security/JwtAuthenticationFilter.java`)
- **Intercepta cada request**: Se ejecuta antes de llegar a los controladores
- **Extrae el token**: Busca el header `Authorization: Bearer <token>`
- **Valida y autentica**: Si el token es válido, carga el usuario y lo autentica en el contexto de seguridad
- Permite que Spring Security reconozca al usuario sin necesidad de sesión

#### 4. **CustomUserDetailsService** (`security/CustomUserDetailsService.java`)
- Implementa `UserDetailsService` de Spring Security
- **Carga usuarios**: Busca el usuario en la BD por email
- **Construye UserDetails**: Crea el objeto que Spring Security usa para autenticación
- **Asigna roles**: Convierte el enum `Role` de la entidad User a `GrantedAuthority`

### Flujo de Autenticación

1. **Login** (`POST /api/v1/auth/login`):
   - Usuario envía email y contraseña
   - Se valida contra la BD
   - Si es correcto, se genera un JWT con el email del usuario
   - Se devuelve el token al cliente

2. **Requests Autenticados**:
   - Cliente envía el token en el header: `Authorization: Bearer <token>`
   - `JwtAuthenticationFilter` intercepta el request
   - Extrae y valida el token
   - Carga el usuario desde la BD usando el email del token
   - Establece la autenticación en el contexto de Spring Security
   - El request continúa al controlador con el usuario autenticado

3. **Expiración**:
   - Los tokens expiran después de 24 horas (configurable)
   - Cuando expira, el cliente debe hacer login nuevamente

### Seguridad por Roles

- **ROLE_ADMIN**: Acceso total, puede gestionar usuarios
- **ROLE_USER**: Solo accede a sus propios datos

Los roles se validan con `@PreAuthorize("hasRole('ADMIN')")` en los controladores.

## Endpoints

### Autenticación (`/api/v1/auth`)
- `POST /login` - Iniciar sesión
- `POST /register` - Registrar usuario (solo ADMIN)
- `POST /forgot-password` - Solicitar recuperación de contraseña
- `POST /reset-password` - Resetear contraseña con token
- `POST /change-password` - Cambiar contraseña (usuario autenticado)

### Transacciones (`/api/v1/transactions`)
- `POST /` - Crear transacción
- `GET /{id}` - Obtener transacción por ID
- `GET /` - Listar todas las transacciones del usuario
- `PUT /{id}` - Actualizar transacción
- `DELETE /{id}` - Eliminar transacción

### Categorías (`/api/v1/categories`)
- `POST /` - Crear categoría
- `GET /{id}` - Obtener categoría por ID
- `GET /` - Listar todas las categorías del usuario
- `PUT /{id}` - Actualizar categoría

### Deudas de Clientes (`/api/v1/customer-debts`)
- `POST /` - Crear deuda de cliente
- `GET /{id}` - Obtener deuda por ID
- `GET /` - Listar todas las deudas del usuario
- `PUT /{id}` - Actualizar deuda
- `DELETE /{id}` - Eliminar deuda

### Usuarios (`/api/v1/users`) - Solo ADMIN
- `GET /{id}` - Obtener usuario por ID
- `GET /` - Listar todos los usuarios
- `PUT /{id}` - Actualizar usuario
- `DELETE /{id}` - Eliminar usuario

## Documentación API (Swagger)

Una vez que la aplicación esté corriendo, puedes acceder a la documentación interactiva de la API:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs (JSON)**: http://localhost:8080/v3/api-docs
- **Health Check**: http://localhost:8080/api/v1/health

La documentación incluye:
- Todos los endpoints disponibles organizados por categorías
- Esquemas de request/response con validaciones
- Posibilidad de probar los endpoints directamente desde el navegador
- Autenticación JWT integrada (usa el botón "Authorize" después de hacer login)

### Endpoints Disponibles

#### 🔐 Autenticación (`/api/v1/auth`)
- `POST /login` - Iniciar sesión
- `POST /register` - Registrar usuario (solo ADMIN)
- `POST /forgot-password` - Recuperar contraseña
- `POST /reset-password` - Resetear contraseña
- `POST /change-password` - Cambiar contraseña

#### 💰 Transacciones (`/api/v1/transactions`)
- `POST /` - Crear transacción
- `GET /{id}` - Obtener por ID
- `GET /` - Listar todas
- `PUT /{id}` - Actualizar
- `DELETE /{id}` - Eliminar

#### 🏷️ Categorías (`/api/v1/categories`)
- `POST /` - Crear categoría
- `GET /{id}` - Obtener por ID
- `GET /` - Listar todas
- `PUT /{id}` - Actualizar

#### 📋 Deudas de Clientes (`/api/v1/customer-debts`)
- `POST /` - Crear deuda
- `GET /{id}` - Obtener por ID
- `GET /` - Listar todas
- `PUT /{id}` - Actualizar
- `DELETE /{id}` - Eliminar

#### 👥 Usuarios (`/api/v1/users`) - Solo ADMIN
- `GET /{id}` - Obtener por ID
- `GET /` - Listar todos
- `PUT /{id}` - Actualizar
- `DELETE /{id}` - Eliminar

#### ✅ Health Check (`/api/v1/health`)
- `GET /` - Estado de la API
- `GET /ping` - Ping simple

### Cómo usar Swagger con JWT

1. Accede a `http://localhost:8080/swagger-ui.html`
2. Primero haz login usando el endpoint `POST /api/v1/auth/login`:
   ```json
   {
     "email": "admin@cuadernito.com",
     "password": "Admin123"
   }
   ```
3. Copia el `token` que recibes en la respuesta
4. Haz clic en el botón **"Authorize"** (arriba a la derecha)
5. Pega el token en el campo (sin la palabra "Bearer")
6. Ahora puedes probar todos los endpoints protegidos

## Próximos Pasos

1. Implementar la lógica completa en los servicios
2. Agregar validaciones de negocio
3. Implementar recuperación de contraseña con email
4. Agregar tests unitarios e integración

## Notas de Desarrollo

- Las entidades **nunca** salen de la capa de Service
- Los Controllers solo reciben y devuelven DTOs
- Todos los mensajes de error al usuario están en castellano
- El código está en inglés
- La validación de que una categoría pertenece al usuario se hace en el servicio
