package br.com.patrimonio;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ManutencaoRepository {
    public record Manutencao(int id, String local, String tipo, LocalDate data, String observacao) { }
    public void salvar(String local, String tipo, LocalDate data, String observacao) {
        String sql = "INSERT INTO manutencao(local,tipo,data_agendada,observacao) VALUES(?,?,?,?)";
        try (Connection c = Database.connect(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, local); p.setString(2, tipo); p.setString(3, data.toString()); p.setString(4, observacao); p.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
    public List<Manutencao> listar() {
        List<Manutencao> lista = new ArrayList<>();
        try (Connection c = Database.connect(); Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT * FROM manutencao ORDER BY data_agendada")) {
            while (r.next()) lista.add(new Manutencao(r.getInt("id"), r.getString("local"), r.getString("tipo"), LocalDate.parse(r.getString("data_agendada")), r.getString("observacao")));
        } catch (SQLException e) { throw new RuntimeException(e); }
        return lista;
    }
    
    public void excluir(int id) {
    String sql = "DELETE FROM manutencao WHERE id = ?";

    try (Connection c = Database.connect();
         PreparedStatement p = c.prepareStatement(sql)) {

        p.setInt(1, id);
        p.executeUpdate();

    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
}
}
