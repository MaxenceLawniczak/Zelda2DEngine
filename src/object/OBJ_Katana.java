package object;

import entity.Entity;
import entity.Player;
import main.GamePanel;

public class OBJ_Katana extends Entity{

	public OBJ_Katana(GamePanel gp) {
		super(gp);
		
		type = typeSword;
		name = "Katana";
		down1 = setup("/objects/weapons/Katana", gp.getTileSize(),gp.getTileSize());
		attackValue = 4;
		
		// Zone touchée par le coup (portée) et durée des 3 étapes de l'animation (vitesse)
		attackArea.width = gp.getTileSize() * 30 / 32;
		attackArea.height = gp.getTileSize() * 30 / 32;
		motion1Duration = 4;
		motion2Duration = 8;
		motion3Duration = 12;
		
		description = "[" + name + "]\n"
								 + "Une lame forgée et \n"
								 + "repliée des centaines de \n"
								 + "fois. Rapide et \n"
								 + "tranchante, elle ne \n"
								 + "pardonne aucune erreur.";

		// Durabilité et spécificité
		maxDurability = 30;
		durability = maxDurability;
		specialName = "Tranchant";
		specialDescription = "Ignore complètement la défense des ennemis.";
	}

	@Override
	public int modifyDamage(Player player, Entity target, int damage) {
		return damage + target.getDefense();
	}
}
