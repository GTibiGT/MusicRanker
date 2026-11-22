package musicranker;

import musicranker.model.Question;
import musicranker.model.Rating;
import musicranker.model.Song;
import musicranker.service.RankingService;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.SortedMap;


public class Main {
    private static List<Question> QUESTIONS = List.of(
        new Question("Listenability", "How likely are you to listen to the entire song if it comes on shuffle?", 15),
        new Question("Emotional Impact", "How strong is the emotion you feel while listening to this song?", 15),
        new Question("Replayability", "How often do you intentionally seek out this song to listen to it?", 15),
        new Question("Musical Quality", "How much do you appreciate the instrumentation, melodies, vocals or production?", 15),
        new Question("Personal Attachment", "How disappointed would you be if you could never listen to this song again?", 10),
        new Question("Live Appeal", "How much would you want to experience this song performed live?", 10),
        new Question("Memorability", "How memorable are the riffs, melodies, lyrics or rhythms of this song?", 10),
        new Question("Versatility", "How well does this song fit different moods, environments and situations?", 10)
    );
    
    private Main() {}

    public static void main(String[] args){
        RankingService ranking = new RankingService(QUESTIONS);
        List<Song> songs = new ArrayList<>();
        try (Scanner scanner = new Scanner(System.in)){
            System.out.println("=== MUSIC RANKER ===");
            boolean running = true;
            while (running){
                System.out.println("\n1. Rate a song\n2. View rankings\n3. Exit");
                int choice = readInt(scanner, "Choose an option: ", 1, 3);
                switch (choice) {
                    case 1 -> {
                        Song song = rateSong(scanner, ranking);
                        songs.add(song);
                        System.out.printf("\nSaved: %s — %s | %.1f/100 | Tier %s%n",
                            song.getTitle(), song.getArtist(), ranking.calculateScore(song), ranking.determineTier(song));
                    }
                    case 2 -> showRankings(songs, ranking);
                    case 3 -> running = false;
                    default -> throw new IllegalStateException("Unexpected menu option");
                }
            }
            System.out.println("Goodbye! Note: Phase 1 ratings are kept only until the program exits.");
        }
    }
private static Song rateSong(Scanner scanner, RankingService ranking) {
        String title = readRequired(scanner, "Song title: ");
        String artist = readRequired(scanner, "Artist: ");
        List<Integer> answers = new ArrayList<>();
        System.out.println("\nRate each question from 1 (lowest) to 10 (highest):");
        List<Question> questions = ranking.getQuestions();
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            System.out.printf("\n%d. [%s, weight %d%%]%n%s%n", i + 1, q.category(), q.weight(), q.prompt());
            answers.add(readInt(scanner, "Rating (1-10): ", 1, 10));
        }
        System.out.print("\nWhat emotion(s) does this song evoke? (optional): ");
        String emotion = scanner.nextLine();
        System.out.print("Personal story or additional notes (optional): ");
        String notes = scanner.nextLine();
        return new Song(title, artist, new Rating(answers), emotion, notes);
    }

    private static void showRankings(List<Song> songs, RankingService ranking) {
        if (songs.isEmpty()) {
            System.out.println("\nNo songs rated yet. Choose option 1 to add one.");
            return;
        }
        System.out.println("\n=== YOUR SONG RANKINGS ===");
        int position = 1;
        for (Song song : ranking.rankSongs(songs)) {
            System.out.printf("%d. %s — %s | %.1f/100 | Tier %s%n",
                position++, song.getTitle(), song.getArtist(), ranking.calculateScore(song), ranking.determineTier(song));
            if (!song.getEmotion().isBlank()) System.out.println("   Emotions: " + song.getEmotion());
            if (!song.getNotes().isBlank()) System.out.println("   Notes: " + song.getNotes());
        }
    }

    private static String readRequired(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("This field cannot be empty.");
        }
    }

    private static int readInt(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int number = Integer.parseInt(input);
                if (number >= min && number <= max) return number;
            } catch (NumberFormatException ignored) {
                // Ask again if the input is not an integer.
            }
            System.out.printf("Please enter a whole number between %d and %d.%n", min, max);
        }
    }
}
