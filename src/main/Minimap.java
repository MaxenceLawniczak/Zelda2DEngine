package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import entity.Entity;
import main.MapData.Placement;
import object.OBJ_Chest;
import tile.TileManager;

/**
 * Carte du monde (touche M) : petite carte dans un coin -> grande carte -> désactivée.
 * L'image des tiles est calculée une seule fois par map puis mise en cache.
 */
public class Minimap {

	public static final int OFF = 0, CORNER = 1, FULL = 2;

	private final GamePanel gp;
	private final BufferedImage[] cache;
	private int mode = OFF;
	private int blinkCounter = 0;

	// Position de la grande carte (pour convertir un clic en case)
	private int fullX, fullY, fullCell;

	public Minimap(GamePanel gp) {
		this.gp = gp;
		cache = new BufferedImage[gp.getMaxMap()];
	}

	public void cycleMode() {
		mode = (mode + 1) % 3;
	}

	public int getMode() {
		return mode;
	}

	/** À appeler quand les tiles d'une map changent (rechargement R). */
	public void invalidate(int map) {
		cache[map] = null;
	}

	private BufferedImage getMapImage(int map) {
		if (cache[map] == null) {
			final int cell = 8; // Taille d'une case dans l'image en cache
			TileManager tm = gp.getTileM();
			BufferedImage img = new BufferedImage(gp.getMaxWorldCols() * cell, gp.getMaxWorldRows() * cell, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = img.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			for (int col = 0; col < gp.getMaxWorldCols(); col++) {
				for (int row = 0; row < gp.getMaxWorldRows(); row++) {
					g2.drawImage(tm.getTile(tm.getMapTileNum(map, col, row)).getImage(), col * cell, row * cell, cell, cell, null);
				}
			}
			g2.dispose();
			cache[map] = img;
		}
		return cache[map];
	}

	public void draw(Graphics2D g2) {
		if (mode == OFF || gp.getGameState() == gp.getTitleState()) return;
		blinkCounter++;

		if (mode == CORNER) {
			int cell = Math.max(1, 160 / gp.getMaxWorldCols()); // Petite carte d'environ 160 px
			int w = gp.getMaxWorldCols() * cell;
			int h = gp.getMaxWorldRows() * cell;
			int x = gp.getScreenWidth() - w - 10;
			int y = gp.getScreenHeight() - h - 10;
			drawMap(g2, x, y, cell, false);
		} else {
			// Fond sombre + grande carte centrée
			g2.setColor(new Color(0, 0, 0, 190));
			g2.fillRect(0, 0, gp.getScreenWidth(), gp.getScreenHeight());
			fullCell = Math.min((gp.getScreenWidth() - 40) / gp.getMaxWorldCols(), (gp.getScreenHeight() - 70) / gp.getMaxWorldRows());
			int w = gp.getMaxWorldCols() * fullCell;
			fullX = (gp.getScreenWidth() - w) / 2;
			fullY = 40;
			g2.setFont(new Font("Arial", Font.BOLD, 18));
			g2.setColor(Color.WHITE);
			String title = "Carte - Map " + (gp.getCurrentMap() + 1);
			g2.drawString(title, (gp.getScreenWidth() - g2.getFontMetrics().stringWidth(title)) / 2, 28);
			drawMap(g2, fullX, fullY, fullCell, true);

			g2.setFont(new Font("Arial", Font.PLAIN, 12));
			g2.setColor(Color.LIGHT_GRAY);
			String legend = "Jaune : vous   Rouge : monstres   Bleu : PNJ   Orange : coffres"
					+ (gp.getDebug().isEnabled() ? "   (clic = téléportation)" : "");
			g2.drawString(legend, (gp.getScreenWidth() - g2.getFontMetrics().stringWidth(legend)) / 2, gp.getScreenHeight() - 10);
		}
	}

	private void drawMap(Graphics2D g2, int x, int y, int cell, boolean details) {
		int map = gp.getCurrentMap();
		int w = gp.getMaxWorldCols() * cell;
		int h = gp.getMaxWorldRows() * cell;
		int ts = gp.getTileSize();

		g2.drawImage(getMapImage(map), x, y, w, h, null);

		// Tiles interactives (elles peuvent être détruites, donc dessinées en direct)
		for (Entity t : gp.getItileTab()) {
			if (t != null && t.getDown1() != null) {
				g2.drawImage(t.getDown1(), x + t.getWorldX() * cell / ts, y + t.getWorldY() * cell / ts, cell, cell, null);
			}
		}

		// Événements (debug uniquement)
		if (details && gp.getDebug().isEnabled()) {
			for (Placement p : gp.getASetter().getMapData(map).get(MapData.EVENT)) {
				EntityFactory.Kind k = EntityFactory.getKind(MapData.EVENT, p.id);
				g2.setColor(k != null ? k.color : Color.MAGENTA);
				g2.drawRect(x + p.col * cell, y + p.row * cell, cell - 1, cell - 1);
			}
		}

		int dot = Math.max(3, cell);
		for (Entity o : gp.getObjTab()) {
			if (o instanceof OBJ_Chest) drawDot(g2, x, y, cell, o, ((OBJ_Chest) o).isOpened() ? new Color(120, 80, 30) : Color.ORANGE, dot);
		}
		for (Entity n : gp.getNPCTab()) {
			if (n != null) drawDot(g2, x, y, cell, n, new Color(80, 160, 255), dot);
		}
		for (Entity m : gp.getMonTab()) {
			if (m != null && m.isAlive()) drawDot(g2, x, y, cell, m, Color.RED, dot);
		}
		// Joueur (clignote)
		if ((blinkCounter / 15) % 2 == 0) {
			drawDot(g2, x, y, cell, gp.getPlayer(), Color.YELLOW, dot + 2);
		}

		// Zone visible à l'écran
		if (details) {
			int vx = x + gp.getCameraX() * cell / ts;
			int vy = y + gp.getCameraY() * cell / ts;
			g2.setColor(new Color(255, 255, 255, 120));
			g2.setStroke(new BasicStroke(1));
			g2.drawRect(vx, vy, gp.getScreenWidth() * cell / ts, gp.getScreenHeight() * cell / ts);
		}

		g2.setColor(Color.WHITE);
		g2.setStroke(new BasicStroke(2));
		g2.drawRect(x - 1, y - 1, w + 1, h + 1);
	}

	private void drawDot(Graphics2D g2, int x, int y, int cell, Entity e, Color color, int size) {
		int ts = gp.getTileSize();
		int cx = x + (e.getWorldX() + ts / 2) * cell / ts;
		int cy = y + (e.getWorldY() + ts / 2) * cell / ts;
		g2.setColor(Color.BLACK);
		g2.fillOval(cx - size / 2 - 1, cy - size / 2 - 1, size + 2, size + 2);
		g2.setColor(color);
		g2.fillOval(cx - size / 2, cy - size / 2, size, size);
	}

	/** Case de la map sous un point de l'écran (grande carte uniquement), ou null. */
	public Point screenToTile(int sx, int sy) {
		if (mode != FULL || fullCell == 0) return null;
		int col = (sx - fullX) / fullCell;
		int row = (sy - fullY) / fullCell;
		if (sx < fullX || sy < fullY || col >= gp.getMaxWorldCols() || row >= gp.getMaxWorldRows()) return null;
		return new Point(col, row);
	}
}
