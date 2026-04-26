package mediaManager;

import java.util.ArrayList;
import java.util.List;

/**
 * An Album represents a catalog of songs
 * 
 * @author Team Assignment: Keanu Cruz + Logan Chess
 */
public class Album {
	private String name;
	private List<Song> songs = new ArrayList<>();

	/**
	 * Intializes the field name for an Album
	 * 
	 * @param name
	 */
	public Album(String name) {
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("Album name cannot be empty");
		}

		this.name = Library.inputFormatter(name);
	}

	/**
	 * Returns Album name
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns list of songs on an Album
	 * 
	 * @return
	 */
	public List<Song> getSongs() {
		return songs;
	}

	/**
	 * Adds a song to list of songs only if it isn't already contained in album.
	 * 
	 * @param song
	 * @return
	 */
	public boolean addSong(Song song) {
		for (Song s : songs) {
			if (s.getTitle().equalsIgnoreCase(song.getTitle())) {
				System.out.println("Song Not Added");
				return false;
			}
		}
		songs.add(song);
		return true;
	}

	/**
	 * Removes a song from an Album's songs listing
	 * 
	 * @param song
	 */
	public void removeSong(Song song) {
		songs.remove(song);
	}

	/**
	 * Returns a String with the name of the album
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		return name;
	}
}
