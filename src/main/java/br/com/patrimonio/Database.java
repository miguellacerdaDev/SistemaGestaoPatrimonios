package br.com.patrimonio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

public final class Database {

    private static final Path DIRETORIO_DADOS = Path.of(
            System.getenv("LOCALAPPDATA"),
            "GestaoPatrimonioTI"
    );

    private static final String URL = "jdbc:sqlite:"
            + DIRETORIO_DADOS.resolve("patrimonio-ti.db").toAbsolutePath();

    private Database() {
    }

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initialize() {
        try {
            Files.createDirectories(DIRETORIO_DADOS);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Não foi possível criar a pasta de dados da aplicação.",
                    e
            );
        }
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

        String localizacoes = """
        CREATE TABLE IF NOT EXISTS localizacao (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          bloco TEXT NOT NULL,
          sala TEXT NOT NULL,
          UNIQUE(bloco, sala)
        )""";

        try (Connection c = connect(); Statement s = c.createStatement()) {
            s.execute(patrimoni);
            s.execute(manutencao);
            s.execute(localizacoes);

            s.executeUpdate("""
        INSERT OR IGNORE INTO localizacao(bloco, sala)
        VALUES
        ('Bloco 6', 'Sala de Redes'),
        ('Bloco 6', 'Sala de Manutenção'),
        ('Bloco 6', 'Laboratório de Informática')
        """);
        } catch (SQLException e) {
            throw new RuntimeException("Não foi possível criar o banco de dados.", e);
        }
    }
}
