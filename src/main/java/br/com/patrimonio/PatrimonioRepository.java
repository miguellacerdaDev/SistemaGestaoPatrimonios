package br.com.patrimonio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatrimonioRepository {
    public List<Patrimonio> buscar(String categoria, String termo) {
        String sql = "SELECT * FROM patrimonio WHERE categoria = ? AND (codigo LIKE ? OR nome LIKE ? OR local_alocado LIKE ?) ORDER BY nome";
        List<Patrimonio> itens = new ArrayList<>();
        String filtro = "%" + termo.trim() + "%";
        try (Connection c = Database.connect(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, categoria); p.setString(2, filtro); p.setString(3, filtro); p.setString(4, filtro);
            ResultSet r = p.executeQuery();
            while (r.next()) itens.add(new Patrimonio(r.getInt("id"), r.getString("codigo"), r.getString("nome"),
                    r.getString("categoria"), r.getBoolean("em_uso"), r.getString("local_alocado")));
        } catch (SQLException e) { throw new RuntimeException(e); }
        return itens;
    }

    public void salvar(Patrimonio item) {
        boolean novo = item.id() == 0;
        String sql = novo ? "INSERT INTO patrimonio(codigo,nome,categoria,em_uso,local_alocado) VALUES(?,?,?,?,?)"
                : "UPDATE patrimonio SET codigo=?,nome=?,categoria=?,em_uso=?,local_alocado=? WHERE id=?";
        try (Connection c = Database.connect(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, item.codigo()); p.setString(2, item.nome()); p.setString(3, item.categoria());
            p.setBoolean(4, item.emUso()); p.setString(5, item.localAlocado());
            if (!novo) p.setInt(6, item.id());
            p.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(novo && e.getMessage().contains("UNIQUE") ? "Este código já está cadastrado." : e.getMessage(), e); }
    }
}
