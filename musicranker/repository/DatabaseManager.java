package musicranker.repository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.IOException;

public final class DatabaseManager {

    private static final Path DB_PATH =
            Path.of("data", "musicranker.db");

    private DatabaseManager() {}

    public static Connection getConnection()
            throws SQLException {

        try {
            Files.createDirectories(DB_PATH.getParent());
        } catch (IOException e) {
            throw new SQLException(
                    "Could not create database directory", e);
        }

        Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + DB_PATH.toAbsolutePath()
        );

        try (Statement statement =
                     connection.createStatement()) {

            statement.execute("PRAGMA foreign_keys = ON");
        }

        return connection;
    }

    public static void initializeDatabase()
            throws SQLException {

        String songsTable = """
            CREATE TABLE IF NOT EXISTS songs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL COLLATE NOCASE,
                artist TEXT NOT NULL COLLATE NOCASE,
                emotion TEXT NOT NULL DEFAULT '',
                notes TEXT NOT NULL DEFAULT '',
                UNIQUE(title, artist)
            )
            """;

        String ratingsTable = """
            CREATE TABLE IF NOT EXISTS ratings (
                song_id INTEGER NOT NULL,
                question_index INTEGER NOT NULL
                    CHECK(question_index BETWEEN 0 AND 7),
                score INTEGER NOT NULL
                    CHECK(score BETWEEN 1 AND 10),
                PRIMARY KEY(song_id, question_index),
                FOREIGN KEY(song_id)
                    REFERENCES songs(id)
                    ON DELETE CASCADE
            )
            """;

        try (Connection connection = getConnection();
             Statement statement =
                     connection.createStatement()) {

            statement.execute(songsTable);
            statement.execute(ratingsTable);
        }
    }
}

