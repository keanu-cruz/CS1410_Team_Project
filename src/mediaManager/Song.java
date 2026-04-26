package mediaManager;

/**
 * A song is a singular musical composition that has been recorded.
 * 
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
	 * @return duration
	 */
	public double getDuration() {
		return duration;
	}

	/**
	 * Returns song duration minutes counter.
	 * 
	 * @return minutes
	 */
	public int getMinutes() {
		return (int) duration;
	}

	/**
	 * Returns song duration seconds counter.
	 * 
	 * @return seconds
	 */
	public int getSeconds() {
		return (int) Math.round((duration - getMinutes()) * 100);
	}

	/**
	 * Returns a String with title truncated if bigger than 28 and duration of song.
	 *
	 * @return
	 */
	@Override
	public String toString() {
		int minutes = (int) duration;
		int seconds = (int) Math.round((duration - minutes) * 100);

		int TITLE_WIDTH = 28; // Max Title Width allowed

		String displayTitle = title;

		if (displayTitle.length() > TITLE_WIDTH) {
			displayTitle = displayTitle.substring(0, TITLE_WIDTH - 3) + "...";
		}

		return String.format("%-" + TITLE_WIDTH + "s %5d:%02d", displayTitle, minutes, seconds);
	}
}
