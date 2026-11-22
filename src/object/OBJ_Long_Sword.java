package object;

import entity.Entity;
import entity.Player;
import main.GamePanel;

public class OBJ_Long_Sword extends Entity{

	public OBJ_Long_Sword(GamePanel gp) {
		super(gp);
		
		type = typeSword;
		name = "Épée longue";
		down1 = setup("/objects/weapons/LongSword", gp.getTileSize(),gp.getTileSize());
		attackValue = 4;
		
		// Zone touchée par le coup (portée) et durée des 3 étapes de l'animation (vitesse)
		attackArea.width = gp.getTileSize() * 40 / 32;
		attackArea.height = gp.getTileSize() * 40 / 32;
		motion1Duration = 6;
		motion2Duration = 12;
		motion3Duration = 18;
		
		description = "[" + name + "]\n"
								 + "Une épée à deux mains \n"
								 + "à la portée impression- \n"
								 + "nante. Un peu lente, \n"
								 + "elle permet de frapper \n"
								 + "les ennemis de loin.";

		// Durabilité et spécificité
		maxDurability = 40;
		durability = maxDurability;
		specialName = "Repousse";
		specialDescription = "Ses grands coups envoient les ennemis valser au loin.";
	}

	@Override
	public void onHit(Player player, Entity target, boolean killed) {
		if (!killed) target.knockback(player.getDirection(), 14);
	}
}
