-- EduCore · esquema P2 (MariaDB)

SET NAMES utf8mb4;

-- =====================================================
-- TABLA ESTUDIANTE
-- =====================================================

CREATE TABLE estudiante (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tipo VARCHAR(20) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    carnet VARCHAR(50) NOT NULL,
    porcentaje_beca DECIMAL(3,2) NULL,

    CONSTRAINT uq_estudiante_email UNIQUE (email),
    CONSTRAINT uq_estudiante_carnet UNIQUE (carnet)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- =====================================================
-- DATOS SEMILLA DE ESTUDIANTES
-- =====================================================

INSERT INTO estudiante
(tipo, nombre, apellidos, email, carnet, porcentaje_beca)
VALUES
('REGULAR', 'Ana', 'Rojas Mora',
 'ana.rojas@uam.edu', '202410000001', NULL),

('REGULAR', 'Luis', 'Castro Vega',
 'luis.castro@uam.edu', '202410000002', NULL),

('BECADO', 'Marta', 'Solis Pena',
 'marta.solis@uam.edu', '202410000003', 0.50);


-- =====================================================
-- TABLA EMPLEADO
-- =====================================================

CREATE TABLE empleado (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    salario DECIMAL(10,2) NOT NULL,
    fecha_ingreso DATE NOT NULL,
    tipo VARCHAR(30) NOT NULL,

    CONSTRAINT uq_empleado_email UNIQUE (email)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- =====================================================
-- DATOS SEMILLA DE EMPLEADOS
-- =====================================================

INSERT INTO empleado
(nombre, apellidos, email, salario, fecha_ingreso, tipo)
VALUES
('Carlos', 'Ramirez',
 'carlos@uam.edu', 850000, '2023-01-10', 'DOCENTE'),

('Maria', 'Lopez',
 'maria@uam.edu', 720000, '2022-06-15', 'ADMINISTRATIVO');


-- =====================================================
-- TABLA EDIFICIO
-- =====================================================

CREATE TABLE edificio (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL,
    nombre VARCHAR(100) NOT NULL,

    CONSTRAINT uq_edificio_codigo UNIQUE (codigo)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- =====================================================
-- TABLA AULA
-- =====================================================

CREATE TABLE aula (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL,
    capacidad INT NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    edificio_id INT NOT NULL,

    CONSTRAINT uq_aula_codigo UNIQUE (codigo),

    CONSTRAINT fk_aula_edificio
        FOREIGN KEY (edificio_id)
        REFERENCES edificio(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- =====================================================
-- TABLA SECCION
-- =====================================================

CREATE TABLE seccion (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(30) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    docente_id INT NOT NULL,
    aula_id INT NOT NULL,

    CONSTRAINT uq_seccion_codigo UNIQUE (codigo),

    CONSTRAINT fk_seccion_docente
        FOREIGN KEY (docente_id)
        REFERENCES empleado(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_seccion_aula
        FOREIGN KEY (aula_id)
        REFERENCES aula(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- =====================================================
-- TABLA MATRICULA
-- =====================================================

CREATE TABLE matricula (
    seccion_id INT NOT NULL,
    estudiante_id INT NOT NULL,

    PRIMARY KEY (seccion_id, estudiante_id),

    CONSTRAINT fk_matricula_seccion
        FOREIGN KEY (seccion_id)
        REFERENCES seccion(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_matricula_estudiante
        FOREIGN KEY (estudiante_id)
        REFERENCES estudiante(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;