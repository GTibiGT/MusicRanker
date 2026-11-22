package musicranker.service;

import musicranker.model.Song;
import musicranker.model.Tier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Manages songs in memory. Phase 3 will add permanent storage. */
public final class SongService {
    private final List<Song> songs = new ArrayList<>();
    private final RankingService ranking;

    public SongService(RankingService ranking) {
        if (ranking == null) throw new IllegalArgumentException("Ranking service is required");
        this.ranking = ranking;
    }

    public List<Song> getAllRanked() { return ranking.rankSongs(songs); }
    public int count() { return songs.size(); }

    public boolean add(Song song) {
        if (song == null) throw new IllegalArgumentException("Song is required");
        if (isDuplicate(song, -1)) return false;
        songs.add(song);
        return true;
    }

    /** Replace the song at its zero-based position in the current ranked list. */
    public boolean update(int rankedIndex, Song replacement) {
        if (replacement == null) throw new IllegalArgumentException("Song is required");
        Song original = getByRankedIndex(rankedIndex);
        int storageIndex = songs.indexOf(original);
        if (isDuplicate(replacement, storageIndex)) return false;
        songs.set(storageIndex, replacement);
        return true;
    }

    public Song delete(int rankedIndex) {
        Song original = getByRankedIndex(rankedIndex);
        songs.remove(original);
        return original;
    }

    public Song getByRankedIndex(int rankedIndex) {
        List<Song> ranked = getAllRanked();
        if (rankedIndex < 0 || rankedIndex >= ranked.size()) {
            throw new IndexOutOfBoundsException("Invalid song position");
        }
        return ranked.get(rankedIndex);
    }

    public List<Song> search(String text) {
        String query = normalize(text);
        return getAllRanked().stream()
                .filter(s -> normalize(s.getTitle()).contains(query)
                        || normalize(s.getArtist()).contains(query))
                .toList();
    }

    public List<Song> filterByArtist(String artist) {
        String query = normalize(artist);
        return getAllRanked().stream()
                .filter(s -> normalize(s.getArtist()).contains(query))
                .toList();
    }

    public List<Song> filterByTier(Tier tier) {
        if (tier == null) throw new IllegalArgumentException("Tier is required");
        return getAllRanked().stream()
                .filter(s -> ranking.determineTier(s) == tier)
                .toList();
    }

    public List<Song> filterByMinimumScore(double minimum) {
        if (!Double.isFinite(minimum) || minimum < 10 || minimum > 100) {
            throw new IllegalArgumentException("Minimum score must be 10-100");
        }
        return getAllRanked().stream()
                .filter(s -> ranking.calculateScore(s) >= minimum)
                .toList();
    }

    private boolean isDuplicate(Song candidate, int exceptIndex) {
        for (int i = 0; i < songs.size(); i++) {
            Song existing = songs.get(i);
            if (i != exceptIndex
                    && normalize(existing.getTitle()).equals(normalize(candidate.getTitle()))
                    && normalize(existing.getArtist()).equals(normalize(candidate.getArtist()))) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
