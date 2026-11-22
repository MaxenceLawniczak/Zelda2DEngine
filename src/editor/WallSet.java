package editor;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import main.UtilityTool;

/**
 * Jeu de murs pour les outils "Salle" (rectangle) et "Tracé" (forme libre) :
 *  - 8 pièces (coins + côtés), 4 coins intérieurs facultatifs (formes en L, en escalier...),
 *  - une tile "sous le bord du haut" facultative (face d'une falaise),
 *  - une tile de sol,
 * ou un motif répété (ex : arbres 2x2) pour les bordures épaisses.
 * Stockés dans res/tiles/wallsets.txt.
 */
public class WallSet {

	public static final String FILE = "/tiles/wallsets.txt";
	// Ordre des 8 pièces
	public static final int TL = 0, T = 1, TR = 2, L = 3, R = 4, BL = 5, B = 6, BR = 7;
	// Ordre des coins intérieurs
	public static final int ITL = 0, ITR = 1, IBL = 2, IBR = 3;
	public static final String[] PIECE_NAMES = { "Coin haut-gauche", "Haut", "Coin haut-droit", "Gauche", "Droite",
			"Coin bas-gauche", "Bas", "Coin bas-droit" };
	public static final String[] INNER_NAMES = { "Coin int. haut-gauche", "Coin int. haut-droit", "Coin int. bas-gauche", "Coin int. bas-droit" };

	public String name = "Nouveau";
	public int[] pieces = { 180, 181, 182, 183, 185, 186, 187, 188 };
	public int[] inner = { -1, -1, -1, -1 }; // -1 = choisi automatiquement parmi les 8 pièces
	public int under = -1;                     // Tile posée sous les bords du haut (-1 = rien)
	public int[] pattern = null;               // Motif (si non null, remplace les pièces)
	public int patternW = 2, patternH = 2;
	public int floor = -1;
	public String cliff = null;                // "pit" (fosse) ou "plateau" : falaise avec les petits coins

	// ===== Falaise : tile selon les 4 quarts de la case touchés par le niveau haut =====
	// Masque : 8 = quart haut-gauche, 4 = haut-droite, 2 = bas-gauche, 1 = bas-droite.
	// Un quart est "touché" si une des 3 cases voisines de ce côté (2 côtés + diagonale) est en haut.
	private static final int[] CLIFF_TILES = new int[16];
	static {
		CLIFF_TILES[0b1100] = 20; // Haut en haut : rebord + face
		CLIFF_TILES[0b0011] = 19; // Haut en bas : rebord seul
		CLIFF_TILES[0b0101] = 49; // Haut à droite
		CLIFF_TILES[0b1010] = 50; // Haut à gauche
		CLIFF_TILES[0b1101] = 26; // Grands coins
		CLIFF_TILES[0b1110] = 27;
		CLIFF_TILES[0b0111] = 22;
		CLIFF_TILES[0b1011] = 23;
		CLIFF_TILES[0b0100] = 63; // Petits coins (le haut ne touche qu'en diagonale)
		CLIFF_TILES[0b1000] = 64;
		CLIFF_TILES[0b0001] = 21;
		CLIFF_TILES[0b0010] = 24;
		CLIFF_TILES[0b1111] = 20; // Cas rares (passage d'une seule case)
		CLIFF_TILES[0b1001] = 21;
		CLIFF_TILES[0b0110] = 24;
	}
	private static final int CLIFF_FACE = 51;           // Face de falaise sous un rebord
	private static final int CLIFF_FACE_END_LEFT = 47;  // Sous un petit coin 63
	private static final int CLIFF_FACE_END_RIGHT = 48; // Sous un petit coin 64

	public boolean isCliff() {
		return cliff != null;
	}

	public boolean isPattern() {
		return pattern != null;
	}

	/** Épaisseur de la bordure (1 pour les pièces, taille du motif sinon). */
	public int thicknessX() { return isPattern() ? patternW : 1; }
	public int thicknessY() { return isPattern() ? patternH : 1; }

	private int innerPiece(int which) {
		if (inner[which] >= 0) return inner[which];
		// Pas de coin intérieur défini : on prend la pièce qui s'en rapproche le plus
		switch (which) {
		case ITL: return pieces[BR];
		case ITR: return pieces[BL];
		case IBL: return pieces[TR];
		default: return pieces[TL];
		}
	}

	// ===================== Rectangle (outil Salle) =====================

	/** Cases modifiées par une salle rectangulaire (bornes incluses). */
	public Map<Point, Integer> rectangle(int c1, int r1, int c2, int r2, boolean fillInside) {
		int left = Math.min(c1, c2), right = Math.max(c1, c2);
		int top = Math.min(r1, r2), bottom = Math.max(r1, r2);
		if (isCliff()) {
			if (right - left < 1 || bottom - top < 1) return new LinkedHashMap<>();
			List<Point> corners = List.of(new Point(left, top), new Point(right, top), new Point(right, bottom), new Point(left, bottom));
			return cliffTrace(pathThrough(corners, true), true, false, fillInside);
		}
		if (isPattern()) {
			// Taille arrondie au motif pour que les motifs (arbres) restent toujours entiers
			right = left + ceilTo(right - left + 1, patternW) - 1;
			bottom = top + ceilTo(bottom - top + 1, patternH) - 1;
		}
		int tx = thicknessX(), ty = thicknessY();
		Map<Point, Integer> cells = new LinkedHashMap<>();
		for (int c = left; c <= right; c++) {
			for (int r = top; r <= bottom; r++) {
				int tile = tileAt(c, r, left, top, right, bottom, tx, ty);
				if (tile >= 0) cells.put(new Point(c, r), tile);
				else if (fillInside && floor >= 0) cells.put(new Point(c, r), floor);
			}
		}
		// Face sous le bord du haut
		if (under >= 0 && !isPattern() && bottom - top >= 2) {
			for (int c = left + 1; c < right; c++) cells.put(new Point(c, top + 1), under);
		}
		return cells;
	}

	private static int ceilTo(int value, int step) {
		return ((value + step - 1) / step) * step;
	}

	public void apply(EditorModel m, int c1, int r1, int c2, int r2, boolean fillInside) {
		for (Map.Entry<Point, Integer> e : rectangle(c1, r1, c2, r2, fillInside).entrySet()) {
			m.setTile(e.getKey().x, e.getKey().y, e.getValue());
		}
	}

	/** Tile de mur à cette case d'un rectangle, ou -1 si c'est l'intérieur. */
	public int tileAt(int c, int r, int left, int top, int right, int bottom, int tx, int ty) {
		boolean inTop = r < top + ty, inBottom = r > bottom - ty;
		boolean inLeft = c < left + tx, inRight = c > right - tx;
		if (!inTop && !inBottom && !inLeft && !inRight) return -1;

		if (isPattern()) {
			int px = Math.floorMod(c - left, patternW);
			int py = Math.floorMod(r - top, patternH);
			return pattern[py * patternW + px];
		}
		if (inTop && inLeft) return pieces[TL];
		if (inTop && inRight) return pieces[TR];
		if (inBottom && inLeft) return pieces[BL];
		if (inBottom && inRight) return pieces[BR];
		if (inTop) return pieces[T];
		if (inBottom) return pieces[B];
		if (inLeft) return pieces[L];
		return pieces[R];
	}

	// ===================== Forme libre (outil Tracé) =====================

	/** Chemin case par case entre les points cliqués (segments horizontaux puis verticaux). */
	public static List<Point> pathThrough(List<Point> points, boolean closed) {
		List<Point> cells = new ArrayList<>();
		List<Point> pts = new ArrayList<>(points);
		if (closed && !pts.isEmpty()) pts.add(pts.get(0));
		for (int i = 0; i < pts.size(); i++) {
			Point target = pts.get(i);
			if (cells.isEmpty()) {
				cells.add(new Point(target));
				continue;
			}
			Point cur = new Point(cells.get(cells.size() - 1));
			while (cur.x != target.x) {
				cur.x += Integer.signum(target.x - cur.x);
				addStep(cells, cur);
			}
			while (cur.y != target.y) {
				cur.y += Integer.signum(target.y - cur.y);
				addStep(cells, cur);
			}
		}
		if (closed && cells.size() > 1 && cells.get(0).equals(cells.get(cells.size() - 1))) {
			cells.remove(cells.size() - 1);
		}
		return cells;
	}

	private static void addStep(List<Point> cells, Point p) {
		// Si on revient sur ses pas, on efface la case précédente au lieu de faire un aller-retour
		if (cells.size() >= 2 && cells.get(cells.size() - 2).equals(p)) {
			cells.remove(cells.size() - 1);
		} else {
			cells.add(new Point(p));
		}
	}

	/**
	 * Cases modifiées par un tracé libre. Les pièces sont choisies selon la direction :
	 * l'intérieur est à droite du sens du tracé (les formes fermées sont remises dans
	 * le bon sens automatiquement). flip = intérieur à gauche (tracés ouverts).
	 */
	public Map<Point, Integer> trace(List<Point> points, boolean closed, boolean flip, boolean fillInside) {
		Map<Point, Integer> cells = new LinkedHashMap<>();
		List<Point> path = pathThrough(points, closed);
		if (path.isEmpty()) return cells;
		if (isCliff()) return cliffTrace(path, closed, flip, fillInside);

		if (isPattern()) {
			// Motif (arbres) : on pose des motifs entiers le long du tracé, alignés sur le premier point
			Point origin = points.get(0);
			Set<Point> anchors = new HashSet<>();
			for (Point p : path) {
				anchors.add(new Point(origin.x + Math.floorDiv(p.x - origin.x, patternW) * patternW,
						origin.y + Math.floorDiv(p.y - origin.y, patternH) * patternH));
			}
			for (Point a : anchors) {
				for (int dx = 0; dx < patternW; dx++) {
					for (int dy = 0; dy < patternH; dy++) {
						cells.put(new Point(a.x + dx, a.y + dy), pattern[dy * patternW + dx]);
					}
				}
			}
			return cells;
		}

		if (closed && path.size() >= 4) {
			long area = 0;
			for (int i = 0; i < path.size(); i++) {
				Point a = path.get(i), b = path.get((i + 1) % path.size());
				area += (long) a.x * b.y - (long) b.x * a.y;
			}
			if (area < 0) java.util.Collections.reverse(path); // Sens horaire : intérieur à droite
		} else if (flip) {
			java.util.Collections.reverse(path);
		}

		int n = path.size();
		Set<Point> onPath = new HashSet<>(path);
		List<Point> tops = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			Point p = path.get(i);
			Point prev = (i > 0) ? path.get(i - 1) : (closed && n > 2 ? path.get(n - 1) : null);
			Point next = (i < n - 1) ? path.get(i + 1) : (closed && n > 2 ? path.get(0) : null);
			char in = prev != null ? dir(prev, p) : 0;
			char out = next != null ? dir(p, next) : 0;
			if (in == 0) in = out;
			if (out == 0) out = in;
			int tile;
			if (in == out) {
				switch (in) {
				case 'E': tile = pieces[T]; tops.add(p); break;
				case 'W': tile = pieces[B]; break;
				case 'S': tile = pieces[R]; break;
				default: tile = pieces[L]; break;
				}
			} else {
				String turn = "" + in + out;
				switch (turn) {
				// Virages à droite : coins extérieurs
				case "ES": tile = pieces[TR]; break;
				case "SW": tile = pieces[BR]; break;
				case "WN": tile = pieces[BL]; break;
				case "NE": tile = pieces[TL]; break;
				// Virages à gauche : coins intérieurs
				case "EN": tile = innerPiece(ITL); break;
				case "NW": tile = innerPiece(IBL); break;
				case "WS": tile = innerPiece(IBR); break;
				case "SE": tile = innerPiece(ITR); break;
				default: tile = pieces[T]; break; // Demi-tour (ne devrait pas arriver)
				}
			}
			cells.put(p, tile);
		}

		// Intérieur d'une forme fermée
		if (closed && fillInside && floor >= 0) {
			for (Point p : interior(path, onPath)) cells.put(p, floor);
		}
		// Face sous les bords du haut
		if (under >= 0) {
			for (Point p : tops) {
				Point below = new Point(p.x, p.y + 1);
				if (!onPath.contains(below)) cells.put(below, under);
			}
		}
		return cells;
	}

	/**
	 * Falaise : le tracé suit les cases du niveau bas qui touchent le niveau haut.
	 *  - fosse (pit) : l'intérieur est en bas, l'extérieur en haut -> on ne voit que le mur du fond ;
	 *  - plateau : l'intérieur est en haut -> les murs sont visibles à l'avant.
	 * Tracé ouvert : le niveau haut est à gauche du sens du tracé (à droite avec flip).
	 */
	private Map<Point, Integer> cliffTrace(List<Point> path, boolean closed, boolean flip, boolean fillInside) {
		Map<Point, Integer> cells = new LinkedHashMap<>();
		Set<Point> onPath = new HashSet<>(path);
		boolean isClosed = closed && path.size() >= 4;
		Set<Point> inside = isClosed ? interior(path, onPath) : new HashSet<>();
		Set<Point> highOpen = new HashSet<>();
		if (!isClosed) {
			for (int i = 0; i < path.size(); i++) {
				Point p = path.get(i);
				if (i > 0) addSide(highOpen, p, dir(path.get(i - 1), p), flip, onPath);
				if (i < path.size() - 1) addSide(highOpen, p, dir(p, path.get(i + 1)), flip, onPath);
			}
		}
		boolean pit = "pit".equals(cliff);
		java.util.function.Predicate<Point> high = q -> {
			if (onPath.contains(q)) return false;
			if (!isClosed) return highOpen.contains(q);
			return pit ? !inside.contains(q) : inside.contains(q);
		};

		for (Point p : path) {
			int mask = 0;
			if (high.test(at(p, 0, -1)) || high.test(at(p, -1, 0)) || high.test(at(p, -1, -1))) mask |= 8;
			if (high.test(at(p, 0, -1)) || high.test(at(p, 1, 0)) || high.test(at(p, 1, -1))) mask |= 4;
			if (high.test(at(p, 0, 1)) || high.test(at(p, -1, 0)) || high.test(at(p, -1, 1))) mask |= 2;
			if (high.test(at(p, 0, 1)) || high.test(at(p, 1, 0)) || high.test(at(p, 1, 1))) mask |= 1;
			if (mask == 0) continue;
			int tile = CLIFF_TILES[mask];
			cells.put(p, tile);

			// Face de la falaise sous les rebords du haut
			Point below = at(p, 0, 1);
			if (!onPath.contains(below) && !high.test(below)) {
				if ((mask & 0b1100) == 0b1100) cells.put(below, CLIFF_FACE);
				else if (mask == 0b0100) cells.put(below, CLIFF_FACE_END_LEFT);
				else if (mask == 0b1000) cells.put(below, CLIFF_FACE_END_RIGHT);
			}
		}

		if (isClosed && fillInside && floor >= 0) {
			for (Point q : inside) cells.putIfAbsent(q, floor);
		}
		return cells;
	}

	private static Point at(Point p, int dx, int dy) {
		return new Point(p.x + dx, p.y + dy);
	}

	/** Tracé ouvert de falaise : marque comme "haut" la case à gauche (ou à droite si flip) du sens du tracé. */
	private static void addSide(Set<Point> high, Point p, char d, boolean flip, Set<Point> onPath) {
		int dx = 0, dy = 0;
		switch (d) {
		case 'E': dy = -1; break; // Vers l'est, la gauche c'est le nord
		case 'W': dy = 1; break;
		case 'S': dx = 1; break;
		default: dx = -1; break;  // Vers le nord, la gauche c'est l'ouest
		}
		if (flip) { dx = -dx; dy = -dy; }
		Point q = new Point(p.x + dx, p.y + dy);
		if (!onPath.contains(q)) high.add(q);
	}

	private static char dir(Point a, Point b) {
		if (b.x > a.x) return 'E';
		if (b.x < a.x) return 'W';
		if (b.y > a.y) return 'S';
		return 'N';
	}

	/** Cases à l'intérieur d'un contour fermé (remplissage depuis l'extérieur de la boîte englobante). */
	private static Set<Point> interior(List<Point> path, Set<Point> onPath) {
		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
		for (Point p : path) {
			minX = Math.min(minX, p.x); maxX = Math.max(maxX, p.x);
			minY = Math.min(minY, p.y); maxY = Math.max(maxY, p.y);
		}
		minX--; minY--; maxX++; maxY++;
		Set<Point> outside = new HashSet<>();
		Deque<Point> stack = new ArrayDeque<>();
		stack.push(new Point(minX, minY));
		while (!stack.isEmpty()) {
			Point p = stack.pop();
			if (p.x < minX || p.x > maxX || p.y < minY || p.y > maxY || onPath.contains(p) || !outside.add(p)) continue;
			stack.push(new Point(p.x + 1, p.y));
			stack.push(new Point(p.x - 1, p.y));
			stack.push(new Point(p.x, p.y + 1));
			stack.push(new Point(p.x, p.y - 1));
		}
		Set<Point> inside = new HashSet<>();
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				Point p = new Point(x, y);
				if (!outside.contains(p) && !onPath.contains(p)) inside.add(p);
			}
		}
		return inside;
	}

	// ===================== Aperçu =====================

	/** Petit aperçu : une salle en forme de L (montre aussi les coins intérieurs). */
	public BufferedImage preview(int cell) {
		int n = isPattern() ? Math.max(6, patternW * 3) : 6;
		BufferedImage img = new BufferedImage(n * cell, n * cell, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		for (int c = 0; c < n; c++) {
			for (int r = 0; r < n; r++) {
				g.drawImage(EditorImages.tile(0), c * cell, r * cell, cell, cell, null);
			}
		}
		Map<Point, Integer> cells;
		if (isPattern()) {
			cells = rectangle(0, 0, n - 1, n - 1, true);
		} else {
			List<Point> shape = List.of(new Point(0, 0), new Point(n - 1, 0), new Point(n - 1, n - 1),
					new Point(n / 2, n - 1), new Point(n / 2, n / 2), new Point(0, n / 2));
			cells = trace(shape, true, false, true);
		}
		for (Map.Entry<Point, Integer> e : cells.entrySet()) {
			g.drawImage(EditorImages.tile(e.getValue()), e.getKey().x * cell, e.getKey().y * cell, cell, cell, null);
		}
		g.dispose();
		return img;
	}

	public WallSet copy() {
		WallSet w = new WallSet();
		w.name = name;
		w.pieces = pieces.clone();
		w.inner = inner.clone();
		w.under = under;
		w.pattern = pattern == null ? null : pattern.clone();
		w.patternW = patternW;
		w.patternH = patternH;
		w.floor = floor;
		w.cliff = cliff;
		return w;
	}

	@Override
	public String toString() {
		return name;
	}

	// ===================== Fichier =====================

	public static List<WallSet> load() {
		List<WallSet> list = new ArrayList<>();
		File file = new File(UtilityTool.RES_DIR, FILE);
		if (file.isFile()) {
			try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
				String line;
				while ((line = br.readLine()) != null) {
					line = line.trim();
					if (line.isEmpty() || line.startsWith("#")) continue;
					try {
						list.add(parse(line));
					} catch (RuntimeException e) {
						System.err.println("Jeu de murs invalide : " + line);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (list.isEmpty()) {
			WallSet w = new WallSet();
			w.name = "Donjon (briques vertes)";
			w.floor = 184;
			list.add(w);
		}
		return list;
	}

	private static WallSet parse(String line) {
		String[] parts = line.split("\\|");
		WallSet w = new WallSet();
		w.name = parts[0].trim();
		String[] tiles = parts[1].trim().split("\\s+");
		if (tiles[0].equals("cliff")) {
			w.cliff = tiles.length > 1 ? tiles[1] : "pit";
		} else if (tiles[0].equals("pattern")) {
			w.patternW = Integer.parseInt(tiles[1]);
			w.patternH = Integer.parseInt(tiles[2]);
			w.pattern = new int[w.patternW * w.patternH];
			for (int i = 0; i < w.pattern.length; i++) w.pattern[i] = Integer.parseInt(tiles[3 + i]);
		} else {
			for (int i = 0; i < 8; i++) w.pieces[i] = Integer.parseInt(tiles[i]);
		}
		w.floor = parts.length > 2 ? Integer.parseInt(parts[2].trim()) : -1;
		// Champs facultatifs : "inner a b c d" et "under x"
		for (int i = 3; i < parts.length; i++) {
			String[] f = parts[i].trim().split("\\s+");
			if (f[0].equals("inner")) {
				for (int k = 0; k < 4; k++) w.inner[k] = Integer.parseInt(f[1 + k]);
			} else if (f[0].equals("under")) {
				w.under = Integer.parseInt(f[1]);
			}
		}
		return w;
	}

	public static void save(List<WallSet> sets) throws IOException {
		try (PrintWriter pw = new PrintWriter(new File(UtilityTool.RES_DIR, FILE), StandardCharsets.UTF_8)) {
			pw.println("# Jeux de murs des outils \"Salle\" et \"Tracé\" de l'éditeur de map (modifiables dans l'éditeur).");
			pw.println("# Format : nom | coinHautGauche haut coinHautDroit gauche droite coinBasGauche bas coinBasDroit | sol");
			pw.println("#                [| inner coinIntHG coinIntHD coinIntBG coinIntBD] [| under tileSousLeHaut]");
			pw.println("#      ou : nom | pattern <largeur> <hauteur> <tiles du motif...> | sol");
			pw.println("#      ou : nom | cliff pit|plateau | sol   (falaise : fosse = niveau inférieur, plateau = niveau supérieur)");
			pw.println("# -1 = rien / automatique.");
			for (WallSet w : sets) {
				StringBuilder sb = new StringBuilder(w.name.replace("|", "/")).append(" | ");
				if (w.isCliff()) {
					sb.append("cliff ").append(w.cliff);
				} else if (w.isPattern()) {
					sb.append("pattern ").append(w.patternW).append(' ').append(w.patternH);
					for (int t : w.pattern) sb.append(' ').append(t);
				} else {
					for (int i = 0; i < 8; i++) sb.append(i == 0 ? "" : " ").append(w.pieces[i]);
				}
				sb.append(" | ").append(w.floor);
				if (!w.isPattern() && !w.isCliff()) {
					if (w.inner[0] >= 0 || w.inner[1] >= 0 || w.inner[2] >= 0 || w.inner[3] >= 0) {
						sb.append(" | inner ").append(w.inner[0]).append(' ').append(w.inner[1]).append(' ')
								.append(w.inner[2]).append(' ').append(w.inner[3]);
					}
					if (w.under >= 0) sb.append(" | under ").append(w.under);
				}
				pw.println(sb);
			}
		}
	}
}
