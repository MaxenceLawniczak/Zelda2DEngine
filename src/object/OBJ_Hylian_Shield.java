package object;

import java.awt.Color;

import entity.Entity;
import entity.Player;
import entity.Projectile;
import main.GamePanel;

public class OBJ_Hylian_Shield extends Entity{

	public OBJ_Hylian_Shield(GamePanel gp) {
		super(gp);

		type = typeShield;
		name = "Bouclier du héros";
		down1 = setup("/objects/shields/HylianShield", gp.getTileSize(),gp.getTileSize());
		defenseValue = 5;
		description = "[" + name + "]\n"
								 + "Un bouclier légendaire \n"
								 + "transmis de héros en \n"
								 + "héros. Presque rien ne \n"
								 + "peut le traverser.";

		// Durabilité et spécificité
		maxDurability = 80;
		durability = maxDurability;
		specialName = "Égide du héros";
		specialDescription = "Protège de tous les côtés et renvoie les projectiles.";
	}

	@Override
	public boolean blocksFrom(String side) {
		return true;
	}

	@Override
	public void onBlock(Player player, Entity attacker, boolean perfect) {
		if (attacker instanceof Projectile) {
			((Projectile) attacker).reflect(player);
			gp.addFloatingText("Renvoyé !", player.getWorldX() + gp.getTileSize() / 2, player.getWorldY() - gp.getTileSize() / 3, new Color(200, 220, 255));
		}
	}
}
