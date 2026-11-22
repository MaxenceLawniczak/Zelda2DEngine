package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Reinforced_Shield extends Entity{

	public OBJ_Reinforced_Shield(GamePanel gp) {
		super(gp);

		type = typeShield;
		name = "Bouclier renforcé";
		down1 = setup("/objects/shields/ReinforcedShield", gp.getTileSize(),gp.getTileSize());
		defenseValue = 3;
		description = "[" + name + "]\n"
								 + "Un bouclier en bois \n"
								 + "cerclé de bandes de fer. \n"
								 + "Lourd, mais il résiste \n"
								 + "aux coups les plus \n"
								 + "violents.";

		// Durabilité et spécificité
		maxDurability = 50;
		durability = maxDurability;
		specialName = "Rempart";
		specialDescription = "Protège devant et sur les côtés. Très résistant.";
	}

	@Override
	public boolean blocksFrom(String side) {
		return !side.equals("back");
	}
}
