package musicranker.model;
import java.util.Objects;
public final class Song {
    private final String title, artist, emotion, notes;
    private final Rating rating;
    public Song(String title, String artist, Rating rating, String emotion, String notes) {
        if (title == null || title.isBlank() || artist == null || artist.isBlank())
            throw new IllegalArgumentException("Title and artist are required");
        this.title = title.trim(); this.artist = artist.trim();
        this.rating = Objects.requireNonNull(rating);
        this.emotion = emotion == null ? "" : emotion.trim();
        this.notes = notes == null ? "" : notes.trim();
    }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public Rating getRating() { return rating; }
    public String getEmotion() { return emotion; }
    public String getNotes() { return notes; }
}
