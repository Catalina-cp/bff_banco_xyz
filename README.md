# BFF Banco XYZ — Backend for Frontend

Implementación del patrón **Backend for Frontend (BFF)** para optimizar la comunicación entre los distintos clientes del Banco XYZ (web, aplicación móvil y cajeros automáticos), cada uno con su propio backend adaptado a sus necesidades específicas.

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
- **Spring Security** (autenticación por API Key + autorización por rol)
- **MySQL / MariaDB** (reutiliza la base `bank_batch` del proyecto batch)
- **Maven**

---

## Estructura del proyecto