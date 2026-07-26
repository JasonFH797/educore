
package edu.uam.educore.dao;

import edu.uam.educore.db.Conexion;
import edu.uam.educore.db.ConfiguracionBD;

import edu.uam.educore.enums.TipoAula;

import edu.uam.educore.model.infraestructura.Aula;
import edu.uam.educore.model.infraestructura.Edificio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class EdificioRepoSql extends Repositorio<Edificio>
{

    private final ConfiguracionBD config;

    public EdificioRepoSql(ConfiguracionBD config)
    {
        this.config = config;
    }

    private Connection abrir() throws Exception
    {
        return Conexion.getConnection(
                config.url(),
                config.usuario(),
                config.contrasena());
    }
    
    @Override
    public void guardar(Edificio edificio) throws Exception
    {
        String sql =
                "INSERT INTO edificio (codigo, nombre) "
                + "VALUES (?, ?)";

        try (Connection con = abrir())
        {
            con.setAutoCommit(false);

            try
            {
                try (PreparedStatement ps = con.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS))
                {

                    ps.setString(1, edificio.getCodigo());
                    ps.setString(2, edificio.getNombre());

                    ps.executeUpdate();

                    try (ResultSet claves = ps.getGeneratedKeys())
                    {
                        if (claves.next())
                        {
                            edificio.setId(claves.getInt(1));
                        }
                    }
                }

                // Aquí guardaremos las aulas
                guardarAulas(con, edificio);

                con.commit();
            }
            catch (Exception ex)
            {
                con.rollback();
                throw ex;
            }
        }
    }

    
    private void guardarAulas(Connection con, Edificio edificio) throws Exception
    {
        String sql =
                "INSERT INTO aula (codigo, capacidad, tipo, edificio_id) "
                + "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS))
        {

            for (Aula aula : edificio.getAulas())
            {

                ps.setString(1, aula.getCodigo());
                ps.setInt(2, aula.getCapacidad());
                ps.setString(3, aula.getTipo().name());
                ps.setInt(4, edificio.getId());

                ps.executeUpdate();

                try (ResultSet claves = ps.getGeneratedKeys())
                {
                    if (claves.next())
                    {
                        aula.setId(claves.getInt(1));
                    }
                }

            }

        }

    }

    @Override
    public void actualizar(Edificio edificio) throws Exception
    {
        String sql =
                "UPDATE edificio " +
                "SET codigo = ?, nombre = ? " +
                "WHERE id = ?";

        try (Connection con = abrir())
        {
            con.setAutoCommit(false);

            try
            {
                try (PreparedStatement ps = con.prepareStatement(sql))
                {
                    ps.setString(1, edificio.getCodigo());
                    ps.setString(2, edificio.getNombre());
                    ps.setInt(3, edificio.getId());

                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM aula WHERE edificio_id = ?"))
                {
                    ps.setInt(1, edificio.getId());
                    ps.executeUpdate();
                }

                guardarAulas(con, edificio);

                con.commit();
            }
            catch (Exception ex)
            {
                con.rollback();
                throw ex;
            }
        }
    }
    
    @Override
    public void eliminar(int id) throws Exception
    {
        try (Connection con = abrir())
        {
            con.setAutoCommit(false);

            try
            {
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM aula WHERE edificio_id = ?"))
                {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM edificio WHERE id = ?"))
                {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }

                con.commit();
            }
            catch (Exception ex)
            {
                con.rollback();
                throw ex;
            }
        }
    }
    
    @Override
    public Optional<Edificio> buscarPorId(int id) throws Exception
    {
        String sql = "SELECT * FROM edificio WHERE id = ?";

        try (Connection con = abrir();
             PreparedStatement ps = con.prepareStatement(sql))
        {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    Edificio edificio = mapearEdificio(rs);

                    cargarAulas(con, edificio);

                    return Optional.of(edificio);
                }

                return Optional.empty();
            }
        }
    }

    @Override
    public List<Edificio> buscarTodos() throws Exception
    {
        List<Edificio> lista = new ArrayList<>();

        String sql = "SELECT * FROM edificio";

        try (Connection con = abrir();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery())
        {

            while (rs.next())
            {
                Edificio edificio = mapearEdificio(rs);

                cargarAulas(con, edificio);

                lista.add(edificio);
            }

        }

        return lista;
    }
    
    private Edificio mapearEdificio(ResultSet rs) throws Exception
    {
        int id = rs.getInt("id");
        String codigo = rs.getString("codigo");
        String nombre = rs.getString("nombre");

        return new Edificio(id, codigo, nombre);
    }
    
    private Aula mapearAula(ResultSet rs, Edificio edificio) throws Exception
    {
        int id = rs.getInt("id");
        String codigo = rs.getString("codigo");
        int capacidad = rs.getInt("capacidad");
        TipoAula tipo = TipoAula.valueOf(rs.getString("tipo"));

        return new Aula(
                id,
                codigo,
                capacidad,
                tipo,
                edificio
        );
    }
    
    private void cargarAulas(Connection con, Edificio edificio) throws Exception
    {
        String sql =
                "SELECT * FROM aula WHERE edificio_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setInt(1, edificio.getId());

            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    Aula aula = mapearAula(rs, edificio);

                    edificio.agregarAula(aula);
                }
            }
        }
    }

}
