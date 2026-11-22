package object;

import java.awt.Color;

import entity.Entity;
import entity.Player;
import main.GamePanel;

public class OBJ_Royal_Guard_Sword extends Entity{

	public OBJ_Royal_Guard_Sword(GamePanel gp) {
		super(gp);
		
		type = typeSword;
		name = "Épée de la Garde Royale";
		down1 = setup("/objects/weapons/RoyalGuardSword", gp.getTileSize(),gp.getTileSize());
		attackValue = 5;
		
		// Zone touchée par le coup (portée) et durée des 3 étapes de l'animation (vitesse)
		attackArea.width = gp.getTileSize() * 34 / 32;
		attackArea.height = gp.getTileSize() * 34 / 32;
		motion1Duration = 5;
		motion2Duration = 10;
		motion3Duration = 15;
		
		description = "[" + name + "]\n"
								 + "L'arme réglementaire des \n"
								 + "gardes du château. \n"
								 + "Équilibrée et fiable, \n"
								 + "elle est réservée aux \n"
								 + "combattants d'élite.";

		// Durabilité et spécificité
		maxDurability = 50;
		durability = maxDurability;
		specialName = "Vigueur";
		specialDescription = "Chaque ennemi vaincu rend un quart de cœur.";
	}

	@Override
	public void onHit(Player player, Entity target, boolean killed) {
		if (killed && player.getLife() < player.getMaxLife()) {
			player.setLife(player.getLife() + 1);
			gp.addFloatingText("+1", player.getWorldX() + gp.getTileSize() / 2, player.getWorldY() - gp.getTileSize() / 3, new Color(120, 255, 120));
		}
	}
}
