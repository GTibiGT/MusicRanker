package musicranker.service;
import java.util.List;
import musicranker.model.*;
public final class RankingService {
    private final List<Question> questions;
    public RankingService(List<Question> questions) {
        if (questions == null || questions.size() != 8) throw new IllegalArgumentException("Eight questions required");
        this.questions = List.copyOf(questions);
    }
    public List<Question> getQuestions() { return questions; }
    public double calculateScore(Song song) {
        double sum = 0, weights = 0;
        for (int i = 0; i < questions.size(); i++) {
            int weight = questions.get(i).weight();
            sum += song.getRating().getAnswers().get(i) * weight;
            weights += weight;
        }
        return sum / weights * 10;
    }
    public Tier determineTier(Song song) {
        double score = calculateScore(song);
        if (score >= 95) return Tier.S_PLUS;
        if (score >= 90) return Tier.S;
        if (score >= 80) return Tier.A;
        if (score >= 70) return Tier.B;
        if (score >= 60) return Tier.C;
        if (score >= 50) return Tier.D;
        return Tier.F;
    }
}
