package object;

import entity.Entity;
import entity.Player;
import main.GamePanel;

public class OBJ_Master_Sword extends Entity{

	public OBJ_Master_Sword(GamePanel gp) {
		super(gp);
		
		type = typeSword;
		name = "Lame sacrée";
		down1 = setup("/objects/weapons/MasterSword", gp.getTileSize(),gp.getTileSize());
		attackValue = 8;
		
		// Zone touchée par le coup (portée) et durée des 3 étapes de l'animation (vitesse)
		attackArea.width = gp.getTileSize() * 38 / 32;
		attackArea.height = gp.getTileSize() * 38 / 32;
		motion1Duration = 4;
		motion2Duration = 8;
		motion3Duration = 12;
		
		description = "[" + name + "]\n"
								 + "Une lame légendaire qui \n"
								 + "semble briller d'elle- \n"
								 + "même. On raconte \n"
								 + "qu'elle ne choisit que \n"
								 + "les cœurs les plus purs.";

		// Durabilité et spécificité
		maxDurability = 0; // Incassable
		durability = maxDurability;
		specialName = "Rayon sacré";
		specialDescription = "Quand votre vie est pleine, chaque coup lance un rayon de lumière. Incassable.";
	}

	@Override
	public void onAttack(Player player) {
		if (player.getLife() >= player.getMaxLife()) player.fireBeam();
	}
}
