package entity;

import java.util.Random;

import main.GamePanel;
import main.Sprites;
import main.UtilityTool;

/**
 * Villageois configurable depuis le fichier de la map :
 *   npc villager <col> <row> Nom : première page // deuxième page // ...
 * Sprite provisoire : celui du joueur recoloré en bleu.
 */
public class NPC_Villager extends Entity {

	private final Random rand = new Random();
	private int homeX = -1, homeY = -1;
	private String[] pages = { "Bonjour !" };

	public NPC_Villager(GamePanel gp) {
		this(gp, 0.58f, 0.55f);
	}

	/** hue / saturation : couleur de la tunique (provisoire, à partir du sprite du joueur). */
	public NPC_Villager(GamePanel gp, float hue, float saturation) {
		super(gp);
		type = typeNPC;
		name = "Villageois";
		direction = "Down";
		speed = 1;
		solidArea.x = 0;
		solidArea.y = gp.getTileSize() / 2;
		solidArea.width = gp.getTileSize();
		solidArea.height = gp.getTileSize() / 2;
		solidAreaDefaultX = solidArea.x;
		solidAreaDefaultY = solidArea.y;

		int ts = gp.getTileSize();
		String[] dirs = { "Up", "Down", "Left", "Right" };
		java.awt.image.BufferedImage[][] imgs = new java.awt.image.BufferedImage[4][2];
		for (int d = 0; d < 4; d++) {
			for (int f = 0; f < 2; f++) {
				imgs[d][f] = Sprites.recolor(UtilityTool.loadImage("/player/walk/Link" + dirs[d] + f, ts, ts), hue, saturation);
			}
		}
		up1 = imgs[0][0]; up2 = imgs[0][1];
		down1 = imgs[1][0]; down2 = imgs[1][1];
		left1 = imgs[2][0]; left2 = imgs[2][1];
		right1 = imgs[3][0]; right2 = imgs[3][1];
	}

	@Override
	public void configure(String[] args) {
		if (args.length == 0) return;
		String text = String.join(" ", args);
		int colon = text.indexOf(" : ");
		if (colon > 0) {
			name = text.substring(0, colon).trim();
			text = text.substring(colon + 3);
		}
		pages = text.split("\\s*//\\s*");
	}

	@Override
	public void setAction() {
		if (homeX < 0) {
			homeX = worldX;
			homeY = worldY;
		}
		actionLockCounter++;
		if (actionLockCounter >= 150) {
			int i = rand.nextInt(100);
			speed = i < 40 ? 0 : 1;
			direction = i < 25 ? "Up" : i < 50 ? "Down" : i < 75 ? "Left" : "Right";
			int range = gp.getTileSize() * 2;
			if (worldX - homeX > range) direction = "Left";
			else if (homeX - worldX > range) direction = "Right";
			else if (worldY - homeY > range) direction = "Up";
			else if (homeY - worldY > range) direction = "Down";
			actionLockCounter = 0;
		}
	}

	@Override
	public void speak() {
		switch (gp.getPlayer().getDirection()) {
		case "Up": direction = "Down"; break;
		case "Down": direction = "Up"; break;
		case "Left": direction = "Right"; break;
		default: direction = "Left"; break;
		}
		actionLockCounter = 0;
		gp.ui.getDialogueBox().start(name, pages);
	}
}
