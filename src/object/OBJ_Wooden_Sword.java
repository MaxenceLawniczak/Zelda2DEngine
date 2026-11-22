package object;

import entity.Entity;
import entity.Player;
import main.GamePanel;

public class OBJ_Wooden_Sword extends Entity{

	public OBJ_Wooden_Sword(GamePanel gp) {
		super(gp);
		
		type = typeSword;
		name = "Épée en bois";
		down1 = setup("/objects/weapons/WoodenSword", gp.getTileSize(),gp.getTileSize());
		attackValue = 2;
		
		// Zone touchée par le coup (portée) et durée des 3 étapes de l'animation (vitesse)
		attackArea.width = gp.getTileSize() * 28 / 32;
		attackArea.height = gp.getTileSize() * 28 / 32;
		motion1Duration = 5;
		motion2Duration = 10;
		motion3Duration = 15;
		
		description = "[" + name + "]\n"
								 + "Une épée d'entraînement \n"
								 + "taillée dans du chêne. \n"
								 + "Elle ne coupe pas grand- \n"
								 + "chose, mais elle est plus \n"
								 + "solide qu'une branche.";

		// Durabilité et spécificité
		maxDurability = 25;
		durability = maxDurability;
		specialName = "Étourdissement";
		specialDescription = "Les ennemis touchés sont étourdis pendant une seconde.";
	}

	@Override
	public void onHit(Player player, Entity target, boolean killed) {
		if (!killed) target.stun(60);
	}
}
