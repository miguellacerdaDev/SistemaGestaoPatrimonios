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
    
    public void salvarEmLote(
        String codigoInicial,
        String nome,
        String categoria,
        boolean emUso,
        String localAlocado,
        int quantidade
) {
    int ultimaPosicao = codigoInicial.length() - 1;

    while (ultimaPosicao >= 0
            && Character.isDigit(codigoInicial.charAt(ultimaPosicao))) {
        ultimaPosicao--;
    }

    if (ultimaPosicao == codigoInicial.length() - 1) {
        throw new RuntimeException(
                "O código inicial precisa terminar com um número. Exemplo: PC-LEN-001"
        );
    }

    String prefixo = codigoInicial.substring(0, ultimaPosicao + 1);
    String numeroTexto = codigoInicial.substring(ultimaPosicao + 1);
    int numeroInicial = Integer.parseInt(numeroTexto);
    int quantidadeDigitos = numeroTexto.length();

    String sql = """
            INSERT INTO patrimonio(codigo, nome, categoria, em_uso, local_alocado)
            VALUES(?,?,?,?,?)
            """;

    try (Connection c = Database.connect()) {
        c.setAutoCommit(false);

        try (PreparedStatement p = c.prepareStatement(sql)) {
            for (int i = 0; i < quantidade; i++) {
                String codigoGerado = prefixo
                        + String.format("%0" + quantidadeDigitos + "d",
                        numeroInicial + i);

                p.setString(1, codigoGerado);
                p.setString(2, nome);
                p.setString(3, categoria);
                p.setBoolean(4, emUso);
                p.setString(5, localAlocado);
                p.addBatch();
            }

            p.executeBatch();
            c.commit();

        } catch (SQLException e) {
            c.rollback();

            if (e.getMessage().contains("UNIQUE")) {
                throw new RuntimeException(
                        "Um dos códigos gerados já existe. Escolha outro código inicial."
                );
            }

            throw e;
        }

    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
}
}
