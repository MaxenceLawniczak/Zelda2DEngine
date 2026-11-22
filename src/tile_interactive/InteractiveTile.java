package tile_interactive;

import entity.Entity;
import main.GamePanel;

public class InteractiveTile extends Entity{

	protected boolean destructible;
	protected int col;
	protected int row;
	
	public InteractiveTile(GamePanel gp, int col, int row) {
		super(gp);
		this.col = col;
		this.row = row;
	}

	public void isCorrectItem() {}
	
	public void update() {}

	public boolean isDestructible() {
		return destructible;
	}

	/** Bloque-t-elle le passage ? (l'herbe et les dalles non) */
	public boolean isSolid() {
		return true;
	}

	/** Appelé quand le joueur frappe la tile avec son arme (torches...). */
	public void onStrike(entity.Player player) {}

	public int getCol() {
		return col;
	}

	public int getRow() {
		return row;
	}
	
}
