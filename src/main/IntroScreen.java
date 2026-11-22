package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Introduction : texte qui défile vers le haut en s'éloignant, façon générique de film spatial.
 * Entrée ou Échap : passer l'intro. Espace : accélérer.
 */
public class IntroScreen {

	private static final String TITLE = "LES ÉCLATS DU CRISTAL D'AUBE";
	private static final String TEXT =
			"Il y a bien longtemps, le royaume d'Ardelune vivait en paix, veillé par la lumière du Cristal d'Aube.\n\n"
			+ "Mais une nuit sans lune, le Cristal se brisa. Ses éclats furent dispersés aux quatre coins du monde, "
			+ "et les ténèbres commencèrent lentement à s'étendre sur les terres.\n\n"
			+ "Les monstres sortirent des grottes. Les forêts se couvrirent d'une étrange brume. "
			+ "Les anciens disaient que seul celui qui réunirait les éclats pourrait ramener la lumière.\n\n"
			+ "Un jeune voyageur prit la route, bien décidé à retrouver les éclats perdus. "
			+ "Mais en traversant le Bois des Murmures, la brume l'enveloppa...\n\n"
			+ "Quand il rouvrit les yeux, il était seul, au cœur d'une forêt inconnue. "
			+ "Sans le moindre souvenir de ce qui l'avait mené ici.";

	private final GamePanel gp;
	private final Font font, titleFont, hintFont;
	private final float[][] stars = new float[160][];
	private List<String> lines = null;
	private float scroll = 0;
	private boolean finished = false;
	private boolean fast = false;

	public IntroScreen(GamePanel gp, Font baseFont) {
		this.gp = gp;
		font = baseFont.deriveFont(Font.BOLD, gp.getTileSize() * 0.75f);
		titleFont = baseFont.deriveFont(Font.BOLD, gp.getTileSize() * 1.0f);
		hintFont = baseFont.deriveFont(gp.getTileSize() * 0.32f);
		Random r = new Random(4);
		for (int i = 0; i < stars.length; i++) {
			stars[i] = new float[] { r.nextFloat() * gp.getScreenWidth(), r.nextFloat() * gp.getScreenHeight(), 1 + r.nextInt(2) };
		}
	}

	public void start() {
		scroll = 0;
		finished = false;
		fast = false;
		gp.setGameState(gp.getIntroState());
	}

	/** Entrée : passe l'intro. */
	public void advance() {
		skip();
	}

	/** Espace maintenu : accélère le défilement. */
	public void setFast(boolean fast) {
		this.fast = fast;
	}

	public void skip() {
		if (finished) return;
		finished = true;
		gp.fadeTo(gp::beginAdventure);
	}

	public void update() {
		if (finished) return;
		scroll += fast ? 4f : 0.8f;
		// Fin du texte : on lance la partie
		if (lines != null && scroll > (lines.size() + 6) * lineSpacing() + gp.getScreenHeight()) skip();
	}

	private int lineSpacing() {
		return (int) (gp.getTileSize() * 0.95);
	}

	public void draw(Graphics2D g2) {
		int w = gp.getScreenWidth(), h = gp.getScreenHeight(), ts = gp.getTileSize();
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, w, h);
		for (float[] s : stars) {
			g2.setColor(new Color(255, 255, 255, 180));
			g2.fillRect((int) s[0], (int) s[1], (int) s[2], (int) s[2]);
		}
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		// Découpe du texte en lignes (une seule fois)
		if (lines == null) {
			g2.setFont(font);
			lines = new ArrayList<>();
			lines.add("#" + TITLE); // # = ligne de titre
			lines.add("");
			lines.addAll(DialogueBox.wrap(TEXT, g2.getFontMetrics(), (int) (w * 0.62)));
		}

		// Chaque ligne monte depuis le bas de l'écran et rapetisse en s'éloignant vers l'horizon
		int horizon = h / 6;
		for (int i = 0; i < lines.size(); i++) {
			float baseY = h + i * lineSpacing() - scroll; // Position "à plat" de la ligne
			if (baseY > h + ts || baseY < -h) continue;
			// Distance parcourue depuis le bas (0) jusqu'à l'horizon (1)
			float depth = Math.max(0f, Math.min(1f, (h - baseY) / (float) (h * 1.6)));
			float scale = 1f - depth * 0.85f;
			if (scale <= 0.12f) continue;
			int y = (int) (h - (h - horizon) * (1 - Math.pow(1 - depth, 1.6)));
			float alpha = depth > 0.75f ? Math.max(0f, 1f - (depth - 0.75f) / 0.25f) : 1f;

			String line = lines.get(i);
			boolean isTitle = line.startsWith("#");
			if (isTitle) line = line.substring(1);
			Font f = (isTitle ? titleFont : font).deriveFont((isTitle ? titleFont : font).getSize2D() * scale);
			g2.setFont(f);
			FontMetrics fm = g2.getFontMetrics();
			int x = (w - fm.stringWidth(line)) / 2;
			g2.setColor(new Color(255, 220, 90, (int) (255 * alpha)));
			g2.drawString(line, x, y);
		}

		g2.setFont(hintFont);
		g2.setColor(new Color(255, 255, 255, 130));
		String hint = "Entrée / Échap : passer    Espace : accélérer";
		g2.drawString(hint, w - g2.getFontMetrics().stringWidth(hint) - 12, h - 10);
	}
}
