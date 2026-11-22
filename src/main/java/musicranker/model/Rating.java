package musicranker.model;
import java.util.List;
public final class Rating {
    private final List<Integer> answers;
    public Rating(List<Integer> answers) {
        if (answers == null || answers.size() != 8 || answers.stream().anyMatch(x -> x == null || x < 1 || x > 10))
            throw new IllegalArgumentException("Exactly eight ratings between 1 and 10 are required");
        this.answers = List.copyOf(answers);
    }
    public List<Integer> getAnswers() { return answers; }
}
