package object;

import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

import entity.Entity;
import entity.Player;
import main.GamePanel;
import main.UtilityTool;

public class OBJ_Chest extends Entity{

	private Entity loot;       // Contenu du coffre (défini dans mapXX_data.txt)
	private boolean opened = false;
	private BufferedImage closedImage, openedImage;

	public OBJ_Chest(GamePanel gp) {
		super(gp);

		type = typeChest;
		name = "Coffre";
		closedImage = setup("/objects/containers/Chest", gp.getTileSize(), gp.getTileSize());

		// Si tu dessines un jour "ChestOpened.png", il sera utilisé automatiquement.
		// En attendant, le coffre ouvert est simplement assombri.
		openedImage = UtilityTool.loadImageOptional("/objects/containers/ChestOpened", gp.getTileSize(), gp.getTileSize());
		if (openedImage == null && closedImage != null) {
			openedImage = new RescaleOp(0.55f, 0, null).filter(closedImage, null);
		}

		down1 = closedImage;
		up1 = left1 = right1 = down1;
		collision = true;
	}

	public void setLoot(Entity loot) {
		this.loot = loot;
	}

	public Entity getLoot() {
		return loot;
	}

	public boolean isOpened() {
		return opened;
	}

	public void open(Player player) {
		gp.setGameState(gp.getDialogueState());

		if (opened) {
			gp.ui.setCurrentDialogue("Le coffre est vide.");
			return;
		}
		if (loot == null) {
			gp.ui.setCurrentDialogue("Vous ouvrez le coffre...\nIl n'y a rien dedans.");
		} else if (loot.isPickUpOnly()) {
			// Clé, réceptacle de cœur, éclat du cristal : utilisés directement
			gp.ui.setCurrentDialogue("Vous ouvrez le coffre !\nVous obtenez :\n" + loot.getName());
			loot.use(player);
			loot = null;
		} else if (player.giveItem(loot)) {
			gp.ui.setCurrentDialogue("Vous ouvrez le coffre !\nVous obtenez :\n" + loot.getName());
			loot = null;
		} else {
			gp.ui.setCurrentDialogue("Votre inventaire est plein.\nImpossible de prendre\n" + loot.getName() + ".");
			return; // Le coffre reste fermé
		}
		opened = true;
		down1 = up1 = left1 = right1 = openedImage;
	}
}
