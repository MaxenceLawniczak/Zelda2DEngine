package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Normal_Bow extends Entity{

	public OBJ_Normal_Bow(GamePanel gp) {
		super(gp);

		type = typeBow;
		name = "Arc de voyageur";
		down1 = setup("/objects/bows/NormalBow", gp.getTileSize(),gp.getTileSize());
		attackValue = 3; // Dégâts des flèches tirées avec cet arc
		description = "[" + name + "]\n"
								 + "Un arc en bois souple \n"
								 + "et bien tendu. Plus \n"
								 + "précis et plus puissant \n"
								 + "qu'un arc de fortune.";

		// Durabilité et spécificité
		maxDurability = 70;
		durability = maxDurability;
		specialName = "Tir perçant";
		specialDescription = "Les flèches traversent les ennemis et peuvent en toucher plusieurs.";
	}

	@Override
	public boolean piercingArrows() {
		return true;
	}
}
