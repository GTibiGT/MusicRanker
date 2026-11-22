package musicranker.model;
public record Question(String category, String prompt, int weight) {
    public Question {
        if (category == null || category.isBlank() || prompt == null || prompt.isBlank() || weight <= 0)
            throw new IllegalArgumentException("Invalid question");
    }
}
