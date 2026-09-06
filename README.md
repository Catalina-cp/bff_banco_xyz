
---

## Los 3 BFF

### 1. BFF Web (puerto 8081)

Optimizado para navegadores: expone los datos **completos** de cada cuenta, pensado para un dashboard rico en información.

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/web/cuentas` | Lista todas las cuentas con todos sus campos |
| GET | `/api/web/cuentas/{id}` | Detalle completo de una cuenta |

**Campos devueltos:** `cuentaId`, `nombre`, `saldo`, `edad`, `tipo`, `interesCalculado`, `saldoFinal`.

### 2. BFF Mobile (puerto 8082)

Respuestas **livianas**, con solo los datos esenciales, para reducir el consumo de datos móviles y acelerar la carga en la app.

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/mobile/cuentas` | Lista todas las cuentas (formato resumido) |
| GET | `/api/mobile/cuentas/{id}` | Detalle resumido de una cuenta |

**Campos devueltos:** solo `cuentaId`, `nombre`, `saldoFinal` (3 de los 7 campos disponibles).

### 3. BFF Cajero Automático (puerto 8083)

Interfaz mínima y segura para operaciones críticas: consulta de saldo y retiro, con validaciones estrictas dado que involucra movimiento real de dinero.

| Método | Endpoint | Descripción |
|---|---|---|
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

## Autenticación y autorización por canal

Cada BFF implementa autenticación por **API Key**, enviada en el header `X-API-KEY`, mapeada a un rol específico mediante Spring Security. Esto satisface el requisito de "gestionar autenticación y autorización específicas para cada canal": cada API Key solo es válida para su propio backend, y cada backend solo acepta su propia key.

| Canal | API Key | Rol asignado |
|---|---|---|
| Web | `web-secret-123` | `ROLE_WEB` |
| Mobile | `mobile-secret-456` | `ROLE_MOBILE` |
| ATM | `atm-secret-789` | `ROLE_ATM` |

Una petición sin la API Key correcta recibe `403 Forbidden`. No hay login de usuario/contraseña: es autenticación máquina-a-máquina, apropiada para un backend consumido por una app o un dispositivo (no una persona ingresando credenciales).

**Ejemplo de petición autenticada (cURL):**
```bash
curl -H "X-API-KEY: web-secret-123" http://localhost:8081/api/web/cuentas
```

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

Usa Postman (o cURL) agregando el header `X-API-KEY` correspondiente a cada canal, como se detalla en la sección de autenticación.

---

## Notas técnicas

- Los 3 proyectos reutilizan intencionalmente el mismo modelo `Interest` y tabla `interests`, ya que representan la misma información de negocio (cuentas y saldos) vista desde 3 perspectivas distintas — así es como funciona el patrón BFF en la práctica: mismos datos de origen, distintas formas de exponerlos según el consumidor.
- Se usó `spring.jpa.hibernate.ddl-auto=none` en los 3 módulos para evitar que Hibernate intente modificar el schema, ya que las tablas pertenecen y son gestionadas por el proyecto batch.