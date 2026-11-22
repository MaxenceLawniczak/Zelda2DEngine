package editor;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.MapData;
import main.MapData.Placement;
import main.UtilityTool;
import tile.TileManager;

/** Données de la map en cours d'édition + annuler/rétablir + lecture/écriture des fichiers. */
public class EditorModel {

	public static final int COLS = MapData.COLS;
	public static final int ROWS = MapData.ROWS;
	private static final int MAX_UNDO = 200;

	private static class Snapshot {
		final int[][] tiles;
		final MapData data;

		Snapshot(int[][] tiles, MapData data) {
			this.tiles = tiles;
			this.data = data;
		}
	}

	public final int tileCount;
	public final Set<Integer> collisions;
	public final List<TileGroup> groups;
	public final TileVariants variants = new TileVariants();
	public boolean randomVariants = true; // Herbe 0 -> parfois 1, chemin 79 -> parfois 80...

	private int mapNumber = 1;
	private int[][] tiles = new int[COLS][ROWS];
	private MapData data = new MapData();
	private boolean dirty = false;
	private boolean collisionsDirty = false;

	private final Deque<Snapshot> undoStack = new ArrayDeque<>();
	private final Deque<Snapshot> redoStack = new ArrayDeque<>();

	public EditorModel() {
		tileCount = TileManager.countTiles();
		collisions = new TreeSet<>(TileManager.loadCollisions());
		groups = TileGroup.load();
	}

	// ===================== Fichiers =====================

	public static File mapsDir() {
		return new File(UtilityTool.RES_DIR, "maps");
	}

	/** Numéros des maps existantes (map01.txt -> 1, ...). */
	public static List<Integer> listMaps() {
		List<Integer> list = new ArrayList<>();
		File[] files = mapsDir().listFiles();
		if (files != null) {
			Pattern p = Pattern.compile("map(\\d+)\\.txt");
			for (File f : files) {
				Matcher m = p.matcher(f.getName());
				if (m.matches()) list.add(Integer.parseInt(m.group(1)));
			}
		}
		Collections.sort(list);
		return list;
	}

	public void load(int number) {
		mapNumber = number;
		int[][] loaded = TileManager.readMap(MapData.tilesPath(number), tileCount);
		tiles = loaded != null ? loaded : new int[COLS][ROWS];
		data = MapData.load(number);
		undoStack.clear();
		redoStack.clear();
		dirty = false;
	}

	public void createNew(int number, int fillTile) {
		mapNumber = number;
		tiles = new int[COLS][ROWS];
		for (int[] column : tiles) java.util.Arrays.fill(column, fillTile);
		data = new MapData();
		undoStack.clear();
		redoStack.clear();
		dirty = true;
	}

	public void save() throws IOException {
		File dir = mapsDir();
		if (!dir.isDirectory() && !dir.mkdirs()) {
			throw new IOException("Impossible de créer le dossier " + dir);
		}
		// Même format que les maps existantes : un numéro suivi d'un espace, fins de ligne Windows
		try (PrintWriter pw = new PrintWriter(new File(UtilityTool.RES_DIR, MapData.tilesPath(mapNumber)), StandardCharsets.UTF_8)) {
			for (int row = 0; row < ROWS; row++) {
				StringBuilder sb = new StringBuilder();
				for (int col = 0; col < COLS; col++) {
					sb.append(tiles[col][row]).append(' ');
				}
				pw.print(sb);
				pw.print("\r\n");
			}
		}
		data.save(new File(UtilityTool.RES_DIR, MapData.dataPath(mapNumber)));
		if (collisionsDirty) {
			saveCollisions();
		}
		dirty = false;
	}

	private void saveCollisions() throws IOException {
		try (PrintWriter pw = new PrintWriter(new File(UtilityTool.RES_DIR, TileManager.COLLISIONS_PATH), StandardCharsets.UTF_8)) {
			pw.print("# Tiles qui bloquent le joueur (numéros des images tileXXXX.png).\r\n");
			pw.print("# Modifiable à la main ou avec l'éditeur de map (clic droit sur une tile de la palette).\r\n");
			int count = 0;
			StringBuilder line = new StringBuilder();
			for (int id : collisions) {
				line.append(id);
				if (++count % 25 == 0) {
					pw.print(line.toString().trim() + "\r\n");
					line.setLength(0);
				} else {
					line.append(' ');
				}
			}
			if (line.length() > 0) pw.print(line.toString().trim() + "\r\n");
		}
		collisionsDirty = false;
	}

	// ===================== Annuler / rétablir =====================

	private Snapshot snapshot() {
		int[][] copy = new int[COLS][];
		for (int c = 0; c < COLS; c++) copy[c] = tiles[c].clone();
		return new Snapshot(copy, data.copy());
	}

	/** À appeler AVANT chaque modification (une fois par coup de pinceau). */
	public void pushUndo() {
		undoStack.push(snapshot());
		if (undoStack.size() > MAX_UNDO) undoStack.removeLast();
		redoStack.clear();
	}

	public boolean undo() {
		if (undoStack.isEmpty()) return false;
		redoStack.push(snapshot());
		restore(undoStack.pop());
		return true;
	}

	public boolean redo() {
		if (redoStack.isEmpty()) return false;
		undoStack.push(snapshot());
		restore(redoStack.pop());
		return true;
	}

	private void restore(Snapshot s) {
		tiles = s.tiles;
		data = s.data;
		dirty = true;
	}

	// ===================== Tiles =====================

	public boolean inBounds(int col, int row) {
		return col >= 0 && row >= 0 && col < COLS && row < ROWS;
	}

	public int getTile(int col, int row) {
		return inBounds(col, row) ? tiles[col][row] : 0;
	}

	public void setTile(int col, int row, int tile) {
		if (inBounds(col, row) && tiles[col][row] != tile) {
			tiles[col][row] = tile;
			dirty = true;
		}
	}

	// ===================== Groupes de tiles (arbres 2x2...) =====================

	private Point strokeAnchor = null; // Alignement des groupes pendant un coup de pinceau

	/** À appeler au début de chaque coup de pinceau / ligne. */
	public void beginStroke() {
		strokeAnchor = null;
	}

	/** Variante aléatoire de la tile (si activé dans l'éditeur). */
	public int variant(int tile) {
		return randomVariants ? variants.pick(tile) : tile;
	}

	public TileGroup groupOf(int tile) {
		for (TileGroup g : groups) {
			if (g.offsetOf(tile) != null) return g;
		}
		return null;
	}

	/** Pose une tile en respectant les groupes : une tile de groupe pose le groupe entier, sinon on casse le groupe écrasé. */
	public void paintTile(int col, int row, int tile) {
		TileGroup g = groupOf(tile);
		if (g != null) {
			placeGroupCovering(g, tile, col, row);
			return;
		}
		breakGroupAt(col, row, tile);
		setTile(col, row, variant(tile));
	}

	/** Si la case fait partie d'un groupe intact, tout le groupe est remplacé par "replacement". */
	private void breakGroupAt(int col, int row, int replacement) {
		int current = getTile(col, row);
		TileGroup g = groupOf(current);
		if (g == null) return;
		int[] o = g.offsetOf(current);
		int ax = col - o[0], ay = row - o[1];
		if (!isIntact(g, ax, ay)) return;
		for (int dx = 0; dx < g.width; dx++) {
			for (int dy = 0; dy < g.height; dy++) {
				setTile(ax + dx, ay + dy, replacement);
			}
		}
	}

	private boolean isIntact(TileGroup g, int ax, int ay) {
		for (int dx = 0; dx < g.width; dx++) {
			for (int dy = 0; dy < g.height; dy++) {
				if (!inBounds(ax + dx, ay + dy) || tiles[ax + dx][ay + dy] != g.tileAt(dx, dy)) return false;
			}
		}
		return true;
	}

	/** Pose le groupe entier dont la case (col, row) fait partie (aligné sur le premier du coup de pinceau). */
	private void placeGroupCovering(TileGroup g, int tile, int col, int row) {
		int ax, ay;
		if (strokeAnchor == null) {
			int[] o = g.offsetOf(tile);
			ax = col - o[0];
			ay = row - o[1];
			strokeAnchor = new Point(ax, ay);
		} else {
			ax = strokeAnchor.x + Math.floorDiv(col - strokeAnchor.x, g.width) * g.width;
			ay = strokeAnchor.y + Math.floorDiv(row - strokeAnchor.y, g.height) * g.height;
		}
		placeGroup(g, ax, ay);
	}

	/** Pose un groupe entier à partir de son coin haut-gauche (rien si ça dépasse de la map). */
	public boolean placeGroup(TileGroup g, int ax, int ay) {
		if (!inBounds(ax, ay) || !inBounds(ax + g.width - 1, ay + g.height - 1)) return false;
		if (isIntact(g, ax, ay)) return true;
		for (int dx = 0; dx < g.width; dx++) {
			for (int dy = 0; dy < g.height; dy++) {
				breakGroupAt(ax + dx, ay + dy, 0); // Les arbres écrasés à moitié disparaissent en entier
			}
		}
		for (int dx = 0; dx < g.width; dx++) {
			for (int dy = 0; dy < g.height; dy++) {
				setTile(ax + dx, ay + dy, g.tileAt(dx, dy));
			}
		}
		return true;
	}

	// ===================== Outils de dessin =====================

	public void fillRect(int c1, int r1, int c2, int r2, int tile) {
		int left = Math.min(c1, c2), right = Math.max(c1, c2), top = Math.min(r1, r2), bottom = Math.max(r1, r2);
		TileGroup g = groupOf(tile);
		if (g != null) {
			// Uniquement des groupes entiers qui tiennent dans le rectangle
			for (int ax = left; ax + g.width - 1 <= right; ax += g.width) {
				for (int ay = top; ay + g.height - 1 <= bottom; ay += g.height) {
					placeGroup(g, ax, ay);
				}
			}
			return;
		}
		for (int c = left; c <= right; c++) {
			for (int r = top; r <= bottom; r++) {
				paintTile(c, r, tile);
			}
		}
	}

	/** Pot de peinture : remplace la zone contiguë de même tile. */
	public void floodFill(int col, int row, int tile) {
		if (!inBounds(col, row)) return;
		int target = tiles[col][row];
		if (target == tile) return;
		// Zone à remplir
		java.util.Set<Point> region = new java.util.HashSet<>();
		Deque<Point> stack = new ArrayDeque<>();
		stack.push(new Point(col, row));
		while (!stack.isEmpty()) {
			Point p = stack.pop();
			if (!inBounds(p.x, p.y) || tiles[p.x][p.y] != target || !region.add(p)) continue;
			stack.push(new Point(p.x + 1, p.y));
			stack.push(new Point(p.x - 1, p.y));
			stack.push(new Point(p.x, p.y + 1));
			stack.push(new Point(p.x, p.y - 1));
		}
		TileGroup g = groupOf(tile);
		if (g != null) {
			// Des groupes entiers, alignés sur la case cliquée, là où ils tiennent dans la zone
			int[] o = g.offsetOf(tile);
			int ox = col - o[0], oy = row - o[1];
			for (Point p : region) {
				int ax = ox + Math.floorDiv(p.x - ox, g.width) * g.width;
				int ay = oy + Math.floorDiv(p.y - oy, g.height) * g.height;
				if (ax != p.x || ay != p.y) continue; // Un seul test par groupe (depuis son coin)
				boolean fits = true;
				for (int dx = 0; dx < g.width && fits; dx++) {
					for (int dy = 0; dy < g.height && fits; dy++) {
						fits = region.contains(new Point(ax + dx, ay + dy));
					}
				}
				if (fits) placeGroup(g, ax, ay);
			}
		} else {
			for (Point p : region) paintTile(p.x, p.y, tile);
		}
		dirty = true;
	}

	/** Pinceau de taille size (carré de size x size cases centré sur la case). Un groupe est toujours posé seul. */
	public void stamp(int col, int row, int tile, int size) {
		if (groupOf(tile) != null) {
			paintTile(col, row, tile);
			return;
		}
		int start = -(size - 1) / 2;
		for (int dc = 0; dc < size; dc++) {
			for (int dr = 0; dr < size; dr++) {
				paintTile(col + start + dc, row + start + dr, tile);
			}
		}
	}

	/** Ligne droite (algorithme de Bresenham) avec un pinceau de taille size. */
	public void line(int c1, int r1, int c2, int r2, int tile, int size) {
		int dc = Math.abs(c2 - c1), dr = -Math.abs(r2 - r1);
		int sc = c1 < c2 ? 1 : -1, sr = r1 < r2 ? 1 : -1;
		int err = dc + dr;
		while (true) {
			stamp(c1, r1, tile, size);
			if (c1 == c2 && r1 == r2) break;
			int e2 = 2 * err;
			if (e2 >= dr) { err += dr; c1 += sc; }
			if (e2 <= dc) { err += dc; r1 += sr; }
		}
	}

	/** Contour d'un rectangle (épaisseur size ; un groupe = une rangée de groupes entiers). */
	public void rectOutline(int c1, int r1, int c2, int r2, int tile, int size) {
		int left = Math.min(c1, c2), right = Math.max(c1, c2), top = Math.min(r1, r2), bottom = Math.max(r1, r2);
		TileGroup g = groupOf(tile);
		if (g != null) {
			int lastX = left + ((right - left + 1) / g.width - 1) * g.width;
			int lastY = top + ((bottom - top + 1) / g.height - 1) * g.height;
			for (int ax = left; ax <= lastX; ax += g.width) {
				for (int ay = top; ay <= lastY; ay += g.height) {
					if (ax == left || ax == lastX || ay == top || ay == lastY) placeGroup(g, ax, ay);
				}
			}
			return;
		}
		for (int c = left; c <= right; c++) {
			for (int r = top; r <= bottom; r++) {
				if (c < left + size || c > right - size || r < top + size || r > bottom - size) paintTile(c, r, tile);
			}
		}
	}

	/** Remplace toutes les tiles "from" de la map par "to". Renvoie le nombre de cases modifiées. */
	public int replaceAll(int from, int to) {
		int count = 0;
		for (int c = 0; c < COLS; c++) {
			for (int r = 0; r < ROWS; r++) {
				if (tiles[c][r] == from && from != to) {
					tiles[c][r] = to;
					count++;
				}
			}
		}
		if (count > 0) dirty = true;
		return count;
	}

	// ===================== Copier / coller =====================

	/** Morceau de map copié (tiles + éléments, en coordonnées relatives). */
	public static class Clip {
		public final int width, height;
		public final int[][] tiles;
		public final List<Placement> placements = new ArrayList<>();

		Clip(int width, int height) {
			this.width = width;
			this.height = height;
			tiles = new int[width][height];
		}
	}

	public Clip copy(Rectangle area) {
		Clip clip = new Clip(area.width, area.height);
		for (int c = 0; c < area.width; c++) {
			for (int r = 0; r < area.height; r++) {
				clip.tiles[c][r] = getTile(area.x + c, area.y + r);
			}
		}
		for (Placement p : data.placements) {
			if (area.contains(p.col, p.row)) {
				Placement copy = p.copy();
				copy.col -= area.x;
				copy.row -= area.y;
				clip.placements.add(copy);
			}
		}
		return clip;
	}

	/** Efface une zone : tiles remplacées par "tile" et éléments supprimés. */
	public void clear(Rectangle area, int tile) {
		fillRect(area.x, area.y, area.x + area.width - 1, area.y + area.height - 1, tile);
		if (data.placements.removeIf(p -> area.contains(p.col, p.row))) dirty = true;
	}

	public void paste(Clip clip, int col, int row) {
		for (int c = 0; c < clip.width; c++) {
			for (int r = 0; r < clip.height; r++) {
				setTile(col + c, row + r, clip.tiles[c][r]);
			}
		}
		for (Placement p : clip.placements) {
			Placement copy = p.copy();
			copy.col += col;
			copy.row += row;
			place(copy);
		}
	}

	public void toggleCollision(int tile) {
		if (!collisions.remove(tile)) collisions.add(tile);
		collisionsDirty = true;
		dirty = true;
	}

	// ===================== Éléments (objets, monstres, événements...) =====================

	public List<Placement> placementsAt(int col, int row) {
		List<Placement> list = new ArrayList<>();
		for (Placement p : data.placements) {
			if (p.col == col && p.row == row) list.add(p);
		}
		return list;
	}

	/**
	 * Pose un élément. Sur une même case on garde au plus un élément par catégorie
	 * (ex : un coffre et un événement peuvent cohabiter, pas deux monstres).
	 */
	public void place(Placement placement) {
		if (!inBounds(placement.col, placement.row)) return;
		data.placements.removeIf(p -> p.col == placement.col && p.row == placement.row && p.category.equals(placement.category));
		data.placements.add(placement);
		dirty = true;
	}

	public void remove(Placement placement) {
		if (data.placements.remove(placement)) dirty = true;
	}

	public void setSpawn(int col, int row) {
		data.spawnCol = col;
		data.spawnRow = row;
		dirty = true;
	}

	public void setMapName(String name) {
		if (!name.equals(data.name)) {
			data.name = name;
			dirty = true;
		}
	}

	public void markDirty() {
		dirty = true;
	}

	public MapData getData() { return data; }
	public int getMapNumber() { return mapNumber; }
	public boolean isDirty() { return dirty; }
	public boolean canUndo() { return !undoStack.isEmpty(); }
	public boolean canRedo() { return !redoStack.isEmpty(); }
}
