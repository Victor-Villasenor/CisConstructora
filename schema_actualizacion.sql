-- ============================================================
-- CisConstructora — Script COMPLETO desde cero
-- Ejecutar TODO de una sola vez (botón ⚡ Execute) en un servidor
-- MySQL nuevo, sin nada previo.
-- ============================================================

CREATE DATABASE IF NOT EXISTS cisconstructora;
USE cisconstructora;

-- ===================== EMPLEADOS =====================
CREATE TABLE empleados (
    id_Empleado         INT AUTO_INCREMENT PRIMARY KEY,
    nombre              VARCHAR(150) NOT NULL,
    telefono            VARCHAR(20)  NOT NULL,
    direccion           VARCHAR(200) NOT NULL,
    cargo               VARCHAR(100) NOT NULL,
    estado              VARCHAR(30)  NOT NULL,   -- 'Activo', 'Ocupado', 'Ausente'
    salario             DECIMAL(12,2) NOT NULL,
    ultima_actualizacion DATE,
    proyecto_asignado   VARCHAR(150) NOT NULL DEFAULT '',
    activo              TINYINT(1)   NOT NULL DEFAULT 1
);

-- ===================== INVENTARIO (Materiales) =====================
CREATE TABLE inventario (
    id_Material         INT AUTO_INCREMENT PRIMARY KEY,
    nombre              VARCHAR(150) NOT NULL,
    cantidad            INT          NOT NULL,
    unidad              VARCHAR(30)  NOT NULL,   -- 'Sacos', 'Toneladas', 'Piezas'
    precio              DECIMAL(12,2) NOT NULL,
    origen              VARCHAR(150) NOT NULL,
    ultima_actualizacion DATE,
    activo              TINYINT(1)   NOT NULL DEFAULT 1,
    id_proyecto         INT NULL
);

-- ===================== MAQUINARIA =====================
CREATE TABLE maquinaria (
    id_Maquinaria        INT AUTO_INCREMENT PRIMARY KEY,
    descripcion          VARCHAR(150) NOT NULL,
    tipo                 VARCHAR(20)  NOT NULL,   -- 'Pesada' o 'Manual'
    estado               VARCHAR(30)  NOT NULL,   -- 'Disponible', 'En uso', 'En reparación'
    ultimo_mantenimiento DATE,
    activo               TINYINT(1)   NOT NULL DEFAULT 1,
    costo                DECIMAL(12,2) NOT NULL DEFAULT 0,
    id_proyecto          INT NULL
);

-- ===================== PROYECTOS =====================
CREATE TABLE proyectos (
    id_Proyecto         INT AUTO_INCREMENT PRIMARY KEY,
    nombre              VARCHAR(150) NOT NULL,
    ubicacion           VARCHAR(150) NOT NULL,
    estado              VARCHAR(30)  NOT NULL,   -- 'En curso', 'Completado', 'Planificación'
    progreso            INT          NOT NULL DEFAULT 0,  -- 0 a 100
    activo              TINYINT(1)   NOT NULL DEFAULT 1,
    cliente_nombre      VARCHAR(150) NOT NULL DEFAULT '',
    cliente_telefono    VARCHAR(20)  NOT NULL DEFAULT '',
    cliente_correo      VARCHAR(150) NOT NULL DEFAULT '',
    id_supervisor       INT NULL     -- cuenta con rol SUPERVISOR (se enlaza más abajo)
);

-- Llaves foráneas (van después de crear "proyectos", por eso hasta aquí)
ALTER TABLE inventario
    ADD CONSTRAINT fk_inventario_proyecto FOREIGN KEY (id_proyecto) REFERENCES proyectos(id_Proyecto);

ALTER TABLE maquinaria
    ADD CONSTRAINT fk_maquinaria_proyecto FOREIGN KEY (id_proyecto) REFERENCES proyectos(id_Proyecto);

-- ===================== USUARIOS (login con roles) =====================
CREATE TABLE usuarios (
    id_Usuario   INT AUTO_INCREMENT PRIMARY KEY,
    correo       VARCHAR(150) NOT NULL UNIQUE,
    contrasena   VARCHAR(255) NOT NULL,
    rol          VARCHAR(20)  NOT NULL,   -- 'ADMINISTRADOR', 'DUENO' o 'SUPERVISOR'
    nombre       VARCHAR(150) NOT NULL,
    activo       TINYINT(1)   NOT NULL DEFAULT 1
);

INSERT INTO usuarios (correo, contrasena, rol, nombre) VALUES
('admin@cisconstructora.com', 'admin123', 'ADMINISTRADOR', 'Administrador General'),
('dueno@cisconstructora.com', 'dueno123', 'DUENO', 'Dueño de la Obra'),
('supervisor@cisconstructora.com', 'supervisor123', 'SUPERVISOR', 'Supervisor de Obra');

ALTER TABLE proyectos
    ADD CONSTRAINT fk_proyecto_supervisor FOREIGN KEY (id_supervisor) REFERENCES usuarios(id_Usuario);

-- ===================== NOTIFICACIONES (campana) =====================
CREATE TABLE notificaciones (
    id_Notificacion  INT AUTO_INCREMENT PRIMARY KEY,
    mensaje          VARCHAR(300) NOT NULL,
    tipo             VARCHAR(30)  NOT NULL,   -- 'INFORMATIVA'
    rol_destino      VARCHAR(20)  NOT NULL,   -- 'DUENO', 'ADMINISTRADOR', 'TODOS'
    estado           VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE', -- 'PENDIENTE','APROBADA','RECHAZADA','N/A'
    leida            TINYINT(1)   NOT NULL DEFAULT 0,
    id_proyecto      INT NULL,
    monto            DECIMAL(12,2) NULL,
    categoria        VARCHAR(50)  NULL,
    datos_solicitud  TEXT         NULL,
    fecha            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_proyecto FOREIGN KEY (id_proyecto) REFERENCES proyectos(id_Proyecto)
);

-- ============================================================
-- Si tu base de datos YA EXISTÍA (versión 1.0) y no quieres borrarla,
-- corre solo esto en lugar de todo el script:
--
--   DROP TABLE IF EXISTS finanzas;
--   ALTER TABLE proyectos
--       DROP COLUMN presupuesto,
--       DROP COLUMN gasto_comprometido,
--       ADD COLUMN cliente_nombre   VARCHAR(150) NOT NULL DEFAULT '',
--       ADD COLUMN cliente_telefono VARCHAR(20)  NOT NULL DEFAULT '',
--       ADD COLUMN cliente_correo   VARCHAR(150) NOT NULL DEFAULT '',
--       ADD COLUMN id_supervisor    INT NULL,
--       ADD CONSTRAINT fk_proyecto_supervisor FOREIGN KEY (id_supervisor) REFERENCES usuarios(id_Usuario);
--   INSERT INTO usuarios (correo, contrasena, rol, nombre) VALUES
--       ('supervisor@cisconstructora.com', 'supervisor123', 'SUPERVISOR', 'Supervisor de Obra');
-- ============================================================
