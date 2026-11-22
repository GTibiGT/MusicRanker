package musicranker;

import musicranker.model.Question;
import musicranker.model.Rating;
import musicranker.model.Song;
import musicranker.model.Tier;
import musicranker.service.RankingService;
import musicranker.service.SongService;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import musicranker.repository.DatabaseManager;
import musicranker.repository.SongRepository;
import java.sql.SQLException;

public final class Main {
    private static final List<Question> QUESTIONS = List.of(
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

    
    public static void main(String[] args) {

        // Phase 1: Ranking system
        RankingService ranking = new RankingService(QUESTIONS);

        // Phase 2: Song management
        SongService library = new SongService(ranking);

        // Phase 3: Database
        SongRepository repository = new SongRepository();

        // Initialize database and load saved songs
        try {
            DatabaseManager.initializeDatabase();

            for (Song song : repository.loadAll()) {
                library.add(song);
            }

            System.out.println("Music library loaded successfully.");
            System.out.println("Loaded " + library.count() + " songs.");

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            return;
        }

        // Main application loop
        try (Scanner scanner = new Scanner(System.in)) {

            boolean running = true;

            while (running) {

                System.out.println("\n=== MUSIC RANKER | "
                        + library.count() + " songs ===");

                System.out.println("1. Add song");
                System.out.println("2. View rankings");
                System.out.println("3. Search songs");
                System.out.println("4. View song details");
                System.out.println("5. Edit song");
                System.out.println("6. Delete song");
                System.out.println("7. Filter songs");
                System.out.println("8. Exit");

                switch (readInt(scanner, "Choose: ", 1, 8)) {

                    case 1 -> {
                        addSong(scanner, library, ranking);
                        saveLibrary(repository, library);
                    }

                    case 2 -> printSongs(
                            library.getAllRanked(), ranking);

                    case 3 -> {
                        String term = readRequired(
                                scanner, "Search title or artist: ");

                        printSongs(library.search(term), ranking);
                    }

                    case 4 -> showDetails(
                            scanner, library, ranking);

                    case 5 -> {
                        editSong(scanner, library, ranking);
                        saveLibrary(repository, library);
                    }

                    case 6 -> {
                        deleteSong(scanner, library, ranking);
                        saveLibrary(repository, library);
                    }

                    case 7 -> filterSongs(
                            scanner, library, ranking);

                    case 8 -> {
                        if (saveLibrary(repository, library)) {
                            running = false;
                        } else {
                            System.out.println(
                                    "Exit cancelled because saving failed.");
                        }
                    }
                }
            }
        }

        System.out.println("Goodbye!");
    }


    private static void addSong(Scanner scanner, SongService library, RankingService ranking) {
        Song song = createSong(scanner, ranking);
        if (!library.add(song)) {
            System.out.println("That title and artist are already in your library. Use Edit instead.");
            return;
        }
        System.out.printf("Added: %s — %s | %.1f/100 | %s%n", song.getTitle(), song.getArtist(),
                ranking.calculateScore(song), ranking.determineTier(song));
    }

    private static Song createSong(Scanner scanner, RankingService ranking) {
        String title = readRequired(scanner, "Song title: ");
        String artist = readRequired(scanner, "Artist: ");
        Rating rating = askRatings(scanner, ranking, null);
        String emotion = readOptional(scanner, "Emotions (optional): ");
        String notes = readOptional(scanner, "Personal story / notes (optional): ");
        return new Song(title, artist, rating, emotion, notes);
    }

    private static Rating askRatings(Scanner scanner, RankingService ranking, Rating previous) {
        List<Integer> answers = new ArrayList<>();
        List<Question> questions = ranking.getQuestions();
        System.out.println("\nAnswer from 1 (lowest) to 10 (highest).");
        if (previous != null) System.out.println("Press Enter to keep an existing rating.");
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            System.out.printf("%n%d. %s (%d%%)%n%s%n", i + 1, q.category(), q.weight(), q.prompt());
            int answer = previous == null
                    ? readInt(scanner, "Rating (1-10): ", 1, 10)
                    : readIntOrKeep(scanner, "Rating (1-10) [" + previous.getAnswers().get(i) + "]: ",
                            1, 10, previous.getAnswers().get(i));
            answers.add(answer);
        }
        return new Rating(answers);
    }

    private static void showDetails(Scanner scanner, SongService library, RankingService ranking) {
        if (library.count() == 0) { System.out.println("No songs yet."); return; }
        printSongs(library.getAllRanked(), ranking);
        Song song = library.getByRankedIndex(readInt(scanner, "Song number: ", 1, library.count()) - 1);
        System.out.printf("%n=== %s — %s ===%nScore: %.1f/100 | Tier: %s%n",
                song.getTitle(), song.getArtist(), ranking.calculateScore(song), ranking.determineTier(song));
        for (int i = 0; i < ranking.getQuestions().size(); i++) {
            System.out.printf("%s: %d/10%n", ranking.getQuestions().get(i).category(),
                    song.getRating().getAnswers().get(i));
        }
        System.out.println("Emotions: " + (song.getEmotion().isBlank() ? "(none)" : song.getEmotion()));
        System.out.println("Notes: " + (song.getNotes().isBlank() ? "(none)" : song.getNotes()));
    }

    private static void editSong(Scanner scanner, SongService library, RankingService ranking) {
        if (library.count() == 0) { System.out.println("No songs to edit."); return; }
        printSongs(library.getAllRanked(), ranking);
        int index = readInt(scanner, "Song number to edit: ", 1, library.count()) - 1;
        Song old = library.getByRankedIndex(index);
        System.out.println("Press Enter to keep a value. Enter '-' to clear emotions or notes.");
        String title = readKeep(scanner, "Title [" + old.getTitle() + "]: ", old.getTitle(), false);
        String artist = readKeep(scanner, "Artist [" + old.getArtist() + "]: ", old.getArtist(), false);
        System.out.print("Edit ratings? (y/N): ");
        boolean editRatings = scanner.nextLine().trim().equalsIgnoreCase("y");
        Rating rating = editRatings ? askRatings(scanner, ranking, old.getRating()) : old.getRating();
        String emotion = readKeep(scanner, "Emotions [" + old.getEmotion() + "]: ", old.getEmotion(), true);
        String notes = readKeep(scanner, "Notes [" + old.getNotes() + "]: ", old.getNotes(), true);
        Song replacement = new Song(title, artist, rating, emotion, notes);
        if (!library.update(index, replacement)) {
            System.out.println("Edit cancelled: another song already has that title and artist.");
        } else {
            System.out.printf("Updated! New score: %.1f/100 | Tier: %s%n",
                    ranking.calculateScore(replacement), ranking.determineTier(replacement));
        }
    }

    private static void deleteSong(Scanner scanner, SongService library, RankingService ranking) {
        if (library.count() == 0) { System.out.println("No songs to delete."); return; }
        printSongs(library.getAllRanked(), ranking);
        int index = readInt(scanner, "Song number to delete: ", 1, library.count()) - 1;
        Song song = library.getByRankedIndex(index);
        System.out.printf("Delete '%s' by %s? Type DELETE to confirm: ", song.getTitle(), song.getArtist());
        if (scanner.nextLine().trim().equals("DELETE")) {
            library.delete(index);
            System.out.println("Song deleted.");
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    
    private static void filterSongs(
            Scanner scanner,
            SongService library,
            RankingService ranking) {

        System.out.println("\n=== FILTER SONGS ===");
        System.out.println("1. By artist");
        System.out.println("2. By tier");
        System.out.println("3. By minimum score");

        int option = readInt(scanner, "Filter: ", 1, 3);

        switch (option) {

            case 1 -> {
                String artist = readRequired(
                        scanner, "Artist name: ");

                printSongs(
                        library.filterByArtist(artist),
                        ranking);
            }

            case 2 -> {
                Tier tier = readTier(scanner);

                printSongs(
                        library.filterByTier(tier),
                        ranking);
            }

            case 3 -> {
                double minimum = readDouble(
                        scanner,
                        "Minimum score (0-100): ",
                        0,
                        100);

                printSongs(
                        library.filterByMinimumScore(minimum),
                        ranking);
            }
        }
    }


    private static Tier readTier(Scanner scanner) {
        while (true) {
            System.out.print("Tier (S+, S, A, B, C, D, F): ");
            String input = scanner.nextLine().trim().toUpperCase();
            for (Tier tier : Tier.values()) {
                if (tier.toString().equals(input)) return tier;
            }
            System.out.println("Unknown tier.");
        }
    }

    private static void printSongs(List<Song> songs, RankingService ranking) {
        if (songs.isEmpty()) { System.out.println("No matching songs."); return; }
        System.out.println("\n=== SONGS (HIGHEST SCORE FIRST) ===");
        for (int i = 0; i < songs.size(); i++) {
            Song song = songs.get(i);
            System.out.printf("%d. %s — %s | %.1f/100 | %s%n", i + 1, song.getTitle(),
                    song.getArtist(), ranking.calculateScore(song), ranking.determineTier(song));
        }
        System.out.println("Note: To edit/delete/view details, select the song number from the full rankings menu.");
    }

    private static String readRequired(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String text = scanner.nextLine().trim();
            if (!text.isEmpty()) return text;
            System.out.println("This field cannot be empty.");
        }
    }

    private static String readOptional(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static String readKeep(Scanner scanner, String prompt, String old, boolean allowClear) {
        while (true) {
            String input = readOptional(scanner, prompt);
            if (input.isEmpty()) return old;
            if (allowClear && input.equals("-")) return "";
            return input;
        }
    }

    private static int readInt(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= min && value <= max) return value;
            } catch (NumberFormatException ignored) { }
            System.out.printf("Enter a whole number from %d to %d.%n", min, max);
        }
    }

    private static int readIntOrKeep(Scanner scanner, String prompt, int min, int max, int old) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) return old;
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) return value;
            } catch (NumberFormatException ignored) { }
            System.out.printf("Enter a whole number from %d to %d, or press Enter.%n", min, max);
        }
    }

    private static double readDouble(Scanner scanner, String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt);
            try {
                double value = Double.parseDouble(scanner.nextLine().trim());
                if (Double.isFinite(value) && value >= min && value <= max) return value;
            } catch (NumberFormatException ignored) { }
            System.out.printf("Enter a number from %.0f to %.0f.%n", min, max);
        }
    }

    private static boolean saveLibrary(SongRepository repository, SongService library) {
        try {
            repository.saveAll(library.getAllRanked());
            System.out.println("Library saved successfully.");
            return true;

        } catch (SQLException e) {
            System.err.println(
                    "Could not save library: " + e.getMessage());
            System.err.println(
                    "Changes are only in memory. Do not exit yet.");
            return false;
        }
    }
}
