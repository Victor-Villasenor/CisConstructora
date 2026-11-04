# CisConstructora 2.0

Sistema de escritorio (Java Swing + MySQL) para controlar las obras de una constructora de casas habitación.

## Roles

| Rol | Entra a | Qué hace |
|---|---|---|
| Dueño | Proyectos | Registra obras con los datos del cliente y elige al Supervisor |
| Administrador | Inventario | Maneja materiales, maquinaria y empleados |
| Supervisor | Mi obra | Ve su obra asignada y reporta el avance |

> En esta versión ya no existen Finanzas ni presupuestos. La cubicación y el bloqueo de salidas de material van en la siguiente etapa.

## Cómo correrlo

1. Ejecuta `cisconstructora_bd.sql` en MySQL (crea la base `cisconstructora` y las cuentas de prueba).
2. Revisa usuario y contraseña de MySQL en `src/conexion/conexionBD.java`.
3. Compila y corre `controlador.App` (o `vista.frontend`) con `lib/mysql-connector-j-9.7.0.jar` en el classpath:

```
javac -encoding UTF-8 -cp lib/mysql-connector-j-9.7.0.jar -d out $(find src -name "*.java")
java -cp out:lib/mysql-connector-j-9.7.0.jar controlador.App
```

## Cuentas de prueba

| Correo | Contraseña |
|---|---|
| dueno@cisconstructora.com | dueno123 |
| admin@cisconstructora.com | admin123 |
| supervisor@cisconstructora.com | supervisor123 |

Los botones de cuentas de prueba del login se apagan poniendo `MOSTRAR_DEMO = false` en `src/vista/frontend.java`.


## Diseño y logo

- Todo el diseño compartido (colores, íconos, encabezado, menú lateral, tablas, campos y botones) está en `src/vista/Tema.java` y `src/vista/Chrome.java`. Si quieres cambiar un color o una fuente, se hace ahí y se refleja en todas las pantallas.
- Los íconos del menú, la campana y el usuario se dibujan con vectores, por eso se ven nítidos en cualquier tamaño o zoom de Windows.
- **Logo:** deja el archivo original `CisConstructora.jpg` (o `CisConstructora.png`) dentro de `src/vista/`. El sistema lo usa solo y le quita el fondo blanco al cargarlo. Si no existe, usa `LogoSinFondo.png`. Entre más grande sea la imagen (mínimo 1000 px de ancho), mejor se ve.

## Cubicación (módulo del Supervisor)
El Supervisor entra a **Cubicación** desde el menú lateral, captura las medidas de su obra y el sistema calcula en vivo el material por etapa
(cimentación, muros, enjarre y techo): block/ladrillo, cemento, arena, grava y agua. Al guardar queda como la cubicación vigente de la obra;
si ya había una, la anterior pasa al historial con fecha y usuario. Requiere la tabla `cubicaciones` (ya incluida en `cisconstructora_bd.sql`).
