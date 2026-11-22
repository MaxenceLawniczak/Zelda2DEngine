package tile_interactive;

import java.awt.Color;
import java.awt.image.BufferedImage;

import entity.Player;
import main.GamePanel;
import main.Sprites;

/**
 * Torche à allumer en la frappant avec son arme. Quand toutes les torches qui ont le même
 * drapeau sont allumées, l'événement est accompli. Avec un numéro d'ordre, il faut les allumer
 * dans le bon ordre (1, 2, 3...) : une erreur les éteint toutes.
 */
public class IT_Torch extends InteractiveTile {

	private final String flag;
	private final int order; // 0 = n'importe quel ordre
	private boolean lit = false;
	private int strikeCooldown = 0;
	private int animCounter = 0;
	private final BufferedImage[] frames = new BufferedImage[4];
	private final BufferedImage unlitImage;

	public IT_Torch(GamePanel gp, int col, int row, String flag, int order) {
		super(gp, col, row);
		this.flag = flag;
		this.order = order;
		this.worldX = gp.getTileSize() * col;
		this.worldY = gp.getTileSize() * row;
		for (int i = 0; i < 4; i++) frames[i] = setup("/tiles_interactive/lights/Light" + i, gp.getTileSize(), gp.getTileSize());
		unlitImage = Sprites.darker(frames[0], 0.35f);
		down1 = unlitImage;
		destructible = false;
		name = "Torche";
	}

	public boolean isLit() {
		return lit;
	}

	@Override
	public void onStrike(Player player) {
		if (lit || strikeCooldown > 0) return;
		strikeCooldown = 30; // Un seul effet par coup d'épée

		if (order > 0) {
			int expected = 1;
			for (InteractiveTile t : gp.getItileTab()) {
				if (t instanceof IT_Torch && ((IT_Torch) t).flag.equals(flag) && ((IT_Torch) t).lit) expected++;
			}
			if (order != expected) {
				// Mauvais ordre : tout s'éteint
				for (InteractiveTile t : gp.getItileTab()) {
					if (t instanceof IT_Torch && ((IT_Torch) t).flag.equals(flag)) ((IT_Torch) t).extinguish();
				}
				gp.addFloatingText("Les flammes s'éteignent...", worldX + gp.getTileSize() / 2, worldY, new Color(160, 180, 255));
				return;
			}
		}
		lit = true;
		gp.addFloatingText("Allumée !", worldX + gp.getTileSize() / 2, worldY, new Color(255, 190, 80));

		for (InteractiveTile t : gp.getItileTab()) {
			if (t instanceof IT_Torch && ((IT_Torch) t).flag.equals(flag) && !((IT_Torch) t).lit) return;
		}
		gp.setFlag(flag);
	}

	private void extinguish() {
		lit = false;
		down1 = unlitImage;
	}

	@Override
	public void update() {
		if (strikeCooldown > 0) strikeCooldown--;
		if (lit) {
			animCounter++;
			down1 = frames[(animCounter / 8) % 4];
		}
	}
}
