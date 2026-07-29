package edu.uam.educore.api;

import edu.uam.educore.api.Dtos.EstudianteDto;
import edu.uam.educore.api.Dtos.EdificioDto;
import edu.uam.educore.api.Dtos.EdificioRequest;
import edu.uam.educore.api.Dtos.EstudianteRequest;
import edu.uam.educore.api.Dtos.MatriculaRequest;
import edu.uam.educore.api.Dtos.AulaDto;
import edu.uam.educore.api.Dtos.AulaRequest;
import edu.uam.educore.api.Dtos.EmpleadoDto;
import edu.uam.educore.api.Dtos.EmpleadoRequest;
import edu.uam.educore.controller.EstudianteController;
import edu.uam.educore.controller.EmpleadoController;
import edu.uam.educore.dao.EstudianteRepoSql;
import edu.uam.educore.dao.EmpleadoRepoSql;
import edu.uam.educore.dao.ListaEstudianteRepo;
import edu.uam.educore.dao.Repositorio;
import edu.uam.educore.db.ConfiguracionBD;
import edu.uam.educore.model.personas.Estudiante;
import edu.uam.educore.model.personas.Empleado;
import edu.uam.educore.model.infraestructura.Aula;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import edu.uam.educore.enums.TipoAula;
import edu.uam.educore.enums.TipoEmpleado;


import edu.uam.educore.controller.EdificioController;

import edu.uam.educore.dao.EdificioRepoSql;

import edu.uam.educore.model.infraestructura.Edificio;

/**
 * Arma EduCore como app web. Estudiante corre sobre base de datos (referencia) con su controlador
 * real. Empleado, Edificio/Aula y Sección son de cada grupo (P1) — estas rutas no llaman a ningún
 * controlador de nombre fijo, ver los TODO(estudiante · P1) en cada una.
 */
public class ServidorApi {

  public static void iniciar(int puerto) throws IOException {
    Repositorio<Estudiante> estudianteRepo;
    try {
      estudianteRepo = new EstudianteRepoSql(ConfiguracionBD.desdeArchivo(".env"));
    } catch (IOException e) {
      // Sin .env legible caemos a memoria para no bloquear el arranque en desarrollo.
      estudianteRepo = new ListaEstudianteRepo();
    }

    EstudianteController estudianteController = new EstudianteController(estudianteRepo);
    Repositorio<Empleado> empleadoRepo;

try {
    empleadoRepo = new EmpleadoRepoSql(
            ConfiguracionBD.desdeArchivo(".env"));
} catch (IOException e) {
    throw new RuntimeException(
            "No fue posible inicializar EmpleadoRepoSql", e);
}

EmpleadoController empleadoController =
        new EmpleadoController(empleadoRepo);

    Repositorio<Edificio> edificioRepo;

    try {
        edificioRepo = new EdificioRepoSql(
                ConfiguracionBD.desdeArchivo(".env"));
    } catch (IOException e) {
        throw new RuntimeException(
                "No fue posible inicializar EdificioRepoSql", e);
    }

    EdificioController edificioController =
            new EdificioController(edificioRepo);

    Javalin app =
        Javalin.create(
            cfg -> {
              cfg.bundledPlugins.enableDevLogging();
              cfg.spaRoot.addFile("/", "/web/index.html");

              cfg.routes.exception(
                  IllegalArgumentException.class,
                  (e, ctx) -> ctx.status(400).json(Map.of("error", e.getMessage())));
              cfg.routes.exception(
                  Exception.class,
                  (e, ctx) -> ctx.status(500).json(Map.of("error", e.getMessage())));

              registrarEstudiantes(cfg, estudianteController);
              registrarEmpleados(cfg, empleadoController);
              registrarEdificios(cfg, edificioController);
              registrarSecciones(cfg);
              registrarMatricula(cfg);
              registrarReporte(cfg);
            });

    app.start(puerto);
    System.out.println("API EduCore escuchando en http://localhost:" + puerto);
  }

  // ── Estudiantes ──

  private static void registrarEstudiantes(JavalinConfig cfg, EstudianteController controller) {
    cfg.routes.get(
        "/api/estudiantes",
        ctx -> {
          List<EstudianteDto> lista =
              controller.listar().stream().map(EstudianteDto::desde).toList();
          ctx.json(lista);
        });

    cfg.routes.post(
        "/api/estudiantes",
        ctx -> {
          EstudianteRequest r = ctx.bodyAsClass(EstudianteRequest.class);
          Estudiante creado =
              "BECADO".equalsIgnoreCase(r.tipo())
                  ? controller.registrarBecado(
                      r.nombre(),
                      r.apellidos(),
                      r.email(),
                      r.carnet(),
                      r.porcentajeBeca() != null ? r.porcentajeBeca() : 0.0)
                  : controller.registrarRegular(r.nombre(), r.apellidos(), r.email(), r.carnet());
          ctx.status(201).json(EstudianteDto.desde(creado));
        });

    cfg.routes.put(
        "/api/estudiantes/{id}",
        ctx -> {
          int id = Integer.parseInt(ctx.pathParam("id"));
          EstudianteRequest r = ctx.bodyAsClass(EstudianteRequest.class);
          Estudiante e =
              controller.actualizar(
                  id, r.nombre(), r.apellidos(), r.email(), r.carnet(), r.porcentajeBeca());
          ctx.json(EstudianteDto.desde(e));
        });

    cfg.routes.delete(
        "/api/estudiantes/{id}",
        ctx -> {
          controller.eliminar(Integer.parseInt(ctx.pathParam("id")));
          ctx.status(204);
        });
  }

  // ── Empleados (P1 de cada grupo — sin controlador de nombre fijo) ──

  private static void registrarEmpleados(
        JavalinConfig cfg,
        EmpleadoController controller) {
    cfg.routes.get(
    "/api/empleados",
    ctx -> {

        List<EmpleadoDto> lista =
                controller.listar()
                .stream()
                .map(EmpleadoDto::desde)
                .toList();

        ctx.json(lista);
    });
 cfg.routes.post(
    "/api/empleados",
    ctx -> {

        EmpleadoRequest r =
                ctx.bodyAsClass(EmpleadoRequest.class);

        Empleado creado =
                controller.registrar(
                    r.nombre(),
                    r.apellidos(),
                    r.email(),
                    r.salario(),
                    LocalDate.parse(r.fechaIngreso()),
                    r.tipo()
                );

        ctx.status(201)
           .json(EmpleadoDto.desde(creado));
    });
  cfg.routes.put(
    "/api/empleados/{id}",
    ctx -> {

        int id =
            Integer.parseInt(ctx.pathParam("id"));

        EmpleadoRequest r =
            ctx.bodyAsClass(EmpleadoRequest.class);


        Empleado actualizado =
            controller.actualizar(
                id,
                r.nombre(),
                r.apellidos(),
                r.email(),
                r.salario(),
                LocalDate.parse(r.fechaIngreso()),
                r.tipo()
            );


        ctx.json(
            EmpleadoDto.desde(actualizado)
        );
    });
    cfg.routes.delete(
    "/api/empleados/{id}",
    ctx -> {

        int id =
            Integer.parseInt(ctx.pathParam("id"));

        controller.eliminar(id);

        ctx.status(204);
    });
  }

  // ── Edificios / Aulas (P1 de cada grupo — sin controlador de nombre fijo) ──

  private static void registrarEdificios(JavalinConfig cfg, EdificioController controller) {
    
    cfg.routes.get(
    "/api/edificios",
    ctx -> {
        List<EdificioDto> lista =
                EdificioDto.listaDesde(controller.listar());

        ctx.json(lista);
    });
    
    
    cfg.routes.post(
    "/api/edificios",
    ctx -> {

        EdificioRequest r =
                ctx.bodyAsClass(EdificioRequest.class);

        Edificio creado =
                controller.registrar(
                        r.codigo(),
                        r.nombre());

        ctx.status(201)
           .json(EdificioDto.desde(creado));
    });
    
    cfg.routes.put(
    "/api/edificios/{id}",
    ctx -> {

        int id = Integer.parseInt(ctx.pathParam("id"));

        EdificioRequest r =
                ctx.bodyAsClass(EdificioRequest.class);

        Edificio actualizado =
                controller.actualizar(
                        id,
                        r.codigo(),
                        r.nombre());

        ctx.json(
                EdificioDto.desde(actualizado));
    });
    
    cfg.routes.delete(
    "/api/edificios/{id}",
    ctx -> {

        int id =
                Integer.parseInt(ctx.pathParam("id"));

        controller.eliminar(id);

        ctx.status(204);
    });

    
    cfg.routes.post(
        "/api/edificios/{id}/aulas",
        ctx -> {

            int edificioId = Integer.parseInt(ctx.pathParam("id"));

            AulaRequest r = ctx.bodyAsClass(AulaRequest.class);

            Aula aula = controller.agregarAula(
                    edificioId,
                    r.codigo(),
                    r.capacidad(),
                    r.tipo() != null ? r.tipo() : TipoAula.TEORICA);

            ctx.status(201).json(AulaDto.desde(aula));
        });    
  }

  // ── Secciones (P1 de cada grupo — sin controlador de nombre fijo) ──

  private static void registrarSecciones(JavalinConfig cfg) {
    cfg.routes.get(
        "/api/secciones",
        ctx -> {
          // TODO(estudiante · P1): reemplacen este bloque por su código. Ej.:
          //   List<Seccion> secciones = MiControladorSeccion.listar();
          //   ctx.json(SeccionDto.listaDesde(secciones));
          ctx.status(501).json(Map.of("error", "secciones: pendiente de implementar"));
        });

    cfg.routes.post(
        "/api/secciones",
        ctx -> {
          // TODO(estudiante · P1): parseen el body y llamen a su método de registro. Ej.:
          //   SeccionRequest r = ctx.bodyAsClass(SeccionRequest.class);
          //   Seccion creada = MiControladorSeccion.registrar(r.codigo(), r.nombre(),
          //       r.aulaId(), r.docenteId());
          //   ctx.status(201).json(SeccionDto.desde(creada));
          ctx.status(501).json(Map.of("error", "secciones: pendiente de implementar"));
        });

    cfg.routes.put(
        "/api/secciones/{id}",
        ctx -> {
          // TODO(estudiante · P1): parseen el id y el body, y llamen a su método de
          // actualización. Ej.:
          //   int id = Integer.parseInt(ctx.pathParam("id"));
          //   SeccionRequest r = ctx.bodyAsClass(SeccionRequest.class);
          //   Seccion actualizada = MiControladorSeccion.actualizar(id, r.codigo(), r.nombre(),
          //       r.aulaId(), r.docenteId());
          //   ctx.json(SeccionDto.desde(actualizada));
          ctx.status(501).json(Map.of("error", "secciones: pendiente de implementar"));
        });

    cfg.routes.delete(
        "/api/secciones/{id}",
        ctx -> {
          // TODO(estudiante · P1): llamen a su método de eliminación. Ej.:
          //   MiControladorSeccion.eliminar(Integer.parseInt(ctx.pathParam("id")));
          //   ctx.status(204);
          ctx.status(501).json(Map.of("error", "secciones: pendiente de implementar"));
        });

    cfg.routes.post(
        "/api/secciones/{id}/estudiantes",
        ctx -> {
          // TODO(estudiante · P1): parseen el id, el body y llamen a su método para
          // inscribir un estudiante. Ej.:
          //   int seccionId = Integer.parseInt(ctx.pathParam("id"));
          //   InscripcionRequest r = ctx.bodyAsClass(InscripcionRequest.class);
          //   MiControladorSeccion.inscribir(seccionId, r.estudianteId());
          //   ctx.json(SeccionDto.desde(MiControladorSeccion.buscarPorId(seccionId)));
          ctx.status(501).json(Map.of("error", "secciones: pendiente de implementar"));
        });

    cfg.routes.delete(
        "/api/secciones/{id}/estudiantes/{estudianteId}",
        ctx -> {
          // TODO(estudiante · P1): llamen a su método para remover un estudiante. Ej.:
          //   int seccionId = Integer.parseInt(ctx.pathParam("id"));
          //   int estudianteId = Integer.parseInt(ctx.pathParam("estudianteId"));
          //   MiControladorSeccion.remover(seccionId, estudianteId);
          //   ctx.status(204);
          ctx.status(501).json(Map.of("error", "secciones: pendiente de implementar"));
        });
  }

  // ── Matrícula (puente HTTP→socket) ──

  private static void registrarMatricula(JavalinConfig cfg) {
    cfg.routes.post(
        "/api/matricula",
        ctx -> {
          // Bridge HTTP→socket (provisto por el docente como ejemplo de sockets + archivos):
          // guarda el CSV que sube la SPA en ENTRADA_DIR y delega el lote al ServidorMatricula.
          // La lógica de la transacción vive en ServidorMatricula.procesarLote (la implementan
          // los estudiantes).
          MatriculaRequest r = ctx.bodyAsClass(MatriculaRequest.class);
          String archivo = r.archivo() != null ? r.archivo() : "matriculas.csv";
          String contenido = r.contenido() != null ? r.contenido() : "";
          Path entrada = Path.of(System.getenv("ENTRADA_DIR"));
          Files.createDirectories(entrada);
          Files.writeString(entrada.resolve(archivo), contenido);
          String host = System.getenv("MATRICULA_HOST");
          int puertoMatricula = Integer.parseInt(System.getenv("MATRICULA_PORT"));
          try (Socket socket = new Socket(host, puertoMatricula);
              PrintWriter out =
                  new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
              BufferedReader in =
                  new BufferedReader(
                      new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            out.println("MATRICULAR " + archivo);
            String respuesta = in.readLine();
            ctx.json(
                Map.of(
                    "respuesta",
                    respuesta != null ? respuesta : "sin respuesta del servicio de matricula"));
          }
        });
  }

  // ── Reporte (puente HTTP→socket) ──

  private static void registrarReporte(JavalinConfig cfg) {
    cfg.routes.post(
        "/api/reporte",
        ctx -> {
          // Bridge HTTP→socket (provisto por el docente como ejemplo de sockets + archivos):
          // pide el reporte al ServidorReportes y devuelve el TXT que la SPA descarga. La lógica
          // de conteo y escritura del archivo vive en ServidorReportes.generarYGuardar (la
          // implementan los estudiantes).
          String host = System.getenv("REPORTE_HOST");
          int puertoReporte = Integer.parseInt(System.getenv("REPORTE_PORT"));
          try (Socket socket = new Socket(host, puertoReporte);
              PrintWriter out =
                  new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
              BufferedReader in =
                  new BufferedReader(
                      new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            out.println("REPORTE");
            String encabezado = in.readLine(); // "200 <n>" en éxito, "500 <msg>" en error
            if (encabezado == null || !encabezado.startsWith("200 ")) {
              ctx.status(502).json(Map.of("error", "reporte no disponible: " + encabezado));
              return;
            }
            int lineas = Integer.parseInt(encabezado.substring("200 ".length()).trim());
            StringBuilder contenido = new StringBuilder();
            for (int i = 0; i < lineas; i++) {
              String linea = in.readLine();
              contenido.append(linea == null ? "" : linea).append("\n");
            }
            ctx.contentType("text/plain; charset=utf-8");
            ctx.header("Content-Disposition", "attachment; filename=\"reporte.txt\"");
            ctx.result(contenido.toString());
          }
        });
  }
}
