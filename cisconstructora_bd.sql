-- ============================================================
--  CisConstructora 2.0 — Base de datos completa
--
--  Cómo usarlo: abre este archivo en MySQL Workbench (o DBeaver)
--  y ejecútalo TODO de una sola vez (botón del rayo).
--
--  OJO: este script BORRA la base "cisconstructora" si ya existía
--  y la crea de nuevo desde cero.
--
--  Parte 1: estructura (tablas)        -> necesaria
--  Parte 2: cuentas para iniciar sesión -> necesaria
--  Parte 3: datos de ejemplo           -> opcional (sirve para
--           las capturas del portafolio; bórrala si quieres la
--           base vacía)
-- ============================================================

DROP DATABASE IF EXISTS cisconstructora;
CREATE DATABASE cisconstructora CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cisconstructora;

-- ============================================================
--  PARTE 1: ESTRUCTURA
-- ============================================================

-- ---------- Usuarios (inicio de sesión con roles) ----------
CREATE TABLE usuarios (
    id_Usuario   INT AUTO_INCREMENT PRIMARY KEY,
    correo       VARCHAR(150) NOT NULL UNIQUE,
    contrasena   VARCHAR(255) NOT NULL,
    rol          VARCHAR(20)  NOT NULL,   -- 'DUENO', 'ADMINISTRADOR' o 'SUPERVISOR'
    nombre       VARCHAR(150) NOT NULL,
    activo       TINYINT(1)   NOT NULL DEFAULT 1
);

-- ---------- Proyectos (obras) ----------
-- El Dueño registra la obra con los datos del cliente y elige al Supervisor.
CREATE TABLE proyectos (
    id_Proyecto       INT AUTO_INCREMENT PRIMARY KEY,
    nombre            VARCHAR(150) NOT NULL,
    ubicacion         VARCHAR(150) NOT NULL,
    estado            VARCHAR(30)  NOT NULL DEFAULT 'En curso',  -- 'En curso', 'Completado', 'Planificación'
    progreso          INT          NOT NULL DEFAULT 0,           -- 0 a 100 (lo reporta el Supervisor)
    activo            TINYINT(1)   NOT NULL DEFAULT 1,
    cliente_nombre    VARCHAR(150) NOT NULL DEFAULT '',
    cliente_telefono  VARCHAR(20)  NOT NULL DEFAULT '',
    cliente_correo    VARCHAR(150) NOT NULL DEFAULT '',
    id_supervisor     INT NULL,                                  -- cuenta con rol SUPERVISOR
    CONSTRAINT fk_proyecto_supervisor FOREIGN KEY (id_supervisor) REFERENCES usuarios(id_Usuario)
);

-- ---------- Inventario (materiales) ----------
CREATE TABLE inventario (
    id_Material           INT AUTO_INCREMENT PRIMARY KEY,
    nombre                VARCHAR(150)  NOT NULL,
    cantidad              INT           NOT NULL,
    unidad                VARCHAR(30)   NOT NULL,   -- 'Sacos', 'Toneladas', 'Piezas'...
    precio                DECIMAL(12,2) NOT NULL,
    origen                VARCHAR(150)  NOT NULL,
    ultima_actualizacion  DATE,
    activo                TINYINT(1)    NOT NULL DEFAULT 1,
    id_proyecto           INT NULL,                 -- obra a la que se cargó (NULL = stock general)
    CONSTRAINT fk_inventario_proyecto FOREIGN KEY (id_proyecto) REFERENCES proyectos(id_Proyecto)
);

-- ---------- Maquinaria y herramientas ----------
CREATE TABLE maquinaria (
    id_Maquinaria         INT AUTO_INCREMENT PRIMARY KEY,
    descripcion           VARCHAR(150)  NOT NULL,
    tipo                  VARCHAR(20)   NOT NULL,   -- 'Pesada' o 'Manual'
    estado                VARCHAR(30)   NOT NULL,   -- 'Disponible', 'En uso', 'En reparación'
    ultimo_mantenimiento  DATE,
    activo                TINYINT(1)    NOT NULL DEFAULT 1,
    costo                 DECIMAL(12,2) NOT NULL DEFAULT 0,
    id_proyecto           INT NULL,
    CONSTRAINT fk_maquinaria_proyecto FOREIGN KEY (id_proyecto) REFERENCES proyectos(id_Proyecto)
);

-- ---------- Empleados ----------
CREATE TABLE empleados (
    id_Empleado           INT AUTO_INCREMENT PRIMARY KEY,
    nombre                VARCHAR(150)  NOT NULL,
    telefono              VARCHAR(20)   NOT NULL,
    direccion             VARCHAR(200)  NOT NULL,
    cargo                 VARCHAR(100)  NOT NULL,
    estado                VARCHAR(30)   NOT NULL,   -- 'Activo', 'Ocupado', 'Ausente'
    salario               DECIMAL(12,2) NOT NULL,
    ultima_actualizacion  DATE,
    proyecto_asignado     VARCHAR(150)  NOT NULL DEFAULT '',
    activo                TINYINT(1)    NOT NULL DEFAULT 1
);

-- ---------- Cubicaciones (cálculo de material por obra) ----------
-- Cada vez que el Supervisor guarda una cubicación se agrega un renglón nuevo
-- y el anterior queda como historial (activa = 0), con fecha y usuario.
CREATE TABLE cubicaciones (
    id_Cubicacion      INT AUTO_INCREMENT PRIMARY KEY,
    id_proyecto        INT NOT NULL,
    id_usuario         INT NOT NULL,
    fecha              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activa             TINYINT(1) NOT NULL DEFAULT 1,
    m2_totales         DECIMAL(10,2) NOT NULL,
    ml_cimentacion     DECIMAL(10,2) NOT NULL,
    ancho_cimentacion  DECIMAL(6,2)  NOT NULL DEFAULT 0.40,
    alto_cimentacion   DECIMAL(6,2)  NOT NULL DEFAULT 0.30,
    ml_muro            DECIMAL(10,2) NOT NULL,
    altura_muro        DECIMAL(6,2)  NOT NULL,
    tipo_muro          VARCHAR(20)   NOT NULL,   -- 'Block' o 'Ladrillo'
    piezas_m2          DECIMAL(6,2)  NOT NULL,
    tipo_enjarre       VARCHAR(30)   NOT NULL,   -- 'Interior y exterior', 'Solo exterior', 'Solo interior'
    espesor_techo_cm   DECIMAL(6,2)  NOT NULL,
    calidad_techo      INT           NOT NULL,   -- f'c en kg/cm2: 150, 200, 250 o 300
    agua_m2            DECIMAL(8,2)  NOT NULL,   -- litros por m2 de enjarre
    arena_m2           DECIMAL(8,2)  NOT NULL,   -- cubetas por m2 de enjarre
    cemento_m2         DECIMAL(8,2)  NOT NULL,   -- kg por m2 de enjarre
    CONSTRAINT fk_cubic_proyecto FOREIGN KEY (id_proyecto) REFERENCES proyectos(id_Proyecto),
    CONSTRAINT fk_cubic_usuario  FOREIGN KEY (id_usuario)  REFERENCES usuarios(id_Usuario)
);

-- ---------- Notificaciones (campana del encabezado) ----------
CREATE TABLE notificaciones (
    id_Notificacion  INT AUTO_INCREMENT PRIMARY KEY,
    mensaje          VARCHAR(300) NOT NULL,
    tipo             VARCHAR(30)  NOT NULL DEFAULT 'INFORMATIVA',
    rol_destino      VARCHAR(20)  NOT NULL,         -- 'DUENO', 'ADMINISTRADOR', 'SUPERVISOR' o 'TODOS'
    estado           VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
    leida            TINYINT(1)   NOT NULL DEFAULT 0,
    id_proyecto      INT NULL,
    monto            DECIMAL(12,2) NULL,
    categoria        VARCHAR(50)  NULL,
    datos_solicitud  TEXT         NULL,
    fecha            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_proyecto FOREIGN KEY (id_proyecto) REFERENCES proyectos(id_Proyecto)
);

-- ============================================================
--  PARTE 2: CUENTAS PARA INICIAR SESIÓN
-- ============================================================
INSERT INTO usuarios (correo, contrasena, rol, nombre) VALUES
('dueno@cisconstructora.com',       'dueno123',      'DUENO',         'Dueño de la Obra'),
('admin@cisconstructora.com',       'admin123',      'ADMINISTRADOR', 'Administrador General'),
('supervisor@cisconstructora.com',  'supervisor123', 'SUPERVISOR',    'Carlos Ibarra'),
('supervisor2@cisconstructora.com', 'supervisor123', 'SUPERVISOR',    'Roberto Salazar');

-- ============================================================
--  PARTE 3: DATOS DE EJEMPLO (opcional)
-- ============================================================
INSERT INTO proyectos (nombre, ubicacion, estado, progreso, cliente_nombre, cliente_telefono, cliente_correo, id_supervisor) VALUES
('Casa Los Olivos',         'Los Mochis, Sinaloa', 'En curso',      45,  'María Fernanda López', '668 123 4567', 'maria.lopez@correo.com',    3),
('Residencial Valle Verde', 'Culiacán, Sinaloa',   'Completado',    100, 'Jorge Ramírez',        '667 555 0142', 'jorge.ramirez@correo.com',  4),
('Casa Del Mar',            'Mazatlán, Sinaloa',   'Planificación', 0,   'Ana Sofía Cota',       '669 311 2288', 'anasofia.cota@correo.com',  NULL);

INSERT INTO inventario (nombre, cantidad, unidad, precio, origen, ultima_actualizacion, id_proyecto) VALUES
('Cemento gris 50 kg',     120, 'Sacos',     235.00, 'Cemex Los Mochis',    '2026-11-02', 1),
('Varilla corrugada 3/8',    8, 'Piezas',    118.50, 'Aceros del Pacífico', '2026-11-01', 1),
('Arena de río',            14, 'Toneladas', 310.00, 'Materiales Sinaloa',  '2026-10-30', 1),
('Block 15x20x40',         640, 'Piezas',     14.80, 'Bloquera Norte',      '2026-10-29', 1),
('Grava 3/4',                6, 'Toneladas', 295.00, 'Materiales Sinaloa',  '2026-10-27', NULL);

INSERT INTO maquinaria (descripcion, tipo, estado, ultimo_mantenimiento, costo, id_proyecto) VALUES
('Revolvedora 1 saco',    'Pesada', 'Disponible',    '2026-09-15', 18500.00, NULL),
('Rotomartillo Bosch',    'Manual', 'En uso',        '2026-10-02',  4200.00, 1),
('Compactadora de placa', 'Pesada', 'En reparación', '2026-08-20', 26800.00, NULL);

INSERT INTO empleados (nombre, telefono, direccion, cargo, estado, salario, ultima_actualizacion, proyecto_asignado) VALUES
('Juan Pérez Ruiz',   '668 123 4567', 'Col. Centro #12',    'Albañil',         'Ocupado', 4500.00, '2026-11-01', 'Casa Los Olivos'),
('Luis Armenta Soto', '668 555 0101', 'Fracc. Las Palmas',  'Maestro de obra', 'Ocupado', 7200.00, '2026-10-30', 'Casa Los Olivos'),
('Marco Beltrán',     '668 222 8899', 'Col. Juárez #45',    'Electricista',    'Activo',  5800.00, '2026-10-28', '');

INSERT INTO notificaciones (mensaje, tipo, rol_destino, estado, leida, id_proyecto) VALUES
('Bajo stock: Varilla corrugada 3/8 (quedan 8 piezas).', 'INFORMATIVA', 'ADMINISTRADOR', 'PENDIENTE', 0, 1),
('Casa Los Olivos reportó 45 % de avance.',              'INFORMATIVA', 'DUENO',         'PENDIENTE', 0, 1);
