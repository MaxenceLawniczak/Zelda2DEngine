package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/** Écran titre simple (sans animation) : fond dégradé, titre, héros et menu. */
public class TitleScreen {

	private static final String[] MENU = { "Nouveau", "Charger", "Quitter" };

	private final GamePanel gp;
	private final Font titleFont, menuFont, smallFont;
	private BufferedImage background;

	public TitleScreen(GamePanel gp, Font baseFont) {
		this.gp = gp;
		int ts = gp.getTileSize();
		titleFont = baseFont.deriveFont(Font.BOLD, ts * 1.05f);
		menuFont = baseFont.deriveFont(Font.BOLD, ts * 0.7f);
		smallFont = baseFont.deriveFont(ts * 0.35f);
	}

	public void reset() {}

	/** Gardé pour compatibilité : l'écran titre est prêt tout de suite. */
	public boolean isReady() {
		return true;
	}

	public void skipAppear() {}

	public void draw(Graphics2D g2, int commandNum) {
		int w = gp.getScreenWidth(), h = gp.getScreenHeight(), ts = gp.getTileSize();

		if (background == null) {
			background = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			Graphics2D bg = background.createGraphics();
			bg.setPaint(new GradientPaint(0, 0, new Color(0x0b1033), 0, h, new Color(0x3a1d5c)));
			bg.fillRect(0, 0, w, h);
			bg.dispose();
		}
		g2.drawImage(background, 0, 0, null);

		// Titre
		String title = "The Tales Of Zeldo";
		g2.setFont(titleFont);
		int tx = (w - g2.getFontMetrics().stringWidth(title)) / 2;
		int ty = h / 4;
		g2.setColor(Color.BLACK);
		g2.drawString(title, tx + 4, ty + 4);
		g2.setColor(new Color(255, 245, 210));
		g2.drawString(title, tx, ty);

		g2.setFont(smallFont.deriveFont(ts * 0.45f));
		String subtitle = "~ Les éclats du Cristal d'Aube ~";
		g2.setColor(new Color(200, 220, 255));
		g2.drawString(subtitle, (w - g2.getFontMetrics().stringWidth(subtitle)) / 2, ty + ts);

		// Héros
		int size = ts * 2;
		int hy = h / 4 + ts * 2;
		g2.drawImage(gp.getPlayer().getImage("Down", 0), (w - size) / 2, hy, size, size, null);

		// Menu
		g2.setFont(menuFont);
		int y = hy + size + ts * 2;
		for (int i = 0; i < MENU.length; i++) {
			String text = MENU[i];
			int x = (w - g2.getFontMetrics().stringWidth(text)) / 2;
			boolean selected = i == commandNum;
			if (selected) {
				g2.setColor(new Color(255, 220, 120));
				g2.drawString(">", x - ts, y);
			}
			g2.setColor(Color.BLACK);
			g2.drawString(text, x + 3, y + 3);
			g2.setColor(selected ? new Color(255, 230, 150) : (i == 1 ? new Color(150, 150, 160) : Color.WHITE));
			g2.drawString(text, x, y);
			y += (int) (ts * 1.1);
		}

		g2.setFont(smallFont);
		String hint = "Entrée : valider   Z/S : choisir";
		g2.setColor(new Color(255, 255, 255, 170));
		g2.drawString(hint, (w - g2.getFontMetrics().stringWidth(hint)) / 2, h - ts / 2);
	}
}
