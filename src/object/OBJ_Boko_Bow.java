package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Boko_Bow extends Entity{

	public OBJ_Boko_Bow(GamePanel gp) {
		super(gp);

		type = typeBow;
		name = "Arc Boko";
		down1 = setup("/objects/bows/BokoBow", gp.getTileSize(),gp.getTileSize());
		attackValue = 2; // Dégâts des flèches tirées avec cet arc
		description = "[" + name + "]\n"
								 + "Un arc de fortune façonné \n"
								 + "dans du bois brut, utilisé \n"
								 + "par les combattants les \n"
								 + "moins expérimentés. Simple \n"
								 + "mais efficace, il permet \n"
								 + "d'attaquer à distance avec \n"
								 + "agilité.";
	

		// Durabilité et spécificité
		maxDurability = 40;
		durability = maxDurability;
		specialName = "Récupération";
		specialDescription = "30 % de chance de ne pas consommer de flèche.";
	}

	@Override
	public float arrowSaveChance() {
		return 0.3f;
	}
}
