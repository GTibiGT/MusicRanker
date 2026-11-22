package musicranker.model;

public enum Tier {
    S_PLUS("S+"), S("S"), A("A"), B("B"), C("C"), D("D"), F("F");

    private final String label;
    Tier(String label) {
        this.label = label;
    }

    public String toString(){
        return label;
    }

    public static Tier fromScore(double score){
        if (score < 10 || score > 100 || !Double.isFinite(score)){
            throw new IllegalArgumentException("Score must be between 10 and 100");
        }
        if (score >= 95) return S_PLUS;
        if (score >= 90) return S;
        if (score >= 80) return A;
        if (score >= 70) return B;
        if (score >= 60) return C;
        if (score >= 50) return D;
        return F;
    }
}
