package musicranker.model;
import java.util.List;

public class Rating {
    private List<Integer> answers;
    
    public Rating(List<Integer> answers){
        if (answers == null || answers.isEmpty() || answers.stream().anyMatch(a -> a == null || a < 1 || a > 10)){
            throw new IllegalArgumentException("Every rating must be between 1 and 10");
        }
        this.answers = List.copyOf(answers);
    }

    public List<Integer> getAnswers(){
        return answers;
    }
}
