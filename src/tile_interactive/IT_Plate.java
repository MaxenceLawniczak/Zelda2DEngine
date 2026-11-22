package tile_interactive;

import java.awt.image.BufferedImage;

import main.GamePanel;
import main.Sprites;

/**
 * Dalle : s'enfonce quand un rocher est poussé dessus. Quand toutes les dalles
 * qui ont le même drapeau sont enfoncées, l'événement est accompli (une barrière s'ouvre).
 */
public class IT_Plate extends InteractiveTile {

	private final String flag;
	private boolean pressed = false;
	private final BufferedImage upImage, downImage;

	public IT_Plate(GamePanel gp, int col, int row, String flag) {
		super(gp, col, row);
		this.flag = flag;
		this.worldX = gp.getTileSize() * col;
		this.worldY = gp.getTileSize() * row;
		upImage = Sprites.plate(gp.getTileSize(), false);
		downImage = Sprites.plate(gp.getTileSize(), true);
		down1 = upImage;
		destructible = false;
		name = "Dalle";
	}

	@Override
	public boolean isSolid() {
		return false;
	}

	public boolean isPressed() {
		return pressed;
	}

	public String getFlag() {
		return flag;
	}

	@Override
	public void update() {
		boolean now = false;
		for (InteractiveTile t : gp.getItileTab()) {
			if (t instanceof IT_PushRock && !((IT_PushRock) t).isMoving() && t.getWorldX() == worldX && t.getWorldY() == worldY) {
				now = true;
				break;
			}
		}
		if (now == pressed) return;
		pressed = now;
		down1 = pressed ? downImage : upImage;

		// Toutes les dalles du même groupe sont enfoncées ?
		if (pressed && !gp.hasFlag(flag)) {
			for (InteractiveTile t : gp.getItileTab()) {
				if (t instanceof IT_Plate && ((IT_Plate) t).flag.equals(flag) && !((IT_Plate) t).pressed) return;
			}
			gp.setFlag(flag);
		}
	}
}
