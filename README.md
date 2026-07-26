# EduCore — Sistema de Administración Universitaria
## Programación III · ISI-BSI-09 · Universidad Americana

---

## ¿Qué es este proyecto?

EduCore es el proyecto programado incremental del semestre. Lo desarrollan en
grupo a lo largo del curso en **dos entregas**, diseñando e implementando los
módulos de un sistema de administración universitaria.

Este repositorio **no es la solución**: es un **punto de partida**. Contiene la
infraestructura base y la rama de referencia `Estudiante` resuelta de punta a
punta (Model → DAO → Controller → View) como **patrón a imitar**. El resto del
sistema lo diseñan ustedes.

> Las especificaciones, requisitos, rúbrica y entregables de cada entrega viven
> en su propio enunciado dentro de `docs/`. Este README solo describe el proyecto
> y cómo ponerlo a correr.

## Requisitos

- Java 21
- Apache Maven 3.8+
- NetBeans (recomendado) o cualquier IDE compatible con Maven
- MariaDB + Docker (solo para el Proyecto 2)

## Cómo empezar

### 1. Hacer fork del repositorio

En GitHub, clic en **Fork**. Esto crea una copia en su cuenta personal.

### 2. Clonar su fork

```bash
git clone https://github.com/SU-USUARIO/educore.git
cd educore
```

### 3. Compilar y correr los tests

```bash
mvn compile
mvn test
```

Al clonar, los tests de la rama de referencia (`EstudianteRegularTest`,
`EstudianteBecadoTest`) deben pasar en **verde**. No los modifiquen.

### 4. Ejecutar la aplicación

```bash
mvn exec:java -Dexec.mainClass=edu.uam.educore.Main
```

(o ejecuten `Main` desde el IDE).

> **Nota (Proyecto 2):** si ya crearon su `.env` para Docker, `DB_HOST=db` solo resuelve dentro
> de la red de Docker Compose. Corriendo así, fuera de Docker (IDE o `mvn exec:java`), cualquier
> endpoint que consulte la base de datos va a fallar con un error de conexión — es esperado, no
> un bug. Para desarrollar así (más rápido que reconstruir la imagen en cada cambio), corran solo
> `docker compose up db` y cambien `DB_HOST` a `localhost` y el puerto a `DB_HOST_PORT` en su
> `.env` local (no el que usa Docker Compose).

### 5. Formatear antes de cada commit

No hay hook automático: **antes de cada commit** formateen sus archivos a mano.

```bash
mvn fmt:format
```

El tag de entrega debe pasar `mvn fmt:check` sin errores.

## Estructura

```
src/main/java/edu/uam/educore/
├── model/        ← entidades del dominio (personas resuelto; academico e infraestructura los crean ustedes)
├── dao/          ← repositorios (contrato Repositorio<T> + implementaciones)
├── controller/   ← coordinan validación y persistencia
├── view/         ← interacción por consola (heredan de VistaBase)
├── api/          ← servidor REST y SPA (Proyecto 2)
├── socket/       ← servidores de socket: Matrícula y Reportes (Proyecto 2)
├── util/         ← utilidades (Validador)
└── db/           ← fábrica de conexiones a la base de datos (Proyecto 2)
```

La rama `Estudiante` está completa **a propósito**: muestra cómo se conectan las
cuatro capas. Repliquen ese patrón en los módulos que diseñen.

## Flujo de Git y entregas

Cada grupo trabaja sobre su fork, con una rama por módulo, y entrega marcando un
tag anotado. El docente publica la base de cada proyecto como un tag (`v0.0`
para el Proyecto 1, `v1.0` para el Proyecto 2) y califica el código en el tag
exacto de cada entrega. Los detalles (nombre del tag, ramas, entregables y
rúbrica) están en el enunciado de cada proyecto.
