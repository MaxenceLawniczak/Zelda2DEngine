package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

/**
 * Images provisoires dessinées en code pour les éléments qui n'ont pas encore de PNG.
 * Chaque méthode essaie d'abord de charger une vraie image (ex : res/objects/Key.png) :
 * dès que tu en dessines une, elle est utilisée à la place.
 */
public final class Sprites {

	private Sprites() {}

	private static BufferedImage canvas(int size) {
		return new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
	}

	private static Graphics2D pen(BufferedImage img) {
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		return g;
	}

	private static BufferedImage orLoad(String path, int size) {
		return UtilityTool.loadImageOptional(path, size, size);
	}

	/** Petite clé dorée. */
	public static BufferedImage key(int s) {
		BufferedImage own = orLoad("/objects/Key", s);
		if (own != null) return own;
		BufferedImage img = canvas(s);
		Graphics2D g = pen(img);
		g.setStroke(new BasicStroke(Math.max(2, s / 10f)));
		g.setColor(new Color(60, 40, 0));
		g.drawOval(s / 6, s / 6, s / 3, s / 3);
		g.drawLine(s / 2, s / 2, s * 5 / 6, s * 5 / 6);
		g.drawLine(s * 2 / 3, s * 2 / 3, s * 3 / 4, s / 2 + s / 10);
		g.setStroke(new BasicStroke(Math.max(1, s / 16f)));
		g.setColor(new Color(255, 210, 60));
		g.drawOval(s / 6, s / 6, s / 3, s / 3);
		g.drawLine(s / 2, s / 2, s * 5 / 6, s * 5 / 6);
		g.drawLine(s * 2 / 3, s * 2 / 3, s * 3 / 4, s / 2 + s / 10);
		g.dispose();
		return img;
	}

	/** Panneau en bois. */
	public static BufferedImage sign(int s) {
		BufferedImage own = orLoad("/objects/Sign", s);
		if (own != null) return own;
		BufferedImage img = canvas(s);
		Graphics2D g = pen(img);
		g.setColor(new Color(60, 35, 10));
		g.fillRect(s / 2 - s / 14, s / 2, s / 7, s / 2 - 2);
		g.fillRoundRect(s / 8 - 2, s / 6 - 2, s * 3 / 4 + 4, s / 2 + 4, 6, 6);
		g.setColor(new Color(170, 110, 50));
		g.fillRoundRect(s / 8, s / 6, s * 3 / 4, s / 2, 6, 6);
		g.setColor(new Color(110, 70, 30));
		for (int i = 1; i <= 3; i++) g.drawLine(s / 4, s / 6 + i * s / 8, s * 3 / 4, s / 6 + i * s / 8);
		g.dispose();
		return img;
	}

	/** Dalle de pierre (relevée ou enfoncée). */
	public static BufferedImage plate(int s, boolean pressed) {
		BufferedImage img = canvas(s);
		Graphics2D g = pen(img);
		int m = s / 8;
		g.setColor(new Color(40, 40, 50));
		g.fillRoundRect(m - 2, m - 2, s - 2 * m + 4, s - 2 * m + 4, 8, 8);
		g.setColor(pressed ? new Color(90, 170, 110) : new Color(150, 150, 165));
		g.fillRoundRect(m, m, s - 2 * m, s - 2 * m, 8, 8);
		g.setColor(pressed ? new Color(60, 120, 80) : new Color(190, 190, 205));
		g.fillPolygon(new Polygon(new int[] { s / 2, s - 2 * m, s / 2, 2 * m }, new int[] { 2 * m, s / 2, s - 2 * m, s / 2 }, 4));
		g.dispose();
		return img;
	}

	/** Barrière de pierre et de bois qui bloque un passage. */
	public static BufferedImage gate(int s) {
		BufferedImage own = orLoad("/tiles_interactive/Gate", s);
		if (own != null) return own;
		BufferedImage img = canvas(s);
		Graphics2D g = pen(img);
		g.setColor(new Color(70, 70, 80));
		g.fillRect(0, s / 8, s, s * 3 / 4);
		g.setColor(new Color(30, 30, 35));
		g.fillRect(0, s / 8, s, s / 10);
		g.fillRect(0, s * 3 / 4, s, s / 10);
		for (int i = 0; i < 4; i++) {
			int x = s / 10 + i * s / 4;
			g.setPaint(new GradientPaint(x, 0, new Color(200, 200, 210), x + s / 8, 0, new Color(110, 110, 125)));
			g.fillRect(x, s / 8, s / 8, s * 3 / 4);
			g.setColor(new Color(30, 30, 35));
			g.drawRect(x, s / 8, s / 8, s * 3 / 4);
		}
		g.dispose();
		return img;
	}

	/** Réceptacle de cœur (gros cœur qui brille). */
	public static BufferedImage heartContainer(int s) {
		BufferedImage img = canvas(s);
		Graphics2D g = pen(img);
		BufferedImage heart = UtilityTool.loadImage("/objects/pickups/HeartF", s * 3 / 4, s * 3 / 4);
		g.setColor(new Color(255, 255, 200, 90));
		g.fillOval(0, 0, s, s);
		if (heart != null) g.drawImage(heart, s / 8, s / 8, null);
		g.dispose();
		return img;
	}

	/** Éclat du Cristal d'Aube. */
	public static BufferedImage crystal(int s) {
		BufferedImage img = canvas(s);
		Graphics2D g = pen(img);
		g.setColor(new Color(170, 240, 255, 90));
		g.fillOval(0, 0, s, s);
		Polygon gem = new Polygon(new int[] { s / 2, s * 3 / 4, s / 2, s / 4 }, new int[] { s / 10, s / 2, s * 9 / 10, s / 2 }, 4);
		g.setColor(new Color(140, 225, 255));
		g.fill(gem);
		g.setColor(new Color(235, 255, 255));
		g.fillPolygon(new int[] { s / 2, s * 3 / 4, s / 2 }, new int[] { s / 10, s / 2, s / 2 }, 3);
		g.setColor(Color.WHITE);
		g.draw(gem);
		g.dispose();
		return img;
	}

	/** Version assombrie d'une image (torche éteinte, coffre ouvert...). */
	public static BufferedImage darker(BufferedImage src, float factor) {
		if (src == null) return null;
		return new RescaleOp(factor, 0, null).filter(src, null);
	}

	/** Change la teinte de toutes les couleurs vives d'un sprite (variantes de monstres). */
	public static BufferedImage hueShift(BufferedImage base, float hue) {
		if (base == null) return null;
		BufferedImage img = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < base.getHeight(); y++) {
			for (int x = 0; x < base.getWidth(); x++) {
				int argb = base.getRGB(x, y);
				int alpha = argb >>> 24;
				float[] hsb = Color.RGBtoHSB((argb >> 16) & 255, (argb >> 8) & 255, argb & 255, null);
				if (alpha > 0 && hsb[1] > 0.25f && hsb[2] > 0.15f) {
					argb = (alpha << 24) | (Color.HSBtoRGB(hue, hsb[1], hsb[2]) & 0xFFFFFF);
				}
				img.setRGB(x, y, argb);
			}
		}
		return img;
	}

	/** Recolore les pixels verts d'un sprite (pour faire des PNJ à partir du sprite du joueur). */
	public static BufferedImage recolor(BufferedImage base, float hue, float saturation) {
		if (base == null) return null;
		BufferedImage img = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < base.getHeight(); y++) {
			for (int x = 0; x < base.getWidth(); x++) {
				int argb = base.getRGB(x, y);
				int alpha = argb >>> 24;
				float[] hsb = Color.RGBtoHSB((argb >> 16) & 255, (argb >> 8) & 255, argb & 255, null);
				if (alpha > 0 && hsb[0] > 0.2f && hsb[0] < 0.5f && hsb[1] > 0.4f) {
					argb = (alpha << 24) | (Color.HSBtoRGB(hue, saturation, hsb[2] * 0.85f) & 0xFFFFFF);
				}
				img.setRGB(x, y, argb);
			}
		}
		return img;
	}
}
