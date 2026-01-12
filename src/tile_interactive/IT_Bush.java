package tile_interactive;

import java.util.Random;

import main.GamePanel;
import object.OBJ_RubisGreen;

public class IT_Bush extends InteractiveTile{

	public IT_Bush(GamePanel gp, int col, int row) {
		super(gp, col, row);
		
		this.worldX = gp.getTileSize() * col;
		this.worldY = gp.getTileSize() * row;
		
		down1 = setup("/tiles_interactive/Bush", gp.getTileSize(), gp.getTileSize());
		destructible = true;
	}
	
	public void checkDrop() {
        int i = new Random().nextInt(100) + 1;
        if (i < 10) {
            dropItem(new OBJ_RubisGreen(gp));
        }
    }

}
