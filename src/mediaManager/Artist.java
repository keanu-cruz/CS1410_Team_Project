package mediaManager;

import java.util.ArrayList;
import java.util.List;

/**
 * An Artist is the performer of songs in an Album
 * 
 * @author Team Assignment: Keanu Cruz + Logan Chess
 */
public class Artist {
	private String name;
	private List<Album> albums = new ArrayList<>();

	/**
	 * Initializes the field name for an Artist
	 * 
	 * @param name
	 */
	public Artist(String name) {
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("Artist name cannont be empty");
		}

		this.name = Library.inputFormatter(name);
	}

	/**
	 * Returns the Artist's name
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns catalog of albums from an Artist.
	 * 
	 * @return
	 */
	public List<Album> getAlbums() {
		return albums;
	}

	/**
	 * Adds an album to albums list.
	 * 
	 * @param album
	 * @return
	 */
	public boolean addAlbum(Album album) {
		for (Album a : albums) {
			if (a.getName().equalsIgnoreCase(album.getName())) {
				System.out.println("Album Not Added");
				return false;
			}
		}
		albums.add(album);
		return true;
	}

	/**
	 * Removes an album from albums list.
	 * 
	 * @param album
	 */
	public void removeAlbum(Album album) {
		albums.remove(album);
	}

	/**
	 * Returns a String with the name of Artist
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		return name;
	}
}
