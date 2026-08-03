package edu.uam.educore.api;

import edu.uam.educore.api.Dtos.AulaDto;
import edu.uam.educore.api.Dtos.AulaRequest;
import edu.uam.educore.api.Dtos.EdificioDto;
import edu.uam.educore.api.Dtos.EdificioRequest;
import edu.uam.educore.api.Dtos.EmpleadoDto;
import edu.uam.educore.api.Dtos.EmpleadoRequest;
import edu.uam.educore.api.Dtos.EstudianteDto;
import edu.uam.educore.api.Dtos.EstudianteRequest;
import edu.uam.educore.api.Dtos.InscripcionRequest;
import edu.uam.educore.api.Dtos.MatriculaRequest;
import edu.uam.educore.api.Dtos.SeccionDto;
import edu.uam.educore.api.Dtos.SeccionRequest;
import edu.uam.educore.controller.EdificioController;
import edu.uam.educore.controller.EmpleadoController;
import edu.uam.educore.controller.EstudianteController;
import edu.uam.educore.controller.SeccionController;
import edu.uam.educore.dao.EdificioRepoSql;
import edu.uam.educore.dao.EmpleadoRepoSql;
import edu.uam.educore.dao.EstudianteRepoSql;
import edu.uam.educore.dao.ListaEstudianteRepo;
import edu.uam.educore.dao.Repositorio;
import edu.uam.educore.dao.SeccionRepoSql;
import edu.uam.educore.db.ConfiguracionBD;
import edu.uam.educore.enums.TipoAula;
import edu.uam.educore.model.academico.Seccion;
import edu.uam.educore.model.infraestructura.Aula;
import edu.uam.educore.model.infraestructura.Edificio;
import edu.uam.educore.model.personas.Empleado;
import edu.uam.educore.model.personas.Estudiante;
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
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ServidorApi {

  public static void iniciar(int puerto) throws IOException {

    // =====================================================
    // ESTUDIANTES
    // =====================================================

    Repositorio<Estudiante> estudianteRepo;

    try {
      estudianteRepo = new EstudianteRepoSql(ConfiguracionBD.desdeArchivo(".env"));
    } catch (IOException e) {
      estudianteRepo = new ListaEstudianteRepo();
    }

    EstudianteController estudianteController = new EstudianteController(estudianteRepo);

    // =====================================================
    // EMPLEADOS
    // =====================================================

    Repositorio<Empleado> empleadoRepo;

    try {
      empleadoRepo = new EmpleadoRepoSql(ConfiguracionBD.desdeArchivo(".env"));
    } catch (IOException e) {
      throw new RuntimeException("No fue posible inicializar EmpleadoRepoSql.", e);
    }

    EmpleadoController empleadoController = new EmpleadoController(empleadoRepo);

    // =====================================================
    // EDIFICIOS Y AULAS
    // =====================================================

    Repositorio<Edificio> edificioRepo;

    try {
      edificioRepo = new EdificioRepoSql(ConfiguracionBD.desdeArchivo(".env"));
    } catch (IOException e) {
      throw new RuntimeException("No fue posible inicializar EdificioRepoSql.", e);
    }

    EdificioController edificioController = new EdificioController(edificioRepo);

    // =====================================================
    // SECCIONES
    // =====================================================

    Repositorio<Seccion> seccionRepo;

    try {
      seccionRepo = new SeccionRepoSql(ConfiguracionBD.desdeArchivo(".env"));
    } catch (IOException e) {
      throw new RuntimeException("No fue posible inicializar SeccionRepoSql.", e);
    }

    SeccionController seccionController =
        new SeccionController(seccionRepo, empleadoRepo, estudianteRepo, edificioRepo);

    // =====================================================
    // JAVALIN
    // =====================================================

    Javalin app =
        Javalin.create(
            cfg -> {
              cfg.bundledPlugins.enableDevLogging();
              cfg.spaRoot.addFile("/", "/web/index.html");

              // Errores de validación
              cfg.routes.exception(
                  IllegalArgumentException.class,
                  (e, ctx) -> ctx.status(400).json(Map.of("error", e.getMessage())));

              // Errores por relaciones entre tablas
              cfg.routes.exception(
                  SQLIntegrityConstraintViolationException.class,
                  (e, ctx) ->
                      ctx.status(409)
                          .json(
                              Map.of(
                                  "error",
                                  "No se puede eliminar: "
                                      + "el registro tiene datos asociados.")));

              // Errores generales
              cfg.routes.exception(
                  Exception.class,
                  (e, ctx) ->
                      ctx.status(500)
                          .json(
                              Map.of(
                                  "error",
                                  e.getMessage() != null
                                      ? e.getMessage()
                                      : "Error interno del servidor.")));

              registrarEstudiantes(cfg, estudianteController);

              registrarEmpleados(cfg, empleadoController);

              registrarEdificios(cfg, edificioController);

              registrarSecciones(cfg, seccionController);

              registrarMatricula(cfg);
              registrarReporte(cfg);
            });

    app.start(puerto);

    System.out.println("API EduCore escuchando en http://localhost:" + puerto);
  }

  // =====================================================
  // ESTUDIANTES
  // =====================================================

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
          EstudianteRequest request = ctx.bodyAsClass(EstudianteRequest.class);

          Estudiante creado;

          if ("BECADO".equalsIgnoreCase(request.tipo())) {

            creado =
                controller.registrarBecado(
                    request.nombre(),
                    request.apellidos(),
                    request.email(),
                    request.carnet(),
                    request.porcentajeBeca() != null ? request.porcentajeBeca() : 0.0);

          } else {

            creado =
                controller.registrarRegular(
                    request.nombre(), request.apellidos(), request.email(), request.carnet());
          }

          ctx.status(201).json(EstudianteDto.desde(creado));
        });

    cfg.routes.put(
        "/api/estudiantes/{id}",
        ctx -> {
          int id = Integer.parseInt(ctx.pathParam("id"));

          EstudianteRequest request = ctx.bodyAsClass(EstudianteRequest.class);

          Estudiante actualizado =
              controller.actualizar(
                  id,
                  request.nombre(),
                  request.apellidos(),
                  request.email(),
                  request.carnet(),
                  request.porcentajeBeca());

          ctx.json(EstudianteDto.desde(actualizado));
        });

    cfg.routes.delete(
        "/api/estudiantes/{id}",
        ctx -> {
          int id = Integer.parseInt(ctx.pathParam("id"));

          controller.eliminar(id);

          ctx.status(204);
        });
  }

  // =====================================================
  // EMPLEADOS
  // =====================================================

  private static void registrarEmpleados(JavalinConfig cfg, EmpleadoController controller) {

    cfg.routes.get(
        "/api/empleados",
        ctx -> {
          List<EmpleadoDto> lista = controller.listar().stream().map(EmpleadoDto::desde).toList();

          ctx.json(lista);
        });

    cfg.routes.post(
        "/api/empleados",
        ctx -> {
          EmpleadoRequest request = ctx.bodyAsClass(EmpleadoRequest.class);

          Empleado creado =
              controller.registrar(
                  request.nombre(),
                  request.apellidos(),
                  request.email(),
                  request.salario(),
                  LocalDate.parse(request.fechaIngreso()),
                  request.tipo());

          ctx.status(201).json(EmpleadoDto.desde(creado));
        });

    cfg.routes.put(
        "/api/empleados/{id}",
        ctx -> {
          int id = Integer.parseInt(ctx.pathParam("id"));

          EmpleadoRequest request = ctx.bodyAsClass(EmpleadoRequest.class);

          Empleado actualizado =
              controller.actualizar(
                  id,
                  request.nombre(),
                  request.apellidos(),
                  request.email(),
                  request.salario(),
                  LocalDate.parse(request.fechaIngreso()),
                  request.tipo());

          ctx.json(EmpleadoDto.desde(actualizado));
        });

    cfg.routes.delete(
        "/api/empleados/{id}",
        ctx -> {
          int id = Integer.parseInt(ctx.pathParam("id"));

          controller.eliminar(id);

          ctx.status(204);
        });
  }

  // =====================================================
  // EDIFICIOS Y AULAS
  // =====================================================

  private static void registrarEdificios(JavalinConfig cfg, EdificioController controller) {

    cfg.routes.get(
        "/api/edificios",
        ctx -> {
          List<EdificioDto> lista = EdificioDto.listaDesde(controller.listar());

          ctx.json(lista);
        });

    cfg.routes.post(
        "/api/edificios",
        ctx -> {
          EdificioRequest request = ctx.bodyAsClass(EdificioRequest.class);

          Edificio creado = controller.registrar(request.codigo(), request.nombre());

          ctx.status(201).json(EdificioDto.desde(creado));
        });

    cfg.routes.put(
        "/api/edificios/{id}",
        ctx -> {
          int id = Integer.parseInt(ctx.pathParam("id"));

          EdificioRequest request = ctx.bodyAsClass(EdificioRequest.class);

          Edificio actualizado = controller.actualizar(id, request.codigo(), request.nombre());

          ctx.json(EdificioDto.desde(actualizado));
        });

    cfg.routes.delete(
        "/api/edificios/{id}",
        ctx -> {
          int id = Integer.parseInt(ctx.pathParam("id"));

          controller.eliminar(id);

          ctx.status(204);
        });

    cfg.routes.post(
        "/api/edificios/{id}/aulas",
        ctx -> {
          int edificioId = Integer.parseInt(ctx.pathParam("id"));

          AulaRequest request = ctx.bodyAsClass(AulaRequest.class);

          Aula aula =
              controller.agregarAula(
                  edificioId,
                  request.codigo(),
                  request.capacidad(),
                  request.tipo() != null ? request.tipo() : TipoAula.TEORICA);

          ctx.status(201).json(AulaDto.desde(aula));
        });

    cfg.routes.put(
        "/api/edificios/{id}/aulas/{aulaId}",
        ctx -> {
          int edificioId = Integer.parseInt(ctx.pathParam("id"));

          int aulaId = Integer.parseInt(ctx.pathParam("aulaId"));

          AulaRequest request = ctx.bodyAsClass(AulaRequest.class);

          Aula actualizada =
              controller.actualizarAula(
                  edificioId,
                  aulaId,
                  request.codigo(),
                  request.capacidad(),
                  request.tipo() != null ? request.tipo() : TipoAula.TEORICA);

          ctx.json(AulaDto.desde(actualizada));
        });

    cfg.routes.delete(
        "/api/edificios/{id}/aulas/{aulaId}",
        ctx -> {
          int edificioId = Integer.parseInt(ctx.pathParam("id"));

          int aulaId = Integer.parseInt(ctx.pathParam("aulaId"));

          controller.eliminarAula(edificioId, aulaId);

          ctx.status(204);
        });
  }

  // =====================================================
  // SECCIONES
  // =====================================================

  private static void registrarSecciones(JavalinConfig cfg, SeccionController controller) {

    cfg.routes.get(
        "/api/secciones",
        ctx -> {
          List<SeccionDto> lista = SeccionDto.listaDesde(controller.listar());

          ctx.json(lista);
        });

    cfg.routes.post(
        "/api/secciones",
        ctx -> {
          SeccionRequest request = ctx.bodyAsClass(SeccionRequest.class);

          Seccion creada =
              controller.registrar(
                  request.codigo(), request.nombre(), request.aulaId(), request.docenteId());

          ctx.status(201).json(SeccionDto.desde(creada));
        });

    cfg.routes.put(
        "/api/secciones/{id}",
        ctx -> {
          int id = Integer.parseInt(ctx.pathParam("id"));

          SeccionRequest request = ctx.bodyAsClass(SeccionRequest.class);

          Seccion actualizada =
              controller.actualizar(
                  id, request.codigo(), request.nombre(), request.aulaId(), request.docenteId());

          ctx.json(SeccionDto.desde(actualizada));
        });

    cfg.routes.delete(
        "/api/secciones/{id}",
        ctx -> {
          int id = Integer.parseInt(ctx.pathParam("id"));

          controller.eliminar(id);

          ctx.status(204);
        });

    cfg.routes.post(
        "/api/secciones/{id}/estudiantes",
        ctx -> {
          int seccionId = Integer.parseInt(ctx.pathParam("id"));

          InscripcionRequest request = ctx.bodyAsClass(InscripcionRequest.class);

          controller.agregarEstudiante(seccionId, request.estudianteId());

          Seccion actualizada = controller.buscarPorId(seccionId);

          ctx.json(SeccionDto.desde(actualizada));
        });

    cfg.routes.delete(
        "/api/secciones/{id}/estudiantes/{estudianteId}",
        ctx -> {
          int seccionId = Integer.parseInt(ctx.pathParam("id"));

          int estudianteId = Integer.parseInt(ctx.pathParam("estudianteId"));

          controller.removerEstudiante(seccionId, estudianteId);

          ctx.status(204);
        });
  }

  // =====================================================
  // MATRÍCULA POR LOTE
  // =====================================================

  private static void registrarMatricula(JavalinConfig cfg) {

    cfg.routes.post(
        "/api/matricula",
        ctx -> {
          MatriculaRequest request = ctx.bodyAsClass(MatriculaRequest.class);

          String archivo = request.archivo() != null ? request.archivo() : "matriculas.csv";

          String contenido = request.contenido() != null ? request.contenido() : "";

          String entradaDir = System.getenv("ENTRADA_DIR");

          if (entradaDir == null || entradaDir.isBlank()) {

            throw new IllegalStateException("ENTRADA_DIR no está configurado.");
          }

          Path entrada = Path.of(entradaDir);

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
                    respuesta != null ? respuesta : "Sin respuesta del servicio de matrícula."));
          }
        });
  }

  // =====================================================
  // REPORTE
  // =====================================================

  private static void registrarReporte(JavalinConfig cfg) {

    cfg.routes.post(
        "/api/reporte",
        ctx -> {
          String host = System.getenv("REPORTE_HOST");

          int puertoReporte = Integer.parseInt(System.getenv("REPORTE_PORT"));

          try (Socket socket = new Socket(host, puertoReporte);
              PrintWriter out =
                  new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
              BufferedReader in =
                  new BufferedReader(
                      new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

            out.println("REPORTE");

            String encabezado = in.readLine();

            if (encabezado == null || !encabezado.startsWith("200 ")) {

              ctx.status(502).json(Map.of("error", "Reporte no disponible: " + encabezado));

              return;
            }

            int lineas = Integer.parseInt(encabezado.substring("200 ".length()).trim());

            StringBuilder contenido = new StringBuilder();

            for (int i = 0; i < lineas; i++) {

              String linea = in.readLine();

              contenido.append(linea != null ? linea : "").append("\n");
            }

            ctx.contentType("text/plain; charset=utf-8");

            ctx.header("Content-Disposition", "attachment; filename=\"reporte.txt\"");

            ctx.result(contenido.toString());
          }
        });
  }
}
