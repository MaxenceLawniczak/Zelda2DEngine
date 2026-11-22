package editor;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import main.EntityFactory;
import main.EntityFactory.Kind;
import main.MapData;
import main.MapData.Placement;

/** Zone d'édition de la map : affichage des tiles, des éléments et gestion des outils à la souris. */
public class MapCanvas extends JPanel {

	public enum Tool {
		PENCIL("Pinceau", "B", "Peindre avec la tile choisie (taille réglable)"),
		LINE("Ligne", "L", "Tracer une ligne droite"),
		RECT("Rectangle", "R", "Remplir un rectangle"),
		OUTLINE("Contour", "O", "Tracer le contour d'un rectangle"),
		ROOM("Salle", "W", "Rectangle entouré de murs (jeu de murs dans l'onglet Salles)"),
		TRACE("Tracé", "T", "Forme libre : clique des points, chaque clic trace un trait depuis le précédent.<br>"
				+ "Reclique le 1er point pour fermer · Entrée, double-clic ou clic droit pour finir"),
		TERRAIN("Terrain", "A", "Peindre de l'eau, un chemin... les bords se posent tout seuls<br>"
				+ "Clic droit : effacer · Maj + glisser : rectangle (terrain choisi dans l'onglet Terrains)"),
		FILL("Remplissage", "F", "Pot de peinture (Maj+clic : remplacer cette tile partout)"),
		PICKER("Pipette", "I", "Prendre la tile d'une case"),
		ERASER("Gomme", "E", "Remettre la tile 0"),
		SELECT("Sélection", "V", "Sélectionner (Ctrl+C / Ctrl+X / Ctrl+V / Suppr)"),
		ELEMENT("Éléments", "P", "Poser objets, monstres, PNJ, événements"),
		SPAWN("Départ", "S", "Point d'apparition du joueur");

		public final String label, key, help;

		Tool(String label, String key, String help) {
			this.label = label;
			this.key = key;
			this.help = help;
		}
	}

	private static final double[] ZOOMS = { 0.25, 0.5, 0.75, 1, 1.5, 2, 3 };

	private final EditorModel model;
	private final MapEditor editor;

	private Tool tool = Tool.PENCIL;
	private int zoomIndex = 3;
	private int brushSize = 1;
	private boolean showGrid = true, showCollisions = false, showElements = true, showEvents = true;

	private int hoverCol = -1, hoverRow = -1;
	private Point dragStart = null;       // Début de la forme en cours (ligne, rectangle, salle, sélection)
	private Point panStart = null;        // Déplacement de la vue (clic milieu)
	private Point lastPainted = null;     // Évite de repeindre la même case pendant un glisser
	private Rectangle selection = null;   // Zone sélectionnée (en cases)
	private EditorModel.Clip pasting = null; // Morceau en cours de collage (suit la souris)
	private final List<Point> tracePoints = new ArrayList<>(); // Points cliqués avec l'outil Tracé

	public MapCanvas(EditorModel model, MapEditor editor) {
		this.model = model;
		this.editor = editor;
		setBackground(EditorTheme.BACKGROUND);
		setFocusable(true);
		updateSize();

		MouseAdapter mouse = new MouseAdapter() {
			@Override public void mousePressed(MouseEvent e) { onPress(e); }
			@Override public void mouseDragged(MouseEvent e) { onDrag(e); }
			@Override public void mouseReleased(MouseEvent e) { onRelease(e); }
			@Override public void mouseMoved(MouseEvent e) { updateHover(e); }
			@Override public void mouseExited(MouseEvent e) { hoverCol = hoverRow = -1; repaint(); editor.updateStatus(); }
			@Override public void mouseEntered(MouseEvent e) { requestFocusInWindow(); }
			@Override public void mouseWheelMoved(MouseWheelEvent e) { onWheel(e); }
		};
		addMouseListener(mouse);
		addMouseMotionListener(mouse);
		addMouseWheelListener(mouse);
	}

	// ===================== Réglages =====================

	public int cell() {
		return (int) Math.round(EditorImages.SIZE * ZOOMS[zoomIndex]);
	}

	public void updateSize() {
		setPreferredSize(new Dimension(EditorModel.COLS * cell(), EditorModel.ROWS * cell()));
		revalidate();
		repaint();
	}

	public void zoom(int delta, Point anchor) {
		int old = cell();
		int newIndex = Math.max(0, Math.min(ZOOMS.length - 1, zoomIndex + delta));
		if (newIndex == zoomIndex) return;
		zoomIndex = newIndex;

		// Garde la case sous la souris au même endroit
		JViewport viewport = viewport();
		Point view = viewport != null ? viewport.getViewPosition() : new Point();
		if (anchor == null && viewport != null) {
			anchor = new Point(view.x + viewport.getWidth() / 2, view.y + viewport.getHeight() / 2);
		}
		updateSize();
		if (viewport != null) {
			double ratio = cell() / (double) old;
			int nx = (int) (anchor.x * ratio - (anchor.x - view.x));
			int ny = (int) (anchor.y * ratio - (anchor.y - view.y));
			SwingUtilities.invokeLater(() -> viewport.setViewPosition(new Point(Math.max(0, nx), Math.max(0, ny))));
		}
		editor.updateStatus();
	}

	public void resetZoom() {
		zoomIndex = 3;
		updateSize();
		editor.updateStatus();
	}

	private JViewport viewport() {
		return (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, this);
	}

	public int getZoomPercent() { return (int) (ZOOMS[zoomIndex] * 100); }
	public Tool getTool() { return tool; }
	public int getBrushSize() { return brushSize; }
	public void setBrushSize(int size) { brushSize = Math.max(1, Math.min(9, size)); repaint(); }

	public void setTool(Tool tool) {
		this.tool = tool;
		dragStart = null;
		pasting = null;
		tracePoints.clear();
		if (tool != Tool.SELECT) selection = null;
		setCursor(tool == Tool.PICKER ? Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR) : Cursor.getDefaultCursor());
		repaint();
		editor.updateStatus();
	}

	public void toggleGrid() { showGrid = !showGrid; repaint(); }
	public void toggleCollisions() { showCollisions = !showCollisions; repaint(); }
	public void toggleElements() { showElements = !showElements; repaint(); }
	public void toggleEvents() { showEvents = !showEvents; repaint(); }

	public int getHoverCol() { return hoverCol; }
	public int getHoverRow() { return hoverRow; }
	public Rectangle getSelection() { return selection; }
	public boolean isPasting() { return pasting != null; }

	// ===================== Sélection / presse-papiers =====================

	public void copySelection(boolean cut) {
		if (selection == null) return;
		editor.setClipboard(model.copy(selection));
		if (cut) {
			model.pushUndo();
			model.clear(selection, 0);
			editor.modelChanged();
		}
	}

	public void deleteSelection() {
		if (selection == null) return;
		model.pushUndo();
		model.clear(selection, 0);
		editor.modelChanged();
	}

	/** Mode collage : le morceau suit la souris, clic gauche pour le poser, Échap pour arrêter. */
	public void startPaste(EditorModel.Clip clip) {
		if (clip == null) return;
		setTool(Tool.SELECT);
		pasting = clip;
		repaint();
		editor.updateStatus();
	}

	public void selectAll() {
		selection = new Rectangle(0, 0, EditorModel.COLS, EditorModel.ROWS);
		repaint();
		editor.updateStatus();
	}

	public boolean isTracing() {
		return !tracePoints.isEmpty();
	}

	/** Termine le tracé en cours (closed = forme fermée). */
	public void finishTrace(boolean closed) {
		WallSet ws = editor.getWallSetPanel().getSelected();
		if (ws != null && tracePoints.size() >= 2) {
			model.pushUndo();
			for (Map.Entry<Point, Integer> e : traceCells(tracePoints, closed).entrySet()) {
				model.setTile(e.getKey().x, e.getKey().y, e.getValue());
			}
			editor.modelChanged();
		}
		tracePoints.clear();
		repaint();
		editor.updateStatus();
	}

	private Map<Point, Integer> traceCells(List<Point> points, boolean closed) {
		WallSet ws = editor.getWallSetPanel().getSelected();
		if (ws == null) return new HashMap<>();
		Map<Point, Integer> cells = ws.trace(points, closed, editor.getWallSetPanel().isFlip(), editor.getWallSetPanel().isFillInside());
		cells.keySet().removeIf(p -> !model.inBounds(p.x, p.y));
		return cells;
	}

	/** Échap : annule la forme / le collage / la sélection en cours. */
	public void cancel() {
		dragStart = null;
		pasting = null;
		tracePoints.clear();
		selection = null;
		repaint();
		editor.updateStatus();
	}

	// ===================== Souris =====================

	private Point cellAt(MouseEvent e) {
		return new Point(Math.floorDiv(e.getX(), cell()), Math.floorDiv(e.getY(), cell()));
	}

	private Point clampCell(Point p) {
		return new Point(Math.max(0, Math.min(EditorModel.COLS - 1, p.x)), Math.max(0, Math.min(EditorModel.ROWS - 1, p.y)));
	}

	private void updateHover(MouseEvent e) {
		Point p = cellAt(e);
		boolean inside = model.inBounds(p.x, p.y);
		int col = inside ? p.x : -1, row = inside ? p.y : -1;
		if (col != hoverCol || row != hoverRow) {
			hoverCol = col;
			hoverRow = row;
			repaint();
			editor.updateStatus();
		}
	}

	private void onPress(MouseEvent e) {
		requestFocusInWindow();
		if (SwingUtilities.isMiddleMouseButton(e)) {
			panStart = e.getLocationOnScreen();
			setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			return;
		}
		Point c = cellAt(e);
		if (!model.inBounds(c.x, c.y)) return;

		if (SwingUtilities.isRightMouseButton(e)) {
			if (tool == Tool.TRACE && !tracePoints.isEmpty()) {
				finishTrace(false);
				return;
			}
			if (tool == Tool.TERRAIN) {
				model.pushUndo();
				lastPainted = null;
				terrainAt(c, true);
				editor.modelChanged();
				return;
			}
			if (pasting != null || dragStart != null) {
				cancel();
			} else if (tool == Tool.ELEMENT || tool == Tool.SPAWN || tool == Tool.SELECT) {
				showContextMenu(e, c.x, c.y);
			} else {
				// Clic droit avec un outil de tiles = pipette
				editor.getTilePalette().setSelected(model.getTile(c.x, c.y));
			}
			return;
		}

		if (pasting != null) {
			model.pushUndo();
			model.paste(pasting, c.x, c.y);
			editor.modelChanged();
			return;
		}

		int tile = editor.getTilePalette().getSelected();
		model.beginStroke();
		switch (tool) {
		case TRACE:
			if (!tracePoints.isEmpty() && c.equals(tracePoints.get(0)) && tracePoints.size() >= 2) {
				finishTrace(true); // Retour au premier point : forme fermée
			} else if (!tracePoints.isEmpty() && (c.equals(tracePoints.get(tracePoints.size() - 1)) || e.getClickCount() >= 2)) {
				finishTrace(false); // Double-clic : tracé ouvert
			} else {
				tracePoints.add(c);
			}
			editor.updateStatus();
			break;
		case PENCIL:
		case ERASER:
			model.pushUndo();
			lastPainted = null;
			paintAt(c);
			break;
		case TERRAIN:
			model.pushUndo();
			lastPainted = null;
			if (e.isShiftDown()) {
				dragStart = c; // Rectangle de terrain
			} else {
				terrainAt(c, false);
			}
			break;
		case LINE:
		case RECT:
		case OUTLINE:
		case ROOM:
		case SELECT:
			dragStart = c;
			if (tool == Tool.SELECT) selection = new Rectangle(c.x, c.y, 1, 1);
			break;
		case FILL:
			model.pushUndo();
			if (e.isShiftDown()) {
				int count = model.replaceAll(model.getTile(c.x, c.y), tile);
				editor.showMessage(count + " case(s) remplacée(s) par la tile " + tile);
			} else {
				model.floodFill(c.x, c.y, tile);
			}
			break;
		case PICKER:
			editor.getTilePalette().setSelected(model.getTile(c.x, c.y));
			editor.setTool(Tool.PENCIL);
			break;
		case ELEMENT:
			model.pushUndo();
			lastPainted = null;
			placeAt(c);
			break;
		case SPAWN:
			model.pushUndo();
			model.setSpawn(c.x, c.y);
			break;
		}
		repaint();
		editor.modelChanged();
	}

	private void onDrag(MouseEvent e) {
		if (panStart != null) {
			JViewport viewport = viewport();
			if (viewport != null) {
				Point now = e.getLocationOnScreen();
				Point view = viewport.getViewPosition();
				view.translate(panStart.x - now.x, panStart.y - now.y);
				view.x = Math.max(0, Math.min(Math.max(0, getWidth() - viewport.getWidth()), view.x));
				view.y = Math.max(0, Math.min(Math.max(0, getHeight() - viewport.getHeight()), view.y));
				viewport.setViewPosition(view);
				panStart = now;
			}
			return;
		}
		updateHover(e);
		if (tool == Tool.TERRAIN && SwingUtilities.isRightMouseButton(e) && model.inBounds(cellAt(e).x, cellAt(e).y)) {
			terrainAt(cellAt(e), true);
			editor.modelChanged();
			repaint();
			return;
		}
		if (!SwingUtilities.isLeftMouseButton(e) || pasting != null) return;
		Point c = cellAt(e);
		if (tool == Tool.SELECT && dragStart != null) {
			Point end = clampCell(c);
			selection = new Rectangle(Math.min(dragStart.x, end.x), Math.min(dragStart.y, end.y),
					Math.abs(end.x - dragStart.x) + 1, Math.abs(end.y - dragStart.y) + 1);
		}
		if (!model.inBounds(c.x, c.y)) { repaint(); return; }
		if (tool == Tool.TERRAIN && dragStart == null) {
			terrainAt(c, SwingUtilities.isRightMouseButton(e));
			editor.modelChanged();
		} else if (tool == Tool.PENCIL || tool == Tool.ERASER) {
			paintAt(c);
			editor.modelChanged();
		} else if (tool == Tool.ELEMENT) {
			placeAt(c); // Permet de "peindre" de l'herbe, des buissons...
			editor.modelChanged();
		}
		repaint();
	}

	private void onRelease(MouseEvent e) {
		if (panStart != null) {
			panStart = null;
			setCursor(tool == Tool.PICKER ? Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR) : Cursor.getDefaultCursor());
			return;
		}
		if (dragStart == null || !SwingUtilities.isLeftMouseButton(e)) return;
		Point end = clampCell(cellAt(e));
		int tile = editor.getTilePalette().getSelected();
		switch (tool) {
		case LINE:
			model.pushUndo();
			model.line(dragStart.x, dragStart.y, end.x, end.y, tile, brushSize);
			break;
		case RECT:
			model.pushUndo();
			model.fillRect(dragStart.x, dragStart.y, end.x, end.y, tile);
			break;
		case OUTLINE:
			model.pushUndo();
			model.rectOutline(dragStart.x, dragStart.y, end.x, end.y, tile, brushSize);
			break;
		case TERRAIN: {
			Terrain terrain = editor.getTerrainPanel().getSelected();
			if (terrain != null) {
				List<Point> cells = new ArrayList<>();
				for (int x = Math.min(dragStart.x, end.x); x <= Math.max(dragStart.x, end.x); x++)
					for (int y = Math.min(dragStart.y, end.y); y <= Math.max(dragStart.y, end.y); y++) cells.add(new Point(x, y));
				terrain.paint(model, cells, false);
			}
			break;
		}
		case ROOM:
			WallSet ws = editor.getWallSetPanel().getSelected();
			if (ws != null) {
				model.pushUndo();
				for (Map.Entry<Point, Integer> cell : ws.rectangle(dragStart.x, dragStart.y, end.x, end.y,
						editor.getWallSetPanel().isFillInside()).entrySet()) {
					model.setTile(cell.getKey().x, cell.getKey().y, cell.getValue());
				}
			}
			break;
		default:
			break;
		}
		dragStart = null;
		repaint();
		editor.modelChanged();
	}

	private void onWheel(MouseWheelEvent e) {
		if (e.isControlDown()) {
			zoom(e.getWheelRotation() < 0 ? 1 : -1, e.getPoint());
		} else if (e.isAltDown()) {
			// Alt + molette : taille du pinceau
			editor.setBrushSize(brushSize + (e.getWheelRotation() < 0 ? 1 : -1));
		} else {
			JViewport viewport = viewport();
			if (viewport == null) return;
			Point view = viewport.getViewPosition();
			int amount = e.getWheelRotation() * cell() * 3;
			if (e.isShiftDown()) view.x += amount; else view.y += amount;
			view.x = Math.max(0, Math.min(Math.max(0, getWidth() - viewport.getWidth()), view.x));
			view.y = Math.max(0, Math.min(Math.max(0, getHeight() - viewport.getHeight()), view.y));
			viewport.setViewPosition(view);
		}
	}

	private void paintAt(Point c) {
		if (c.equals(lastPainted)) return;
		// Si la souris a sauté des cases (mouvement rapide), on relie par une ligne
		int tile = tool == Tool.ERASER ? 0 : editor.getTilePalette().getSelected();
		if (lastPainted != null) {
			model.line(lastPainted.x, lastPainted.y, c.x, c.y, tile, brushSize);
		} else {
			model.stamp(c.x, c.y, tile, brushSize);
		}
		lastPainted = c;
		if (tool == Tool.PENCIL) editor.tileUsed(tile);
	}

	/** Peint (ou efface) le terrain choisi avec le pinceau, puis les bords sont recalculés. */
	private void terrainAt(Point c, boolean erase) {
		if (c.equals(lastPainted)) return;
		lastPainted = c;
		Terrain terrain = editor.getTerrainPanel().getSelected();
		if (terrain == null) return;
		List<Point> cells = new ArrayList<>();
		int start = -(brushSize - 1) / 2;
		for (int dc = 0; dc < brushSize; dc++)
			for (int dr = 0; dr < brushSize; dr++) cells.add(new Point(c.x + start + dc, c.y + start + dr));
		terrain.paint(model, cells, erase);
	}

	private void placeAt(Point c) {
		if (c.equals(lastPainted)) return;
		lastPainted = c;
		Placement p = editor.getElementPalette().createPlacement(c.x, c.y);
		if (p != null) model.place(p);
	}

	private void showContextMenu(MouseEvent e, int col, int row) {
		JPopupMenu menu = new JPopupMenu();
		if (selection != null && selection.contains(col, row)) {
			menu.add(menuItem("Copier la sélection (Ctrl+C)", () -> copySelection(false)));
			menu.add(menuItem("Couper la sélection (Ctrl+X)", () -> copySelection(true)));
			menu.add(menuItem("Effacer la sélection (Suppr)", this::deleteSelection));
			menu.add(menuItem("Remplir avec la tile choisie", () -> {
				model.pushUndo();
				model.fillRect(selection.x, selection.y, selection.x + selection.width - 1, selection.y + selection.height - 1,
						editor.getTilePalette().getSelected());
				editor.modelChanged();
			}));
			menu.addSeparator();
		}
		List<Placement> here = model.placementsAt(col, row);
		for (Placement p : here) {
			Kind k = EntityFactory.getKind(p.category, p.id);
			String name = k != null ? k.label : p.id;
			if (ElementPalette.isEditable(p)) {
				menu.add(menuItem("Modifier : " + name + "...", () -> {
					model.pushUndo();
					if (ElementPalette.editPlacement(this, p)) {
						model.markDirty();
						editor.modelChanged();
					}
				}));
			}
			menu.add(menuItem("Supprimer : " + name, () -> {
				model.pushUndo();
				model.remove(p);
				editor.modelChanged();
			}));
		}
		if (!here.isEmpty()) menu.addSeparator();
		menu.add(menuItem("Point de départ ici", () -> {
			model.pushUndo();
			model.setSpawn(col, row);
			editor.modelChanged();
		}));
		menu.add(menuItem("Utiliser comme destination de téléporteur",
				() -> editor.getElementPalette().setTeleportTarget(model.getMapNumber(), col, row)));
		menu.add(menuItem("Tester le jeu ici", () -> editor.testMap(col, row)));
		menu.show(this, e.getX(), e.getY());
	}

	private static JMenuItem menuItem(String label, Runnable action) {
		JMenuItem item = new JMenuItem(label);
		item.addActionListener(a -> action.run());
		return item;
	}

	// ===================== Aperçu des formes =====================

	/** Cases (et tiles) que la forme en cours va modifier, pour l'aperçu. */
	private Map<Point, Integer> previewCells() {
		Map<Point, Integer> cells = new HashMap<>();
		int tile = tool == Tool.ERASER ? 0 : editor.getTilePalette().getSelected();
		if (dragStart != null && hoverCol >= 0) {
			int left = Math.min(dragStart.x, hoverCol), right = Math.max(dragStart.x, hoverCol);
			int top = Math.min(dragStart.y, hoverRow), bottom = Math.max(dragStart.y, hoverRow);
			switch (tool) {
			case LINE: {
				int c1 = dragStart.x, r1 = dragStart.y, dc = Math.abs(hoverCol - c1), dr = -Math.abs(hoverRow - r1);
				int sc = c1 < hoverCol ? 1 : -1, sr = r1 < hoverRow ? 1 : -1, err = dc + dr;
				while (true) {
					addBrush(cells, c1, r1, tile);
					if (c1 == hoverCol && r1 == hoverRow) break;
					int e2 = 2 * err;
					if (e2 >= dr) { err += dr; c1 += sc; }
					if (e2 <= dc) { err += dc; r1 += sr; }
				}
				break;
			}
			case TERRAIN:
			case RECT:
			case OUTLINE:
				if (tool == Tool.TERRAIN) {
					Terrain terrain = editor.getTerrainPanel().getSelected();
					if (terrain != null) tile = terrain.base;
				}
				for (int c = left; c <= right; c++) {
					for (int r = top; r <= bottom; r++) {
						boolean border = c < left + brushSize || c > right - brushSize || r < top + brushSize || r > bottom - brushSize;
						if (tool != Tool.OUTLINE || border) cells.put(new Point(c, r), tile);
					}
				}
				break;
			case ROOM: {
				WallSet ws = editor.getWallSetPanel().getSelected();
				if (ws == null) break;
				cells.putAll(ws.rectangle(dragStart.x, dragStart.y, hoverCol, hoverRow, editor.getWallSetPanel().isFillInside()));
				break;
			}
			default:
				break;
			}
		} else if (tool == Tool.TRACE && !tracePoints.isEmpty() && hoverCol >= 0) {
			List<Point> points = new ArrayList<>(tracePoints);
			Point hover = new Point(hoverCol, hoverRow);
			boolean closing = hover.equals(points.get(0)) && points.size() >= 2;
			if (!closing && !hover.equals(points.get(points.size() - 1))) points.add(hover);
			cells.putAll(traceCells(points, closing));
		} else if (hoverCol >= 0 && tool == Tool.TERRAIN && editor.getTerrainPanel().getSelected() != null) {
			addBrush(cells, hoverCol, hoverRow, editor.getTerrainPanel().getSelected().base);
		} else if (hoverCol >= 0 && (tool == Tool.PENCIL || tool == Tool.ERASER)) {
			TileGroup group = tool == Tool.PENCIL ? model.groupOf(tile) : null;
			if (group != null) {
				// Arbre (ou autre groupe) : on montre le groupe entier
				int[] o = group.offsetOf(tile);
				for (int dx = 0; dx < group.width; dx++) {
					for (int dy = 0; dy < group.height; dy++) {
						int c = hoverCol - o[0] + dx, r = hoverRow - o[1] + dy;
						if (model.inBounds(c, r)) cells.put(new Point(c, r), group.tileAt(dx, dy));
					}
				}
			} else {
				addBrush(cells, hoverCol, hoverRow, tile);
			}
		}
		cells.keySet().removeIf(p -> !model.inBounds(p.x, p.y));
		return cells;
	}

	private void addBrush(Map<Point, Integer> cells, int col, int row, int tile) {
		int start = -(brushSize - 1) / 2;
		for (int dc = 0; dc < brushSize; dc++) {
			for (int dr = 0; dr < brushSize; dr++) {
				int c = col + start + dc, r = row + start + dr;
				if (model.inBounds(c, r)) cells.put(new Point(c, r), tile);
			}
		}
	}

	// ===================== Dessin =====================

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		int cell = cell();
		Rectangle clip = g2.getClipBounds();
		if (clip == null) clip = new Rectangle(0, 0, getWidth(), getHeight());
		int firstCol = Math.max(0, clip.x / cell);
		int firstRow = Math.max(0, clip.y / cell);
		int lastCol = Math.min(EditorModel.COLS - 1, (clip.x + clip.width) / cell);
		int lastRow = Math.min(EditorModel.ROWS - 1, (clip.y + clip.height) / cell);

		// Tiles
		for (int col = firstCol; col <= lastCol; col++) {
			for (int row = firstRow; row <= lastRow; row++) {
				int tile = model.getTile(col, row);
				g2.drawImage(EditorImages.tile(tile), col * cell, row * cell, cell, cell, null);
				if (showCollisions && model.collisions.contains(tile)) {
					g2.setColor(new Color(255, 0, 0, 90));
					g2.fillRect(col * cell, row * cell, cell, cell);
				}
			}
		}

		// Grille (une ligne plus marquée toutes les 10 cases)
		if (showGrid && cell >= 12) {
			g2.setStroke(new BasicStroke(1));
			for (int col = firstCol; col <= lastCol + 1; col++) {
				g2.setColor(col % 10 == 0 ? new Color(0, 0, 0, 120) : new Color(0, 0, 0, 45));
				g2.drawLine(col * cell, firstRow * cell, col * cell, (lastRow + 1) * cell);
			}
			for (int row = firstRow; row <= lastRow + 1; row++) {
				g2.setColor(row % 10 == 0 ? new Color(0, 0, 0, 120) : new Color(0, 0, 0, 45));
				g2.drawLine(firstCol * cell, row * cell, (lastCol + 1) * cell, row * cell);
			}
		}

		MapData data = model.getData();
		Font small = new Font("Arial", Font.BOLD, Math.max(9, cell / 3));
		g2.setFont(small);

		// Éléments (tiles interactives d'abord, puis objets, PNJ, monstres)
		if (showElements) {
			for (String category : new String[] { MapData.TILE, MapData.OBJECT, MapData.NPC, MapData.MONSTER }) {
				for (Placement p : data.get(category)) {
					drawPlacement(g2, p, cell);
				}
			}
		}

		// Événements
		if (showEvents) {
			for (Placement p : data.get(MapData.EVENT)) {
				Kind k = EntityFactory.getKind(MapData.EVENT, p.id);
				Color c = k != null ? k.color : Color.MAGENTA;
				int x = p.col * cell, y = p.row * cell;
				g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 90));
				g2.fillRect(x, y, cell, cell);
				g2.setColor(c);
				g2.setStroke(new BasicStroke(2));
				g2.drawRect(x + 1, y + 1, cell - 2, cell - 2);
				String label = p.id.equals("teleport") ? "→" + p.arg(1, "?") : p.id.equals("pit") ? "!" : "+";
				outlinedText(g2, label, x + 3, y + small.getSize() + 1, Color.WHITE);
			}
		}

		// Point de départ
		if (data.hasSpawn()) {
			int x = data.spawnCol * cell, y = data.spawnRow * cell;
			g2.setColor(new Color(0, 255, 120, 110));
			g2.fillOval(x + 2, y + 2, cell - 4, cell - 4);
			g2.setColor(new Color(0, 160, 70));
			g2.setStroke(new BasicStroke(2));
			g2.drawOval(x + 2, y + 2, cell - 4, cell - 4);
			outlinedText(g2, "S", x + cell / 2 - small.getSize() / 3, y + cell / 2 + small.getSize() / 3, Color.WHITE);
		}

		// Aperçu de la forme / du pinceau
		Map<Point, Integer> preview = previewCells();
		if (!preview.isEmpty()) {
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
			for (Map.Entry<Point, Integer> entry : preview.entrySet()) {
				Point p = entry.getKey();
				g2.drawImage(EditorImages.tile(entry.getValue()), p.x * cell, p.y * cell, cell, cell, null);
			}
			g2.setComposite(AlphaComposite.SrcOver);
			if (dragStart != null && hoverCol >= 0) {
				int w = Math.abs(hoverCol - dragStart.x) + 1, h = Math.abs(hoverRow - dragStart.y) + 1;
				outlinedText(g2, w + " x " + h, Math.max(dragStart.x, hoverCol) * cell + cell + 4, Math.max(dragStart.y, hoverRow) * cell + cell, EditorTheme.ACCENT);
			}
		}

		// Points déjà cliqués du tracé (le premier en vert : cliquer dessus ferme la forme)
		for (int i = 0; i < tracePoints.size(); i++) {
			Point p = tracePoints.get(i);
			g2.setColor(i == 0 ? new Color(90, 230, 120) : EditorTheme.ACCENT);
			g2.fillOval(p.x * cell + cell / 3, p.y * cell + cell / 3, cell / 3, cell / 3);
			g2.setColor(Color.BLACK);
			g2.drawOval(p.x * cell + cell / 3, p.y * cell + cell / 3, cell / 3, cell / 3);
		}

		// Collage en cours : le morceau suit la souris
		if (pasting != null && hoverCol >= 0) {
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
			for (int c = 0; c < pasting.width; c++) {
				for (int r = 0; r < pasting.height; r++) {
					g2.drawImage(EditorImages.tile(pasting.tiles[c][r]), (hoverCol + c) * cell, (hoverRow + r) * cell, cell, cell, null);
				}
			}
			g2.setComposite(AlphaComposite.SrcOver);
			drawDashedRect(g2, hoverCol * cell, hoverRow * cell, pasting.width * cell, pasting.height * cell, EditorTheme.ACCENT);
		}

		// Sélection
		if (selection != null && pasting == null) {
			g2.setColor(new Color(255, 196, 80, 40));
			g2.fillRect(selection.x * cell, selection.y * cell, selection.width * cell, selection.height * cell);
			drawDashedRect(g2, selection.x * cell, selection.y * cell, selection.width * cell, selection.height * cell, Color.WHITE);
		}

		// Case survolée
		if (hoverCol >= 0 && dragStart == null && pasting == null) {
			int x = hoverCol * cell, y = hoverRow * cell;
			if (tool == Tool.ELEMENT && editor.getElementPalette().getSelectedKind() != null) {
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
				g2.drawImage(ElementPalette.iconFor(editor.getElementPalette().getSelectedKind(), cell), x, y, cell, cell, null);
				g2.setComposite(AlphaComposite.SrcOver);
			}
			g2.setColor(Color.WHITE);
			g2.setStroke(new BasicStroke(2));
			int size = (tool == Tool.PENCIL || tool == Tool.ERASER || tool == Tool.TERRAIN) ? brushSize : 1;
			int start = -(size - 1) / 2;
			g2.drawRect(x + start * cell, y + start * cell, size * cell - 1, size * cell - 1);
		}
	}

	private static void drawDashedRect(Graphics2D g2, int x, int y, int w, int h, Color color) {
		g2.setColor(Color.BLACK);
		g2.setStroke(new BasicStroke(2));
		g2.drawRect(x, y, w, h);
		g2.setColor(color);
		g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[] { 6, 4 }, 0));
		g2.drawRect(x, y, w, h);
		g2.setStroke(new BasicStroke(1));
	}

	private void drawPlacement(Graphics2D g2, Placement p, int cell) {
		Kind k = EntityFactory.getKind(p.category, p.id);
		int x = p.col * cell, y = p.row * cell;
		if (k == null) {
			// Élément inconnu (faute de frappe dans le fichier ?) : croix rouge
			g2.setColor(Color.RED);
			g2.drawLine(x, y, x + cell, y + cell);
			g2.drawLine(x + cell, y, x, y + cell);
			return;
		}
		boolean small = p.id.equals("heart") || p.id.equals("rupee_green");
		int size = small ? cell / 2 : cell;
		int offset = small ? cell / 4 : 0;
		g2.drawImage(ElementPalette.iconFor(k, size), x + offset, y + offset, size, size, null);

		if (p.category.equals(MapData.MONSTER) || p.category.equals(MapData.NPC)) {
			g2.setColor(p.category.equals(MapData.MONSTER) ? Color.RED : Color.CYAN);
			g2.setStroke(new BasicStroke(2));
			g2.drawRect(x + 1, y + 1, cell - 2, cell - 2);
		}
		// Contenu des coffres / direction des lumières murales
		if (p.id.equals("chest") && p.args.length > 0) {
			Kind loot = EntityFactory.getKind(MapData.OBJECT, p.args[0]);
			if (loot != null) g2.drawImage(ElementPalette.iconFor(loot, cell / 2), x + cell / 2, y + cell / 2, cell / 2, cell / 2, null);
		} else if (p.id.equals("wall_light")) {
			outlinedText(g2, p.arg(0, "up").substring(0, 1).toUpperCase(), x + 2, y + cell - 3, Color.YELLOW);
		}
	}

	private static void outlinedText(Graphics2D g2, String text, int x, int y, Color color) {
		g2.setColor(Color.BLACK);
		g2.drawString(text, x - 1, y);
		g2.drawString(text, x + 1, y);
		g2.drawString(text, x, y - 1);
		g2.drawString(text, x, y + 1);
		g2.setColor(color);
		g2.drawString(text, x, y);
	}

}
