package object;

import java.awt.Color;

import entity.Entity;
import entity.Player;
import entity.Projectile;
import main.GamePanel;

public class OBJ_Soldier_Shield extends Entity{

	public OBJ_Soldier_Shield(GamePanel gp) {
		super(gp);

		type = typeShield;
		name = "Bouclier de soldat";
		down1 = setup("/objects/shields/SoldierShield", gp.getTileSize(),gp.getTileSize());
		defenseValue = 2;
		description = "[" + name + "]\n"
								 + "Le bouclier standard des \n"
								 + "soldats du royaume. \n"
								 + "Sans fioritures, il fait \n"
								 + "exactement ce qu'on \n"
								 + "attend de lui.";

		// Durabilité et spécificité
		maxDurability = 25;
		durability = maxDurability;
		specialName = "Parade";
		specialDescription = "Levez le bouclier juste avant le coup : l'ennemi est étourdi.";
	}

	@Override
	public void onBlock(Player player, Entity attacker, boolean perfect) {
		if (!perfect) return;
		Entity target = attacker instanceof Projectile ? ((Projectile) attacker).getUser() : attacker;
		if (target != null && target != player) {
			target.stun(120);
			gp.addFloatingText("PARADE !", player.getWorldX() + gp.getTileSize() / 2, player.getWorldY() - gp.getTileSize() / 3, new Color(120, 200, 255));
		}
	}
}
