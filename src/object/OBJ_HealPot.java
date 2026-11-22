package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_HealPot extends Entity{
	
	public OBJ_HealPot(GamePanel gp) {
		super(gp);
		
		type = typePotionSoin;
		name = "Potion de soin";
		value = 4;
		down1 = setup("/objects/inventory/PotionSoin", gp.getTileSize(),gp.getTileSize());
		
		description = "[" + name + "]\n"
				 + "Un fiole en verre remplie\n"
				 + "d'un liquide rouge vif\n"
				 + "aux vertues médicinales\n"
				 + "incroyables.";
	}
}
