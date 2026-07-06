# EduCore - Instrucciones de compilación, ejecución y uso

## Requisitos

Para compilar y ejecutar el proyecto se requiere:

- Java JDK 21 o superior.
- Apache Maven 3.9 o superior.
- NetBeans IDE 29 (recomendado).

## Compilación

Desde la carpeta raíz del proyecto ejecutar:

```bash
mvn clean install
```

También puede compilarse desde NetBeans utilizando la opción **Clean and Build**.

## Ejecución

Para ejecutar el sistema desde NetBeans:

1. Abrir el proyecto EduCore.
2. Esperar a que Maven descargue las dependencias.
3. Ejecutar la clase `Main.java` o presionar el botón **Run**.

También puede ejecutarse desde consola con:

```bash
mvn exec:java
```

## Pruebas

Para ejecutar las pruebas del proyecto:

```bash
mvn test
```

## Verificación del formato

Para comprobar el formato del código fuente:

```bash
mvn fmt:check
```

## Uso del sistema

Al iniciar la aplicación se presenta el siguiente menú principal:

- Gestión de Estudiantes.
- Gestión de Empleados.
- Gestión Académica.

Dentro del módulo de Gestión Académica se encuentran las opciones:

- Gestión de Edificios.
- Gestión de Aulas.
- Gestión de Secciones.

Cada módulo permite registrar, consultar, actualizar y eliminar la información correspondiente.

## Versión de entrega

La entrega final corresponde al tag:

**v1.0-p1**
