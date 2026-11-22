package entity;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import main.GamePanel;
import main.UtilityTool;

/**
 * Le vieil ermite de la forêt : il raconte au joueur pourquoi il est là.
 * Ses images (/npc/OldMan...) n'existent pas encore : en attendant, son sprite est
 * généré à partir de celui du joueur (robe violette + bâton).
 * Dès que tu ajoutes res/npc/OldManDown0.png, OldManDown1.png, etc., elles seront utilisées.
 */
public class NPC_OldMan extends Entity{

	private final Random rand = new Random();
	private int homeX = -1, homeY = -1;    // Il ne s'éloigne pas trop de chez lui
	private boolean alreadyTalked = false;

	public NPC_OldMan(GamePanel gp) {
	    super(gp);

	    type = typeNPC;
	    name = "Aldric";
	    direction = "Down";
	    speed = 1;

		solidArea.x = 0;
		solidArea.y = gp.getTileSize()/2;
		solidArea.width = gp.getTileSize();
		solidArea.height = gp.getTileSize()/2;
		solidAreaDefaultX = solidArea.x;
		solidAreaDefaultY = solidArea.y;

	    getImage();
	}

	public void getImage() {
		up1 = sprite("Up", 0);
		up2 = sprite("Up", 1);
		down1 = sprite("Down", 0);
		down2 = sprite("Down", 1);
		left1 = sprite("Left", 0);
		left2 = sprite("Left", 1);
		right1 = sprite("Right", 0);
		right2 = sprite("Right", 1);
	}

	private BufferedImage sprite(String dir, int frame) {
		int ts = gp.getTileSize();
		BufferedImage own = UtilityTool.loadImageOptional("/npc/OldMan" + dir + frame, ts, ts);
		if (own != null) return own;
		return makeHermit(UtilityTool.loadImage("/player/walk/Link" + dir + frame, ts, ts), dir);
	}

	/** Sprite provisoire : celui du joueur avec une robe violette et un bâton. */
	private BufferedImage makeHermit(BufferedImage base, String dir) {
		if (base == null) return null;
		BufferedImage img = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < base.getHeight(); y++) {
			for (int x = 0; x < base.getWidth(); x++) {
				int argb = base.getRGB(x, y);
				int alpha = argb >>> 24;
				int r = (argb >> 16) & 255, g = (argb >> 8) & 255, b = argb & 255;
				float[] hsb = Color.RGBtoHSB(r, g, b, null);
				if (alpha > 0 && hsb[0] > 0.2f && hsb[0] < 0.5f && hsb[1] > 0.4f) {
					// Vert -> violet
					argb = (alpha << 24) | (Color.HSBtoRGB(0.76f, 0.45f, hsb[2] * 0.85f) & 0xFFFFFF);
				}
				img.setRGB(x, y, argb);
			}
		}
		// Bâton de marche
		if (!dir.equals("Up")) {
			Graphics2D g2 = img.createGraphics();
			int ts = gp.getTileSize();
			int sx = dir.equals("Left") ? ts / 8 : ts - ts / 8;
			g2.setStroke(new BasicStroke(Math.max(2, ts / 16f)));
			g2.setColor(Color.BLACK);
			g2.drawLine(sx, ts / 6, sx, ts - 2);
			g2.setStroke(new BasicStroke(Math.max(1, ts / 32f)));
			g2.setColor(new Color(150, 100, 50));
			g2.drawLine(sx, ts / 6 + 1, sx, ts - 3);
			g2.dispose();
		}
		return img;
	}

	public void setAction() {
		if (homeX < 0) {
			homeX = worldX;
			homeY = worldY;
		}

		actionLockCounter ++;

		if(actionLockCounter >= 120) {
			int i = rand.nextInt(100)+1;

			// De temps en temps, il s'arrête un peu
			speed = i <= 30 ? 0 : 1;

			if (i <= 25) direction = "Up";
			else if (i <= 50) direction = "Down";
			else if (i <= 75) direction = "Left";
			else direction = "Right";

			// Trop loin de chez lui : il revient
			int range = gp.getTileSize() * 2;
			if (worldX - homeX > range) direction = "Left";
			else if (homeX - worldX > range) direction = "Right";
			else if (worldY - homeY > range) direction = "Up";
			else if (homeY - worldY > range) direction = "Down";

			actionLockCounter = 0;
		}
	}

	public void speak() {

		// Il se tourne vers le joueur
		switch(gp.getPlayer().getDirection()) {
		case "Up": direction = "Down"; break;
		case "Down": direction = "Up"; break;
		case "Left": direction = "Right"; break;
		case "Right": direction = "Left"; break;
		}
		actionLockCounter = 0;

		if (!alreadyTalked) {
			gp.ui.getDialogueBox().start("Aldric, l'ermite", () -> {
				alreadyTalked = true;
				gp.setFlag("ermite"); // Ouvre la barrière de la sortie de la forêt
			},
				"Ah ! Tu es enfin réveillé. Je t'ai trouvé ce matin, inconscient au milieu des fougères.",
				"Tu ne te souviens de rien ? C'est l'œuvre de la Brume des Murmures. Elle vole la mémoire de ceux qui se perdent dans ces bois.",
				"Tu portais une lettre sur toi. La brume l'a presque entièrement effacée... Voilà ce qu'on peut encore lire :",
				"« ...retrouver les éclats du Cristal d'Aube... avant que les ténèbres ne recouvrent Ardelune... »",
				"On raconte qu'un des éclats est tombé dans la Grotte aux Lueurs, sur le rivage, au sud-est d'ici.",
				"J'ai fermé la sortie de la forêt avec une barrière pour tenir les monstres à l'écart. Je viens de l'ouvrir pour toi : suis le sentier vers le sud-est.",
				"Avant de partir, va voir le coffre près de l'étang, à l'ouest. Et les vieilles ruines à l'est cachent une clé... si tu sais allumer leurs torches.",
				"Frappe les torches avec ton arme pour les allumer. Si tu te perds, appuie sur M pour regarder ta carte. Bonne chance, jeune voyageur.");
		} else {
			gp.ui.getDialogueBox().start("Aldric, l'ermite",
				"La barrière au sud-est est ouverte. Le rivage et la grotte t'attendent.",
				"N'oublie pas le coffre près de l'étang à l'ouest, et la clé des ruines à l'est ! (M pour la carte)");
		}
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		// Petite bulle "..." quand le joueur est tout près : on peut lui parler
		Entity p = gp.getPlayer();
		int dx = Math.abs(p.getWorldX() - worldX);
		int dy = Math.abs(p.getWorldY() - worldY);
		if (gp.getGameState() == gp.getPlayState() && dx < gp.getTileSize() * 2 && dy < gp.getTileSize() * 2) {
			int ts = gp.getTileSize();
			int bx = gp.worldToScreenX(worldX) + ts / 2;
			int by = gp.worldToScreenY(worldY) - ts / 3 + (int) (Math.sin(System.nanoTime() / 2e8) * 3);
			int w = ts * 2 / 3, h = ts * 2 / 5;
			g2.setColor(Color.WHITE);
			g2.fillRoundRect(bx, by - h, w, h, 10, 10);
			g2.setColor(Color.BLACK);
			g2.setStroke(new BasicStroke(2));
			g2.drawRoundRect(bx, by - h, w, h, 10, 10);
			for (int i = 0; i < 3; i++) {
				g2.fillOval(bx + w / 4 + i * w / 4 - 2, by - h / 2 - 2, 4, 4);
			}
		}
	}
}
