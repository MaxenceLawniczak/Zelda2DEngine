package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Door extends Entity{
	
	public OBJ_Door(GamePanel gp) {
		super(gp);
		
		name = "Door";
		down1 = setup("/objects/door", gp.getTileSize(),gp.getTileSize());
		collisionOn = true;
		
		solidArea.x = 0;
		solidArea.y = gp.getTileSize()/4;
		solidArea.width = gp.getTileSize();
		solidArea.height = 3*(gp.getTileSize()/4);
		solidAreaDefaultX = solidArea.x;
		solidAreaDefaultY = solidArea.y;
	}
}
