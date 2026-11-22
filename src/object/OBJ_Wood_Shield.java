package object;

import java.awt.Color;

import entity.Entity;
import entity.Player;
import entity.Projectile;
import main.GamePanel;

public class OBJ_Wood_Shield extends Entity{

	public OBJ_Wood_Shield(GamePanel gp) {
		super(gp);

		type = typeShield;
		name = "Bouclier en bois";
		down1 = setup("/objects/shields/BigShield", gp.getTileSize(),gp.getTileSize());
		defenseValue = 2;
		description = "[" + name + "]\n"
								 + "Un bouclier en bois marqué \n"
								 + "par le temps, récupéré \n"
								 + "parmi de vieux objets. \n"
								 + "Fragile et fissuré, il \n"
								 + "peut encore encaisser \n"
								 + "quelques coups avant de "
								 + "\nrendre l’âme.";

		// Durabilité et spécificité
		maxDurability = 15;
		durability = maxDurability;
		specialName = "Épines";
		specialDescription = "Les ennemis qui frappent le bouclier se blessent et sont repoussés.";
	}

	@Override
	public void onBlock(Player player, Entity attacker, boolean perfect) {
		if (!(attacker instanceof Projectile)) {
			attacker.takeDamage(1, new Color(160, 230, 90));
			attacker.knockback(player.getDirection(), 8);
		}
	}
}
