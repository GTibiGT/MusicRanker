package musicranker.model;
public enum Tier {
    S_PLUS("S+"), S("S"), A("A"), B("B"), C("C"), D("D"), F("F");
    private final String label;
    Tier(String label) { this.label = label; }
    @Override public String toString() { return label; }
}
