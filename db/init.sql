-- EduCore · esquema P2 (MariaDB). Se ejecuta una vez al inicializar el contenedor.
-- Herencia de Estudiante mapeada single-table (columna `tipo`).
--
-- Esqueleto mínimo a propósito: solo `estudiante` es la implementación de referencia
-- (completa y funcional, ver EstudianteRepoSql). Empleado, Edificio/Aula, Sección y
-- Matrícula son P1 de cada grupo — cada equipo diseña y agrega sus propias tablas aquí
-- (con sus FKs hacia `estudiante` donde corresponda) al completar su esquema.

SET NAMES utf8mb4;

CREATE TABLE estudiante (
  id              INT AUTO_INCREMENT PRIMARY KEY,
  tipo            VARCHAR(20)  NOT NULL,          -- 'REGULAR' | 'BECADO'
  nombre          VARCHAR(100) NOT NULL,
  apellidos       VARCHAR(100) NOT NULL,
  email           VARCHAR(150) NOT NULL,
  carnet          VARCHAR(50)  NOT NULL,
  porcentaje_beca DECIMAL(3,2) NULL               -- solo becados (0.00 .. 1.00)
);

-- ── Datos semilla ────────────────────────────────────────────────────────────
INSERT INTO estudiante (tipo, nombre, apellidos, email, carnet, porcentaje_beca) VALUES
  ('REGULAR', 'Ana',   'Rojas Mora',   'ana.rojas@uam.edu',   '202410000001', NULL),
  ('REGULAR', 'Luis',  'Castro Vega',  'luis.castro@uam.edu', '202410000002', NULL),
  ('BECADO',  'Marta', 'Solis Pena',   'marta.solis@uam.edu', '202410000003', 0.50);
