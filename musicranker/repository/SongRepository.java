
package musicranker.repository;

import musicranker.model.Song;
import musicranker.model.Rating;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SongRepository {

    public List<Song> loadAll() throws SQLException {

        List<Song> songs = new ArrayList<>();

        String sql = """
            SELECT s.id, s.title, s.artist,
                   s.emotion, s.notes,
                   r.question_index, r.score
            FROM songs s
            LEFT JOIN ratings r ON s.id = r.song_id
            ORDER BY s.id, r.question_index
            """;

        try (Connection connection =
                     DatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            int currentId = -1;
            String title = null;
            String artist = null;
            String emotion = null;
            String notes = null;
            List<Integer> answers = new ArrayList<>();

            while (result.next()) {

                int id = result.getInt("id");

                if (currentId != -1 && id != currentId) {
                    songs.add(buildSong(
                            title, artist, emotion, notes, answers));
                    answers = new ArrayList<>();
                }

                if (id != currentId) {
                    currentId = id;
                    title = result.getString("title");
                    artist = result.getString("artist");
                    emotion = result.getString("emotion");
                    notes = result.getString("notes");
                }

                int questionIndex =
                        result.getInt("question_index");

                if (!result.wasNull()) {
                    if (questionIndex != answers.size()) {
                        throw new SQLException(
                                "Missing or unordered rating for song "
                                + id);
                    }
                    answers.add(result.getInt("score"));
                }
            }

            if (currentId != -1) {
                songs.add(buildSong(
                        title, artist, emotion, notes, answers));
            }
        }

        return songs;
    }

    private Song buildSong(
            String title,
            String artist,
            String emotion,
            String notes,
            List<Integer> answers) throws SQLException {

        if (answers.size() != 8) {
            throw new SQLException(
                    "Expected 8 ratings for " + title);
        }

        return new Song(
                title,
                artist,
                new Rating(answers),
                emotion,
                notes
        );
    }

    public void saveAll(List<Song> songs)
            throws SQLException {

        String insertSong = """
            INSERT INTO songs(title, artist, emotion, notes)
            VALUES (?, ?, ?, ?)
            """;

        String insertRating = """
            INSERT INTO ratings(song_id, question_index, score)
            VALUES (?, ?, ?)
            """;

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            connection.setAutoCommit(false);

            try {
                try (Statement statement =
                             connection.createStatement()) {
                    statement.executeUpdate("DELETE FROM songs");
                }

                try (PreparedStatement songStatement =
                             connection.prepareStatement(
                                     insertSong,
                                     Statement.RETURN_GENERATED_KEYS);
                     PreparedStatement ratingStatement =
                             connection.prepareStatement(insertRating)) {

                    for (Song song : songs) {

                        songStatement.setString(
                                1, song.getTitle());
                        songStatement.setString(
                                2, song.getArtist());
                        songStatement.setString(
                                3, song.getEmotion());
                        songStatement.setString(
                                4, song.getNotes());

                        songStatement.executeUpdate();

                        int songId;

                        try (ResultSet keys =
                                     songStatement.getGeneratedKeys()) {
                            if (!keys.next()) {
                                throw new SQLException(
                                        "No generated song ID");
                            }
                            songId = keys.getInt(1);
                        }

                        List<Integer> answers =
                                song.getRating().getAnswers();

                        if (answers.size() != 8) {
                            throw new SQLException(
                                    "A song must have 8 ratings");
                        }

                        for (int i = 0; i < answers.size(); i++) {
                            ratingStatement.setInt(1, songId);
                            ratingStatement.setInt(2, i);
                            ratingStatement.setInt(3, answers.get(i));
                            ratingStatement.executeUpdate();
                        }
                    }
                }

                connection.commit();

            } catch (SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        }
    }
}
