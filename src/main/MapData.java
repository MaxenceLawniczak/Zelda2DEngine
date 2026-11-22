package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Contenu d'une map hors tiles : objets, coffres, monstres, PNJ, tiles interactives,
 * événements et point d'apparition. Lu depuis res/maps/mapXX_data.txt
 * (partagé entre le jeu et l'éditeur de map).
 *
 * Format (une ligne par élément, # = commentaire) :
 *   object  <id> <col> <row> [contenu du coffre]
 *   monster <id> <col> <row>
 *   npc     <id> <col> <row>
 *   tile    <id> <col> <row> [direction]
 *   event   pit      <col> <row> <direction>
 *   event   heal     <col> <row> <direction>
 *   event   teleport <col> <row> <direction> <map cible> <col cible> <row cible>
 *   spawn   <col> <row>
 *   name    <nom du lieu affiché en arrivant>
 * Les numéros de map commencent à 1 (map01.txt = map 1), comme les noms de fichiers.
 */
public class MapData {

	// Taille des maps (utilisée par le jeu et l'éditeur)
	public static final int COLS = 80;
	public static final int ROWS = 80;

	public static final String OBJECT = "object";
	public static final String MONSTER = "monster";
	public static final String NPC = "npc";
	public static final String TILE = "tile";
	public static final String EVENT = "event";
	public static final List<String> CATEGORIES = Arrays.asList(OBJECT, MONSTER, NPC, TILE, EVENT);

	public static class Placement {
		public String category;
		public String id;
		public int col, row;
		public String[] args;

		public Placement(String category, String id, int col, int row, String... args) {
			this.category = category;
			this.id = id;
			this.col = col;
			this.row = row;
			this.args = args == null ? new String[0] : args;
		}

		public String arg(int i, String defaultValue) {
			return i < args.length ? args[i] : defaultValue;
		}

		public int argInt(int i, int defaultValue) {
			try {
				return i < args.length ? Integer.parseInt(args[i]) : defaultValue;
			} catch (NumberFormatException e) {
				return defaultValue;
			}
		}

		public Placement copy() {
			return new Placement(category, id, col, row, args.clone());
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(category + " " + id + " " + col + " " + row);
			for (String a : args) sb.append(' ').append(a);
			return sb.toString();
		}
	}

	public final List<Placement> placements = new ArrayList<>();
	public int spawnCol = -1, spawnRow = -1;
	public String name = "";  // Nom du lieu, affiché en arrivant sur la map

	public static String tilesPath(int mapNumber) {
		return String.format("/maps/map%02d.txt", mapNumber);
	}

	public static String dataPath(int mapNumber) {
		return String.format("/maps/map%02d_data.txt", mapNumber);
	}

	public List<Placement> get(String category) {
		List<Placement> list = new ArrayList<>();
		for (Placement p : placements) {
			if (p.category.equals(category)) list.add(p);
		}
		return list;
	}

	public boolean hasSpawn() {
		return spawnCol >= 0 && spawnRow >= 0;
	}

	public MapData copy() {
		MapData d = new MapData();
		for (Placement p : placements) d.placements.add(p.copy());
		d.spawnCol = spawnCol;
		d.spawnRow = spawnRow;
		d.name = name;
		return d;
	}

	/** Charge les données d'une map (renvoie des données vides si le fichier n'existe pas). */
	public static MapData load(int mapNumber) {
		MapData data = new MapData();
		InputStream is = UtilityTool.openResource(dataPath(mapNumber));
		if (is == null) {
			return data;
		}
		try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
			String line;
			int lineNumber = 0;
			while ((line = br.readLine()) != null) {
				lineNumber++;
				int comment = line.indexOf('#');
				if (comment >= 0) line = line.substring(0, comment);
				line = line.trim();
				if (line.isEmpty()) continue;

				String[] parts = line.split("\\s+");
				try {
					if (parts[0].equals("name")) {
						data.name = line.substring(4).trim();
					} else if (parts[0].equals("spawn")) {
						data.spawnCol = Integer.parseInt(parts[1]);
						data.spawnRow = Integer.parseInt(parts[2]);
					} else if (CATEGORIES.contains(parts[0])) {
						data.placements.add(new Placement(parts[0], parts[1],
								Integer.parseInt(parts[2]), Integer.parseInt(parts[3]),
								Arrays.copyOfRange(parts, 4, parts.length)));
					} else {
						System.err.println(dataPath(mapNumber) + " ligne " + lineNumber + " : type inconnu '" + parts[0] + "'");
					}
				} catch (RuntimeException e) {
					System.err.println(dataPath(mapNumber) + " ligne " + lineNumber + " invalide : " + line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

	public void save(File file) throws IOException {
		try (PrintWriter pw = new PrintWriter(file, StandardCharsets.UTF_8)) {
			pw.println("# Contenu de la map (généré par l'éditeur de map, modifiable à la main)");
			pw.println("# object <id> <col> <row> [contenu du coffre] | monster/npc/tile <id> <col> <row> [args]");
			pw.println("# event pit|heal <col> <row> <dir> | event teleport <col> <row> <dir> <map> <col> <row>");
			if (!name.isEmpty()) {
				pw.println();
				pw.println("name " + name);
			}
			if (hasSpawn()) {
				pw.println();
				pw.println("spawn " + spawnCol + " " + spawnRow);
			}
			for (String category : CATEGORIES) {
				List<Placement> list = get(category);
				if (list.isEmpty()) continue;
				pw.println();
				for (Placement p : list) pw.println(p);
			}
		}
	}
}
