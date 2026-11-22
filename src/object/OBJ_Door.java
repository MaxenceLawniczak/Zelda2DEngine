package object;

import entity.Entity;
import entity.Player;
import main.GamePanel;
import main.UtilityTool;

/**
 * Porte fermée à clé : on l'ouvre en marchant dedans avec une petite clé.
 * Paramètre facultatif : un drapeau accompli quand elle s'ouvre.
 */
public class OBJ_Door extends Entity{

	private String flag = "";

	public OBJ_Door(GamePanel gp) {
		super(gp);

		type = typeDoor;
		name = "Porte verrouillée";
		down1 = UtilityTool.loadImageOptional("/objects/door", gp.getTileSize(), gp.getTileSize());
		if (down1 == null) down1 = setup("/tiles/tile0193", gp.getTileSize(), gp.getTileSize());
		up1 = left1 = right1 = down1;
		collision = true;
	}

	@Override
	public void configure(String[] args) {
		if (args.length > 0) flag = args[0];
	}

	/** Ouvre la porte si le joueur a une clé. Renvoie true si elle s'est ouverte. */
	public boolean tryOpen(Player player, boolean talk) {
		if (player.useKey()) {
			gp.ui.showNotification("La porte s'ouvre !");
			gp.setFlag(flag);
			return true;
		}
		if (talk) gp.ui.getDialogueBox().start(null, "C'est fermé à clé.", "Il doit bien y avoir une clé quelque part...");
		return false;
	}
}
