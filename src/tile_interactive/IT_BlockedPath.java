package tile_interactive;

import main.GamePanel;

public class IT_BlockedPath extends InteractiveTile{

	public IT_BlockedPath(GamePanel gp, int col, int row) {
		super(gp, col, row);
		
		this.worldX = gp.getTileSize() * col;
		this.worldY = gp.getTileSize() * row;
		
		down1 = setup("/tiles_interactive/BlockedPath", gp.getTileSize(), gp.getTileSize());
		destructible = true;
	}

}
