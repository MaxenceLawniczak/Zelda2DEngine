package object;

import java.awt.Color;

import entity.Entity;
import entity.Player;
import entity.Projectile;
import main.GamePanel;

public class OBJ_Knight_Shield extends Entity{

	public OBJ_Knight_Shield(GamePanel gp) {
		super(gp);

		type = typeShield;
		name = "Bouclier de chevalier";
		down1 = setup("/objects/shields/KnightShield", gp.getTileSize(),gp.getTileSize());
		defenseValue = 3;
		description = "[" + name + "]\n"
								 + "Un bouclier d'acier aux \n"
								 + "armoiries d'un ordre de \n"
								 + "chevalerie oublié. Il \n"
								 + "encaisse sans broncher.";

		// Durabilité et spécificité
		maxDurability = 30;
		durability = maxDurability;
		specialName = "Renvoi";
		specialDescription = "Les projectiles bloqués repartent vers celui qui les a lancés.";
	}

	@Override
	public void onBlock(Player player, Entity attacker, boolean perfect) {
		if (attacker instanceof Projectile) {
			((Projectile) attacker).reflect(player);
			gp.addFloatingText("Renvoyé !", player.getWorldX() + gp.getTileSize() / 2, player.getWorldY() - gp.getTileSize() / 3, new Color(200, 220, 255));
		}
	}
}
