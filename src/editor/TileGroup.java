package editor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import main.UtilityTool;

/**
 * Groupe de tiles qui doivent toujours rester ensemble (ex : un arbre = 2x2 tiles).
 * Défini dans res/tiles/groups.txt.
 */
public class TileGroup {

	public static final String FILE = "/tiles/groups.txt";

	public final String name;
	public final int width, height;
	public final int[] tiles; // Ligne par ligne

	public TileGroup(String name, int width, int height, int[] tiles) {
		this.name = name;
		this.width = width;
		this.height = height;
		this.tiles = tiles;
	}

	/** Position (x, y) de la tile dans le groupe, ou null si elle n'en fait pas partie. */
	public int[] offsetOf(int tile) {
		for (int i = 0; i < tiles.length; i++) {
			if (tiles[i] == tile) return new int[] { i % width, i / width };
		}
		return null;
	}

	public int tileAt(int dx, int dy) {
		return tiles[dy * width + dx];
	}

	public static List<TileGroup> load() {
		List<TileGroup> list = new ArrayList<>();
		File file = new File(UtilityTool.RES_DIR, FILE);
		if (file.isFile()) {
			try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
				String line;
				while ((line = br.readLine()) != null) {
					line = line.trim();
					if (line.isEmpty() || line.startsWith("#")) continue;
					try {
						String[] parts = line.split("\\|");
						String[] size = parts[1].trim().split("\\s+");
						String[] ids = parts[2].trim().split("\\s+");
						int w = Integer.parseInt(size[0]), h = Integer.parseInt(size[1]);
						int[] tiles = new int[w * h];
						for (int i = 0; i < tiles.length; i++) tiles[i] = Integer.parseInt(ids[i]);
						list.add(new TileGroup(parts[0].trim(), w, h, tiles));
					} catch (RuntimeException e) {
						System.err.println("Groupe de tiles invalide : " + line);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			list.add(new TileGroup("Arbre", 2, 2, new int[] { 15, 16, 17, 18 }));
		}
		return list;
	}
}
