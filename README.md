# TPGrupalOO2Hito2Grupo02

Aplicación Spring Boot para la gestión de Epicentro Gourmet.

## Administración de festivales

Los usuarios con rol `ADMINISTRADOR` pueden crear, consultar, modificar y eliminar festivales en `/festivales`. Cada festival puede tener costos opcionales de superficie, montaje y electricidad, aplicables a todas las unidades o a un tipo específico.

## Crear el administrador inicial

El esquema de la base se crea/actualiza al iniciar la aplicación mediante Hibernate. Hay dos scripts disponibles para inicializar el usuario administrador:

- `seed-admin.sql`: cuenta inicial con una contraseña aleatoria.
- `seed-admin-admin.sql`: cuenta `admin` con contraseña `admin`. **Solo para desarrollo local**; restablece la contraseña de esa cuenta al ejecutarse.

Ejecuta **uno solo** desde PowerShell sobre la base `Hito2`:

```powershell
& 'C:\Program Files\MySQL\MySQL Server 26.7\bin\mysql.exe' -u root -p Hito2 --execute='source src/main/resources/db/seed-admin-admin.sql'
```

El script guarda la contraseña como hash BCrypt.
