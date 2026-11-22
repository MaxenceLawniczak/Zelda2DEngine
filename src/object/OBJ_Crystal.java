package object;

import entity.Entity;
import main.GamePanel;
import main.Sprites;

/** Éclat du Cristal d'Aube (paramètre : numéro de l'éclat, 1 à 3). Le 3e termine la démo. */
public class OBJ_Crystal extends Entity{

	private int number = 1;

	public OBJ_Crystal(GamePanel gp) {
		super(gp);
		type = typePickUpOnly;
		name = "Éclat du Cristal d'Aube";
		down1 = Sprites.crystal(gp.getTileSize());
	}

	@Override
	public void configure(String[] args) {
		if (args.length > 0) {
			try {
				number = Integer.parseInt(args[0]);
			} catch (NumberFormatException ignored) {
				// Garde 1
			}
		}
	}

	@Override
	public void use(Entity entity) {
		gp.setFlag("eclat_" + number);
		switch (number) {
		case 1:
			gp.ui.getDialogueBox().start(null,
					"Vous avez trouvé le premier éclat du Cristal d'Aube !",
					"Une douce lumière vous enveloppe... Au loin, vers l'est, un autre éclat semble répondre à son appel.",
					"Le doyen de Bourg-Brise, au nord du rivage, voudra sûrement le voir.");
			break;
		case 2:
			gp.ui.getDialogueBox().start(null,
					"Vous avez trouvé le deuxième éclat du Cristal d'Aube !",
					"Les deux éclats vibrent ensemble. Leur lumière désigne le sommet du Pic Venteux, au nord de la plaine.");
			break;
		default:
			gp.ui.getDialogueBox().start(null,
					"Vous avez réuni trois éclats du Cristal d'Aube !",
					"Leur lumière chasse la brume qui recouvrait Ardelune... mais d'autres éclats restent encore à trouver.",
					"Fin de la démo. Merci d'avoir joué ! La suite de l'aventure arrive bientôt...");
		}
	}
}
