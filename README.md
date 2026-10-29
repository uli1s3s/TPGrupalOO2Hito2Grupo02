# TPGrupalOO2Hito2Grupo02

Aplicación Spring Boot para la gestión de Epicentro Gourmet.

## Administración de festivales

Los usuarios con rol `ADMINISTRADOR` pueden crear, consultar, modificar y eliminar festivales en `/festivales`. Cada festival puede tener costos opcionales de superficie, montaje y electricidad, aplicables a todas las unidades o a un tipo específico.

## Administración de platos

Los usuarios con rol `RESPONSABLE` pueden crear, consultar y modificar platos en `/platos`. Cada plato se asocia a una unidad de venta que pertenezca al responsable autenticado; las bajas son lógicas para conservar los platos vinculados con pedidos anteriores. En el listado, una estrella identifica el plato con mayor cantidad vendida de cada unidad del responsable.

Al crear o modificar un festival, el administrador puede asignarle unidades de venta. Al registrar pedidos, el empleado selecciona un festival y una unidad participante; todos los platos del pedido deben corresponder a esa unidad. Los pedidos anteriores a este cambio conservan sus referencias de festival y unidad vacías y no forman parte del cálculo del plato estrella. Hibernate agrega las nuevas relaciones al iniciar la aplicación (`ddl-auto: update`).

## Crear el administrador inicial

El esquema de la base se crea/actualiza al iniciar la aplicación mediante Hibernate. Hay dos scripts disponibles para inicializar el usuario administrador:

- `seed-admin.sql`: cuenta inicial con una contraseña aleatoria.
- `seed-admin-admin.sql`: cuenta `admin` con contraseña `admin`. **Solo para desarrollo local**; restablece la contraseña de esa cuenta al ejecutarse.

Ejecuta **uno solo** desde PowerShell sobre la base `Hito2`:

```powershell
& 'C:\Program Files\MySQL\MySQL Server 26.7\bin\mysql.exe' -u root -p Hito2 --execute='source src/main/resources/db/seed-admin-admin.sql'
```

El script guarda la contraseña como hash BCrypt.
