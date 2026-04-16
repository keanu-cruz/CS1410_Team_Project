package mediaManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Keanu Cruz
 */
public class TestApp {
    public static void main(String[] args) {

        Library library = new Library();

        library.addArtist(new Artist("Green Day"));
        library.addArtist(new Artist("Blink 182"));
        library.addArtist(new Artist("Brand New"));

        System.out.println(library.getArtists());

        Artist greenDay = library.getArtists().get(0);
        Artist blink = library.getArtists().get(1);
        Artist brandNew = library.getArtists().get(2);

        greenDay.addAlbum(new Album("Dookie"));
        greenDay.addAlbum(new Album("AmeRicaN iDiot"));

        Album dookie = greenDay.getAlbums().get(0);
        Album americanIdiot = greenDay.getAlbums().get(1);

        dookie.addSong(new Song("Burnout", 2.07));
        dookie.addSong(new Song("Having a Blast", 2.44));
        dookie.addSong(new Song("Chump", 2.53));
        dookie.addSong(new Song("Longview", 3.59));

        americanIdiot.addSong(new Song("American Idiot", 3.08));


        for (Artist a : library.getArtists()){
            System.out.println("Artist: " + a.getName() + " Album: " + a.getAlbums());
            for (Album al : a.getAlbums()){
                System.out.println(al.getName() + " Songs: " + al.getSongs());
            }
        }

        // Search Artist
        System.out.println(library.searchSongs("Green Day"));
        System.out.println();

        // Search Album
        System.out.println(library.searchSongs("Dookie"));
        System.out.println();

        // Search Song
        System.out.println(library.searchSongs("Burnout"));
        System.out.println();

        library.removeArtist(greenDay);
        System.out.println(library.getArtists());
        System.out.println();

        System.out.println(library.searchSongs("Dookie"));
        System.out.println();


    }
}
