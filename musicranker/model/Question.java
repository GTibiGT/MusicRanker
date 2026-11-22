package musicranker.model;

public record Question(String category, String prompt, int weight){
    public Question {
        if (category == null || category.isBlank() || prompt == null || prompt.isBlank()){
            throw new IllegalArgumentException("Question text and category are required");
        }
        if (weight <= 0) throw new IllegalArgumentException("Weight must be positive");
    }
}