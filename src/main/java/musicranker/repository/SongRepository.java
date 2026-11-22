package musicranker.repository;
import musicranker.model.*;
import java.sql.*;
import java.util.*;
public final class SongRepository {
    public List<Song> loadAll() throws SQLException {
        List<Song> songs = new ArrayList<>();
        String sql = """
            SELECT s.id, s.title, s.artist, s.emotion, s.notes, r.question_index, r.score
            FROM songs s LEFT JOIN ratings r ON s.id = r.song_id
            ORDER BY s.id, r.question_index
            """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            int currentId = -1;
            String title = null, artist = null, emotion = null, notes = null;
            List<Integer> answers = new ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt("id");
                if (currentId != -1 && currentId != id) {
                    songs.add(buildSong(title, artist, emotion, notes, answers));
                    answers = new ArrayList<>();
                }
                if (currentId != id) {
                    currentId = id;
                    title = rs.getString("title"); artist = rs.getString("artist");
                    emotion = rs.getString("emotion"); notes = rs.getString("notes");
                }
                int questionIndex = rs.getInt("question_index");
                if (!rs.wasNull()) {
                    if (questionIndex != answers.size()) throw new SQLException("Missing rating for song ID " + id);
                    answers.add(rs.getInt("score"));
                }
            }
            if (currentId != -1) songs.add(buildSong(title, artist, emotion, notes, answers));
        }
        return songs;
    }
    private Song buildSong(String title, String artist, String emotion, String notes, List<Integer> answers)
            throws SQLException {
        if (answers.size() != 8) throw new SQLException("Expected eight ratings for " + title);
        try { return new Song(title, artist, new Rating(answers), emotion, notes); }
        catch (IllegalArgumentException e) { throw new SQLException("Invalid saved song: " + title, e); }
    }
    // Atomic snapshot: suitable for a small single-user collection. Future phases can use per-row CRUD.
    public void saveAll(List<Song> songs) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (Statement st = conn.createStatement()) { st.executeUpdate("DELETE FROM songs"); }
                try (PreparedStatement insertSong = conn.prepareStatement(
                        "INSERT INTO songs(title, artist, emotion, notes) VALUES (?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS);
                     PreparedStatement insertRating = conn.prepareStatement(
                        "INSERT INTO ratings(song_id, question_index, score) VALUES (?, ?, ?)")) {
                    for (Song song : songs) {
                        insertSong.setString(1, song.getTitle()); insertSong.setString(2, song.getArtist());
                        insertSong.setString(3, song.getEmotion()); insertSong.setString(4, song.getNotes());
                        insertSong.executeUpdate();
                        int id;
                        try (ResultSet keys = insertSong.getGeneratedKeys()) {
                            if (!keys.next()) throw new SQLException("No generated song ID");
                            id = keys.getInt(1);
                        }
                        List<Integer> answers = song.getRating().getAnswers();
                        if (answers.size() != 8) throw new SQLException("Eight ratings required");
                        for (int i = 0; i < 8; i++) {
                            insertRating.setInt(1, id); insertRating.setInt(2, i);
                            insertRating.setInt(3, answers.get(i)); insertRating.executeUpdate();
                        }
                    }
                }
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                try { conn.rollback(); } catch (SQLException ex) { e.addSuppressed(ex); }
                throw e;
            }
        }
    }
}
