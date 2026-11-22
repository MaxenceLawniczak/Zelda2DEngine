package tile;

import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import main.GamePanel;
import main.MapData;
import main.UtilityTool;

public class TileManager {

	public static final String COLLISIONS_PATH = "/tiles/collisions.txt";

	private GamePanel gp;
	private Tile[] tiles;
	private int mapTileNum[][][];
	private boolean[] mapExists;
	private int[][] generatedTiles;  // Stocke les tiles générées
	private Random rand = new Random();
	private Set<Integer> collidableTiles;

	public TileManager(GamePanel gp) {
		this.gp = gp;
		tiles = new Tile[countTiles()];
		mapTileNum = new int[gp.getMaxMap()][gp.getMaxWorldCols()][gp.getMaxWorldRows()];
		mapExists = new boolean[gp.getMaxMap()];
		generatedTiles = new int[gp.getMaxWorldCols()][gp.getMaxWorldRows()];

		collidableTiles = loadCollisions();
		getTileImage();

		// Charge toutes les maps existantes : map01.txt, map02.txt, ...
		for (int map = 0; map < gp.getMaxMap(); map++) {
			reloadMap(map);
		}
		initGeneratedTiles();
	}

	/** Nombre d'images tileXXXX.png disponibles (tile0000, tile0001... jusqu'à la première manquante). */
	public static int countTiles() {
		int count = 0;
		while (UtilityTool.class.getResource(String.format("/tiles/tile%04d.png", count)) != null) {
			count++;
		}
		return count;
	}

	/** Liste des tiles qui bloquent le joueur (res/tiles/collisions.txt). */
	public static Set<Integer> loadCollisions() {
		Set<Integer> set = new HashSet<>();
		InputStream is = UtilityTool.openResource(COLLISIONS_PATH);
		if (is == null) {
			System.err.println("Fichier introuvable : " + COLLISIONS_PATH + " (aucune tile ne bloquera le joueur)");
			return set;
		}
		try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
			String line;
			while ((line = br.readLine()) != null) {
				int comment = line.indexOf('#');
				if (comment >= 0) line = line.substring(0, comment);
				for (String number : line.trim().split("[\\s,;]+")) {
					if (!number.isEmpty()) set.add(Integer.parseInt(number));
				}
			}
		} catch (Exception e) {
			System.err.println("Erreur de lecture de " + COLLISIONS_PATH);
			e.printStackTrace();
		}
		return set;
	}

	public void getTileImage() {
	    for(int i = 0 ; i < tiles.length ; i++) {
	    	// tile0000, tile0001, ...
	    	setup(i, String.format("tile%04d", i), collidableTiles.contains(i));
	    }
	}

	public void setup(int index, String imgPath, boolean collision) {

		tiles[index] = new Tile();
		tiles[index].setImage(UtilityTool.loadImage("/tiles/" + imgPath, gp.getTileSize(), gp.getTileSize()));
		tiles[index].setCollision(collision);
	}

	/** (Re)lit le fichier d'une map (map 0 = map01.txt). */
	public void reloadMap(int map) {
		String path = MapData.tilesPath(map + 1);
		mapExists[map] = UtilityTool.resourceExists(path);
		if (mapExists[map]) {
			loadMap(path, map);
		}
	}

	public boolean mapExists(int map) {
		return mapExists[map];
	}

	public void loadMap(String fileName, int map) {
		int[][] loaded = readMap(fileName, tiles.length);
		if (loaded != null) {
			mapTileNum[map] = loaded;
		}
	}

	/**
	 * Lit un fichier de map (lignes de numéros de tiles séparés par des espaces).
	 * Utilisé par le jeu et par l'éditeur. Renvoie null si le fichier n'existe pas.
	 */
	public static int[][] readMap(String fileName, int tileCount) {
		InputStream is = UtilityTool.openResource(fileName);
		if (is == null) {
			System.err.println("Map introuvable : " + fileName);
			return null;
		}

		int[][] map = new int[MapData.COLS][MapData.ROWS];
		try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

			for (int row = 0; row < MapData.ROWS; row++) {
				String line = br.readLine();
				if (line == null) break; // Fichier plus court que prévu : le reste reste à 0
				String[] numbers = line.trim().split("\\s+");

				for (int col = 0; col < MapData.COLS && col < numbers.length; col++) {
					if (numbers[col].isEmpty()) continue;
					int num = Integer.parseInt(numbers[col]);
					// Numéro de tile inconnu : on met 0 plutôt que de planter au dessin
					map[col][row] = (num >= 0 && num < tileCount) ? num : 0;
				}
			}
		} catch (Exception e) {
			System.err.println("Erreur de lecture de la map " + fileName);
			e.printStackTrace();
		}
		return map;
	}

	// Initialise les tiles aléatoires une seule fois
	private void initGeneratedTiles() {
		for (int col = 0; col < gp.getMaxWorldCols(); col++) {
			for (int row = 0; row < gp.getMaxWorldRows(); row++) {
				if (mapTileNum[gp.getCurrentMap()][col][row] == 0) {
					int randTile = rand.nextInt(1, 6);
					if (randTile == 1)
						generatedTiles[col][row] = 1;
					else
						generatedTiles[col][row] = 0;
				}else if (mapTileNum[gp.getCurrentMap()][col][row] == 79) {
					int randTile = rand.nextInt(1, 6);
					if (randTile == 1)
						generatedTiles[col][row] = 80;
					else
						generatedTiles[col][row] = 79;
				} else {
					generatedTiles[col][row] = mapTileNum[gp.getCurrentMap()][col][row];
				}
			}
		}
	}

	public void draw(Graphics2D g2, int currentMap) {
		int tileSize = gp.getTileSize();

		// Position du coin haut-gauche de l'écran dans le monde (suit la caméra)
		int viewWorldX = -gp.worldToScreenX(0);
		int viewWorldY = -gp.worldToScreenY(0);

		// On ne parcourt que les cases visibles (au lieu des 50x50 cases à chaque frame)
		int firstCol = Math.max(0, viewWorldX / tileSize - 1);
		int lastCol  = Math.min(gp.getMaxWorldCols() - 1, (viewWorldX + gp.getScreenWidth()) / tileSize + 1);
		int firstRow = Math.max(0, viewWorldY / tileSize - 1);
		int lastRow  = Math.min(gp.getMaxWorldRows() - 1, (viewWorldY + gp.getScreenHeight()) / tileSize + 1);

	    for (int row = firstRow; row <= lastRow; row++) {
	        for (int col = firstCol; col <= lastCol; col++) {

	            int tileNum = mapTileNum[currentMap][col][row];

	            int screenX = col * tileSize - viewWorldX;
	            int screenY = row * tileSize - viewWorldY;

	            g2.drawImage(tiles[tileNum].getImage(), screenX, screenY, null);
	        }
	    }
	}

	public int getMapTileNum(int col, int row) {
	    return getMapTileNum(gp.getCurrentMap(), col, row);
	}

	public int getMapTileNum(int map, int col, int row) {
	    if (col < 0 || col >= gp.getMaxWorldCols() || row < 0 || row >= gp.getMaxWorldRows()) {
	        return 0;
	    }
	    return mapTileNum[map][col][row];
	}

	public Tile getTile(int tileNum) {
		return tiles[tileNum];
	}

	public int getTileCount() {
		return tiles.length;
	}
}
