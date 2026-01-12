package main;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

import javax.swing.JPanel;

import entity.Entity;
import entity.Player;
import tile.TileManager;
import tile_interactive.IT_Bush;
import tile_interactive.InteractiveTile;

public class GamePanel extends JPanel implements Runnable{
	
	// Paramètres écran
	private final int originalTileSize = 16; //Taille en 16 x 16
	private final int scale = 2;
	
	private final int tileSize = originalTileSize * scale; //Mise à l'echelle en 32 x 32
	private final int maxScreenCols = 20;
	private final int maxScreenRows = 16;
	private final int screenWidth = maxScreenCols * tileSize; // 640 pixels
	private final int screenHeight = maxScreenRows * tileSize; // 448 pixels

	//Paramètres du monde
	private final int maxWorldCols = 50;
	private final int maxWorldRows = 50;
	private final int worldWidth = tileSize * maxWorldCols;
	private final int worldHeight = tileSize * maxWorldRows;
	private final int maxMap = 10;
	private int currentMap = 0;
	
	//FPS
	private int FPS = 60;
	
	//Système
	private TileManager tileM = new TileManager(this);
	private KeyHandler kH = new KeyHandler(this);
	private Thread gameThread;
	private CollisionChecker cChecker = new CollisionChecker(this);
	private AssetSetter aSetter = new AssetSetter(this);
	public UI ui = new UI(this);
	private EventHandler eHandler = new EventHandler(this);
	
	//Entity and Objects
	private Player player = new Player(this, kH);
	private Entity obj[][] = new Entity[maxMap][20];
	private Entity npc[][] = new Entity[maxMap][10];
	private Entity mon[][] = new Entity[maxMap][20];
	private InteractiveTile iTile[][] = new InteractiveTile[maxMap][1000];
	private ArrayList<Entity> projectileList = new ArrayList<>();
	private ArrayList<Entity> entityList = new ArrayList<>();
	
	//Game State
	private int gameState;
	private final int titleState = 0;
	private final int playState = 1;
	private final int pauseState = 2;
	private final int dialogueState = 3;
	private final int characterState = 4;
	private final int optionsState = 5;
	private final int gameOverState = 6;

	public GamePanel() {
		
		this.setPreferredSize(new Dimension(screenWidth, screenHeight));
		//Color c = new Color(58,190,65);
		this.setBackground(Color.BLACK);
		//this.setBackground(c);
		this.setDoubleBuffered(true);
		this.addKeyListener(kH);
		this.setFocusable(true);
	}
	
	public void setupGame() {
		
		aSetter.setObject();
		aSetter.setNPC();
		aSetter.setMonster();
		aSetter.setInteractiveTiles();
		gameState = titleState;
	}
	
	public void retry() {
		player.setDefaultPositions();
		player.restoreLife();
		aSetter.setNPC();
		aSetter.setMonster();
	}
	
	public void restart() {
		player.setDefaultValues();
		player.setDefaultPositions();
		player.restoreLife();
		player.setItems();
		aSetter.setObject();
		aSetter.setNPC();
		aSetter.setMonster();
		aSetter.setInteractiveTiles();
	}
	

	public void startGameThread() {
		
		gameThread = new Thread(this);
		gameThread.start();
	}
	
	public void update() {

	    if(gameState == playState) {
	        // Player
	        player.update();

	        // NPC
	        if(npc[currentMap] != null) {
		        for (int i = 0; i < npc[currentMap].length; i++) {
		            if(npc[currentMap][i] != null)
		                npc[currentMap][i].update();
		        }
	        }

	        // Monster
	        if (mon[currentMap] != null) {
	            for (int i = 0; i < mon[currentMap].length; i++) {
	                if (mon[currentMap][i] != null) {
	                    if (mon[currentMap][i].isAlive() && !mon[currentMap][i].isDying()) {
	                        mon[currentMap][i].update();
	                    } else if (mon[currentMap][i].isDying()) {
	                        mon[currentMap][i].update();
	                    } else {
	                        mon[currentMap][i].checkDrop();
	                        mon[currentMap][i] = null;
	                    }
	                }
	            }
	        }

	        // Projectiles
	        for (int i = 0; i < projectileList.size(); i++) {
	            if (projectileList.get(i) != null) {
	                if (projectileList.get(i).isAlive())
	                    projectileList.get(i).update();

	                if (!projectileList.get(i).isAlive())
	                    projectileList.remove(i);
	            }
	        }
	        if(iTile[currentMap] != null) {
		        for(int i = 0 ; i < iTile[currentMap].length ; i ++) {
		        	if(iTile[currentMap][i] != null) {
		        		iTile[currentMap][i].update();
		        	}
		        }
	        }
	    }

	    if(gameState == pauseState) {
	        // Code du jeu en pause
	    }
	    
	    eHandler.checkEvent();
	}

	public void removeMonster(Entity monstre) {
	    // Parcours du tableau des monstres et suppression du monstre
	    for (int i = 0; i < mon[currentMap].length; i++) {
	        if (mon[currentMap][i] != null && mon[currentMap][i] == monstre) {
	            // Vérification et gestion du drop avant suppression
	            mon[currentMap][i].checkDrop();  // Vérifie si le monstre doit faire un drop
	            
	            // Retirer le monstre du tableau
	            mon[i] = null;  // Retirer le monstre du tableau après gestion du drop
	            break;
	        }
	    }
	}

	@Override
	public void paintComponent(Graphics g) {
	    super.paintComponent(g);
	    Graphics2D g2 = (Graphics2D) g;

	    // Debug
	    long drawStart = 0;
	    if (kH.isDebug()) {
	        drawStart = System.nanoTime();
	    }

	    // Écran titre
	    if (gameState == titleState) {
	        ui.draw(g2);
	    } else {
	        // Tiles de fond
	        tileM.draw(g2,currentMap);

	        // Isometric Tiles
	        if (iTile[currentMap] != null) {
	            for (int i = 0; i < iTile[currentMap].length; i++) {
	                if (iTile[currentMap][i] != null) {
	                    iTile[currentMap][i].draw(g2);
	                }
	            }
	        }

	        // Liste des entités à dessiner
	        entityList.clear(); // Toujours commencer par clear

	        // Joueur
	        entityList.add(player);

	        // Objets
	        if (obj[currentMap] != null) {
	            for (int i = 0; i < obj[currentMap].length; i++) {
	                if (obj[currentMap][i] != null) {
	                    entityList.add(obj[currentMap][i]);
	                }
	            }
	        }

	        // Monstres
	        if (mon[currentMap] != null) {
	            for (int i = 0; i < mon[currentMap].length; i++) {
	                if (mon[currentMap][i] != null) {
	                    entityList.add(mon[currentMap][i]);
	                }
	            }
	        }

	        // Projectiles
	        for (Entity proj : projectileList) {
	            if (proj != null) {
	                entityList.add(proj);
	            }
	        }

	        // Nettoyer les nulls si jamais
	        entityList.removeIf(Objects::isNull);

	        // Trier selon la position verticale
	        entityList.sort(Comparator.comparingInt(Entity::getWorldY));

	        // Dessiner les entités
	        for (Entity e : entityList) {
	            e.draw(g2);
	        }

	        // UI
	        ui.draw(g2);
	    }

	    // Debug
	    if (kH.isDebug()) {
	        long drawEnd = System.nanoTime();
	        long passed = drawEnd - drawStart;
	        System.out.println("Draw Time : " + passed + "ns");
	    }

	    g2.dispose();
	}

	@Override
	public void run() {
		
		double drawInterval = 1000000000/FPS;
		double delta = 0;
		long lastTime = System.nanoTime();
		long currentTime;
		long timer = 0, drawCount = 0;
		
		while(gameThread != null) {
			
			currentTime = System.nanoTime();
			
			delta += (currentTime - lastTime) / drawInterval;
			timer += (currentTime - lastTime);
			lastTime = currentTime;
			
			if(delta >= 1) {
				update();
				repaint();
				delta --;
				drawCount++;
			}
			
			// Compteur de FPS (60)
			if(timer>=1000000000) {
				String text = "FPS : " + String.valueOf(drawCount);
				System.out.println(text);
				drawCount = 0;
				timer = 0;
			}
		}
	}
	
	public int getOriginalTileSize() {
		return originalTileSize;
	}

	public int getScale() {
		return scale;
	}

	public int getTileSize() {
		return tileSize;
	}

	public int getMaxScreenCols() {
		return maxScreenCols;
	}

	public int getMaxScreenRows() {
		return maxScreenRows;
	}

	public int getScreenWidth() {
		return screenWidth;
	}

	public int getScreenHeight() {
		return screenHeight;
	}

	public Player getPlayer() {
		return player;
	}

	public int getMaxWorldCols() {
		return maxWorldCols;
	}

	public int getMaxMap() {
		return maxMap;
	}
	
	public void setCurrentMap(int map) {
		currentMap = map;
	}
	
	public int getCurrentMap() {
		return currentMap;
	}
	
	public int getMaxWorldRows() {
		return maxWorldRows;
	}
	
	public CollisionChecker getCChecker() {
		return cChecker;
	}
	
	public TileManager getTileM() {
		return tileM;
	}
	
	public Entity getObj(int i) {
		return obj[currentMap][i];
	}
	
	public Entity[] getObjTab() {
		return obj[currentMap];
	}
	
	public void setItile(int nextMap, InteractiveTile tile, int i) {
		iTile[nextMap][i] = tile;
	}
	
	public InteractiveTile getItile(int i) {
		return iTile[currentMap][i];
	}
	
	public InteractiveTile[] getItileTab() {
		return iTile[currentMap];
	}
	
	public void setObj(int nextMap, Entity supObj, int i) {
	    //System.out.println("Ajout d'un objet à obj[" + i + "] : " + supObj.getClass().getSimpleName() + " à " + supObj.getWorldX() + ", " + supObj.getWorldY());
	    this.obj[nextMap][i] = supObj;
	}
	
	public void setNPC(int nextMap, Entity npc, int i) {
		this.npc[nextMap][i] = npc;
	}
	
	public Entity getNPC(int i) {
		return npc[currentMap][i];
	}
	
	public Entity[] getNPCTab() {
		return npc[currentMap];
	}
	
	public void setMon(int nextMap, Entity mon, int i) {
		this.mon[nextMap][i] = mon;
	}
	
	public Entity getMon(int i) {
		return mon[currentMap][i];
	}
	
	public Entity[] getMonTab() {
		return mon[currentMap];
	}
	
	public int getPlayState() {
		return playState;
	}
	
	public int getPauseState() {
		return pauseState;
	}
	
	public int getDialogueState() {
		return dialogueState;
	}
	
	public int getTitleState() {
		return titleState;
	}
	
	public int getGameState() {
		return gameState;
	}
	
	public int getCharacterState() {
		return characterState;
	}
	
	public void setGameState(int gameState) {
		this.gameState = gameState;
	}

	public KeyHandler getkH() {
		return kH;
	}
	
	public EventHandler getEHandler() {
		return eHandler;
	}
	
	public ArrayList<Entity> getProjList() {
		return projectileList;
	}

	public int getOptionsState() {
		// TODO Auto-generated method stub
		return optionsState ;
	}

	public int getGameOverState() {
		return gameOverState;
	}
}
