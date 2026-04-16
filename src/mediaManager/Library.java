package mediaManager;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Team Assignment: Keanu Cruz + Logan Chess
 */
public class Library {

    private List<Artist> artists = new ArrayList<>();

    /**
     * Returns a list of Artists in a Library.
     * @return
     */
    public List<Artist> getArtists() {
        return artists;
    }

    /**
     * Adds an Artist to list of Artists in a Library.
     * @param artist
     * @return
     */
    public boolean addArtist(Artist artist){
        for (Artist a : artists){
            if (a.getName().equalsIgnoreCase(artist.getName())){
                return false;
            }
        }
        artists.add(artist);
        return true;
    }

    /**
     * Removes an Artist from a Library
     * @param artist
     */
    public void removeArtist(Artist artist){
        artists.remove(artist);
    }

    /**
     * Returns a song if query is found anywhere in a library.
     * @param query
     * @return
     */
    public List<Song> searchSongs(String query){
        List<Song> results = new ArrayList<>();

        query = query.toLowerCase();

        for (Artist artist: artists){
            for (Album album : artist.getAlbums()){
                for (Song song : album.getSongs()){

                    if (artist.getName().toLowerCase().contains(query) ||
                    album.getName().toLowerCase().contains(query) ||
                    song.getTitle().toLowerCase().contains(query)){

                        results.add(song);
                    }
                }
            }
        }

        return new ArrayList<>(results);
    }

    /**
     * Helper Method:
     * <p>Formats inputted information and adds a capital to index 0 and lowercase
     * for the rest of the word.</p>
     * <p>For example: for the input = "EaGleS" the output would be "Eagles"</p>
     * @param input
     * @return
     */
    public static String inputFormatter(String input){
//        if (input == null){
//            throw  new IllegalArgumentException("Input cannot be null");
//        }

        input = input.trim();

//        if (input.isEmpty()){
//            throw new IllegalArgumentException("Input cannot be empty");
//        }

        String[] words = input.toLowerCase().split("\\s+");
        StringBuilder result = new StringBuilder();

        for (String word : words){
            result.append(Character.toUpperCase(word.charAt(0)));
            result.append(word.substring(1));
            result.append(" ");

        }

        return result.toString().trim();
    }
}
