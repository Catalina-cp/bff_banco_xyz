# BFF Banco XYZ — Backend for Frontend

Implementación del patrón **Backend for Frontend (BFF)** para optimizar la comunicación entre los distintos clientes del Banco XYZ (web, aplicación móvil y cajeros automáticos), cada uno con su propio backend adaptado a sus necesidades específicas, con **HTTPS**, **autenticación** y **autorización** configuradas de forma independiente por canal.

Datos de origen basados en: [github.com/KariVillagran/bank_legacy_data](https://github.com/KariVillagran/bank_legacy_data)

Continuidad del proyecto de migración batch: [github.com/Catalina-cp/bank_legacymigration](https://github.com/Catalina-cp/bank_legacymigration) (misma base de datos `bank_batch`, poblada por ese proyecto).

---

## Estrategia de implementación elegida

Se evaluaron dos estrategias posibles para implementar el patrón BFF:

1. **Un solo backend con lógica condicional por canal** (ej. un parámetro que indica si la petición viene de web/móvil/cajero).
2. **Un backend independiente por cada canal** (3 aplicaciones Spring Boot separadas).

Se eligió la **opción 2 (backends independientes)** porque:

- Es fiel al patrón BFF real: cada canal tiene su propio ciclo de vida, se puede desplegar, escalar y modificar sin afectar a los demás.
- Evita lógica condicional compleja y difícil de mantener dentro de un único backend.
- Permite definir seguridad, validaciones y formato de respuesta completamente distintos por canal, sin comprometer a los otros.
- El enunciado lo pide explícitamente: *"cada cliente deberá tener su propio Backend"*.

Los 3 backends viven en el mismo repositorio (organizados como 3 proyectos Maven independientes) para facilitar la entrega y el mantenimiento conjunto, pero son ejecutables y desplegables por separado.

---

## Tecnologías

- **Java 21**
- **Spring Boot 4.1.0**
- **Spring Web** (APIs REST)
- **Spring Data JPA** (acceso a datos)
- **Spring Security** (autenticación y autorización)
- **JJWT 0.12.6** (generación y validación de tokens JWT)
- **HTTPS/TLS** con certificado autofirmado
- **MySQL / MariaDB** (reutiliza la base `bank_batch` del proyecto batch)
- **Maven**

---

## Estructura del proyecto

```
bff-banco-xyz/
├── bff-web/       → BFF para navegadores (puerto 8081)
├── bff-mobile/    → BFF para apps móviles (puerto 8082)
├── bff-atm/       → BFF para cajeros automáticos (puerto 8083)
└── .gitignore
```

Cada módulo tiene la misma estructura interna:

```
src/main/java/com/bank/<modulo>/
├── model/         # Entidad JPA mapeada a la tabla `interests`
├── repository/    # Interfaz Spring Data JPA
├── dto/           # Objetos de respuesta/petición específicos del canal
├── controller/    # Endpoints REST + AuthController (emisión de tokens)
└── security/      # JwtUtil, JwtAuthFilter, SecurityConfig

src/main/resources/
├── application.properties   # Puerto, BD, API Key, HTTPS, JWT
└── keystore.p12              # Certificado autofirmado para HTTPS
```

---

## Los 3 BFF

### 1. BFF Web (puerto 8081)

Optimizado para navegadores: expone los datos **completos** de cada cuenta, pensado para un dashboard rico en información.

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/auth/token` | Entrega un JWT a cambio de la API Key del canal |
| GET | `/api/web/cuentas` | Lista todas las cuentas con todos sus campos |
| GET | `/api/web/cuentas/{id}` | Detalle completo de una cuenta |

**Campos devueltos:** `cuentaId`, `nombre`, `saldo`, `edad`, `tipo`, `interesCalculado`, `saldoFinal`.

### 2. BFF Mobile (puerto 8082)

Respuestas **livianas**, con solo los datos esenciales, para reducir el consumo de datos móviles y acelerar la carga en la app.

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/auth/token` | Entrega un JWT a cambio de la API Key del canal |
| GET | `/api/mobile/cuentas` | Lista todas las cuentas (formato resumido) |
| GET | `/api/mobile/cuentas/{id}` | Detalle resumido de una cuenta |

**Campos devueltos:** solo `cuentaId`, `nombre`, `saldoFinal` (3 de los 7 campos disponibles).

### 3. BFF Cajero Automático (puerto 8083)

Interfaz mínima y segura para operaciones críticas: consulta de saldo y retiro, con validaciones estrictas dado que involucra movimiento real de dinero.

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/auth/token` | Entrega un JWT a cambio de la API Key del canal |
| GET | `/api/atm/cuentas/{id}/saldo` | Consulta solo el saldo disponible (sin nombre ni otros datos) |
| POST | `/api/atm/cuentas/{id}/retiro` | Realiza un retiro, con validaciones |

**Validaciones del retiro:**
- El monto debe ser mayor a 0 (si no, responde `400 Bad Request`).
- El monto no puede superar el saldo disponible (si no, responde `409 Conflict` con el saldo actual).
- Si es válido, descuenta el monto y persiste el nuevo saldo en la base de datos.

**Body de la petición de retiro:**
```json
{ "monto": 500 }
```

---

## Seguridad: HTTPS, Autenticación y Autorización

### HTTPS

Los 3 BFF corren exclusivamente sobre **HTTPS**, usando un certificado autofirmado (`keystore.p12`) generado con `keytool`:

```bash
keytool -genkeypair -alias bff -keyalg RSA -keysize 2048 -storetype PKCS12 \
  -keystore keystore.p12 -validity 3650 \
  -dname "CN=localhost, OU=BancoXYZ, O=BancoXYZ, L=Santiago, ST=RM, C=CL" \
  -storepass bff123456
```

> **Nota:** por tratarse de un entorno de desarrollo/académico, el certificado es autofirmado (no emitido por una entidad certificadora reconocida). Por eso los clientes HTTP (como Postman) muestran una advertencia de "certificado no confiable" al conectarse — es un comportamiento esperado y no un error. En un entorno de producción real, este certificado sería reemplazado por uno emitido por una CA válida (ej. Let's Encrypt).

Todas las URLs de los 3 backends usan el esquema `https://` (ej. `https://localhost:8081/...`).

### Autenticación por API Key → JWT

Cada canal tiene su propia **API Key**, que se intercambia por un **token JWT** en el endpoint `/auth/token`. A partir de ahí, todas las peticiones a los endpoints protegidos deben incluir ese token.

| Canal | API Key | Rol asignado en el token |
|---|---|---|
| Web | `web-secret-123` | `ROLE_WEB` |
| Mobile | `mobile-secret-456` | `ROLE_MOBILE` |
| ATM | `atm-secret-789` | `ROLE_ATM` |

**Flujo completo:**

1. **Obtener el token** (una vez, con la API Key):
```bash
curl -k -X POST https://localhost:8081/auth/token \
  -H "X-API-KEY: web-secret-123"
```
Respuesta:
```json
{ "token": "eyJhbGciOiJIUzI1NiJ9...", "tipo": "Bearer", "expiraEn": "1 hora" }
```

2. **Usar el token** en cada petición posterior:
```bash
curl -k https://localhost:8081/api/web/cuentas \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

Los tokens expiran a la hora de haber sido emitidos, y cada canal firma sus tokens con una clave secreta distinta — un token generado por `bff-web` no es válido en `bff-mobile` ni en `bff-atm`, y viceversa.

### Autorización por rol

Cada backend, mediante Spring Security, solo acepta tokens que contengan **su propio rol**:

- `bff-web` exige `ROLE_WEB` en las rutas `/api/web/**`
- `bff-mobile` exige `ROLE_MOBILE` en las rutas `/api/mobile/**`
- `bff-atm` exige `ROLE_ATM` en las rutas `/api/atm/**`

Cualquier petición sin token, con token expirado, o con el rol incorrecto, recibe `403 Forbidden`. El único endpoint público (sin autenticación) es `/auth/token`, ya que ahí es donde se obtiene el token en primer lugar.

---

## Modelo de datos

Los 3 BFF leen (y en el caso de ATM, también escriben) sobre la misma tabla `interests` de la base de datos `bank_batch`, creada y poblada por el proyecto de migración batch. No se modifica el schema desde estos proyectos (`spring.jpa.hibernate.ddl-auto=none`).

---

## Cómo ejecutar el proyecto

### 1. Requisitos previos

- Java 21 instalado
- MySQL/MariaDB corriendo localmente, con la base `bank_batch` ya creada y poblada (ver el proyecto [bank_legacymigration](https://github.com/Catalina-cp/bank_legacymigration))
- Postman (u otra herramienta similar) para probar los endpoints

### 2. Configurar la conexión

En cada módulo (`bff-web`, `bff-mobile`, `bff-atm`), edita `src/main/resources/application.properties` con tus credenciales de MySQL:

```properties
spring.datasource.url=jdbc:mariadb://localhost:3306/bank_batch
spring.datasource.username=root
spring.datasource.password=TU_PASSWORD
```

### 3. Compilar y ejecutar cada módulo

Cada módulo es un proyecto Maven independiente. Desde la carpeta de cada uno:

```bash
mvn spring-boot:run
```

Los 3 pueden correr **simultáneamente** (en terminales distintas), ya que usan puertos diferentes (8081, 8082, 8083).

### 4. Probar los endpoints

1. Pide un token en `POST https://<host>:<puerto>/auth/token` con el header `X-API-KEY` correspondiente al canal.
2. Usa ese token en el header `Authorization: Bearer <token>` para llamar a los endpoints de datos.

Si usas Postman y el certificado autofirmado genera una advertencia SSL, desactiva "SSL certificate verification" en la configuración de Postman (Settings → General), ya que es un certificado de desarrollo, no emitido por una CA pública.

---

## Notas técnicas

- Los 3 proyectos reutilizan intencionalmente el mismo modelo `Interest` y tabla `interests`, ya que representan la misma información de negocio (cuentas y saldos) vista desde 3 perspectivas distintas — así es como funciona el patrón BFF en la práctica: mismos datos de origen, distintas formas de exponerlos según el consumidor.
- Se usó `spring.jpa.hibernate.ddl-auto=none` en los 3 módulos para evitar que Hibernate intente modificar el schema, ya que las tablas pertenecen y son gestionadas por el proyecto batch.
- Cada canal firma sus JWT con una clave secreta distinta (`jwt.secret` en cada `application.properties`), reforzando el aislamiento entre canales: comprometer un canal no compromete a los demás.