package br.com.patrimonio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class LocalizacaoRepository {

    public record Localizacao(int id, String bloco, String sala) {

        @Override
        public String toString() {
            return bloco + " — " + sala;
        }
    }

    public List<String> listarBlocos() {
        List<String> blocos = new ArrayList<>();

        String sql = """
                SELECT DISTINCT bloco
                FROM localizacao
                ORDER BY bloco
                """;

        try (Connection c = Database.connect(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {

            while (r.next()) {
                blocos.add(r.getString("bloco"));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return blocos;
    }

    public List<String> listarSalas(String bloco) {
        List<String> salas = new ArrayList<>();

        String sql = """
                SELECT sala
                FROM localizacao
                WHERE bloco = ?
                ORDER BY sala
                """;

        try (Connection c = Database.connect(); PreparedStatement p = c.prepareStatement(sql)) {

            p.setString(1, bloco);

            ResultSet r = p.executeQuery();

            while (r.next()) {
                salas.add(r.getString("sala"));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return salas;
    }

    public void salvar(String bloco, String sala) {
        String sql = """
                INSERT INTO localizacao(bloco, sala)
                VALUES (?, ?)
                """;

        try (Connection c = Database.connect(); PreparedStatement p = c.prepareStatement(sql)) {

            p.setString(1, bloco.trim());
            p.setString(2, sala.trim());
            p.executeUpdate();

        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE")) {
                throw new RuntimeException("Esta sala já está cadastrada neste bloco.");
            }

            throw new RuntimeException(e);
        }
    }

    public void excluir(int id) {
        String sql = "DELETE FROM localizacao WHERE id = ?";

        try (Connection c = Database.connect(); PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, id);
            p.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Localizacao> listarTodas() {
        List<Localizacao> locais = new ArrayList<>();

        String sql = """
                SELECT id, bloco, sala
                FROM localizacao
                ORDER BY bloco, sala
                """;

        try (Connection c = Database.connect(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {

            while (r.next()) {
                locais.add(new Localizacao(
                        r.getInt("id"),
                        r.getString("bloco"),
                        r.getString("sala")
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return locais;
    }
}
