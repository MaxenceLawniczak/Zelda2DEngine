package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/** Petit texte qui monte et disparaît au-dessus d'une entité (dégâts, "CRITIQUE !", "PARADE !"...). */
public class FloatingText {

	private static final int DURATION = 50;

	private final String text;
	private final int worldX;
	private int worldY;
	private final Color color;
	private int life = DURATION;

	public FloatingText(String text, int worldX, int worldY, Color color) {
		this.text = text;
		this.worldX = worldX;
		this.worldY = worldY;
		this.color = color;
	}

	public boolean update() {
		life--;
		if (life % 2 == 0) worldY--;
		return life > 0;
	}

	public void draw(Graphics2D g2, GamePanel gp, Font font) {
		float alpha = Math.min(1f, life / 15f);
		g2.setFont(font);
		int x = gp.worldToScreenX(worldX) - g2.getFontMetrics().stringWidth(text) / 2;
		int y = gp.worldToScreenY(worldY);
		g2.setColor(new Color(0f, 0f, 0f, alpha));
		g2.drawString(text, x + 2, y + 2);
		g2.setColor(new Color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, alpha));
		g2.drawString(text, x, y);
	}
}
