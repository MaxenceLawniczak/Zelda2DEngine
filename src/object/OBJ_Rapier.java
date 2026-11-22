package object;

import java.awt.Color;

import entity.Entity;
import entity.Player;
import main.GamePanel;

public class OBJ_Rapier extends Entity{

	public OBJ_Rapier(GamePanel gp) {
		super(gp);
		
		type = typeSword;
		name = "Rapière";
		down1 = setup("/objects/weapons/Rapier", gp.getTileSize(),gp.getTileSize());
		attackValue = 2;
		
		// Zone touchée par le coup (portée) et durée des 3 étapes de l'animation (vitesse)
		attackArea.width = gp.getTileSize() * 30 / 32;
		attackArea.height = gp.getTileSize() * 30 / 32;
		motion1Duration = 3;
		motion2Duration = 6;
		motion3Duration = 9;
		
		description = "[" + name + "]\n"
								 + "Une lame fine et légère \n"
								 + "pensée pour l'estoc. \n"
								 + "Peu de dégâts, mais \n"
								 + "elle frappe à une \n"
								 + "vitesse redoutable.";

		// Durabilité et spécificité
		maxDurability = 30;
		durability = maxDurability;
		specialName = "Coup critique";
		specialDescription = "Frappe très vite, et 25 % des coups font double dégâts.";
	}

	@Override
	public int modifyDamage(Player player, Entity target, int damage) {
		if (Math.random() < 0.25) {
			gp.addFloatingText("CRITIQUE !", target.getWorldX() + gp.getTileSize() / 2, target.getWorldY() - gp.getTileSize() / 3, new Color(255, 230, 80));
			return damage * 2;
		}
		return damage;
	}
}
