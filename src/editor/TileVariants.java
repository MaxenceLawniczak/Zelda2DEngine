package editor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import main.UtilityTool;

/**
 * Variantes aléatoires : peindre de l'herbe (0) pose parfois une touffe (1),
 * peindre du chemin (79) pose parfois la variante 80, etc.
 * Défini dans res/tiles/variants.txt.
 */
public class TileVariants {

	public static final String FILE = "/tiles/variants.txt";

	private final Map<Integer, int[]> tiles = new HashMap<>();   // tile -> variantes
	private final Map<Integer, int[]> weights = new HashMap<>(); // tile -> poids
	private final Random rand = new Random();

	public TileVariants() {
		File file = new File(UtilityTool.RES_DIR, FILE);
		if (!file.isFile()) return;
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) continue;
				try {
					String[] parts = line.split("=");
					int tile = Integer.parseInt(parts[0].trim());
					String[] entries = parts[1].trim().split("\\s+");
					int[] ids = new int[entries.length], w = new int[entries.length];
					for (int i = 0; i < entries.length; i++) {
						String[] kv = entries[i].split("\\*");
						ids[i] = Integer.parseInt(kv[0]);
						w[i] = kv.length > 1 ? Integer.parseInt(kv[1]) : 1;
					}
					tiles.put(tile, ids);
					weights.put(tile, w);
				} catch (RuntimeException e) {
					System.err.println("Variante invalide : " + line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** Tile à poser quand on peint "tile" (elle-même si elle n'a pas de variantes). */
	public int pick(int tile) {
		int[] ids = tiles.get(tile);
		if (ids == null) return tile;
		int[] w = weights.get(tile);
		int total = 0;
		for (int x : w) total += x;
		int roll = rand.nextInt(Math.max(1, total));
		for (int i = 0; i < ids.length; i++) {
			roll -= w[i];
			if (roll < 0) return ids[i];
		}
		return ids[ids.length - 1];
	}

	public boolean hasVariants(int tile) {
		return tiles.containsKey(tile);
	}
}
