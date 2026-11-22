package object;

import entity.Entity;
import entity.Player;
import main.GamePanel;

public class OBJ_Royal_Guard_Shield extends Entity{

	public OBJ_Royal_Guard_Shield(GamePanel gp) {
		super(gp);

		type = typeShield;
		name = "Bouclier de la Garde Royale";
		down1 = setup("/objects/shields/RyalGuardShield", gp.getTileSize(),gp.getTileSize());
		defenseValue = 4;
		description = "[" + name + "]\n"
								 + "Le bouclier des gardes \n"
								 + "d'élite du château. \n"
								 + "Léger et robuste, il \n"
								 + "porte fièrement les \n"
								 + "couleurs du royaume.";

		// Durabilité et spécificité
		maxDurability = 40;
		durability = maxDurability;
		specialName = "Riposte";
		specialDescription = "Après avoir bloqué une attaque, votre coup suivant fait double dégâts.";
	}

	@Override
	public void onBlock(Player player, Entity attacker, boolean perfect) {
		player.setRiposteReady(true);
	}
}
