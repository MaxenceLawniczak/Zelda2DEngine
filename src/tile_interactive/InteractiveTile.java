package tile_interactive;

import java.util.Random;

import entity.Entity;
import main.GamePanel;
import object.OBJ_Heart;
import object.OBJ_RubisGreen;

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
	
}
