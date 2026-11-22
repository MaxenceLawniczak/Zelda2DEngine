package object;

import java.awt.Color;

import entity.Entity;
import entity.Player;
import main.GamePanel;

public class OBJ_Old_Shield extends Entity{

	public OBJ_Old_Shield(GamePanel gp) {
		super(gp);

		type = typeShield;
		name = "Vieux bouclier";
		down1 = setup("/objects/shields/OldShield", gp.getTileSize(),gp.getTileSize());
		defenseValue = 1;
		description = "[" + name + "]\n"
								 + "Un bouclier cabossé \n"
								 + "trouvé dans un grenier. \n"
								 + "Il a vu passer bien des \n"
								 + "coups... et il en a gardé \n"
								 + "toutes les marques.";

		// Durabilité et spécificité
		maxDurability = 6;
		durability = maxDurability;
		specialName = "Dernier souffle";
		specialDescription = "Quand il se brise, il rend un cœur entier.";
	}

	@Override
	public void onBreak(Player player) {
		player.setLife(Math.min(player.getMaxLife(), player.getLife() + 4));
		gp.addFloatingText("+1 coeur", player.getWorldX() + gp.getTileSize() / 2, player.getWorldY() - gp.getTileSize() / 3, new Color(120, 255, 120));
	}
}
