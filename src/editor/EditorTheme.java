package editor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.UIManager;

/** Thème sombre de l'éditeur + icônes des outils (dessinées en code, aucune image à fournir). */
public final class EditorTheme {

	public static final Color BACKGROUND = new Color(37, 38, 43);
	public static final Color PANEL = new Color(50, 52, 58);
	public static final Color ACCENT = new Color(255, 196, 80);
	public static final Color TEXT = new Color(225, 225, 230);

	private EditorTheme() {}

	/** Thème "Nimbus" en version sombre (fourni avec Java, rien à installer). */
	public static void apply() {
		UIManager.put("control", PANEL);
		UIManager.put("info", PANEL);
		UIManager.put("nimbusBase", new Color(30, 32, 38));
		UIManager.put("nimbusAlertYellow", ACCENT);
		UIManager.put("nimbusDisabledText", new Color(120, 120, 128));
		UIManager.put("nimbusFocus", new Color(255, 196, 80, 160));
		UIManager.put("nimbusGreen", new Color(120, 190, 90));
		UIManager.put("nimbusInfoBlue", new Color(70, 120, 200));
		UIManager.put("nimbusLightBackground", new Color(43, 44, 50));
		UIManager.put("nimbusOrange", ACCENT);
		UIManager.put("nimbusRed", new Color(200, 70, 70));
		UIManager.put("nimbusSelectedText", Color.BLACK);
		UIManager.put("nimbusSelectionBackground", ACCENT);
		UIManager.put("text", TEXT);
		UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 13));
		try {
			for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					return;
				}
			}
		} catch (Exception ignored) {
			// Thème par défaut si Nimbus n'est pas disponible
		}
	}

	/** Icône 22x22 d'un outil. */
	public static ImageIcon icon(MapCanvas.Tool tool) {
		int s = 22;
		BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		Color main = TEXT, accent = ACCENT;
		switch (tool) {
		case PENCIL:
			g.setColor(accent);
			g.drawLine(5, 17, 16, 6);
			g.setColor(main);
			g.drawLine(16, 6, 18, 4);
			g.fillPolygon(new int[] { 3, 7, 4 }, new int[] { 19, 17, 15 }, 3);
			break;
		case LINE:
			g.setColor(main);
			g.drawLine(4, 18, 18, 4);
			g.setColor(accent);
			g.fillOval(2, 16, 5, 5);
			g.fillOval(15, 1, 5, 5);
			break;
		case RECT:
			g.setColor(accent);
			g.fillRect(4, 5, 14, 12);
			g.setColor(main);
			g.drawRect(4, 5, 14, 12);
			break;
		case OUTLINE:
			g.setColor(main);
			g.drawRect(4, 5, 14, 12);
			g.setColor(accent);
			g.drawRect(7, 8, 8, 6);
			break;
		case ROOM:
			g.setColor(new Color(90, 150, 80));
			g.fillRect(3, 4, 16, 14);
			g.setColor(new Color(170, 140, 90));
			g.fillRect(7, 8, 8, 6);
			g.setColor(main);
			g.drawRect(3, 4, 16, 14);
			break;
		case TRACE:
			g.setColor(new Color(120, 190, 255));
			g.drawPolyline(new int[] { 3, 3, 12, 12, 19 }, new int[] { 19, 9, 9, 3, 3 }, 5);
			g.setColor(accent);
			g.fillOval(1, 17, 5, 5);
			g.fillOval(10, 7, 5, 5);
			g.fillOval(17, 1, 5, 5);
			break;
		case TERRAIN:
			g.setColor(new Color(240, 210, 90));
			g.fillRoundRect(2, 3, 18, 16, 8, 8);
			g.setColor(new Color(60, 150, 230));
			g.fillOval(6, 7, 10, 8);
			g.setColor(main);
			g.drawRoundRect(2, 3, 18, 16, 8, 8);
			break;
		case FILL:
			g.setColor(main);
			g.drawPolygon(new int[] { 4, 11, 17, 10 }, new int[] { 10, 3, 10, 17 }, 4);
			g.setColor(accent);
			g.fillOval(15, 13, 5, 7);
			break;
		case PICKER:
			g.setColor(main);
			g.drawLine(4, 18, 13, 9);
			g.setColor(accent);
			g.fillOval(12, 3, 8, 8);
			break;
		case ERASER:
			g.rotate(Math.toRadians(-35), 11, 11);
			g.setColor(new Color(240, 130, 150));
			g.fillRoundRect(3, 7, 16, 8, 4, 4);
			g.setColor(main);
			g.drawRoundRect(3, 7, 16, 8, 4, 4);
			g.drawLine(9, 7, 9, 15);
			break;
		case SELECT:
			g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[] { 3, 2 }, 0));
			g.setColor(main);
			g.drawRect(3, 4, 15, 13);
			g.setColor(accent);
			g.fillRect(14, 13, 6, 6);
			break;
		case ELEMENT:
			g.setColor(new Color(200, 140, 60));
			g.fillRect(4, 8, 14, 10);
			g.setColor(accent);
			g.fillRect(4, 5, 14, 5);
			g.setColor(main);
			g.drawRect(4, 5, 14, 13);
			g.fillRect(10, 9, 3, 3);
			break;
		case SPAWN:
			g.setColor(main);
			g.drawLine(6, 3, 6, 19);
			g.setColor(new Color(90, 220, 120));
			g.fillPolygon(new int[] { 7, 18, 7 }, new int[] { 3, 7, 11 }, 3);
			break;
		}
		g.dispose();
		return new ImageIcon(img);
	}
}
