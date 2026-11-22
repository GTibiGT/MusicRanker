package musicranker.service;
import musicranker.model.*;
import java.util.*;
import java.util.stream.Collectors;
public final class SongService {
    private final RankingService ranking;
    private final List<Song> songs = new ArrayList<>();
    public SongService(RankingService ranking) { this.ranking = Objects.requireNonNull(ranking); }
    private boolean duplicate(Song candidate, Song except) {
        return songs.stream().anyMatch(s -> s != except && s.getTitle().equalsIgnoreCase(candidate.getTitle())
                && s.getArtist().equalsIgnoreCase(candidate.getArtist()));
    }
    public boolean add(Song song) {
        if (duplicate(song, null)) return false;
        return songs.add(song);
    }
    public int count() { return songs.size(); }
    public List<Song> getAllRanked() {
        return songs.stream().sorted(Comparator.comparingDouble(ranking::calculateScore).reversed()
                .thenComparing(Song::getTitle, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Song::getArtist, String.CASE_INSENSITIVE_ORDER)).collect(Collectors.toList());
    }
    public Song getByRankedIndex(int index) { return getAllRanked().get(index); }
    public boolean update(int rankedIndex, Song replacement) {
        Song old = getByRankedIndex(rankedIndex);
        if (duplicate(replacement, old)) return false;
        songs.set(songs.indexOf(old), replacement);
        return true;
    }
    public void delete(int rankedIndex) { songs.remove(getByRankedIndex(rankedIndex)); }
    public List<Song> search(String term) {
        String q = term.toLowerCase(Locale.ROOT);
        return getAllRanked().stream().filter(s -> s.getTitle().toLowerCase(Locale.ROOT).contains(q)
                || s.getArtist().toLowerCase(Locale.ROOT).contains(q)).toList();
    }
    public List<Song> filterByArtist(String artist) {
        return getAllRanked().stream().filter(s -> s.getArtist().toLowerCase(Locale.ROOT)
                .contains(artist.toLowerCase(Locale.ROOT))).toList();
    }
    public List<Song> filterByTier(Tier tier) {
        return getAllRanked().stream().filter(s -> ranking.determineTier(s) == tier).toList();
    }
    public List<Song> filterByMinimumScore(double min) {
        return getAllRanked().stream().filter(s -> ranking.calculateScore(s) >= min).toList();
    }
}
