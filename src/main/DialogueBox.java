package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Boîte de dialogue en bas de l'écran :
 *  - plusieurs pages (Entrée = page suivante),
 *  - texte qui s'écrit lettre par lettre (Entrée = tout afficher d'un coup),
 *  - retour à la ligne automatique, nom de la personne qui parle.
 */
public class DialogueBox {

	private final GamePanel gp;
	private final Font font;
	private final Font nameFont;

	private List<String> pages = new ArrayList<>();
	private int pageIndex = 0;
	private int visibleChars = 0;
	private int frameCounter = 0;
	private String speaker = null;
	private Runnable onFinish = null;

	public DialogueBox(GamePanel gp, Font baseFont) {
		this.gp = gp;
		this.font = baseFont.deriveFont(gp.getTileSize() * 0.62f);
		this.nameFont = baseFont.deriveFont(Font.BOLD, gp.getTileSize() * 0.5f);
	}

	/** Lance un dialogue (passe le jeu en état dialogue). speaker peut être null. */
	public void start(String speaker, String... pages) {
		start(speaker, null, pages);
	}

	public void start(String speaker, Runnable onFinish, String... pages) {
		this.speaker = speaker;
		this.onFinish = onFinish;
		this.pages = new ArrayList<>(Arrays.asList(pages));
		pageIndex = 0;
		visibleChars = 0;
		gp.setGameState(gp.getDialogueState());
	}

	public String getCurrentPage() {
		return pages.isEmpty() ? "" : pages.get(pageIndex);
	}

	public boolean isPageComplete() {
		return visibleChars >= getCurrentPage().length();
	}

	/** Touche Entrée : finit d'écrire la page, ou passe à la suivante, ou ferme le dialogue. */
	public void advance() {
		if (!isPageComplete()) {
			visibleChars = getCurrentPage().length();
		} else if (pageIndex + 1 < pages.size()) {
			pageIndex++;
			visibleChars = 0;
		} else {
			gp.setGameState(gp.getPlayState());
			if (onFinish != null) {
				Runnable r = onFinish;
				onFinish = null;
				r.run();
			}
		}
	}

	public void draw(Graphics2D g2) {
		// Une lettre de plus toutes les frames (≈ 60 lettres par seconde)
		frameCounter++;
		if (!isPageComplete()) visibleChars++;

		int ts = gp.getTileSize();
		int x = ts;
		int width = gp.getScreenWidth() - ts * 2;
		int height = (int) (ts * 3.6);
		int y = gp.getScreenHeight() - height - ts / 2;

		gp.ui.drawSubWindow(x, y, width, height);

		// Nom de la personne qui parle, dans un onglet au-dessus de la boîte
		if (speaker != null) {
			g2.setFont(nameFont);
			int nameWidth = g2.getFontMetrics().stringWidth(speaker) + ts;
			int tabY = y - (int) (ts * 0.75);
			g2.setColor(new Color(0, 0, 0, 220));
			g2.fillRoundRect(x + ts / 2, tabY, nameWidth, (int) (ts * 0.9), 20, 20);
			g2.setColor(new Color(255, 220, 120));
			g2.drawString(speaker, x + ts, tabY + (int) (ts * 0.6));
		}

		// Texte (avec retour à la ligne automatique)
		g2.setFont(font);
		g2.setColor(Color.WHITE);
		FontMetrics fm = g2.getFontMetrics();
		String visible = getCurrentPage().substring(0, Math.min(visibleChars, getCurrentPage().length()));
		List<String> lines = wrap(getCurrentPage(), fm, width - ts * 2);
		int textY = y + ts + fm.getAscent() / 3;
		int remaining = visible.length();
		for (String line : lines) {
			if (remaining <= 0) break;
			String part = line.substring(0, Math.min(line.length(), remaining));
			g2.drawString(part, x + ts, textY);
			remaining -= line.length() + 1; // +1 : espace ou retour à la ligne consommé
			textY += (int) (fm.getHeight() * 1.05);
		}

		// Petit triangle qui clignote quand on peut continuer
		if (isPageComplete() && (frameCounter / 20) % 2 == 0) {
			int tx = x + width - ts;
			int ty = y + height - ts / 2;
			Polygon arrow = new Polygon(new int[] { tx - 8, tx + 8, tx }, new int[] { ty - 8, ty - 8, ty + 4 }, 3);
			g2.setColor(new Color(255, 220, 120));
			g2.fill(arrow);
			g2.setStroke(new BasicStroke(1));
		}

		// Numéro de page
		if (pages.size() > 1) {
			g2.setFont(nameFont.deriveFont(Font.PLAIN, ts * 0.35f));
			g2.setColor(new Color(255, 255, 255, 140));
			String count = (pageIndex + 1) + "/" + pages.size();
			g2.drawString(count, x + width - ts - g2.getFontMetrics().stringWidth(count) - 16, y + height - ts / 3);
		}
	}

	/** Découpe un texte en lignes qui tiennent dans maxWidth (respecte aussi les \n). */
	static List<String> wrap(String text, FontMetrics fm, int maxWidth) {
		List<String> lines = new ArrayList<>();
		for (String paragraph : text.split("\n", -1)) {
			StringBuilder line = new StringBuilder();
			for (String word : paragraph.split(" ", -1)) {
				String candidate = line.length() == 0 ? word : line + " " + word;
				if (fm.stringWidth(candidate) > maxWidth && line.length() > 0) {
					lines.add(line.toString());
					line = new StringBuilder(word);
				} else {
					line = new StringBuilder(candidate);
				}
			}
			lines.add(line.toString());
		}
		return lines;
	}
}
