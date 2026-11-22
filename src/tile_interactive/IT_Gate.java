package tile_interactive;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;

import main.GamePanel;
import main.Sprites;

/**
 * Barrière qui bloque un passage jusqu'à ce qu'un événement soit accompli
 * (ex : "ermite" = avoir parlé à l'ermite, "torches_foret+cle" = les deux).
 */
public class IT_Gate extends InteractiveTile {

	private final String flag;
	private int openCounter = -1; // >= 0 : en train de s'ouvrir

	public IT_Gate(GamePanel gp, int col, int row, String flag) {
		super(gp, col, row);
		this.flag = flag;
		this.worldX = gp.getTileSize() * col;
		this.worldY = gp.getTileSize() * row;
		down1 = Sprites.gate(gp.getTileSize());
		destructible = false;
		name = "Barrière";
	}

	/** Le drapeau vient d'être accompli et la barrière ne s'est pas encore ouverte. */
	public boolean willOpen() {
		return openCounter < 0 && gp.hasFlag(flag);
	}

	@Override
	public boolean isSolid() {
		return openCounter < 0;
	}

	@Override
	public void update() {
		if (openCounter < 0 && gp.hasFlag(flag)) openCounter = 0;
		if (openCounter >= 0 && ++openCounter > 30) alive = false; // Disparaît en fondu
	}

	@Override
	public void draw(Graphics2D g2) {
		if (openCounter >= 0) {
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, 1f - openCounter / 30f)));
		}
		super.draw(g2);
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
	}
}
