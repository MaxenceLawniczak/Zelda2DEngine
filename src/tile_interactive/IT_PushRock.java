package tile_interactive;

import entity.Entity;
import main.GamePanel;

/** Rocher qu'on peut pousser d'une case en restant appuyé contre lui (énigmes avec des dalles). */
public class IT_PushRock extends InteractiveTile {

	private int targetX, targetY; // Case vers laquelle il glisse

	public IT_PushRock(GamePanel gp, int col, int row) {
		super(gp, col, row);
		this.worldX = gp.getTileSize() * col;
		this.worldY = gp.getTileSize() * row;
		targetX = worldX;
		targetY = worldY;
		down1 = withArrows(setup("/tiles_interactive/Rock", gp.getTileSize(), gp.getTileSize()));
		destructible = false;
		name = "Rocher (à pousser)";
	}

	/** Le rocher avec 4 petites flèches : on voit qu'il peut être poussé. */
	private java.awt.image.BufferedImage withArrows(java.awt.image.BufferedImage base) {
		int s = gp.getTileSize();
		java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(s, s, java.awt.image.BufferedImage.TYPE_INT_ARGB);
		java.awt.Graphics2D g = img.createGraphics();
		if (base != null) g.drawImage(base, 0, 0, null);
		g.setColor(new java.awt.Color(255, 255, 255, 200));
		int a = s / 8, m = s / 2;
		g.fillPolygon(new int[] { m - a, m + a, m }, new int[] { a + 2, a + 2, 2 }, 3);
		g.fillPolygon(new int[] { m - a, m + a, m }, new int[] { s - a - 2, s - a - 2, s - 2 }, 3);
		g.fillPolygon(new int[] { a + 2, a + 2, 2 }, new int[] { m - a, m + a, m }, 3);
		g.fillPolygon(new int[] { s - a - 2, s - a - 2, s - 2 }, new int[] { m - a, m + a, m }, 3);
		g.dispose();
		return img;
	}

	public boolean isMoving() {
		return worldX != targetX || worldY != targetY;
	}

	/** Essaie de pousser le rocher d'une case. Renvoie true s'il bouge. */
	public boolean push(String direction) {
		if (isMoving()) return false;
		// Posé sur une dalle : il s'y verrouille (l'énigme ne peut plus être défaite par erreur)
		for (InteractiveTile t : gp.getItileTab()) {
			if (t instanceof IT_Plate && t.getWorldX() == worldX && t.getWorldY() == worldY) return false;
		}
		int ts = gp.getTileSize();
		int col = worldX / ts, row = worldY / ts;
		switch (direction) {
		case "Up": row--; break;
		case "Down": row++; break;
		case "Left": col--; break;
		default: col++; break;
		}
		if (!isFree(col, row)) return false;
		targetX = col * ts;
		targetY = row * ts;
		this.col = col;
		this.row = row;
		return true;
	}

	/** La case est-elle libre (pas de mur, pas d'autre obstacle) ? */
	private boolean isFree(int col, int row) {
		if (col < 0 || row < 0 || col >= gp.getMaxWorldCols() || row >= gp.getMaxWorldRows()) return false;
		if (gp.getTileM().getTile(gp.getTileM().getMapTileNum(col, row)).isCollision()) return false;
		int ts = gp.getTileSize();
		for (InteractiveTile t : gp.getItileTab()) {
			if (t != null && t != this && t.isSolid() && t.getWorldX() / ts == col && t.getWorldY() / ts == row) return false;
		}
		for (Entity[] group : new Entity[][] { gp.getObjTab(), gp.getMonTab(), gp.getNPCTab() }) {
			for (Entity e : group) {
				if (e == null) continue;
				int ec = (e.getWorldX() + ts / 2) / ts, er = (e.getWorldY() + ts / 2) / ts;
				if (ec == col && er == row) return false;
			}
		}
		return true;
	}

	@Override
	public void update() {
		// Glisse doucement vers sa nouvelle case
		int step = gp.getTileSize() / 8;
		if (worldX != targetX) worldX += Integer.signum(targetX - worldX) * Math.min(step, Math.abs(targetX - worldX));
		if (worldY != targetY) worldY += Integer.signum(targetY - worldY) * Math.min(step, Math.abs(targetY - worldY));
	}
}
