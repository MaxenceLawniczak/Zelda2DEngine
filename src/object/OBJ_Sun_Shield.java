package object;

import java.awt.Color;

import entity.Entity;
import entity.Player;
import entity.Projectile;
import main.GamePanel;

public class OBJ_Sun_Shield extends Entity{

	public OBJ_Sun_Shield(GamePanel gp) {
		super(gp);

		type = typeShield;
		name = "Bouclier solaire";
		down1 = setup("/objects/shields/SunShield", gp.getTileSize(),gp.getTileSize());
		defenseValue = 4;
		description = "[" + name + "]\n"
								 + "Un bouclier doré frappé \n"
								 + "d'un soleil. Il renvoie \n"
								 + "la lumière et semble \n"
								 + "toujours chaud au \n"
								 + "toucher.";

		// Durabilité et spécificité
		maxDurability = 30;
		durability = maxDurability;
		specialName = "Flammes solaires";
		specialDescription = "Bloquer une attaque enflamme l'attaquant, qui brûle pendant quelques secondes.";
	}

	@Override
	public void onBlock(Player player, Entity attacker, boolean perfect) {
		Entity target = attacker instanceof Projectile ? ((Projectile) attacker).getUser() : attacker;
		if (target != null && target != player && target.isMonster()) {
			target.burn(180);
			gp.addFloatingText("Brûlé !", target.getWorldX() + gp.getTileSize() / 2, target.getWorldY() - gp.getTileSize() / 3, new Color(255, 140, 30));
		}
	}
}
