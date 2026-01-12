package tile_interactive;

import main.GamePanel;

public class IT_HeavyRock extends InteractiveTile{

	public IT_HeavyRock(GamePanel gp, int col, int row) {
		super(gp, col, row);
		
		this.worldX = gp.getTileSize() * col;
		this.worldY = gp.getTileSize() * row;
		
		down1 = setup("/tiles_interactive/HeavyRock", gp.getTileSize(), gp.getTileSize());
		destructible = false;
	}

}
