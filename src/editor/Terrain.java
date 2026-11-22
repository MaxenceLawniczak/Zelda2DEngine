package editor;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import main.UtilityTool;

/**
 * Terrain "auto-tile" (eau, chemin...) : on peint la zone, et les bords autour sont
 * recalculés automatiquement selon les 4 quarts de chaque case touchés par la zone.
 * Défini dans res/tiles/terrains.txt.
 */
public class Terrain {

	public static final String FILE = "/tiles/terrains.txt";

	public String name;
	public final Set<Integer> zone = new HashSet<>();       // Tiles qui font partie de la zone
	public int base;                                        // Tile peinte
	public int outside;                                     // Tile remise quand un bord disparaît
	public final Set<Integer> editable = new HashSet<>();   // Tiles qu'on peut transformer en bord
	public final int[] edges = new int[16];                 // Masque -> tile de bord (-1 = rien)
	public final Set<Integer> edgeTiles = new HashSet<>();  // Toutes les tiles de bord
	public String next = null;                              // Terrain à recalculer ensuite (ex : eau -> chemin)
	public Terrain nextTerrain = null;

	public Terrain() {
		Arrays.fill(edges, -1);
	}

	@Override
	public String toString() {
		return name;
	}

	/** Peint (ou efface) la zone sur ces cases puis recalcule les bords autour. */
	public void paint(EditorModel m, Collection<Point> cells, boolean erase) {
		if (cells.isEmpty()) return;
		Rectangle box = null;
		for (Point p : cells) {
			if (!m.inBounds(p.x, p.y)) continue;
			if (erase) {
				if (zone.contains(m.getTile(p.x, p.y))) m.setTile(p.x, p.y, outside);
			} else {
				m.setTile(p.x, p.y, m.variant(base));
			}
			box = box == null ? new Rectangle(p.x, p.y, 1, 1) : box.union(new Rectangle(p.x, p.y, 1, 1));
		}
		if (box != null) refreshChain(m, box);
	}

	private void refreshChain(EditorModel m, Rectangle box) {
		Rectangle area = new Rectangle(box.x - 2, box.y - 2, box.width + 4, box.height + 4);
		refresh(m, area);
		refresh(m, area); // 2e passe : les cases devenues "zone" changent les masques voisins
		if (nextTerrain != null && nextTerrain != this) {
			Rectangle bigger = new Rectangle(area.x - 1, area.y - 1, area.width + 2, area.height + 2);
			nextTerrain.refresh(m, bigger);
			nextTerrain.refresh(m, bigger);
		}
	}

	/** Recalcule les bords dans une zone rectangulaire. */
	public void refresh(EditorModel m, Rectangle area) {
		for (int c = area.x; c < area.x + area.width; c++) {
			for (int r = area.y; r < area.y + area.height; r++) {
				if (!m.inBounds(c, r)) continue;
				int tile = m.getTile(c, r);
				if (zone.contains(tile)) continue;
				int mask = mask(m, c, r);
				int edge = mask == 0 ? -1 : edges[mask];
				if (edge < 0) {
					// Plus aucun bord ici : on remet la tile "dehors" si c'était un de nos bords
					if (edgeTiles.contains(tile)) m.setTile(c, r, outside);
				} else if (editable.contains(tile) || edgeTiles.contains(tile)) {
					m.setTile(c, r, edge);
				}
			}
		}
	}

	private boolean in(EditorModel m, int c, int r) {
		// Le bord de la map compte comme "hors zone"
		return m.inBounds(c, r) && zone.contains(m.getTile(c, r));
	}

	private int mask(EditorModel m, int c, int r) {
		boolean n = in(m, c, r - 1), s = in(m, c, r + 1), w = in(m, c - 1, r), e = in(m, c + 1, r);
		boolean nw = in(m, c - 1, r - 1), ne = in(m, c + 1, r - 1), sw = in(m, c - 1, r + 1), se = in(m, c + 1, r + 1);
		int mask = 0;
		if (n || w || nw) mask |= 8;
		if (n || e || ne) mask |= 4;
		if (s || w || sw) mask |= 2;
		if (s || e || se) mask |= 1;
		return mask;
	}

	// ===================== Fichier =====================

	public static List<Terrain> load() {
		List<Terrain> list = new ArrayList<>();
		File file = new File(UtilityTool.RES_DIR, FILE);
		if (!file.isFile()) return list;
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#")) continue;
				try {
					list.add(parse(line));
				} catch (RuntimeException e) {
					System.err.println("Terrain invalide : " + line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (Terrain t : list) {
			for (Terrain other : list) {
				if (other.name.equals(t.next)) t.nextTerrain = other;
			}
		}
		return list;
	}

	private static Terrain parse(String line) {
		String[] parts = line.split("\\|");
		Terrain t = new Terrain();
		t.name = parts[0].trim();
		for (int i = 1; i < parts.length; i++) {
			String field = parts[i].trim();
			int space = field.indexOf(' ');
			String key = space < 0 ? field : field.substring(0, space);
			String value = space < 0 ? "" : field.substring(space + 1).trim();
			switch (key) {
			case "zone": for (String v : value.split("\\s+")) t.zone.add(Integer.parseInt(v)); break;
			case "base": t.base = Integer.parseInt(value); break;
			case "dehors": t.outside = Integer.parseInt(value); break;
			case "modifiables": for (String v : value.split("\\s+")) t.editable.add(Integer.parseInt(v)); break;
			case "bords":
				for (String pair : value.split("\\s+")) {
					String[] kv = pair.split(":");
					int mask = Integer.parseInt(kv[0], 2);
					int tile = Integer.parseInt(kv[1]);
					t.edges[mask] = tile;
					if (!t.zone.contains(tile) && tile != t.outside) t.edgeTiles.add(tile);
				}
				break;
			case "ensuite": t.next = value; break;
			default: System.err.println("Champ de terrain inconnu : " + key);
			}
		}
		return t;
	}
}
