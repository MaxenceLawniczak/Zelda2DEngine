package editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;

import main.EntityFactory;
import main.EntityFactory.Kind;
import main.MapData;
import main.MapData.Placement;
import main.UtilityTool;

/**
 * Éditeur de map du jeu. À lancer avec "Run As > Java Application" sur cette classe
 * (le dossier de travail doit être la racine du projet, comme pour le jeu).
 *
 * Il modifie directement les fichiers du dossier res/ :
 *   res/maps/mapXX.txt       les tiles
 *   res/maps/mapXX_data.txt  objets, coffres, monstres, PNJ, tiles interactives, événements, départ, nom
 *   res/tiles/collisions.txt les tiles qui bloquent le joueur
 *   res/tiles/wallsets.txt   les jeux de murs de l'outil Salle
 * En jeu, active le mode debug (T) puis appuie sur R pour recharger la map sans relancer.
 */
public class MapEditor extends JFrame {

	private final EditorModel model;
	private final TilePalette tilePalette;
	private final ElementPalette elementPalette;
	private final WallSetPanel wallSetPanel;
	private final TerrainPanel terrainPanel;
	private final MapCanvas canvas;
	private final JScrollPane canvasScroll;
	private final NavigatorPanel navigator;
	private final JTabbedPane tabs = new JTabbedPane();
	private final JComboBox<String> mapBox = new JComboBox<>();
	private final JTextField mapName = new JTextField(16);
	private final JSpinner brushSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 9, 1));
	private final JLabel zoomLabel = new JLabel("100 %");
	private final JLabel status = new JLabel(" ");
	private final JLabel message = new JLabel(" ");
	private final JPanel recentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
	private final List<Integer> recentTiles = new ArrayList<>();
	private final JToggleButton[] toolButtons = new JToggleButton[MapCanvas.Tool.values().length];
	private final Timer messageTimer = new Timer(4000, e -> message.setText(" "));
	private EditorModel.Clip clipboard = null;
	private boolean updatingMapBox = false;

	public static void main(String[] args) {
		EditorTheme.apply();
		SwingUtilities.invokeLater(() -> {
			if (!findResFolder()) return;
			new MapEditor().setVisible(true);
		});
	}

	/** Vérifie que le dossier res/ est trouvable, sinon le demande à l'utilisateur. */
	private static boolean findResFolder() {
		if (new File(UtilityTool.RES_DIR, "maps").isDirectory()) return true;
		JOptionPane.showMessageDialog(null, "Dossier res/ introuvable depuis " + new File(".").getAbsolutePath()
				+ "\nSélectionne le dossier res du projet.", "Éditeur de map", JOptionPane.INFORMATION_MESSAGE);
		JFileChooser chooser = new JFileChooser(new File("."));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return false;
		UtilityTool.RES_DIR = chooser.getSelectedFile();
		return true;
	}

	public MapEditor() {
		super("Éditeur de map");
		model = new EditorModel();
		tilePalette = new TilePalette(model, this::paletteChanged);
		elementPalette = new ElementPalette(() -> setTool(MapCanvas.Tool.ELEMENT));
		wallSetPanel = new WallSetPanel(tilePalette::getSelected, () -> setTool(MapCanvas.Tool.ROOM));
		terrainPanel = new TerrainPanel(() -> setTool(MapCanvas.Tool.TERRAIN));
		canvas = new MapCanvas(model, this);
		canvasScroll = new JScrollPane(canvas);
		navigator = new NavigatorPanel(model, canvas, canvasScroll.getViewport());

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (confirmDiscard()) System.exit(0);
			}
		});
		messageTimer.setRepeats(false);

		buildMenu();
		buildLayout();
		bindShortcuts();

		// Ouvre la première map existante
		List<Integer> maps = EditorModel.listMaps();
		model.load(maps.isEmpty() ? 1 : maps.get(0));
		refreshMapBox();
		setTool(MapCanvas.Tool.PENCIL);
		modelChanged();

		setSize(1440, 900);
		setLocationRelativeTo(null);
		showMessage("Bienvenue ! F1 pour la liste des raccourcis.");
	}

	// ===================== Interface =====================

	private void buildLayout() {
		// --- Barre du haut : map, nom, enregistrer, tester ---
		JToolBar fileBar = new JToolBar();
		fileBar.setFloatable(false);
		fileBar.add(new JLabel("  Map "));
		mapBox.setMaximumSize(new Dimension(200, 30));
		mapBox.addActionListener(e -> {
			if (updatingMapBox || mapBox.getSelectedIndex() < 0) return;
			List<Integer> maps = EditorModel.listMaps();
			if (mapBox.getSelectedIndex() < maps.size() && maps.get(mapBox.getSelectedIndex()) != model.getMapNumber()) {
				openMap(maps.get(mapBox.getSelectedIndex()));
			}
		});
		fileBar.add(mapBox);
		fileBar.add(Box.createHorizontalStrut(10));
		fileBar.add(new JLabel("Nom du lieu "));
		mapName.setMaximumSize(new Dimension(220, 30));
		mapName.setToolTipText("Affiché en jeu quand le joueur arrive sur cette map");
		mapName.addActionListener(e -> applyMapName());
		mapName.addFocusListener(new java.awt.event.FocusAdapter() {
			@Override public void focusLost(java.awt.event.FocusEvent e) { applyMapName(); }
		});
		fileBar.add(mapName);
		fileBar.add(Box.createHorizontalStrut(10));
		fileBar.add(flatButton("Nouvelle", "Créer une nouvelle map (Ctrl+N)", this::newMap));
		fileBar.add(flatButton("Enregistrer", "Ctrl+S", this::save));
		JButton test = flatButton("▶  Tester en jeu", "Enregistre et lance le jeu sur cette map (F5)", () -> testMap(-1, -1));
		test.setForeground(new Color(120, 230, 140));
		test.setFont(test.getFont().deriveFont(Font.BOLD));
		fileBar.add(test);
		fileBar.add(Box.createHorizontalGlue());
		fileBar.add(flatButton("Annuler", "Ctrl+Z", this::undo));
		fileBar.add(flatButton("Rétablir", "Ctrl+Y", this::redo));
		fileBar.addSeparator();

		// --- Barre des outils ---
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		ButtonGroup group = new ButtonGroup();
		for (MapCanvas.Tool tool : MapCanvas.Tool.values()) {
			JToggleButton b = new JToggleButton(tool.label, EditorTheme.icon(tool));
			b.setToolTipText("<html><b>" + tool.label + " (" + tool.key + ")</b><br>" + tool.help + "</html>");
			b.setFocusable(false);
			b.addActionListener(e -> setTool(tool));
			group.add(b);
			toolBar.add(b);
			toolButtons[tool.ordinal()] = b;
			if (tool == MapCanvas.Tool.TERRAIN || tool == MapCanvas.Tool.ERASER || tool == MapCanvas.Tool.SELECT) toolBar.addSeparator();
		}
		toolBar.addSeparator();
		toolBar.add(new JLabel(" Taille "));
		brushSpinner.setMaximumSize(new Dimension(56, 28));
		brushSpinner.setToolTipText("Taille du pinceau, de la gomme, de la ligne et du contour ([ et ] ou Alt+molette)");
		brushSpinner.addChangeListener(e -> canvas.setBrushSize((Integer) brushSpinner.getValue()));
		toolBar.add(brushSpinner);
		JToggleButton variantsButton = new JToggleButton("Variantes", true);
		variantsButton.setToolTipText("<html>Pose des variantes au hasard : herbe 0 -> parfois touffe 1, chemin 79 -> parfois 80...<br>"
				+ "Réglages dans res/tiles/variants.txt</html>");
		variantsButton.setFocusable(false);
		variantsButton.addActionListener(e -> {
			model.randomVariants = variantsButton.isSelected();
			showMessage("Variantes aléatoires : " + (model.randomVariants ? "activées" : "désactivées"));
		});
		toolBar.add(variantsButton);

		JToolBar viewBar = fileBar; // Les boutons d'affichage vont sur la ligne du haut
		viewBar.add(layerToggle("Grille", "G", true, canvas::toggleGrid));
		viewBar.add(layerToggle("Collisions", "C", false, canvas::toggleCollisions));
		viewBar.add(layerToggle("Éléments", null, true, canvas::toggleElements));
		viewBar.add(layerToggle("Événements", null, true, canvas::toggleEvents));
		viewBar.addSeparator();
		viewBar.add(flatButton("−", "Dézoomer (Ctrl+molette)", () -> canvas.zoom(-1, null)));
		zoomLabel.setPreferredSize(new Dimension(50, 20));
		zoomLabel.setHorizontalAlignment(JLabel.CENTER);
		viewBar.add(zoomLabel);
		viewBar.add(flatButton("+", "Zoomer (Ctrl+molette)", () -> canvas.zoom(1, null)));

		JPanel toolRow = new JPanel(new BorderLayout());
		toolRow.add(toolBar, BorderLayout.CENTER);
		JPanel north = new JPanel(new BorderLayout());
		north.add(fileBar, BorderLayout.NORTH);
		north.add(toolRow, BorderLayout.SOUTH);

		// --- Onglet Tiles : récentes + recherche + palette ---
		JPanel tilesTab = new JPanel(new BorderLayout(0, 4));
		tilesTab.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		JPanel tilesTop = new JPanel(new BorderLayout(4, 4));
		JPanel search = new JPanel(new BorderLayout(4, 0));
		search.add(new JLabel("Tile n° "), BorderLayout.WEST);
		JTextField searchField = new JTextField();
		searchField.setToolTipText("Tape un numéro de tile puis Entrée");
		searchField.addActionListener(e -> {
			try {
				int n = Integer.parseInt(searchField.getText().trim());
				if (n >= 0 && n < model.tileCount) {
					tilePalette.setSelected(n);
					paletteChanged();
				}
			} catch (NumberFormatException ignored) {
				// Pas un nombre : on ignore
			}
		});
		search.add(searchField, BorderLayout.CENTER);
		tilesTop.add(search, BorderLayout.NORTH);
		recentPanel.setBorder(BorderFactory.createTitledBorder("Récentes"));
		recentPanel.setPreferredSize(new Dimension(240, 64));
		tilesTop.add(recentPanel, BorderLayout.CENTER);
		tilesTab.add(tilesTop, BorderLayout.NORTH);
		JScrollPane tileScroll = new JScrollPane(tilePalette);
		tileScroll.getVerticalScrollBar().setUnitIncrement(36);
		tilesTab.add(tileScroll, BorderLayout.CENTER);
		JLabel hint = new JLabel("<html>Clic : choisir · Clic droit : collision<br>Croix rouge = bloque le joueur</html>");
		hint.setForeground(new Color(170, 170, 180));
		tilesTab.add(hint, BorderLayout.SOUTH);

		tabs.addTab("Tiles", tilesTab);
		tabs.addTab("Salles", wallSetPanel);
		tabs.addTab("Terrains", terrainPanel);
		tabs.addTab("Éléments", elementPalette);
		tabs.addChangeListener(e -> {
			if (tabs.getSelectedIndex() == 1 && canvas.getTool() != MapCanvas.Tool.TRACE) setTool(MapCanvas.Tool.ROOM);
			else if (tabs.getSelectedIndex() == 2) setTool(MapCanvas.Tool.TERRAIN);
			else if (tabs.getSelectedIndex() == 3) setTool(MapCanvas.Tool.ELEMENT);
			else if (tabs.getSelectedIndex() == 0 && (canvas.getTool() == MapCanvas.Tool.ROOM || canvas.getTool() == MapCanvas.Tool.TRACE
					|| canvas.getTool() == MapCanvas.Tool.ELEMENT || canvas.getTool() == MapCanvas.Tool.TERRAIN)) setTool(MapCanvas.Tool.PENCIL);
		});

		JPanel navPanel = new JPanel(new BorderLayout());
		navPanel.setBorder(BorderFactory.createTitledBorder("Navigateur"));
		navPanel.add(navigator, BorderLayout.CENTER);

		JSplitPane sidebar = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabs, navPanel);
		sidebar.setResizeWeight(1);
		sidebar.setDividerLocation(560);
		sidebar.setPreferredSize(new Dimension(290, 600));

		canvasScroll.getViewport().setBackground(EditorTheme.BACKGROUND);
		canvasScroll.getVerticalScrollBar().setUnitIncrement(32);
		canvasScroll.getHorizontalScrollBar().setUnitIncrement(32);
		canvasScroll.setBorder(BorderFactory.createEmptyBorder());

		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, canvasScroll);
		split.setDividerLocation(290);

		JPanel statusBar = new JPanel(new BorderLayout());
		statusBar.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
		statusBar.add(status, BorderLayout.CENTER);
		message.setForeground(EditorTheme.ACCENT);
		statusBar.add(message, BorderLayout.EAST);

		getContentPane().add(north, BorderLayout.NORTH);
		getContentPane().add(split, BorderLayout.CENTER);
		getContentPane().add(statusBar, BorderLayout.SOUTH);
		refreshRecent();
	}

	private JButton flatButton(String text, String tooltip, Runnable action) {
		JButton b = new JButton(text);
		b.setToolTipText(tooltip);
		b.setFocusable(false);
		b.addActionListener(e -> action.run());
		return b;
	}

	private JToggleButton layerToggle(String text, String key, boolean selected, Runnable action) {
		JToggleButton b = new JToggleButton(text, selected);
		b.setToolTipText("Afficher / masquer : " + text + (key != null ? " (" + key + ")" : ""));
		b.setFocusable(false);
		b.addActionListener(e -> action.run());
		if (key != null) {
			// Le raccourci clavier doit aussi mettre le bouton à jour
			bind(canvas, key, () -> {
				b.setSelected(!b.isSelected());
				action.run();
			});
		}
		return b;
	}

	private void buildMenu() {
		JMenuBar bar = new JMenuBar();

		JMenu file = new JMenu("Fichier");
		file.add(item("Nouvelle map...", KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK), this::newMap));
		file.add(item("Ouvrir une map...", KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK), this::chooseMap));
		file.add(item("Enregistrer", KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), this::save));
		file.add(item("Recharger depuis le disque", null, () -> { if (confirmDiscard()) openMap(model.getMapNumber()); }));
		file.addSeparator();
		file.add(item("Quitter", null, () -> { if (confirmDiscard()) System.exit(0); }));
		bar.add(file);

		JMenu edit = new JMenu("Édition");
		edit.add(item("Annuler", KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), this::undo));
		edit.add(item("Rétablir", KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), this::redo));
		edit.addSeparator();
		edit.add(item("Copier", KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK), () -> canvas.copySelection(false)));
		edit.add(item("Couper", KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK), () -> canvas.copySelection(true)));
		edit.add(item("Coller", KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK), () -> {
			if (clipboard == null) showMessage("Rien à coller : sélectionne une zone (V) puis Ctrl+C");
			else canvas.startPaste(clipboard);
		}));
		edit.add(item("Tout sélectionner", KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK), () -> {
			setTool(MapCanvas.Tool.SELECT);
			canvas.selectAll();
		}));
		edit.addSeparator();
		edit.add(item("Remplacer une tile partout...", null, this::replaceDialog));
		edit.add(item("Remplir toute la map avec la tile choisie", null, () -> {
			if (JOptionPane.showConfirmDialog(this, "Remplacer TOUTES les tiles par la tile " + tilePalette.getSelected() + " ?",
					"Remplir", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
			model.pushUndo();
			model.fillRect(0, 0, EditorModel.COLS - 1, EditorModel.ROWS - 1, tilePalette.getSelected());
			modelChanged();
		}));
		edit.add(item("Entourer la map d'une bordure (salle choisie)", null, () -> {
			WallSet ws = wallSetPanel.getSelected();
			if (ws == null) return;
			model.pushUndo();
			ws.apply(model, 0, 0, EditorModel.COLS - 1, EditorModel.ROWS - 1, false);
			modelChanged();
		}));
		edit.add(item("Supprimer tous les éléments de la map", null, () -> {
			if (JOptionPane.showConfirmDialog(this, "Supprimer tous les objets, monstres, PNJ, tiles interactives et événements ?",
					"Tout supprimer", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
			model.pushUndo();
			model.getData().placements.clear();
			model.markDirty();
			modelChanged();
		}));
		bar.add(edit);

		JMenu view = new JMenu("Affichage");
		view.add(item("Zoom +", KeyStroke.getKeyStroke(KeyEvent.VK_ADD, InputEvent.CTRL_DOWN_MASK), () -> canvas.zoom(1, null)));
		view.add(item("Zoom -", KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, InputEvent.CTRL_DOWN_MASK), () -> canvas.zoom(-1, null)));
		view.add(item("Zoom 100 %", KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0, InputEvent.CTRL_DOWN_MASK), canvas::resetZoom));
		bar.add(view);

		JMenu test = new JMenu("Tester");
		test.add(item("Tester la map (depuis le point de départ)", KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), () -> testMap(-1, -1)));
		bar.add(test);

		JMenu help = new JMenu("Aide");
		help.add(item("Raccourcis...", KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), this::showHelp));
		bar.add(help);

		setJMenuBar(bar);
	}

	private JMenuItem item(String label, KeyStroke key, Runnable action) {
		JMenuItem item = new JMenuItem(label);
		if (key != null) item.setAccelerator(key);
		item.addActionListener(e -> action.run());
		return item;
	}

	/** Raccourcis à une lettre : actifs quand la souris est sur la map (pas pendant la saisie d'un champ). */
	private void bindShortcuts() {
		for (MapCanvas.Tool tool : MapCanvas.Tool.values()) {
			bind(canvas, tool.key, () -> setTool(tool));
		}
		bind(canvas, "ADD", () -> canvas.zoom(1, null));
		bind(canvas, "SUBTRACT", () -> canvas.zoom(-1, null));
		bind(canvas, "DELETE", canvas::deleteSelection);
		bind(canvas, "BACK_SPACE", canvas::deleteSelection);
		bind(canvas, "ESCAPE", canvas::cancel);
		bind(canvas, "ENTER", () -> canvas.finishTrace(false));
		bind(canvas, "OPEN_BRACKET", () -> setBrushSize(canvas.getBrushSize() - 1));
		bind(canvas, "CLOSE_BRACKET", () -> setBrushSize(canvas.getBrushSize() + 1));
		for (int i = 1; i <= 9; i++) {
			int size = i;
			bind(canvas, String.valueOf(i), () -> setBrushSize(size));
		}
	}

	private void bind(JComponent c, String key, Runnable action) {
		c.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(key), key);
		c.getActionMap().put(key, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				action.run();
			}
		});
	}

	// ===================== Actions =====================

	public void setTool(MapCanvas.Tool tool) {
		if (canvas == null) return; // Appelé pendant la construction des palettes
		canvas.setTool(tool);
		if (toolButtons[tool.ordinal()] != null) toolButtons[tool.ordinal()].setSelected(true);
		// Affiche l'onglet qui correspond à l'outil
		if ((tool == MapCanvas.Tool.ROOM || tool == MapCanvas.Tool.TRACE) && tabs.getSelectedIndex() != 1) tabs.setSelectedIndex(1);
		else if (tool == MapCanvas.Tool.TERRAIN && tabs.getSelectedIndex() != 2) tabs.setSelectedIndex(2);
		else if (tool == MapCanvas.Tool.ELEMENT && tabs.getSelectedIndex() != 3) tabs.setSelectedIndex(3);
		else if ((tool == MapCanvas.Tool.PENCIL || tool == MapCanvas.Tool.FILL || tool == MapCanvas.Tool.RECT || tool == MapCanvas.Tool.LINE
				|| tool == MapCanvas.Tool.OUTLINE) && tabs.getSelectedIndex() != 0) tabs.setSelectedIndex(0);
	}

	public void setBrushSize(int size) {
		size = Math.max(1, Math.min(9, size));
		brushSpinner.setValue(size);
		canvas.setBrushSize(size);
		updateStatus();
	}

	public void setClipboard(EditorModel.Clip clip) {
		clipboard = clip;
		showMessage("Copié : " + clip.width + " x " + clip.height + " cases (Ctrl+V pour coller)");
	}

	public void showMessage(String text) {
		message.setText(text + "  ");
		messageTimer.restart();
	}

	private void paletteChanged() {
		tileUsed(tilePalette.getSelected());
		canvas.repaint();
		modelChanged();
	}

	/** Ajoute une tile à la liste des tiles récentes. */
	public void tileUsed(int tile) {
		if (!recentTiles.isEmpty() && recentTiles.get(0) == tile) return;
		recentTiles.remove(Integer.valueOf(tile));
		recentTiles.add(0, tile);
		while (recentTiles.size() > 12) recentTiles.remove(recentTiles.size() - 1);
		refreshRecent();
	}

	private void refreshRecent() {
		recentPanel.removeAll();
		for (int tile : recentTiles) {
			JButton b = new JButton(new ImageIcon(EditorImages.tile(tile).getScaledInstance(24, 24, java.awt.Image.SCALE_FAST)));
			b.setMargin(new java.awt.Insets(0, 0, 0, 0));
			b.setPreferredSize(new Dimension(28, 28));
			b.setFocusable(false);
			b.setToolTipText("Tile " + tile);
			b.addActionListener(e -> {
				tilePalette.setSelected(tile);
				if (canvas.getTool() != MapCanvas.Tool.FILL && canvas.getTool() != MapCanvas.Tool.RECT
						&& canvas.getTool() != MapCanvas.Tool.LINE && canvas.getTool() != MapCanvas.Tool.OUTLINE) setTool(MapCanvas.Tool.PENCIL);
				canvas.repaint();
			});
			recentPanel.add(b);
		}
		recentPanel.revalidate();
		recentPanel.repaint();
	}

	private void applyMapName() {
		model.setMapName(mapName.getText().trim());
		modelChanged();
	}

	private void replaceDialog() {
		String answer = JOptionPane.showInputDialog(this, "Remplacer quelle tile par la tile choisie (" + tilePalette.getSelected() + ") ?\nNuméro de la tile à remplacer :");
		if (answer == null) return;
		try {
			int from = Integer.parseInt(answer.trim());
			model.pushUndo();
			int count = model.replaceAll(from, tilePalette.getSelected());
			showMessage(count + " case(s) remplacée(s)");
			modelChanged();
		} catch (NumberFormatException e) {
			showMessage("Numéro invalide");
		}
	}

	private void undo() {
		if (model.undo()) modelChanged();
		else showMessage("Rien à annuler");
	}

	private void redo() {
		if (model.redo()) modelChanged();
		else showMessage("Rien à rétablir");
	}

	private boolean confirmDiscard() {
		if (!model.isDirty()) return true;
		int choice = JOptionPane.showConfirmDialog(this, "La map " + model.getMapNumber() + " a été modifiée. Enregistrer ?",
				"Modifications non enregistrées", JOptionPane.YES_NO_CANCEL_OPTION);
		if (choice == JOptionPane.YES_OPTION) return save();
		return choice == JOptionPane.NO_OPTION;
	}

	private void openMap(int number) {
		if (number != model.getMapNumber() && !confirmDiscard()) {
			refreshMapBox();
			return;
		}
		model.load(number);
		canvas.cancel();
		refreshMapBox();
		modelChanged();
	}

	private void chooseMap() {
		List<Integer> maps = EditorModel.listMaps();
		if (maps.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Aucune map dans " + EditorModel.mapsDir());
			return;
		}
		Integer choice = (Integer) JOptionPane.showInputDialog(this, "Map à ouvrir :", "Ouvrir",
				JOptionPane.PLAIN_MESSAGE, null, maps.toArray(), model.getMapNumber());
		if (choice != null) openMap(choice);
	}

	private void newMap() {
		if (!confirmDiscard()) return;
		List<Integer> maps = EditorModel.listMaps();
		int next = maps.isEmpty() ? 1 : maps.get(maps.size() - 1) + 1;
		JPanel panel = new JPanel(new java.awt.GridLayout(0, 2, 6, 6));
		JSpinner number = new JSpinner(new SpinnerNumberModel(next, 1, 99, 1));
		JTextField name = new JTextField("Nouvelle zone");
		JComboBox<WallSet> border = new JComboBox<>();
		border.addItem(null);
		for (WallSet w : wallSetPanel.all()) border.addItem(w);
		panel.add(new JLabel("Numéro (le jeu gère 1 à 10) :"));
		panel.add(number);
		panel.add(new JLabel("Nom du lieu :"));
		panel.add(name);
		panel.add(new JLabel("Remplie avec la tile :"));
		panel.add(new JLabel(String.valueOf(tilePalette.getSelected()), new ImageIcon(EditorImages.tile(tilePalette.getSelected())), JLabel.LEFT));
		panel.add(new JLabel("Bordure autour de la map :"));
		panel.add(border);
		if (JOptionPane.showConfirmDialog(this, panel, "Nouvelle map", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;

		int n = (Integer) number.getValue();
		if (maps.contains(n) && JOptionPane.showConfirmDialog(this, "La map " + n + " existe déjà. L'écraser ?",
				"Nouvelle map", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
		model.createNew(n, tilePalette.getSelected());
		model.setMapName(name.getText().trim());
		WallSet ws = (WallSet) border.getSelectedItem();
		if (ws != null) ws.apply(model, 0, 0, EditorModel.COLS - 1, EditorModel.ROWS - 1, false);
		model.setSpawn(EditorModel.COLS / 2, EditorModel.ROWS / 2);
		canvas.cancel();
		save();
		refreshMapBox();
		modelChanged();
	}

	private boolean save() {
		applyMapNameSilently();
		try {
			model.save();
			refreshMapBox();
			modelChanged();
			showMessage("Map " + model.getMapNumber() + " enregistrée (en jeu : T puis R pour recharger)");
			return true;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Erreur d'enregistrement :\n" + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	private void applyMapNameSilently() {
		model.setMapName(mapName.getText().trim());
	}

	/**
	 * Enregistre puis lance le jeu sur cette map, dans une fenêtre à part.
	 * (col, row) = position de départ ; -1 = point de départ de la map (ou case survolée, ou centre).
	 */
	public void testMap(int col, int row) {
		if (model.isDirty() && !save()) return;
		if (col < 0) {
			MapData data = model.getData();
			if (data.hasSpawn()) {
				col = data.spawnCol;
				row = data.spawnRow;
			} else if (canvas.getHoverCol() >= 0) {
				col = canvas.getHoverCol();
				row = canvas.getHoverRow();
			} else {
				col = EditorModel.COLS / 2;
				row = EditorModel.ROWS / 2;
			}
		}
		try {
			String java = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
			ProcessBuilder pb = new ProcessBuilder(java, "-cp", System.getProperty("java.class.path"), "main.Main",
					"--map", String.valueOf(model.getMapNumber()), "--pos", String.valueOf(col), String.valueOf(row));
			pb.directory(UtilityTool.RES_DIR.getAbsoluteFile().getParentFile());
			pb.inheritIO();
			pb.start();
			showMessage("Jeu lancé sur la map " + model.getMapNumber() + " en " + col + ", " + row);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Impossible de lancer le jeu :\n" + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void showHelp() {
		JOptionPane.showMessageDialog(this,
				"OUTILS (souris sur la map)\n"
				+ "  B pinceau   L ligne   R rectangle   O contour   W salle (murs auto)   T tracé libre (murs auto)\n"
				+ "  A terrain (eau, chemins : bords automatiques)   F remplissage (Maj+clic : partout)   I pipette   E gomme\n"
				+ "  V sélection   P éléments   S point de départ\n"
				+ "  1-9, [ ], Alt+molette : taille du pinceau / de la ligne / du contour\n"
				+ "  Clic droit : pipette (tiles) ou menu (éléments, sélection) · Échap : annuler la forme\n\n"
				+ "SÉLECTION\n"
				+ "  Ctrl+C copier   Ctrl+X couper   Ctrl+V coller (clic pour poser, Échap pour finir)   Suppr effacer\n\n"
				+ "SALLES\n"
				+ "  Onglet Salles : choisis un jeu de murs (donjon, tapis, haie d'arbres...) ou crée le tien,\n"
				+ "  puis trace un rectangle avec l'outil Salle : les murs et les coins sont posés tout seuls.\n"
				+ "  Outil Tracé : clique des points (chaque clic trace un trait depuis le précédent), reclique le\n"
				+ "  premier point pour fermer la forme. Falaise : \"fosse\" = niveau inférieur (mur du fond seulement),\n"
				+ "  \"plateau\" = niveau supérieur (murs visibles à l'avant). Tracé ouvert : le haut est à gauche du trait.\n"
				+ "  Les arbres se posent toujours en blocs de 2x2 (voir res/tiles/groups.txt).\n\n"
				+ "NAVIGATION\n"
				+ "  Molette : défiler (Maj = horizontal)   Ctrl+molette : zoom   Clic milieu + glisser : se déplacer\n"
				+ "  Navigateur (en bas à gauche) : clic pour aller directement à un endroit\n"
				+ "  G grille   C collisions\n\n"
				+ "FICHIERS\n"
				+ "  Ctrl+S enregistrer   Ctrl+Z / Ctrl+Y annuler / rétablir   F5 tester en jeu\n"
				+ "  En jeu : T (mode debug) puis R recharge la map après un enregistrement",
				"Aide de l'éditeur", JOptionPane.INFORMATION_MESSAGE);
	}

	private void refreshMapBox() {
		updatingMapBox = true;
		mapBox.removeAllItems();
		List<Integer> maps = EditorModel.listMaps();
		int selected = -1;
		for (int i = 0; i < maps.size(); i++) {
			MapData d = maps.get(i) == model.getMapNumber() ? model.getData() : MapData.load(maps.get(i));
			mapBox.addItem(String.format("%d - %s", maps.get(i), d.name.isEmpty() ? String.format("map%02d", maps.get(i)) : d.name));
			if (maps.get(i) == model.getMapNumber()) selected = i;
		}
		if (selected < 0) {
			mapBox.addItem(String.format("%d - (non enregistrée)", model.getMapNumber()));
			selected = mapBox.getItemCount() - 1;
		}
		mapBox.setSelectedIndex(selected);
		mapName.setText(model.getData().name);
		updatingMapBox = false;
	}

	// ===================== Appelé par le canvas / les palettes =====================

	public void modelChanged() {
		setTitle("Éditeur de map - " + model.getMapNumber() + (model.getData().name.isEmpty() ? "" : " - " + model.getData().name)
				+ (model.isDirty() ? "  •  non enregistré" : ""));
		canvas.repaint();
		navigator.refresh();
		updateStatus();
	}

	public void updateStatus() {
		zoomLabel.setText(canvas.getZoomPercent() + " %");
		StringBuilder sb = new StringBuilder();
		sb.append(canvas.getTool().label);
		if (canvas.getTool() == MapCanvas.Tool.TERRAIN && terrainPanel.getSelected() != null) {
			sb.append(" : ").append(terrainPanel.getSelected().name);
		}
		if (canvas.getTool() == MapCanvas.Tool.PENCIL || canvas.getTool() == MapCanvas.Tool.ERASER || canvas.getTool() == MapCanvas.Tool.TERRAIN
				|| canvas.getTool() == MapCanvas.Tool.LINE || canvas.getTool() == MapCanvas.Tool.OUTLINE) {
			sb.append(" (taille ").append(canvas.getBrushSize()).append(")");
		}
		if ((canvas.getTool() == MapCanvas.Tool.ROOM || canvas.getTool() == MapCanvas.Tool.TRACE) && wallSetPanel.getSelected() != null) {
			sb.append(" : ").append(wallSetPanel.getSelected().name);
		}
		if (canvas.isTracing()) sb.append(" - clique le point suivant (1er point = fermer, Entrée / clic droit = finir)");
		if (canvas.isPasting()) sb.append(" - collage : clic pour poser, Échap pour finir");
		Rectangle sel = canvas.getSelection();
		if (sel != null) sb.append("   |   Sélection ").append(sel.width).append(" x ").append(sel.height);
		int col = canvas.getHoverCol(), row = canvas.getHoverRow();
		if (col >= 0) {
			int tile = model.getTile(col, row);
			sb.append("   |   Case ").append(col).append(", ").append(row).append("   tile ").append(tile);
			if (model.collisions.contains(tile)) sb.append(" (bloque)");
			for (Placement p : model.placementsAt(col, row)) {
				Kind k = EntityFactory.getKind(p.category, p.id);
				sb.append("   ").append(k != null ? k.label : p.id + " (inconnu)");
				if (p.args.length > 0) sb.append(" ").append(String.join(" ", p.args));
			}
		}
		status.setText(sb.toString());
	}

	public TilePalette getTilePalette() {
		return tilePalette;
	}

	public ElementPalette getElementPalette() {
		return elementPalette;
	}

	public TerrainPanel getTerrainPanel() {
		return terrainPanel;
	}

	public WallSetPanel getWallSetPanel() {
		return wallSetPanel;
	}
}
