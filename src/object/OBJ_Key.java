package object;

import java.awt.Color;

import entity.Entity;
import entity.Player;
import main.GamePanel;
import main.Sprites;

/** Petite clé : ramassée directement, elle ouvre une porte fermée à clé. */
public class OBJ_Key extends Entity{

	public OBJ_Key(GamePanel gp) {
		super(gp);

		type = typePickUpOnly;
		name = "Petite clé";
		down1 = Sprites.key(gp.getTileSize());
		description = "[" + name + "]\n"
								 + "Une petite clé en métal \n"
								 + "usée par le temps. Elle \n"
								 + "ouvre une porte fermée \n"
								 + "à clé.";
	}

	@Override
	public void use(Entity entity) {
		if (entity instanceof Player) {
			((Player) entity).addKey();
			gp.addFloatingText("+1 clé", entity.getWorldX() + gp.getTileSize() / 2, entity.getWorldY(), new Color(255, 220, 90));
			gp.ui.showNotification("Vous avez trouvé une petite clé !");
		}
	}
}
