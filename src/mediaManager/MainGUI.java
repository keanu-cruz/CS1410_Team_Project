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
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.border.LineBorder;
import javax.swing.border.CompoundBorder;

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
    private Song selectedSong;
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
	private JButton removeContentButton;
	private boolean internalChange = false; // Guard for onSearchChanged
	private boolean autoSaveEnabled = false;
	private JCheckBoxMenuItem autoSaveOnOff;
    
    public MainGUI() {
        super("Media Collection Manager");
        getContentPane().setBackground(new Color(23, 25, 32));
        setBackground(new Color(84, 98, 126));
        this.library = new Library();
 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);
        getContentPane().setLayout(new BorderLayout(6, 6));
 
        setJMenuBar(buildMenuBar());
        getContentPane().add(buildSearchBar(), BorderLayout.NORTH);
        getContentPane().add(buildArtistPanel(), BorderLayout.WEST);
        getContentPane().add(buildContentPanel(), BorderLayout.CENTER);
        
        
        getContentPane().add(buildSaveLoadPanel(), BorderLayout.SOUTH);
        buildSaveLoadPanel();
 
        refreshArtistList();
        refreshContentPane();
    }

	private JPanel buildSaveLoadPanel() {
		JPanel panel = new JPanel();
		panel.setBackground(new Color(23, 25, 32));
		panel.setBorder(new EmptyBorder(15, 0, 15, 0));
		
        JButton saveButton = new JButton("Save");
        saveButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        saveButton.setBorderPainted(false);
        saveButton.setForeground(new Color(52, 138, 191));
        saveButton.setBackground(new Color(20, 63, 89));
        saveButton.setFocusPainted(false);
        saveButton.setMargin(new Insets(10, 50, 10, 50));
        saveButton.addActionListener(e -> onSaveAs());
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 5));
        panel.add(saveButton);
        
        JButton loadButton = new JButton("Load");
        loadButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        loadButton.setBorderPainted(false);
        loadButton.setForeground(new Color(52, 138, 191));
        loadButton.setBackground(new Color(20, 63, 89));
        loadButton.setFocusPainted(false);
        loadButton.setMargin(new Insets(10, 50, 10, 50));
        loadButton.addActionListener(e -> onOpen());
        panel.add(loadButton);
        
        return panel;
	}
    
    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        bar.setForeground(new Color(255, 255, 255));
        bar.setBackground(new Color(20, 63, 89));
        bar.setBorder(new EmptyBorder(3, 3, 3, 3));
        JMenu fileMenu = new JMenu("File");
        fileMenu.setBackground(new Color(107, 123, 156));
        fileMenu.setForeground(new Color(255, 255, 255));
 
        JMenuItem openItem = new JMenuItem("Open...");
        openItem.setForeground(new Color(255, 255, 255));
        openItem.setBackground(new Color(23, 25, 32));
        openItem.addActionListener(e -> onOpen());
        fileMenu.add(openItem);
 
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.setBackground(new Color(23, 25, 32));
        saveItem.setForeground(new Color(255, 255, 255));
        saveItem.addActionListener(e -> onSave());
        fileMenu.add(saveItem);
 
        JMenuItem saveAsItem = new JMenuItem("Save As...");
        saveAsItem.setBackground(new Color(23, 25, 32));
        saveAsItem.setForeground(new Color(255, 255, 255));
        saveAsItem.addActionListener(e -> onSaveAs());
        fileMenu.add(saveAsItem);
        
        autoSaveOnOff = new JCheckBoxMenuItem("AutoSave: OFF");
        autoSaveOnOff.setBackground(new Color(23, 25, 32));
        autoSaveOnOff.setForeground(new Color(255, 255, 255));
        autoSaveOnOff.setSelected(false);
        autoSaveOnOff.addActionListener(e -> {
        	autoSaveEnabled = autoSaveOnOff.isSelected();
        	System.out.println("[DEBUG] toggled = " + autoSaveEnabled);
        	updateAutoSaveMenuText();
        });
        fileMenu.add(autoSaveOnOff);
 
        bar.add(fileMenu);
        return bar;
    }
    
    private void updateAutoSaveMenuText() {
    	autoSaveOnOff.setText(autoSaveEnabled ? "Auto Save: ON" : "Auto Save: OFF");
    }
 
    private JPanel buildSearchBar() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBackground(new Color(23, 25, 32));
        panel.setBorder(new EmptyBorder(6, 6, 6, 6));
        JLabel label = new JLabel("Search: ");
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(new Color(255, 255, 255));
        panel.add(label, BorderLayout.WEST);
 
        searchField = new JTextField();
        searchField.setForeground(new Color(255, 255, 255));
        searchField.setBorder(new LineBorder(new Color(192, 192, 192), 1, true));
        searchField.setBackground(new Color(23, 25, 32));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { onSearchChanged(); }
            @Override public void removeUpdate(DocumentEvent e)  { onSearchChanged(); }
            @Override public void changedUpdate(DocumentEvent e) { onSearchChanged(); }
        });
        panel.add(searchField, BorderLayout.CENTER);
        
        JButton clearTextButton = new JButton("");
        clearTextButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		searchField.setText("");
        	}
        });
        clearTextButton.setFocusPainted(false);
        clearTextButton.setContentAreaFilled(false);
        clearTextButton.setBorder(new EmptyBorder(6, 2, 6, 8));
        ImageIcon originalIcon = new ImageIcon(MainGUI.class.getResource("/mediaManager/Resources/png-clipart-computer-icons-backspace-arrow-symbol-cross-arrow-angle-text.png"));
        Image image = originalIcon.getImage();
        Image newImage = image.getScaledInstance(18, 13, java.awt.Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(newImage);
        clearTextButton.setIcon(scaledIcon);
        panel.add(clearTextButton, BorderLayout.EAST);
        return panel;
    }
    
    private JPanel buildArtistPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBackground(new Color(23, 25, 32));
        panel.setBorder(new CompoundBorder(new EmptyBorder(0, 10, 0, 0), new CompoundBorder(new LineBorder(new Color(192, 192, 192), 1, true), new EmptyBorder(5, 5, 5, 5))));
        panel.setPreferredSize(new Dimension(220, 0));
 
        JPanel header = new JPanel(new BorderLayout());
        header.setFont(new Font("Tahoma", Font.PLAIN, 16));
        header.setBackground(new Color(20, 63, 89));
        JLabel label = new JLabel("Artists");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(new Color(255, 255, 255));
        label.setHorizontalTextPosition(SwingConstants.CENTER);
        header.add(label, BorderLayout.CENTER);
 
        JButton addArtistButton = new JButton("+");
        addArtistButton.setForeground(new Color(255, 255, 255));
        addArtistButton.setContentAreaFilled(false);
        addArtistButton.setBorder(new CompoundBorder(new LineBorder(new Color(192, 192, 192), 1, true), new EmptyBorder(6, 8, 6, 8)));
        addArtistButton.setMargin(new Insets(6, 8, 6, 8));
        addArtistButton.setBackground(new Color(255, 255, 255));
        addArtistButton.setFocusPainted(false);
        addArtistButton.setToolTipText("Add artist");
        addArtistButton.addActionListener(e -> onAddArtist());
        header.add(addArtistButton, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);
 
        artistList = new JList<>(artistModel);
        artistList.setFixedCellHeight(25);
        
        JScrollPane artistScroll = new JScrollPane(artistList);
        artistScroll.setBorder(BorderFactory.createEmptyBorder());
        artistScroll.setViewportBorder(BorderFactory.createEmptyBorder());
        artistScroll.getViewport().setBackground(artistList.getBackground());
        artistList.setBorder(new EmptyBorder(10, 10, 0, 0));
        artistList.setForeground(new Color(255, 255, 255));
        artistList.setBackground(new Color(23, 25, 32));
        artistList.setFont(new Font("Lucida Sans Typewriter", Font.BOLD, 13));
        artistList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        artistList.addListSelectionListener(this::onArtistSelectionChanged);
        panel.add(artistScroll, BorderLayout.CENTER);
        
        JButton removeArtistButton = new JButton("Remove Artist");
        removeArtistButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        removeArtistButton.setBorderPainted(false);
        removeArtistButton.setBackground(new Color(89, 45, 45));
        removeArtistButton.setForeground(new Color(242, 114, 114));
        removeArtistButton.addActionListener(e -> onRemoveArtist());
        panel.add(removeArtistButton, BorderLayout.SOUTH);
 
        return panel;
    }
 
    private JPanel buildContentPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBackground(new Color(23, 25, 32));
        panel.setBorder(new CompoundBorder(new EmptyBorder(0, 0, 0, 10), new CompoundBorder(new LineBorder(new Color(192, 192, 192), 1, true), new EmptyBorder(5, 5, 5, 5))));
 
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(20, 63, 89));
        contentHeader = new JLabel("Library");
        contentHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        contentHeader.setHorizontalAlignment(SwingConstants.CENTER);
        contentHeader.setForeground(new Color(255, 255, 255));
        header.add(contentHeader, BorderLayout.CENTER);
 
        FlowLayout fl_headerButtons = new FlowLayout(FlowLayout.RIGHT, 5, 0);
        JPanel headerButtons = new JPanel(fl_headerButtons);
        headerButtons.setBackground(new Color(20, 63, 89));
        backButton = new JButton("Back");
        backButton.setForeground(new Color(255, 255, 255));
        backButton.setContentAreaFilled(false);
        backButton.setBorder(new CompoundBorder(new LineBorder(new Color(192, 192, 192), 1, true), new EmptyBorder(6, 8, 6, 8)));
        backButton.setMargin(new Insets(6, 8, 6, 8));
        backButton.setBackground(new Color(255, 255, 255));
        backButton.setEnabled(false);
        backButton.addActionListener(e -> onBack());
        headerButtons.add(backButton);
 
        addContentButton = new JButton("+");
        addContentButton.setForeground(new Color(255, 255, 255));
        addContentButton.setBorder(new CompoundBorder(new LineBorder(new Color(192, 192, 192), 1, true), new EmptyBorder(6, 8, 6, 8)));
        addContentButton.setContentAreaFilled(false);
        addContentButton.setMargin(new Insets(6, 8, 6, 8));
        addContentButton.setBackground(new Color(255, 255, 255));
        addContentButton.setEnabled(false);
        addContentButton.addActionListener(e -> onAddContent());
        headerButtons.add(addContentButton);
 
        header.add(headerButtons, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);
 
        contentList = new JList<>(contentModel);
        
        JScrollPane contentScroll = new JScrollPane(contentList);
        contentScroll.setBorder(BorderFactory.createEmptyBorder());
        contentScroll.setViewportBorder(BorderFactory.createEmptyBorder());
        contentScroll.getViewport().setBackground(contentList.getBackground());
        
        contentList.setFixedCellHeight(25);
        contentList.setBorder(new EmptyBorder(10, 10, 0, 0));
        contentList.setBackground(new Color(23, 25, 32));
        contentList.setForeground(new Color(255, 255, 255));
        contentList.setFont(new Font("Lucida Sans Typewriter", Font.BOLD, 13));
        contentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contentList.addListSelectionListener(this::onContentSelectionChanged);
        panel.add(contentScroll, BorderLayout.CENTER);
        
        removeContentButton = new JButton("Remove Song");
        removeContentButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        removeContentButton.setBorderPainted(false);
        removeContentButton.setBackground(new Color(89, 45, 45));
        removeContentButton.setForeground(new Color(242, 114, 114));
        removeContentButton.addActionListener(e -> {
            if (selectedAlbum != null && selectedSong == null) {
                onRemoveAlbum();
            } else if (selectedSong != null) {
                onRemoveSong();
            }
        });
        panel.add(removeContentButton, BorderLayout.SOUTH);
 
        return panel;
    }
    
    private void removeSongFromLibrary(Song song) {
        for (Artist artist : library.getArtists()) {
            for (Album album : artist.getAlbums()) {
                if (album.getSongs().remove(song)) {
                    return; // stop once removed
                }
            }
        }
    }
    
    private void onRemoveArtist() {
        Artist selected = artistList.getSelectedValue();
        if (selected == null) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete artist \"" + selected.getName() + "\"?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        library.getArtists().remove(selected);

        selectedArtist = null;
        selectedAlbum = null;

        refreshArtistList();
        refreshContentPane();
    }
    
    private void onRemoveAlbum() {
        if (selectedArtist == null || selectedAlbum == null) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete album \"" + selectedAlbum.getName() + "\"?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        selectedArtist.getAlbums().remove(selectedAlbum);

        selectedAlbum = null;

        refreshContentPane();
    }
    
    private void onRemoveSong() {
        Object selected = contentList.getSelectedValue();
        if (!(selected instanceof Song song)) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete song \"" + song.getTitle() + "\"?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        if (selectedAlbum != null) {
            selectedAlbum.getSongs().remove(song);
        } else {
            removeSongFromLibrary(song);
        }

        refreshContentPane();
    }
    
    private void onArtistSelectionChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;

        selectedArtist = artistList.getSelectedValue();
        selectedAlbum = null;

        if (selectedArtist != null && searchActive) {
            internalChange = true;
            searchField.setText("");
            searchActive = false;
            internalChange = false;
        }

        refreshContentPane();
    }
    
    private void onContentSelectionChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;

        Object selected = contentList.getSelectedValue();

        if (selected instanceof Album album) {
            selectedAlbum = album;
            selectedSong = null;
            refreshContentPane();
            return;
        }

        if (selected instanceof Song song) {
            selectedSong = song;
        } else {
            selectedSong = null;
        }

        if (selectedAlbum != null) {
            if (selectedSong != null) {
                removeContentButton.setText("Remove Song");
            } else {
                removeContentButton.setText("Remove Album");
            }
        } else if (selectedArtist != null) {
            removeContentButton.setText("Remove Album");
        } else {
            removeContentButton.setText("Remove Song");
        }
    }
 
    private void onAddArtist() {
        System.out.println("[GUI] Add Artist button clicked");
        String name = JOptionPane.showInputDialog(this, "Artist name:",
                "Add Artist", JOptionPane.PLAIN_MESSAGE);
        if (name == null) {
            System.out.println("[GUI] Cancelled add operation or empty input");
            return;
        }
        if (name.isBlank()) {
        	JOptionPane.showMessageDialog(this,
                    "Artist name is required.",
                    "Missing field", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Artist artist = new Artist(name.trim());
        if (!library.addArtist(artist)) {
            showDuplicateWarning("An artist with that name already exists.");
            return;
        }
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
    	
    	if (searchActive) {
    	    internalChange = true;
    	    searchField.setText("");
    	    searchActive = false;
    	    internalChange = false;
    	}
    	 
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
    	if (internalChange) return;
    	
        String query = searchField.getText().trim();
        if (!query.isEmpty()) {
            searchActive = true;

            selectedArtist = null;
            selectedAlbum = null;
            artistList.clearSelection();
        }
        else {
        	searchActive = false;
        }
        
        refreshContentPane();
    }
    
    private void showAddAlbumDialog(Artist parent) {
        String name = JOptionPane.showInputDialog(this, "Album name:",
                "Add Album", JOptionPane.PLAIN_MESSAGE);
        
        if (name == null) {
            System.out.println("[GUI] Add album cancelled");
            return;
        }
        if (name.isBlank()) {
        	JOptionPane.showMessageDialog(this,
                    "Album name is required.",
                    "Missing field", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Album album = new Album(name.trim());
        if (!parent.addAlbum(album)) {
            showDuplicateWarning("This artist already has an album with that name.");
            return;
        }
  
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
            System.out.println("[GUI] Add song operation cancelled");
            return;
        }
 
        String title = titleField.getText().trim();
        String durationStr = durationField.getText().trim();
        if (title.isBlank() || durationStr.isBlank()) {
            JOptionPane.showMessageDialog(this,
                    "Title and duration are required.",
                    "Missing field", JOptionPane.WARNING_MESSAGE);
            return;
        }
 
        Double duration = parseDuration(durationStr);
        if (duration == null || duration <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Duration must be in M:SS format (such as 3:45) or a positive decimal.",
                    "Invalid duration", JOptionPane.WARNING_MESSAGE);
            return;
        }
 
        Song song = new Song(title, duration);
        if (!parent.addSong(song)) {
            showDuplicateWarning("This album already has a song with that title.");
            return;
        }
        System.out.println("[GUI] Added song '" + song.getTitle()
                + "' to album '" + parent.getName() + "'");
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
 
    private void showDuplicateWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Duplicate",
                JOptionPane.WARNING_MESSAGE);
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
            contentHeader.setText("Search Results");
            for (Song s : library.searchSongs(searchField.getText())) {
                contentModel.addElement(s);
            }
            addContentButton.setEnabled(false);
            addContentButton.setToolTipText(null);
            backButton.setEnabled(false);
            
            return;
        }
 
        if (selectedAlbum != null) {
            contentHeader.setText(selectedAlbum.getName() + " - Songs");
            for (Song s : selectedAlbum.getSongs()) {
                contentModel.addElement(s);
            }
            addContentButton.setEnabled(true);
            addContentButton.setToolTipText("Add song to this album");
            backButton.setEnabled(true);
        } else if (selectedArtist != null) {
            contentHeader.setText(selectedArtist.getName() + " - Albums");
            for (Album a : selectedArtist.getAlbums()) {
                contentModel.addElement(a);
            }
            addContentButton.setEnabled(true);
            addContentButton.setToolTipText("Add album to this artist");
            backButton.setEnabled(true);
        } else {
            contentHeader.setText("Library");
            for (Song s : getAllSongs()) {
                contentModel.addElement(s);
            }
            addContentButton.setEnabled(false);
            addContentButton.setToolTipText(null);
            backButton.setEnabled(false);
        }
        
        if (selectedAlbum != null) {
            if (contentList.isSelectionEmpty()) {
                removeContentButton.setText("Remove Album");
            } else {
                removeContentButton.setText("Remove Song");
            }
        } else if (selectedArtist != null) {
            removeContentButton.setText("Remove Album");
        } else {
            removeContentButton.setText("Remove Song");
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
        try {
        	library = FileHandler.load(file);
        	currentFile = file;
        	selectedArtist = null;
        	selectedAlbum = null;
        	searchField.setText("");
        	refreshArtistList();
        	refreshContentPane();
        	System.out.println("[GUI] File loaded " + file.getName());
        } catch (IOException e) {
        	System.out.println("[GUI] Could not open file - error: " + e.getMessage());
        	JOptionPane.showMessageDialog(this, "Failed to open file: " + e.getMessage(),
                    "Open error", JOptionPane.ERROR_MESSAGE);
        }
        
    }
 
    private void onSave() {
        System.out.println("[GUI] File > Save clicked");
        if (currentFile == null) {
            onSaveAs();
            return;
        }
        try {
            FileHandler.save(library, currentFile);
            System.out.println("[GUI] Saved file " + currentFile.getName());
        } catch (IOException e) {
            System.out.println("[GUI] Could not save file - error: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Failed to save file: " + e.getMessage(),
                    "Save error", JOptionPane.ERROR_MESSAGE);
        }
    }
 
    private void onSaveAs() {
        System.out.println("[GUI] File > Save As clicked");
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            System.out.println("[GUI] File save cancelled");
            return;
        }
        File file = chooser.getSelectedFile();
        try {
        	FileHandler.save(library, file);
            if (!file.getName().endsWith(".txt")) {
                file = new File(file.getAbsolutePath() + ".txt");
            }
            currentFile = file;
            System.out.println("[GUI] Saved file " + currentFile.getName());
        } catch (IOException e) {
            System.out.println("[GUI] Could not save file - error: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Failed to save file: " + e.getMessage(),
                    "Save error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void autoSave() {
    	
        System.out.println("[DEBUG] autoSave() called");
        System.out.println("[DEBUG] autoSaveEnabled = " + autoSaveEnabled);
    	
    	if (!autoSaveEnabled) {
    		System.out.println("[GUI} Auto-save is off. - skipping save.");
    		return;
    	}
    	
        if (currentFile == null) {
            System.out.println("File saved in-memory (no file was set)");
            return;
        }
        try {
        	FileHandler.save(library,  currentFile);
        	System.out.println("[GUI] Auto-saved to " + currentFile.getName());
        } catch (IOException e) {
        	System.out.println("[GUI] Auto-save failed: " + e.getMessage());
        	JOptionPane.showMessageDialog(this,
                    "Auto-save failed: " + e.getMessage(),
                    "Save error", JOptionPane.ERROR_MESSAGE);

        }
    }
 
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainGUI().setVisible(true));
    }
}
