public class Song {
    private String title;
    private String artist;
    private int[] ratings;
    private String notes;

    public Song(String title, String artist, int[] ratings, String notes){
        this.title = title;
        this.artist = artist;
        this.ratings = ratings.clone();
        this.notes = notes;
    }

    public String getTitle(){
        return title;
    }

    public String getArtist(){
        return artist;
    }

    public int[] getRatings(){
        return ratings.clone();
    }

    public String getNotes(){
        return notes;
    }

        //songs.sort(
        //          Comparator.comparingDouble(
        //              RankingCalc::calcScore
        //          ).reversed()
        //      );

}

