package musicranker.service;

import musicranker.model.Question;
import musicranker.model.Rating;
import musicranker.model.Song;
import musicranker.model.Tier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class RankingService {
    private final List<Question> questions;
    private final int totalWeight;

    public RankingService(List<Question> questions) {
        if (questions == null || questions.isEmpty()) {
            throw new IllegalArgumentException("At least one question is required");
        }
        this.questions = List.copyOf(questions);
        this.totalWeight = questions.stream().mapToInt(Question::weight).sum();
    }

    public List<Question> getQuestions() { return questions; }

    public double calculateScore(Rating rating) {
        if (rating == null || rating.getAnswers().size() != questions.size()) {
            throw new IllegalArgumentException("One answer per question is required");
        }
        double weightedSum = 0;
        for (int i = 0; i < questions.size(); i++) {
            weightedSum += rating.getAnswers().get(i) * questions.get(i).weight();
        }
        return 10.0 * weightedSum / totalWeight;
    }

    public double calculateScore(Song song) { return calculateScore(song.getRating()); }
    public Tier determineTier(Song song) { return Tier.fromScore(calculateScore(song)); }

    public List<Song> rankSongs(List<Song> songs) {
        List<Song> sorted = new ArrayList<>(songs);
        sorted.sort(Comparator.comparingDouble((Song s) -> calculateScore(s)).reversed()
                .thenComparing(Song::getTitle, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Song::getArtist, String.CASE_INSENSITIVE_ORDER));
        return sorted;
    }
}
