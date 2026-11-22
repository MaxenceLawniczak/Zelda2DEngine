package object;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;

import entity.Projectile;
import main.GamePanel;

/** Rayon lancé par la Lame sacrée quand le joueur a toute sa vie (dessiné en code, pas d'image). */
public class OBJ_SwordBeam extends Projectile {

	public OBJ_SwordBeam(GamePanel gp) {
		super(gp);
		name = "Rayon sacré";
		speed = 9;
		maxLife = 45;
		life = maxLife;
		attack = 4;
		alive = false;
		solidArea.x = gp.getTileSize() / 4;
		solidArea.y = gp.getTileSize() / 4;
		solidArea.width = gp.getTileSize() / 2;
		solidArea.height = gp.getTileSize() / 2;
		solidAreaDefaultX = solidArea.x;
		solidAreaDefaultY = solidArea.y;
	}

	@Override
	public void draw(Graphics2D g2) {
		if (!alive || !isOnScreen()) return;
		int ts = gp.getTileSize();
		int cx = gp.worldToScreenX(worldX) + ts / 2;
		int cy = gp.worldToScreenY(worldY) + ts / 2;
		boolean horizontal = direction.equals("Left") || direction.equals("Right");
		int dx = direction.equals("Left") ? 1 : direction.equals("Right") ? -1 : 0;
		int dy = direction.equals("Up") ? 1 : direction.equals("Down") ? -1 : 0;
		float fade = Math.min(1f, life / 10f);

		// Traînée lumineuse
		for (int i = 3; i >= 1; i--) {
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade * 0.25f / i));
			g2.setColor(new Color(150, 220, 255));
			int tx = cx + dx * i * ts / 3, ty = cy + dy * i * ts / 3;
			g2.fillOval(tx - ts / 3, ty - ts / 3, ts * 2 / 3, ts * 2 / 3);
		}
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade));
		// Halo
		g2.setPaint(new RadialGradientPaint(new Point2D.Float(cx, cy), ts * 0.7f, new float[] { 0f, 1f },
				new Color[] { new Color(200, 240, 255, 200), new Color(120, 200, 255, 0) }));
		g2.fillOval(cx - ts, cy - ts, ts * 2, ts * 2);
		// Lame d'énergie (croissant allongé dans le sens du tir)
		int w = horizontal ? ts * 3 / 4 : ts / 4, h = horizontal ? ts / 4 : ts * 3 / 4;
		g2.setColor(new Color(220, 250, 255));
		g2.fillOval(cx - w / 2, cy - h / 2, w, h);
		g2.setColor(Color.WHITE);
		g2.fillOval(cx - w / 4, cy - h / 4, w / 2, h / 2);
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
	}
}
