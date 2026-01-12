package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Wood_Shield extends Entity{

	public OBJ_Wood_Shield(GamePanel gp) {
		super(gp);

		type = typeWeakShield;
		name = "Petit Bouclier";
		down1 = setup("/objects/BigShield", gp.getTileSize(),gp.getTileSize());
		defenseValue = 2;
		description = "[" + name + "]\n"
								 + "Un bouclier en bois marqué \n"
								 + "par le temps, récupéré \n"
								 + "parmi de vieux objets. \n"
								 + "Fragile et fissuré, il \n"
								 + "peut encore encaisser \n"
								 + "quelques coups avant de "
								 + "\nrendre l’âme.";
	}
}
