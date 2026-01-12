package tile_interactive;

import main.GamePanel;

public class IT_Rock extends InteractiveTile{

	public IT_Rock(GamePanel gp, int col, int row) {
		super(gp, col, row);
		
		this.worldX = gp.getTileSize() * col;
		this.worldY = gp.getTileSize() * row;
		
		down1 = setup("/tiles_interactive/Rock", gp.getTileSize(), gp.getTileSize());
		destructible = false;
	}

}
