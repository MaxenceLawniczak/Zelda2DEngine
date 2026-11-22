package object;

import java.awt.Color;

import entity.Entity;
import entity.Player;
import main.GamePanel;

public class OBJ_Eyed_Shield extends Entity{

	public OBJ_Eyed_Shield(GamePanel gp) {
		super(gp);

		type = typeShield;
		name = "Bouclier à l'œil";
		down1 = setup("/objects/shields/EyedShield", gp.getTileSize(),gp.getTileSize());
		defenseValue = 3;
		description = "[" + name + "]\n"
								 + "Un étrange bouclier orné \n"
								 + "d'un œil peint. On dit \n"
								 + "qu'il intimide les \n"
								 + "monstres... ou alors \n"
								 + "c'est juste de la déco.";

		// Durabilité et spécificité
		maxDurability = 25;
		durability = maxDurability;
		specialName = "Regard pétrifiant";
		specialDescription = "Bouclier levé, il paralyse régulièrement les ennemis qui sont devant vous.";
	}

	@Override
	public void whileGuarding(Player player, int guardFrames) {
		if (guardFrames % 60 != 1) return;
		int range = gp.getTileSize() * 3;
		for (Entity m : gp.getMonTab()) {
			if (m == null || !m.isAlive() || m.isDying()) continue;
			int dx = m.getWorldX() - player.getWorldX();
			int dy = m.getWorldY() - player.getWorldY();
			boolean inFront;
			switch (player.getDirection()) {
			case "Up": inFront = dy < 0 && -dy <= range && Math.abs(dx) <= gp.getTileSize(); break;
			case "Down": inFront = dy > 0 && dy <= range && Math.abs(dx) <= gp.getTileSize(); break;
			case "Left": inFront = dx < 0 && -dx <= range && Math.abs(dy) <= gp.getTileSize(); break;
			default: inFront = dx > 0 && dx <= range && Math.abs(dy) <= gp.getTileSize(); break;
			}
			if (inFront) {
				m.stun(90);
				gp.addFloatingText("Paralysé", m.getWorldX() + gp.getTileSize() / 2, m.getWorldY() - gp.getTileSize() / 3, new Color(200, 120, 255));
			}
		}
	}
}
