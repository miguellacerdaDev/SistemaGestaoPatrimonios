package br.com.patrimonio;

import java.nio.file.Path;
import java.sql.*;

public final class Database {
    private static final String URL = "jdbc:sqlite:" + Path.of("patrimonio-ti.db").toAbsolutePath();
    private Database() { }

    public static Connection connect() throws SQLException { return DriverManager.getConnection(URL); }

    public static void initialize() {
        String patrimoni = """
            CREATE TABLE IF NOT EXISTS patrimonio (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              codigo TEXT NOT NULL UNIQUE,
              nome TEXT NOT NULL,
              categoria TEXT NOT NULL CHECK(categoria IN ('Computador','Equipamento')),
              em_uso INTEGER NOT NULL DEFAULT 1,
              local_alocado TEXT NOT NULL
            )""";
        String manutencao = """
            CREATE TABLE IF NOT EXISTS manutencao (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              local TEXT NOT NULL,
              tipo TEXT NOT NULL,
              data_agendada TEXT NOT NULL,
              observacao TEXT
            )""";
        try (Connection c = connect(); Statement s = c.createStatement()) {
            s.execute(patrimoni);
            s.execute(manutencao);
        } catch (SQLException e) { throw new RuntimeException("Não foi possível criar o banco de dados.", e); }
    }
}
