package object;

import entity.Entity;
import entity.Player;
import tile_interactive.InteractiveTile;
import tile_interactive.IT_HeavyRock;
import main.GamePanel;

public class OBJ_Heavy_Sword extends Entity{

	public OBJ_Heavy_Sword(GamePanel gp) {
		super(gp);
		
		type = typeSword;
		name = "Épée lourde";
		down1 = setup("/objects/weapons/HeavySword", gp.getTileSize(),gp.getTileSize());
		attackValue = 6;
		
		// Zone touchée par le coup (portée) et durée des 3 étapes de l'animation (vitesse)
		attackArea.width = gp.getTileSize() * 44 / 32;
		attackArea.height = gp.getTileSize() * 44 / 32;
		motion1Duration = 8;
		motion2Duration = 16;
		motion3Duration = 24;
		
		description = "[" + name + "]\n"
								 + "Un énorme bloc d'acier \n"
								 + "qu'il faut des deux bras \n"
								 + "pour soulever. Très \n"
								 + "lente, mais chaque coup \n"
								 + "écrase tout.";

		// Durabilité et spécificité
		maxDurability = 55;
		durability = maxDurability;
		specialName = "Écrasement";
		specialDescription = "Brise les gros rochers, étourdit et repousse les ennemis.";
	}

	@Override
	public boolean canBreakTile(InteractiveTile tile) {
		return tile.isDestructible() || tile instanceof IT_HeavyRock;
	}

	@Override
	public void onHit(Player player, Entity target, boolean killed) {
		if (!killed) {
			target.stun(45);
			target.knockback(player.getDirection(), 8);
		}
	}
}
