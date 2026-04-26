package mediaManager;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * A library is a collection of artists that holds albums that holds songs.
 * 
 * @author Team Assignment: Keanu Cruz + Logan Chess
 */
public class Library {

	private List<Artist> artists = new ArrayList<>();

	/**
	 * Returns a list of Artists in a Library.
	 * 
	 * @return
	 */
	public List<Artist> getArtists() {
		return artists;
	}

	/**
	 * Adds an Artist to list of Artists in a Library.
	 * 
	 * @param artist
	 * @return
	 */
	public boolean addArtist(Artist artist) {
		for (Artist a : artists) {
			if (a.getName().equalsIgnoreCase(artist.getName())) {
				System.out.println("Artist Not Added");
				return false;
			}
		}
		artists.add(artist);
		return true;
	}

	/**
	 * Removes an Artist from a Library
	 * 
	 * @param artist
	 */
	public void removeArtist(Artist artist) {
		artists.remove(artist);
	}

	/**
	 * Returns query search results entered by User in Media Collection Library.
	 * <ul>
	 * <li>If searching Artist search returns Albums</li>
	 * <li>If searching Album search returns Albums</li>
	 * <li>If searching songs search returns Songs</li>
	 * </ul>
	 * 
	 * @param query
	 * @return
	 */
	public List<Object> search(String query) {
		if (query == null || query.isBlank()) {
			return new ArrayList<>();
		}

		String q = query.toLowerCase();
		// the linked hashset prevents duplicates from artists and albums
		LinkedHashSet<Object> results = new LinkedHashSet<>();

		for (Artist artist : artists) {
			List<Album> albums = artist.getAlbums();

			// match artist > returns albums
			if (artist.getName().toLowerCase().contains(q)) {
				results.addAll(artist.getAlbums());
			}

			for (Album album : artist.getAlbums()) {

				// match album > return a album
				if (album.getName().toLowerCase().contains(q)) {
					results.add(album);
				}

				// match song > return a song
				for (Song song : album.getSongs()) {
					if (song.getTitle().toLowerCase().contains(q)) {
						results.add(song);
					}
				}
			}
		}

		return new ArrayList<>(results);
	}

	/**
	 * Helper Method:
	 * <p>
	 * Formats inputted information and adds a capital to index 0 and lowercase for
	 * the rest of the word.
	 * </p>
	 * <p>
	 * For example: for the input = "EaGleS" the output would be "Eagles"
	 * </p>
	 * 
	 * @param input
	 * @return
	 */
	public static String inputFormatter(String input) {
//        if (input == null){
//            throw  new IllegalArgumentException("Input cannot be null");
//        }

		input = input.trim();

//        if (input.isEmpty()){
//            throw new IllegalArgumentException("Input cannot be empty");
//        }

		String[] words = input.toLowerCase().split("\\s+");
		StringBuilder result = new StringBuilder();

		for (String word : words) {
			result.append(Character.toUpperCase(word.charAt(0)));
			result.append(word.substring(1));
			result.append(" ");

		}

		return result.toString().trim();
	}
}
