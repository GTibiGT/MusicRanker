import java.util.Comparator;
import java.util.List;

public class RankingCalc {
    private List<Question> questions;

    public RankingCalc(List<Question> questions){
        this.questions = questions;
    }

    public double calcScore(Song song){
        int[] ratings = song.getRatings();

        if (ratings.length != questions.size()){
            throw new IllegalArgumentException("Incorrect number of ratings!");
        }

        double score = 0;
        double totalWeight = 0;

        for (int i = 0; i < ratings.length; i++){
            if (ratings[i] < 1 || ratings[i] > 10){
                throw new IllegalArgumentException("Ratings must be between 1 and 10");
            }

            double weight = questions.get(i).weight();
            score += ratings[i] * weight;
            totalWeight += weight;
        }

        if (totalWeight <= 0){
            throw new IllegalArgumentException("Invalid weights");
        }

        return 10 * score / totalWeight;
    }

    public String determineTier(double score){
        return "TODO";
    }

}

