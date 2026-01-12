package tile;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;

import main.GamePanel;
import main.UtilityTool;

public class TileManager {
	
	private GamePanel gp;
	private Tile[] tiles;
	private int mapTileNum[][][];
	private int[][] generatedTiles;  // Stocke les tiles générées
	private Random rand = new Random();
	private ArrayList<Integer> collidableTiles = new ArrayList<Integer>();
	
	
	
	public TileManager(GamePanel gp) {
		this.gp = gp;
		tiles = new Tile[214];
		mapTileNum = new int[gp.getMaxMap()][gp.getMaxWorldCols()][gp.getMaxWorldRows()];
		generatedTiles = new int[gp.getMaxWorldCols()][gp.getMaxWorldRows()];
		

		initCollidableTiles();
		getTileImage();
		loadMap("/maps/map01.txt", 0);
		loadMap("/maps/map02.txt", 1);
		initGeneratedTiles();
	}
	
	public void getTileImage() {
		boolean contiens = false;
	    for(int i = 0 ; i < tiles.length ; i++) {
	    	contiens = collidableTiles.contains(i);
	        tiles[i] = new Tile();
	        if(i < 10)
	            setup(i,"tile000"+i,contiens);
	        else if(i < 100)
	            setup(i,"tile00"+i,contiens);
	        else if(i < 1000)
	        	setup(i,"tile0"+i, contiens);
	        else
	        	setup(i,"tile0"+i, contiens);
	    }
	}

	public void setup(int index, String imgPath, boolean collision) {
		
		UtilityTool uTool = new UtilityTool();
		
		try {
			tiles[index] = new Tile();
			tiles[index].setImage(ImageIO.read(getClass().getResourceAsStream("/tiles/" + imgPath +".png")));
			tiles[index].setImage(uTool.scaleImage(tiles[index].getImage(), gp.getTileSize(), gp.getTileSize()));
			tiles[index].setCollision(collision);
			
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void initCollidableTiles() {
		int[] collidables = {11, 12, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 
							 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 
							 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 
							 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 81, 82, 83, 
							 84, 102, 103, 104, 105, 106, 111, 116, 117, 118, 127, 128, 129, 
							 133, 134, 135, 136, 147, 148, 149, 150, 151, 152, 153, 154, 155, 
							 156, 157, 167, 168, 169, 170, 171, 172, 173, 175, 176, 179, 180,
							 181, 182, 183, 185, 186, 187, 188, 189, 190, 191, 192, 193, 194,
							 196, 197, 198, 199, 200};

	    for (int tile : collidables) {
	        collidableTiles.add(tile);
	    }
	}
	
	public void loadMap(String fileName, int map) {
		try {
			InputStream is = getClass().getResourceAsStream(fileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			
			int col = 0, row = 0;
			
			while(col < gp.getMaxWorldCols() && row < gp.getMaxWorldRows()) {
				String line = br.readLine();
				String[] numbers = line.split(" ");
				
				while (col < gp.getMaxWorldCols()) {
					int num = Integer.parseInt(numbers[col]);
					mapTileNum[map][col][row] = num;
					col++;
				}
				if(col == gp.getMaxWorldCols()) {
					col = 0;
					row++;
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	    for (int row = 0; row < gp.getMaxWorldRows(); row++) {
	        for (int col = 0; col < gp.getMaxWorldCols(); col++) {

	            int tileNum = mapTileNum[currentMap][col][row];

	            int worldX = col * gp.getTileSize();
	            int worldY = row * gp.getTileSize();
	            int screenX = worldX - gp.getPlayer().getWorldX() + gp.getPlayer().getScreenX();
	            int screenY = worldY - gp.getPlayer().getWorldY() + gp.getPlayer().getScreenY();

	            if (worldX > gp.getPlayer().getWorldX() - gp.getPlayer().getScreenX() - gp.getTileSize() &&
	                worldX < gp.getPlayer().getWorldX() + gp.getPlayer().getScreenX() + gp.getTileSize() &&
	                worldY > gp.getPlayer().getWorldY() - gp.getPlayer().getScreenY() - gp.getTileSize() &&
	                worldY < gp.getPlayer().getWorldY() + gp.getPlayer().getScreenY() + gp.getTileSize()) {

	                g2.drawImage(tiles[tileNum].getImage(), screenX, screenY, null);
	            }
	        }
	    }
	}

	public int getMapTileNum(int col, int row) {
	    if (col < 0 || col >= gp.getMaxWorldCols() || row < 0 || row >= gp.getMaxWorldRows()) {
	        return 0;
	    }
	    int tileNum = mapTileNum[gp.getCurrentMap()][col][row];
	    return tileNum;
	}

	
	public Tile getTile(int tileNum) {
		return tiles[tileNum];
	}
}
