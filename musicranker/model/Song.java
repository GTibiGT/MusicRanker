package musicranker.model;

public final class Song {
    private final String title;
    private final String artist;
    private final Rating rating;
    private final String emotion;
    private final String notes;

    public Song(String title, String artist, Rating rating, String emotion, String notes) {
        if (title == null || title.isBlank() || artist == null || artist.isBlank() || rating == null) {
            throw new IllegalArgumentException("Song title, artist and rating are required");
        }
        this.title = title.trim();
        this.artist = artist.trim();
        this.rating = rating;
        this.emotion = emotion == null ? "" : emotion.trim();
        this.notes = notes == null ? "" : notes.trim();
    }

    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public Rating getRating() { return rating; }
    public String getEmotion() { return emotion; }
    public String getNotes() { return notes; }
}
