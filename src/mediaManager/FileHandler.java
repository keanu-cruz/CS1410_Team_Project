package mediaManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * FileHandler reads and writes Library data from a .txt file.
 * @author Team Assignment: Keanu Cruz + Logan Chess
 */
public class FileHandler {

    /**
     * Saves a library to a file using JFileChooser
     * @param library
     * @param file
     * @throws IOException
     */
    public static void save(Library library, File file) throws IOException {

        if(!file.getName().endsWith(".txt")){
            file = new File(file.getAbsolutePath() + ".txt");
        }

        PrintWriter writer = new PrintWriter(new FileWriter(file));

        for (Artist artist : library.getArtists()){
            writer.println("ARTIST|" + artist.getName());

            for (Album album : artist.getAlbums()){
                writer.println("ALBUM|" + album.getName());

                for (Song song : album.getSongs()){
                    writer.println("Song|" + song.getTitle() + "|" + song.getDuration());
                }
            }
        }
        writer.close();
    }

    public static Library load(File file) throws IOException{

        Library library = new Library();

        Scanner scanner = new Scanner(file);

        Artist currentArtist = null;
        Album currentAlbum = null;

        while (scanner.hasNextLine()){
            String line = scanner.nextLine();

            String[] parts = line.split("\\|");

            switch (parts[0]){

                case "ARTIST":
                    currentArtist = new Artist((parts[1]));
                    library.addArtist(currentArtist);
                    break;

                case "ALBUM":
                    if (currentArtist == null) continue;

                    currentAlbum = new Album(parts[1]);
                    currentArtist.addAlbum(currentAlbum);
                    break;

                case "SONG":
                    if (currentAlbum == null) continue;

                    String title = parts[1];
                    double duration = Double.parseDouble(parts[2]);

                    currentAlbum.addSong(new Song(title, duration));
                    break;
            }
        }
        scanner.close();
        return library;
    }
}
