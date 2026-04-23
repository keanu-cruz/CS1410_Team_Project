package mediaManager;

/**
 * A song is a singular musical composition that has been recorded.
 * @author Team Assignment: Keanu Cruz + Logan Chess
 */
public class Song {
    private String title;
    private double duration;

    /**
     * Initializes the fields title and duration for a song.
     *
     * @param title
     * @param duration
     */
    public Song(String title, double duration) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Song title cannot be empty");
        }

        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }

        this.title = Library.inputFormatter(title);
        this.duration = duration;
    }

    /**
     * Returns title of a song.
     *
     * @return
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns duration of a song.
     *
     * @return
     */
    public double getDuration() {
        return duration;
    }

    /**
     * Returns a String in the following format:
     * {title}|{duration}
     *
     * @return
     */
    @Override
    public String toString() {
        int minutes = (int) duration;
        int seconds = (int) Math.round((duration - minutes) * 100);

        return String.format("%-25s %5d:%02d", title, minutes, seconds);
    }
}
