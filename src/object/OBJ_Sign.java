package object;

import entity.Entity;
import main.GamePanel;
import main.Sprites;

/** Panneau : Entrée devant lui pour le lire. Texte dans le fichier de la map ("//" = page suivante). */
public class OBJ_Sign extends Entity{

	private String[] pages = { "..." };

	public OBJ_Sign(GamePanel gp) {
		super(gp);
		type = typeSign;
		name = "Panneau";
		down1 = Sprites.sign(gp.getTileSize());
		up1 = left1 = right1 = down1;
		collision = true;
	}

	@Override
	public void configure(String[] args) {
		if (args.length == 0) return;
		String text = String.join(" ", args);
		pages = text.split("\\s*//\\s*");
	}

	public void read() {
		gp.ui.getDialogueBox().start("Panneau", pages);
	}
}
