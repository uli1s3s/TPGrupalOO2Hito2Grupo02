# Epicentro Gourmet – TP Grupal OO2 Hito 2

**Grupo 02** · Orientación a Objetos II

Aplicación web Spring Boot para administrar los festivales gastronómicos del predio **Epicentro Gourmet**: festivales y sus costos, unidades de venta (Food Truck y Puesto Desmontable), personal, platos, pedidos, liquidación de haberes y reportes económicos.

## Integrantes

| Integrante | Usuario de GitHub |
|---|---|
| Dylan Thomas Lopez | [@Dylan-Lopez](https://github.com/Dylan-Lopez) |
| Martín Lucarelli | [@martinlucarelli](https://github.com/martinlucarelli) |
| Ulises Izquierdo | [@uli1s3s](https://github.com/uli1s3s) |
| Valentín Hurtado | [@valenxpeke](https://github.com/valenxpeke) |


## Diagrama de clases

**[Ver diagrama de clases UML](https://drive.google.com/file/d/14-wxRBTZ9226GsmYlrRVA_HGJxptK0Yj/view?usp=sharing)**

## Excel con Casos de Uso

**[Ver Excel de Casos de Uso](https://docs.google.com/spreadsheets/d/1jY5EijAJSyeqUxUD4QP0P7rhF41nYdRb/edit?usp=sharing&ouid=111084632132324747103&rtpof=true&sd=true)**

## Tecnologías

- Java 17
- Spring Boot 3.3.12 (Web, Data JPA, Security, Validation, Mail, Thymeleaf)
- Hibernate con MySQL (`ddl-auto: update`)
- Thymeleaf + Bootstrap
- Maven (incluye `mvnw`)

## Requisitos

- JDK 17
- MySQL 8 (o MariaDB) y MySQL Workbench
- Eclipse / Spring Tools (o cualquier IDE con soporte Maven)
- Opcional: una cuenta de Gmail con **contraseña de aplicación** para el envío real de mails

## Configuración

La aplicación lee la conexión a la base y la cuenta de mail desde **variables de entorno**, así ninguna credencial queda en el repositorio.

| Variable | Obligatoria | Ejemplo | Para qué |
|---|---|---|---|
| `DB_URL` | Sí | `jdbc:mysql://localhost:3306/tp_oo2_grupo02` | Conexión a la base |
| `USERNAME` | Sí | `root` | Usuario de MySQL |
| `PASSWORD` | Sí | `tu_clave` | Contraseña de MySQL |
| `MAIL_USERNAME` | No | `epicentrogourmet.grupo2@gmail.com` | Cuenta desde la que se envían los mails |
| `MAIL_PASSWORD` | No | `jdwx ppei nvtr vlfw` | Contraseña de aplicación de Gmail |

Sin `MAIL_USERNAME` y `MAIL_PASSWORD` la aplicación funciona igual: los mails (credenciales, códigos de verificación, avisos) **se muestran en la consola** en lugar de enviarse.

**En Eclipse:** clic derecho en el proyecto → *Run As → Run Configurations…* → la configuración de la app → pestaña *Environment* → agregar las variables.

## Cómo ejecutar

1. Crear la base de datos:
   ```sql
   CREATE DATABASE tp_oo2_grupo02;
   ```
2. Cargar las variables de entorno (ver arriba).
3. Instalar las dependencias (Maven las descarga solo):
   - Eclipse: clic derecho en el proyecto → *Maven → Update Project…*, o
   - consola: `./mvnw clean install -DskipTests` (en Windows `mvnw.cmd clean install -DskipTests`).
4. Ejecutar la aplicación una vez para que Hibernate cree las tablas:
   - Eclipse: *Run As → Spring Boot App* sobre `GhsicilianotfiApplication`, o
   - consola: `./mvnw spring-boot:run` (en Windows `mvnw.cmd spring-boot:run`).
5. Ejecutar el script de datos de prueba `src/main/resources/db/datos-prueba.sql` sobre la base. **Borra y vuelve a cargar los datos**: es solo para desarrollo y defensa.
6. Abrir [http://localhost:8080](http://localhost:8080).

## Usuarios de prueba

Cargados por `datos-prueba.sql`. Todas las contraseñas de prueba son `1234`, salvo la del administrador.

| Rol | Usuario | Contraseña |
|---|---|---|
| Administrador | `admin` | `admin` |
| Responsable | `resp1@epicentro.com` … `resp10@epicentro.com` | `1234` |
| Empleado | `nombre.apellido@epicentro.com` (ej. `juan.perez@epicentro.com`) | `1234` |

El script también carga:

- **2 festivales:** *Festival de Invierno 2026* (inactivo) y *Festival de Primavera 2026* (activo), con 7 costos configurados para cada uno.
- **10 unidades de venta:** 6 Food Trucks y 4 Puestos Desmontables, cada una con su responsable.
- **23 empleados:** entre cocineros y cajeros, incluyendo 2 disponibles para asignar y 1 dado de baja.
- **33 platos:** distribuidos entre las distintas unidades, incluyendo platos inactivos para probar los filtros.
- **201 pedidos y 402 detalles de pedidos**, con precios de venta y costos históricos para probar consultas, filtros, ranking y rentabilidad.
- **27 liquidaciones de haberes**, correspondientes a distintos empleados y períodos, utilizadas también para consultar el historial laboral y los recibos de sueldo.
- **3 cierres de caja históricos:** 2 cerrados y 1 reabierto, para probar la consulta y el funcionamiento del cierre de caja.
- **34 usuarios de prueba**, distribuidos entre administrador, responsables y empleados.
- **3 roles y 3 categorías de cocinero.**

Al final del script se incluyen consultas SQL de verificación para comprobar las cantidades de registros, las liquidaciones, los cierres de caja y los pedidos por fecha y unidad.

## Casos de uso implementados, responsables y Pull Requests

Cada caso de uso se desarrolló en su propia rama y se integró a `develop` con un Pull Request.

### Dylan Thomas Lopez

| Caso de uso | PR |
|---|---|
| Entidad abstracta `Personal` con herencia JOINED | [#1](https://github.com/uli1s3s/TPGrupalOO2Hito2Grupo02/pull/1) |
| Categoría de cocinero (entidad, repositorio, servicio, DTO y API REST) | [#2](https://github.com/uli1s3s/TPGrupalOO2Hito2Grupo02/pull/2) |
| Cocinero (entidad, repositorio, servicio, DTO y API REST) | [#3](https://github.com/uli1s3s/TPGrupalOO2Hito2Grupo02/pull/3) |
| Cajero (entidad, repositorio, servicio, DTO y API REST) | [#4](https://github.com/uli1s3s/TPGrupalOO2Hito2Grupo02/pull/4) |
| ABM de empleados con envío de credenciales por mail | [#5](https://github.com/uli1s3s/TPGrupalOO2Hito2Grupo02/pull/5) |
| Liquidación de haberes | [#10](https://github.com/uli1s3s/TPGrupalOO2Hito2Grupo02/pull/10) |
| Recibo de sueldo con descarga en PDF | [#18](https://github.com/uli1s3s/TPGrupalOO2Hito2Grupo02/pull/18) |

### Martín Lucarelli

| Caso de uso | PR |
|---|---|
| ABM de festivales y sus costos | [#13](https://github.com/uli1s3s/TPGrupalOO2Hito2Grupo02/pull/13), [#16](https://github.com/uli1s3s/TPGrupalOO2Hito2Grupo02/pull/16) |
| ABM de platos y plato estrella | [#15](https://github.com/uli1s3s/TPGrupalOO2Hito2Grupo02/pull/15) |

### Ulises Izquierdo

| Caso de uso | PR / Rama |
|---|---|
| Registro y listado de pedidos con su detalle (Alta Pedido) | [#6](https://github.com/uli1s3s/TPGrupalOO2Hito2Grupo02/pull/6) |
| Integración y actualización de pantallas HTML | [#9](https://github.com/uli1s3s/TPGrupalOO2Hito2Grupo02/pull/9) |
| Cambiar contraseña | [#12](https://github.com/uli1s3s/TPGrupalOO2Hito2Grupo02/pull/12) |
| Historial de pedidos (con precio y costo históricos) | [#26](https://github.com/uli1s3s/TPGrupalOO2Hito2Grupo02/pull/26) |
| Ranking de unidades por recaudación | [#27](https://github.com/uli1s3s/TPGrupalOO2Hito2Grupo02/pull/27) |
| Recuperar contraseña por mail | [#28](https://github.com/uli1s3s/TPGrupalOO2Hito2Grupo02/pull/28) |
| Historial laboral del empleado (festivales y unidades donde trabajó) | [Rama arreglos](https://github.com/uli1s3s/TPGrupalOO2Hito2Grupo02/tree/arreglos) |
| Consulta de asignación actual del empleado (festival activo y unidad asignada) | [Rama arreglos](https://github.com/uli1s3s/TPGrupalOO2Hito2Grupo02/tree/arreglos) |
| Cierre y reapertura de caja por parte del cajero | [Rama arreglos](https://github.com/uli1s3s/TPGrupalOO2Hito2Grupo02/tree/arreglos) |
| Mejoras en el registro de pedidos y validaciones por unidad de venta | [Rama arreglos](https://github.com/uli1s3s/TPGrupalOO2Hito2Grupo02/tree/arreglos) |

**Nota:** Las últimas cuatro funcionalidades fueron desarrolladas en la rama `arreglos` y se encuentran pendientes de integración a `develop`, con el objetivo de preservar la estabilidad de la versión principal del proyecto.

### Valentín Hurtado

| Caso de uso | PR |
|---|---|
| Unidad de venta: Food Truck y Puesto Desmontable, código único y alta | [#7](https://github.com/uli1s3s/TPGrupalOO2Hito2Grupo02/pull/7) |
| Liquidación: primera versión de la vista y el cálculo | [#8](https://github.com/uli1s3s/TPGrupalOO2Hito2Grupo02/pull/8) |
| Registro público de responsable con su unidad de venta | [#11](https://github.com/uli1s3s/TPGrupalOO2Hito2Grupo02/pull/11) |
| Unidades con mayor canon (`ReporteMayoresCanonDTO`) | [#14](https://github.com/uli1s3s/TPGrupalOO2Hito2Grupo02/pull/14) |
| Relación Unidad de venta – Festival y validación de festival activo | [#17](https://github.com/uli1s3s/TPGrupalOO2Hito2Grupo02/pull/17) |
| Canon por festival según los costos del festival | [#19](https://github.com/uli1s3s/TPGrupalOO2Hito2Grupo02/pull/19) |
| Mi unidad: asignar y quitar empleados + envío de mails por SMTP | [#20](https://github.com/uli1s3s/TPGrupalOO2Hito2Grupo02/pull/20) |
| Navegación por rol (Unidades, Mi unidad, "Quiero ser responsable") | [#21](https://github.com/uli1s3s/TPGrupalOO2Hito2Grupo02/pull/21) |
| Unidades de venta por festival (filtro del administrador) | [#22](https://github.com/uli1s3s/TPGrupalOO2Hito2Grupo02/pull/22) |
| Rentabilidad neta de una unidad en un festival (`RentabilidadNetaDTO`) | [#23](https://github.com/uli1s3s/TPGrupalOO2Hito2Grupo02/pull/23), [#24](https://github.com/uli1s3s/TPGrupalOO2Hito2Grupo02/pull/24) |
| Verificación del email del responsable con código | [#25](https://github.com/uli1s3s/TPGrupalOO2Hito2Grupo02/pull/25) |
| Reactivación de empleados y vista unificada del personal + login del responsable | [#29](https://github.com/uli1s3s/TPGrupalOO2Hito2Grupo02/pull/29) |
| Corrección del doble envío del registro | [#30](https://github.com/uli1s3s/TPGrupalOO2Hito2Grupo02/pull/30) |

## Funcionalidades por rol

### Visitante (sin iniciar sesión)

| Funcionalidad | Ruta |
|---|---|
| Inicio con los festivales y botón "Quiero ser responsable" en cada uno | `/index` |
| Registrar una unidad de venta (Food Truck o Puesto Desmontable) con su responsable, en un festival activo | `/registro-responsable` |
| Verificar el email del responsable con un código enviado por mail (reenvío incluido) | `/registro-responsable/verificar` |
| Recuperar la contraseña por mail | `/recuperar-contrasena` |
| Iniciar sesión | `/login` |

### Administrador

| Funcionalidad | Ruta |
|---|---|
| ABM de festivales y sus costos | `/festivales` |
| Unidades de venta, con filtro por festival y aviso de responsables pendientes de verificación | `/unidades` |
| Ranking de unidades por recaudación en un festival | `/ranking-unidades` |
| Unidades con mayor canon en un festival (`ReporteMayoresCanonDTO`) | `/reportes/mayor-canon` |
| Empleados | `/empleados` |
| Liquidación de haberes | `/liquidaciones` |
| Pedidos y recibos de sueldo | `/pedidos`, `/recibos` |

### Responsable

| Funcionalidad | Ruta |
|---|---|
| **Mi unidad**: canon a pagar en cada festival en el que participa | `/mi-unidad` |
| Personal del predio con su estado (libre, ocupado en otra unidad o dado de baja), búsqueda en vivo, asignar, quitar y reactivar empleados | `/mi-unidad` |
| Rentabilidad neta de su unidad en un festival (`RentabilidadNetaDTO`) | `/mi-unidad/{id}/rentabilidad` |
| ABM de empleados (con envío de credenciales por mail) | `/empleados` |
| ABM de platos, con el plato estrella marcado | `/platos` |
| Liquidación de haberes, pedidos y recibos | `/liquidaciones`, `/pedidos`, `/recibos` |

Al iniciar sesión, el responsable entra directamente a **Mi unidad**.

### Empleado

| Funcionalidad | Ruta |
|---|---|
| Registrar y consultar pedidos | `/pedidos` |
| Ver y descargar sus recibos de sueldo en PDF | `/recibos` |

Todos los usuarios con sesión pueden cambiar su contraseña en `/cambiar-contrasena`.

## Reglas de negocio principales

- **Canon por festival.** Se usan los costos que cargó el administrador en ese festival; si falta alguno, se aplica el valor por defecto del enunciado.
  - Food Truck: `superficie × costo por m² + plus por electricidad` (el plus solo si usa electricidad).
  - Puesto Desmontable: `superficie × costo por m² − minutos de montaje × descuento por minuto`, nunca menor a 0.
  - Un costo cargado solo para el otro tipo de unidad cuenta como 0.
- **Rentabilidad neta** de una unidad en un festival = `(ventas − costo de los platos vendidos) − sueldos liquidados − canon`. Ventas y costos usan el precio y el costo **guardados en cada pedido**, no los actuales del plato.
- **Código único de la unidad**: `FT` o `PD` + 8 dígitos correlativos por tipo (ej. `FT00000001`). Lo genera el sistema y se valida con una expresión regular.
- **Registro de responsable**: solo en festivales activos que no terminaron. La cuenta queda **inactiva** hasta ingresar el código de verificación: 6 dígitos, válido 15 minutos, 5 intentos como máximo. Si el responsable intenta iniciar sesión sin haber verificado, se lo lleva a la pantalla del código.
- **Empleados**: deben ser mayores de edad. Un empleado trabaja en una sola unidad. Al darlo de baja se lo desasigna de su unidad. Al reactivarlo desde *Mi unidad* recibe una contraseña temporal nueva por mail.

## Parámetros configurables

Valores por defecto que se pueden cambiar en `application.yml` sin tocar el código:

| Propiedad | Valor por defecto |
|---|---|
| `canon.costo-por-m2` | 500 |
| `canon.plus-electricidad` | 2000 |
| `canon.descuento-por-minuto-montaje` | 10 |
| `liquidacion.sueldo-base` | 100000 |
| `liquidacion.monto-antiguedad-anual` | 5000 |
| `liquidacion.plus-cocinero` | 150000 |
| `liquidacion.plus-cocinero-ayudante` | 100000 |
| `liquidacion.plus-lavaplatos` | 50000 |
| `liquidacion.dias-por-tramo` | 15 |
| `liquidacion.porcentaje-por-tramo` | 0.50 |
| `app.base-url` | `http://localhost:8080` |

## Estructura del proyecto

```
src/main/java/com/unla/ghsicilianotfi/
├── configuration/   Seguridad, parámetros de canon y liquidación, manejo de login fallido
├── controllers/     Controladores MVC (vistas Thymeleaf) y API REST (api/v1)
├── dtos/            Objetos para formularios y reportes (no se persisten)
├── entities/        Entidades JPA (herencia JOINED en UnidadVenta y Personal)
├── helpers/         Rutas de vistas y tarifa de canon
├── repositories/    Repositorios Spring Data JPA
└── services/        Interfaces y su implementación (lógica de negocio)

src/main/resources/
├── application.yml
├── db/datos-prueba.sql
├── static/          CSS, JS y Bootstrap
└── templates/       Vistas Thymeleaf
```

## Flujo de trabajo

- Una rama por caso de uso (`feature/CU<Nombre>`) o por corrección (`fix/<Nombre>`), creada desde `develop`.
- Commits chicos y descriptivos (`feat:`, `fix:`, `docs:`).
- Integración a `develop` mediante **Pull Request** revisado por el grupo.
