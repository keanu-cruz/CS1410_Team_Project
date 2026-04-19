package mediaManager;


import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;

/**
 * Main window for the media collection manager.
 *
 * The UI has a two-panel layout: a fixed Artists list on the left and a
 * content pane on the right. Navigation depth is derived from two main fields:
 * {@code selectedArtist} and {@code selectedAlbum}.
 *
 * Each panel owns its own "+" button. The left "+" button always adds an {@link Artist}.
 * The right "+" is context-dependent: it adds an {@link Album} when an artist is
 * selected and adds a {@link Song} when an album is selected. When nothing is selected
 * (all-songs view), the right "+" is disabled because adding a song
 * requires an album context.
 * 
 * File operations are delegated to {@link FileHandler}. The current file
 * is tracked by currentFile, if set. If not set, it performs in-memory
 * writes. Note: File operations are not fully implemented.
 *
 * @author Keanu Cruz, Logan Chess
 * @see Library
 * @see Artist
 * @see Album
 * @see Song
 * @see FileHandler
 */

public class MainGUI extends JFrame {
    private Library library;
    private Artist selectedArtist;
    private Album selectedAlbum;
    private JList<Artist> artistList;
    private JList<Object> contentList;
    private JTextField searchField;
    
    
    private final DefaultListModel<Artist> artistModel = new DefaultListModel<>();
    private final DefaultListModel<Object> contentModel = new DefaultListModel<>(); // this is Object because it must hold either Album or Song
    private File currentFile;
    private JLabel contentHeader;
    private JButton addContentButton;
    private JButton backButton;
    private boolean searchActive = false;	// whether or not search bar is being used.
    
    public MainGUI() {
        super("Media Collection");
        this.library = new Library();
 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(6, 6));
 
        setJMenuBar(buildMenuBar());
        add(buildSearchBar(), BorderLayout.NORTH);
        add(buildArtistPanel(), BorderLayout.WEST);
        add(buildContentPanel(), BorderLayout.CENTER);
 
        refreshArtistList();
        refreshContentPane();
    }
    
    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
 
        JMenuItem openItem = new JMenuItem("Open...");
        openItem.addActionListener(e -> onOpen());
        fileMenu.add(openItem);
 
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(e -> onSave());
        fileMenu.add(saveItem);
 
        JMenuItem saveAsItem = new JMenuItem("Save As...");
        saveAsItem.addActionListener(e -> onSaveAs());
        fileMenu.add(saveAsItem);
 
        bar.add(fileMenu);
        return bar;
    }
 
    private JPanel buildSearchBar() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 0, 6));
        panel.add(new JLabel("Search: "), BorderLayout.WEST);
 
        searchField = new JTextField();
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { onSearchChanged(); }
            @Override public void removeUpdate(DocumentEvent e)  { onSearchChanged(); }
            @Override public void changedUpdate(DocumentEvent e) { onSearchChanged(); }
        });
        panel.add(searchField, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel buildArtistPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 6, 6, 0));
        panel.setPreferredSize(new Dimension(220, 0));
 
        JPanel header = new JPanel(new BorderLayout());
        header.add(new JLabel("Artists"), BorderLayout.WEST);
 
        JButton addArtistButton = new JButton("+");
        addArtistButton.setToolTipText("Add artist");
        addArtistButton.addActionListener(e -> onAddArtist());
        header.add(addArtistButton, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);
 
        artistList = new JList<>(artistModel);
        artistList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        artistList.addListSelectionListener(this::onArtistSelectionChanged);
        panel.add(new JScrollPane(artistList), BorderLayout.CENTER);
 
        return panel;
    }
 
    private JPanel buildContentPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 6));
 
        JPanel header = new JPanel(new BorderLayout());
        contentHeader = new JLabel("All Songs");
        header.add(contentHeader, BorderLayout.WEST);
 
        JPanel headerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        backButton = new JButton("Back");
        backButton.setEnabled(false);
        backButton.addActionListener(e -> onBack());
        headerButtons.add(backButton);
 
        addContentButton = new JButton("+");
        addContentButton.setEnabled(false);
        addContentButton.addActionListener(e -> onAddContent());
        headerButtons.add(addContentButton);
 
        header.add(headerButtons, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);
 
        contentList = new JList<>(contentModel);
        contentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contentList.addListSelectionListener(this::onContentSelectionChanged);
        panel.add(new JScrollPane(contentList), BorderLayout.CENTER);
 
        return panel;
    }
    
    private void onArtistSelectionChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        selectedArtist = artistList.getSelectedValue();
        selectedAlbum = null;

        if (selectedArtist != null && searchActive) {
            searchField.setText("");
            return;
        }
        refreshContentPane();
    }
    
    private void onContentSelectionChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        Object selected = contentList.getSelectedValue();
        if (selected instanceof Album album) {
            selectedAlbum = album;
            refreshContentPane();
        }
    }
 
    private void onAddArtist() {
        System.out.println("[GUI] Add Artist button clicked");
        String name = JOptionPane.showInputDialog(this, "Artist name:",
                "Add Artist", JOptionPane.PLAIN_MESSAGE);
        Artist artist = new Artist(name.trim());
        library.addArtist(artist);
        System.out.println("[GUI] Added artist: " + artist.getName());
        autoSave();
        refreshArtistList();
    }
    
    private void onAddContent() {
        if (selectedAlbum != null) {
            System.out.println("[GUI] Add button clicked, context=song, album=" + selectedAlbum.getName());
            showAddSongDialog(selectedAlbum);
        } else if (selectedArtist != null) {
            System.out.println("[GUI] Add button clicked, context=album, artist=" + selectedArtist.getName());
            showAddAlbumDialog(selectedArtist);
        }
    }
    
    private void onBack() {
        if (selectedAlbum != null) {
            System.out.println("[GUI] Back button clicked: songs -> albums view (" + selectedArtist.getName() + ")");
            selectedAlbum = null;
            contentList.clearSelection();
        } else if (selectedArtist != null) {
            System.out.println("[GUI] Back button was clicked: albums -> all songs view");
            selectedArtist = null;
            artistList.clearSelection();
        }
        refreshContentPane();
    }
 
    private void onSearchChanged() {
        String query = searchField.getText();
        searchActive = !query.isBlank();
        if (searchActive) {
            // Search overrides navigation
            selectedArtist = null;
            selectedAlbum = null;
            artistList.clearSelection();
        }
        refreshContentPane();
    }
    
    private void showAddAlbumDialog(Artist parent) {
        String name = JOptionPane.showInputDialog(this, "Album name:",
                "Add Album", JOptionPane.PLAIN_MESSAGE);
        Album album = new Album(name.trim());
        parent.addAlbum(album);
        System.out.println("[GUI] Added album '" + album.getName()
                + "' to artist '" + parent.getName() + "'");
        autoSave();
        refreshContentPane();
    }
    private void showAddSongDialog(Album parent) {
        JTextField titleField = new JTextField();
        JTextField durationField = new JTextField();
        Object[] form = {
                "Title:", titleField,
                "Duration (M:SS):", durationField
        };
        int result = JOptionPane.showConfirmDialog(this, form, "Add Song",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
 
        String title = titleField.getText().trim();
        String durationStr = durationField.getText().trim();
 
        Double duration = parseDuration(durationStr); 
        Song song = new Song(title, duration);
        parent.addSong(song);
        System.out.println("[GUI] Added song '" + song.getTitle() + "' to album '" + parent.getName() + "'");
        autoSave();
        refreshContentPane();
    }
 
    /**
     * This method parses duration input. Accepts "M:SS" (e.g., "3:45") or plain 
     * decimal ("3.45"). Returns null if the string is malformed. The stored encoding
     * matches Song.toString: 3.45 represents 3 minutes 45 seconds.
     */
    private Double parseDuration(String input) {
        try {
            if (input.contains(":")) {
                String[] parts = input.split(":");
                if (parts.length != 2) return null;
                int minutes = Integer.parseInt(parts[0].trim());
                int seconds = Integer.parseInt(parts[1].trim());
                if (minutes < 0 || seconds < 0 || seconds >= 60) return null;
                return minutes + seconds / 100.0;
            }
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private void refreshArtistList() {
        artistModel.clear();
        for (Artist a : library.getArtists()) {
            artistModel.addElement(a);
        }
    }
    
    private void refreshContentPane() {
        contentModel.clear();
 
        if (searchActive) {
            contentHeader.setText("Search results");
            for (Song s : library.searchSongs(searchField.getText())) {
                contentModel.addElement(s);
            }
            addContentButton.setEnabled(false);
            addContentButton.setToolTipText(null);
            backButton.setEnabled(false);
            return;
        }
 
        if (selectedAlbum != null) {
            contentHeader.setText("Songs - " + selectedAlbum.getName());
            for (Song s : selectedAlbum.getSongs()) {
                contentModel.addElement(s);
            }
            addContentButton.setEnabled(true);
            addContentButton.setToolTipText("Add song to this album");
            backButton.setEnabled(true);
        } else if (selectedArtist != null) {
            contentHeader.setText("Albums - " + selectedArtist.getName());
            for (Album a : selectedArtist.getAlbums()) {
                contentModel.addElement(a);
            }
            addContentButton.setEnabled(true);
            addContentButton.setToolTipText("Add album to this artist");
            backButton.setEnabled(true);
        } else {
            contentHeader.setText("All Songs");
            for (Song s : getAllSongs()) {
                contentModel.addElement(s);
            }
            addContentButton.setEnabled(false);
            addContentButton.setToolTipText(null);
            backButton.setEnabled(false);
        }
    }
    
    private List<Song> getAllSongs() {
        List<Song> all = new ArrayList<>();
        for (Artist a : library.getArtists()) {
            for (Album al : a.getAlbums()) {
                all.addAll(al.getSongs());
            }
        }
        return all;
    }
    
    private void onOpen() {
        System.out.println("[GUI] File > Open clicked");
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            System.out.println("[GUI] File open cancelled");
            return;
        }
        File file = chooser.getSelectedFile();
        System.out.println("[GUI] File loaded " + file.getName() + " (not implemented)");
    }
 
    private void onSave() {
        System.out.println("[GUI] File > Save clicked");
        if (currentFile == null) {
            onSaveAs();
            return;
        }
        System.out.println("[GUI] Saved file " + currentFile.getName() + " (not implemented)");
    }
 
    private void onSaveAs() {
        System.out.println("[GUI] File > Save As clicked");
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            System.out.println("[GUI] File save cancelled");
            return;
        }
        File file = chooser.getSelectedFile();
        System.out.println("[GUI] Saved file " + file.getName() + " (not implemented)");
    }
    
    private void autoSave() {
        if (currentFile == null) {
            System.out.println("File saved in-memory (no file was set)");
            return;
        }
        else {
        	System.out.println("[GUI] auto-saved to " + currentFile.getName() + " (not implemented)");	
        }
    }
 
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainGUI().setVisible(true));
    }
}
