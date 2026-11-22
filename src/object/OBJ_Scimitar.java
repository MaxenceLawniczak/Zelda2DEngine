package object;

import entity.Entity;
import entity.Player;
import main.GamePanel;

public class OBJ_Scimitar extends Entity{

	public OBJ_Scimitar(GamePanel gp) {
		super(gp);
		
		type = typeSword;
		name = "Cimeterre";
		down1 = setup("/objects/weapons/Scimitar", gp.getTileSize(),gp.getTileSize());
		attackValue = 3;
		
		// Zone touchée par le coup (portée) et durée des 3 étapes de l'animation (vitesse)
		attackArea.width = gp.getTileSize() * 36 / 32;
		attackArea.height = gp.getTileSize() * 36 / 32;
		motion1Duration = 5;
		motion2Duration = 10;
		motion3Duration = 15;
		
		description = "[" + name + "]\n"
								 + "Une lame courbe venue \n"
								 + "des déserts du sud. Son \n"
								 + "tranchant large balaie \n"
								 + "tout ce qui se trouve \n"
								 + "devant vous.";

		// Durabilité et spécificité
		maxDurability = 35;
		durability = maxDurability;
		specialName = "Saignement";
		specialDescription = "Les ennemis touchés saignent et perdent des PV pendant quelques secondes.";
	}

	@Override
	public void onHit(Player player, Entity target, boolean killed) {
		if (!killed) target.bleed(180);
	}
}
