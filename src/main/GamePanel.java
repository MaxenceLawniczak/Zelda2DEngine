package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;

import javax.swing.JPanel;

import entity.Entity;
import entity.Player;
import tile.TileManager;
import tile_interactive.InteractiveTile;

public class GamePanel extends JPanel implements Runnable{

	// Paramètres écran
	private final int originalTileSize = 16; //Taille en 16 x 16
	private final int scale = 3;

	private final int tileSize = originalTileSize * scale; //Mise à l'echelle en 48 x 48
	private final int maxScreenCols = 20;
	private final int maxScreenRows = 16;
	private final int screenWidth = maxScreenCols * tileSize; // 640 pixels
	private final int screenHeight = maxScreenRows * tileSize; // 512 pixels

	// Nombre maximum d'éléments par map
	public static final int MAX_OBJECTS = 120, MAX_NPCS = 20, MAX_MONSTERS = 60, MAX_ITILES = 3000;

	//Paramètres du monde
	private final int maxWorldCols = MapData.COLS;
	private final int maxWorldRows = MapData.ROWS;
	private final int maxMap = 10;
	private int currentMap = 0;

	//FPS
	private final int FPS = 60;
	private int currentFPS = 0;
	private long lastDrawTime = 0;

	//Rendu : on dessine d'abord dans une image en mémoire (depuis le thread du jeu),
	//puis Swing ne fait que la recopier à l'écran -> pas de conflit entre threads.
	private final BufferedImage tempScreen = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB);
	private final Graphics2D g2Screen = tempScreen.createGraphics();
	private final Object gameLock = new Object();

	//Système
	private TileManager tileM = new TileManager(this);
	private KeyHandler kH = new KeyHandler(this);
	private Thread gameThread;
	private CollisionChecker cChecker = new CollisionChecker(this);
	private AssetSetter aSetter = new AssetSetter(this);
	public UI ui = new UI(this);
	private EventHandler eHandler = new EventHandler(this);
	private DebugTools debug = new DebugTools(this);
	private Minimap minimap = new Minimap(this);
	private IntroScreen intro = new IntroScreen(this, ui.getCustomFont());

	// Fondu au noir entre deux écrans (titre -> intro -> jeu)
	private float fadeAlpha = 0f;
	private int fadeDirection = 0; // 1 = on assombrit, -1 = on éclaircit, 0 = pas de fondu
	private Runnable fadeAction = null;

	//Entity and Objects
	private Player player = new Player(this, kH);
	private Entity obj[][] = new Entity[maxMap][MAX_OBJECTS];
	private Entity npc[][] = new Entity[maxMap][MAX_NPCS];
	private Entity mon[][] = new Entity[maxMap][MAX_MONSTERS];
	private InteractiveTile iTile[][] = new InteractiveTile[maxMap][MAX_ITILES];

	// Événements accomplis (énigmes résolues, PNJ écoutés...) : ouvrent les barrières
	private final java.util.Set<String> flags = new java.util.HashSet<>();
	private ArrayList<Entity> projectileList = new ArrayList<>();
	private ArrayList<Entity> entityList = new ArrayList<>();
	private ArrayList<FloatingText> floatingTexts = new ArrayList<>();
	private final java.awt.Font floatingFont = new java.awt.Font("Arial", java.awt.Font.BOLD, 18);

	//Game State
	private int gameState;
	private final int titleState = 0;
	private final int playState = 1;
	private final int pauseState = 2;
	private final int dialogueState = 3;
	private final int characterState = 4;
	private final int optionsState = 5;
	private final int gameOverState = 6;
	private final int introState = 7;

	public GamePanel() {

		this.setPreferredSize(new Dimension(screenWidth, screenHeight));
		this.setBackground(Color.BLACK);
		this.setDoubleBuffered(true);
		this.addKeyListener(kH);
		this.addMouseListener(kH); // Clic gauche = attaquer / valider
		this.setFocusable(true);
		
		// Mode debug : clic sur la grande carte (M) = téléportation
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				synchronized (gameLock) {
					if (!debug.isEnabled() || gameState != playState) return;
					Point tile = minimap.screenToTile(e.getX(), e.getY());
					if (tile != null) {
						teleportPlayer(currentMap, tile.x, tile.y);
						debug.showMessage("Téléporté en " + tile.x + ", " + tile.y);
					}
				}
			}
		});
	}

	public void setupGame() {

		aSetter.setObject();
		aSetter.setNPC();
		aSetter.setMonster();
		aSetter.setInteractiveTiles();
		gameState = titleState;
	}

	/** Lance directement une partie sur une map donnée (utilisé par le bouton "Tester" de l'éditeur). */
	public void startAt(int map, int col, int row) {
		gameState = playState;
		teleportPlayer(map, col, row);
	}
	
	// ===================== Enchaînement titre -> intro -> jeu =====================

	/** "Nouveau" sur l'écran titre : fondu au noir puis intro. */
	public void startNewGame() {
		fadeTo(intro::start);
	}

	/** Fin de l'intro : le joueur se réveille au milieu de la forêt. */
	public void beginAdventure() {
		currentMap = 0;
		projectileList.clear();
		player.setDefaultPositions();
		gameState = playState;
		ui.showLocation(aSetter.getMapData(0).name);
		ui.getDialogueBox().start("???",
				"... Hmm ... Où suis-je ?",
				"Je ne me souviens de rien... Comment suis-je arrivé dans cette forêt ?",
				"Il y a peut-être quelqu'un dans les environs qui pourrait m'aider...");
	}

	/** Assombrit l'écran, exécute l'action une fois tout noir, puis réaffiche. */
	public void fadeTo(Runnable action) {
		fadeAction = action;
		fadeDirection = 1;
	}

	public boolean isFading() {
		return fadeDirection != 0;
	}

	private void updateFade() {
		if (fadeDirection == 0) return;
		fadeAlpha += fadeDirection * 0.04f;
		if (fadeAlpha >= 1f) {
			fadeAlpha = 1f;
			fadeDirection = -1;
			if (fadeAction != null) {
				Runnable action = fadeAction;
				fadeAction = null;
				action.run();
			}
		} else if (fadeAlpha <= 0f) {
			fadeAlpha = 0f;
			fadeDirection = 0;
		}
	}

	/** Change de map et place le joueur sur une case. */
	public void teleportPlayer(int map, int col, int row) {
		if (map != currentMap) ui.showLocation(aSetter.getMapData(map).name);
		currentMap = map;
		player.setWorldX(tileSize * col);
		player.setWorldY(tileSize * row);
		projectileList.clear();
	}
	
	/** Relit les fichiers de la map actuelle (tiles + contenu) : pratique avec l'éditeur ouvert à côté. */
	public void reloadCurrentMap() {
		tileM.reloadMap(currentMap);
		aSetter.reloadMap(currentMap);
		minimap.invalidate(currentMap);
		projectileList.clear();
		debug.showMessage("Map " + (currentMap + 1) + " rechargée");
	}

	public void retry() {
		currentMap = 0;
		projectileList.clear();
		player.setDefaultPositions();
		player.restoreLife();
		aSetter.setNPC();
		aSetter.setMonster();
	}

	public void restart() {
		currentMap = 0;
		ui.getTitleScreen().reset();
		projectileList.clear();
		player.setDefaultValues();
		player.setDefaultPositions();
		player.restoreLife();
		player.setItems();

		// On vide le monde (objets lâchés, tiles détruites...) avant de le recréer
		obj = new Entity[maxMap][MAX_OBJECTS];
		npc = new Entity[maxMap][MAX_NPCS];
		mon = new Entity[maxMap][MAX_MONSTERS];
		iTile = new InteractiveTile[maxMap][MAX_ITILES];
		flags.clear();

		aSetter.setObject();
		aSetter.setNPC();
		aSetter.setMonster();
		aSetter.setInteractiveTiles();
	}


	public void startGameThread() {

		gameThread = new Thread(this, "GameLoop");
		gameThread.start();
	}

	public void update() {
		
		debug.update();
		updateFade();

		if (gameState == introState) {
			intro.update();
		}

	    if(gameState == playState) {
	        // Player
	        player.update();

	        // NPC
	        for (int i = 0; i < npc[currentMap].length; i++) {
	            if(npc[currentMap][i] != null)
	                npc[currentMap][i].update();
	        }

	        // Monster
	        for (int i = 0; i < mon[currentMap].length; i++) {
	        	Entity m = mon[currentMap][i];
	            if (m != null) {
	                if (m.isDying()) {
	                    m.updateDying();
	                } else if (m.isAlive()) {
	                    m.update();
	                } else {
	                    m.checkDrop();
	                    mon[currentMap][i] = null;
	                }
	            }
	        }

	        // Projectiles
	        for (int i = 0; i < projectileList.size(); i++) {
	        	Entity p = projectileList.get(i);
	            if (p != null && p.isAlive())
	                p.update();
	        }
	        projectileList.removeIf(p -> p == null || !p.isAlive());
	        
	        // Textes flottants (dégâts...)
	        floatingTexts.removeIf(t -> !t.update());

	        // Interactive Tiles
	        for(int i = 0 ; i < iTile[currentMap].length ; i ++) {
	        	if(iTile[currentMap][i] != null) {
	        		iTile[currentMap][i].update();
	        		// Barrière ouverte, etc. : la tile disparaît
	        		if (!iTile[currentMap][i].isAlive()) iTile[currentMap][i] = null;
	        	}
	        }
	    }
	}

	/** Dessine toute la frame dans tempScreen (appelé depuis le thread du jeu). */
	public void drawToTempScreen() {
		Graphics2D g2 = g2Screen;

	    // Debug
	    long drawStart = System.nanoTime();

	    g2.setColor(Color.BLACK);
	    g2.fillRect(0, 0, screenWidth, screenHeight);

	    // Écran titre et intro (pas de monde à dessiner)
	    if (gameState == titleState || gameState == introState) {
	        ui.draw(g2);
	    } else {
	        // Tiles de fond
	        tileM.draw(g2,currentMap);

	        // Interactive Tiles
	        for (int i = 0; i < iTile[currentMap].length; i++) {
	            if (iTile[currentMap][i] != null) {
	                iTile[currentMap][i].draw(g2);
	            }
	        }

	        // Liste des entités à dessiner
	        entityList.clear();
	        entityList.add(player);

	        for (Entity o : obj[currentMap]) {
	            if (o != null) entityList.add(o);
	        }
	        for (Entity n : npc[currentMap]) {
	            if (n != null) entityList.add(n);
	        }
	        for (Entity m : mon[currentMap]) {
	            if (m != null) entityList.add(m);
	        }
	        for (Entity proj : projectileList) {
	            if (proj != null) entityList.add(proj);
	        }

	        // Trier selon la position verticale
	        entityList.sort(Comparator.comparingInt(Entity::getWorldY));

	        // Dessiner les entités
	        for (Entity e : entityList) {
	            e.draw(g2);
	        }
	        
	        // Textes flottants
	        for (FloatingText t : floatingTexts) {
	        	t.draw(g2, this, floatingFont);
	        }
	        
	        // Hitbox (debug, touche H)
	        debug.drawHitboxes(g2);

	        // UI
	        ui.draw(g2);
	        
	        // Carte (touche M)
	        minimap.draw(g2);
	    }

	    lastDrawTime = System.nanoTime() - drawStart;

	    // Mode debug (touche T) : infos, aide, entité suivie
	    debug.drawOverlay(g2, currentFPS, lastDrawTime);

	    // Fondu au noir
	    if (fadeAlpha > 0f) {
	    	g2.setColor(new Color(0f, 0f, 0f, fadeAlpha));
	    	g2.fillRect(0, 0, screenWidth, screenHeight);
	    }
	}
	
	// ===================== Caméra =====================
	
	/** Entité au centre de l'écran : le joueur, ou le PNJ/monstre suivi en mode debug (N). */
	public Entity getCamera() {
		return debug.getCameraTarget();
	}
	
	/**
	 * Position du coin haut-gauche de l'écran dans le monde. La caméra suit le joueur
	 * mais s'arrête aux bords de la map (en haut, elle s'arrête sous le HUD).
	 */
	public int getCameraX() {
		int x = getCamera().getWorldX() - player.getScreenX();
		int max = maxWorldCols * tileSize - screenWidth;
		return Math.max(0, Math.min(max, x));
	}
	
	public int getCameraY() {
		int hud = tileSize * 2; // Le HUD cache les 2 premières rangées de l'écran
		int y = getCamera().getWorldY() - player.getScreenY();
		int max = maxWorldRows * tileSize - screenHeight;
		return Math.max(-hud, Math.min(max, y));
	}
	
	/** Convertit une coordonnée du monde en coordonnée à l'écran. */
	public int worldToScreenX(int worldX) {
		return worldX - getCameraX();
	}
	
	public int worldToScreenY(int worldY) {
		return worldY - getCameraY();
	}

	@Override
	public void paintComponent(Graphics g) {
	    super.paintComponent(g);
	    // On recopie simplement la dernière frame calculée
	    synchronized (tempScreen) {
	    	g.drawImage(tempScreen, 0, 0, null);
	    }
	}

	@Override
	public void run() {

		final double drawInterval = 1_000_000_000.0 / FPS;
		double delta = 0;
		long lastTime = System.nanoTime();
		long currentTime;
		long timer = 0;
		int drawCount = 0;

		while(gameThread != null) {

			currentTime = System.nanoTime();

			delta += (currentTime - lastTime) / drawInterval;
			timer += (currentTime - lastTime);
			lastTime = currentTime;

			if(delta >= 1) {
				// Le KeyHandler (thread Swing) se synchronise aussi sur ce verrou :
				// l'état du jeu n'est jamais modifié pendant une update ou un dessin.
				synchronized (gameLock) {
					update();
					synchronized (tempScreen) {
						drawToTempScreen();
					}
				}
				repaint();
				Toolkit.getDefaultToolkit().sync(); // Évite les saccades sous Linux
				delta--;
				if (delta > 5) delta = 0; // Grosse latence (ex : fenêtre déplacée) : on ne rattrape pas
				drawCount++;
			} else {
				// On rend la main au processeur au lieu de boucler à vide (CPU à 100%)
				long remainingNs = (long) ((1 - delta) * drawInterval);
				try {
					Thread.sleep(remainingNs / 1_000_000, (int) (remainingNs % 1_000_000));
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return;
				}
			}

			// Compteur de FPS (60)
			if(timer >= 1_000_000_000) {
				currentFPS = drawCount;
				drawCount = 0;
				timer = 0;
			}
		}
	}

	// ===================== Drapeaux d'événements =====================

	/** Marque un événement comme accompli (ex : "ermite", "torches_foret"). */
	public void setFlag(String flag) {
		if (flag == null || flag.isEmpty() || !flags.add(flag)) return;
		// Une barrière vient-elle de s'ouvrir quelque part sur cette map ?
		for (InteractiveTile t : iTile[currentMap]) {
			if (t instanceof tile_interactive.IT_Gate && ((tile_interactive.IT_Gate) t).willOpen()) {
				ui.showNotification("Un passage s'est ouvert...");
				break;
			}
		}
	}

	/** Tous les drapeaux sont-ils accomplis ? ("a+b" = a ET b). */
	public boolean hasFlag(String flag) {
		if (flag == null || flag.isEmpty()) return true;
		for (String f : flag.split("\\+")) {
			if (!flags.contains(f)) return false;
		}
		return true;
	}

	/** Affiche un petit texte qui monte au-dessus d'une position du monde. */
	public void addFloatingText(String text, int worldX, int worldY, Color color) {
		floatingTexts.add(new FloatingText(text, worldX, worldY, color));
	}
	
	public DebugTools getDebug() {
		return debug;
	}
	
	public Minimap getMinimap() {
		return minimap;
	}
	
	public AssetSetter getASetter() {
		return aSetter;
	}
	
	public Object getGameLock() {
		return gameLock;
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
		return optionsState;
	}

	public int getIntroState() {
		return introState;
	}

	public IntroScreen getIntro() {
		return intro;
	}

	public int getGameOverState() {
		return gameOverState;
	}
}
