# 🔐 Explicación Completa: JWT y Autenticación en Spring Security

## 📚 Índice
1. [¿Qué es JWT?](#qué-es-jwt)
2. [Arquitectura General](#arquitectura-general)
3. [Componentes del Sistema](#componentes-del-sistema)
4. [Flujo Completo de Autenticación](#flujo-completo-de-autenticación)
5. [Explicación Detallada de Cada Clase](#explicación-detallada-de-cada-clase)

---

## ¿Qué es JWT?

**JWT (JSON Web Token)** es un estándar para transmitir información de forma segura entre dos partes. Es como un "pase de acceso" que contiene información del usuario.

### Estructura de un JWT:
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNjE2MjM5MDIyLCJleHAiOjE2MTYzMjU0MjJ9.signature
```

Un JWT tiene 3 partes separadas por puntos (.):

1. **Header** (Encabezado): Tipo de token y algoritmo de firma
2. **Payload** (Carga útil): Datos del usuario (email, roles, etc.)
3. **Signature** (Firma): Firma criptográfica que garantiza que el token no fue modificado

### Ventajas de JWT:
- ✅ **Stateless**: El servidor no guarda sesiones
- ✅ **Escalable**: Funciona en múltiples servidores sin compartir sesiones
- ✅ **Portable**: El cliente puede usar el token en diferentes servicios
- ✅ **Seguro**: Firmado criptográficamente

---

## Arquitectura General

```
┌─────────────┐
│   Cliente   │
│  (Frontend) │
└──────┬──────┘
       │
       │ 1. POST /api/v1/auth/login
       │    { email, password }
       │
       ▼
┌─────────────────────────────────┐
│      AuthController              │
│  - Recibe credenciales           │
│  - Valida con AuthenticationManager
└──────┬──────────────────────────┘
       │
       │ 2. Valida usuario
       │
       ▼
┌─────────────────────────────────┐
│  CustomUserDetailsService       │
│  - Busca usuario en BD          │
│  - Crea UserDetails             │
└──────┬──────────────────────────┘
       │
       │ 3. Usuario válido
       │
       ▼
┌─────────────────────────────────┐
│     JwtTokenProvider            │
│  - Genera token JWT             │
│  - Firma con secret key         │
└──────┬──────────────────────────┘
       │
       │ 4. Devuelve token
       │
       ▼
┌─────────────┐
│   Cliente   │
│  Guarda token
└──────┬──────┘
       │
       │ 5. Request con token
       │    Authorization: Bearer <token>
       │
       ▼
┌─────────────────────────────────┐
│  JwtAuthenticationFilter         │
│  - Intercepta cada request       │
│  - Extrae token                  │
│  - Valida token                  │
│  - Carga usuario                 │
└──────┬──────────────────────────┘
       │
       │ 6. Usuario autenticado
       │
       ▼
┌─────────────────────────────────┐
│      Controller                 │
│  - Procesa request              │
│  - Usa Authentication           │
└─────────────────────────────────┘
```

---

## Componentes del Sistema

### 1. **SecurityConfig** - Configuración Principal
### 2. **CustomUserDetailsService** - Carga de Usuarios
### 3. **JwtTokenProvider** - Generación y Validación de Tokens
### 4. **JwtAuthenticationFilter** - Filtro de Autenticación

---

## Flujo Completo de Autenticación

### 🔵 FASE 1: LOGIN (Primera vez)

```
1. Cliente envía: POST /api/v1/auth/login
   {
     "email": "admin@cuadernito.com",
     "password": "Admin123"
   }

2. AuthController recibe el request

3. AuthenticationManager valida las credenciales:
   - Usa CustomUserDetailsService para cargar el usuario
   - Compara la contraseña (BCrypt)
   - Si es correcto, crea un objeto Authentication

4. JwtTokenProvider genera el token:
   - Toma el email del usuario
   - Crea el payload con email, fecha creación, expiración
   - Firma con la secret key
   - Devuelve el token JWT

5. Cliente recibe:
   {
     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     "type": "Bearer",
     "email": "admin@cuadernito.com",
     "role": "ROLE_ADMIN"
   }

6. Cliente guarda el token (localStorage, sessionStorage, etc.)
```

### 🟢 FASE 2: REQUESTS AUTENTICADOS (Después del login)

```
1. Cliente hace request a: GET /api/v1/transactions
   Headers:
     Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

2. JwtAuthenticationFilter intercepta ANTES de llegar al Controller:
   
   a) Extrae el token del header "Authorization"
   b) Valida el token con JwtTokenProvider.validateToken()
   c) Si es válido, extrae el email del token
   d) Carga el usuario completo con CustomUserDetailsService
   e) Crea un objeto Authentication y lo guarda en SecurityContextHolder

3. El request continúa al Controller
   - El Controller puede acceder al usuario autenticado
   - Usa Authentication.getName() para obtener el email
   - Procesa la lógica de negocio

4. Devuelve la respuesta al cliente
```

---

## Explicación Detallada de Cada Clase

---

### 1️⃣ SecurityConfig.java

**¿Qué hace?** Configura toda la seguridad de Spring Security.

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
```

#### Métodos:

##### `passwordEncoder()` - Bean de Codificación de Contraseñas
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```
**¿Qué hace?**
- Crea un codificador BCrypt para hashear contraseñas
- BCrypt es un algoritmo de hash unidireccional (no se puede revertir)
- Cuando guardas una contraseña: `$2a$10$N9qo8uLOickgx2ZMRZoMye...`
- Cuando validas: compara el hash, no la contraseña original

**Ejemplo:**
```java
// Al crear usuario:
String hashedPassword = passwordEncoder.encode("Admin123");
// Resultado: "$2a$10$N9qo8uLOickgx2ZMRZoMye..."

// Al validar login:
boolean matches = passwordEncoder.matches("Admin123", hashedPassword);
// Resultado: true si coincide
```

##### `authenticationProvider()` - Proveedor de Autenticación
```java
@Bean
public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
}
```
**¿Qué hace?**
- Configura CÓMO Spring Security va a autenticar usuarios
- Le dice: "Usa CustomUserDetailsService para cargar usuarios"
- Le dice: "Usa BCrypt para comparar contraseñas"

**Flujo:**
1. Recibe email y password
2. Llama a `userDetailsService.loadUserByUsername(email)`
3. Compara la password con `passwordEncoder.matches()`
4. Si coincide, autentica al usuario

##### `authenticationManager()` - Gestor de Autenticación
```java
@Bean
public AuthenticationManager authenticationManager(
        AuthenticationConfiguration authConfig
) throws Exception {
    return authConfig.getAuthenticationManager();
}
```
**¿Qué hace?**
- Es el "jefe" que coordina la autenticación
- Lo usas en el AuthController para hacer login
- Internamente usa el `authenticationProvider()` que configuraste

##### `filterChain()` - Configuración de Filtros y Rutas
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())  // Desactiva CSRF (no necesario con JWT)
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/v1/auth/**").permitAll()  // Rutas públicas
            .anyRequest().authenticated()  // Resto requiere autenticación
        )
        .authenticationProvider(authenticationProvider())
        .addFilterBefore(
            jwtAuthenticationFilter,
            UsernamePasswordAuthenticationFilter.class
        );
    return http.build();
}
```

**Línea por línea:**

1. **`.csrf(csrf -> csrf.disable())`**
   - Desactiva la protección CSRF
   - CSRF es para formularios HTML, con JWT no es necesario

2. **`.sessionManagement(...)`**
   - `STATELESS` = Sin sesiones
   - Cada request es independiente
   - El token JWT contiene toda la información necesaria

3. **`.authorizeHttpRequests(...)`**
   - Define qué rutas son públicas y cuáles requieren autenticación
   - `/api/v1/auth/**` = Cualquier ruta que empiece así es pública
   - `.anyRequest().authenticated()` = Todo lo demás requiere token válido

4. **`.addFilterBefore(...)`**
   - Agrega nuestro `JwtAuthenticationFilter` ANTES del filtro por defecto
   - Esto hace que el filtro JWT se ejecute primero en cada request

---

### 2️⃣ CustomUserDetailsService.java

**¿Qué hace?** Carga usuarios de la base de datos y los convierte al formato que Spring Security entiende.

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
```

#### Método Principal:

##### `loadUserByUsername(String email)`
```java
@Override
@Transactional
public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    // 1. Busca el usuario en la base de datos
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

    // 2. Convierte tu entidad User a UserDetails (formato de Spring Security)
    return org.springframework.security.core.userdetails.User.builder()
        .username(user.getEmail())           // Email como username
        .password(user.getPassword())        // Password hasheada
        .authorities(getAuthorities(user))   // Roles (ROLE_ADMIN, ROLE_USER)
        .accountExpired(false)               // Cuenta no expirada
        .accountLocked(false)                // Cuenta no bloqueada
        .credentialsExpired(false)           // Credenciales no expiradas
        .disabled(!user.getEnabled())        // Habilitado según tu entidad
        .build();
}
```

**¿Cuándo se llama?**
1. Durante el login (AuthController)
2. Cuando el JwtAuthenticationFilter necesita cargar el usuario desde el token

**Paso a paso:**
1. Recibe el email
2. Busca en la BD con `userRepository.findByEmail(email)`
3. Si no existe, lanza `UsernameNotFoundException`
4. Si existe, crea un objeto `UserDetails` con:
   - Email
   - Password (hasheada)
   - Roles (convertidos a `GrantedAuthority`)
   - Estado de la cuenta

##### `getAuthorities(User user)`
```java
private Collection<? extends GrantedAuthority> getAuthorities(User user) {
    return Collections.singletonList(
        new SimpleGrantedAuthority(user.getRole().name())
    );
}
```
**¿Qué hace?**
- Convierte tu enum `Role` (ROLE_ADMIN, ROLE_USER) a `GrantedAuthority`
- Spring Security necesita este formato para verificar permisos
- Ejemplo: `ROLE_ADMIN` → `SimpleGrantedAuthority("ROLE_ADMIN")`

---

### 3️⃣ JwtTokenProvider.java

**¿Qué hace?** Genera, valida y extrae información de los tokens JWT.

```java
@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String jwtSecret;  // Clave secreta desde application.properties

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;  // 86400000 = 24 horas
```

#### Métodos:

##### `getSigningKey()` - Obtiene la Clave de Firma
```java
private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(jwtSecret.getBytes());
}
```
**¿Qué hace?**
- Convierte tu string secreto en una clave criptográfica
- Esta clave se usa para FIRMAR y VERIFICAR tokens
- Si alguien modifica el token, la firma no coincidirá

**Ejemplo:**
```java
jwtSecret = "mi-clave-secreta-256-bits"
→ SecretKey (formato que JWT entiende)
```

##### `generateToken(Authentication authentication)` - Genera Token
```java
public String generateToken(Authentication authentication) {
    // 1. Obtiene el email del usuario autenticado
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    
    // 2. Calcula fechas
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + jwtExpirationMs);  // Ahora + 24 horas

    // 3. Construye el token JWT
    return Jwts.builder()
        .subject(userDetails.getUsername())      // Email del usuario
        .issuedAt(now)                           // Fecha de creación
        .expiration(expiryDate)                  // Fecha de expiración
        .signWith(getSigningKey())               // Firma con la clave secreta
        .compact();                              // Genera el string final
}
```

**¿Qué hace?**
1. Toma el email del usuario autenticado
2. Crea el payload del JWT con:
   - `subject`: Email del usuario
   - `issuedAt`: Fecha de creación
   - `expiration`: Fecha de expiración (24 horas después)
3. Firma el token con la clave secreta
4. Devuelve el token como string

**Ejemplo de token generado:**
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbkBjdWFkZXJuaXRvLmNvbSIsImlhdCI6MTYxNjIzOTAyMiwiZXhwIjoxNjE2MzI1NDIyfQ.signature
```

##### `getUsernameFromToken(String token)` - Extrae Email del Token
```java
public String getUsernameFromToken(String token) {
    Claims claims = Jwts.parser()
        .verifyWith(getSigningKey())        // Verifica la firma
        .build()
        .parseSignedClaims(token)           // Parsea el token
        .getPayload();                      // Obtiene el payload

    return claims.getSubject();             // Devuelve el email
}
```

**¿Qué hace?**
1. Parsea el token JWT
2. Verifica que la firma sea válida (no fue modificado)
3. Extrae el payload (datos del token)
4. Devuelve el `subject` que es el email del usuario

**Ejemplo:**
```java
String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
String email = getUsernameFromToken(token);
// Resultado: "admin@cuadernito.com"
```

##### `validateToken(String token)` - Valida el Token
```java
public boolean validateToken(String token) {
    try {
        Jwts.parser()
            .verifyWith(getSigningKey())    // Verifica la firma
            .build()
            .parseSignedClaims(token);      // Intenta parsear
        return true;                        // Si no lanza excepción, es válido
    } catch (JwtException | IllegalArgumentException e) {
        return false;                      // Si hay error, es inválido
    }
}
```

**¿Qué hace?**
1. Intenta parsear y verificar el token
2. Si la firma es inválida → `JwtException` → retorna `false`
3. Si el token expiró → `JwtException` → retorna `false`
4. Si el formato es incorrecto → `IllegalArgumentException` → retorna `false`
5. Si todo está bien → retorna `true`

**Casos de validación:**
- ✅ Token válido y no expirado → `true`
- ❌ Token modificado → `false`
- ❌ Token expirado → `false`
- ❌ Token con formato incorrecto → `false`

---

### 4️⃣ JwtAuthenticationFilter.java

**¿Qué hace?** Intercepta CADA request HTTP y valida el token JWT antes de que llegue al Controller.

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
```

**`OncePerRequestFilter`** = Se ejecuta UNA VEZ por cada request HTTP.

#### Métodos:

##### `shouldNotFilter(HttpServletRequest request)` - Excluye Rutas
```java
@Override
protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getServletPath();
    
    return path.startsWith("/api/v1/auth")      // Login, register, etc.
        || path.startsWith("/api/v1/health")     // Health check
        || path.startsWith("/swagger-ui")        // Swagger UI
        || path.startsWith("/v3/api-docs");       // API docs
}
```

**¿Qué hace?**
- Define qué rutas NO deben pasar por el filtro JWT
- Si retorna `true`, el filtro se salta (no valida token)
- Si retorna `false`, el filtro se ejecuta

**Ejemplo:**
- `/api/v1/auth/login` → `true` → No valida token (es público)
- `/api/v1/transactions` → `false` → Valida token (requiere autenticación)

##### `doFilterInternal(...)` - Lógica Principal del Filtro
```java
@Override
protected void doFilterInternal(
    HttpServletRequest request, 
    HttpServletResponse response, 
    FilterChain filterChain
) throws ServletException, IOException {
    try {
        // 1. Extrae el token del header Authorization
        String jwt = getJwtFromRequest(request);

        // 2. Si hay token Y es válido
        if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
            
            // 3. Extrae el email del token
            String username = tokenProvider.getUsernameFromToken(jwt);

            // 4. Carga el usuario completo de la BD
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 5. Crea un objeto Authentication
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(
                    userDetails,                    // Usuario
                    null,                           // Credenciales (no necesarias)
                    userDetails.getAuthorities()    // Roles
                );
            
            // 6. Agrega detalles del request (IP, etc.)
            authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
            );

            // 7. Guarda la autenticación en el contexto de Spring Security
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    } catch (Exception ex) {
        logger.error("Could not set user authentication", ex);
    }

    // 8. Continúa con el siguiente filtro o el Controller
    filterChain.doFilter(request, response);
}
```

**Paso a paso detallado:**

1. **Extrae el token:**
   ```java
   String jwt = getJwtFromRequest(request);
   // Busca: Authorization: Bearer <token>
   // Devuelve: <token> (sin "Bearer ")
   ```

2. **Valida el token:**
   ```java
   if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt))
   // Verifica que:
   // - El token no esté vacío
   // - El token sea válido (firma correcta, no expirado)
   ```

3. **Extrae el email:**
   ```java
   String username = tokenProvider.getUsernameFromToken(jwt);
   // Del token: "admin@cuadernito.com"
   ```

4. **Carga el usuario:**
   ```java
   UserDetails userDetails = userDetailsService.loadUserByUsername(username);
   // Busca en BD y crea UserDetails con roles, password, etc.
   ```

5. **Crea Authentication:**
   ```java
   UsernamePasswordAuthenticationToken authentication = ...
   // Objeto que Spring Security usa para representar un usuario autenticado
   ```

6. **Guarda en SecurityContext:**
   ```java
   SecurityContextHolder.getContext().setAuthentication(authentication);
   // Ahora cualquier parte del código puede acceder al usuario autenticado
   ```

7. **Continúa el request:**
   ```java
   filterChain.doFilter(request, response);
   // El request continúa al siguiente filtro o al Controller
   ```

##### `getJwtFromRequest(HttpServletRequest request)` - Extrae Token del Header
```java
private String getJwtFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
        return bearerToken.substring(7);  // Quita "Bearer " y devuelve solo el token
    }
    return null;
}
```

**¿Qué hace?**
- Busca el header `Authorization`
- Verifica que empiece con `"Bearer "`
- Devuelve solo el token (sin "Bearer ")

**Ejemplo:**
```
Header: Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Resultado: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## 🔄 Flujo Completo Ejemplo

### Escenario: Usuario quiere ver sus transacciones

```
1. CLIENTE hace request:
   GET /api/v1/transactions
   Headers:
     Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

2. REQUEST llega al servidor

3. JwtAuthenticationFilter intercepta:
   a) shouldNotFilter() → false (no es ruta pública)
   b) doFilterInternal() se ejecuta:
      - Extrae token: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
      - Valida token: tokenProvider.validateToken() → true
      - Extrae email: "admin@cuadernito.com"
      - Carga usuario: userDetailsService.loadUserByUsername()
      - Crea Authentication
      - Guarda en SecurityContextHolder

4. REQUEST continúa a TransactionController:
   @GetMapping
   public ResponseEntity<List<TransactionDTO>> getAllTransactions(
       Authentication authentication  // ← Spring inyecta automáticamente
   ) {
       String email = authentication.getName();  // "admin@cuadernito.com"
       // Usa el email para buscar transacciones del usuario
   }

5. RESPONSE se devuelve al cliente
```

---

## 🎯 Resumen Visual

```
┌─────────────────────────────────────────────────────────────┐
│                    REQUEST HTTP                              │
│  GET /api/v1/transactions                                   │
│  Authorization: Bearer <token>                              │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│           JwtAuthenticationFilter                            │
│  1. Extrae token del header                                │
│  2. Valida token (JwtTokenProvider)                         │
│  3. Extrae email del token                                 │
│  4. Carga usuario (CustomUserDetailsService)               │
│  5. Crea Authentication                                     │
│  6. Guarda en SecurityContextHolder                        │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│              TransactionController                           │
│  - Recibe Authentication inyectado                          │
│  - Obtiene email: authentication.getName()                  │
│  - Busca transacciones del usuario                          │
│  - Devuelve respuesta                                       │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔑 Conceptos Clave

### SecurityContextHolder
- Es como un "contenedor global" donde Spring Security guarda el usuario autenticado
- Cualquier parte del código puede acceder: `SecurityContextHolder.getContext().getAuthentication()`
- Se limpia automáticamente después de cada request

### Authentication
- Objeto que representa un usuario autenticado
- Contiene: usuario, roles, credenciales
- Se obtiene con: `Authentication authentication` (inyección automática)

### Stateless
- No hay sesiones en el servidor
- Cada request es independiente
- El token JWT contiene toda la información necesaria
- Ventaja: Escalable (múltiples servidores sin compartir sesiones)

---

## ❓ Preguntas Frecuentes

**P: ¿Por qué cargar el usuario de la BD si ya está en el token?**
R: El token solo tiene el email. Necesitas cargar roles, estado de cuenta, etc. de la BD para verificar permisos actualizados.

**P: ¿Qué pasa si el token expira?**
R: `validateToken()` retorna `false`, el filtro no autentica, y Spring Security rechaza el request con 401 Unauthorized.

**P: ¿Cómo funciona `@PreAuthorize("hasRole('ADMIN')")`?**
R: Spring Security verifica los roles del `Authentication` en `SecurityContextHolder`. Si no tiene ROLE_ADMIN, rechaza el request.

**P: ¿Por qué STATELESS?**
R: Con JWT, el servidor no guarda sesiones. El token es autosuficiente. Esto permite escalar horizontalmente sin problemas.

---

¡Espero que esta explicación te haya ayudado a entender cómo funciona todo el sistema de autenticación JWT! 🚀
